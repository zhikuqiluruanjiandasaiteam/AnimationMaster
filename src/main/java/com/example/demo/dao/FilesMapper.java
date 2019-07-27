package com.example.demo.dao;

import com.example.demo.entity.Files;

public interface FilesMapper {
    int deleteByPrimaryKey(Integer fileId);

    int insert(Files record);

    int insertSelective(Files record);

    Files selectByPrimaryKey(Integer fileId);

    int updateByPrimaryKeySelective(Files record);

    int updateByPrimaryKey(Files record);
}