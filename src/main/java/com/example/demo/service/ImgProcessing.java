package com.example.demo.service;


import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.example.demo.util.RemoteShellExecutor;
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

    @Value("${ExecShell}")
    private String ExecShell;

    @Value("${ExecShelltransferEnv}")
    private String ExecShelltransferEnv;

    @Value("${ExecShelltransferSinglePic}")
    private String ExecShelltransferSinglePic;

    private String charset = Charset.defaultCharset().toString();
    private static final int TIME_OUT = 1000 * 5 * 60;

    public void ProcessSingleDir(String initFilePath,String outputPath,String style,Integer longEdgeLength,int taskId)throws Exception{
        RemoteShellExecutor executor = new RemoteShellExecutor(DLServerIP, DLServerUserName, DLServerPassword,new File(DLServerSecretKey));
        System.out.println(executor.execProcessSingleDir(ExecShell+" "+initFilePath+" "+outputPath+" "+longEdgeLength+" "+style+" "+taskId));
    }

    public Connection ProcessSinglePicLogin(){
        Connection conn = new Connection(DLServerIP);
        try {
            conn.connect();
            conn.authenticateWithPublicKey(DLServerUserName,new File(DLServerSecretKey),DLServerPassword);
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Integer ProcessSinglePicTransferEnv(Connection conn){
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outStr = "";
        String outErr = "";
        int ret = -1;
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(ExecShelltransferEnv);
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
        }
    }
    public Integer ProcessSinglePic(String initFile,String outputFile,String style,Integer longEdgeLength,Connection conn)throws Exception{
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outStr = "";
        String outErr = "";
        int ret = -1;
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(ExecShelltransferSinglePic+" "+initFile+" "+outputFile+" "+longEdgeLength+" "+style+" ");
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

}
