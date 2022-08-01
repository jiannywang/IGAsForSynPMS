/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.model;

import java.util.Arrays;

/**订单的加工工序类，一般会关联到特定订单
 * @see Order
 */
public class Process {
	/**该工序的编码，从0开始编码
	 * */
    public int processId;
    
    /**本道工序可用设备数量
     * */
    public int machQty;//
    
    /**本道工序可用设备编码，是设备在全部设备中的顺序号,从0开始编码
     * */    
    public int[] machIndex;//
    
    /**本订单该工序可拆分子订单数量，即可以分为几个子订单
     * */    
    public int splitQty;
    
    /**该订单在本道工序的加工总时间
     * */    
    public double cycleTime; //    
    
/**获取该工序的编号，从0开始编码
 * @return 非负整数
 */
  public int getProcessId() {
    return this.processId;
  } 
 /**获取该工序可使用机器的数量
 * @return 正整数值
 */ 
  public int getMachQty() {
    return this.machQty;
  } 
/**获取该工序可以分批的数量
 * @return 正整数
 */  
  public int getSplitQty() {
    return this.splitQty;
  }
  
/**获取该工序的加工时间
 * @return 非负实数
 */  
  public double getCycleTime() {
    return this.cycleTime;
  }
  
/**获取该工序可以使用的多台机器在全部机器中的编号数组，从0开始编码
 * @return 一维非负整数数组
 */  
  public int[] getMachIndex() {
    return this.machIndex;
  }  
 
  /**设置该订单该工序的编码，从0开始编码
   * @param processId 非负整数
   */    
  public void setProcessId(int processId) {
    this.processId = processId;
  }

  /**设置该订单该工序可用机器数量
   * @param machQty 正整数
   */  
  public void setMachQty(int machQty) {
    this.machQty = machQty;
  }

  /**设置该工序可以使用的多台机器在全部机器中的编号数组，从0开始编码
   * @param machIndex 一维非负整数数组
   */  
  public void setMachIndex(int[] machIndex) {
    this.machIndex = machIndex;
  }

  /**获取该订单该工序可以分批的数量
   * @param splitQty 正整数
   */   
  public void setSplitQty(int splitQty) {
    this.splitQty = splitQty;
  }

  /**获取该订单该工序的加工时间
   * @param cycleTime 非负实数
   */    
  public void setCycleTime(double cycleTime) {
    this.cycleTime = cycleTime;
  }


    @Override
    public String toString() {
      return "Process{" + "processId=" + processId + ", machQty=" + machQty + ", machIndex=" + Arrays.toString(machIndex) + ", splitQty=" + splitQty + ", cycleTime=" + cycleTime + '}';
    }
        
}

