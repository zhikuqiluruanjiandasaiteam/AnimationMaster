package com.example.demo.service;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.AudioStyleMapper;
import com.example.demo.dao.ImageStyleMapper;
import com.example.demo.dao.TaskMapper;
import com.example.demo.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

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
        String parameterValues= imageStyleMapper.selectByPrimaryKey( imsId ).getImsParameterValues();
        System.out.println("<<<<<<<<<<<<<");
        System.out.println(parameterValues);
        System.out.println(">>>>>>>>>>>>>>");
        if(parameterValues==null){
            String ml="cp -f";
            if( System.getProperty("os.name").toLowerCase().startsWith("win")){//判断操作系统
                ml="copy /y";
            }
            String shell=ml+" "+ParameterConfiguration.FilePath.uploadSava+File.separator+fileName+" "
                    +ParameterConfiguration.FilePath.finalSave+File.separator+fileName;
            System.out.println(shell);/////////////////
            AudioProcessing.runExec( shell );
            return;
        }
        try {
            imgProcessing.ProcessSinglePic( ParameterConfiguration.FilePath.uploadSava+File.separator+fileName,
                    ParameterConfiguration.FilePath.finalSave+File.separator+fileName,
                    parameterValues,clarity ,taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runAudio(String fileName,Integer ausId){
        String parameterValues= audioStyleMapper.selectByPrimaryKey( ausId ).getAusParameterValues();
        if(parameterValues==null){
            String ml="cp";
            if( System.getProperty("os.name").toLowerCase().startsWith("win")){//判断操作系统
                ml="copy";
            }
            String shell=ml+" "+ParameterConfiguration.FilePath.uploadSava+File.separator+fileName+" "
                    +ParameterConfiguration.FilePath.finalSave+File.separator+fileName;
            AudioProcessing.runExec( shell );
            return;
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
            return;
        //改变音调

        // ausId需要用ausId.toString,不用int不会自动转化string，只会返回null
        state=AudioProcessing.changePitch(intermedPath+fileFrontName+".wav",
                intermedPath+fileFrontName+"_out.wav",
                Integer.parseInt( parameterValues ));
        if(state!=0)
            return;
        //转wav为原类型System.out.println(filesService.getUniqueStr());
        AudioProcessing.file2Wav( "wav", intermedPath+fileFrontName+"_out.wav",
                suffix,ParameterConfiguration.FilePath.finalSave+File.separator+fileName);
    }

    //创建文件夹
    private void newFolder(String path){
        File dir =new File( path);
        if  (!dir .exists()&&!dir .isDirectory()) {
            dir.mkdirs();
        }
    }

}
