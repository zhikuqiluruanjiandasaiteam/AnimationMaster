package com.example.demo.service;

import com.example.demo.dao.AudioStyleMapper;
import com.example.demo.dao.ImageStyleMapper;
import com.example.demo.entity.AudioStyle;
import com.example.demo.entity.ImageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StyleSeverice {

    @Autowired
    private ImageStyleMapper imageStyleMapper;
    @Autowired
    private AudioStyleMapper audioStyleMapper;

    public Map[] getImsList(){
        List<ImageStyle> list=imageStyleMapper.selectAll();
        Map[] maps=new HashMap[list.size()];
        int i=0;
        for (ImageStyle imageStyle : list) {
            Map<String,Object> map=new HashMap<>();
            map.put("ims_id",imageStyle.getImsId());
            map.put("ims_name",imageStyle.getImsName());
            map.put("ims_show_path",imageStyle.getImsShowPath());
            map.put("ims_description",imageStyle.getImsDescription());
            maps[i++]=map;
        }
        return maps;
    }

    public Map[] getAusList(){
        List<AudioStyle> list=audioStyleMapper.selectAll();
        Map[] maps=new HashMap[list.size()];
        int i=0;
        for (AudioStyle audioStyle : list) {
            Map<String,Object> map=new HashMap<>();
            map.put("aus_id",audioStyle.getAusId());
            map.put("aus_name",audioStyle.getAusName());
//            map.put("aus_show_path",audioStyle.getAusShowPath());
            map.put("aus_description",audioStyle.getAusDescription());
            maps[i++]=map;
        }
        return maps;
    }

}
