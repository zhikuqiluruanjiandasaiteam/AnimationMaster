package com.example.demo.controller;

import ch.ethz.ssh2.Connection;
import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.UserMapper;
import com.example.demo.util.ImgProcessing;
import com.example.demo.util.VideoProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

//    @ResponseBody//长久运行，保障通知测试网站人员
//    @RequestMapping("/")
//    public String main(){
//        return "<h2>【第十七届山东省大学生软件设计大赛作品-动漫大师】 欢迎您的使用!</h2><br/>" +
//                "\n软件服务器太贵了╮(╯﹏╰）╭，非评审时只能关着，如果您为评审老师" +
//                "，要测试网站功能，请联系管理员开启正式服务器（QQ:1481440484 邮箱：1481440484@qq.com）";
//    }

    @ResponseBody//返回字符串，而不是字符串对应名字的jsp
    @RequestMapping("test")
    public String hello(){
        return "hello word!:"+ ParameterConfiguration.fileRoot+ParameterConfiguration.Tools.rootPath;
    }
    @ResponseBody
    @RequestMapping("/testImg1")
    public String testImg1() throws Exception {
        String initFilePath1= "/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_img1";
        String initFilePath2= "/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_img2";
        String outputPath1="/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_output1";
        String outputPath2="/home/ubuntu/CartoonGAN-Test-Pytorch-Torch/test_output2";
        Integer longEdgeLength=100;
        String style= "Shinkai";
        imgProcessing.ProcessSingleDir(initFilePath1,outputPath1,style,longEdgeLength);
        for (int i=0;i<20;i++){
            System.out.println("====================");
        }
        imgProcessing.ProcessSingleDir(initFilePath2,outputPath2,style,longEdgeLength);
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
        for (int i=0;i<20;i++){
            System.out.println("====================");
        }
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
        for (int i=0;i<20;i++){
            System.out.println("====================");
        }
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
        for (int i=0;i<20;i++){
            System.out.println("====================");
        }
        imgProcessing.ProcessSinglePicOnceConn(initFile2,outputPath2,outputFile2,style,longEdgeLength,connection);
        imgProcessing.closeConnect(connection);
        return "1";

    }

    @ResponseBody
    @RequestMapping("/testvideo")
    public String testvideo() throws Exception {
      return ""+VideoProcessing.videoAddAudio("/home/ubuntu/out_dmt.mp4","/home/ubuntu/dmt.wav","/home/ubuntu/output.mp4");
    }

}
