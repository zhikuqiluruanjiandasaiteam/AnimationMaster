package com.example.demo.service;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.FilesMapper;
import com.example.demo.dao.TaskMapper;
import com.example.demo.entity.Files;
import com.example.demo.entity.Task;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;

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


}
