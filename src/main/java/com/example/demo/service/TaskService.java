package com.example.demo.service;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.FilesMapper;
import com.example.demo.dao.TaskMapper;
import com.example.demo.entity.Files;
import com.example.demo.entity.Task;
import com.example.demo.util.RemoteShellExecutor;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ImgProcessing imgProcessing;

    @Value(value = "#{${typle2Value.audio}}")
    private Map<String,Integer> audioStyle2Value;
    @Value(value = "#{${typle2Value.image}}")
    private Map<String,String> imageStyle2Value;



    /**
     * 创建任务（仅创建记录，未开启任务）
     * @param userId
     * @param fileId
     * @param imsId
     * @param ausId
     * @param clarity
     * @param estimateTime
     * @param isFrameSpeed
     * @param taskType
     * @return 任务id
     */
    public Integer createTask(Integer userId,Integer fileId,Integer imsId,Integer ausId,Integer clarity,Integer estimateTime,
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
        return task.getTaskId();
    }

    /**
     * 获得任务列表
     * @param userId 用户id
     * @return 任务列表
     */
    public Map[] getList(int userId){
        List<Task> list=taskMapper.selectsByUserId( userId );
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
     * @param fileName
     * @param taskId
     * @param type
     * @param imsId
     * @param ausId
     * @param clarity
     * @param is
     * @throws Exception
     */
    //todo:还未完全测试
    public void startTask(String fileName,int taskId,String type,
                          Integer imsId,Integer ausId,Integer clarity,boolean is) throws Exception {
        new Thread(  ){
            @Override
            public void run() {
                super.run();
                //储存文件夹不存在则创建
                newFolder(ParameterConfiguration.FilePath.finalSave);
                newFolder(ParameterConfiguration.FilePath.intermediateSave);
                if(type.equals( ParameterConfiguration.Type.video )){
                    runVideo();;
                }else if(type.equals( ParameterConfiguration.Type.image )){
                    runImage(fileName,imsId,clarity,taskId);
                }else if(type.equals( ParameterConfiguration.Type.audio )){
                    runAudio(fileName,ausId);
                }
            }
        }.start();
    }
    private void runVideo(){

    }
    private void runImage(String fileName,Integer imsId,int clarity,int taskId){
        try {
            imgProcessing.ProcessSinglePic( ParameterConfiguration.FilePath.uploadSava+"/"+fileName,
                    ParameterConfiguration.FilePath.finalSave+"/"+fileName,
                    imageStyle2Value.get(imsId.toString()),clarity ,taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runAudio(String fileName,Integer ausId){
        String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String fileFrontName = fileName.substring(0,fileName.lastIndexOf('.'));
        String intermedPath=ParameterConfiguration.FilePath.intermediateSave+"/"+fileFrontName;
        newFolder(intermedPath);
        intermedPath+="/";
        FilesService filesService=new FilesService();////////////////////
        System.out.println(filesService.getUniqueStr());
        //转音频为wav
        Process process=AudioProcessing.audio2Wav( suffix,
                ParameterConfiguration.FilePath.uploadSava+"/"+fileName,
                "wav",intermedPath+fileFrontName+".wav");
        System.out.println(filesService.getUniqueStr());
        //取得命令结果的输出流
        InputStream fis=process.getInputStream();
        System.out.println(filesService.getUniqueStr());
        //改变音调
        // ausId需要用ausId.toString,不用int不会自动转化string，只会返回null
        AudioProcessing.changePitch(intermedPath+fileFrontName+".wav",
                intermedPath+fileFrontName+"_out.wav",
                audioStyle2Value.get(ausId.toString()) );
        System.out.println(filesService.getUniqueStr());
        //转wav为原类型
        AudioProcessing.audio2Wav( "wav", intermedPath+fileFrontName+"_out.wav",
                suffix,ParameterConfiguration.FilePath.finalSave+"/"+fileName);

    }

    //创建文件夹
    private void newFolder(String path){
        File dir =new File( path);
        if  (!dir .exists()&&!dir .isDirectory()) {
            dir .mkdirs();
        }
    }

}
