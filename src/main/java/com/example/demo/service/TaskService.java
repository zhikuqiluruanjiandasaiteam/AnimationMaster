package com.example.demo.service;

import ch.ethz.ssh2.Connection;
import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.AudioStyleMapper;
import com.example.demo.dao.ImageStyleMapper;
import com.example.demo.dao.TaskMapper;
import com.example.demo.entity.AudioStyle;
import com.example.demo.entity.ImageStyle;
import com.example.demo.entity.Task;
import com.example.demo.util.AudioProcessing;
import com.example.demo.util.PatchFrameUtil;
import com.example.demo.util.VideoProcessing;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ImgProcessing imgProcessing;
    @Autowired
    private ImageStyleMapper imageStyleMapper;
    @Autowired
    private AudioStyleMapper audioStyleMapper;

    private long imsEstimatedTime;//处理图片耗时（微秒）
    private long ausEstimatedTime;//处理音频耗时（微秒）
    private int patchFrameNone;//使用补帧时直接通过风格转换未补帧的帧数
    private long patchFrameTime;//使用补帧时补帧所耗费的时长（微秒）
    private int numWidth=6;//中间文件数字编号宽度

    List<String> outNames;
    /**
     * 创建任务（仅创建记录，未开启任务）
     * @param userId 用户id
     * @param fileId 文件id
     * @param imsId 使用图像风格
     * @param ausId 使用音频风格
     * @param clarity 清晰度
     * @param estimateTime 预计耗时
     * @param isFrameSpeed 是否采用补帧加速
     * @param taskType 任务类型
     * @return 任务对象（带自增id值）
     */
    public Task createTask(Integer userId,Integer fileId,Integer imsId,Integer ausId,Integer clarity,Integer estimateTime,
                           Boolean isFrameSpeed,String taskType){
        Task task=new Task();
        task.setUserId( userId );
        task.setImsId( imsId );
        task.setAusId( ausId );
        task.setClarity( clarity );
        task.setEstimateTime( estimateTime );
        task.setFileId( fileId );
        task.setIsFrameSpeed( isFrameSpeed );
        task.setTaskType( taskType );
        task.setCreateTime( new Date(  ) );
        taskMapper.insertGetId( task );
        return task;
    }

    /**
     * 获得任务列表
     * @param userId 用户id
     * @return 任务列表
     */
    public Map[] getList(int userId,boolean isDesc,boolean isAll,boolean isFinish){
        List<Task> list=taskMapper.selectsByUserId( userId ,isDesc,isAll,isFinish);
        Map[] maps=new HashMap[list.size()];
        int i=0;
        for (Task task : list) {
            Map<String,Object> map=new HashMap<>();
            map.put("task_id",task.getTaskId());
            map.put("task_type",task.getTaskType());
            map.put("ims_id",task.getImsId());
            map.put("aus_id",task.getAusId());
            map.put("clarity",task.getClarity());
            map.put("is_frame_speed",task.getIsFrameSpeed());
            map.put("estimate_time",task.getEstimateTime());
            map.put("file_id",task.getFileId());
            map.put("start_time",task.getStartTime());
            map.put("final_time",task.getFinishTime());
            map.put("create_time",task.getCreateTime());
            maps[i++]=map;
        }

        return maps;
    }

    /**
     * 开启任务
     * @param fileName 储存所用文件名
     * @param task 任务对象
     */
    public void startTask(String fileName,Task task) {
        new Thread(  ){
            @Override
            public void run() {
                super.run();
                //储存文件夹不存在则创建
                newFolder(ParameterConfiguration.FilePath.finalSave);
                newFolder(ParameterConfiguration.FilePath.intermediateSave);
                assert task != null;
                switch (task.getTaskType()) {
                    case ParameterConfiguration.Type.video:
                        initialRecord( task );
                        if(runVideo(fileName,task)){
                            finishRecord( task,fileName );
                        }
                        break;
                    case ParameterConfiguration.Type.image:
                        initialRecord( task );
                        if (runImage( fileName, task.getImsId(), task.getClarity(), task.getTaskId() )) {
                            finishRecord( task, fileName );
                        }
                        break;
                    case ParameterConfiguration.Type.audio:
                        initialRecord( task );
                        if (runAudio( fileName, task.getAusId() )) {
                            finishRecord( task, fileName );
                        }
                        break;
                }
            }
        }.start();
    }

    /**
     * 任务完成后处理
     */
    private void finishRecord(Task task,String fileName) {
        if(task==null)
            return;
        task.setFinishTime( new Date(  ) );
        taskMapper.updateByPrimaryKey( task );
        //调整风格平均耗时//java中date精确到毫秒，mysql中只精确到秒，不能从数据库中查询后做差
        long spendingTime=task.getFinishTime().getTime()-task.getStartTime().getTime();
        spendingTime*=1000;//数据库中储存微秒级
        switch (task.getTaskType()) {
            case ParameterConfiguration.Type.video:
                int[] videoInfo=VideoProcessing.getVideoInfo( ParameterConfiguration.FilePath.finalSave
                        +File.separator+fileName );
                //音频
                AudioStyle audioStyle0 = audioStyleMapper.selectByPrimaryKey( task.getAusId() );
                long averageTime=ausEstimatedTime/videoInfo[3];//平均耗时
                int nowet0 = (audioStyle0.getAusUsedCount() * audioStyle0.getAusEstimatedTime() + (int)averageTime) /
                        (audioStyle0.getAusUsedCount() + 1);
                audioStyle0.setAusUsedCount( audioStyle0.getAusUsedCount() + 1 );
                audioStyle0.setAusEstimatedTime( nowet0 );
                audioStyleMapper.updateByPrimaryKey( audioStyle0 );
                //图像
                ImageStyle imageStyle0=imageStyleMapper.selectByPrimaryKey( task.getImsId() );
                if(task.getIsFrameSpeed()){
                    //更新补帧变量值
                    Map map=getPatchFrameInfo();
                    int estimated_time=Integer.parseInt( (String) map.get("estimated_time"));
                    int used_count=Integer.parseInt( (String) map.get("used_count"));
                    float frame_patch_rate=Float.parseFloat( (String) map.get("frame_patch_rate"));
                    averageTime=patchFrameTime/((long)videoInfo[0]*videoInfo[1]*(videoInfo[2]-patchFrameNone));
                    estimated_time=(int)((averageTime+estimated_time*used_count)/(used_count+1));
                    float fpr=(float)1.0*(videoInfo[2]-patchFrameNone)/videoInfo[2];
                    frame_patch_rate=(fpr+frame_patch_rate*used_count)/(used_count+1);
                    used_count+=1;
                    map.put("estimated_time",estimated_time);
                    map.put("used_count",used_count);
                    map.put("frame_patch_rate",frame_patch_rate);
                    setPatchFrameInfo( map );

                    averageTime=((imsEstimatedTime-patchFrameTime)/((long)videoInfo[0]*videoInfo[1]*patchFrameNone));
                }else{
                    averageTime=(imsEstimatedTime/((long)videoInfo[0]*videoInfo[1]*videoInfo[2]));
                }
                nowet0 = (int)((averageTime+imageStyle0.getImsUsedCount() * imageStyle0.getImsEstimatedTime()) /
                        (imageStyle0.getImsUsedCount() + 1));
                imageStyle0.setImsUsedCount( imageStyle0.getImsUsedCount()+1 );
                imageStyle0.setImsEstimatedTime( nowet0 );
                imageStyleMapper.updateByPrimaryKey( imageStyle0 );
                break;
            case ParameterConfiguration.Type.image:
                int[] pixel=getImgPixel(ParameterConfiguration.FilePath.finalSave+File.separator+fileName);
                if(pixel==null)
                    break;
                ImageStyle imageStyle=imageStyleMapper.selectByPrimaryKey( task.getImsId() );
                int estimatedTime1 = (int) (spendingTime / (pixel[0]*pixel[1]));
                int nowet1 = (imageStyle.getImsUsedCount() * imageStyle.getImsEstimatedTime() + estimatedTime1) /
                        (imageStyle.getImsUsedCount() + 1);
                imageStyle.setImsEstimatedTime( nowet1 );
                imageStyle.setImsUsedCount( imageStyle.getImsUsedCount()+1 );
                imageStyleMapper.updateByPrimaryKey( imageStyle );
                break;
            case ParameterConfiguration.Type.audio:
                int estimatedTime = (int) (spendingTime / getDuration( ParameterConfiguration.FilePath.uploadSave
                        + File.separator + fileName ));
                AudioStyle audioStyle = audioStyleMapper.selectByPrimaryKey( task.getAusId() );
                int nowet = (audioStyle.getAusUsedCount() * audioStyle.getAusEstimatedTime() + estimatedTime) /
                        (audioStyle.getAusUsedCount() + 1);
                audioStyle.setAusEstimatedTime( nowet );
                audioStyle.setAusUsedCount( audioStyle.getAusUsedCount() + 1 );
                audioStyleMapper.updateByPrimaryKey( audioStyle );
                break;
        }
        //todo:删除中间文件，节省储存空间
    }

    /**
     * 任务开始前处理
     */
    private void initialRecord(Task task){
        task.setStartTime( new Date(  ) );
    }

    //todo:未完
    private boolean runVideo(String fileName,Task task) {
        //创建文件夹
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        String intermediatePath=ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName;
        newFolder( intermediatePath);
        intermediatePath+=File.separator;

        String imsParameterValues= imageStyleMapper.selectByPrimaryKey( task.getImsId() ).getImsParameterValues();
        String ausParameterValues= audioStyleMapper.selectByPrimaryKey( task.getAusId() ).getAusParameterValues();
        if(imsParameterValues==null&&ausParameterValues==null){//原画原声
            long time1=System.nanoTime();
            copyFile(ParameterConfiguration.FilePath.uploadSave +File.separator+fileName+" "
                    ,ParameterConfiguration.FilePath.finalSave+File.separator+fileName);
            long wtime=(System.nanoTime()-time1)/1000;
            imsEstimatedTime=ausEstimatedTime=wtime;
        }else if(imsParameterValues==null){//原画非原声
            splitVideoAudio( fileName, false);//拆音频
            long imsStartTime =System.currentTimeMillis();
            splitVideoImage( fileName, true ,false);//拆图片，不能和拆音频同时，会强占文件
            imsEstimatedTime =(System.currentTimeMillis()-imsStartTime)*1000;
            //转音频
            long time1=System.nanoTime();
            String fromFile=intermediatePath+ParameterConfiguration.FilePath.vidoe_audio
                    +File.separator+fileFrontName+".wav";
            AudioProcessing.changePitch( fromFile,
                    intermediatePath+fileFrontName+".wav",
                    Integer.parseInt( ausParameterValues ));
            ausEstimatedTime+=(System.nanoTime()-time1)/1000;

        }else{//非原画
            String finalIntermediatePath = intermediatePath;
            long ausStartTime =System.currentTimeMillis();
            if(ausParameterValues==null){
                splitVideoAudio( fileName,true );//拆音频，不能和拆图片同时，会强占文件
            }else {
                splitVideoAudio( fileName, false );
            }
            splitVideoImage(fileName,false ,task.getIsFrameSpeed());//拆图片
            new Thread(  ) {//线程中处理音频
                @Override
                public void run() {
                    super.run();
                    if(ausParameterValues!=null){
                        //转化音频
                        AudioProcessing.changePitch( finalIntermediatePath +ParameterConfiguration.FilePath.vidoe_audio+
                                        File.separator+fileFrontName+".wav",
                                finalIntermediatePath +fileFrontName+".wav",
                                Integer.parseInt( ausParameterValues ));
                    }
                    ausEstimatedTime =(System.currentTimeMillis()-ausStartTime)*1000;
                }
            }.start();
            //todo:调试
            if(task.getIsFrameSpeed()){
                long time1=System.nanoTime();
                patchFrameNone=0;
                patchFrameTime=0;
                if(outNames==null)
                    return false;
                Connection connection=imgProcessing.Login();
                try {
                    imgProcessing.ProcessSinglePicOnceConn(intermediatePath+ParameterConfiguration.FilePath.video_ImagesForm
                                    +File.separator+outNames.get( 0 ),
                            intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo,
                            outNames.get( 0 ),imsParameterValues,task.getClarity() ,connection);//todo:有待改为登陆分离版
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(outNames.size()>1){
                    PatchFrameUtil patchFrameUtil=new PatchFrameUtil( "",numWidth,".jpg" ,
                            intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo);
                    int len=outNames.size();
                    boolean isRuning=false;
                    for(int i=1;i<len;i++){
                        String name=outNames.get( i );
                        try {
                            imgProcessing.ProcessSinglePicOnceConn(intermediatePath+ParameterConfiguration.FilePath.video_ImagesForm
                                            +File.separator+name,
                                    intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo,
                                    name,imsParameterValues,task.getClarity(),connection );//todo:有待改为登陆分离版
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        int startNum=Integer.parseInt((outNames.get( i-1 ).split( "\\." ))[0]);
                        int endNum=Integer.parseInt(name.split( "\\." )[0]);//使用的是正则表达式不能只写.
                        startNum+=1;
                        if(endNum-startNum<1){
                            continue;
                        }
                        long timepf=System.nanoTime();
                        patchFrameUtil.add(startNum,endNum-startNum,
                                intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo+File.separator+outNames.get( i-1 ),
                                intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo+name);
                        if(!isRuning){
                            patchFrameUtil.run();
                            isRuning=true;
                        }
                        patchFrameNone+=endNum-startNum;
                        patchFrameTime+=(System.nanoTime()-timepf)/1000;
                    }
                    patchFrameUtil.close();
                }
                imgProcessing.closeConnect( connection );
                VideoProcessing.images2Video( intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo,
                        "",numWidth,".jpg",intermediatePath+fileFrontName+".mp4",
                        ParameterConfiguration.FilePath.uploadSave+File.separator+fileName );
                imsEstimatedTime=(System.nanoTime()-time1)/1000;
            }else{
                long time1=System.nanoTime();
                try {
                    imgProcessing.ProcessSingleDir(
                            intermediatePath+ParameterConfiguration.FilePath.video_ImagesForm,
                            intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo,
                            imsParameterValues,task.getClarity());
                }catch (Exception e){
                    e.printStackTrace();
                }
                VideoProcessing.images2Video( intermediatePath+ParameterConfiguration.FilePath.vidoe_ImagesTo,
                        "",numWidth,"_"+imsParameterValues+".jpg",intermediatePath+fileFrontName+".mp4",
                        ParameterConfiguration.FilePath.uploadSave+File.separator+fileName );
                imsEstimatedTime=(System.nanoTime()-time1)/1000;
            }
        }
        //合并视频音频

        VideoProcessing.videoAddAudio( intermediatePath+fileFrontName+".mp4",
                intermediatePath+fileFrontName+".wav",
                ParameterConfiguration.FilePath.finalSave+File.separator+fileFrontName+".mp4");

        return true;
    }

    private boolean runImage(String fileName,Integer imsId,int clarity,int taskId){
        String parameterValues= imageStyleMapper.selectByPrimaryKey( imsId ).getImsParameterValues();
        if(parameterValues==null){//原画
            String fromPath=ParameterConfiguration.FilePath.uploadSave +File.separator+fileName;
            String toPath=ParameterConfiguration.FilePath.finalSave+File.separator+fileName;
            int [] pixel=getImgPixel( fromPath );
            assert pixel != null;
            if(pixel[0]>pixel[1]){
                pixel[1]=(int)(pixel[1]*(1.0*clarity/pixel[0]));
                pixel[0]=clarity;
                if(pixel[1]<=0)
                    pixel[1]=1;
            }else{
                pixel[0]=(int)(pixel[0]*(1.0*clarity/pixel[1]));
                pixel[1]=clarity;
                if(pixel[0]<=0)
                    pixel[0]=1;
            }
            return changeImageSize(fromPath,pixel[0],pixel[1],toPath);
        }
        Connection connection = null;
        try {

//            connection=imgProcessing.ProcessSinglePicLogin();
            imgProcessing.ProcessSinglePic( ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,
                    ParameterConfiguration.FilePath.finalSave,fileName,
                    parameterValues,clarity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
//        finally {
            //关闭连接
//            imgProcessing.closeConnect( connection );
//        }
        return true;
    }

    private boolean runAudio(String fileName,Integer ausId){
        String parameterValues= audioStyleMapper.selectByPrimaryKey( ausId ).getAusParameterValues();
        if(parameterValues==null){
            copyFile(ParameterConfiguration.FilePath.uploadSave +File.separator+fileName+" "
                    ,ParameterConfiguration.FilePath.finalSave+File.separator+fileName);
            return true;
        }
        String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        String intermedPath=ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName;
        newFolder(intermedPath);

        intermedPath+=File.separator;
        //转音频为wav
        int state=AudioProcessing.file2Wav( suffix,
                ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,
                "wav",intermedPath+fileFrontName+".wav");
        if(state!=0)
            return false;

        //改变音调// ausId需要用ausId.toString,不用int不会自动转化string，只会返回null
        state=AudioProcessing.changePitch(intermedPath+fileFrontName+".wav",
                intermedPath+fileFrontName+"_out.wav",
                Integer.parseInt( parameterValues ));
        if(state!=0)
            return false;
        //转wav为原类型System.out.println(filesService.getUniqueStr());
        AudioProcessing.file2Wav( "wav", intermedPath+fileFrontName+"_out.wav",
                suffix,ParameterConfiguration.FilePath.finalSave+File.separator+fileName);
        return true;
    }

    /**
     * 音频文件获取文件时长
     * @param source 文件地址
     * @return 时长/秒
     */
    public static long getDuration(String source) {
        File file=new File( source );
        Encoder encoder = new Encoder();
        long ls = 0;
        MultimediaInfo m;
        try {
            m = encoder.getInfo(file);
            ls = m.getDuration()/1000;
        } catch (Exception e) {
            System.out.println("获取音频时长有误：" + e.getMessage());
        }
        return ls;
    }

    //创建文件夹
    private void newFolder(String path){
        File dir =new File( path);
        if  (!dir .exists()&&!dir .isDirectory()) {
            dir.mkdirs();
        }
    }

    /**
     * 获取图片像素
     * @param path 图片路径
     * @return
     */
    private int[] getImgPixel(String path) {
        File file = new File( path);//读取文件路径
        BufferedImage bi = null;
        try {
            bi = ImageIO.read( file );
        }catch (IOException e){
            e.printStackTrace();
        }
        if(bi==null)
            return null;
        int width = bi.getWidth();
        int height = bi.getHeight();
        return new int[]{width, height};
    }

    /**
     * 改变图片的尺寸
     * @param fromPath
     * @param newWidth
     * @param newHeight
     * @param toPath
     * @return
     */
    private boolean changeImageSize(String fromPath,int newWidth, int newHeight, String toPath) {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(fromPath));
            //字节流转图片对象
            Image bi = ImageIO.read(in);
            //构建图片流
            BufferedImage tag = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            //绘制改变尺寸后的图
            tag.getGraphics().drawImage(bi, 0, 0, newWidth, newHeight, null);
            //输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(toPath));
            //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            //encoder.encode(tag);
            String formName="PNG";
            String suffix = fromPath.substring(fromPath.lastIndexOf('.')+1);//文件后缀
            if(suffix.equalsIgnoreCase( "jpg" )){
                formName="JPEG";
            }else if(suffix.equalsIgnoreCase( "gif" )){
                formName="GIF";
            }
            ImageIO.write(tag,formName,out);
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void copyFile(String formFile,String toFile){
        String ml="sudo cp -f";
        if( System.getProperty("os.name").toLowerCase().startsWith("win")){//判断操作系统
            ml="cmd.exe /c copy /y";//windoes,执行命令前要加“cmd.exe /c”
        }
        String shell=ml+" "+formFile+" "+toFile;
        AudioProcessing.runExec( shell );
    }

    //拆分视频出图片
    private void splitVideoImage(String fileName, boolean isOriginalIms ,boolean ispFrame){
//        String fileSuffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        if(isOriginalIms){
            //取出
            VideoProcessing.removeVideoSound( ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,
                    ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName+
                            File.separator+fileFrontName+".mp4");
        }else{
            String toPath=ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName+
                    File.separator;
            newFolder( toPath+ParameterConfiguration.FilePath.video_ImagesForm );
            newFolder( toPath+ParameterConfiguration.FilePath.vidoe_ImagesTo );
            toPath=toPath+ParameterConfiguration.FilePath.video_ImagesForm;
            //拆分视频为图片
            if(ispFrame){
                outNames=VideoProcessing.video2ImagesPf(ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,
                        toPath,numWidth,ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName);
            }else{
                VideoProcessing.video2Images(ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,
                        toPath,numWidth);
            }

        }
    }

    //拆分视频出音频
    private void splitVideoAudio(String fileName, boolean isOriginalAus){
        String fileSuffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        String toFile=ParameterConfiguration.FilePath.intermediateSave+File.separator+
                fileFrontName+File.separator;
        if(isOriginalAus){
            toFile+=fileFrontName+".wav";
        }else{
            toFile+=ParameterConfiguration.FilePath.vidoe_audio;
            newFolder( toFile );
            toFile+=File.separator+fileFrontName+".wav";
        }
        AudioProcessing.file2Wav( fileSuffix,ParameterConfiguration.FilePath.uploadSave +File.separator+fileName,"wav",toFile);
    }

    public static Map<String,Object> getPatchFrameInfo(){
        String filePath=ParameterConfiguration.Tools.rootPath +File.separator+"PatchFrameInfo.json";
        Map map=null;
        try {
            map=new HashMap(  );
            File file = new File(filePath);
            String jsonString = FileUtils.readFileToString(file);
//            System.out.println(jsonString);//////////////////
            jsonString=jsonString.replaceAll("[\\t\\n\\r{}\" ]","" );
//            System.out.println( "替换后1：\n"+jsonString );///////////////////////
//            jsonString=jsonString.replace( "}","" );
//            System.out.println( "替换后2：\n"+jsonString );///////////////////////
//            jsonString=jsonString.replace( " ","" );
//            System.out.println( "替换后3：\n"+jsonString );///////////////////////
            jsonString=jsonString.replace( ",",":" );
//            System.out.println( "替换后4：\n"+jsonString );///////////////////////
//            jsonString=jsonString.replace( "\n","" );
//            System.out.println( "替换后5：\n"+jsonString );///////////////////////
//            jsonString=jsonString.replace( "\"","" );
//            System.out.println( "替换后6：\n"+jsonString );///////////////////////
            String[] strs=jsonString.split( ":" );
            System.out.println( Arrays.toString( strs ) );///////////////////////
            map.put(strs[0],strs[1]);
            map.put(strs[2],strs[3]);
            map.put(strs[4],strs[5]);
        }catch (Exception e){
            e.printStackTrace();
        }
        assert map != null;
        System.out.println(map.toString());//////////////////
        return map;
    }

    private void setPatchFrameInfo(Map<String,Object> map){
        String filePath=ParameterConfiguration.Tools.rootPath +File.separator+"PatchFrameInfo.json";
        try {
            Writer writer = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(writer);
            String jsonStr="{";
            Set set = map.keySet();
            for(Iterator iter = set.iterator(); iter.hasNext();) {
                String key = (String)iter.next();
                String value = (String)map.get(key);
                jsonStr+=key+":"+value+",";
            }
            jsonStr+="}";
            bw.write(jsonStr);
            // 注意这两个关闭的顺序
            bw.close();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//
//    public static  void test(Integer a,Boolean b){
//        System.out.print( a );
//        System.out.print( b );
//    }
//    public static void main(String[] s){
//        test(null,null);
//    }

}
