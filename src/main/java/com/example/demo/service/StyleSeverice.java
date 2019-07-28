package com.example.demo.service;

import com.example.demo.dao.ImageStyleMapper;
import com.example.demo.entity.ImageStyle;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class StyleSeverice {

    @Autowired
    private ImageStyleMapper imageStyleMapper;

    public List<ImageStyle> getImsList(){
//        List list=imageStyleMapper.selectAll();
        System.out.println(imageStyleMapper.selectByPrimaryKey( 1 ));
        return null;
    }
}
