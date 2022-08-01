package easyopt.model;

import java.util.ArrayList;
import java.util.List;

public class Machine  {
	int machId;
	List<Order> orderList=new ArrayList<>();
	public int getMachId() {
		return machId;
	}
	@Override
	public String toString() {
		return "Machine [machId=" + machId + ", orderList=" + orderList.toString()+ "]";
	}
	public void setMachId(int machId) {
		this.machId = machId;
	}
	public List<Order> getOrderList() {
		return orderList;
	}
	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}
}
