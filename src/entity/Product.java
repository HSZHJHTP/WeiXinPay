package entity;

/**
 * @author hansz
 * @version 2018-3-10 下午4:06:10
 * @Description TODO
 */
public class Product {
	private String pname;
	private String price;
	private String orderId;
	private String money;

	public Product() {
		super();
	}

	public Product(String pname, String price, String orderId, String money) {
		super();
		this.pname = pname;
		this.price = price;
		this.orderId = orderId;
		this.money = money;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

}
