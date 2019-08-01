package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


    public User findByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }


    public int save(User user) {
        return userMapper.insert(user);
    }
}
