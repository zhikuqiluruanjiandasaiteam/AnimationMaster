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

    @Value("${DLServerIP}")
    private String DLServerIP;

    @Value("${DLServerUserName}")
    private String DLServerUserName;

    @Value("${DLServerPassword}")
    private String DLServerPassword;

    @Value("${DLServerSecretKey}")
    private String DLServerSecretKey;

    @Value("${ExecShell}")
    private String ExecShell;

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


}
