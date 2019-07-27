package com.example.demo.controller;

import com.example.demo.severice.StyleSeverice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("style")
public class StyleController {

    @ResponseBody
    @RequestMapping("list")
    public String getStyleList(){
        StyleSeverice styleSeverice=new StyleSeverice();
        return styleSeverice.getImsList().toString();
    }
}
