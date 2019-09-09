/// <reference path="./sweetalert2.js"/>
/// <reference path="./jquery.js"/>
;
(function () {
	'use strict';
	// Placeholder
	var placeholderFunction = function () {
		$('input, textarea').placeholder({
			customClass: 'my-placeholder'
		});
	}
	// Placeholder
	var contentWayPoint = function () {
		var i = 0;
		$('.animate-box').waypoint(function (direction) {
			if (direction === 'down' && !$(this.element).hasClass('animated-fast')) {
				i++;
				$(this.element).addClass('item-animate');
				setTimeout(function () {
					$('body .animate-box.item-animate').each(function (k) {
						var el = $(this);
						setTimeout(function () {
							var effect = el.data('animate-effect');
							if (effect === 'fadeIn') {
								el.addClass('fadeIn animated-fast');
							} else if (effect === 'fadeInLeft') {
								el.addClass('fadeInLeft animated-fast');
							} else if (effect === 'fadeInRight') {
								el.addClass('fadeInRight animated-fast');
							} else {
								el.addClass('fadeInUp animated-fast');
							}
							el.removeClass('item-animate');
						}, k * 200, 'easeInOutExpo');
					});
				}, 100);
			}
		}, {
			offset: '85%'
		});
	};
	// On load
	$(function () {
		placeholderFunction();
		contentWayPoint();
	});
}());

//设置全局变量
var testVar={};
var userInfo = {
	 userId: 0,
	 username: ""
}
//判断是否处于登陆状态
var isLogin = function () {
	$.ajax({
		type: "POST",
		url: "/user/getSession",
	}).done((resultdata) => {
		let error_code = resultdata.error_code;
		switch (error_code) {
			case 0:
				console.log(resultdata.data);
				if (resultdata.data !== null) {
					userInfo = resultdata.data;
				} else;
			// case -1:
			// 	Swal.fire({
			// 		// toast: true,
			// 		// position: 'top',
			// 		showConfirmButton: true,
			// 		timer: 2000,
			// 		type: 'info',
			// 		title: "出错了",
			// 	});
			// 	break;
		}
	});
   if(userInfo.userId!==0&&userInfo!=null){
   	console.log("islogin");
   	return true;
   }else {
   	console.log("islogin");
   	return false;
   }
	//let username;
	// let myCookie=document.cookie
	// let myCookie="u"

	// if (myCookie !== "") {
	// 	return true;
	// }
	// else
	// 	return false;
};


//切换登陆与未登录状态后的变化
// 登陆状态与未登录状态的区别:
// 1.任务列表的跳转按钮
// 2.右上角 登录注册 修改为 用户名 退出登陆
// 3.input部分的删除与生成 

var toggleLoginState = function () {
	if (isLogin()) {
		console.log('login!');
		document.getElementById('turn-to-list').style.display = 'inline-block';
		$('#menuList').empty();
		let menuItem_1 = document.createElement("li");
		menuItem_1.className = "pure-menu-item";
		//TODO:暂时不知道对不对
		let username = userInfo.username;
		menuItem_1.innerHTML = "欢迎您: "+username;
		$('#menuList').append(menuItem_1);
		let menuItem_2 = document.createElement("li");
		menuItem_2.className = "pure-menu-item";
		menuItem_2.innerHTML = '退出';
		$('#menuList').append(menuItem_2);

		//创建input节点
		if (document.getElementById('fileUpload') === null) {
			let input = document.createElement('input');
			input.type = "file";
			input.accept = "image/png,image/jpeg,audio/mp3, audio/mp4, video/mp4";
			input.id = "fileUpload";
			$('#icon-upload').append(input);
            //为input绑定事件
            $("#fileUpload").change(function () {
                console.log($("#fileUpload")[0].files);
                let file = $("#fileUpload")[0].files[0];
                //如果不合格式，重新选文件
                if (
                    file.type !== "image/png" &&
                    file.type !== "image/jpeg" &&
                    file.type !== "audio/mp3" &&
                    file.type !== "audio/mp4" &&
                    file.type !== "video/mp4"
                ) {
                    Swal.fire({
                        // toast: true,
                        // position: 'top',
                        showConfirmButton: true,
                        allowOutsideClick: false, //点击背景不会关闭
                        type: "warning",
                        title: "不支持该格式",
                        text: "仅支持mp4，jpg，jpeg，png，mp3,请重新选择"
                    });
                }
                else if (file.size > 400 * 1000 * 1000) {
                    Swal.fire({
                        // toast: true,
                        // position: 'top',
                        showConfirmButton: true,
                        allowOutsideClick: false, //点击背景不会关闭
                        type: "warning",
                        title: "文件太大了",
                        text: "请尝试上传小于400M的文件"
                    });
                }
                //符合要求的格式
                else {
                    fileInformation.name = file.name;
                    fileInformation.type = file.type;
                    fileInformation.uploadFile = file;
                    console.log(fileInformation);
                    //"image/png,image/jpeg,audio/mpeg,audio/mp4, video/mp4"
                    //传的是图片
                    if (file.type === "image/png" || file.type === "image/jpeg") {
                        let reader = new FileReader();
                        reader.readAsDataURL(file); //这里把一个文件用base64编码
                        reader.onload = function (e) {
                            let img = new Image();
                            img.src = e.target.result; //获取编码后的值,也可以用this.result获取
                            img.onload = function () {
                                console.log("height:" + this.height + "----width:" + this.width);
                                document.getElementById("uploadFilename").innerHTML =
                                    fileInformation.name;
                                fileInformation.height = this.height;
                                fileInformation.width = this.width;
                                fileInformation.duration = 0;
                            };
                        };
                    }
                    //传的是视频
                    else if (file.type === "video/mp4") {
                        //等待视频数据加载弹框
                        Swal.fire({
                            // toast: true,
                            // position: 'top',
                            showConfirmButton: false,
                            allowOutsideClick: false, //点击背景不会关闭
                            type: "info",
                            title: "文件正在处理中..."
                        });

                        let reader = new FileReader();
                        reader.readAsDataURL(file); //这里把一个文件用base64编码
                        reader.onload = function (e) {
                            var dataUrl = reader.result;
                            var videoId = "videoMain";
                            var $videoEl = $('<video id="' + videoId + '"></video>');
                            // $("body").append($videoEl);
                            $videoEl.attr("src", dataUrl);
                            var videoTagRef = $videoEl[0];

                            //媒体（音频、视频）相关事件loadstart、loadmetadata
                            //参考https://developer.mozilla.org/zh-CN/docs/Web/Guide/Events/Media_events

                            videoTagRef.addEventListener("loadedmetadata", function (e) {
                                console.log(
                                    videoTagRef.videoWidth,
                                    videoTagRef.videoHeight,
                                    videoTagRef.duration
                                );
                                document.getElementById("uploadFilename").innerHTML =
                                    fileInformation.name;
                                fileInformation.height = videoTagRef.videoHeight;
                                fileInformation.width = videoTagRef.videoWidth;
                                fileInformation.duration = videoTagRef.duration;
                                swal.close(); //元数据加载完成后自动关闭对话框
                            });
                        };
                    }
                    //传的是音频
                    else if (file.type === "audio/mp3") {
                        console.log("mp3加载中");
                        //等待视频数据加载弹框
                        Swal.fire({
                            // toast: true,
                            // position: 'top',
                            showConfirmButton: false,
                            allowOutsideClick: false, //点击背景不会关闭
                            type: "info",
                            title: "文件正在处理中..."
                        });

                        let reader = new FileReader();
                        reader.readAsDataURL(file); //这里把一个文件用base64编码
                        reader.onload = function (e) {
                            let dataUrl = reader.result;
                            let audioId = "videoMain";
                            let $audioEl = $('<audio id="' + audioId + '"></audio>');
                            // $("body").append($videoEl);
                            $audioEl.attr("src", dataUrl);
                            let audioTagRef = $audioEl[0];

                            //媒体（音频、视频）相关事件loadstart、loadmetadata
                            audioTagRef.addEventListener("loadedmetadata", function (e) {
                                console.log(audioTagRef.duration);
                                document.getElementById("uploadFilename").innerHTML =
                                    fileInformation.name;
                                fileInformation.height = 1;
                                fileInformation.width = 1;
                                fileInformation.duration = audioTagRef.duration;

                                swal.close(); //元数据加载完成后自动关闭对话框
                            });
                        };
                    }

                    //TODO:暂且不用自己判断，用户决定
                    //是否需要用户选择类型？？？展示对话框之前需要做一下判断.暂且不用自己判断，用户决定
                    showModal("styleContainer");
                }
                //"image/png,image/jpeg,audio/mpeg,audio/mp4, video/mp4"
                // showModal('styleContainer');
                //document.getElementById('uploadFilename').innerHTML = fileInformation.name;
                //showModal('styleContainer');
            });
		}
		document.getElementById('icon-upload').style.cursor = "none";
	} else {
		console.log('not login');
		document.getElementById('turn-to-list').style.display = 'none';
		$('#menuList').empty();
		let menuItem_1 = document.createElement("li");
		menuItem_1.className = "pure-menu-item";
		menuItem_1.id = 'login-button';
		menuItem_1.innerHTML = '<a href="#" class="pure-menu-link">登陆</a>';
		$('#menuList').append(menuItem_1);

		let menuItem_2 = document.createElement("li");
		menuItem_2.className = "pure-menu-item";
		menuItem_2.id = 'register-button';
		menuItem_2.innerHTML = '<a href="#" class="pure-menu-link">注册</a>';
		$('#menuList').append(menuItem_2);

		//删除input节点
		if (document.getElementById('fileUpload') !== null) {
			$('#fileUpload').remove();
		}
		document.getElementById('icon-upload').style.cursor = "pointer";
		document.getElementById("login-button").addEventListener("click", loginModal);
		document.getElementById("register-button").addEventListener("click", registerModal);
	}
}

$(toggleLoginState()); //页面加载时执行

//点击登陆按钮
function loginModal() {
	showModal('login-box');
};

//点击注册按钮
function registerModal() {
	showModal('register-box');
};

//切换注册登陆界面
document.getElementById('turn-to-register').addEventListener('click', function () {
	closeModal('login-box');
	showModal('register-box');
})
document.getElementById('turn-to-login').addEventListener('click', function () {
	closeModal('register-box');
	showModal('login-box');
})


//判断某个input里是否为空的函数
var inputIsNull = function (elementId) {
	return $.trim($('input[id=' + elementId + ']').val()) === "";
}
//登陆-提交表单按钮
$('#login-submit').click(function () {
	console.log('yes!');
	// console.log($('#login-box').serialize());
	console.log($("input[id='l-username']").val() === "");
	//当输入值不等于""时才能进行提交
	if (!inputIsNull('l-username') && !inputIsNull('l-password')) {
		$.ajax({
			url: '/user/login',
			type: "POST",
			// type:"GET",
			data: {
				'userName': $('#l-username').val(),
				'password': $('#l-password').val(),
			},
			// dataType:'',
			//接收返回值还需要变化的,这个TODO应该没问题了~ 剩下注册那块
			success: function (resultData) {
				// alert("请求成功");
				console.log(resultData);
				//TODO:因为用mock，直接那样格式是json，所以这里转换了一下，最后完成后不用转
				// var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
				var dataObj=resultData;
				console.log(dataObj);
				console.log(dataObj.error_code);
				// console.log(dataObj.data[1]);
				switch (dataObj.error_code) {
					case 0:
						console.log(dataObj.data);
                        console.log(dataObj.data.userId);
						userInfo.userId = dataObj.data.userId;
						userInfo.username = dataObj.data.userName;
						closeModal('login-box');
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'success',
							title: "您已经成功登陆",
						});
						//TODO:保存用户信息
						//登陆成功需要页面变化,调用toggleLoginstate()
						toggleLoginState();


                        break;
					case -1:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;
					case -2:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;
					case -3:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;

					default:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: '返回值错误,请重试',
						})
						break;
				}
			},
			error: function () {
				Swal.fire({
					// toast: true,
					// position: 'top',
					showConfirmButton: false,
					timer: 3000,
					type: 'warning',
					title: "请求出错，请稍等",
				})
			},
		});
	} else if (inputIsNull('l-username')) {
		Swal.fire({
			toast: true,
			position: 'top',
			showConfirmButton: false,
			timer: 3000,
			type: 'warning',
			title: "您未完整填写用户名",
		})
	} else if (inputIsNull('l-password')) {
		Swal.fire({
			toast: true,
			position: 'top',
			showConfirmButton: false,
			timer: 3000,
			type: 'warning',
			title: "您未完整填写密码",
		})
	}
	// $("#testAjaxDiv").html(htmlObj.responseText);
})
//注册-提交表单按钮 
$('#register-submit').click(function () {
	console.log('yes!register');
	console.log($("input[id='l-username']").val() === "");
	//当输入值不等于""时才能进行提交
	if (!inputIsNull('r-username') && !inputIsNull('r-password')) {
		$.ajax({
			url: '/user/register',
			type: "POST",
			data: {
				'userName': $('#r-username').val(),
				'password': $('#r-password').val(),
			},
			// dataType:'',
			success: function (resultData) {
				// alert("请求成功");
				console.log(resultData);
				//TODO:因为用mock，直接那样格式是json，所以这里转换了一下，最后完成后不用转
				// var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
				var dataObj=resultData;
				console.log(dataObj);
				console.log(dataObj.error_code);
				// console.log(dataObj.data[1]);
				switch (dataObj.error_code) {
					case 0:
						closeModal('register-box');
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: false,
							timer: 2000,
							type: 'success',
							title: "您已经成功注册，可以去登陆了",
						})

						break;
					case -1:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;
					case -2:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;
					case -3:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: dataObj.error_msg,
						})
						break;

					default:
						Swal.fire({
							// toast: true,
							// position: 'top',
							showConfirmButton: true,
							timer: 2000,
							type: 'warning',
							title: '接口返回值错误,请重试',
						})
						break;
				}
			},
			error: function () {
				Swal.fire({
					// toast: true,
					// position: 'top',
					showConfirmButton: false,
					timer: 3000,
					type: 'warning',
					title: "请求出错，请稍等",
				})
			},
		});
	} else if (inputIsNull('r-username')) {
		Swal.fire({
			toast: true,
			position: 'top',
			showConfirmButton: false,
			timer: 3000,
			type: 'warning',
			title: "您未完整填写用户名",
		})
	} else if (inputIsNull('r-password')) {
		Swal.fire({
			toast: true,
			position: 'top',
			showConfirmButton: false,
			timer: 3000,
			type: 'warning',
			title: "您未完整填写密码",
		})
	}

})