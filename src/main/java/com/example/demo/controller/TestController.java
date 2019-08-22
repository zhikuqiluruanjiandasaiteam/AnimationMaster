package com.example.demo.controller;

import ch.ethz.ssh2.Connection;
import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import com.example.demo.service.ImgProcessing;
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
    private ImgProcessing imgProcessing;


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
    @ResponseBody
    @RequestMapping("/testImg1")
    public String testImg1() throws Exception {
        String initFilePath= "/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_img";
        String outputPath="/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_output1";
        Integer longEdgeLength=100;
        String style= "Shinkai";
        imgProcessing.ProcessSingleDir(initFilePath,outputPath,style,longEdgeLength);
        return "1";
    }

    @ResponseBody
    @RequestMapping("/testImg2")
    public String testImg2() throws Exception {
        String initFile1= "/home/ubuntu/test_inputImg/aa.jpg";
        String initFile2= "/home/ubuntu/test_inputImg/bb.jpg";
        String outputPath="/home/ubuntu/test_outputImg";
        String outputFile1="aa1.jpg";
        String outputFile2="bb1.jpg";
        Integer longEdgeLength=100;
        String style= "Shinkai";
        imgProcessing.ProcessSinglePic(initFile1,outputPath,outputFile1,style,longEdgeLength);
        imgProcessing.ProcessSinglePic(initFile2,outputPath,outputFile2,style,longEdgeLength);
        return "1";
    }


    @ResponseBody
    @RequestMapping("/testImg3")
    public String testImg3() throws Exception {
        String inputPath1= "/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_img1";
        String inputPath2= "/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_img2";
        String outputPath1="/home/ubuntu/test_outputImg1";
        String outputPath2="/home/ubuntu/test_outputImg2";
        Integer longEdgeLength=100;
        String style= "Shinkai";
        Connection connection = imgProcessing.Login();
        imgProcessing.ProcessSingleDirOnceConn(inputPath1,outputPath1,style,longEdgeLength,connection);
        imgProcessing.ProcessSingleDirOnceConn(inputPath2,outputPath2,style,longEdgeLength,connection);
        imgProcessing.closeConnect(connection);
        return "1";
    }

    @ResponseBody
    @RequestMapping("/testImg4")
    public String testImg4() throws Exception {
        String initFile1= "/home/ubuntu/test_inputImg/aa.jpg";
        String initFile2= "/home/ubuntu/test_inputImg/bb.jpg";
        String outputPath1="/home/ubuntu/test_outputImg1";
        String outputPath2="/home/ubuntu/test_outputImg2";
        String outputFile1="aa1.jpg";
        String outputFile2="bb1.jpg";
        Integer longEdgeLength=100;
        String style= "Shinkai";
        Connection connection = imgProcessing.Login();
        imgProcessing.ProcessSinglePicOnceConn(initFile1,outputPath1,outputFile1,style,longEdgeLength,connection);
        imgProcessing.ProcessSinglePicOnceConn(initFile2,outputPath2,outputFile2,style,longEdgeLength,connection);
        imgProcessing.closeConnect(connection);
        return "1";

    }


}
