package com.example.demo.service;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.dao.FilesMapper;
import com.example.demo.entity.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Service
public class FilesService {

    @Autowired
    private FilesMapper filesMapper;

    /**
     * 储存上传文件并创建记录
     * @param type 转换模式，用以检查文件类型是否可以转化
     * @param file 文件
     * @param userId 用户id
     * @return 文件记录id
     * @throws IOException .
     */
    public Files createFile(String type, MultipartFile file, Integer userId) throws IOException {
        String storeName=saveFile(type,file);
        if(storeName==null)
            return null;//储存失败
        String originName=file.getOriginalFilename();
        Files files=new Files();
        files.setFileType( type );
        files.setOriginName(originName);
        files.setStoreName( storeName );
        files.setUploadTime( new Date() );
        files.setUserId( userId );
        filesMapper.insertGetId( files );
        return files;
    }

    /**
     * 储存文件
     * @param type 转换模式，用以检查文件类型是否可以转化
     * @param file 文件
     * @return 储存文件名（带后缀）
     * @throws IOException 。
     */
    private String saveFile(String type,MultipartFile file) throws IOException {
        String saveName=null;
        if(file==null)
            return null;
        String filename = file.getOriginalFilename();
        //检查文件类型是否合法
        if(!checkFile(type,filename))
            return null;
        //储存文件夹不存在则创建
        File dir =new File( ParameterConfiguration.FilePath.uploadSava);
        if  (!dir .exists()&&!dir .isDirectory()) {
            dir .mkdirs();
        }
        String suffix = filename.substring(filename.lastIndexOf('.')+1);
        saveName=getUniqueStr()+"."+suffix;
        String path=ParameterConfiguration.FilePath.uploadSava+File.separator+saveName;
        //上传
        file.transferTo(new File(path));

        return saveName;
    }

    /**
     *检查文件是否符合要求
     * @param type 转换模式，用以检查文件类型是否可以转化
     * @param filename 文件名
     * @return bool
     * todo:文件类型判断目前只用了通过后缀名判断，还需补充检查文件真实类型与后缀名是否一致
     */
    private boolean checkFile(String type,String filename){
        boolean re=false;
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf('.')+1);
        String[] strs= ParameterConfiguration.Type.fileName.get(type);
        for(String str:strs){
            if(str.equals( suffix )){//if(suffix.matches(type_reg)){
                re=true;
                break;
            }
        }
        return re;
    }

    //加锁可能会使整个类加锁
    public synchronized String getUniqueStr(){
        long nTime=System.nanoTime();//纳秒时间戳，不是以固定的开时间开始，可能会重复
        return ""+System.currentTimeMillis()+(nTime%1000000);
    }

    public Files queryFileByID(int id) {
        return  filesMapper.selectByPrimaryKey(id);
    }
}