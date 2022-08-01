
package easyopt.shopSch;



import easyopt.common.EasyMath;

/**
 * 对调度结果schedule数据相关处理方法的类，
 * 例如求调度结果的最大完工时间、完工时间之和、最大延误时间
 * @author PeterWang, easy optimizer.
 * @version 1.0
 */

public class Schedule { 
  

  /**获得特定调度方案中最大的机器编号
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，【编号都从1开始】
   * @return 整数值
   */
  public static int getMaxMachId(double[][] nowSchedule){
      int schRow=nowSchedule.length;
      int machId=0;
      for(int i=0;i<schRow;i++) {
    	  machId = (int) Math.max(machId, nowSchedule[i][1]);
      }
      return machId;
  }   

  /**获得特定调度方案中最大的作业编号
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，【编号都从1开始】
   * @return 整数值
   */
  public static int getMaxJobId(double[][] nowSchedule){
      int schRow=nowSchedule.length;
      int jobId=0;
      for(int i=0;i<schRow;i++) {
    	  jobId = (int) Math.max(jobId, nowSchedule[i][0]);
      }
      return jobId;
  }   
  
  /**获得特定调度方案的最大完工时间和设备空闲时间之和
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，
   * @return 长度为2的一维数组，第1个元素-Cmax 最大完工时间，第2元素-总空闲时间
   */
  public static double[] getCmaxIsumTime(double[][] nowSchedule){
      int schRow=nowSchedule.length;
      double[] obj=new double[2];//0-Cmax，1-Isum
      for(int i=0;i<schRow;i++){//先获得最大完工时间
        obj[0] =Math.max(obj[0], nowSchedule[i][4]);
      }      
      //将nowSchedule按照机器序号和开工时间排序
      EasyMath.sortArray(nowSchedule,new int[] {1,3});
      //获得每个机器的开工开始时间和总的加工时间
      double sumIdleTime=0;
      int nowMachId=(int)nowSchedule[0][1];
      double mySumProcTime=nowSchedule[0][4]-nowSchedule[0][3];
      double myStartTime=nowSchedule[0][3];
      for(int i=1;i<schRow;i++){
        if(nowMachId!=(int)nowSchedule[i][1]){
          //将空闲时间累计一下
          sumIdleTime+=obj[0]-myStartTime-mySumProcTime;
          //中间变量初始化一下
          nowMachId=(int)nowSchedule[i][1];
          mySumProcTime=nowSchedule[i][4]-nowSchedule[i][3];
          myStartTime=nowSchedule[i][3];          
        }else{
          mySumProcTime+=nowSchedule[i][4]-nowSchedule[i][3];
        }
      }
      //将最后一个机器的空闲时间累加一下
      sumIdleTime+=obj[0]-myStartTime-mySumProcTime; 
      obj[1]=sumIdleTime;
      return obj;
  }   

  
  /**获得特定调度方案的最大完工时间
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，
   * @return Cmax 最大完工时间
   */
  public static double getMaxFinishTime(double[][] nowSchedule){
      int schRow=nowSchedule.length;
      double maxFinishTime=0.0;
      for(int i=0;i<schRow;i++){
        maxFinishTime =Math.max(maxFinishTime, nowSchedule[i][4]);
      }
      return maxFinishTime;
  }   

  /**获得特定调度方案的各个作业完工时间之和
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，
   * @return Cmax 最大完工时间
   */
  public static double getTotalFinishTime(double[][] nowSchedule){
    //找出每项作业的完工时间，然后累加起来
    //首先找出总共有多少项作业
    int jobQty=0;
    int rows=nowSchedule.length;
    for(int i=0;i<rows;i++){
      jobQty=Math.max(jobQty,(int)nowSchedule[i][0]);
    }
    double[] eachFinishTimes=new double[jobQty];
    for(int i=0;i<rows;i++){
      int nowJobId=(int)nowSchedule[i][0];
      eachFinishTimes[nowJobId-1]=Math.max(eachFinishTimes[nowJobId-1], nowSchedule[i][4]);
    }  
    //累计全部作业的完工时间和
    double sumFinishTime=EasyMath.sum(eachFinishTimes);
      
    return sumFinishTime;
  }   

  
  /**获得特定调度方案的最大延误时间长度
  * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间
  * @return Lmax 最大延误时间长度
  */
  public static double getMaxLateTime(double[][] nowSchedule){
      int schRow=nowSchedule.length;
      double maxLateTime=0.0;
      for(int i=0;i<schRow;i++){
        maxLateTime =Math.max(maxLateTime, nowSchedule[i][4]-nowSchedule[i][5]);
      }
      return maxLateTime;
  }
    /**获得特定调度方案的延误时间之和
   * @param nowSchedule 调度方案的二维数组，共五列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间
   * @return Lmax 最大延误时间长度
   */
  public static double getSumLateTime(double[][] nowSchedule){
    //找出每个作业的最大完工时间，然后将其减去其交付时间，然后求和获得总的延误时间
    int jobQty=0;
    int rows=nowSchedule.length;
    for(int i=0;i<rows;i++){
      jobQty=Math.max(jobQty,(int)nowSchedule[i][0]);
    }
    double[] eachLateTimes=new double[jobQty];    
    for(int i=0;i<rows;i++){
      int nowJobId=(int)nowSchedule[i][0];
      eachLateTimes[nowJobId-1]=Math.max(eachLateTimes[nowJobId-1], nowSchedule[i][4]-nowSchedule[i][5]);
    } 
    double sumLateTime=EasyMath.sum(eachLateTimes);
    return sumLateTime;
  }  
  
  /**获得同步开工并行机调度方案的延误时间之和
 * @param nowSchedule 调度方案的二维数组，共十一列，第一列：作业编号【从1编号】，第二类：机器编号【从1编号】，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   *@param penalty 非同步开工排产的惩罚系数         
 * @return Lmax 最大延误时间长度
 */
public static double getSumLateTime(double[][] nowSchedule,double penalty){
  //找出每个作业的最大完工时间，然后将其减去其交付时间，然后求和获得总的延误时间
  int jobQty=0;
  int rows=nowSchedule.length;
  for(int i=0;i<rows;i++){
    jobQty=Math.max(jobQty,(int)nowSchedule[i][0]);
  }
  double[] eachLateTimes=new double[jobQty]; 
  //记录惩罚系数
  double sumPenalty=0;
  for(int i=0;i<rows;i++){
    int nowJobId=(int)nowSchedule[i][0];
    eachLateTimes[nowJobId-1]=Math.max(eachLateTimes[nowJobId-1], nowSchedule[i][4]-nowSchedule[i][5]);
    if(nowSchedule[i][10]==0){
    	sumPenalty+=penalty;
    }
  } 
  double sumLateTime=EasyMath.sum(eachLateTimes);
  return sumLateTime+sumPenalty;
}   
  
}
