<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
		<table border="1px" align="center" width="30%" cellspacing="0px">
			<tr>
				<td>订单号</td>
				<td>${product.orderId}</td>
			</tr>
			
			<tr>
				<td>商品名称</td>
				<td>${product.pname}</td>
			</tr>
		
			<tr>
				<td>金额</td>
				<td>${product.money}</td>
			</tr>
			
			<tr>
				<td>支付状态</td>
				<td>支付成功</td>
			</tr>
		
		</table>
</body>
</html>