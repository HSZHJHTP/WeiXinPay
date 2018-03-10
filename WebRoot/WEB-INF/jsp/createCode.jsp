<%@page import="entity.Product"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-3.2.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath }/js/qrcode.js"></script>

</head>
<body>
    <div style="text-align: center;" id="code"></div>
    <div style="text-align: center;" id="codeText"></div>
</body>
<script type="text/javascript">
	
	//1 把请求参数发送给CreateCodeServlet
	$.post("${pageContext.request.contextPath }/createCode","pname=${product.pname}&price=${product.price}",
	function(data){  // code_url
		// 成功返回了json格式的数据，那么你需要处理方案
		//1. 获取支付链接地址
		var code_url = data.code_url; 
		//2 创建有一个空白二维码
		var qr = qrcode(10,"H");
		//3 给二维码添加数据
		qr.addData(code_url);
		//4 利用添加的数据生成二维码
		qr.make();
		
		//5 显示二维码
		$("#code").html(qr.createImgTag());
	    $("#codeText").html("请打开微信扫一扫完成支付");
	    
	     //检查用户的支付状态
	     checkPayStatus(data.pname,data.orderId,data.money);
	},"json");
	
	
	//检查用户的支付状态
	function checkPayStatus(pname,orderId,money){
		$.post("${pageContext.request.contextPath }/checkStatus","orderId="+orderId,function(data){
			if(data.trade_state=="SUCCESS"){
				//用户已经支付了
				location.href="${pageContext.request.contextPath }/toSuccess?pname="+pname+"&orderId="+orderId+"&money="+money;
				
			}else{
				//二维码失效
				$("#code").html("<button onclick='javascript:location.reload()'>二维码已经失效，点击刷新</button>");
			}
			
		},"json");
		
	}

	

</script>


</html>