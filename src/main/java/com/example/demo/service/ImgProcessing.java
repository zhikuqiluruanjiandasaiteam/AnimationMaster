package com.example.demo.service;


import com.example.demo.util.RemoteShellExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

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

    public void ProcessSinglePic(String initFilePath,String outputPath,String style,Integer longEdgeLength)throws Exception{
        RemoteShellExecutor executor = new RemoteShellExecutor(DLServerIP, DLServerUserName, DLServerPassword,new File(DLServerSecretKey));
        System.out.println(executor.exec(ExecShell+" "+initFilePath+" "+outputPath+" "+longEdgeLength+" "+style));
    }


}
