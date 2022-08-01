/*
* 车间调度中的订单类
 */
package easyopt.model;

import java.util.ArrayList;
import java.util.List;

/** 车间调度中的订单类，包含了订单的全部信息
 * @see Process
 */
public class Order implements java.lang.Cloneable{
	
@Override
public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

/**当前订单在全部订单中的编号，从0开始
 * */
 public int orderId;
 
 /**当前订单的交付时间
  * */
 public double dueTime;  
 
 /**当前订单的释放时间
  * */
 public double releaseTime;

 /**当前订单的加工时间，当订单仅有一道工序是起作用，例如并行机调度，对于多道工序的订单，将使用过程列表进行表示
  * */
 public double procTime;
 
 /**提前完工单位时长惩罚成本
  * */
 public double alpha;
 
 /**拖后完工单位时长惩罚成本
  * */
 public double belta; 
 
 /**在调度计算中记录当前订单下一个待调度工序的编号，起始从0开始，即第一道工序开始排产
  * */
 public int nextProcId=0;
 
 /**调度计算过程中记录当前订单上一工序的完工时间，即下一工序开工时间不能早于该时间
  * */
 public double preEndTime=0;

 /**当前订单的工艺和工时对象列表
  * */
 public List<Process> processList = new ArrayList<>();//  

 //--------------------------以下为相关方法------------------------
 
 /**获取单一工序订单的作业工时：主要用于并行机调度
  * @return 非负实数
  * */
  public double getProcTime() {
    return procTime;
  }

  /**设置单一工序订单的作业工时：主要用于并行机调度
   * @param procTime 非负实数
   * */
  public void setProcTime(double procTime) {
    this.procTime = procTime;
  }

  /**设置该订单下一排产工序编号，从0开始编号
   * @param nextProcId 非负整数
   * */  
  public void setNextProcId(int nextProcId) {
    this.nextProcId = nextProcId;
  }

  /**设置该订单前一工序排产结束的时间，也即后一工序最早可开工时间
   * @param preEndTime 非负实数
   * */  
  public void setPreEndTime(double preEndTime) {
    this.preEndTime = preEndTime;
  }

  /**获取该订单下一排产工序编号，从0开始编号
   * @return 非负整数
   * */
  public int getNextProcId() {
    return nextProcId;
  }

  /**获取该订单前一工序排产结束的时间，也即后一工序最早可开工时间
  * @return 非负实数 
   * */
  public double getPreEndTime() {
    return preEndTime;
  }
 
/**获取该订单的释放时间
  * @return 非负实数 
 */
  public double getReleaseTime() {
    return releaseTime;
  }

  /**设置该订单的交付时间
   * @param dueTime 非负实数
   * */
  public void setDueTime(double dueTime) {
    this.dueTime = dueTime;
  }

  /**设置该订单的释放时间
   * @param releaseTime 非负实数
   * */
  public void setReleaseTime(double releaseTime) {
    this.releaseTime = releaseTime;
  }

  /**设置该订单的工艺对象列表
   * @param processList Process类的对象列表
   * */
  public void setProcessList(List<Process> processList) {
    this.processList = processList;
  }
  
/**获取该订单各道工序的工艺对象列表
   *@return Process类的对象列表
   */    
  public List<Process> getProcessList() {
    return processList;
  }
  
/**获取该订单在全部订单中的编号，从0开始编号
   *@return 非负整数值
   */ 
  public int getOrderId() {
    return orderId;
  }
  
  /**设置该订单在全部订单中的编号，从0开始编号
   * @param orderId 非负整数
   */ 
  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }  
  
/**获取该订单的交付时间
 @return 非负实数值
 */    
  public double getDueTime(){
    return dueTime;
  }

public double getAlpha() {
	return alpha;
}

public void setAlpha(double alpha) {
	this.alpha = alpha;
}

public double getBelta() {
	return belta;
}

public void setBelta(double belta) {
	this.belta = belta;
}  
  
  /**输出该订单信息的字符串形式
  */    
//   @Override
//  public String toString() {
//    return "Order{" + "orderId=" + orderId + ", dueTime=" + dueTime + ", releaseTime=" + releaseTime + ", procTime=" + procTime + ", nextProcId=" + nextProcId + ", preEndTime=" + preEndTime + ", processList=" + processList + '}';
//  } 
}
    
 
