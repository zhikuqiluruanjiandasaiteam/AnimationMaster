//设置几个全局变量。。。
var fileInformation = {
	name: "",
	type: "",
	styleString: "",
	estimate_time: 0,
	width: 1,
	height: 1,
	duration: 0,
	isFrame: false
}; //TODO:9个属性name，type，estimate_time（预计时间用于显示和传值的，不是接收到的那个）,uploadFile
//以及下面的几个
//视频：宽高，持续时间，是否补帧（isFrame）
//图片:宽高
//音频：持续时间
//fileType：a,v,i //选取的风格变量string

//title美化
var sweetTitles = {
	x: 10,
	y: 20,
	tipElements: "a,span,img,div,option,select",
	noTitle: false,
	init: function () {
		var b = this.noTitle;
		$(this.tipElements).each(function () {
			$(this)
				.mouseover(function (e) {
					if (b) {
						isTitle = true;
					} else {
						isTitle = $.trim(this.title) != "";
					}
					if (isTitle) {
						this.myTitle = this.title;
						this.title = "";
						var a =
							"<div class='tooltip'><div class='tipsy-arrow tipsy-arrow-n'></div><div class='tipsy-inner'>" +
							this.myTitle +
							"</div></div>";
						$("body").append(a);
						$(".tooltip")
							.css({
								top: e.pageY + 20 + "px",
								left: e.pageX - 20 + "px"
							})
							.show("fast");
					}
				})
				.mouseout(function () {
					if (this.myTitle != null) {
						this.title = this.myTitle;
						$(".tooltip").remove();
					}
				})
				.mousemove(function (e) {
					$(".tooltip").css({
						top: e.pageY + 20 + "px",
						left: e.pageX - 20 + "px"
					});
				});
		});
	}
};
sweetTitles.init();

/*点击图标，如果不处于登陆状：去登陆，
 * 处于登陆状态，上传文件
 */
//未登录时候先打开登陆窗口，登陆后应该是先弹出上传（使用input）
$("#uploadImg").click(function () {
	console.log("up up");
	if (!isLogin()) {
		Swal.fire({
			// toast: true,
			// position: 'top',
			showConfirmButton: true, //确定按钮
			allowOutsideClick: false, //点击背景不会关闭
			timer: 3000,
			type: "warning",
			title: "您尚未登陆",
			onClose: function () {
				//弹窗关闭时调用的函数
				showModal("login-box");
			}
		});
	}
	// showModal('styleContainer');
	//获取文件名
});

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

//风格选取对话框的关闭按钮
let oriColor = "#10689a",
	newColor = "#46b3fc";
$("#styleContainerClose").click(function () {
	Swal.fire({
		// toast: true,
		// position: 'top',
		showConfirmButton: true,
		allowOutsideClick: false, //点击背景不会关闭
		type: "warning",
		title: "确认关闭?",
		text: "关闭对话框后需重新选择文件，是否确认？",
		showCancelButton: true,
		confirmButtonColor: "#3085d6",
		cancelButtonColor: "#d33",
		confirmButtonText: "确定关闭！"
	}).then(isConfirm => {
		try {
			//判断 是否 点击的 确定按钮
			if (isConfirm.value) {
				$("#styleForm").css("display", "none");
				$(".is-frame").css("display", "none");
				closeModal("styleContainer");
				$("#filetypeButtonV").css("background-color", oriColor);
				$("#filetypeButtonA").css("background-color", oriColor);
				$("#filetypeButtonI").css("background-color", oriColor);

				//点击了关闭后，清空input里的文件，并初始化fileInformation
				$("#fileUpload").val("");
				console.log($("#fileUpload")[0].files);
				fileInformation = {};
				console.log(fileInformation);
				Swal.fire({
					toast: true,
					position: "top",
					timer: 2000,
					showConfirmButton: true,
					type: "info",
					title: "请重新选择文件"
				});
			} else {
				Swal.fire({
					toast: true,
					position: "top",
					timer: 2000,
					showConfirmButton: true,
					type: "info",
					title: "请您继续选择"
				});
			}
		} catch (e) {
			alert(e);
		}
	});
});

/**
 * 不采用补帧时计算预计时间
 * @param {number} width 宽度
 * @param {number} height 高度
 * @param {number} duration 持续时间
 * @param {number} time_us 以微秒/像素或微秒/s为单位的时间，从后端获取的预估时间
 * @param {boolean} isFrame 是否采用补帧
 * @param {object} frameObj 两个属性的对象
 */
function calculateEsTime(width, height, duration, time_us, isFrame, frameObj) {
	var time; //以秒为单位
	//如果是图像
	if (!isFrame) {
		if (duration === 0) {
			time = (width * height * time_us) / 1000000;
		}
		//否则是音频或视频
		else {
			time = (width * height * duration * time_us) / 1000000;
		}
		// document.getElementById('estimatedTime').innerHTML = "预计时间:" + formatDuraton(time);
	} else {
		time =
			frameObj.frame_patch_rate * duration * 29.9 * frameObj.estimated_time +
			(width * height * duration * (1 - frameObj.frame_patch_rate) * time_us) /
			1000000;
	}
	return time;
}
/**
 * 格式化时间以显示
 * @param {number} time
 */
function formatDuraton(time) {
	time = Math.floor(time);
	if (time > -1) {
		var hour = Math.floor(time / 3600);
		var min = Math.floor(time / 60) % 60;
		var sec = time % 60;
		if (hour < 10) {
			time = "0" + hour + ":";
		} else {
			time = hour + ":";
		}

		if (min < 10) {
			time += "0";
		}
		time += min + ":";

		if (sec < 10) {
			time += "0";
		}
		time += sec;
	}
	return time;
}

/*
 *选取要转换的文件的类型-三个按钮
 */
$("#filetypeButtonV").click(function () {
	fileInformation.styleString = "video";
	console.log("video");
	// $('#styleForm').
	// 通过class来设置隐藏显示
	$("#styleForm").css("display", "block");
	$(".is-frame").css("display", "inline-block");
	$("#filetypeButtonV").css("background-color", newColor);
	$("#filetypeButtonA").css("background-color", oriColor);
	$("#filetypeButtonI").css("background-color", oriColor);

	//发送风格列表请求
	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "video"
		}
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		// var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		var dataObj = resultData;
		console.log(dataObj.data.image);
		//先把之前创建的节点清理空class='select-li'
		$(".style-select").empty();
		//对image数组进行处理
		let imageArray = dataObj.data.image;
		for (let i = 0; i < imageArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = imageArray[i].ims_name;
			optionNode.className = "select-li";
			optionNode.id = "ims_id_" + imageArray[i].ims_id;
			//TODO:
			optionNode.title = imageArray[i].ims_description;
			$("#img_style").append(optionNode);
		}
		//对audio数组处理
		let audioArray = dataObj.data.audio;
		console.log(audioArray);
		for (let i = 0; i < audioArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = audioArray[i].aus_name;
			optionNode.className = "select-li";
			optionNode.id = "aus_id_" + audioArray[i].aus_id;
			$("#audio_style").append(optionNode);
		}
		//对清晰度数组处理
		let clarityArray = dataObj.data.clarity;
		for (let i = 0; i < clarityArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = clarityArray[i];
			optionNode.className = "select-li";
			$("#video_clarity").append(optionNode);
		}

		//TODO:预计时间
		//TODO:与预计时间有关的有三个，图片，音频，是否补帧

		//TODO:补帧率（frame_patch_rate）x帧总数x补帧一张耗时+（1-补帧率）x帧总数x普通转换一张耗时+音频耗时
		fileInformation.estimate_time =
			calculateEsTime(
				1,
				1,
				fileInformation.duration,
				audioArray[0].aus_estimated_time
			) + //音频耗时
			calculateEsTime(
				fileInformation.width,
				fileInformation.height,
				fileInformation.duration,
				imageArray[0].ims_estimated_time,
				true,
				dataObj.data.patch_frame
			); //图像耗时，默认时使用补帧的
		document.getElementById("estimatedTime").innerHTML =
			"预计时间:" + formatDuraton(fileInformation.estimate_time);

		//先移除再绑定事件
		$("select").off("change"); //off解除所有使用on绑定的事件
		$(".is-frame").off("change");

		$(".is-frame").on("change", function () {
			if (fileInformation.isFrame === true) {
				fileInformation.isFrame = false;
				// console.log('false');
			} else {
				fileInformation.isFrame = true;
			}

			//根据上面的结果分类
			let a_option = $("#audio_style option:checked");
			// console.log(a_option);
			let a_optionId = a_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let a_idNumber = parseInt(a_optionId.charAt(a_optionId.length - 1)) - 1;

			let i_option = $('#img_style option:checked');
			let i_optionId = i_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let i_idNumber = parseInt(i_optionId.charAt(i_optionId.length - 1)) - 1;

			if (fileInformation.isFrame === false) {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) +
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time
					);
			} else {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) + //音频耗时
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time,
						true,
						dataObj.data.patch_frame
					);
			}
			document.getElementById("estimatedTime").innerHTML =
				"预计时间:" + formatDuraton(fileInformation.estimate_time);
		});
		$("select#img_style").on("change", function () {
			let a_option = $("#audio_style option:checked");
			console.log(a_option);
			let a_optionId = a_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let a_idNumber = parseInt(a_optionId.charAt(a_optionId.length - 1)) - 1;

			let i_option = $('#img_style option:checked');
			let i_optionId = i_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let i_idNumber = parseInt(i_optionId.charAt(i_optionId.length - 1)) - 1;

			if (fileInformation.isFrame === false) {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) +
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time
					);
			} else {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) + //音频耗时
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time,
						true,
						dataObj.data.patch_frame
					);
			}
			document.getElementById("estimatedTime").innerHTML =
				"预计时间:" + formatDuraton(fileInformation.estimate_time);
		});
		$("select#audio_style").on("change", function () {
			let a_option = $("#audio_style option:checked");
			console.log(a_option);
			let a_optionId = a_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let a_idNumber = parseInt(a_optionId.charAt(a_optionId.length - 1)) - 1;

			let i_option = $('#img_style option:checked');
			let i_optionId = i_option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let i_idNumber = parseInt(i_optionId.charAt(i_optionId.length - 1)) - 1;

			if (fileInformation.isFrame === false) {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) +
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time
					);
			} else {
				fileInformation.estimate_time =
					calculateEsTime(
						1,
						1,
						fileInformation.duration,
						audioArray[a_idNumber].aus_estimated_time
					) + //音频耗时
					calculateEsTime(
						fileInformation.width,
						fileInformation.height,
						fileInformation.duration,
						imageArray[i_idNumber].ims_estimated_time,
						true,
						dataObj.data.patch_frame
					);
			}
			document.getElementById("estimatedTime").innerHTML =
				"预计时间:" + formatDuraton(fileInformation.estimate_time);
		});
	});

	let a_length = $(".audio-style-group").length;
	for (var i = 0; i < a_length; i++) {
		$(".audio-style-group")[i].style.display = "block";
	}

	let v_length = $(".img-style-group").length;
	for (var i = 0; i < v_length; i++) {
		$(".img-style-group")[i].style.display = "block";
	}
});

//按下音频按钮
$("#filetypeButtonA").click(function () {
	fileInformation.styleString = "audio";
	console.log("audio");
	$(".is-frame").css("display", "none");
	$("#filetypeButtonA").css("background-color", newColor);
	$("#filetypeButtonV").css("background-color", oriColor);
	$("#filetypeButtonI").css("background-color", oriColor);
	//通过class来设置隐藏显示
	$("#styleForm").css("display", "block");

	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "audio"
		}
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		console.log(resultData);
		// var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		var dataObj = resultData;
		console.log(dataObj.data.image);
		//先清理
		$(".style-select").empty();
		$(".is-frame").off("change");
		//对audio数组处理
		let audioArray = dataObj.data.audio;
		console.log(audioArray);
		for (let i = 0; i < audioArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = audioArray[i].aus_name;
			optionNode.className = "select-li";
			optionNode.id = "aus_id_" + audioArray[i].aus_id;
			optionNode.title = audioArray[i].aus_description;
			$("#audio_style").append(optionNode);
		}

		//TODO:描述有多长，短的话可以直接加在后面
		$("#audio_style")[0].title = audioArray[0].aus_description;
		sweetTitles.init();

		//TODO:预计时间
		//TODO:在select的地方再加监听,随时更改
		fileInformation.estimate_time = calculateEsTime(
			fileInformation.width,
			fileInformation.height,
			fileInformation.duration,
			dataObj.data.audio[0].aus_estimated_time
		);
		console.log(dataObj.data.audio[0].aus_estimated_time);
		document.getElementById("estimatedTime").innerHTML =
			"预计时间:" + formatDuraton(fileInformation.estimate_time);

		//先移除再绑定事件
		$("select").off("change"); //off解除所有使用on绑定的事件

		//TODO:还需要给其他两个绑定事件
		$("select#audio_style").on("change", function () {
			let option = $("#audio_style option:checked");
			console.log(option);
			let optionId = option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let idNumber = parseInt(optionId.charAt(optionId.length - 1)) - 1;

			$("#audio_style")[0].title = audioArray[idNumber].aus_description;
			console.log(sweetTitles);
			sweetTitles.init();

			fileInformation.estimate_time = calculateEsTime(
				fileInformation.width,
				fileInformation.height,
				fileInformation.duration,
				audioArray[idNumber].aus_estimated_time
			);
			console.log(audioArray[idNumber].aus_estimated_time);
			document.getElementById("estimatedTime").innerHTML =
				"预计时间:" + formatDuraton(fileInformation.estimate_time);
		});
	});

	let a_length = $(".audio-style-group").length;
	for (var i = 0; i < a_length; i++) {
		$(".audio-style-group")[i].style.display = "block";
	}

	let v_length = $(".img-style-group").length;
	for (var i = 0; i < v_length; i++) {
		$(".img-style-group")[i].style.display = "none";
	}
});

//按下图像按钮
$("#filetypeButtonI").click(function () {
	fileInformation.styleString = "image";
	console.log("image");
	$(".is-frame").css("display", "none");
	$("#filetypeButtonI").css("background-color", newColor);
	$("#filetypeButtonA").css("background-color", oriColor);
	$("#filetypeButtonV").css("background-color", oriColor);
	// $('#styleForm').
	// 通过class来设置隐藏显示
	$("#styleForm").css("display", "block");

	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "audio"
		}
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		console.log(resultData);
		// var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		var dataObj = resultData;
		console.log(dataObj.data.image);

		//先清除，再创建
		$(".style-select").empty();
		$(".is-frame").off("change");

		//对image数组进行处理
		var imageArray = dataObj.data.image;
		for (let i = 0; i < imageArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = imageArray[i].ims_name;
			optionNode.className = "select-li";
			optionNode.id = "ims_id_" + imageArray[i].ims_id;
			$("#img_style").append(optionNode);
		}
		//对清晰度数组处理
		let clarityArray = dataObj.data.clarity;
		for (let i = 0; i < clarityArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = clarityArray[i];
			optionNode.className = "select-li";
			$("#video_clarity").append(optionNode);
		}

		//TODO:描述有多长，短的话可以直接加在后面（再看吧）
		// $('#audio_style')[0].title = audioArray[0].aus_description;
		$("#img_style")[0].title = imageArray[0].ims_description;
		sweetTitles.init();

		//TODO:预计时间
		//TODO:在select的地方再加监听,随时更改
		fileInformation.estimate_time = calculateEsTime(
			fileInformation.width,
			fileInformation.height,
			fileInformation.duration,
			imageArray[0].ims_estimated_time
		);
		// console.log(dataObj.data.audio[0].aus_estimated_time);
		document.getElementById("estimatedTime").innerHTML =
			"预计时间:" + formatDuraton(fileInformation.estimate_time);

		//先移除再绑定事件
		$("select").off("change"); //off解除所有使用on绑定的事件

		//TODO:绑定事件
		$("select#img_style").on("change", function () {
			let option = $("#img_style option:checked");
			console.log(option);
			let optionId = option.attr("id");
			//用后端获取的数据拼接的id是从1开始的，所以要-1才能用在数组上
			let idNumber = parseInt(optionId.charAt(optionId.length - 1)) - 1;

			$("#img_style")[0].title = imageArray[idNumber].ims_description;
			console.log(sweetTitles);
			sweetTitles.init();

			fileInformation.estimate_time = calculateEsTime(
				fileInformation.width,
				fileInformation.height,
				fileInformation.duration,
				imageArray[idNumber].ims_estimated_time
			);
			console.log(imageArray[idNumber].ims_estimated_time);
			document.getElementById("estimatedTime").innerHTML =
				"预计时间:" + formatDuraton(fileInformation.estimate_time);
		});
	});

	let a_length = $(".audio-style-group").length;
	for (var i = 0; i < a_length; i++) {
		$(".audio-style-group")[i].style.display = "none";
	}

	let v_length = $(".img-style-group").length;
	for (var i = 0; i < v_length; i++) {
		$(".img-style-group")[i].style.display = "block";
	}
});

//TODO: 创建任务发请求
$("#submitButton").click(function () {
	//图像风格选框
	var imgOptions = $("#img_style option:checked");
	var imgOptionId = imgOptions.attr("id");
	// alert(options.val());
	// alert(options.attr("id"));
	// alert(parseInt(imgOptionId.charAt(imgOptions.attr('id').length-1)));

	//音频风格选框
	var audioOptions = $("#audio_style option:checked");
	var audioOptionId = audioOptions.attr("id");

	//清晰度
	var clarityOptions = $("#video_clarity option:checked");
	// var clarityOptionId = clarityOptions.attr("id");

	//是否补帧加速
	var isFrameOptions = $(".is-frame option:checked");
	var isFrameOptionId = isFrameOptions.attr('id');

	Swal.fire({
		type: 'warning', // 弹框类型
		title: '确认上传并开始转换？', //标题
		// text: "注销后将无法恢复，请谨慎操作！", //显示内容
		confirmButtonColor: '#3085d6', // 确定按钮的 颜色
		confirmButtonText: '确定', // 确定按钮的 文字
		showCancelButton: true, // 是否显示取消按钮
		cancelButtonColor: '#d33', // 取消按钮的 颜色
		cancelButtonText: "取消", // 取消按钮的 文字

		focusCancel: true, // 是否聚焦 取消按钮
		reverseButtons: true // 是否 反转 两个按钮的位置 默认是  左边 确定  右边 取消
	}).then((isConfirm) => {
		try {
			//判断 是否 点击的 确定按钮
			if (isConfirm.value) {
				// Swal.fire("成功", "点击了确定", "success");
				//做个判断，根据styleString传不同的数据
				let dataForm = new FormData();
				switch (fileInformation.styleString) {
					case 'video':
						//TODO:
						console.log(fileInformation.styleString)
						dataForm.set("file", fileInformation.uploadFile);

						dataForm.append('user_id', userInfo.userId); //TODO:
						dataForm.append('type', fileInformation.styleString);
						dataForm.append('estimated_time', fileInformation.estimated_time);
						dataForm.append('ims_id', parseInt(imgOptionId.charAt(imgOptions.attr('id').length - 1)));
						dataForm.append('aus_id', parseInt(audioOptionId.charAt(audioOptions.attr('id').length - 1)));
						//TODO:看他要的是啥？
						dataForm.append('clarity', clarityOptions.text());
						dataForm.append('is_frame_speed', parseInt(isFrameOptionId.charAt(isFrameOptionId.length - 1)));
						// dataForm.append('estimate_time');
						// console.log(dataForm);
						$.ajax({
							type: "POST",
							url: "/task/create",
							data: dataForm,
							processData: false, // 注意：让jQuery不要处理数据
							contentType: false, // 注意：让jQuery不要设置contentType
						}).done(function (resultData) {
							let resultDataObj = resultData;

							let resultErrcode = resultDataObj.error_code;
							switch (resultErrcode) {
								case 0:
									//TODO:成功
									Swal.fire({
										// toast: true,
										// position: 'top',
										showConfirmButton: true,
										timer: 2000,
										type: 'success',
										title: "已成功上传，请耐心等待处理",
									});
									break;
							}
						});
						break;

					case 'audio':
						//
						dataForm.set("file", fileInformation.uploadFile);
						dataForm.append('user_id', userInfo.userId); //TODO:
						dataForm.append('type', fileInformation.styleString);
						dataForm.append('estimated_time', fileInformation.estimated_time);
						// dataForm.append('ims_id', parseInt(imgOptionId.charAt(imgOptions.attr('id').length - 1)));
						dataForm.append('aus_id', parseInt(audioOptionId.charAt(audioOptions.attr('id').length - 1)));
						$.ajax({
							type: "POST",
							url: "/task/create",
							data: dataForm,
							processData: false, // 注意：让jQuery不要处理数据
							contentType: false, // 注意：让jQuery不要设置contentType
						}).done(function (resultData) {
							let resultDataObj = resultData;

							let resultErrcode = resultDataObj.error_code;
							switch (resultErrcode) {
								case 0:
									//成功
									Swal.fire({
										// toast: true,
										// position: 'top',
										showConfirmButton: true,
										timer: 2000,
										type: 'success',
										title: "已成功上传，请耐心等待处理",
									});
									break;
							}
						});
						break;
					case 'image':
						//TODO:
						dataForm.set("file", fileInformation.uploadFile);
						dataForm.append('user_id', userInfo.userId); //TODO:
						dataForm.append('type', fileInformation.styleString);
						// dataForm.append('estimated_time', fileInformation.estimated_time);
						dataForm.append('ims_id', parseInt(imgOptionId.charAt(imgOptions.attr('id').length - 1)));
						// dataForm.append('aus_id', parseInt(audioOptionId.charAt(audioOptions.attr('id').length - 1)));
						dataForm.append('clarity', clarityOptions.text());
						$.ajax({
							type: "POST",
							url: "/task/create",
							data: dataForm,
							processData: false, // 注意：让jQuery不要处理数据
							contentType: false, // 注意：让jQuery不要设置contentType
						}).done(function (resultData) {
							let resultDataObj = resultData;

							let resultErrcode = resultDataObj.error_code;
							switch (resultErrcode) {
								case 0:
									//TODO:成功
									Swal.fire({
										// toast: true,
										// position: 'top',
										showConfirmButton: true,
										timer: 2000,
										type: 'success',
										title: "已成功上传，请耐心等待处理",
									});
									break;
							}
						});
						break;
				}
			} else {
				// Swal.fire("取消", "点击了取消", "error");
			}
		} catch (e) {
			alert(e);
		}
	});
});

//添加拖拽事件
//TODO: 改id做测试
// var dz = document.getElementById('content');
var dz = document.getElementById("dashboard");
dz.ondragover = function (ev) {
	//阻止浏览器默认打开文件的操作
	ev.preventDefault();
	//拖入文件后边框颜色变红
	this.style.borderColor = "#ffdd59";
};

dz.ondragleave = function () {
	//恢复边框颜色
	this.style.borderColor = "rgb(124, 219, 248)";
};
dz.ondrop = function (ev) {
	//恢复边框颜色
	this.style.borderColor = "rgb(124, 219, 248)";
	//阻止浏览器默认打开文件的操作
	ev.preventDefault();
	if (!isLogin()) {
		Swal.fire({
			// toast: true,
			// position: 'top',
			showConfirmButton: true, //确定按钮
			allowOutsideClick: false, //点击背景不会关闭
			timer: 3000,
			type: "warning",
			title: "您尚未登陆",
			onClose: function () {
				//弹窗关闭时调用的函数
				showModal("login-box");
			}
		});
	}
	else {
		let files = ev.dataTransfer.files;
		console.log(files);
		let file = files[0];
		console.log(file);
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
			showModal("styleContainer");
		};

	}

};
