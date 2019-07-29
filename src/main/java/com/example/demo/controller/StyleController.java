package com.example.demo.controller;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.service.StyleSeverice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("style")
public class StyleController {

    @Autowired
    private StyleSeverice styleSeverice;

    /**获取风格类型
     * @param type 转换类型：video,image,audio
     */
    @ResponseBody
    @RequestMapping("list")
    public Map<String,Object> getStyleList(@RequestParam(value = "type") String type){
        Map<String,Object> re=new HashMap<>(  );
        re.put("error_code",0);
        re.put("error_msg","");
        re.put("data","");
        Map<String,Object> data=new HashMap<>(  );
        if(type.equals( ParameterConfiguration.Type.video )||type.equals( ParameterConfiguration.Type.image )) {
            Map[] map=styleSeverice.getImsList();
            if(map.length==0){
                re.put("error_code",1);
                re.put("error_msg","查询失败");
                return re;
            }
            data.put("image",map);
            data.put("clarity",ParameterConfiguration.clarity);
        }
        if(type.equals( ParameterConfiguration.Type.video )||type.equals( ParameterConfiguration.Type.audio )) {
            Map[] map=styleSeverice.getAusList();
            if(map.length==0){
                re.put("error_code",1);
                re.put("error_msg","查询失败");
                return re;
            }
            data.put("audio",map);
        }
        re.put("data",data);
        return re;
    }
}
