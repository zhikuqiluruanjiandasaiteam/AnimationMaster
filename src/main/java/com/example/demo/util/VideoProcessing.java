package com.example.demo.util;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.VideoInfo;
import it.sauronsoftware.jave.MultimediaInfo;
import org.apache.shiro.util.PatternMatcher;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class VideoProcessing {
    /**
     * 视频拆分图片（全部帧）
     * @param videoFile 视频文件完整路径
     * @param toPath 图片储存目录(只生成jpg)
     * @param numWidth 储存编号宽度 （如宽度3）001开始
     * @return 运行结果状态码
     */
    public static int video2Images(String videoFile, String toPath, int numWidth){
        String toolStr=AudioProcessing.getWebRootAbsolutePath()+"static/tools/video2images.py";
        String strShell="python "+toolStr+" --from_file "+videoFile+" --to_path "+toPath
                +" --num_width "+numWidth;
        int re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        return re;
    }

    /**
     * 去除视频声音
     * @param videoFile 视频文件完整路径
     * @param toFile 生成无声视频文件路径
     * @return 运行结果状态码
     */
    public static int removeVideoSound(String videoFile,String toFile){
        String shell="ffmpeg -i "+videoFile+" -vcodec copy -an "+toFile;
        AudioProcessing.runExec( shell );
        return -1;
    }

    /**
     * 图片组合视频（无声）
     * @param imagesPath 图片目录
     * @param prefix 图片文件命名规则——前缀字符串
     * @param numWidth 图片文件命名规则——编号宽度 如：001宽度3
     * @param suffix 图片文件命名规则——后缀字符串（包括文件类型后缀）
     * @param toFile 生成视频路径
     * @param referenceVideo 帧率帧总数参考视频路径（从这个视频中获得帧率与帧总数值）
     * @return 运行结果状态码
     */
    public static int images2Video(String imagesPath,String prefix,int numWidth,
                                   String suffix,String toFile,String referenceVideo){
        String toolStr=AudioProcessing.getWebRootAbsolutePath()+"static/tools/images2video.py";
        String strShell="python "+toolStr+" --image_path "+imagesPath
                +" --num_width "+numWidth
                +" --suffix "+suffix
                +" --to_file "+toFile
                +" --reference_video "+referenceVideo;
        if(prefix!=null&&!prefix.equals( "" )){
            strShell+=" --prefix "+prefix;
        }
        int re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        return re;
    }

     /**
     * 合成视频音频
     * @param videoFile 视频文件(必须无声视频)完整路径
     * @param wavFile wav类型音频文件路径
     * @param toFile 生成视频文件路径
     * @return 运行结果状态码
     */
     //todo:清晰的不足，待做
    public static int videoAddAudio(String videoFile,String wavFile,String toFile){

        return -1;
    }

    /**
     *获取视频信息
     * @param filePath 视频文件路径
     * @return int[0]:视频宽度；[1]:视频高度；[2]:总帧数
     */
    public static int[] getVideoInfo(String filePath){
        File source = new File(filePath);
        Encoder encoder = new Encoder();
        int[] info=null;
        try {
            info=new int[4];
            MultimediaInfo m = encoder.getInfo(source);
            long ls = m.getDuration();
            VideoInfo vi = m.getVideo();
            info[0]=vi.getSize().getWidth();//视频宽度
            info[1]=vi.getSize().getHeight();//视频高度
            info[2]=(int)(vi.getFrameRate()*ls/1000);//总帧数
            info[3]=(int)(ls/1000);//时长
            if(info[3]<=0){
                info[3]=1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
