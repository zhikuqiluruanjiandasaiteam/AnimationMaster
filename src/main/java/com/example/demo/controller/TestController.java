package com.example.demo.controller;

import com.example.demo.severice.AudioProcessing;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {

    @RequestMapping("/")
    public String hello(){
        AudioProcessing.changePitch( "","",1 );////test
        return "hello word!";
    }
}
