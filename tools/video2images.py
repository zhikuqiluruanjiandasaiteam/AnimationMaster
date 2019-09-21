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

        f.close()
    return key
#不补帧，全拆
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
                
                newPath =  os.path.join(svPath,("{:0>"+str(numWidth)+"d}").format(numFrame) + ".jpg")
                cv2.imencode('.jpg', frame)[1].tofile(newPath)

#补帧，拆转场帧，#todo: 暂时没有转场判断，拆关键帧，需传关键帧记录
def getFrame2(videoPath, svPath,numWidth,keyTxt,intervalNum,namesOutTxt):
    setKey=readKey(keyTxt)
    setKey.add(1)
    outNames = open(namesOutTxt, 'w')#输出帧名到文件
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
    lastFrame = 1
    while cap.grab():#捕获下一帧，成功返回真
            flag, frame = cap.retrieve()
            if not flag:
                break
            else:
                #cv2.imshow('video', frame)
                numFrame += 1
                if numFrame in setKey or numFrame-lastFrame >= intervalNum:
                    str0=("{:0>"+str(numWidth)+"d}").format(numFrame) + ".jpg"
                    newPath = os.path.join(svPath, str0)
                    outNames.write(str0+"\n")
                    cv2.imencode('.jpg', frame)[1].tofile(newPath)
                    lastFrame=numFrame
    outNames.close()

#只提取第一张图
def getFrame3(videoPath, svFile):
    cap = cv2.VideoCapture(videoPath)
    while cap.grab():  # 捕获下一帧，成功返回真
        flag, frame = cap.retrieve()
        if not flag:
            break
        else:
            cv2.imencode('.jpg', frame)[1].tofile(svFile)
            break

def main():
    parser = argparse.ArgumentParser()
    #parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--from_file')#不加默认值，也会默认None
    parser.add_argument('--to_path')
    parser.add_argument('--num_width', default = 0 )#编号宽度，前补0
    #使用部分抽帧
    parser.add_argument('--key_txt', default = None )#关键帧记录txt#内格式形如 16:frame,I
    parser.add_argument('--interval_num', default = 1 )#间隔多少帧必有一帧
    parser.add_argument('--names_outtxt', default='names.txt')  # 抽帧文件名输出，只输出文件名
    #只拆第一张图
    parser.add_argument('--frist_to', default=None)
    opt = parser.parse_args()
    if opt.key_txt !=None:
        getFrame2(opt.from_file, opt.to_path, opt.num_width, opt.key_txt, int(opt.interval_num), opt.names_outtxt)
    elif opt.frist_to!=None:
        getFrame3(opt.from_file, opt.frist_to)
    else:
        getFrame(opt.from_file, opt.to_path, opt.num_width)

main()
print('finsh')
