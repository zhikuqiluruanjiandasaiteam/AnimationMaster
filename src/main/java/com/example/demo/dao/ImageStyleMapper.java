package com.example.demo.dao;

import com.example.demo.entity.ImageStyle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ImageStyleMapper {
    int deleteByPrimaryKey(Integer imsId);

    int insert(ImageStyle record);

    int insertSelective(ImageStyle record);

    ImageStyle selectByPrimaryKey(Integer imsId);

    int updateByPrimaryKeySelective(ImageStyle record);

    int updateByPrimaryKeyWithBLOBs(ImageStyle record);

    int updateByPrimaryKey(ImageStyle record);

    List<ImageStyle> selectAll();
}