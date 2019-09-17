package com.example.demo.util;

import com.example.demo.config.ParameterConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

//补帧只能用jpg类型
public class PatchFrameUtil {

    private Queue<PfImage> queue;
    private String prefix;
    private int numWidth;
    private String suffix;

    private boolean canAdd =true;
    private boolean over=false;

    //补帧模型路径，与主文件同级
    private String model="checkpoint/SuperSloMo.ckpt";

    public PatchFrameUtil(String prefix,int numWidth,String suffix){
        this.prefix=prefix;
        this.numWidth=numWidth;
        this.suffix=suffix;
        queue = new LinkedList<PfImage>();
    }

    public void run(){
        canAdd=true;
        new Thread(new Runnable() {
            public void run() {
                while(canAdd ||queue.peek()!=null) {//返回第一个元素
                    if(queue.peek()==null){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                        continue;
                    }
                    PfImage pfImage=queue.poll();//返回第一个元素，并在队列中删除
                    patchFrame(pfImage);
                }
                over=true;
            }
        }).start();
    }

    public void add(int numStart, int pfNumber, String startImage, String endImage){
        queue.offer(new PfImage( numStart,pfNumber,startImage,endImage));
    }

    public void close(){
        canAdd=false;
        while(!over) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private int patchFrame(PfImage pfImage){
        String toolStr= ParameterConfiguration.Tools.pfMainFile;
        //python video_to_slomo.py --imageS s.jpg --imageE e.jpg --sf 8 --checkpoint ../download/SuperSloMo.ckpt --out_path ../download/output
        //--prefix   --num_width  6 --num_start 1  --suffix .jpg
        String strShell="python "+toolStr+" --imageS "+pfImage.startImage+" --imageE "+pfImage.endImage
                +" --sf " +(pfImage.pfNumber+1) +" --checkpoint "+model
                +" --num_width "+numWidth+" --num_start "+pfImage.numStart+" --suffix "+suffix;
        if(prefix!=null&&!prefix.equals( "" )){
            strShell+=" --prefix "+prefix;
        }
        int re=-1;
        System.out.println(strShell);
        re = AudioProcessing.runExec(strShell);
        return re;
    }

    public class PfImage{
        public int numStart;
        public int pfNumber;//补帧个数
        public String startImage;
        public String endImage;

        public PfImage(int numStart, int pfNumber, String startImage, String endImage) {
            this.numStart = numStart;
            this.pfNumber = pfNumber;
            this.startImage = startImage;
            this.endImage = endImage;
        }
    }

}
