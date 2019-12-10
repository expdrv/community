$(function(){
	$("#publishBtn").click(publish);
});

function publish() {

	$("#publishModal").modal("hide");

	// 发送异步请求之前获取内容
	var title= $("#recipient-name").val();
	var content= $("#message-text").val();
	
	//发送异步请求
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data=$.parseJSON(data);
			//在提示框只显示消息
			$("#hintBody").text(data.msg);
			//在返回消息后显示消息两秒，然后自动隐藏
			$("#hintModal").modal("show");

			setTimeout(function(){
				$("#hintModal").modal("hide");

				if(data.code==0)
				{
					window.location.reload();
				}

			}, 2000);
		}
	)
	

}