package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@Service
public class ParameterConfiguration {

    public static String fileRoot;

    // 静态变量不能直接value,必须先建立一个对象
    //todo：不知原因，但经测试，FilePath.root静态变量不会先于这个创建（如果先于就会null/Ad...；目前没问题，如果出错，可能这里有问题）
    @Value( "${fileRoot}" )
    public void setDriver(String fileRoot) {
        this.fileRoot= fileRoot;
    }
    @Value( "${toolsRoot}" )
    public void setDriver2(String toolsRoot) {
        Tools.rootPath= toolsRoot;
    }
    @Value( "${pfRoot}" )
    public void setDriver3(String pfRoot) {
        Tools.pfRoot= pfRoot;
        Tools.pfMainFile=pfRoot+File.separator+"patch_frame.py";
    }

    public static final int[] clarity= {720,480,360,240};

    /**数据库，任务类型枚举值
     */
    public static abstract class Type{
        public static final String video="video";
        public static final String image="image";
        public static final String audio="audio";

        private static String[] file_video={"mp4"};
        private static String[] file_image={"jpg","png","mp4","svg","gif"};
        private static String[] file_audio={"mp3","mp4"};
        //必须写在上面file_xxx下面，比如键对应的值会得到null
        public static final HashMap<String,String[]> fileName = getFileName();

        private static HashMap<String,String[]> getFileName(){
            HashMap<String,String[]> map=new HashMap<String,String[]>(  );
            map.put(video,file_video);
            map.put(image,file_image);
            map.put(audio,file_audio);
            return map;
        }
    }
    /**文件储存位置
     */
    public static class FilePath{
        public static final String root=getRoot();
        public static final String uploadSave = root+File.separator+"UploadFiles";
        public static final String finalSave=root+File.separator+"FinishFiles";
        public static final String intermediateSave=root+File.separator+"IntermediateFiles";
        //视频类任务中间文件的中间目录
        public static final String video_ImagesForm="ImagesFrom";
        public static final String vidoe_ImagesTo="ImagesTo";
        public static final String vidoe_audio="Audio";

        private static String getRoot(){
            String os = System.getProperty("os.name");//判断操作系统
            if(os.toLowerCase().startsWith("win")){
                return "E:"+ File.separator+"AnimationMaster";
            }else{
                return fileRoot+File.separator+"AnimationMaster";
            }
        }
    }

    public static class Tools{
        public static String rootPath;
        public static String pfRoot;
        public static String pfMainFile;//不能在这里赋值，@Value注入后不会自动改变
    }

}
