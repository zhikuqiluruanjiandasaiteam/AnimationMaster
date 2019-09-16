#!/usr/bin/env python3
#只插两个文件的中间帧

import argparse
import os
import os.path
import ctypes
import shutil
from shutil import rmtree, move
from PIL import Image
import torch
import torchvision.transforms as transforms
import torch.nn.functional as F
import model
import dataloader
import platform
from tqdm import tqdm

# For parsing commandline arguments
parser = argparse.ArgumentParser()
parser.add_argument("--imageS", type=str, required=True, help='path of video to be converted')
parser.add_argument("--imageE", type=str, required=True, help='path of video to be converted')
parser.add_argument('--out_path')  # 不加默认值，也会默认None
parser.add_argument('--prefix', default='')
parser.add_argument('--num_width', default=6)  # 编号宽度，前补0
parser.add_argument('--num_start', default=0)  # 初始编号，只输出中间帧
parser.add_argument('--suffix', default='.jpg')
parser.add_argument("--checkpoint", type=str, required=True, help='path of checkpoint for pretrained model')
parser.add_argument("--sf", type=int, required=True,
                    help='specify the slomo factor N. This will increase the frames by Nx. Example sf=2 ==> 2x frames')
parser.add_argument("--batch_size", type=int, default=1,
                    help='Specify batch size for faster conversion. This will depend on your cpu/gpu memory. Default: 1')
args = parser.parse_args()


def check():
    """
    Checks the validity of commandline arguments.
    Parameters
    ----------
        None
    Returns
    -------
        error : string
            Error message if error occurs otherwise blank string.
    """

    error = ""
    if (args.sf < 2):
        error = "Error: --sf/slomo factor has to be atleast 2"
    if (args.batch_size < 1):
        error = "Error: --batch_size has to be atleast 1"
    # if (args.fps < 1):
    #     error = "Error: --fps has to be atleast 1"
    return error

def main():
    # Check if arguments are okay
    error = check()
    if error:
        print(error)
        exit(1)

    # Create extraction folder and extract frames
    IS_WINDOWS = 'Windows' == platform.system()
    extractionDir = "tmpSuperSloMo"
    if not IS_WINDOWS:
        # Assuming UNIX-like system where "." indicates hidden directories
        extractionDir = "." + extractionDir
    if os.path.isdir(extractionDir):
        rmtree(extractionDir)
    os.mkdir(extractionDir)
    if IS_WINDOWS:
        FILE_ATTRIBUTE_HIDDEN = 0x02
        # ctypes.windll only exists on Windows
        ctypes.windll.kernel32.SetFileAttributesW(extractionDir, FILE_ATTRIBUTE_HIDDEN)

    extractionPath = os.path.join(extractionDir, "input")
    # outputPath = os.path.join(extractionDir, "output")
    outputPath=args.out_path
    os.mkdir(extractionPath)
    # os.mkdir(outputPath)

    shutil.copy(args.imageS,os.path.join(extractionPath, '{}/%06d.jpg'.format(1)))
    shutil.copy(args.imageE, os.path.join(extractionPath, '{}/%06d.jpg'.format(2)))

    # error = extract_frames(args.video, extractionPath)
    # if error:
    #     print(error)
    #     exit(1)

    # Initialize transforms
    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

    mean = [0.429, 0.431, 0.397]
    std = [1, 1, 1]
    normalize = transforms.Normalize(mean=mean,
                                     std=std)

    negmean = [x * -1 for x in mean]
    revNormalize = transforms.Normalize(mean=negmean, std=std)

    # Temporary fix for issue #7 https://github.com/avinashpaliwal/Super-SloMo/issues/7 -
    # - Removed per channel mean subtraction for CPU.
    if (device == "cpu"):
        transform = transforms.Compose([transforms.ToTensor()])
        TP = transforms.Compose([transforms.ToPILImage()])
    else:
        transform = transforms.Compose([transforms.ToTensor(), normalize])
        TP = transforms.Compose([revNormalize, transforms.ToPILImage()])

    # Load data
    videoFrames = dataloader.Video(root=extractionPath, transform=transform)
    videoFramesloader = torch.utils.data.DataLoader(videoFrames, batch_size=args.batch_size, shuffle=False)

    # Initialize model
    flowComp = model.UNet(6, 4)
    flowComp.to(device)
    for param in flowComp.parameters():
        param.requires_grad = False
    ArbTimeFlowIntrp = model.UNet(20, 5)
    ArbTimeFlowIntrp.to(device)
    for param in ArbTimeFlowIntrp.parameters():
        param.requires_grad = False

    flowBackWarp = model.backWarp(videoFrames.dim[0], videoFrames.dim[1], device)
    flowBackWarp = flowBackWarp.to(device)

    dict1 = torch.load(args.checkpoint, map_location='cpu')
    ArbTimeFlowIntrp.load_state_dict(dict1['state_dictAT'])
    flowComp.load_state_dict(dict1['state_dictFC'])

    # Interpolate frames
    frameCounter = args.num_start

    with torch.no_grad():
        for _, (frame0, frame1) in enumerate(tqdm(videoFramesloader), 0):

            I0 = frame0.to(device)
            I1 = frame1.to(device)

            flowOut = flowComp(torch.cat((I0, I1), dim=1))
            F_0_1 = flowOut[:, :2, :, :]
            F_1_0 = flowOut[:, 2:, :, :]

            # Save reference frames in output folder
            # for batchIndex in range(args.batch_size):
            #     (TP(frame0[batchIndex].detach())).resize(videoFrames.origDim, Image.BILINEAR).save(
            #         os.path.join(outputPath, str(frameCounter + args.sf * batchIndex) + ".jpg"))
            # frameCounter += 1

            # Generate intermediate frames
            for intermediateIndex in range(1, args.sf):
                t = float(intermediateIndex) / args.sf
                temp = -t * (1 - t)
                fCoeff = [temp, t * t, (1 - t) * (1 - t), temp]

                F_t_0 = fCoeff[0] * F_0_1 + fCoeff[1] * F_1_0
                F_t_1 = fCoeff[2] * F_0_1 + fCoeff[3] * F_1_0

                g_I0_F_t_0 = flowBackWarp(I0, F_t_0)
                g_I1_F_t_1 = flowBackWarp(I1, F_t_1)

                intrpOut = ArbTimeFlowIntrp(
                    torch.cat((I0, I1, F_0_1, F_1_0, F_t_1, F_t_0, g_I1_F_t_1, g_I0_F_t_0), dim=1))

                F_t_0_f = intrpOut[:, :2, :, :] + F_t_0
                F_t_1_f = intrpOut[:, 2:4, :, :] + F_t_1
                V_t_0 = F.sigmoid(intrpOut[:, 4:5, :, :])
                V_t_1 = 1 - V_t_0

                g_I0_F_t_0_f = flowBackWarp(I0, F_t_0_f)
                g_I1_F_t_1_f = flowBackWarp(I1, F_t_1_f)

                wCoeff = [1 - t, t]

                Ft_p = (wCoeff[0] * V_t_0 * g_I0_F_t_0_f + wCoeff[1] * V_t_1 * g_I1_F_t_1_f) / (
                            wCoeff[0] * V_t_0 + wCoeff[1] * V_t_1)

                # Save intermediate frame
                for batchIndex in range(args.batch_size):
                    (TP(Ft_p[batchIndex].cpu().detach())).resize(videoFrames.origDim, Image.BILINEAR).save(
                        os.path.join(outputPath, ('{}{:'+args.num_width+'d}{}').format(args.prefix,(frameCounter + args.sf * batchIndex),args.suffix)))
                frameCounter += 1

            # Set counter accounting for batching of frames
            frameCounter += args.sf * (args.batch_size - 1)

    # Generate video from interpolated frames
    # create_video(outputPath)
    # Remove temporary files
    rmtree(extractionDir)

    exit(0)

main()