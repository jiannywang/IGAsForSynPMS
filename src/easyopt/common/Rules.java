/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.common;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 车间调度过程中一些常见的规则排产运算方法
 * @author PeterWang at WOR
 */
public class Rules {

   /**利用先来先服务FCFS规则进行作业排序
  * @param rTimes 作业的释放时间 
  * @return 返回根据FCFS规则排列的作业顺序数组，作业顺序从1开始排序
  */
  public static int[] seqFCFS(double[] rTimes){
    int jobQty=rTimes.length;
    double[][] arriveTimes=new double[jobQty][2];//0-达到时间，1-作业编号
    for(int i=0;i<jobQty;i++){
      arriveTimes[i][0]=rTimes[i];
      arriveTimes[i][1]=i+1;//作业编号从1开始，而非从0开始
    }
    EasyMath.sortArray(arriveTimes, new int[] {0});//按照释放时间排序
    int[] seq=new int[jobQty];
    for(int i=0;i<jobQty;i++){
      seq[i]=(int)arriveTimes[i][1];
    }   
    return seq;   
  } 

   /**利用先来先服务FCFS和最短加工时间优先规则进行作业排序
  * @param pTimes 作业的加工时间  
  * @param rTimes 作业的释放时间 
  * @return 返回根据FCFS规则排列的作业顺序数组，作业顺序从1开始排序
  */
  public static int[] seqFSPT(double[] pTimes,double[] rTimes){
    int jobQty=rTimes.length;
    double[][] arriveTimes=new double[jobQty][3];//0-达到时间,1-加工时间，2-作业编号
    for(int i=0;i<jobQty;i++){
      arriveTimes[i][0]=rTimes[i];
      arriveTimes[i][1]=pTimes[i];      
      arriveTimes[i][2]=i+1;//作业编号从1开始，而非从0开始
    }
    EasyMath.sortArray(arriveTimes, new int[] {0,1});//按照释放时间和加工时间排序
    int[] seq=new int[jobQty];
    for(int i=0;i<jobQty;i++){
      seq[i]=(int)arriveTimes[i][2];
    }   
    return seq;   
  } 

  /**利用最短加工时间优先规则进行作业排序
 * @param pTimes 作业的加工时间  
 * @return 返回根据FCFS规则排列的作业顺序数组，作业顺序从1开始排序
 */
 public static int[] seqSPT(double[] pTimes){
   int jobQty=pTimes.length;
   double[][] sortPTimes=new double[jobQty][2];//0-加工时间，1-作业编号
   for(int i=0;i<jobQty;i++){
	   sortPTimes[i][0]=pTimes[i];      
	   sortPTimes[i][1]=i+1;//作业编号从1开始，而非从0开始
   }
   EasyMath.sortArray(sortPTimes, new int[] {0});//按照释放时间和加工时间排序
   int[] seq=new int[jobQty];
   for(int i=0;i<jobQty;i++){
     seq[i]=(int)sortPTimes[i][1];
   }   
   return seq;   
 }   
  
   /**利用先来先服务FCFS和最长加工时间优先LPT规则进行作业排序
  * @param pTimes 作业的加工时间  
  * @param rTimes 作业的释放时间 
  * @return 返回根据FCFS规则排列的作业顺序数组，作业顺序从1开始排序
  */
  public static int[] seqFLPT(double[] pTimes,double[] rTimes){
    int jobQty=rTimes.length;
    double[][] arriveTimes=new double[jobQty][3];//0-达到时间,1-加工时间，2-作业编号
    for(int i=0;i<jobQty;i++){
      arriveTimes[i][0]=rTimes[i];
      arriveTimes[i][1]=pTimes[i];      
      arriveTimes[i][2]=i+1;//作业编号从1开始，而非从0开始
    }
    EasyMath.sortArray(arriveTimes, new int[] {0,-1});//按照释放时间[升序]和加工时间[降序]排序
    int[] seq=new int[jobQty];
    for(int i=0;i<jobQty;i++){
      seq[i]=(int)arriveTimes[i][2];
    }   
    return seq;   
  }   

   /**利用先来先服务FCFS和最早交付时间优先EDD规则进行作业排序
  * @param dTimes 作业的加工时间  
  * @param rTimes 作业的释放时间 
  * @return 返回根据FCFS规则排列的作业顺序数组，作业顺序从1开始排序
  */
  public static int[] seqFEDD(double[] dTimes,double[] rTimes){
    int jobQty=rTimes.length;
    double[][] arriveTimes=new double[jobQty][3];//0-达到时间,1-交付时间，2-作业编号
    for(int i=0;i<jobQty;i++){
      arriveTimes[i][0]=rTimes[i];
      arriveTimes[i][1]=dTimes[i];      
      arriveTimes[i][2]=i+1;//作业编号从1开始，而非从0开始
    }
    EasyMath.sortArray(arriveTimes, new int[] {0,1});//按照释放时间[升序]和交付时间[升序]排序
    int[] seq=new int[jobQty];
    for(int i=0;i<jobQty;i++){
      seq[i]=(int)arriveTimes[i][2];
    }   
    return seq;   
  }     
  
   /**利用交付时间优先规则进行作业排序
  * @param dtimes 作业的交付时间 
  * @return 返回根据EDD规则排列的作业顺序数组，作业顺序从1开始排序
  */
  public static int[] seqEDD(double[] dtimes){
    int jobQty=dtimes.length;
    double[][] dueTimes=new double[jobQty][2];//0-达到时间，1-作业编号
    for(int i=0;i<jobQty;i++){
      dueTimes[i][0]=dtimes[i];
      dueTimes[i][1]=i+1;//作业编号从1开始，而非从0开始
    }
    EasyMath.sortArray(dueTimes, new int[] {0});//按照释放时间排序
    int[] seq=new int[jobQty];
    for(int i=0;i<jobQty;i++){
      seq[i]=(int)dueTimes[i][1];
    }   
    return seq;   
  } 
  
  /**利用修正交付时间优先规则进行作业排序， Modified Due Date，已获取延误总时长最小Total Tardiness
   * ref: Bahram Alidaee and Gopalan, Suresh. A note on the equivalence of two heuristics to minimize total tardiness[J]. European Journal of Operational Research, 1997(96): 514-517
 * @param ptimes 各作业的加工时间 
 * @param dtimes 各作业的交付时间  
 * @return 返回根据MDD规则排列的作业顺序数组，作业顺序从1开始排序
 */
 public static int[] seqMDD4TT(double[] ptimes,double[] dtimes){
   int jobQty=dtimes.length;
   //step1: 首先将个作业按照SPT规则排序，获得一个初始的排序
   int[] initSeq=seqSPT(ptimes);
   //step2: 逐次对前后相邻的两个作业进行比较，观察是否需要进行位置调换，直至没有需要调换的为止，获得最后的排序
   double[][] sch=getSingleMachSch(ptimes,dtimes,initSeq);
   boolean improved=true;
   while(improved){
	   improved=false;
	   for(int i=0;i<jobQty-1;i++){
		   if(Math.max(sch[i][4],sch[i][5])>Math.max(sch[i+1][4],sch[i+1][5])){
			   int midJobId=initSeq[i];
			   initSeq[i]=initSeq[i+1];
			   initSeq[i+1]=midJobId;
			   sch=getSingleMachSch(ptimes,dtimes,initSeq);
			   improved=true;
		   }
	   }
   }
 
   return initSeq;   
 }   

 /**利用Panwalkar-Smith-Koulamas的算法进行最小延误总时间排序Total Tardiness
  * ref: Panwalkar, S. S., Smith, M. L. and Koulamas, C. P. A heuristic for the single machine tardiness problem[J]. European Journal of Operational Research, 1993, 70(3): 304-310
* @param ptimes 各作业的加工时间 
* @param dtimes 各作业的交付时间  
* @return 返回根据PSK规则排列的作业顺序数组，作业顺序从1开始排序
*/
public static int[] seqPSK4TT(double[] ptimes,double[] dtimes){
  int jobQty=dtimes.length;
  //step1: 首先将个作业按照SPT规则排序，获得一个初始的排序
  int[] initSeq=seqSPT(ptimes);
  ArrayList<Number> waitSeq=new ArrayList<Number>();//待排序的作业列表
  ArrayList<Number> finalSequence=new ArrayList<Number>();//排序完成的作业排序列表 
  for(int i=0;i<jobQty;i++){
	  waitSeq.add(initSeq[i]-1);//作业排序中，作业编号从0开始，便于调度ptimes和dtimes数组中的值
  }
  //step2: 循环判断排序
  double canStartTime=0;
  while(waitSeq.size()>0){
	  int jobi=0;
	  int remainQty=waitSeq.size();
	  if(remainQty==1){
		  //goto the remove and insert part-step8
	  }else{
		  int idOfJobI=waitSeq.get(0).intValue();
		  if(canStartTime+ptimes[idOfJobI]>=dtimes[idOfJobI]){
			  //goto the remove and insert part-step8
		  }else{
			  boolean gotoStep8=false;
			  while(!gotoStep8){
				  for(int j=jobi+1;j<remainQty;j++){
					  int midJobIid=waitSeq.get(jobi).intValue();
					  int midJobJid=waitSeq.get(j).intValue();
					  if(canStartTime+ptimes[midJobJid]>=dtimes[midJobIid]){
						  gotoStep8=true;
					  }else{
						  if(dtimes[midJobIid]<=dtimes[midJobJid]){
							  if(j==remainQty-1){
								  gotoStep8=true;
							  }
						  }else{
							  jobi=j;
							  if(j==remainQty-1){
								  gotoStep8=true;
							  }
						  }
					  }
					  if (gotoStep8){
						  break;
					  }					  
				  }
			  }//endwhile
		  }
	  }//end if(remainQty==1)
	  //step8: remove and insert a job from waitSeq and into finalSequence
	  int outJobId=waitSeq.get(jobi).intValue();
	  waitSeq.remove(jobi);
	  finalSequence.add(outJobId);
	  canStartTime+=ptimes[outJobId];
  }
  
  //step3: 将排序列表转换为排序数组，并以1开始
  for(int i=0;i<jobQty;i++){
	  initSeq[i]=finalSequence.get(i).intValue()+1;
  }
  return initSeq;   
  //夸夸自己，一把头将这个程序搞定，好NB啊：）
} 
 
 /**根据传入的加工时间、交付时间和作业排序生成单一机器生产的详细调度方案
  * @param ptimes 各作业的加工时间 
  * @param dtimes 各作业的交付时间  
  * @param seq 作业排序数组，从1开始编号
  * @return 返回调度排序的二维数组，共六列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间
  */
  public static double[][] getSingleMachSch(double[] ptimes,double[] dtimes,int[] seq){
	    //获得作业的数量
	    int jobQty=seq.length;
	    //定义最后调度排序的数组
	    double[][] schedule=new double[jobQty][6];	    
	    //依次对每个作业进行排产
	    double machCanStartTimes=0;
	    for(int i=0;i<jobQty;i++){
	      double startTimes=machCanStartTimes;
	      int nowJobId=seq[i];
	      schedule[i][0]=nowJobId;
	      schedule[i][1]=1;
	      schedule[i][3]=startTimes;
	      schedule[i][4]=startTimes+ptimes[nowJobId-1];
	      schedule[i][5]=dtimes[nowJobId-1];
	      machCanStartTimes+=ptimes[nowJobId-1];
	    }
    return schedule;
  }  
  
  
}
