package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {

    public User findByUserName(String userName);

    int save(User user);
}
