package com.example.demo.dao;

import com.example.demo.entity.Task;
import org.springframework.stereotype.Component;

@Component
public interface TaskMapper {
    int deleteByPrimaryKey(Integer taskId);

    int insert(Task record);

    int insertSelective(Task record);

    Task selectByPrimaryKey(Integer taskId);

    int updateByPrimaryKeySelective(Task record);

    int updateByPrimaryKey(Task record);
}