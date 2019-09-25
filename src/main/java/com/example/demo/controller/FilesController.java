package com.example.demo.controller;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.entity.Files;
import com.example.demo.service.FilesService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Controller
@RequestMapping(value = "file")
public class FilesController {

    @Autowired
    private FilesService fileService;




    @RequestMapping("/downloadfile")
    public void downloadfile(String fileId,HttpServletResponse response) throws Exception {
        System.out.println("下载："+fileId);//////////////
        Files userFile = fileService.queryFileByID(Integer.parseInt(fileId));
        String originName = userFile.getOriginName();
        String storeName = userFile.getStoreName();
        response.setHeader("Content-Disposition", "attachment;filename=" + originName);
        ServletOutputStream os = response.getOutputStream();
        String path= ParameterConfiguration.FilePath.finalSave;
        File file = new File(path, storeName);
        if(file.exists()){
            byte[] bytes = FileUtils.readFileToByteArray(file);
            os.write(bytes);
            os.flush();
        }
        os.close();
    }

}
