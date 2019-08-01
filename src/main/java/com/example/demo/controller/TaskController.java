package com.example.demo.controller;

import com.example.demo.config.ParameterConfiguration;

import com.example.demo.service.FilesService;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "task")
public class TaskController {

    @Autowired
    private FilesService filesService;
    @Autowired
    private TaskService taskService;

    /**
     *创建任务
     * @param file
     * @param userId
     * @param image
     * @param audio
     * @param clarity
     * @param type
     * @param estimateTime
     * @param isFrameSpeed 1表示true
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "create",method = RequestMethod.POST)
    public Map create(@RequestParam("file") MultipartFile file,@RequestParam("usr_id") Integer userId,
                      @RequestParam("ims_id") String image, @RequestParam("aus_id") String audio,
                      @RequestParam("clarity") String clarity, @RequestParam("type") String type,
                      @RequestParam("estimate_time")Integer estimateTime,@RequestParam("is_frame_speed")Integer isFrameSpeed)
            throws Exception {
        HashMap re=new HashMap(  );
        re.put("data","");
        Integer imsId=null,
                ausId=null,
                clarityVaule=null;
        if(userId==null){
            re.put("error_code",1);
            re.put("error_msg","没有用户id");
            return re;
        }
        if(type.equals( ParameterConfiguration.Type.video )){
            if(image==null||audio==null||clarity==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择图片或音频风格或清晰度");
                return re;
            }
            imsId=Integer.parseInt( image );
            ausId=Integer.parseInt( audio );
            clarityVaule=Integer.parseInt( clarity );
        }else if(type.equals( ParameterConfiguration.Type.image )){
            if(image==null||clarity==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择图片风格或清晰度");
                return re;
            }
            imsId=Integer.parseInt( image );
            clarityVaule=Integer.parseInt( clarity );
        }else if(type.equals( ParameterConfiguration.Type.audio )){
            if(audio==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择音频风格");
                return re;
            }
            ausId=Integer.parseInt( audio );
        }else{
            re.put("error_code",2);
            re.put("error_msg","没有这种模式");
            return re;
        }
        Integer filesId=filesService.createFile( type,file,userId );
        if(filesId==null){
            re.put("error_code",3);
            re.put("error_msg","上传失败或文件类型不符合选择转换模式要求");
            return re;
        }
        taskService.createTask( userId,
                filesId,
                imsId,
                ausId,
                clarityVaule,
                estimateTime,
                isFrameSpeed==1,
                type);
        //开启任务

        re.put("error_code",0);
        re.put("error_msg","");

        return re;
    }
}
