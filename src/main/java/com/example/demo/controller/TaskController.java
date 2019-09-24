package com.example.demo.controller;

import com.example.demo.config.ParameterConfiguration;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.service.FilesService;
import com.example.demo.service.TaskService;
import com.example.demo.util.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.entity.Files;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/task")
public class TaskController {

    @Autowired
    private FilesService filesService;
    @Autowired
    private TaskService taskService;

    /**
     *创建任务
     * @param file
     *
     * @param imsId
     * @param ausId
     * @param clarity
     * @param type
     * @param estimateTime
     * @param isFrameSpeed 1表示true
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "create",method = RequestMethod.POST)
    public Map create(@RequestParam(value = "file", required = true) MultipartFile file,
                      @RequestParam(value ="ims_id", required = false) Integer imsId,
                      @RequestParam(value ="aus_id", required = false) Integer ausId,
                      @RequestParam(value ="clarity", required = false) Integer clarity,
                      @RequestParam(value ="type", required = true) String type,
                      @RequestParam(value ="estimate_time", required = true)Integer estimateTime,
                      @RequestParam(value ="is_frame_speed", required = false,defaultValue = "0")Boolean isFrameSpeed, HttpSession session)
            throws Exception {
        HashMap<String,Object> re=new HashMap<>(  );
        re.put("data","");
        if(type.equals( ParameterConfiguration.Type.video )){
            if(imsId==null||ausId==null||clarity==null||isFrameSpeed==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择图片或音频风格或清晰度或是否补帧加速");
                return re;
            }
        }else if(type.equals( ParameterConfiguration.Type.image )){
            if(imsId==null||clarity==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择图片风格或清晰度");
                return re;
            }
        }else if(type.equals( ParameterConfiguration.Type.audio )){
            if(ausId==null){
                re.put("error_code",1);
                re.put("error_msg","没有选择音频风格");
                return re;
            }
        }else{
            re.put("error_code",2);
            re.put("error_msg","没有这种模式");
            return re;
        }
        User currentUser=(User)session.getAttribute("currentUser");
        Files saveFiles=filesService.createFile( type,file,currentUser.getUserId() );
        if(saveFiles==null){
            re.put("error_code",3);
            re.put("error_msg","上传失败或文件类型不符合选择转换模式要求");
            return re;
        }
        Task task=taskService.createTask( currentUser.getUserId(), saveFiles.getFileId(), imsId, ausId, clarity, estimateTime,
                isFrameSpeed, type);
        if(task==null){
            re.put("error_code",3);
            re.put("error_msg","任务创建失败");
            return re;
        }
        //开启任务//任务过多需要排队
        TaskQueue.add(saveFiles.getStoreName(),task,taskService);
        TaskQueue.run();
//        taskService.startTask( saveFiles.getStoreName(),task);

        re.put("error_code",0);
        re.put("error_msg","");

        return re;
    }

    /**
     * 获取任务列表
     *
     *@param isDesc true("true"/1)：按创建时间倒序排序，false("false"/0)；正序排序
     * @param finishState 1：完成，-1：未完成，0:全部
     * @return 任务列表
     */
    @ResponseBody
    @RequestMapping(value = "list")
    public Map list(@RequestParam(value ="is_desc", required = false,defaultValue = "true") boolean isDesc,
                    @RequestParam(value ="finish_state", required = false,defaultValue = "0") Integer finishState,HttpSession session){
        HashMap<String,Object> re=new HashMap<>();
        User currentUser=(User)session.getAttribute("currentUser");
        Map[] data=taskService.getList( currentUser.getUserId(),isDesc,finishState==0,finishState==1 );
        if(data==null){
            re.put("error_code",3);
            re.put("error_msg","查询失败");
            re.put("data","");
            return re;
        }

        re.put("error_code",0);
        re.put("error_msg","");
        re.put("data",data);
        return re;
    }


    /**
     *
     * @param taskId  每处理完一张图片返回的通知(任务Id)
     * @param fileName 每处理完一张图片返回的通知(处理后的文件名：默认为 初始文件名_风格名.jpg)
     */
    @RequestMapping("/inform")
    public void inform(Integer taskId,String fileName){
        System.out.println(taskId+"----------"+fileName);
    }

}
