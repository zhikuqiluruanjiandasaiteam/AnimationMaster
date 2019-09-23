/**
 * 点击登陆或注册，弹出窗口进行登陆或者注册
 */
function showModal(string) {  //打开上传框
	console.log(string);
	var modal = document.getElementById(string);
	var overlay = document.getElementsByClassName('modal-overlay')[0];
	overlay.style.display = 'block';
	modal.style.display = 'block';
}
function closeModal(string) {  //关闭上传框
	var modal = document.getElementById(string);
	var overlay = document.getElementsByClassName('modal-overlay')[0];
	overlay.style.display = 'none';
	modal.style.display = 'none';
}

//关闭登陆或注册框
$(".close-box").click(function () {
	closeModal('login-box');
	closeModal('register-box');
	// closeModal('styleContainer')
});
