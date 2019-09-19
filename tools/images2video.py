import cv2
import os
import argparse


def getName(prefix, num_width, suffix, x):
    # print(im_dir+prefix+("{:0>"+str(num_width)+"d}").format(x)+suffix)
    return prefix + ("{:0>" + str(num_width) + "d}").format(x) + suffix


def images2video(im_dir, prefix, num_width, suffix, to_file, reference_video):
    # 图片路径 im_dir
    # 输出视频路径 to_file
    # 图片文件名规则：prefix+int编号+suffix(包括文件类型后缀)
    # 帧率帧数参照视频 reference_video
    # 帧率 fps
    # 图片数 num
    # 图片尺寸 img_size

    cap = cv2.VideoCapture(reference_video)
    fps = cap.get(cv2.CAP_PROP_FPS)  # 帧率
    # fps= round(fps)#四舍五入取整
    num = 0
    while cap.grab():  # 捕获下一帧，成功返回真
        num += 1
    print(fps)
    print(num)

    # im_name = os.path.join(im_dir, getName(prefix,num_width,suffix,1) )
    print('{} -r {} -i {}%0{}d{} -qscale:v 2 {}'.format('ffmpeg', fps, os.path.join(im_dir, prefix), num_width,suffix,to_file))
    os.system('{} -r {} -i {}%0{}d{} -qscale:v 2 {}'.format('ffmpeg', fps, os.path.join(im_dir, prefix), num_width,suffix,to_file))

    # frame = cv2.imread(im_name)#返回矩阵三维数组
    # if frame is not None:
    #     img_size = (len(frame[0]),len(frame))#1维高，2维宽
    # else:
    #     img_size = (1280,720)#视频与图片不符，可能无法生成
    # #print(img_size)
    # #fourcc = cv2.VideoWriter_fourcc('A', 'V', 'C', '1')#mp4格式###linux不支持
    # fourcc = cv2.VideoWriter_fourcc('M', 'P', '4', '2')#mp4格式
    # video_writer = cv2.VideoWriter(to_file, fourcc, fps, img_size)
    #
    # for i in range(1,num+1):
    #     im_name = os.path.join(im_dir, getName(prefix,num_width,suffix,i) )
    #     #print(im_name)
    #     frame = cv2.imread(im_name)#返回矩阵数组
    #     if frame is None:
    #         #print(im_name)
    #         continue
    #     video_writer.write(frame)
    #
    # video_writer.release()


def main():
    parser = argparse.ArgumentParser()
    # parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--image_path')  # 不加默认值，也会默认None
    parser.add_argument('--prefix', default='')
    parser.add_argument('--num_width', default=0)  # 编号宽度，前补0
    parser.add_argument('--suffix', default='.jpg')
    parser.add_argument('--to_file')
    parser.add_argument('--reference_video')
    opt = parser.parse_args()
    images2video(opt.image_path, opt.prefix, opt.num_width, opt.suffix, opt.to_file, opt.reference_video)


main()
# images2video('./dmtpng','',6,'.png','outdmt123.mp4','outdmt123.mp4')
print('finish')
