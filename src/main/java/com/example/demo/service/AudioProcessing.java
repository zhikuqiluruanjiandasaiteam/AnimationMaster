package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLDecoder;

@Service
public class AudioProcessing {

    /**
     * 改变音调
     * @param infile 输入wav文件路径
     * @param outfile 输出wav文件路径
     * @param relPitch 相对音调值，-3==降低3个（半音）
     * @return int 运行结果状态码
     */
    public static int changePitch(String infile,String outfile,Integer relPitch){
        if(relPitch==null)
            return -1;
        String os = System.getProperty("os.name");//判断操作系统
        String strShell;
        String strPitch;
        if(relPitch>=0){
            strPitch="+"+relPitch;
        }else{
            strPitch=""+relPitch;
        }
        if(os.toLowerCase().startsWith("win")){
            String str=getWebRootAbsolutePath()+"static/tools/soundstretch.exe";
            strShell=str+" "+infile+" "+outfile+" -pitch="+strPitch;
        }else{
            strShell="soundstretch "+infile+" "+outfile+" -pitch="+strPitch;
        }
        int re=-1;
        System.out.println(strShell);///////////////////////
        re=runExec( strShell );
        return re;
    }

    /**
     * wav文件与别的文件转化
     * @param fromType
     * @param fromFile
     * @param toType
     * @param toFile
     * @return
     */
    public static int file2Wav(String fromType, String fromFile, String toType, String toFile){
        String str=getWebRootAbsolutePath()+"static/tools/video2wav.py";
        String strShell="python "+str+" --from_type "+fromType+" --from_file "+fromFile
                +" --to_type "+toType+" --to_file "+toFile;
        int re=-1;
        System.out.println(strShell);
        re = runExec(strShell);
        return re;
    }

    //运行命令并等待命令执行完
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

    /**
     * 得到WebRoot文件夹下的根路径，及web项目的根路径--classes的绝对路径,
     * 注意返回...../classes/最后有‘/’，再加一个‘/’，‘...//...’会报错
     */
    private static String getWebRootAbsolutePath() {
        String path = AudioProcessing.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if(path.charAt( 0 )=='/'||path.charAt( 0 )=='\\'){//如果开头是‘\’
            path=path.substring(1);//截取第二到最后的字符串
        }
        //后可能有中文编码%25
        try{
            path=URLDecoder.decode(path, "utf-8");
        }catch (Exception e){
            return path;
        }
        return path;
    }

//    public static void main(String args[]){
//        changePitch("C:\\Users\\Think\\Desktop\\fsdownload\\mp.wav",
//                "C:\\Users\\Think\\Desktop\\fsdownload\\omp.wav",-7);
//    }

}
