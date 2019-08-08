package com.example.demo.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class ParameterConfiguration {

    public static final int[] clarity= {720,480,360,240};

    /**数据库，任务类型枚举值
     */
    public static abstract class Type{
        public static final String video="video";
        public static final String image="image";
        public static final String audio="audio";

        private static String[] file_video={"mp4"};
        private static String[] file_image={"jpg","png","svg","gif"};
        private static String[] file_audio={"mp3"};
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
        public static final String uploadSava= root+File.separator+"UploadFiles";
        public static final String finalSave=root+File.separator+"FinalFiles";
        public static final String intermediateSave=root+File.separator+"IntermediateFiles";

        private static String getRoot(){
            String os = System.getProperty("os.name");//判断操作系统
            if(os.toLowerCase().startsWith("win")){
                return "E:"+ File.separator+"AnimationMaster";
            }else{
                return "~"+ File.separator+"AnimationMaster";
            }
        }

    }
}
