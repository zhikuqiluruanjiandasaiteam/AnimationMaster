package com.example.demo.util;


import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.example.demo.config.ParameterConfiguration;
import com.example.demo.util.AudioProcessing;
import com.example.demo.util.RemoteShellExecutor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;

@Service
public class ImgProcessing {

    @Value("${DLServerIP}")
    private String DLServerIP;

    @Value("${DLServerUserName}")
    private String DLServerUserName;

    @Value("${DLServerPassword}")
    private String DLServerPassword;

    @Value("${DLServerSecretKey}")
    private String DLServerSecretKey;

    @Value("${ExecShellSingleDir}")
    private String ExecShellSingleDir;

    @Value("${ExecShellSinglePic}")
    private String ExecShellSinglePic;

    private String charset = Charset.defaultCharset().toString();
    private static final int TIME_OUT = 1000 * 5 * 60;


    /**
     *  一次处理一个目录中的图片
     * @param initFilePath  输入图片的目录
     * @param outputPath    输出图片的目录(不存在会创建)
     * @param style         风格
     * @param longEdgeLength  长边的长度
     * @throws Exception
     */
    public void ProcessSingleDir(String initFilePath,String outputPath,String style,Integer longEdgeLength)throws Exception{
        if(style.equals( ParameterConfiguration.Style.imsLine )||style.equals( ParameterConfiguration.Style.imsLine2 )){
            changeLineD(initFilePath,outputPath,style,longEdgeLength);
        }else{
            RemoteShellExecutor executor = new RemoteShellExecutor(DLServerIP, DLServerUserName, DLServerPassword,new File(DLServerSecretKey));
            System.out.println(executor.execProcessSingleDir(ExecShellSingleDir+" "+initFilePath+" "+outputPath+" "+longEdgeLength+" "+style));
        }
    }


    /**
     *  该方法为一次处理一张图片的方法(低效版)
     * @param input_file        输入文件的路径
     * @param outputPath        输出文件的目录 如 /aaa/bbb
     * @param output_file       输出文件的名称  如 aaa.jpg
     * @param style             风格
     * @param longEdgeLength    长边的长度
     * @throws Exception
     */
    public int ProcessSinglePic(String input_file,String outputPath,String output_file,String style,Integer longEdgeLength)throws Exception{
        if(style.equals( ParameterConfiguration.Style.imsLine )||style.equals( ParameterConfiguration.Style.imsLine2 )){
            return changeLineO(input_file,outputPath,style,longEdgeLength);
        }
        RemoteShellExecutor executor = new RemoteShellExecutor(DLServerIP, DLServerUserName, DLServerPassword,new File(DLServerSecretKey));
        int re=executor.execProcessSinglePic(ExecShellSinglePic+" "+input_file+" "+outputPath+" "+output_file+" "+longEdgeLength+" "+style);
        System.out.println(re);
        return re;
    }

    /**
     * 该方法为提前 获取连接对象的方法
     * @return
     */
     public Connection Login(){
        Connection connection=null;
        connection = new Connection(DLServerIP);
        try {
             connection.connect();
             connection.authenticateWithPublicKey(DLServerUserName,new File(DLServerSecretKey),DLServerPassword);
            return connection;
         } catch (IOException e) {
            e.printStackTrace();
            return null;
         }
     }

    /**
     * 该方法为处理图片高效版  获取了连接对象后，调用该方法完成对一张图片的处理
     * @param initFile
     * @param outputPath
     * @param outputFile
     * @param style
     * @param longEdgeLength
     * @param conn
     * @return
     * @throws Exception
     */
     public Integer ProcessSinglePicOnceConn(String initFile,String outputPath,String outputFile,String style,Integer longEdgeLength,Connection conn)throws Exception{
     InputStream stdOut = null;
     InputStream stdErr = null;
     String outStr = "";
     String outErr = "";
     int ret = -1;
     Session session = null;
     try {
     session = conn.openSession();
     session.execCommand(ExecShellSinglePic+" "+initFile+" "+outputPath+" "+outputFile+" "+longEdgeLength+" "+style+" ");
     stdOut = new StreamGobbler(session.getStdout());
     outStr = processStream(stdOut, charset);
     stdErr = new StreamGobbler(session.getStderr());
     outErr = processStream(stdErr, charset);
     session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
     System.out.println("outStr=" + outStr);
     System.out.println("outErr=" + outErr);
     ret = session.getExitStatus();
     return ret;
     } catch (Exception e) {
     e.printStackTrace();
     return -1;
     }finally {
     IOUtils.closeQuietly(stdOut);
     IOUtils.closeQuietly(stdErr);
     }
     }

     public Integer ProcessSingleDirOnceConn(String input_dir,String output_dir,String style,Integer longEdgeLength,Connection conn)throws Exception{
         InputStream stdOut = null;
         InputStream stdErr = null;
         String outStr = "";
         String outErr = "";
         int ret = -1;
         Session session = null;
         try {
             session = conn.openSession();
             session.execCommand(ExecShellSingleDir+" "+input_dir+" "+output_dir+" "+longEdgeLength+" "+style);
             stdOut = new StreamGobbler(session.getStdout());
             outStr = processStream(stdOut, charset);
             stdErr = new StreamGobbler(session.getStderr());
             outErr = processStream(stdErr, charset);
             session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
             System.out.println("outStr=" + outStr);
             System.out.println("outErr=" + outErr);
             ret = session.getExitStatus();
             return ret;
         } catch (Exception e) {
             e.printStackTrace();
             return -1;
         }finally {
             IOUtils.closeQuietly(stdOut);
             IOUtils.closeQuietly(stdErr);
         }
     }

     private String processStream(InputStream in, String charset) throws Exception {
     byte[] buf = new byte[1024];
     StringBuilder sb = new StringBuilder();
     while (in.read(buf) != -1) {
     sb.append(new String(buf, charset));
     }
     return sb.toString();
     }

    /**
     * 该方法为 关闭连接对象
     * @param connection
     */
     public void closeConnect(Connection connection){
     if (connection != null) {
       //  Integer exitStatus = session.getExitStatus();
         connection.close();
     }
     }

     //转线条风格
     private int changeLineO(String from_path,String to_path,String style,int clearity){
         String tool= ParameterConfiguration.Tools.rootPath+File.separator+"linestyle.py";
         String shell="python "+tool+" --from_file "+from_path+" --to_file "+to_path
                 +" --clearity "+clearity+" --suffix _"+style;
         System.out.println( shell );
         return AudioProcessing.runExec( shell );
     }

     private int changeLineD(String from_file,String to_file,String style,int clearity){
        String tool= ParameterConfiguration.Tools.rootPath+File.separator+"linestyle.py";
        String shell="python "+tool+" --from_file "+from_file+" --to_path "+to_file
                +" --clearity "+clearity+" --suffix _"+style;
         System.out.println( shell );
        return AudioProcessing.runExec( shell );
    }

}
