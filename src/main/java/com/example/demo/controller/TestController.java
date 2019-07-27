package com.example.demo.controller;

import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

//@Controller

@Controller
//@RequestMapping("/")
public class TestController {

    private final UserMapper userMapper;

    @Autowired
    public TestController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @ResponseBody//返回字符串，而不是字符串对应名字的jsp
    @RequestMapping("test")
    public String hello(){
        User user=userMapper.selectByPrimaryKey( 1 );
        return "hello word!"+user.getUserName();
    }
}
