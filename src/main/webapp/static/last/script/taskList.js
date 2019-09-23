//这里是跳转到任务列表界面执行的默认操作

//跳转页面，并且传递参数
//主页代码
//window.location.href="../SystemInfoJsp/add_user.jsp?"+"user_id="+user_id;
//任务列表代码
var user_id = 0;

/*$(function() {
	var loc = location.href;//获取整个跳转地址内容，其实就是你传过来的整个地址字符串
	console.log("我的地址"+loc);
	var n1 = loc.length;//地址的总长
	var n2 = loc.indexOf("?");//取得=号的位置
    user_id = decodeURI(loc.substr(n2+1, n1-n2));//截取从?号后面的内容,也就是参数列表，因为传过来的路径是加了码的，所以要解码
    console.log("账号："+user_id);

	/*var parameters  = parameter.split("&");//从&处拆分，返回字符串数组
	console.log("参数列表"+parameters);
	var paValue = new Array();//创建一个用于保存具体值得数组
	for (var i = 0; i < parameters.length; i++) {
		console.log("参数键值对值"+i+":"+parameters[i]);
		var m1 = parameters[i].length;//获得每个键值对的长度
		var m2 = parameters[i].indexOf("=");//获得每个键值对=号的位置
		var value = parameters[i].substr(m2+1, m1-m2);//获取每个键值对=号后面具体的值
		paValue[i] = value;
		console.log("参数值"+i+":"+value);
	}
    console.log("具体参数数组："+paValue);	
    */
//}); 



function sorting() {
    //TODO:这里判断排序
    var invertedOrder = document.getElementById("invertedOrder");
    var is_disec = 0;
    if (invertedOrder.checked === true) is_disec = 1;

    var all = document.getElementById("all");
    var hideDone = document.getElementById("hideDone");
    var hideUndone = document.getElementById("hideUndone");
    var finish_state = 0; //0 全部；1 仅已完成；-1 仅未完成
    if (hideDone.checked === true) finish_state = -1;
    if (hideUndone.checked === true) finish_state = 1;
    else finish_state = 0;

    $.ajax({
        url: "/task/list",
        dataType: "json",
        type: "POST",
        data: {
            user_id,
            is_disec,
            finish_state
        },
    }).done(function (resultData) {
        //首先清空任务列表div的内容
        $("#middle").html("");
        //TODO:这里是是否隐藏已完成任务的操作
        //用data.调用   这里data传过来是一个对象数组
        console.log(resultData);
        // var dataObj=JSON.parse(resultData);//将收到的data转换成js对象
        var dataObj = resultData;
        console.log(dataObj);
        console.log(dataObj.error_code);
        console.log(dataObj.data.length); //在console中查看数据
        var htmlContent = "";
        for (var i = 0; i < dataObj.data.length; i++) {

            if (i == 0) {
                htmlContent += "<div class=\"task-list-outside-top\">";
            } else {
                htmlContent += "<div class=\"task-list-outside-normal\">";
            }

            //处理传来的系统时间
            var createTime = dataObj.data[i].create_time.split("-");
            var date = createTime[2].substring(0, 2);
            var time = createTime[0] + "年" + createTime[1] + "月" + date + "日";



            htmlContent += "<p>&nbsp;&nbsp;" + time + "</p>";
            htmlContent += "<div class=\"task-list\">";
            htmlContent += "<table class=\"task-list-table\" cellspacing=\"8\">";

            //TODO:任务名称无
            htmlContent += "<tr><td class=\"first-row\" colspan=\"5\"title=\""+dataObj.data[i].task_id+"\">No." + dataObj.data[i].task_id + "</td></tr>";

            htmlContent += "<tr><th>任务风格：</th>";

            //判断任务类型video,image,audio，选择类型，风格
            if (dataObj.data[i].task_type == "video") htmlContent += "<td class=\"first-column\">视频</td><td>" + dataObj.data[i].ims_id + "</td></tr>";
            else if (dataObj.data[i].task_type == "image") htmlContent += "<td class=\"first-column\">图片</td><td>" + dataObj.data[i].ims_id + "</td>";
            else
                htmlContent += "<td class=\"first-column\">音频</td><td>" + dataObj.data[i].aus_id + "</td></tr>";

            //补帧加速
            htmlContent += "<tr><th>补帧加速：</th><td class=\"first-column\">" + (dataObj.data[i].is_frame_speed ? "是" : "否") + "</td></tr>";

            //清晰度
            htmlContent += "<tr><th>清晰度：</th><td class=\"first-column\">" + dataObj.data[i].clarity + "</td></tr>";

            //任务进度
            if (dataObj.data[i].start_time == null)
                htmlContent += "<tr><th>任务进度:</th><td class=\"first-column\">未完成</td><td font-weight=\"bold\">(预计时间:" + dataObj.data[i].estimate_time + ")</td></tr>";
            else
                htmlContent += "<tr><th>任务进度:</th><td class=\"first-column\">已完成</td></tr>";

            htmlContent += "</table><div class=\"download-div\"><button class=\"download\" id=\""+dataObj.data[i].file_id+"\" onclick=\"selectFile(this)\">下载</button></div>";
            htmlContent += "</div></div>";
            console.log("分开数组成功");

        }
        // let x=document.getElementById("middle");
        // x.innerHTML=htmlContent;
        $("#middle").append(htmlContent);
    }).fail(function () {
        Swal.fire({
            // toast: true,
            // position: 'top',
            showConfirmButton: false,
            timer: 3000,
            type: 'warning',
            title: "请求出错，请稍等",
        })
    });
}

//刚刚转进界面  js是顺序执行的
$(document).ready(sorting());



function selectFile(obj){
    // var inputObj=document.createElement('input')
    //      inputObj.setAttribute('id','_ef');
    //      inputObj.setAttribute('type','file');
    //      inputObj.setAttribute("style",'visibility:hidden');
    //      document.body.appendChild(inputObj);
    //      inputObj.click();
    //      inputObj.value ;
    var fileId=obj.id;
    $.ajax({
        url:"/file/downloadfile",
        type:"POST",
        data:{
            fileId
        },
    }).done(
        alert('下载中')
    )
}