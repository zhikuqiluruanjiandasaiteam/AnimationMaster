package com.example.demo.service;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.AudioStyleMapper;
import com.example.demo.dao.ImageStyleMapper;
import com.example.demo.dao.TaskMapper;
import com.example.demo.entity.AudioStyle;
import com.example.demo.entity.ImageStyle;
import com.example.demo.entity.Task;
import com.example.demo.util.AudioProcessing;
import com.example.demo.util.VideoProcessing;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;
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
    //todo:还未完全测试
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
                        runVideo(fileName,task);
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
    private void finishRecord(Task task,String fileName){
        if(task==null)
            return;
        task.setFinishTime( new Date(  ) );
        //调整风格平均耗时//java中date精确到毫秒，mysql中只精确到秒，不能从数据库中查询后做差
        long spendingTime=task.getFinishTime().getTime()-task.getStartTime().getTime();
        spendingTime*=1000;//数据库中储存微秒级
        switch (task.getTaskType()) {
            case ParameterConfiguration.Type.video:

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
                int estimatedTime = (int) (spendingTime / getDuration( ParameterConfiguration.FilePath.uploadSava
                        + File.separator + fileName ));
                AudioStyle audioStyle = audioStyleMapper.selectByPrimaryKey( task.getAusId() );
                int nowet = (audioStyle.getAusUsedCount() * audioStyle.getAusEstimatedTime() + estimatedTime) /
                        (audioStyle.getAusUsedCount() + 1);
                audioStyle.setAusEstimatedTime( nowet );
                audioStyle.setAusUsedCount( audioStyle.getAusUsedCount() + 1 );
                audioStyleMapper.updateByPrimaryKey( audioStyle );
                break;
        }
        taskMapper.updateByPrimaryKey( task );

        //todo:删除中间文件，节省储存空间
    }

    /**
     * 任务开始前处理
     */
    private void initialRecord(Task task){
        task.setStartTime( new Date(  ) );
    }

    private boolean runVideo(String fileName,Task task){
        String fileSuffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        //拆分视频为图片
        VideoProcessing.video2images(ParameterConfiguration.FilePath.uploadSava+File.separator+fileName,
                ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName+
                        File.separator+ParameterConfiguration.FilePath.video_ImagesForm,7);
        return false;
    }

    private boolean runImage(String fileName,Integer imsId,int clarity,int taskId){
        String parameterValues= imageStyleMapper.selectByPrimaryKey( imsId ).getImsParameterValues();
        if(parameterValues==null){//原画
            String fromPath=ParameterConfiguration.FilePath.uploadSava+File.separator+fileName;
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
        try {
            imgProcessing.ProcessSinglePic( ParameterConfiguration.FilePath.uploadSava+File.separator+fileName,
                    ParameterConfiguration.FilePath.finalSave+File.separator+fileName,
                    parameterValues,clarity ,taskId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean runAudio(String fileName,Integer ausId){
        String parameterValues= audioStyleMapper.selectByPrimaryKey( ausId ).getAusParameterValues();
        if(parameterValues==null){
            String ml="sudo cp -f";
            if( System.getProperty("os.name").toLowerCase().startsWith("win")){//判断操作系统
                ml="cmd.exe /c copy /y";//windoes,执行命令前要加“cmd.exe /c”
            }
            String shell=ml+" "+ParameterConfiguration.FilePath.uploadSava+File.separator+fileName+" "
                    +ParameterConfiguration.FilePath.finalSave+File.separator+fileName;
            AudioProcessing.runExec( shell );
            return true;
        }
        String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        String intermedPath=ParameterConfiguration.FilePath.intermediateSave+File.separator+fileFrontName;
        newFolder(intermedPath);
        intermedPath+=File.separator;
        //转音频为wav
        int state=AudioProcessing.file2Wav( suffix,
                ParameterConfiguration.FilePath.uploadSava+File.separator+fileName,
                "wav",intermedPath+fileFrontName+".wav");
        if(state!=0)
            return false;
        //改变音调

        // ausId需要用ausId.toString,不用int不会自动转化string，只会返回null
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
    private int[] getImgPixel(String path){
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
}
