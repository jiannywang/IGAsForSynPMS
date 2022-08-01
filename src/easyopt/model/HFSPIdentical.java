package easyopt.model;

import java.util.Arrays;

/**混合流水车间算例对象，processQty,jobQty分别为工序数量和作业数量，
 * procMachQty为每个工序中机器的数量【同一工序的机器为同质并行机，即加工同一种作业的工时是相同的】，processTimes为各个作业在不同工序的加工时间
 * */
public class HFSPIdentical {
  public int processQty,jobQty;
  public int[] procMachQty;
  public double[][] processTimes;
  /**构造函数，用于类对象各个属性的值示例*/
  public HFSPIdentical() {
    this.processQty = 10;
    this.jobQty = 5;
    this.procMachQty = new int[]{3,3,2,3,3};
    this.processTimes =new double[][] {{5,6,55,35,77},{46,20,85,72,41},{90,20,87,27,52},
      {18,68,78,48,70},{18,90,60,24,48},{83,49,39,15,8},{96,81,50,87,63},
      {34,97,58,23,22},{33,57,94,21,67},{40,28,25,95,69}};
  }
  @Override
  public String toString() {
    return "HFSPIdentical [processQty=" + processQty + ", jobQty=" + jobQty + ", procMachQty="
        + Arrays.toString(procMachQty) + ", processTimes=" + Arrays.deepToString(processTimes) + "]";
  }
  
  
}
