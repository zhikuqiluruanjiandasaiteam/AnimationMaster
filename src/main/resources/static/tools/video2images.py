import cv2
import os
import argparse

def readKey(keyTxt):
    key=set()
    with open(keyTxt) as f:
        line=f.readline()
        while line:
            if line.strip()=='':
                line=f.readline()
                continue
            i=int(line.split(':')[0])
            if i>1:
                key.add(i-1)
            key.add(i)
            line=f.readline()
    return key

def getFrame(videoPath, svPath,numWidth):
    cap = cv2.VideoCapture(videoPath)
    fps = cap.get(cv2.CAP_PROP_FPS)#帧率
    print(fps)
    isExists=os.path.exists(svPath)# 判断结果
    if not isExists:
        os.makedirs(svPath)
    numFrame = 0
    while cap.grab():#捕获下一帧，成功返回真
            flag, frame = cap.retrieve()
            if not flag:
                break
            else:
                #cv2.imshow('video', frame)
                numFrame += 1
                
                newPath = svPath+'/' + ("{:0>"+str(numWidth)+"d}").format(numFrame) + ".jpg"
                cv2.imencode('.jpg', frame)[1].tofile(newPath)


def getFrame2(videoPath, svPath,numWidth,setKey,intervalNum):
    setKey.add(1)
    cap = cv2.VideoCapture(videoPath)
    sumFrame=0
    while cap.grab():#捕获下一帧，成功返回真
        sumFrame+=1
    setKey.add(sumFrame)
    cap = cv2.VideoCapture(videoPath)
    fps = cap.get(cv2.CAP_PROP_FPS)#帧率
    print(fps)
    isExists=os.path.exists(svPath)# 判断结果
    if not isExists:
        os.makedirs(svPath)
    numFrame = 0
    lastFrame = 1#必须等于这个数，确保第一帧必有
    while cap.grab():#捕获下一帧，成功返回真
            flag, frame = cap.retrieve()
            if not flag:
                break
            else:
                #cv2.imshow('video', frame)
                numFrame += 1
                if numFrame in setKey or numFrame-lastFrame >= intervalNum:
                    newPath = svPath+'/' + ("{:0>"+str(numWidth)+"d}").format(numFrame) + ".jpg"
                    cv2.imencode('.jpg', frame)[1].tofile(newPath)
                    lastFrame=numFrame

def main():
    parser = argparse.ArgumentParser()
    #parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--from_file')#不加默认值，也会默认None
    parser.add_argument('--to_path')
    parser.add_argument('--num_width', default = 0 )#编号宽度，前补0
    #使用部分抽帧
    parser.add_argument('--key_txt', default = None )#关键帧记录txt#内格式形如 16:frame,I
    parser.add_argument('--interval_num', default = 1 )#间隔多少帧必有一帧
    opt = parser.parse_args()
    filePath='dmt'
    getFrame(opt.from_file,opt.to_path,opt.num_width)
    
# main()
keys=readKey('C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\tesxt.txt')
getFrame2('C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\testx.mp4', 'C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\testx',
          7,keys,5)
print('finsh')
