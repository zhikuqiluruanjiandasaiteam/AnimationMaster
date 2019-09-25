package com.example.demo.util;

import com.example.demo.config.ParameterConfiguration;
import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TaskQueue {
    private static Queue<Task> qTask = new LinkedList<Task>();
    private static Queue<String> qFName =new LinkedList<String>();;
    private static Queue<TaskService> qTakeS =new LinkedList<TaskService>();;//一个任务对应一个任务类，不能使用static唯一任务类
    private static boolean canAdd =true;
    private static boolean isRun=false;

    public static void run(){
        canAdd=true;
        if(isRun)
            return;
        new Thread(new Runnable() {
            public void run() {
                isRun=true;
                while(canAdd ||qTask.peek()!=null) {//返回第一个元素
                    if(qTask.peek()==null){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                        continue;
                    }
                    if(ParameterConfiguration.taskNum<2){//最多两个任务执行
                        Task task=qTask.poll();//返回第一个元素，并在队列中删除
                        String fileName=qFName.poll();
                        TaskService taskService=qTakeS.poll();
                        if(task==null||fileName==null||taskService==null)
                            continue;
                        taskService.startTask( fileName, task);
                        ParameterConfiguration.taskNum++;
                    }
                }
                isRun=false;
            }
        }).start();
    }

    public static void add(String fileName,Task task,TaskService taskService){
        qTask.offer(task);
        qFName.offer( fileName );
        qTakeS.offer( taskService );
    }

    public static int close(){
        canAdd=false;
        while(isRun) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return 0;
    }
}
