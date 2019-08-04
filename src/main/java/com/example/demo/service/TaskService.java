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

import java.io.File;
import java.io.IOException;
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
        //储存文件夹不存在则创建
        File dir =new File( ParameterConfiguration.FilePath.finalSave);
        if  (!dir .exists()&&!dir .isDirectory()) {
            dir .mkdirs();
        }
        if(type.equals( ParameterConfiguration.Type.video )){

        }else if(type.equals( ParameterConfiguration.Type.image )){
            imgProcessing.ProcessSinglePic( ParameterConfiguration.FilePath.uploadSava+"/"+fileName,
                    ParameterConfiguration.FilePath.finalSave+"/"+fileName,
                    imageStyle2Value.get(imsId.toString()),clarity );
        }else if(type.equals( ParameterConfiguration.Type.audio )){
            //ausId需要用ausId.toString,不用int不会自动转化string，只会返回null
            //todo:mp3 to wav待写
            AudioProcessing.changePitch(ParameterConfiguration.FilePath.uploadSava+"/"+fileName,
                    ParameterConfiguration.FilePath.finalSave+"/"+fileName,audioStyle2Value.get(ausId.toString()) );
        }
    }


}
