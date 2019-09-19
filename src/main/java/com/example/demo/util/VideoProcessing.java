package com.example.demo.util;

import com.example.demo.config.ParameterConfiguration;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.VideoInfo;
import it.sauronsoftware.jave.MultimediaInfo;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class VideoProcessing {

    //补帧时，间隔多少帧必有一帧
    private static int intervalNum=10;

    /**
     * 视频拆分图片（全部帧）
     * @param videoFile 视频文件完整路径
     * @param toPath 图片储存目录(只生成jpg)
     * @param numWidth 储存编号宽度 （如宽度3）001开始
     * @return 运行结果状态码
     */
    public static int video2Images(String videoFile, String toPath, int numWidth){
        String toolStr= ParameterConfiguration.Tools.rootPath +File.separator+"video2images.py";
        String strShell="python "+toolStr+" --from_file "+videoFile+" --to_path "+toPath
                +" --num_width "+numWidth;
        int re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        return re;
    }
    /**
     * 视频拆分图片（补帧用帧）
     * @param videoFile 视频文件完整路径
     * @param toPath 图片储存目录(只生成jpg)
     * @param numWidth 储存编号宽度 （如宽度3）001开始
     * @return 运行结果状态码
     */
    public static List<String> video2ImagesPf(String videoFile, String toPath, int numWidth,String intermediatePath){
        String kfTxt=intermediatePath+File.separator+"keyFrameNum.txt";
        String namesTxt=intermediatePath+File.separator+"namesTxt.txt";
        int re=findKeyFrame(videoFile,kfTxt);
        System.out.println( re );
        if(re!=0)
            return null;
//        to//do:测试临时换掉
        String toolStr= ParameterConfiguration.Tools.rootPath +File.separator+"video2images.py";
//        String toolStr="E:\\Workbench\\IDEA\\动漫大师\\tools临时存放\\tools\\"+"video2images.py";
        String strShell="python "+toolStr+" --from_file "+videoFile+" --to_path "+toPath
                +" --num_width "+numWidth+" --key_txt "+kfTxt+" --interval_num "+intervalNum
                +" --names_outtxt "+namesTxt;
        re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        if(re!=0)
            return null;
        //获取输出帧名称序列
        List<String> names=new ArrayList<>(  );
        File file = new File(namesTxt);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if(tempString.trim().equals( "" ))
                    continue;
                names.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return names;

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
        String toolStr= ParameterConfiguration.Tools.rootPath +File.separator+"images2video.py";
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
    public static int videoAddAudio(String videoFile,String wavFile,String toFile){
        //ffmpeg -i video.mp4 -i audio.wav -c:v copy -c:a aac -strict experimental output.mp4
        // ffmpeg -i videoFile -i wavFile -c:v copy -c:a aac -strict experimental toFile
        String strFfmprg="ffmpeg";
        String os = System.getProperty("os.name");//判断操作系统
        if(os.toLowerCase().startsWith("win")){
            strFfmprg=ParameterConfiguration.Tools.rootPath +File.separator+"ffmpeg.exe";
        }
        return AudioProcessing.runExec(strFfmprg+" -i"+"  "+videoFile+"  -i "+ wavFile+" -c:v copy -c:a aac -strict experimental  "+toFile);
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

    public static int runExec(String shell) {
        try{
            final Process process = Runtime.getRuntime().exec(shell);//生成一个新的进程去运行调用的程序
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            return process.waitFor();//得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private static void printMessage(final InputStream input) {
        new Thread(new Runnable() {
            public void run() {
                Reader reader = new InputStreamReader(input);
                BufferedReader bf = new BufferedReader(reader);
                String line = null;
                try {
                    while((line=bf.readLine())!=null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static int findKeyFrame(String video,String outTxt){
        //ffprobe -select_streams v -show_frames -show_entries frame=pict_type -of csv test.mp4 | grep -n I >> tesxt.txt
        String strff="ffprobe";//windows系统先安装ffmpeg，再配置环境变量
        String shell=strff+" -select_streams v -show_frames -show_entries frame=pict_type -of csv "
                +video;//+" | grep -n I >  "+outTxt
        System.out.println( shell );

        //！！！不能执行AudioProcessing.runExec(shell)，linux中会截获输出流，导致无法自动输出到文件
        try{
            final Process process = Runtime.getRuntime().exec(shell);//生成一个新的进程去运行调用的程序
            printMessage2File( process.getInputStream(),outTxt );
            return process.waitFor();//得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void printMessage2File(final InputStream input,String outTxt) {
        new Thread(new Runnable() {
            public void run() {
                Reader reader = new InputStreamReader(input);
                BufferedReader bf = new BufferedReader(reader);
                String line = null;
                try {
                    StringBuilder out= new StringBuilder();
                    int i=0;
                    while((line=bf.readLine())!=null) {
                        line=line.trim();//删除两端空白字符
                        String[] strs=line.split( "," );
                        if(strs.length==2&&strs[0].equals( "frame" )){
                            i++;
                            if(strs[1].equals( "I" ))
                                out.append( i ).append( ":" ).append( line ).append( "\n" );
                        }
//                        System.out.println(line);
                    }
                    FileWriter fileWritter = new FileWriter(outTxt);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write( String.valueOf( out ) );
                    bufferWritter.close();
                    System.out.println("Finish");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    public static void main(String[] args){
//        video2ImagesPf("C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\testx.mp4",
//                "C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\newx",6,
//                "C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\newx");
//
////        int re=findKeyFrame("C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\testx.mp4",
////                "C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\txtx.txt");
//
//    }

}
