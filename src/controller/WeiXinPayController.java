package controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.wxpay.sdk.WXPayUtil;
import com.google.gson.Gson;

import util.HttpClient;
import util.PayConfig;

import entity.Product;

/**
 * @author hansz
 * @version 2018-3-10 下午3:50:11
 * @Description TODO
 */
@Controller
public class WeiXinPayController {
	protected Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	/**
	 * @author hansz
	 * @version 2018-3-10 下午6:41:15
	 * @Description TODO 去往商品页面
	 */
	@RequestMapping(value = "/toProductList", method = { RequestMethod.POST, RequestMethod.GET })
	public String toProductList() {
		return "product_list";
	}

	/**
	 * @author hansz
	 * @version 2018-3-10 下午6:41:35
	 * @Description TODO 去往生成收款二维码的页面
	 */
	@RequestMapping(value = "/toCreateCode", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView toCreateCode(Product product) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		modelAndView.setViewName("createCode");
		return modelAndView;
	}

	/**
	 * @author hansz
	 * @version 2018-3-10 下午6:41:59
	 * @Description TODO 从微信那获取生成的二维码的信息
	 */
	@RequestMapping(value = "/createCode", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createCode(Product product) {
		String json = "";
		Date date = new Date();
		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sdFormatter.format(date);
		BigDecimal bigDecimal = new BigDecimal(product.getPrice());
		String price = String.valueOf((bigDecimal.multiply(new BigDecimal(100)).intValue()));
		try {
			// 在线微信支付开发文档:https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_1
			// 1 编写商户的信息
			Map<String, String> param = new HashMap<String, String>();
			param.put("appid", PayConfig.appid);// 公众号id
			param.put("mch_id", PayConfig.partner); // 商户的账号
			param.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
			param.put("body", product.getPname()); // 商品描述
			String orderId = time + (WXPayUtil.generateNonceStr()).substring(2, 12);
			param.put("out_trade_no", orderId); // 订单号
			param.put("total_fee", price); // 支付的金额 , 单位是分
			param.put("spbill_create_ip", "127.0.0.1"); // 购物终端的ip地址
			param.put("notify_url", PayConfig.notifyurl); // 通知地址
			param.put("trade_type", "NATIVE"); // 交易类型

			// 2 生成数字签名， 同时把上面的参数都编写成xml格式的数据
			String xml = WXPayUtil.generateSignedXml(param, PayConfig.partnerkey);
			logger.info("发送信息：" + System.getProperty("line.separator") + xml);

			// 3 把这些信息发送给微信支付后台
			String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			HttpClient client = new HttpClient(url);
			client.setHttps(true);// 开启https协议
			client.setXmlParam(xml);// 设置xml格式的数据

			client.post();// 发送请求

			// 4 获取微信支付后台返回的数据
			String content = client.getContent();
			logger.info("返回信息：" + System.getProperty("line.separator") + content);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);// 把返回的数据转换成map集合
			resultMap.put("orderId", orderId);
			resultMap.put("money", product.getPrice());
			resultMap.put("pname", product.getPname());

			// 5 把map集合转换成json格式的数据，然后写出
			Gson gson = new Gson();
			json = gson.toJson(resultMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * @author hansz
	 * @version 2018-3-10 下午6:42:46
	 * @Description TODO 检查支付状态
	 */
	@RequestMapping(value = "/checkStatus", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String checkStatus(Product product) {
		String json = "";
		try {
			// 1 填写商户信息
			Map<String, String> param = new HashMap<String, String>();
			param.put("appid", PayConfig.appid); // 公众号的唯一标识
			param.put("mch_id", PayConfig.partner); // 商户的账号
			param.put("out_trade_no", product.getOrderId()); // 订单号
			param.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串

			// 2 数字签名与把上面参数写成xml格式
			String xml = WXPayUtil.generateSignedXml(param, PayConfig.partnerkey);

			// 3 把信息发送给微信支付后台
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);
			client.setXmlParam(xml);

			long startTime = System.currentTimeMillis(); // 定义一个变量记录开始发送请求的时间

			while (true) {
				// 发送请求
				client.post();

				// 4获取微信支付的返回值
				String content = client.getContent();
				// 把xml转换map对象
				Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

				// 把map转换成json向页面输出
				Gson gson = new Gson();
			    json = gson.toJson(resultMap);

				// 情况一：用户已经支付,trade_state=success
				if ("success".equalsIgnoreCase(resultMap.get("trade_state"))) {
					break;
				}

				// 情况2： 超过30秒用户还是没有支付,trade_state=NOTPAY
				if (System.currentTimeMillis() - startTime > 30000) {
					break;
				}

				Thread.sleep(3000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@RequestMapping(value = "/toSuccess", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView toSuccess(Product product) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		modelAndView.setViewName("success");
		return modelAndView;
	}
}
