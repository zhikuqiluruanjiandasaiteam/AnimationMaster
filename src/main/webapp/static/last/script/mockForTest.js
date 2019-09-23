// 使用 Mock
var Random = Mock.Random;
//随机返回20到100的数字
Mock.mock('/user/login', {
    "error_msg": "用户名为空！",
    "error_code": 0,
});

Mock.mock('/user/register', {
    "error_code": 0,
});

Mock.mock('/style/list', {
    "error_msg": "",
    "data": {
        "image": [
            {
                "ims_name": "原画",
                "ims_description": "1",
                "ims_id": 1,
                "ims_estimated_time": 0,
                "ims_show_path": "1"
            },
            {
                "ims_name": "新海诚风",
                "ims_description": "2",
                "ims_id": 2,
                "ims_estimated_time": 100,
                "ims_show_path": "1"
            }
        ],
        "clarity": [
            720,
            480,
            360,
            240
        ],
        "patch_frame": {
            "used_count": "0",
            "frame_patch_rate": "0.0",
            "estimated_time": "0"
        },
        "audio": [
            {
                "aus_description": "1",
                "aus_id": 1,
                "aus_estimated_time": 112823,
                "aus_name": "原音"
            },
            {
                "aus_description": "2",
                "aus_id": 2,
                "aus_estimated_time": 192117,
                "aus_name": "小黄人音"
            }
        ]
    },
    "error_code": 0
})
// 输出结果
// console.log(JSON.stringify(data, null, 4))