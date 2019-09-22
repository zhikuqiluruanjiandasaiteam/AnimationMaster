package com.example.demo.dao;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskExt;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TaskMapper {
    int deleteByPrimaryKey(Integer taskId);

    int insert(Task record);

    int insertSelective(Task record);

    Task selectByPrimaryKey(Integer taskId);

    int updateByPrimaryKeySelective(Task record);

    int updateByPrimaryKey(Task record);

    int insertGetId(Task record);

    List<Task> selectsByUserId(@Param( "userId" ) Integer userId,
                               @Param( "isDesc" ) boolean isDesc,
                               @Param( "isAll" ) boolean isAll,
                               @Param( "isFinish" ) boolean isFinish);

    List<TaskExt> selectsExtByUserId(@Param( "userId" ) Integer userId,
                                     @Param( "isDesc" ) boolean isDesc,
                                     @Param( "isAll" ) boolean isAll,
                                     @Param( "isFinish" ) boolean isFinish);
}