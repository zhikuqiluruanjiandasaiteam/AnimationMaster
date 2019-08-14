import cv2
import os
import argparse

def getFrame(videoPath, svPath,numWidth):
    cap = cv2.VideoCapture(videoPath)
    fps = cap.get(cv2.CAP_PROP_FPS)#帧率
    print(fps)
    numFrame = 0
    while cap.grab():#捕获下一帧，成功返回真
            flag, frame = cap.retrieve()
            if not flag:
                break
            else:
                #cv2.imshow('video', frame)
                numFrame += 1
                isExists=os.path.exists(svPath)# 判断结果
                if not isExists:
                    os.makedirs(svPath)
                newPath = svPath+'/' + ("{:0>"+str(numWidth)+"d}").format(numFrame) + ".jpg"
                cv2.imencode('.jpg', frame)[1].tofile(newPath)
                
def main():
    parser = argparse.ArgumentParser()
    #parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--from_file')#不加默认值，也会默认None
    parser.add_argument('--to_path')
    parser.add_argument('--num_width', default = 0 )#编号宽度，前补0
    opt = parser.parse_args()
    filePath='dmt'
    getFrame(opt.from_file,opt.to_path,opt.num_width)
    
main()
print('finsh')
