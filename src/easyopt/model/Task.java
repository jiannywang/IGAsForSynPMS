/*
 * 各项加工任务
 */
package easyopt.model;

/**
 * 该类对象将表示一项具体的操作：在哪台机器上加工哪种订单，在什么时候开始，又在什么时候结束，
 * 在该操作执行机器上，该操作紧前空闲时段的时长、开始时间和结束时间
 * @author Peterwang@163.com
 * @see Process
 * @see Order
 */
public class Task {
  /**该任务在全部任务中的编号，从0开始编码*/
    public int taskId;
    
    /**该任务所用机器在全部机器中的编码，全部机器从0开始编号*/
    public int machId;
    
    /**任务的开始时间*/
    public double startTime;
    
    /**任务的结束时间*/    
    public double endTime;
    /**任务的交付时间*/    
    public double dueTime;    
    public double getDueTime() {
		return dueTime;
	}

	public void setDueTime(double dueTime) {
		this.dueTime = dueTime;
	}

	/**任务所加工的订单在全部订单中的编号，从0开始*/       
    public int orderId;
    
    /**对应该任务所加工订单的工序编号，从0开始*/       
    public int processId;
    
    /**当前订单当前任务紧前空闲时段的结束时间*/     
    public double gapEndTime;
    
    /**当前订单当前任务紧前空闲时段的开始时间*/     
    public double gapStartTime;
    
    /**当前订单当前任务紧前空闲时段的总空闲时间长度，
     *  记录在需求时间点之前可用的gap最大值，这样后续排产可根据各台机器的gap从大到小排序，进行订单排产*/     
    public double gap;
    
    /**当前任务对应订单的释放时间*/     
    public double releaseTime;
    
    /**同步启停并行机调度中该任务是否可开工，0不可同步开工，1可同步开工*/     
    public double isSynStart;//
    
    /**同步启停并行机调度中该任务所处的工作时段，组号为1以上的整数表示可以同步开工，为0表示不可同步开工*/     
    public double periodNum;    
   

    /**设置同步启停并行机调度中该任务所处的工作时段，组号为1以上的整数表示可以同步开工，为0表示不可同步开工
     * @param periodNum 非负整数
     * 
     */ 
	  public void setPeriodNum(double periodNum) {
	    this.periodNum = periodNum;
	  }
	  
	/**获得同步启停并行机调度中该任务所处的工作时段，组号为1以上的整数表示可以同步开工，为0表示不可同步开工
  * @return 非负整数
	*/ 
	public double getPeriodNum() {
	  return periodNum;
	} 
	
  /**若是同步启停作业调度，设置该任务是否可以开工，0不可开工，1可开工
   * @param isSynStart 0或1
   * 
   */     
  public void setIsSynStart(double isSynStart) {
    this.isSynStart = isSynStart;
  }
  /**获取该任务是否为同步启停
   * @return 0或1
   * 
   */ 
  public double getIsSynStart() {
    return isSynStart;
  }
  /**设置该任务对应订单的释放时间
   * @param releaseTime 非负实数
   * 
   */   
  public void setReleaseTime(double releaseTime) {
    this.releaseTime = releaseTime;
  }

  /**设置该任务所用机器编码，全部机器编码从0开始
   * @param machId 非负整数
   * 
   */    
  public void setMachId(int machId) {
    this.machId = machId;
  }
  /**设置该任务的开工时间
   * @param startTime 非负实数，要比完工时间小
   * 
   */ 
  public void setStartTime(double startTime) {
    this.startTime = startTime;
  }
  /**设置该任务的完工时间
   * @param endTime 非负实数，要比开工时间大
   * 
   */ 
  public void setEndTime(double endTime) {
    this.endTime = endTime;
  }
  /**设置该任务的作业编码，为对应作业在全部作业中的编码，从0开始
   * @param orderId 从0开始的非负整数
   * 
   */ 
  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }
  /**设置该任务的工序编码，为对应作业的工序编码，从0开始
   * @param processId 从0开始的非负整数
   * 
   */  
  public void setProcessId(int processId) {
    this.processId = processId;
  }

  /**设置该任务所用机器在此任务之前紧前空闲时段的结束时间
   * @param gapEndTime 非负实数
   * 
   */  
  public void setGapEndTime(double gapEndTime) {
    this.gapEndTime = gapEndTime;
  }
  /**设置该任务所用机器在此任务之前紧前空闲时段的开始时间
   * @param gapStartTime 非负实数
   * 
   */  
  public void setGapStartTime(double gapStartTime) {
    this.gapStartTime = gapStartTime;
  }

  /**设置该任务所用机器在此任务之前紧前空闲时段的长度
   * @param gap 非负实数，等于该任务紧前空闲时段的结束时间与开始时间之差
   */   
  public void setGap(double gap) {
    this.gap = gap;
  }


/**获得该任务在全部任务列表中的编码，从0开始编码
 * @return 非负整数值
 */
  public int getTaskId() {
    return taskId;
  }
  
  /**设置该任务在全部任务列表中的编码，从0开始编码
   * @param taskId 一个非负整数序列值
   */
  public void setTaskId(int taskId) {
    this.taskId = taskId;
  }
    
/** 任务的基本信息构造函数
 * @param inMachId 该任务对应机器在全部机器中的编码，从0开始编码
 * @param inStartTime 该任务在该机器上的开工时间
 * @param inEndTime 该任务在该机器上的完工时间
 * @param inOrderId 该任务对应订单在全部订单中的编码，从0开始编码
 */    
  public Task(int inMachId,double inStartTime ,double inEndTime,int inOrderId){
    machId=inMachId;
    startTime=inStartTime;
    endTime=inEndTime;
    orderId=inOrderId;
    gap=0;gapEndTime=0;gapStartTime=0;
  }
/**获得该任务对应的订单的编码，从0开始编码
 * @return 非负整数值
 */
  public int getOrderId() {
    return orderId;
  }
/**获得该任务对应的作业工序的编码，从0开始编码
 * @return 非负整数值
 */
  public int getProcessId() {
    return processId;
  }
  /**获得该任务对应的完工时间
   * @return 非负实数
   */
  public double getEndTime(){
    return endTime;
  }
  /**同一台机器上前后任务时间可能存在空闲时间，该函数获得当前任务紧前空闲时间的结束时间，等于该任务的开工时间
   * @return 非负实数
   */
  public double getGapEndTime(){
    return gapEndTime;
  }  
  /**同一台机器上前后任务时间可能存在空闲时间，该函数是设置当前任务紧前空闲时间的开始时间
   * @param time 非负实数
   */    
  public void setGapStime(int time){
    this.gapStartTime=time;
  }
  
  /**同一台机器上前后任务时间可能存在空闲时间，该函数是设置当前任务紧前空闲时间的结束时间
   * @param time 非负实数
   */    
  public void setGapEtime(int time){
    this.gapEndTime=time;
  }
  /**同一台机器上前后任务时间可能存在空闲时间，该函数是将当前任务紧前空闲时间的开始时间和结束时间置0
   */    
   public void setZeros(){
    this.gapEndTime=0;
    this.gapStartTime=0;
    this.gap=0;
  }   
     
/**获得该任务的开工时间
 * @return 非负实数值
 */     
  public double getStartTime(){
    return this.startTime;
  }
/**获得该任务对应作业的释放时间
 * @return 非负实数值
 */   
  public double getReleaseTime(){
    return this.releaseTime;
  }  
  
/**获得该任务对应所使用的机器在全部机器中的编码，从0开始编码
 * @return 0至【machQty-1】区间的整数
 */    
  public int getMachId(){
    return this.machId;
  }  
/**获得该任务所用机器在此任务之前紧前空闲时段的长度
 * @return 正实数
 */    
  public double getGap(){
    return this.gap;
  }
/**获得该任务所用机器在此任务之前空闲时段的开始时间，
 * 除非是该机器上的第一个任务，要不然该数组等于该任务之前的任务的完工时间
 * @return 非负实数值
 */       
  public double getGapStartTime(){
    return this.gapStartTime;
  }    

  @Override
//  public String toString() {
//    return "Task{" + "machId=" + machId + ", startTime=" + startTime + ", endTime=" + endTime 
//            + ", orderId=" + orderId  + ", processId=" + processId + ", gapEndTime=" + gapEndTime 
//            + ", gapStartTime=" + gapStartTime + ", gap=" + gap+ ", releaseTime=" + releaseTime + '}';
//  }
  public String toString() {
	    return "Task{" +  "startTime=" + startTime + ", endTime=" + endTime 
	            + ", orderId=" + orderId  + ", dueTime=" + dueTime + '}';
	  }  
    
}
