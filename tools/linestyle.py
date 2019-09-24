import os
import argparse
from PIL import Image, ImageFilter, ImageOps
def dodge(a, b, alpha):
    return min(int(a*255/(256-b*alpha)), 255)
def draw(img,to_file,blur=25, alpha=1.0):
    img1 = img.convert('L')        #图片转换成灰色
    img2 = img1.copy()
    img2 = ImageOps.invert(img2)
    for i in range(blur):          #模糊度
        img2 = img2.filter(ImageFilter.BLUR)
    width, height = img1.size
    for x in range(width):
        for y in range(height):
            a = img1.getpixel((x, y))
            b = img2.getpixel((x, y))
            img1.putpixel((x, y), dodge(a, b, alpha))
    img1.save(to_file)

def fa2(img,to_file):
    img_all =  to_file
    new = Image.new("L", img.size, 255)
    width, height = img.size
    img = img.convert("L")
     
    # 定义画笔的大小
    Pen_size = 3
    # 色差扩散器
    Color_Diff = 6
    for i in range(Pen_size + 1, width - Pen_size - 1):
        for j in range(Pen_size + 1, height - Pen_size - 1):
            # 原始的颜色
            originalColor = 255
            lcolor = sum([img.getpixel((i - r, j)) for r in range(Pen_size)]) // Pen_size
            rcolor = sum([img.getpixel((i + r, j)) for r in range(Pen_size)]) // Pen_size
     
            # 通道----颜料
            if abs(lcolor - rcolor) > Color_Diff:
                originalColor -= (255 - img.getpixel((i, j))) // 4
                new.putpixel((i, j), originalColor)
     
            ucolor = sum([img.getpixel((i, j - r)) for r in range(Pen_size)]) // Pen_size
            dcolor = sum([img.getpixel((i, j + r)) for r in range(Pen_size)]) // Pen_size
     
            # 通道----颜料
            if abs(ucolor - dcolor) > Color_Diff:
                originalColor -= (255 - img.getpixel((i, j))) // 4
                new.putpixel((i, j), originalColor)
     
            acolor = sum([img.getpixel((i - r, j - r)) for r in range(Pen_size)]) // Pen_size
            bcolor = sum([img.getpixel((i + r, j + r)) for r in range(Pen_size)]) // Pen_size
     
            # 通道----颜料
            if abs(acolor - bcolor) > Color_Diff:
                originalColor -= (255 - img.getpixel((i, j))) // 4
                new.putpixel((i, j), originalColor)
     
            qcolor = sum([img.getpixel((i + r, j - r)) for r in range(Pen_size)]) // Pen_size
            wcolor = sum([img.getpixel((i - r, j + r)) for r in range(Pen_size)]) // Pen_size
     
            # 通道----颜料
            if abs(qcolor - wcolor) > Color_Diff:
                originalColor -= (255 - img.getpixel((i, j))) // 4
                new.putpixel((i, j), originalColor)
 
    new.save(img_all)

def change(from_file,to_file,clearity,suffix):
    img = Image.open(from_file)
    h=img.size[0]#高
    w=img.size[1]#宽
    if(h>w):
        w=round(1.0*w/h*clearity)
        h=clearity
    else:
        h=int(1.0*h/w*clearity)
        w=clearity
    img = img.resize((h, w), Image.ANTIALIAS)
    if suffix=='_line':
        draw(img,to_file)
    else:
        fa2(img,to_file)
    #im=Image.open(from_file)
    #om=im.filter(ImageFilter.CONTOUR)
    #om.save(to_file)
#文件名最多只能有一个'.'
def all2(from_file,to_path,clearity,suffix):
    listf = os.listdir(from_file) #列出文件夹下所有的目录与文件
    for i in range(0,len(listf)):
        lstr=listf[i].split('.')
        if len(lstr)>1 and lstr[1]=='jpg':
            path = os.path.join(from_file,listf[i])
            change(path,os.path.join(to_path,lstr[0]+suffix+'.'+lstr[1]),clearity,suffix)

def main():
    parser = argparse.ArgumentParser()
    #parser.add_argument('--gpu', type=int, default = 0)
    parser.add_argument('--from_file')#不加默认值，也会默认None
    parser.add_argument('--to_path')
    parser.add_argument('--clearity',default='')#清晰度，长边长
    parser.add_argument('--suffix', default ='_line')
    parser.add_argument('--to_file', default = None )
    opt = parser.parse_args()
    if opt.to_file==None:
        all2(opt.from_file,opt.to_path,int(opt.clearity),opt.suffix)
    else:
        change(opt.from_file,opt.to_file,int(opt.clearity),opt.suffix)

all2('C:\\Users\\Think\\Desktop\\智库齐软大赛\\工作台\\视频\\newx','E:\\Workbench\\IDLE\\Python实验\\实验7\\w7-4out',720,'_line')
# main()
print('finish')
