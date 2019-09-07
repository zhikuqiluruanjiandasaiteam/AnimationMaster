//设置几个全局变量。。。
//选取的风格变量string
var styleString;
var fileInformation = {}; //6个属性name，type，estimate_time（预计时间）以及下面三个
//视频：宽高，持续时间
//图片:宽高
//音频：持续时间

/*点击图标，如果不处于登陆状：去登陆，
 * 处于登陆状态，上传文件
 */
//TODO:记得修改这里的逻辑,未登录时候先打开登陆窗口，登陆后应该是先弹出上传
$('#uploadImg').click(function () {
	console.log("up up");
	if (!isLogin()) {
		showModal('login-box');
	}
	// showModal('styleContainer');
	//获取文件名
});

//为input绑定事件
//TODO:
$("#fileUpload").change(function () {
	alert('Changed!');
	console.log($("#fileUpload")[0].files);
	let file = $("#fileUpload")[0].files[0];
	//如果不合格式，重新选文件
	if (file.type !== "image/png" && file.type !== "image/jpeg" && file.type !== "audio/mp3" && file.type !== "audio/mp4" && file.type !== "video/mp4") {
		Swal.fire({
			// toast: true,
			// position: 'top',
			showConfirmButton: true,
			type: 'warning',
			title: "不支持该格式",
			text: "仅支持mp4，jpg，jpeg，png，mp3",
		});
	}
	//符合要求的格式
	else {
		fileInformation.name = file.name;
		fileInformation.type = file.type;
		console.log(fileInformation);
		//"image/png,image/jpeg,audio/mpeg,audio/mp4, video/mp4"
		//传的是图片
		if (file.type === 'image/png' || file.type === 'image/jpeg') {
			let reader = new FileReader();
			reader.readAsDataURL(file); //这里把一个文件用base64编码
			reader.onload = function (e) {
				let img = new Image();
				img.src = e.target.result; //获取编码后的值,也可以用this.result获取
				img.onload = function () {
					console.log('height:' + this.height + '----width:' + this.width);
					document.getElementById('uploadFilename').innerHTML = fileInformation.name;
					fileInformation.height = this.height;
					fileInformation.width = this.width;
					fileInformation.duration = 0;
				}
			}
		}
		//传的是视频
		else if (file.type === 'video/mp4') {
			//等待视频数据加载弹框
			Swal.fire({
				// toast: true,
				// position: 'top',
				showConfirmButton: false,
				type: 'info',
				title: "文件正在处理中...",
			});

			let reader = new FileReader();
			reader.readAsDataURL(file); //这里把一个文件用base64编码
			reader.onload = function (e) {
				var dataUrl = reader.result;
				var videoId = "videoMain";
				var $videoEl = $('<video id="' + videoId + '"></video>');
				// $("body").append($videoEl);
				$videoEl.attr('src', dataUrl);
				var videoTagRef = $videoEl[0];

				//媒体（音频、视频）相关事件loadstart、loadmetadata
				//参考https://developer.mozilla.org/zh-CN/docs/Web/Guide/Events/Media_events

				videoTagRef.addEventListener('loadedmetadata', function (e) {
					console.log(videoTagRef.videoWidth, videoTagRef.videoHeight, videoTagRef.duration);
					document.getElementById('uploadFilename').innerHTML = fileInformation.name;
					fileInformation.height = videoTagRef.videoHeight;
					fileInformation.width = videoTagRef.videoWidth;
					fileInformation.duration = videoTagRef.duration;
					swal.close(); //元数据加载完成后自动关闭对话框
				});
			}
		}
		//传的是音频
		else if (file.type === 'audio/mp3') {
			console.log('mp3加载中');
			//等待视频数据加载弹框
			Swal.fire({
				// toast: true,
				// position: 'top',
				showConfirmButton: false,
				type: 'info',
				title: "文件正在处理中...",
			});

			let reader = new FileReader();
			reader.readAsDataURL(file); //这里把一个文件用base64编码
			reader.onload = function (e) {
				let dataUrl = reader.result;
				let audioId = "videoMain";
				let $audioEl = $('<audio id="' + audioId + '"></audio>');
				// $("body").append($videoEl);
				$audioEl.attr('src', dataUrl);
				let audioTagRef = $audioEl[0];

				//媒体（音频、视频）相关事件loadstart、loadmetadata
				audioTagRef.addEventListener('loadedmetadata', function (e) {
					console.log(audioTagRef.duration);
					document.getElementById('uploadFilename').innerHTML = fileInformation.name;
					fileInformation.height =0;
					fileInformation.width = 0;
					fileInformation.duration =audioTagRef.duration;

					swal.close(); //元数据加载完成后自动关闭对话框
				});
			}
		}
		showModal('styleContainer');
		
	}
	//"image/png,image/jpeg,audio/mpeg,audio/mp4, video/mp4"
	// showModal('styleContainer');
	//document.getElementById('uploadFilename').innerHTML = fileInformation.name;
	//showModal('styleContainer');

});



//风格选取对话框的关闭按钮
let oriColor = '#10689a',
	newColor = '#46b3fc';
$('#styleContainerClose').click(function () {
	$('#styleForm').css('display', 'none');
	$('.is-frame').css('display', 'none');
	closeModal('styleContainer');
	$('#filetypeButtonV').css('background-color', oriColor);
	$('#filetypeButtonA').css('background-color', oriColor);
	$('#filetypeButtonI').css('background-color', oriColor);
})

/*
 *选取要转换的文件的类型-三个按钮
 */
$('#filetypeButtonV').click(function () {
	styleString = 'video';
	console.log('video');
	// $('#styleForm').
	// 通过class来设置隐藏显示
	$('#styleForm').css('display', 'block');
	$('.is-frame').css('display', 'inline-block');
	$('#filetypeButtonV').css('background-color', newColor);
	$('#filetypeButtonA').css('background-color', oriColor);
	$('#filetypeButtonI').css('background-color', oriColor);

	//发送风格列表请求
	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "video"
		},
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		// var dataObj=resultData;
		console.log(dataObj.data.image);


		//先把之前创建的节点清理空class='select-li'
		$('.style-select').empty();
		//对image数组进行处理
		let imageArray = dataObj.data.image;
		for (let i = 0; i < imageArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = imageArray[i].ims_name;
			optionNode.className = 'select-li';
			optionNode.id = "ims_id_" + imageArray[i].ims_id;
			$('#img_style').append(optionNode);
		};
		//对audio数组处理
		let audioArray = dataObj.data.audio;
		console.log(audioArray);
		for (let i = 0; i < audioArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = audioArray[i].aus_name;
			optionNode.className = 'select-li';
			optionNode.id = "aus_id_" + audioArray[i].aus_id;
			$('#audio_style').append(optionNode);
		};
		//对清晰度数组处理
		let clarityArray = dataObj.data.clarity;
		for (let i = 0; i < clarityArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = clarityArray[i];
			optionNode.className = 'select-li';
			$('#video_clarity').append(optionNode);
		}

	});

	let a_length = $('.audio-style-group').length;
	for (var i = 0; i < a_length; i++) {
		$('.audio-style-group')[i].style.display = 'block';
	}

	let v_length = $('.img-style-group').length;
	for (var i = 0; i < v_length; i++) {
		$('.img-style-group')[i].style.display = 'block';
	}
});

//按下音频按钮
$('#filetypeButtonA').click(function () {
	styleString = 'audio';
	console.log('audio');
	$('.is-frame').css('display', 'none');
	$('#filetypeButtonA').css('background-color', newColor);
	$('#filetypeButtonV').css('background-color', oriColor);
	$('#filetypeButtonI').css('background-color', oriColor);
	//通过class来设置隐藏显示
	$('#styleForm').css('display', 'block');

	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "audio"
		},
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		console.log(resultData);
		var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		// var dataObj=resultData;
		console.log(dataObj.data.image);
		//先清理
		$('.style-select').empty();

		//对audio数组处理
		let audioArray = dataObj.data.audio;
		console.log(audioArray);
		for (let i = 0; i < audioArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = audioArray[i].aus_name;
			optionNode.className = 'select-li';
			optionNode.id = "aus_id_" + audioArray[i].aus_id;
			$('#audio_style').append(optionNode);
		};
	});


	let a_length = $('.audio-style-group').length;
	for (var i = 0; i < a_length; i++) {
		$('.audio-style-group')[i].style.display = 'block';
	}

	let v_length = $('.img-style-group').length;
	for (var i = 0; i < v_length; i++) {
		$('.img-style-group')[i].style.display = 'none';
	}

});

//按下图像按钮
$('#filetypeButtonI').click(function () {
	styleString = 'image';
	console.log('image');
	$('.is-frame').css('display', 'none');
	$('#filetypeButtonI').css('background-color', newColor);
	$('#filetypeButtonA').css('background-color', oriColor);
	$('#filetypeButtonV').css('background-color', oriColor);
	// $('#styleForm').
	// 通过class来设置隐藏显示
	$('#styleForm').css('display', 'block');

	let request = $.ajax({
		type: "POST",
		url: "/style/list",
		data: {
			type: "audio"
		},
	});
	request.done(function (resultData) {
		// 返回的有三个参数，其中data又有四个参数
		console.log(resultData);
		var dataObj = JSON.parse(resultData); //将收到的数据转换成js对象
		// var dataObj=resultData;
		console.log(dataObj.data.image);

		//先清除，再创建
		$('.style-select').empty();
		//对image数组进行处理
		var imageArray = dataObj.data.image;
		for (let i = 0; i < imageArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = imageArray[i].ims_name;
			optionNode.className = 'select-li';
			optionNode.id = "ims_id_" + imageArray[i].ims_id;
			$('#img_style').append(optionNode);
		}
		//对清晰度数组处理
		let clarityArray = dataObj.data.clarity;
		for (let i = 0; i < clarityArray.length; i++) {
			let optionNode = document.createElement("option");
			optionNode.innerHTML = clarityArray[i];
			optionNode.className = 'select-li';
			$('#video_clarity').append(optionNode);
		}
	});

	let a_length = $('.audio-style-group').length;
	for (var i = 0; i < a_length; i++) {
		$('.audio-style-group')[i].style.display = 'none';
	}

	let v_length = $('.img-style-group').length;
	for (var i = 0; i < v_length; i++) {
		$('.img-style-group')[i].style.display = 'block';
	}
});

//TODO: 创建任务发请求
$('#submitButton').click(function () {
	//图像风格选框
	var imgOptions = $("#img_style option:checked");
	var imgOptionId = imgOptions.attr('id');
	// alert(options.val());
	// alert(options.attr("id"));
	// alert(parseInt(imgOptionId.charAt(imgOptions.attr('id').length-1)));

	//音频风格选框
	var audioOptions = $("#audio_style option:checked");
	var audioOptionId = audioOptions.attr('id');

	//清晰度
	var clarityOptions = $("#video_clarity option:checked");
	var clarityOptionId = clarityOptions.attr('id');

	//是否补帧加速
	var isFrameOptions = $(".is-frame option:checked");
	// var audioOptionId=isFrameOptions.attr('id');

	// Swal.fire({
	// 	type: 'warning', // 弹框类型
	// 	title: '确认上传并开始转换？', //标题
	// 	// text: "注销后将无法恢复，请谨慎操作！", //显示内容           
	// 	confirmButtonColor: '#3085d6',// 确定按钮的 颜色
	// 	confirmButtonText: '确定',// 确定按钮的 文字
	// 	showCancelButton: true, // 是否显示取消按钮
	// 	cancelButtonColor: '#d33', // 取消按钮的 颜色
	// 	cancelButtonText: "取消", // 取消按钮的 文字

	// 	focusCancel: true, // 是否聚焦 取消按钮
	// 	reverseButtons: true  // 是否 反转 两个按钮的位置 默认是  左边 确定  右边 取消
	// }).then((isConfirm) => {
	// 	try {
	// 		//判断 是否 点击的 确定按钮
	// 		if (isConfirm.value) {
	// 			// Swal.fire("成功", "点击了确定", "success");
	// 			//做个判断，根据styleString传不同的数据
	// 			switch (styleString) {
	// 				case 'video':
	// 					//TODO:
	// 					$.ajax({
	// 						type: "POST",
	// 						url: "/task/create",
	// 						data: {
	// 							file: "example.mp4", //TODO:文件（不是文件名）
	// 							usr_id: "",			 //TODO:用户名
	// 							type: styleString,   //类型
	// 							estimate_time: '',   //TODO:预估时间
	// 							ims_id: '',			 //图片风格
	// 							aus_id:'',			 //音频风格
	// 							clarity:'',			 //清晰度	
	// 							is_frame_speed:'',	 //是否采用补帧加速
	// 						},

	// 					});
	// 					break;

	// 				case 'audio':
	// 					//TODO:
	// 					break;
	// 				case 'image':
	// 					//TODO:
	// 					break;
	// 			}


	// 			/*
	// 			$.ajax({
	// 				type: "POST",
	// 				url: "/task/create",
	// 				data: {
	// 					file:"example.mp4",
	// 					usr_id:"",
	// 					type:styleString,
	// 					estimate_time: '',
	// 					ims_id:'',
	// 				},
	// 				dataType: "dataType",
	// 				success: function (response) {

	// 				}
	// 			});
	// 			*/
	// 		}
	// 		else {
	// 			// Swal.fire("取消", "点击了取消", "error");
	// 		}
	// 	} catch (e) {
	// 		alert(e);
	// 	}
	// });
});





//利用html5 FormData() API,创建一个接收文件的对象，因为可以多次拖拽，这里采用单例模式创建对象Dragfiles
var Dragfiles = (function () {
	var instance;
	return function () {
		if (!instance) {
			instance = new FormData();
		}
		return instance;
	}
}());
//为Dragfiles添加一个清空所有文件的方法
FormData.prototype.deleteAll = function () {
	var _this = this;
	this.forEach(function (value, key) {
		_this.delete(key);
	})
}
//添加拖拽事件
//TODO: 改id做测试
// var dz = document.getElementById('content');
var dz = document.getElementById('dashboard');
dz.ondragover = function (ev) {
	//阻止浏览器默认打开文件的操作
	ev.preventDefault();
	//拖入文件后边框颜色变红
	this.style.borderColor = '#ffdd59';
}

dz.ondragleave = function () {
	//恢复边框颜色
	this.style.borderColor = 'rgb(124, 219, 248)';
}
dz.ondrop = function (ev) {
	//恢复边框颜色
	this.style.borderColor = 'rgb(124, 219, 248)';
	//阻止浏览器默认打开文件的操作
	ev.preventDefault();
	var files = ev.dataTransfer.files;
	var len = files.length,
		i = 0;
	var frag = document.createDocumentFragment(); //为了减少js修改dom树的频度，先创建一个fragment，然后在fragment里操作
	var tr, time, size;
	var newForm = Dragfiles(); //获取单例
	var it = newForm.entries(); //创建一个迭代器，测试用
	while (i < len) {
		tr = document.createElement('tr');
		//获取文件大小
		size = Math.round(files[i].size * 100 / 1024) / 100 + 'KB';
		//获取格式化的修改时间
		time = files[i].lastModifiedDate.toLocaleDateString() + ' ' + files[i].lastModifiedDate.toTimeString().split(' ')[0];
		tr.innerHTML = '<td>' + files[i].name + '</td><td>' + time + '</td><td>' + size + '</td><td>删除</td>';
		console.log(size + ' ' + time);
		frag.appendChild(tr);
		//添加文件到newForm
		newForm.append(files[i].name, files[i]);
		//console.log(it.next());
		i++;
	}
	this.chigldNodes[1].childNodes[1].appendChild(fra);
	//为什么是‘1’？文档里几乎每一样东西都是一个节点，甚至连空格和换行符都会被解释成节点。而且都包含在childNodes属性所返回的数组中.不同于jade模板
}

function blink() {
	document.getElementById('content').style.borderColor = 'rgb(124, 219, 248)';
}

//ajax上传文件
function upload() {
	if (document.getElementsByTagName('tbody')[0].hasChildNodes() == false) {
		document.getElementById('content').style.borderColor = 'red';
		setTimeout(blink, 200);
		return false;
	}
	var data = Dragfiles(); //获取formData
	$.ajax({
		url: 'upload',
		type: 'POST',
		data: data,
		async: true,
		cache: false,
		contentType: false,
		processData: false,
		success: function (data) {
			alert('succeed!') //可以替换为自己的方法
			closeModal();
			data.deleteAll(); //清空formData
			$('.tbody').empty(); //清空列表
		},
		error: function (returndata) {
			alert('failed!') //可以替换为自己的方法
		}
	});
}
// 用事件委托的方法为‘删除’添加点击事件，使用jquery中的on方法
$(".tbody").on('click', 'tr td:last-child', function () {
	//删除拖拽框已有的文件
	var temp = Dragfiles();
	var key = $(this).prev().prev().prev().text();
	console.log(key);
	temp.delete(key);
	$(this).parent().remove();
});
//清空所有内容
function clearAll() {
	if (document.getElementsByTagName('tbody')[0].hasChildNodes() == false) {
		document.getElementById('content').style.borderColor = 'red';
		setTimeout(blink, 300);
		return false;
	}
	var data = Dragfiles();
	data.deleteAll(); //清空formData
	//$('.tbody').empty(); 等同于以下方法
	document.getElementsByTagName('tbody')[0].innerHTML = '';
}