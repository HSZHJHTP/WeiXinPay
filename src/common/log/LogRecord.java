package common.log;

import java.util.Date;



import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;


/**
 * 业务层创建日志切面(环绕通知)
 */
public class LogRecord{

	public Object invoke(ProceedingJoinPoint point) throws Throwable{
		LogService log = new LogService();
		try {
			String methodName = point.getSignature().toString();
			log.setMethod(getSomeString(methodName.replace("WeiXinPay.", " "), 200));
			Object[] args = point.getArgs();
			log.setParams(getSomeString(org.springframework.util.StringUtils.arrayToCommaDelimitedString(args),200));
			Object ret = point.proceed();
			log.setType("success");
			if(ret != null){
				log.setReturm(getSomeString(ret.toString(),400));
			}
			return ret;
		} catch (Exception e){
			log.setType("failure") ;
			log.setException(getSomeString(e.getMessage(),100));
			return point.proceed();
		} catch (Throwable e) {
			log.setType("failure") ;
			log.setException(getSomeString(e.getMessage(),100));
			return point.proceed();
		}
	}
	
	private String getSomeString(String str ,int length ){
		return StringUtils.substring(str, 0, length-1);
	}

}
class LogService{
	private Long id;
	private String userId;
	private String method;
	private String params;
	private String returm;
	private String exception; 
	private String type;
	private Date time;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getReturm() {
		return returm;
	}
	public void setReturm(String returm) {
		this.returm = returm;
	}
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
} 
