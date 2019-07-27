package com.example.demo.dao;

import com.example.demo.entity.AudioStyle;

public interface AudioStyleMapper {
    int deleteByPrimaryKey(Integer ausId);

    int insert(AudioStyle record);

    int insertSelective(AudioStyle record);

    AudioStyle selectByPrimaryKey(Integer ausId);

    int updateByPrimaryKeySelective(AudioStyle record);

    int updateByPrimaryKeyWithBLOBs(AudioStyle record);

    int updateByPrimaryKey(AudioStyle record);
}