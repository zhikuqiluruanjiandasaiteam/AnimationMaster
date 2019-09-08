from pydub import AudioSegment
import subprocess
import argparse

def vidoe2wav(fromType,fromFilePath,toType,toFileName):
    video = AudioSegment.from_file(fromFilePath, format=fromType)
    file_handle = video.export(toFileName, format=toType)

#合成音频和视频文件
def video_add_wav(wavFilePath,videoPath):
    print('ffmpeg -i '+videoPath+' -i '+wavFilePath+' x'+videoPath)
    subprocess.call('ffmpeg -i '+videoPath+' -i '+wavFilePath+' x'+videoPath,shell = True)#todo:命令可以执行，但画质会变差，不能用

def main():
    parser = argparse.ArgumentParser()
    #parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--from_type', default = None )
    parser.add_argument('--to_type', default = None )
    parser.add_argument('--from_file')#不加默认值，也会默认None
    parser.add_argument('--to_file')
    opt = parser.parse_args()
    if opt.from_type != None:
        vidoe2wav(opt.from_type,opt.from_file,opt.to_type,opt.to_file)
    #else:
    #   video_add_wav(opt.from_file,opt.to_file)
    
main()
print('finsh')
