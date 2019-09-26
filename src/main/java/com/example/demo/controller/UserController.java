package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;


    @ResponseBody
    @RequestMapping("/login")
    public Map<String,Object> login(User user,  HttpServletRequest request)throws Exception{
        System.out.println("登录");///////////////
        Map<String,Object> map=new HashMap<String,Object>();
        if(StringUtil.isEmpty(user.getUserName().trim())){
            map.put("error_code", -1);
            map.put("error_msg", "用户名为空！");
        }else if(StringUtil.isEmpty(user.getPassword().trim())){
            map.put("error_code", -2);
            map.put("error_msg", "密码为空！");
        }else{
            try{
                User currentUser = userService.findByUserName(user.getUserName());
                Subject subject = SecurityUtils.getSubject();
                UsernamePasswordToken token=new UsernamePasswordToken(user.getUserName(), new Md5Hash(user.getPassword(),currentUser.getPasswordSalt()).toString());
                subject.login(token);
                SecurityUtils.getSubject().getSession().setTimeout(24*60*60*1000);
                request.getSession().setAttribute("currentUser", currentUser);
                User user1=new User();
                user1.setUserName(currentUser.getUserName());
                user1.setUserId(currentUser.getUserId());
                map.put("error_code",0);
                map.put("error_msg","登录成功");
                map.put("data",user1);
            }catch(Exception e){
//                e.printStackTrace();
                System.out.println(user.getUserName()+"密码错误，登陆失败");
                map.put("error_code", -3);
                map.put("error_msg", "用户名或者密码错误！");
            }
        }
        return map;
    }


    @ResponseBody
    @RequestMapping("/register")
    public Map<String,Object> register(User user)throws Exception{
        Map<String,Object> map=new HashMap<String,Object>();
        if(StringUtil.isEmpty(user.getUserName().trim())){
            map.put("error_code", -1);
            map.put("error_msg", "用户名为空！");
        }else if(StringUtil.isEmpty(user.getPassword().trim())){
            map.put("error_code", -2);
            map.put("error_msg", "密码为空！");
        }else if(userService.findByUserName(user.getUserName())!=null){
            map.put("error_code", -1);
            map.put("error_msg", "用户名已存在，请更换！");
        }else{
            String randomString = StringUtil.getRandomString(32);
            user.setPasswordSalt(randomString);
            user.setPassword(new Md5Hash(user.getPassword(),randomString).toString());
            userService.save(user);
            map.put("error_code", 0);
        }
        return map;
    }

    @ResponseBody
    @RequestMapping("/getSession")
    public Map<String,Object> getSession(HttpSession session){
        Map<String,Object> map=new HashMap<String,Object>();
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if(currentUser!=null){
                Map<String,Object>data=new HashMap<>();
                data.put("userId",currentUser.getUserId());
                data.put("userName",currentUser.getUserName());
                map.put("error_code",0);
                map.put("error_msg","获取成功");
                map.put("data",data);
            }else {
                map.put("error_code",0);
                map.put("error_msg","获取成功");
                map.put("data",null);
            }
        }catch (Exception e){
            map.put("error_code", -1);
            map.put("error_msg", "获取异常");
            map.put("data",null);
        }
        return map;
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session){
        User currentUser = (User) session.getAttribute("currentUser");
        if(currentUser==null||currentUser.getUserId()==null){
            return "redirect:/";
        }
        SecurityUtils.getSubject().logout();
        return "redirect:/";
    }

}
