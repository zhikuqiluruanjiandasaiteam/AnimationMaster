package com.example.demo.config;

public class ParameterConfiguration {

    public static final int[] clarity= {720,480,360,240};

    /**数据库，任务类型枚举值
     */
    public class Type{
        public static final String video="video";
        public static final String image="image";
        public static final String audio="audio";
    }

    /**文件储存位置
     */
    public class SavePath{
        public static final String root="~/AnimationMaster";

    }
}
