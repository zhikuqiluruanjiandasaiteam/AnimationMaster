package com.example.demo.util;

public class VideoProcessing {
    /**
     * 视频拆分图片（全部帧）
     * @param filePath 视频文件完整路径
     * @param toPath 图片储存目录
     * @param numWidth 储存编号宽度 （如宽度3）001开始
     * @return 运行结果状态码
     */
    public static int video2images(String filePath,String toPath,int numWidth){
        String str=AudioProcessing.getWebRootAbsolutePath()+"static/tools/video2images.py";
        String strShell="python "+str+" --from_file "+filePath+" --to_path "+toPath
                +" --num_width "+numWidth;
        int re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        return re;
    }
}
