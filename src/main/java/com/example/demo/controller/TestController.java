package com.example.demo.controller;

import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

//@Controller
@RestController
@RequestMapping("/")
public class TestController {

    private final UserMapper userMapper;

    @Autowired
    public TestController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @RequestMapping("/")
    public String hello(){
        User user=userMapper.selectByPrimaryKey( 1 );
        return "hello word!"+user.getUserName();
    }
}
