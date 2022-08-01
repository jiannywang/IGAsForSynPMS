/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.shopSch.pms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import easyopt.common.ACO;
import easyopt.common.GA;
import easyopt.common.PSO;
import easyopt.common.Rules;
import easyopt.common.EasyMath;
import easyopt.common.TLBO;
import easyopt.model.Machine;
import easyopt.model.Order;
import easyopt.model.Task;
import easyopt.shopSch.SchOptResult;
import easyopt.shopSch.Schedule;
import easyopt.shopSch.Scheduling;

/**
 * 同步启停并行机调度中使用的一些计算方法和优化算法
 * @author PeterWang, ShowClean Optimization Corporation.
 * @version 1.0
 */
public class SynPMS {
	
	  /**基于作业时间、作业排序和机器数量获得并行机【单阶段同质并行机-同步启停并行机，具有交付时间的】详细调度方案
	  * @param ptimes 作业时间
	  * @param dTimes 交付时间
	  * @param rTimes 释放时间
	  * @param seq 作业排序数组，从1开始编号，数据传递过程必须要保证ptimes、dtimes和seq的长度相等
	  * @param machQty 并行机数量
	  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
	  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
	  */
	  public  double[][] getSynPMSch(double[] ptimes,double[] dTimes,int[] seq,int machQty,double[] rTimes){
	//初始化作业列表
	    List<Order> orderList=Scheduling.initOrderListPMS(ptimes, rTimes, dTimes);
	    //初始化最终调度任务列表
	    List<Task> finalTasks=new ArrayList<>();
	    //初始化中间任务列表
	    List<Task> nowSch=new ArrayList<>();
	    //将作业列表按照seq中的顺序逆序排序，便于后续调度结束将对应作业从列表中删除
	    List<Order> sortOrders=new ArrayList<>();
	    int jobQty=ptimes.length;
	    for(int i=jobQty-1;i>=0;i--){
	      int orderId=seq[i]-1;
	      Order or=orderList.get(orderId);
	      sortOrders.add(or);      
	    }
	    //定义设备前一完工时间
	    double[][] machPreEndTime=new double[machQty][2];//第一列前序完工时间，第二列为机器编号，从0开始
	    for(int i=0;i<machQty;i++){
	      machPreEndTime[i][1]=i;
	    }    
	    boolean notEnd=true;//确定排产结束条件是否成立
	    int periodNum=1;
	    while(notEnd&&periodNum<100000){
	      int remainQty=sortOrders.size();
	      int hasJobMachQty=nowSch.size();
	      int noJobMachQty=machQty-hasJobMachQty;
	      if(remainQty<noJobMachQty){//剩余作业数量比需要派任务的机器数量少时，这个时段的任务无法完成
	        notEnd=false;
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=remainQty-1;i>=0;i--){
	          Order or=sortOrders.get(i);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[remainQty-i-1][1];
	          double startTime=machPreEndTime[remainQty-i-1][0];
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          tk.setDueTime(or.getDueTime());
	          nowSch.add(tk);
	          machPreEndTime[remainQty-i-1][0]=endTime;
	        }
	      }else{
	        //最好有一个过程判断nowSch这个TaskList中没有出现同一个机器两个以上的任务，先不处理******
	         //两件事，（1）将序列中前Q个放入队列，然后根据releaseTime生成具体排产方案；（2）判断是否还有其他的作业可放入队列
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=0;i<noJobMachQty;i++){
	          int jobSeqId=remainQty-i-1;
	          Order or=sortOrders.get(jobSeqId);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[i][1];
	          double startTime=machPreEndTime[i][0];
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          tk.setDueTime(or.getDueTime());
	          nowSch.add(tk);
	          sortOrders.remove(jobSeqId);//从作业列表删除
	          machPreEndTime[i][0]=Math.max(endTime, machPreEndTime[i][0]);
	        }
	        //生成具体的调度方案,先获得最大的释放时间，为了预防释放时间小的作业都被安排在后面的序列中，增加
	        //    开工时间的判断
	        double maxReleaseTime=0;
	        for(Task tk:nowSch){
	          maxReleaseTime=Math.max(maxReleaseTime,tk.getReleaseTime());
	          maxReleaseTime=Math.max(maxReleaseTime, tk.getStartTime());
	        }      
	        // -----根据maxReleaseTime进行调度方案的更新
	        EasyMath.sortArray(machPreEndTime, new int[] {1}); //根据机器id升序排序 
	        int schQty=nowSch.size();
	        for(int i=0;i<schQty;i++){
	          double gap=maxReleaseTime-nowSch.get(i).getStartTime();
	          double myStartTime=nowSch.get(i).getStartTime()+gap;
	          double myEndTime=nowSch.get(i).getEndTime()+gap;
	          nowSch.get(i).setStartTime(myStartTime);
	          nowSch.get(i).setEndTime(myEndTime);  
	          nowSch.get(i).setPeriodNum(periodNum);
	          int machId=nowSch.get(i).getMachId();
	          machPreEndTime[machId][0]=Math.max(machPreEndTime[machId][0], myEndTime);//更新前一时段完工时间
	        }      
	        //--判断后续作业是否可以放入当前列表
	        boolean noEnd2=true;
	        while(noEnd2&&periodNum<10000){
	          EasyMath.sortArray(machPreEndTime, new int[] {0}); //根据机器前一完工时间升序排序 
	          int remainJob2=sortOrders.size();
	          if(remainJob2>0){
	            Order nowOrder=sortOrders.get(remainJob2-1);
	            double nextReleaseTime=nowOrder.getReleaseTime();
	            if(nextReleaseTime<=machPreEndTime[0][0]){
	              Order or=nowOrder;
	              int orderId=or.getOrderId();
	              double cycleTime=or.getProcTime();
	              double releaseTime=or.getReleaseTime();
	              int machId=(int)machPreEndTime[0][1];
	              double startTime=machPreEndTime[0][0];
	              double endTime=startTime+cycleTime;
	              Task tk=new Task(machId,startTime,endTime,orderId);
	              tk.setReleaseTime(releaseTime);
	              tk.setPeriodNum(periodNum);
	              tk.setDueTime(or.getDueTime());
	              nowSch.add(tk);
	              sortOrders.remove(remainJob2-1);//从作业列表删除  
	              machPreEndTime[0][0]=Math.max(machPreEndTime[0][0], endTime);//更新前一时段完工时间              
	            }else{
	              noEnd2=false;            
	            }
	          }else{
	            noEnd2=false;
	          }
	        }//后续的可排产作业安排结束     
	        //生成一个完整时段的排序任务，将其放入最终的任务列表，同时将剩下的任务放入nowSch
	        schQty=nowSch.size();
	        double earlyEndTime=machPreEndTime[0][0];
	        for(int i=0;i<machQty;i++){
	          earlyEndTime=Math.min(earlyEndTime, machPreEndTime[i][0]);
	        }
	        
	        //将完工时间大于earlyEndTime的任务拆分
	        List<Task> midSchList=new ArrayList<>();
	        for(int i=0;i<schQty;i++){
	          if(nowSch.get(i).getEndTime()>earlyEndTime){
	            double endTime=nowSch.get(i).getEndTime(); //没拆分前的完工时间           
	            nowSch.get(i).setEndTime(earlyEndTime);
	            int machId=nowSch.get(i).getMachId();
	            int orderId=nowSch.get(i).getOrderId();
	            Task midTk=new Task(machId,earlyEndTime,endTime,orderId);
	            midTk.setReleaseTime(nowSch.get(i).getReleaseTime());
	            midSchList.add(midTk);
	          }
	        }
	        //将nowSch中的任务加入最终任务列表
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }
	        //用拆分后的任务列表替换nowSch
	        nowSch=midSchList;    
	        periodNum++;
	      }
	      //当排产作业列表为空时，如果nowSch中还有其他的部分排产，则将其加入最终排产之后
	      if(sortOrders.isEmpty()){
	        notEnd=false;//如果作业列表中的作业全部排完，则结束循环
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }        
	      }      
	      
	    }
	    //至此，全部任务都排入finalTasks中，下面将finalTasks中的数据转换为数组
	    int schRows=finalTasks.size();
	    double[][] schedule=new double[schRows][11];
	    for(int i=0;i<schRows;i++){
	      Task tk2=finalTasks.get(i);
	      schedule[i][0]=tk2.getOrderId()+1;
	      schedule[i][1]=tk2.getMachId()+1;
	      schedule[i][3]=tk2.getStartTime();
	      schedule[i][4]=tk2.getEndTime();
	      schedule[i][5]=dTimes[tk2.getOrderId()];
	      schedule[i][6]=tk2.getReleaseTime();
	      schedule[i][10]=tk2.getPeriodNum();
	    }   
	    return schedule;
	    
	  }
	  
	  /**基于作业时间、作业排序和机器数量获得并行机【单阶段同质并行机-同步启停并行机】详细调度方案,用于求解Cmax最小化的问题
	  * @param ptimes 作业时间
	  * @param rTimes 释放时间
	  * @param seq 作业排序数组，从1开始编号，数据传递过程必须要保证ptimes、dtimes和seq的长度相等
	  * @param machQty 并行机数量
	  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
	  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
	  */
	  public  double[][] getSynPMSch(double[] ptimes,int[] seq,int machQty,double[] rTimes){
	//初始化作业列表
	    List<Order> orderList=Scheduling.initOrderListPMS(ptimes, rTimes);
	    //初始化最终调度任务列表
	    List<Task> finalTasks=new ArrayList<>();
	    //初始化中间任务列表
	    List<Task> nowSch=new ArrayList<>();
	    //将作业列表按照seq中的顺序逆序排序，便于后续调度结束将对应作业从列表中删除
	    List<Order> sortOrders=new ArrayList<>();
	    int jobQty=ptimes.length;
	    for(int i=jobQty-1;i>=0;i--){
	      int orderId=seq[i]-1;
	      Order or=orderList.get(orderId);
	      sortOrders.add(or);      
	    }
	    //定义设备前一完工时间
	    double[][] machPreEndTime=new double[machQty][2];//第一列前序完工时间，第二列为机器编号，从0开始
	    for(int i=0;i<machQty;i++){
	      machPreEndTime[i][1]=i;
	    }    
	    boolean notEnd=true;//确定排产结束条件是否成立
	    int periodNum=1;
	    while(notEnd&&periodNum<100000){
	      int remainQty=sortOrders.size();
	      int hasJobMachQty=nowSch.size();//新作业时段排产开始时的调度列表中对象数量
	      int noJobMachQty=machQty-hasJobMachQty;
	      if(remainQty<noJobMachQty){//剩余作业数量比需要派任务的机器数量少时，这个时段的任务无法完成
	        notEnd=false;
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=remainQty-1;i>=0;i--){
	          Order or=sortOrders.get(i);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[remainQty-i-1][1];
	          double startTime=Math.max(machPreEndTime[remainQty-i-1][0],or.getReleaseTime());
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          nowSch.add(tk);
	          sortOrders.remove(i);
	          machPreEndTime[remainQty-i-1][0]=endTime;
	        }
	        //生成具体的调度方案,先获得最大的释放时间，为了预防释放时间小的作业都被安排在后面的序列中，增加
	        //    开工时间的判断
	        double maxReleaseTime=0;
	        for(Task tk:nowSch){
	          maxReleaseTime=Math.max(maxReleaseTime,tk.getReleaseTime());
	          maxReleaseTime=Math.max(maxReleaseTime, tk.getStartTime());
	        }      
	        // -----根据maxReleaseTime进行调度方案的更新
	        EasyMath.sortArray(machPreEndTime, new int[] {1}); //根据机器id升序排序 
	        int schQty=nowSch.size();
	        for(int i=0;i<schQty;i++){
	          double gap=maxReleaseTime-nowSch.get(i).getStartTime();
	          double myStartTime=nowSch.get(i).getStartTime()+gap;
	          double myEndTime=nowSch.get(i).getEndTime()+gap;
	          nowSch.get(i).setStartTime(myStartTime);
	          nowSch.get(i).setEndTime(myEndTime);  
	          nowSch.get(i).setPeriodNum(periodNum);
	          int machId=nowSch.get(i).getMachId();
	          machPreEndTime[machId][0]=Math.max(machPreEndTime[machId][0], myEndTime);//更新前一时段完工时间
	        }           
	        
	      }else{
	        //最好有一个过程判断nowSch这个TaskList中没有出现同一个机器两个以上的任务，先不处理******
	         //两件事，（1）将序列中前Q个放入队列，然后根据releaseTime生成具体排产方案；（2）判断是否还有其他的作业可放入队列
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=0;i<noJobMachQty;i++){
	          int jobSeqId=remainQty-i-1;
	          Order or=sortOrders.get(jobSeqId);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[i][1];
	          double startTime=Math.max(machPreEndTime[i][0],releaseTime);
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          nowSch.add(tk);
	          sortOrders.remove(jobSeqId);//从作业列表删除
	          machPreEndTime[i][0]=Math.max(endTime, machPreEndTime[i][0]);
	        }
	        //生成具体的调度方案,先获得最大的释放时间，为了预防释放时间小的作业都被安排在后面的序列中，增加
	        //    开工时间的判断
	        double maxReleaseTime=0;
	        for(Task tk:nowSch){
	          maxReleaseTime=Math.max(maxReleaseTime,tk.getReleaseTime());
	          maxReleaseTime=Math.max(maxReleaseTime, tk.getStartTime());
	        }      
	        // -----根据maxReleaseTime进行调度方案的更新
	        EasyMath.sortArray(machPreEndTime, new int[] {1}); //根据机器id升序排序 
	        int schQty=nowSch.size();
	        for(int i=0;i<schQty;i++){
	          double gap=maxReleaseTime-nowSch.get(i).getStartTime();
	          double myStartTime=nowSch.get(i).getStartTime()+gap;
	          double myEndTime=nowSch.get(i).getEndTime()+gap;
	          nowSch.get(i).setStartTime(myStartTime);
	          nowSch.get(i).setEndTime(myEndTime);  
	          nowSch.get(i).setPeriodNum(periodNum);
	          int machId=nowSch.get(i).getMachId();
	          machPreEndTime[machId][0]=Math.max(machPreEndTime[machId][0], myEndTime);//更新前一时段完工时间
	        }      
	        //--判断后续作业是否可以放入当前列表
	        boolean noEnd2=true;
	        while(noEnd2&&periodNum<10000){
	          EasyMath.sortArray(machPreEndTime, new int[] {0}); //根据机器前一完工时间升序排序 
	          int remainJob2=sortOrders.size();
	          if(remainJob2>0){
	            Order nowOrder=sortOrders.get(remainJob2-1);
	            double nextReleaseTime=nowOrder.getReleaseTime();
	            if(nextReleaseTime<=machPreEndTime[0][0]){
	              Order or=nowOrder;
	              int orderId=or.getOrderId();
	              double cycleTime=or.getProcTime();
	              double releaseTime=or.getReleaseTime();
	              int machId=(int)machPreEndTime[0][1];
	              double startTime=machPreEndTime[0][0];
	              double endTime=startTime+cycleTime;
	              Task tk=new Task(machId,startTime,endTime,orderId);
	              tk.setReleaseTime(releaseTime);
	              tk.setPeriodNum(periodNum);
	              nowSch.add(tk);
	              sortOrders.remove(remainJob2-1);//从作业列表删除  
	              machPreEndTime[0][0]=Math.max(machPreEndTime[0][0], endTime);//更新前一时段完工时间              
	            }else{
	              noEnd2=false;            
	            }
	          }else{
	            noEnd2=false;
	          }
	        }//后续的可排产作业安排结束     
	        //生成一个完整时段的排序任务，将其放入最终的任务列表，同时将剩下的任务放入nowSch
	        schQty=nowSch.size();
	        double earlyEndTime=machPreEndTime[0][0];
	        for(int i=0;i<machQty;i++){
	          earlyEndTime=Math.min(earlyEndTime, machPreEndTime[i][0]);
	        }
	        
	        //将完工时间大于earlyEndTime的任务拆分
	        List<Task> midSchList=new ArrayList<>();
	        for(int i=0;i<schQty;i++){
	          if(nowSch.get(i).getEndTime()>earlyEndTime){
	            double endTime=nowSch.get(i).getEndTime(); //没拆分前的完工时间           
	            nowSch.get(i).setEndTime(earlyEndTime);
	            int machId=nowSch.get(i).getMachId();
	            int orderId=nowSch.get(i).getOrderId();
	            Task midTk=new Task(machId,earlyEndTime,endTime,orderId);
	            midTk.setReleaseTime(nowSch.get(i).getReleaseTime());
	            midSchList.add(midTk);
	          }
	        }
	        //将nowSch中的任务加入最终任务列表
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }
	        //用拆分后的任务列表替换nowSch
	        nowSch=midSchList;    
	        periodNum++;
	      }
	      //当排产作业列表为空时，如果nowSch中还有其他的部分排产，则将其加入最终排产之后
	      if(sortOrders.isEmpty()){
	        notEnd=false;//如果作业列表中的作业全部排完，则结束循环
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }        
	      }      
	      
	    }
	    //至此，全部任务都排入finalTasks中，下面将finalTasks中的数据转换为数组
	    int schRows=finalTasks.size();
	    double[][] schedule=new double[schRows][11];
	    for(int i=0;i<schRows;i++){
	      Task tk2=finalTasks.get(i);
	      schedule[i][0]=tk2.getOrderId()+1;
	      schedule[i][1]=tk2.getMachId()+1;
	      schedule[i][3]=tk2.getStartTime();
	      schedule[i][4]=tk2.getEndTime();
	      schedule[i][6]=tk2.getReleaseTime();
	      schedule[i][10]=tk2.getPeriodNum();
	    }   
	    return schedule;
	    
	  }

	  /**基于机器列表【每个机器对象中有所排定的订单列表】获得并行机【单阶段同质并行机-同步启停并行机，具有交付时间的】详细调度方案
	  * @param inMachList 机器对象列表-每个元素为一个Machine对象，包含机器编号和该机器上的排定的订单对象列表
	  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
	  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
	  */
	  public  double[][] getSynPMSch(ArrayList<Machine> inMachList){
		  ArrayList<Machine> machList=copyMachList(inMachList);
		  int machQty=machList.size();	  
	     //初始化中间任务列表
	     List<Task> nowSch=new ArrayList<>();		  
		  //依次确定各台机器上是否有订单对象
		  boolean notEnd=true;
		  int periodNo=0;//同步周期数
		  double preEndTime=0;//前一个同步周期的完工时间
		  while(notEnd&periodNo<100000){
			  notEnd=false;
			  int remainQty=0;//剩下机器列表上还有待排序订单的机器数量，如果每台机器上都有可用的排产订单，则可以进行一个时段的排产
			  for(int i=0;i<machQty;i++){
				  if(machList.get(i).getOrderList().size()>0){
					  remainQty++;
					  notEnd=true;
				  }
			  }
			  if(remainQty==machQty){//可以排产-同步开工
				  //获取最早开工时间：第一个订单的释放时间、机器前一个作业完工时间的最大值
				  double canStartTime=preEndTime;
				  for(int i=0;i<machQty;i++){
					  canStartTime=Math.max(canStartTime, machList.get(i).getOrderList().get(0).releaseTime);
				  }
				  //如果可开工时间同preEndTime相同且不为0，则周期编号不变，否则周期编号加1,并进行具体的任务分配
				  if(canStartTime==preEndTime&&preEndTime>0){
					  //同步周期不变：找到最早完工的订单，即每台机器上同步订单最早完工的时间			  
					  
				  }else{
					  periodNo++;
					  
				  }
				  //进行具体的调度任务安排
				   //找到最早完工的订单，即每台机器上同步订单最早完工的时间	
				  double minProcTime=Double.MAX_VALUE;
				  for(int i=0;i<machQty;i++){
					  minProcTime=Math.min(minProcTime, machList.get(i).getOrderList().get(0).procTime);
				  }
				  preEndTime=canStartTime+minProcTime;
				  //进行具体的调度任务安排
				  for(int i=0;i<machQty;i++){
					  int machId=i;
					  Order or=machList.get(i).getOrderList().get(0);
					  int jobId=or.orderId;				  
					  Task tk=new Task(machId,canStartTime,preEndTime,jobId);
					  tk.setPeriodNum(periodNo);
			          tk.setReleaseTime(or.releaseTime);
			          tk.setDueTime(or.dueTime);
			          nowSch.add(tk);
			          //进行订单工时处理
			          if(or.procTime==minProcTime){
			        	  machList.get(i).getOrderList().remove(0);
			          }else{
			        	  or.procTime-=minProcTime;
			          }
				  }				  
			  }else{//不能完全同步开工的，则进行一般性处理
				  //如果不能完全同步，则部分同步
				  double canStartTime=preEndTime;
				  double minProcTime=Double.MAX_VALUE;
				  for(int i=0;i<machQty;i++){
					  if(machList.get(i).getOrderList().size()>0){
					      canStartTime=Math.max(canStartTime, machList.get(i).getOrderList().get(0).releaseTime);
						  minProcTime=Math.min(minProcTime, machList.get(i).getOrderList().get(0).procTime);
					  }
				  }	
				  preEndTime+=minProcTime;
				  //进行具体的调度任务安排
				  for(int i=0;i<machQty;i++){
					  if(machList.get(i).getOrderList().size()>0){
						  int machId=i;
						  Order or=machList.get(i).getOrderList().get(0);
						  int jobId=or.orderId;				  
						  Task tk=new Task(machId,canStartTime,preEndTime,jobId);
						  tk.setPeriodNum(0);
				          tk.setReleaseTime(or.releaseTime);
				          tk.setDueTime(or.dueTime);
				          nowSch.add(tk);
				          //进行订单工时处理
				          if(or.procTime==minProcTime){
				        	  machList.get(i).getOrderList().remove(0);
				          }else{
				        	  or.procTime-=minProcTime;
				          }
					  }
				  }					  
			  }
		  }//end while
		  //进行同一个作业同一个同步周期的合并操作
		  //首先进行排序，按照机器、开工时间升序排序
		  nowSch.sort(Comparator.comparingInt(Task::getMachId).thenComparing(Task::getStartTime));
		    int schRows=nowSch.size();
		    for(int i=schRows-1;i>0;i--){
		    	Task tk1=nowSch.get(i);
		    	Task tk2=nowSch.get(i-1);
		    	if(tk1.machId==tk2.machId&&tk1.orderId==tk2.orderId&&tk1.periodNum==tk2.periodNum
		    			&&tk1.startTime==tk2.endTime){
		    		nowSch.get(i-1).endTime=nowSch.get(i).endTime;
		    		nowSch.remove(i);
		    	}
		    }
		    schRows=nowSch.size();
		    double[][] schedule=new double[schRows][11];
		    for(int i=0;i<schRows;i++){
		      Task tk2=nowSch.get(i);
		      schedule[i][0]=tk2.getOrderId()+1;
		      schedule[i][1]=tk2.getMachId()+1;
		      schedule[i][3]=tk2.getStartTime();
		      schedule[i][4]=tk2.getEndTime();
		      schedule[i][5]=tk2.getDueTime();
		      schedule[i][6]=tk2.getReleaseTime();
		      schedule[i][10]=tk2.getPeriodNum();
		    } 		  
	    return schedule;
	    
	  }	  
	  
	  /**基于作业列表和机器数量获得并行机【单阶段同质并行机-同步启停并行机，具有交付时间的】详细调度方案
	  * @param orderList 作业列表-订单列表，每个元素为一个Order对象，包含加工时间和释放时间
	  * @param machQty 并行机数量
	  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
	  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
	  */
	  public  double[][] getSynPMSch(List<Order> orderList,int machQty){
		  //System.out.println(orderList.toString());
		  int jobQty=orderList.size();
		  double[] ptimes=new double[jobQty];
		  double[] duetimes=new double[jobQty];
		  double[] rtimes=new double[jobQty];
		  int[] seq=new int[jobQty];
		  for(int i=0;i<jobQty;i++){
			  Order or=orderList.get(i);
			  int midOrderId=or.getOrderId();
			  ptimes[midOrderId]=or.getProcTime();
			  duetimes[midOrderId]=or.getDueTime();
			  rtimes[midOrderId]=or.getReleaseTime();
			  seq[i]=or.getOrderId()+1;
		  }
		  double[][] sch=getSynPMSch(ptimes, duetimes, seq, machQty, rtimes);
	    return sch;
	    
	  }
	  
	  /**基于作业列表和机器数量获得并行机【单阶段同质并行机-同步启停并行机，具有交付时间的】详细调度方案
	  * @param orderList 作业列表-订单列表，每个元素为一个Order对象，包含加工时间和释放时间
	  * @param machQty 并行机数量
	  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
	  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
	  */
	  public  double[][] getSynPMSchNoUse(List<Order> orderList,int machQty){
	    //初始化最终调度任务列表
	    List<Task> finalTasks=new ArrayList<>();
	    //初始化中间任务列表
	    List<Task> nowSch=new ArrayList<>();
	    //将输入订单列表转换为后续生成详细调度方案所需的输入列表
	    List<Order> sortOrders=new ArrayList<>();
	    sortOrders.addAll(orderList);

	    //定义设备前一完工时间
	    double[][] machPreEndTime=new double[machQty][2];//第一列前序完工时间，第二列为机器编号，从0开始
	    for(int i=0;i<machQty;i++){
	      machPreEndTime[i][1]=i;
	    }    
	    boolean notEnd=true;//确定排产结束条件是否成立
	    int periodNum=1;
	    while(notEnd&&periodNum<100000){
	      int remainQty=sortOrders.size();
	      int hasJobMachQty=nowSch.size();//新作业时段排产开始时的调度列表中对象数量
	      int noJobMachQty=machQty-hasJobMachQty;
	      if(remainQty<noJobMachQty){//剩余作业数量比需要派任务的机器数量少时，这个时段的任务无法完成
	        notEnd=false;
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=remainQty-1;i>=0;i--){
	          Order or=sortOrders.get(i);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[remainQty-i-1][1];
	          double startTime=Math.max(machPreEndTime[remainQty-i-1][0],or.getReleaseTime());
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          tk.setDueTime(or.getDueTime());
	          nowSch.add(tk);
	          sortOrders.remove(i);
	          machPreEndTime[remainQty-i-1][0]=endTime;
	        }
	        //生成具体的调度方案,先获得最大的释放时间，为了预防释放时间小的作业都被安排在后面的序列中，增加
	        //    开工时间的判断
	        double maxReleaseTime=0;
	        for(Task tk:nowSch){
	          maxReleaseTime=Math.max(maxReleaseTime,tk.getReleaseTime());
	          maxReleaseTime=Math.max(maxReleaseTime, tk.getStartTime());
	        }      
	        // -----根据maxReleaseTime进行调度方案的更新
	        EasyMath.sortArray(machPreEndTime, new int[] {1}); //根据机器id升序排序 
	        int schQty=nowSch.size();
	        for(int i=0;i<schQty;i++){
	          double gap=maxReleaseTime-nowSch.get(i).getStartTime();
	          double myStartTime=nowSch.get(i).getStartTime()+gap;
	          double myEndTime=nowSch.get(i).getEndTime()+gap;
	          nowSch.get(i).setStartTime(myStartTime);
	          nowSch.get(i).setEndTime(myEndTime);  
	          nowSch.get(i).setPeriodNum(periodNum);
	          int machId=nowSch.get(i).getMachId();
	          machPreEndTime[machId][0]=Math.max(machPreEndTime[machId][0], myEndTime);//更新前一时段完工时间
	        }           
	        
	      }else{
	        //最好有一个过程判断nowSch这个TaskList中没有出现同一个机器两个以上的任务，先不处理******
	         //两件事，（1）将序列中前Q个放入队列，然后根据releaseTime生成具体排产方案；（2）判断是否还有其他的作业可放入队列
	        EasyMath.sortArray(machPreEndTime, new int[] {0});
	        for(int i=0;i<noJobMachQty;i++){
	          int jobSeqId=remainQty-i-1;
	          Order or=sortOrders.get(jobSeqId);
	          int orderId=or.getOrderId();
	          double cycleTime=or.getProcTime();
	          double releaseTime=or.getReleaseTime();
	          int machId=(int)machPreEndTime[i][1];
	          double startTime=Math.max(machPreEndTime[i][0],releaseTime);
	          double endTime=startTime+cycleTime;
	          Task tk=new Task(machId,startTime,endTime,orderId);
	          tk.setReleaseTime(releaseTime);
	          tk.setPeriodNum(periodNum);
	          tk.setDueTime(or.getDueTime());
	          nowSch.add(tk);
	          sortOrders.remove(jobSeqId);//从作业列表删除
	          machPreEndTime[i][0]=Math.max(endTime, machPreEndTime[i][0]);
	        }
	        //生成具体的调度方案,先获得最大的释放时间，为了预防释放时间小的作业都被安排在后面的序列中，增加
	        //    开工时间的判断
	        double maxReleaseTime=0;
	        for(Task tk:nowSch){
	          maxReleaseTime=Math.max(maxReleaseTime,tk.getReleaseTime());
	          maxReleaseTime=Math.max(maxReleaseTime, tk.getStartTime());
	        }      
	        // -----根据maxReleaseTime进行调度方案的更新
	        EasyMath.sortArray(machPreEndTime, new int[] {1}); //根据机器id升序排序 
	        int schQty=nowSch.size();
	        for(int i=0;i<schQty;i++){
	          double gap=maxReleaseTime-nowSch.get(i).getStartTime();
	          double myStartTime=nowSch.get(i).getStartTime()+gap;
	          double myEndTime=nowSch.get(i).getEndTime()+gap;
	          nowSch.get(i).setStartTime(myStartTime);
	          nowSch.get(i).setEndTime(myEndTime);  
	          nowSch.get(i).setPeriodNum(periodNum);
	          int machId=nowSch.get(i).getMachId();
	          machPreEndTime[machId][0]=Math.max(machPreEndTime[machId][0], myEndTime);//更新前一时段完工时间
	        }      
	        //--判断后续作业是否可以放入当前列表
	        boolean noEnd2=true;
	        while(noEnd2&&periodNum<10000){
	          EasyMath.sortArray(machPreEndTime, new int[] {0}); //根据机器前一完工时间升序排序 
	          int remainJob2=sortOrders.size();
	          if(remainJob2>0){
	            Order nowOrder=sortOrders.get(remainJob2-1);
	            double nextReleaseTime=nowOrder.getReleaseTime();
	            if(nextReleaseTime<=machPreEndTime[0][0]){
	              Order or=nowOrder;
	              int orderId=or.getOrderId();
	              double cycleTime=or.getProcTime();
	              double releaseTime=or.getReleaseTime();
	              int machId=(int)machPreEndTime[0][1];
	              double startTime=machPreEndTime[0][0];
	              double endTime=startTime+cycleTime;
	              Task tk=new Task(machId,startTime,endTime,orderId);
	              tk.setReleaseTime(releaseTime);
	              tk.setPeriodNum(periodNum);
	              tk.setDueTime(or.getDueTime());
	              nowSch.add(tk);
	              sortOrders.remove(remainJob2-1);//从作业列表删除  
	              machPreEndTime[0][0]=Math.max(machPreEndTime[0][0], endTime);//更新前一时段完工时间              
	            }else{
	              noEnd2=false;            
	            }
	          }else{
	            noEnd2=false;
	          }
	        }//后续的可排产作业安排结束     
	        //生成一个完整时段的排序任务，将其放入最终的任务列表，同时将剩下的任务放入nowSch
	        schQty=nowSch.size();
	        double earlyEndTime=machPreEndTime[0][0];
	        for(int i=0;i<machQty;i++){
	          earlyEndTime=Math.min(earlyEndTime, machPreEndTime[i][0]);
	        }
	        
	        //将完工时间大于earlyEndTime的任务拆分
	        List<Task> midSchList=new ArrayList<>();
	        for(int i=0;i<schQty;i++){
	          if(nowSch.get(i).getEndTime()>earlyEndTime){
	            double endTime=nowSch.get(i).getEndTime(); //没拆分前的完工时间           
	            nowSch.get(i).setEndTime(earlyEndTime);
	            int machId=nowSch.get(i).getMachId();
	            int orderId=nowSch.get(i).getOrderId();
	            Task midTk=new Task(machId,earlyEndTime,endTime,orderId);
	            midTk.setReleaseTime(nowSch.get(i).getReleaseTime());
	            midSchList.add(midTk);
	          }
	        }
	        //将nowSch中的任务加入最终任务列表
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }
	        //用拆分后的任务列表替换nowSch
	        nowSch=midSchList;    
	        periodNum++;
	      }
	      //当排产作业列表为空时，如果nowSch中还有其他的部分排产，则将其加入最终排产之后
	      if(sortOrders.isEmpty()){
	        notEnd=false;//如果作业列表中的作业全部排完，则结束循环
	        for(Task tk:nowSch){
	          if(tk.getEndTime()>tk.getStartTime()){//排序刚好开工时间等于前一时段结束时间的空调度
	            finalTasks.add(tk);
	          }
	        }        
	      }      
	      
	    }
	    //至此，全部任务都排入finalTasks中，下面将finalTasks中的数据转换为数组
	    int schRows=finalTasks.size();
	    double[][] schedule=new double[schRows][11];
	    for(int i=0;i<schRows;i++){
	      Task tk2=finalTasks.get(i);
	      schedule[i][0]=tk2.getOrderId()+1;
	      schedule[i][1]=tk2.getMachId()+1;
	      schedule[i][3]=tk2.getStartTime();
	      schedule[i][4]=tk2.getEndTime();
	      schedule[i][5]=tk2.getDueTime();      
	      schedule[i][6]=tk2.getReleaseTime();
	      schedule[i][10]=tk2.getPeriodNum();
	    }   
	    return schedule;
	    
	  }
	
	
	/**根据染色体种群、作业工时、交付时间和机器数量获得每个染色体对应调度方案的总延误时间
	   * 对应问题为Pm|Rj,Syn|Lmax
	   * @param chromes 染色体种群，编码从1开始到jobQty
	  * @param ptimes 作业时间
	  * @param dtimes 作业的交付时间
	  * @param rtimes 作业的释放时间
	  * @param machQty 并行机数量
	   * @return 全部染色体对应的最大延误时间长度一维数组
	   */
	  double[] getSynRjSumLateTimes(int[][] chromes,double[] ptimes, double[] dtimes,double[] rtimes, int machQty){
	      int pop=chromes.length;
	      int jobQty = ptimes.length;
	      double[] lateTimes = new double[pop];
	      //循环对每条染色体解码，并获得其最大延误时间，存入数组lateTimes
	      int[] nowSeq=new int[jobQty];
	      for(int i=0;i<pop;i++){
	        System.arraycopy(chromes[i], 0, nowSeq, 0, jobQty); //获得当前染色体
	        //对当前染色体解码
	        double[][] sch=getSynPMSch(ptimes, dtimes, nowSeq, machQty,rtimes);
	        double midLateTime=Schedule.getSumLateTime(sch);
	        lateTimes[i]=midLateTime;
	      }
	      return lateTimes;
	  }
	  
	   	
	
	
	/**根据染色体种群、作业工时、交付时间和机器数量获得每个染色体对应调度方案的最大完工时间
	   * 对应问题为Pm|Rj,Syn|Cmax
	   * @param chromes 染色体种群，编码从1开始到jobQty
	  * @param ptimes 作业时间
	  * @param rtimes 作业的释放时间
	  * @param machQty 并行机数量
	   * @return 全部染色体对应的最大延误时间长度一维数组
	   */
	  double[] getCmaxSynRjTimes(int[][] chromes,double[] ptimes, double[] rtimes, int machQty){
	      int pop=chromes.length;
	      int jobQty = ptimes.length;
	      double[] cmaxTimes = new double[pop];
	      //循环对每条染色体解码，并获得其最大完工时间，存入数组cmaxTimes
	      int[] nowSeq=new int[jobQty];
	      for(int i=0;i<pop;i++){
	        System.arraycopy(chromes[i], 0, nowSeq, 0, jobQty); //获得当前染色体
	        //对当前染色体解码
	        double[][] sch=getSynPMSch(ptimes, nowSeq, machQty,rtimes);
	        double midCmaxTime=Schedule.getMaxFinishTime(sch);
	        cmaxTimes[i]=midCmaxTime;

	      }
	      return cmaxTimes;
	  }	
	
	  /**根据染色体种群、作业工时、交付时间和机器数量获得每个染色体对应调度方案的最大延误时间
	   * 对应问题为Pm|Rj,Syn|Lmax
	   * @param chromes 染色体种群，编码从1开始到jobQty
	  * @param ptimes 作业时间
	  * @param dtimes 作业的交付时间
	  * @param rtimes 作业的释放时间
	  * @param machQty 并行机数量
	   * @return 全部染色体对应的最大延误时间长度一维数组
	   */
	  double[] getMaxSynRjLateTimes(int[][] chromes,double[] ptimes, double[] dtimes,double[] rtimes, int machQty){
	      int pop=chromes.length;
	      int jobQty = ptimes.length;
	      double[] lateTimes = new double[pop];
	      //循环对每条染色体解码，并获得其最大延误时间，存入数组lateTimes
	      int[] nowSeq=new int[jobQty];
	      for(int i=0;i<pop;i++){
	        System.arraycopy(chromes[i], 0, nowSeq, 0, jobQty); //获得当前染色体
	        //对当前染色体解码
	        double[][] sch=getSynPMSch(ptimes, dtimes, nowSeq, machQty,rtimes);
	        double midLateTime=Schedule.getMaxLateTime(sch);
	        lateTimes[i]=midLateTime;

	      }
	      return lateTimes;
	  }

	  	  
	  
	
  	

	  
	  /**利用交付时间优先进行单阶段同步启停并行机Pm|rj,syn|#问题的求解
	   * @param ptimes 作业时间
	   * @param machQty 并行机数量   
	   * @param dTimes 作业的交付时间
	   * @param rTimes 作业的释放时间 
	   * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	   *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	   *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	   *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	   *         其变量optSeries为空，因为是一次计算，没有进化过程。
	   */
	   public  SchOptResult optPMSSynByEDD(double[] ptimes,double[] dTimes,int machQty,double[] rTimes ){
	     SchOptResult optResult=new SchOptResult();      double[][] sch;
	     //先获得顺序，然后调用getSynPMSch方法即可
	     int jobQty=ptimes.length;
	     double[][] dueTimes=new double[jobQty][2];//0-交付时间，1-作业编号
	     for(int i=0;i<jobQty;i++){
	       dueTimes[i][0]=dTimes[i];
	       dueTimes[i][1]=i+1;
	     }
	     EasyMath.sortArray(dueTimes, new int[] {0});//按照释放时间排序
	     int[] seq=new int[jobQty];
	     for(int i=0;i<jobQty;i++){
	       seq[i]=(int)dueTimes[i][1];
	     }   
	     sch=getSynPMSch(ptimes,dTimes,seq,machQty,rTimes);
	     optResult.schedule=sch;
	     return optResult;
	   }  


	   
	   /**利用先来先服务规则进行单阶段同步启停并行机Pm|rj,syn|#问题的求解
	    * @param ptimes 作业时间
	    * @param machQty 并行机数量   
	    * @param dTimes 作业的交付时间
	    * @param rTimes 作业的释放时间 
	    * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	    *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	    *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	    *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	    *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	    *         其变量optSeries为空，因为是一次计算，没有进化过程。
	    */
	    public  SchOptResult optPMSSynByFCFS(double[] ptimes,double[] dTimes,int machQty,double[] rTimes ){
	      SchOptResult optResult=new SchOptResult();      double[][] sch;
	      //先获得顺序，然后调用getSynPMSch方法即可
	      int jobQty=ptimes.length;
	      double[][] arriveTimes=new double[jobQty][2];//0-达到时间，1-作业编号
	      for(int i=0;i<jobQty;i++){
	        arriveTimes[i][0]=rTimes[i];
	        arriveTimes[i][1]=i+1;
	      }
	      EasyMath.sortArray(arriveTimes, new int[] {0});//按照释放时间排序
	      int[] seq=new int[jobQty];
	      for(int i=0;i<jobQty;i++){
	        seq[i]=(int)arriveTimes[i][1];
	      }   
	      sch=getSynPMSch(ptimes,dTimes,seq,machQty,rTimes);
	      optResult.schedule=sch;
	      return optResult;
	    } 

	     /**利用先来先服务规则进行单阶段同步启停并行机Pm|rj,syn|#问题的求解
	    * @param ptimes 作业时间
	    * @param machQty 并行机数量   
	    * @param rTimes 作业的释放时间 
	    * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	    *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	    *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	    *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	    *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	    *         其变量optSeries为空，因为是一次计算，没有进化过程。
	    */
	    public  SchOptResult optPMSSynCmaxByFCFS(double[] ptimes,int machQty,double[] rTimes ){
	      SchOptResult optResult=new SchOptResult();      double[][] sch;
	      //先获得顺序，然后调用getSynPMSch方法即可
	      int jobQty=ptimes.length;
	      double[][] arriveTimes=new double[jobQty][2];//0-达到时间，1-作业编号
	      for(int i=0;i<jobQty;i++){
	        arriveTimes[i][0]=rTimes[i];
	        arriveTimes[i][1]=i+1;
	      }
	      EasyMath.sortArray(arriveTimes, new int[] {0});//按照释放时间排序
	      int[] seq=new int[jobQty];
	      for(int i=0;i<jobQty;i++){
	        seq[i]=(int)arriveTimes[i][1];
	      }   
	      sch=getSynPMSch(ptimes,seq,machQty,rTimes);
	      optResult.schedule=sch;
	      return optResult;
	    } 

	    
	    
	    /**利用遗传算法进行同步启停单阶段并行机Pm|rj,syn|Cmax问题的优化求解
	     * @param ptimes 作业时间
	     * @param rtimes 作业的释放时间
	     * @param machQty 并行机数量
	     * @param params 遗传算法的相关参数，0-迭代代数，1-种群规模，2-交叉概率，3-变异概率，5-无更新终止代数
	     * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	     *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	     *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	     *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	     *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	     *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	     */
	     public  SchOptResult optPMSSynRjCmaxByGA(double[] ptimes,double[] rtimes,int machQty,double[] params ){
	       SchOptResult optResult=new SchOptResult();      double[][] sch;
	       //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	       double[][] optSeries=new double[(int)params[0]][3];//
	       
	       //作业数量，也是代码长度
	       int codeLength=ptimes.length;   
	         //(1）遗传算法主程序   
	       //算法相关参数设定
	       int maxGeneration=(int)params[0],pop=(int)params[1],maxNoGoodLoop=(int)params[4];
	       int nowGeneration=0,noGoodLoop=0;
	       double crossRate=params[2],muteRate=params[3];
	       //（2）初始化种群，编码从1开始编起
	       int[][] chromes=GA.initSequence1Chrome(pop, codeLength);
	       double[] nowFit =getCmaxSynRjTimes(chromes,ptimes,rtimes,machQty);
	       double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	       //排序获得最优解和最优适应度函数值
	       int[] optSequence = new int[codeLength];//存储算法获得最优解是的作业排序
	       double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	       System.arraycopy(chromes[0], 0, optSequence, 0, codeLength);    
	       //算法循环主程序
	       while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	         //step1: 选择
	         chromes=GA.selectionElistMin(chromes, nowFit, 5);      
	         //step2: 交叉
	         chromes=GA.crossOX(chromes, crossRate);
	         //step3: 变异
	         chromes=GA.muteTwoPointReverse(chromes, muteRate);
	         //计算交叉和变异后的染色体对应的适应度值
	         nowFit =getCmaxSynRjTimes(chromes,ptimes,rtimes,machQty);           
	         for(int i=0;i<pop;i++){
	           fitnessArr[i][0]=nowFit[i];
	           fitnessArr[i][1]=i;
	         }
	         EasyMath.sortArray(fitnessArr, new int[] {0});
	         double nowOptFit=fitnessArr[0][0];
	         int nowOptId=(int) fitnessArr[0][1];
	         //step4: 更新全局解
	         
	         if(nowOptFit>=optFitness){
	           noGoodLoop++;
	         }else{
	           noGoodLoop=0;
	           optFitness=nowOptFit;
	           System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //复制最优染色体
	         }
	         optSeries[nowGeneration][0]=nowGeneration;
	         optSeries[nowGeneration][1]=optFitness;
	         optSeries[nowGeneration][2]=nowOptFit; 
	         nowGeneration++;
	       }
	       //对最优解生成详细调度并返回
	       sch=getSynPMSch(ptimes,optSequence, machQty,rtimes);
	       optResult.schedule=sch;
	       optResult.optSeries=optSeries;
	       return optResult;
	     }
	    
	     
	     /**利用粒子群算法进行同步启停单阶段并行机Pm|rj,syn|Cmax问题的优化求解
	      * @param ptimes 作业时间
	      * @param rtimes 作业的释放时间
	      * @param machQty 并行机数量  
	      * @param params 粒子群算法的相关参数，0-速度惯性系数，1-个体学习因子，2-社会学习因子，3-种群规模，4-迭代代数，5-无更新终止代数
	      * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	      *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	      *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	      *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	      *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	      *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	      */
	      public  SchOptResult optPMSSynRjCmaxByPSO(double[] ptimes,double[] rtimes,int machQty,double[] params ){
	        SchOptResult optResult=new SchOptResult(); 
	        double[][] sch;
	        //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	        double[][] optSeries=new double[(int)params[4]][3];//
	        
	        //作业数量，也是代码长度
	        int codeLength=ptimes.length;   
	          //(1）粒子群算法主程序   
	        //算法相关参数设定
	        int maxGeneration=(int)params[4],pop=(int)params[3],maxNoGoodLoop=(int)params[5];
	        int nowGeneration=0,noGoodLoop=0;
	        //（2）初始化种群，编码从0开始编起
	        double[][] X=PSO.initX(pop, codeLength);
	        double[][] V=PSO.initV(pop, codeLength); 
	        int[][] intXs=PSO.parseInt(X);
	        double[] nowFit =getCmaxSynRjTimes(intXs,ptimes,rtimes,machQty);
	        double[] singleBestFit=getCmaxSynRjTimes(intXs,ptimes,rtimes,machQty);//粒子截止当前最优解
	        double[][] singleBestFitX=X;//粒子截止当前最优解对应的变量值
	        int idx=PSO.getMinFitIdx(nowFit);
	        double groupBestFit=nowFit[idx];//种群截止当前最优解
	        double[] groupBestFitX=new double[codeLength];//种群截止当前最优解对应的变量值
	        System.arraycopy(X[idx], 0, groupBestFitX, 0, codeLength);     
	        //算法循环主程序
	        while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	          //step1: 更新速度
	          V=PSO.updateV(X, V, singleBestFitX, groupBestFitX, params);      
	          //step2: 更新变量值
	          X=PSO.updateX(X, V, singleBestFitX, groupBestFitX, params);  
	          //step3: 计算每个粒子的最好解和变量值
	          intXs=PSO.parseInt(X);
	          nowFit =getCmaxSynRjTimes(intXs,ptimes,rtimes,machQty); 
	          //依次判断是否为最好解
	          noGoodLoop++;
	          for(int i=0;i<pop;i++){
	            if(nowFit[i]<singleBestFit[i]){
	              singleBestFit[i]=nowFit[i];
	              System.arraycopy(X[i],0,singleBestFitX[i],0,codeLength);
	            }
	            if(nowFit[i]<groupBestFit){
	              groupBestFit=nowFit[i];
	              System.arraycopy(X[i],0,groupBestFitX,0,codeLength);
	              noGoodLoop=0;
	            }        
	          }
	          //排序获得当前粒子群最优解
	          int id=PSO.getMinFitIdx(nowFit);
	          optSeries[nowGeneration][0]=nowGeneration;
	          optSeries[nowGeneration][1]=groupBestFit;
	          optSeries[nowGeneration][2]=nowFit[id]; 
	          nowGeneration++;
	        }
	        //对最优解生成详细调度并返回
	        int[] intX=PSO.parseInt(groupBestFitX);
	        sch=getSynPMSch(ptimes,intX,machQty,rtimes);
	        optResult.schedule=sch;
	        optResult.optSeries=optSeries;
	        return optResult;
	        
	      }  
	     
	      
	      /**利用模拟退火算法进行同步启停单阶段并行机Pm|rj,syn|Cmax问题的优化求解
	       * @param ptimes 作业时间
	       * @param rtimes 作业的释放时间 
	       * @param machQty 并行机数量
	       * @param params 模拟退火算法的相关参数，0-降温速率，1-内循环次数，2-外循环次数，3-无更新终止代数
	       * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	       *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	       *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	       *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	       *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	       *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	       */
	       public  SchOptResult optPMSSynRjCmaxBySA(double[] ptimes,double[] rtimes,int machQty,double[] params ){
	         SchOptResult optResult=new SchOptResult(); 
	         double[][] sch;
	         //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	         double[][] optSeries=new double[(int)params[2]][3];//
	         
	         //算法计算过程中存储的数值
	         int codeLength=ptimes.length;
	         //随机初始化第一个解
	         int[] sequence1=EasyMath.randPermStart1(codeLength);//生出初始作业的调度顺序
	             //HmArray.printArray(sequence1);
	         double[][] schedule1=getSynPMSch(ptimes,sequence1,machQty,rtimes);

	         double fit1=Schedule.getMaxFinishTime(schedule1);
	         //存储最优解的类容
	         double optFit=fit1;//最优解
	         double inOptFit;//内循环的最优解   
	         int[] inOptSequence;//内循环最优解    
	         int[] optSequence=sequence1;
	         //定义一些用于存储中间解的变量
	         int[] sequence2;
	         double[][] schedule2;
	         double fit2;
	         double delta;  
	         
	           //(1）模拟退火主程序   
	         //算法相关参数设定
	         double temp=EasyMath.sum(ptimes),alpha=params[0];
	         int inLoopNum=(int)params[1],outStop=0,noGoodLoop=0;
	         int maxLoopNum=(int)params[2],nowLoop=0;
	         //算法循环主程序
	         while(outStop==0&&nowLoop<maxLoopNum){
	           inOptFit=fit1;
	           inOptSequence=Arrays.copyOf(sequence1, codeLength);       
	           for(int i=0;i<inLoopNum;i++){
	             sequence2=EasyMath.reverseArray(sequence1);//在既有最优解基础上通过逆序产生新解
	             schedule2=getSynPMSch(ptimes,  sequence2, machQty,rtimes);
	             fit2=Schedule.getMaxFinishTime(schedule2);        
	             delta=fit2-fit1;
	             if(delta<0){
	               sequence1=Arrays.copyOf(sequence2, codeLength);  
	               fit1=fit2;
	               if(inOptFit>fit1){
	                 inOptFit=fit1;
	                 inOptSequence=Arrays.copyOf(sequence2, codeLength);         
	               }
	             }else{   
	               if(Math.exp(-delta/temp)>Math.random()){
	                 sequence1=Arrays.copyOf(sequence2, codeLength);
	                 fit1=fit2;
	               }
	             }
	           }
	           temp=alpha*temp;//降温操作    

	           if(inOptFit>=optFit){
	             noGoodLoop++;
	           }else{
	             noGoodLoop=0;
	             optFit=inOptFit;
	             optSequence=Arrays.copyOf(inOptSequence, codeLength);
	           }
	           if(noGoodLoop==(int)params[3]){
	             outStop=1;
	           }
	           optSeries[nowLoop][0]=nowLoop;
	           optSeries[nowLoop][1]=optFit;
	           optSeries[nowLoop][2]=inOptFit;     
	           nowLoop++;
	         }
	         
	         //对最优解生成详细调度并返回
	         sch=getSynPMSch(ptimes, optSequence, machQty,rtimes);
	         optResult.schedule=sch;
	         optResult.optSeries=optSeries;
	         return optResult;
	         
	       }
	       
	      
	       /**利用教学算法进行同步启停单阶段并行机Pm|rj,syn|Cmax问题的优化求解
	        * @param ptimes 作业时间
	        * @param rtimes 作业的释放时间
	        * @param machQty 并行机数量   
	        * @param params 教学算法的相关参数，0-学生数量[种群规模]，1-迭代代数，2-无更新终止代数
	        * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	        *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	        *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	        *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	        *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	        *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	        */
	        public  SchOptResult optPMSSynRjCmaxByTLBO(double[] ptimes,double[] rtimes,int machQty,double[] params ){
	          SchOptResult optResult=new SchOptResult();      double[][] sch; 
	          //作业数量，也是代码长度
	          int jobQty=ptimes.length;   
	            //(1）蚁群算法主程序   
	          //算法相关参数设定
	          int maxGeneration=(int)params[1],pop=(int)params[0],maxNoGoodLoop=(int)params[2];
	          int nowGeneration=0,noGoodLoop=0;    
	          //（2）初始化班级科目成绩
	          double[][] scores=TLBO.initClass(pop, jobQty);
	          int[][] sequences=TLBO.getSequenceFromScores(scores);
	          double[] nowFit =getCmaxSynRjTimes(sequences,ptimes,rtimes,machQty);
	          double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	          //=======================排序获得最优解和最优适应度函数值
	          int[] optSequence = new int[jobQty];//存储算法获得最优解时的作业排序
	          double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	          System.arraycopy(sequences[0], 0, optSequence, 0, jobQty); 
	          
	          //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	          double[][] optSeries=new double[maxGeneration][3];//  
	          
	          double[] meanScores;//存储循环计算过程中的各门课程的平均成绩 
	          double[] bestScores;//存储循环计算过程中班级同学综合评价最好的同学的各科成绩     
	          //算法循环主程序
	          while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	            //step1: 找出综合评价最好的学生及其各科成绩、各个科目的平均得分
	            sequences=TLBO.getSequenceFromScores(scores);
	            nowFit =getCmaxSynRjTimes(sequences,ptimes,rtimes,machQty);   
	            for(int i=0;i<pop;i++){
	              fitnessArr[i][0]=nowFit[i];
	              fitnessArr[i][1]=i;
	            }
	            EasyMath.sortArray(fitnessArr, new int[] {0});  //排序，第一个解为最好解   
	            double nowOptFit=fitnessArr[0][0];
	            int nowOptId=(int) fitnessArr[0][1];      
	            meanScores=TLBO.getMeanScores(scores);
	            bestScores=Arrays.copyOf(scores[nowOptId],jobQty);    
	            //step2-1:进行全局最优解的处理，虽然逻辑流程将这个第二步放在最后，但是考虑到少计算一个最优解的过程，
	            //       所以将全局解更新放在这里
	          
	            if(nowOptFit>=optFitness){
	              noGoodLoop++;
	            }else{
	              noGoodLoop=0;
	              optFitness=nowOptFit;
	              System.arraycopy(sequences[nowOptId], 0, optSequence, 0, jobQty); 
	            }    
	            //step2-2:更新适应度曲线数据  
	            optSeries[nowGeneration][0]=nowGeneration;
	            optSeries[nowGeneration][1]=optFitness;
	            optSeries[nowGeneration][2]=nowOptFit; 

	            
	            //step3: 教师学习阶段，对每个学生的科目成绩进行更新  
	            double[][] newScores=TLBO.updateScores(scores, bestScores, meanScores);
	            int[][] newSeq=TLBO.getSequenceFromScores(newScores);
	            double[] newFits =getCmaxSynRjTimes(newSeq,ptimes,rtimes,machQty);      
	            for(int i=0;i<pop;i++){
	              if(newFits[i]<nowFit[i]){
	                scores[i]=Arrays.copyOf(newScores[i],jobQty);
	              }
	            }
	            //step4:学生学习阶段
	            int[] twoStudentIds=TLBO.getTwoDiffNum(pop);
	            double[][] twoScores=new double[2][jobQty];
	            for(int i=0;i<2;i++){//获得两个学生的科目成绩
	              twoScores[i]=Arrays.copyOf(scores[twoStudentIds[i]],jobQty);
	            }
	            int[][] twoSeq=TLBO.getSequenceFromScores(twoScores);
	            double[] twoFits =getCmaxSynRjTimes(twoSeq,ptimes,rtimes,machQty);  
	            boolean isgood=twoFits[0]<twoFits[1];
	            double[][] twoNewScores=TLBO.updateOneScore(twoScores, isgood);
	            int[][] twoNewSeq=TLBO.getSequenceFromScores(twoNewScores);
	            double[] twoNewFits =getCmaxSynRjTimes(twoNewSeq,ptimes,rtimes,machQty);
	            if(twoNewFits[0]<twoFits[0]){
	              scores[twoStudentIds[0]]=Arrays.copyOf(twoNewScores[0],jobQty);
	            }
	            //学生学习阶段结束
	            nowGeneration++;
	          }
	          //对最优解生成详细调度并返回
	          sch=getSynPMSch(ptimes,optSequence,machQty,rtimes);
	          optResult.schedule=sch;
	          optResult.optSeries=optSeries;
	          return optResult;
	          
	        }  
	        
	       
	        /**利用蚁群算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
	         * @param ptimes 作业时间
	         * @param dtimes 作业的交付时间
	         * @param rtimes 作业的释放时间 
	         * @param machQty 并行机数量 
	         * @param params 蚁群算法的相关参数，0-挥发系数，1-种群规模，2-迭代代数，3-无更新终止代数
	         * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	         *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	         *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	         *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	         *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	         *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	         */
	         public  SchOptResult optPMSSynRjLmaxByACO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params ){
	           SchOptResult optResult=new SchOptResult();      double[][] sch;
	           //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	           double[][] optSeries=new double[(int)params[2]][3];//
	           
	           //作业数量，也是代码长度
	           int codeLength=ptimes.length;   
	             //(1）蚁群算法主程序   
	           //算法相关参数设定
	           int maxGeneration=(int)params[2],pop=(int)params[1],maxNoGoodLoop=(int)params[3];
	           int nowGeneration=0,noGoodLoop=0;
	           double rho=params[0];
	           //（2）初始化种群，编码从1开始编起
	           double[][] pherom=ACO.initPheromone(ptimes);
	           int[][] chromes=ACO.createRoutes(pherom, pop);
	           double[] nowFit =getMaxSynRjLateTimes(chromes,ptimes,dtimes,rtimes,machQty);
	           double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	           //排序获得最优解和最优适应度函数值
	           int[] optSequence= new int[codeLength];//存储算法获得最优解是的作业排序
	           double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	           for(int i=0;i<codeLength;i++){
	             optSequence[i]=chromes[0][i];      
	           }    
	           //算法循环主程序
	           while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	             //step1: 产生新解
	             chromes=ACO.createRoutes(pherom, pop); 
	             //step2: 判断是否有更好的解
	             //计算新解对应的适应度值
	             nowFit =getMaxSynRjLateTimes(chromes,ptimes,dtimes,rtimes,machQty);           
	             for(int i=0;i<pop;i++){
	               fitnessArr[i][0]=nowFit[i];
	               fitnessArr[i][1]=i;
	             }
	             EasyMath.sortArray(fitnessArr, new int[] {0});     
	             double nowOptFit=fitnessArr[0][0];
	             int nowOptId=(int) fitnessArr[0][1];
	             //step3: 更新全局解      
	             if(nowOptFit>=optFitness){
	               noGoodLoop++;
	             }else{
	               noGoodLoop=0;
	               optFitness=nowOptFit;
	               System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //染色体编码是从0开始编的，但是排序从1开始编起
	             }
	             //step4:更新信息素
	             pherom=ACO.updatePheromone(pherom, optSequence, optFitness, rho);     
	             optSeries[nowGeneration][0]=nowGeneration;
	             optSeries[nowGeneration][1]=optFitness;
	             optSeries[nowGeneration][2]=nowOptFit; 
	             nowGeneration++;
	           }

	           //对最优解生成详细调度并返回
	           sch=getSynPMSch(ptimes,dtimes,optSequence, machQty,rtimes);
	           optResult.schedule=sch;
	           optResult.optSeries=optSeries;
	           return optResult;
	           
	         }  

	        
	         /**利用遗传算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
	          * @param ptimes 作业时间
	          * @param dtimes 作业的交付时间
	          * @param rtimes 作业的释放时间
	          * @param machQty 并行机数量
	          * @param params 遗传算法的相关参数，0-迭代代数，1-种群规模，2-交叉概率，3-变异概率，5-无更新终止代数
	          * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	          *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	          *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	          *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	          *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	          *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	          */
	          public  SchOptResult optPMSSynRjLmaxByGA(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params ){
	            SchOptResult optResult=new SchOptResult();      double[][] sch;
	            //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	            double[][] optSeries=new double[(int)params[0]][3];//
	            
	            //作业数量，也是代码长度
	            int codeLength=ptimes.length;   
	              //(1）遗传算法主程序   
	            //算法相关参数设定
	            int maxGeneration=(int)params[0],pop=(int)params[1],maxNoGoodLoop=(int)params[4];
	            int nowGeneration=0,noGoodLoop=0;
	            double crossRate=params[2],muteRate=params[3];
	            //（2）初始化种群，编码从1开始编起
	            int[][] chromes=GA.initSequence1Chrome(pop, codeLength);
	            double[] nowFit =getMaxSynRjLateTimes(chromes,ptimes,dtimes,rtimes,machQty);
	            double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	            //排序获得最优解和最优适应度函数值
	            int[] optSequence = new int[codeLength];//存储算法获得最优解是的作业排序
	            double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	            System.arraycopy(chromes[0], 0, optSequence, 0, codeLength);    
	            //算法循环主程序
	            while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	              //step1: 选择
	              chromes=GA.selectionElistMin(chromes, nowFit, 5);      
	              //step2: 交叉
	              chromes=GA.crossOX(chromes, crossRate);
	              //step3: 变异
	              chromes=GA.muteTwoPointReverse(chromes, muteRate);
	              //计算交叉和变异后的染色体对应的适应度值
	              nowFit =getMaxSynRjLateTimes(chromes,ptimes,dtimes,rtimes,machQty);           
	              for(int i=0;i<pop;i++){
	                fitnessArr[i][0]=nowFit[i];
	                fitnessArr[i][1]=i;
	              }
	              EasyMath.sortArray(fitnessArr, new int[] {0});
	              double nowOptFit=fitnessArr[0][0];
	              int nowOptId=(int) fitnessArr[0][1];
	              //step4: 更新全局解
	              
	              if(nowOptFit>=optFitness){
	                noGoodLoop++;
	              }else{
	                noGoodLoop=0;
	                optFitness=nowOptFit;
	                System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //复制最优染色体
	              }
	              optSeries[nowGeneration][0]=nowGeneration;
	              optSeries[nowGeneration][1]=optFitness;
	              optSeries[nowGeneration][2]=nowOptFit; 
	              nowGeneration++;
	            }
	            //对最优解生成详细调度并返回
	            sch=getSynPMSch(ptimes, dtimes, optSequence, machQty,rtimes);
	            optResult.schedule=sch;
	            optResult.optSeries=optSeries;
	            return optResult;
	            
	          }
	          
	         
	          /**利用粒子群算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
	           * @param ptimes 作业时间
	           * @param dtimes 作业的交付时间
	           * @param rtimes 作业的释放时间
	           * @param machQty 并行机数量  
	           * @param params 粒子群算法的相关参数，0-速度惯性系数，1-个体学习因子，2-社会学习因子，3-种群规模，4-迭代代数，5-无更新终止代数
	           * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	           *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	           *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	           *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	           *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	           *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	           */
	           public  SchOptResult optPMSSynRjLmaxByPSO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params ){
	             SchOptResult optResult=new SchOptResult();      double[][] sch;
	             //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	             double[][] optSeries=new double[(int)params[4]][3];//
	             
	             //作业数量，也是代码长度
	             int codeLength=ptimes.length;   
	               //(1）粒子群算法主程序   
	             //算法相关参数设定
	             int maxGeneration=(int)params[4],pop=(int)params[3],maxNoGoodLoop=(int)params[5];
	             int nowGeneration=0,noGoodLoop=0;
	             //（2）初始化种群，编码从0开始编起
	             double[][] X=PSO.initX(pop, codeLength);
	             double[][] V=PSO.initV(pop, codeLength); 
	             int[][] intXs=PSO.parseInt(X);
	             double[] nowFit =getMaxSynRjLateTimes(intXs,ptimes,dtimes,rtimes,machQty);
	             double[] singleBestFit=getMaxSynRjLateTimes(intXs,ptimes,dtimes,rtimes,machQty);//粒子截止当前最优解
	             double[][] singleBestFitX=X;//粒子截止当前最优解对应的变量值
	             int idx=PSO.getMinFitIdx(nowFit);
	             double groupBestFit=nowFit[idx];//种群截止当前最优解
	             double[] groupBestFitX=new double[codeLength];//种群截止当前最优解对应的变量值
	             System.arraycopy(X[idx], 0, groupBestFitX, 0, codeLength);     
	             //算法循环主程序
	             while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	               //step1: 更新速度
	               V=PSO.updateV(X, V, singleBestFitX, groupBestFitX, params);      
	               //step2: 更新变量值
	               X=PSO.updateX(X, V, singleBestFitX, groupBestFitX, params);  
	               //step3: 计算每个粒子的最好解和变量值
	               intXs=PSO.parseInt(X);
	               nowFit =getMaxSynRjLateTimes(intXs,ptimes,dtimes,rtimes,machQty); 
	               //依次判断是否为最好解
	               noGoodLoop++;
	               for(int i=0;i<pop;i++){
	                 if(nowFit[i]<singleBestFit[i]){
	                   singleBestFit[i]=nowFit[i];
	                   System.arraycopy(X[i],0,singleBestFitX[i],0,codeLength);
	                 }
	                 if(nowFit[i]<groupBestFit){
	                   groupBestFit=nowFit[i];
	                   System.arraycopy(X[i],0,groupBestFitX,0,codeLength);
	                   noGoodLoop=0;
	                 }        
	               }
	               //排序获得当前粒子群最优解
	               int id=PSO.getMinFitIdx(nowFit);
	               optSeries[nowGeneration][0]=nowGeneration;
	               optSeries[nowGeneration][1]=groupBestFit;
	               optSeries[nowGeneration][2]=nowFit[id]; 
	               nowGeneration++;
	             }
	             //对最优解生成详细调度并返回
	             int[] intX=PSO.parseInt(groupBestFitX);
	             sch=getSynPMSch(ptimes,dtimes,intX,machQty,rtimes);
	             optResult.schedule=sch;
	             optResult.optSeries=optSeries;
	             return optResult;
	             
	           }  

	          
	           
	           /**利用模拟退火算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
	            * @param ptimes 作业时间
	            * @param dtimes 作业的交付时间
	            * @param rtimes 作业的释放时间 
	            * @param machQty 并行机数量
	            * @param params 模拟退火算法的相关参数，0-降温速率，1-内循环次数，2-外循环次数，3-无更新终止代数
	            * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	            *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	            *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	            *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	            *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	            *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	            */
	            public  SchOptResult optPMSSynRjLmaxBySA(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params ){
	              SchOptResult optResult=new SchOptResult();      double[][] sch;
	              //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	              double[][] optSeries=new double[(int)params[2]][3];//
	              
	              //算法计算过程中存储的数值
	              int codeLength=ptimes.length;
	              //随机初始化第一个解
	              int[] sequence1=EasyMath.randPermStart1(codeLength);//生出初始作业的调度顺序

	              double[][] schedule1=getSynPMSch(ptimes,dtimes,sequence1,machQty,rtimes);
	              double fit1=Schedule.getMaxLateTime(schedule1);
	              //存储最优解的类容
	              double optFit=fit1;//最优解
	              double inOptFit;//内循环的最优解   
	              int[] inOptSequence;//内循环最优解    
	              int[] optSequence=sequence1;
	              //定义一些用于存储中间解的变量
	              int[] sequence2;
	              double[][] schedule2;
	              double fit2;
	              double delta;  
	              
	                //(1）模拟退火主程序   
	              //算法相关参数设定
	              double temp=EasyMath.sum(ptimes),alpha=params[0];
	              int inLoopNum=(int)params[1],outStop=0,noGoodLoop=0;
	              int maxLoopNum=(int)params[2],nowLoop=0;
	              //算法循环主程序
	              while(outStop==0&&nowLoop<maxLoopNum){
	                inOptFit=fit1;
	                inOptSequence=Arrays.copyOf(sequence1, codeLength);       
	                for(int i=0;i<inLoopNum;i++){
	                  sequence2=EasyMath.reverseArray(sequence1);//在既有最优解基础上通过逆序产生新解
	                  schedule2=getSynPMSch(ptimes, dtimes, sequence2, machQty,rtimes);
	                  fit2=Schedule.getMaxLateTime(schedule2);        
	                  delta=fit2-fit1;
	                  if(delta<0){
	                    sequence1=Arrays.copyOf(sequence2, codeLength);  
	                    fit1=fit2;
	                    if(inOptFit>fit1){
	                      inOptFit=fit1;
	                      inOptSequence=Arrays.copyOf(sequence2, codeLength);         
	                    }
	                  }else{   
	                    if(Math.exp(-delta/temp)>Math.random()){
	                      sequence1=Arrays.copyOf(sequence2, codeLength);
	                      fit1=fit2;
	                    }
	                  }
	                }
	                temp=alpha*temp;//降温操作    

	                if(inOptFit>=optFit){
	                  noGoodLoop++;
	                }else{
	                  noGoodLoop=0;
	                  optFit=inOptFit;
	                  optSequence=Arrays.copyOf(inOptSequence, codeLength);
	                }
	                if(noGoodLoop==(int)params[3]){
	                  outStop=1;
	                }
	                optSeries[nowLoop][0]=nowLoop;
	                optSeries[nowLoop][1]=optFit;
	                optSeries[nowLoop][2]=inOptFit;     
	                nowLoop++;
	              }    
	              //对最优解生成详细调度并返回
	              sch=getSynPMSch(ptimes, dtimes, optSequence, machQty,rtimes);
	              optResult.schedule=sch;
	              optResult.optSeries=optSeries;
	              return optResult;
	              
	            }

	           
	            /**利用教学算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
	             * @param ptimes 作业时间
	             * @param dtimes 作业的交付时间
	             * @param rtimes 作业的释放时间
	             * @param machQty 并行机数量   
	             * @param params 教学算法的相关参数，0-学生数量[种群规模]，1-迭代代数，2-无更新终止代数
	             * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	             *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	             *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	             *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	             *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	             *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	             */
	             public  SchOptResult optPMSSynRjLmaxByTLBO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params ){
	               SchOptResult optResult=new SchOptResult();      double[][] sch; 
	               //作业数量，也是代码长度
	               int jobQty=ptimes.length;   
	                 //(1）蚁群算法主程序   
	               //算法相关参数设定
	               int maxGeneration=(int)params[1],pop=(int)params[0],maxNoGoodLoop=(int)params[2];
	               int nowGeneration=0,noGoodLoop=0;    
	               //（2）初始化班级科目成绩
	               double[][] scores=TLBO.initClass(pop, jobQty);
	               int[][] sequences=TLBO.getSequenceFromScores(scores);
	               double[] nowFit =getMaxSynRjLateTimes(sequences,ptimes,dtimes,rtimes,machQty);
	               double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	               //=======================排序获得最优解和最优适应度函数值
	               int[] optSequence = new int[jobQty];//存储算法获得最优解时的作业排序
	               double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	               System.arraycopy(sequences[0], 0, optSequence, 0, jobQty); 
	               
	               //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	               double[][] optSeries=new double[maxGeneration][3];//  
	               
	               double[] meanScores;//存储循环计算过程中的各门课程的平均成绩 
	               double[] bestScores;//存储循环计算过程中班级同学综合评价最好的同学的各科成绩     
	               //算法循环主程序
	               while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	                 //step1: 找出综合评价最好的学生及其各科成绩、各个科目的平均得分
	                 sequences=TLBO.getSequenceFromScores(scores);
	                 nowFit =getMaxSynRjLateTimes(sequences,ptimes,dtimes,rtimes,machQty);   
	                 for(int i=0;i<pop;i++){
	                   fitnessArr[i][0]=nowFit[i];
	                   fitnessArr[i][1]=i;
	                 }
	                 EasyMath.sortArray(fitnessArr, new int[] {0});  //排序，第一个解为最好解   
	                 double nowOptFit=fitnessArr[0][0];
	                 int nowOptId=(int) fitnessArr[0][1];      
	                 meanScores=TLBO.getMeanScores(scores);
	                 bestScores=Arrays.copyOf(scores[nowOptId],jobQty);    
	                 //step2-1:进行全局最优解的处理，虽然逻辑流程将这个第二步放在最后，但是考虑到少计算一个最优解的过程，
	                 //       所以将全局解更新放在这里
	               
	                 if(nowOptFit>=optFitness){
	                   noGoodLoop++;
	                 }else{
	                   noGoodLoop=0;
	                   optFitness=nowOptFit;
	                   System.arraycopy(sequences[nowOptId], 0, optSequence, 0, jobQty); 
	                 }    
	                 //step2-2:更新适应度曲线数据  
	                 optSeries[nowGeneration][0]=nowGeneration;
	                 optSeries[nowGeneration][1]=optFitness;
	                 optSeries[nowGeneration][2]=nowOptFit; 

	                 
	                 //step3: 教师学习阶段，对每个学生的科目成绩进行更新  
	                 double[][] newScores=TLBO.updateScores(scores, bestScores, meanScores);
	                 int[][] newSeq=TLBO.getSequenceFromScores(newScores);
	                 double[] newFits =getMaxSynRjLateTimes(newSeq,ptimes,dtimes,rtimes,machQty);      
	                 for(int i=0;i<pop;i++){
	                   if(newFits[i]<nowFit[i]){
	                     scores[i]=Arrays.copyOf(newScores[i],jobQty);
	                   }
	                 }
	                 //step4:学生学习阶段
	                 int[] twoStudentIds=TLBO.getTwoDiffNum(pop);
	                 double[][] twoScores=new double[2][jobQty];
	                 for(int i=0;i<2;i++){//获得两个学生的科目成绩
	                   twoScores[i]=Arrays.copyOf(scores[twoStudentIds[i]],jobQty);
	                 }
	                 int[][] twoSeq=TLBO.getSequenceFromScores(twoScores);
	                 double[] twoFits =getMaxSynRjLateTimes(twoSeq,ptimes,dtimes,rtimes,machQty);  
	                 boolean isgood=twoFits[0]<twoFits[1];
	                 double[][] twoNewScores=TLBO.updateOneScore(twoScores, isgood);
	                 int[][] twoNewSeq=TLBO.getSequenceFromScores(twoNewScores);
	                 double[] twoNewFits =getMaxSynRjLateTimes(twoNewSeq,ptimes,dtimes,rtimes,machQty);
	                 if(twoNewFits[0]<twoFits[0]){
	                   scores[twoStudentIds[0]]=Arrays.copyOf(twoNewScores[0],jobQty);
	                 }
	                 //学生学习阶段结束
	                 nowGeneration++;
	               }
	               //对最优解生成详细调度并返回
	               sch=getSynPMSch(ptimes,dtimes,optSequence,machQty,rtimes);
	               optResult.schedule=sch;
	               optResult.optSeries=optSeries;
	               return optResult;
	               
	             }  
	            
	            	            
	         
	    
	    /**利用蚁群算法进行同步启停单阶段并行机Pm|rj,syn|Cmax问题的优化求解
	     * @param ptimes 作业时间
	     * @param rtimes 作业的释放时间 
	     * @param machQty 并行机数量 
	     * @param params 蚁群算法的相关参数，0-挥发系数，1-种群规模，2-迭代代数，3-无更新终止代数
	     * @return 返回优化计算的调度结果对象SchOptResult，其变量schedule为具体调度方案的二维数组，
	     *         共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
	     *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，
	     *         第八列：调整开始时间，第九列：调整结束时间，第十列：调整时长，
	     *         第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】；
	     *         其变量optSeries为迭代收敛曲线，第一列为进化代数，第二列为迄今为止最优目标函数值，第三列为当前代的最优解
	     */
	     public  SchOptResult optPMSSynRjCmaxByACO(double[] ptimes,double[] rtimes,int machQty,double[] params ){
	       SchOptResult optResult=new SchOptResult();      double[][] sch;
	       //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
	       double[][] optSeries=new double[(int)params[2]][3];//
	       
	       //作业数量，也是代码长度
	       int codeLength=ptimes.length;   
	         //(1）蚁群算法主程序   
	       //算法相关参数设定
	       int maxGeneration=(int)params[2],pop=(int)params[1],maxNoGoodLoop=(int)params[3];
	       int nowGeneration=0,noGoodLoop=0;
	       double rho=params[0];
	       //（2）初始化种群，编码从1开始编起
	       double[][] pherom=ACO.initPheromone(ptimes);
	       int[][] chromes=ACO.createRoutes(pherom, pop);
	       double[] nowFit =getCmaxSynRjTimes(chromes,ptimes,rtimes,machQty);
	       double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
	       //排序获得最优解和最优适应度函数值
	       int[] optSequence= new int[codeLength];//存储算法获得最优解是的作业排序
	       double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
	       for(int i=0;i<codeLength;i++){
	         optSequence[i]=chromes[0][i];      
	       }    
	       //算法循环主程序
	       while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
	         //step1: 产生新解
	         chromes=ACO.createRoutes(pherom, pop); 
	         //step2: 判断是否有更好的解
	         //计算新解对应的适应度值
	         nowFit =getCmaxSynRjTimes(chromes,ptimes,rtimes,machQty);           
	         for(int i=0;i<pop;i++){
	           fitnessArr[i][0]=nowFit[i];
	           fitnessArr[i][1]=i;
	         }
	         EasyMath.sortArray(fitnessArr, new int[] {0});     
	         double nowOptFit=fitnessArr[0][0];
	         int nowOptId=(int) fitnessArr[0][1];
	         //step3: 更新全局解      
	         if(nowOptFit>=optFitness){
	           noGoodLoop++;
	         }else{
	           noGoodLoop=0;
	           optFitness=nowOptFit;
	           System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //染色体编码是从0开始编的，但是排序从1开始编起
	         }
	         //step4:更新信息素
	         pherom=ACO.updatePheromone(pherom, optSequence, optFitness, rho);     
	         optSeries[nowGeneration][0]=nowGeneration;
	         optSeries[nowGeneration][1]=optFitness;
	         optSeries[nowGeneration][2]=nowOptFit; 
	         nowGeneration++;
	       }
	       //对最优解生成详细调度并返回
	       sch=getSynPMSch(ptimes,optSequence, machQty,rtimes);
	       optResult.schedule=sch;
	       optResult.optSeries=optSeries;
	       return optResult;
	       
	     }  
	    	    
  /**利用第二类迭代贪婪算法[破坏和重构两个步骤，破坏过程提取随机d个作业，然后将d个作业按照NEH的方式再进行解的重构]进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG1a(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
//       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       boolean improved=true;
       //贪婪迭代主循环
       while(improved){
    	   improved=false;
    	   ArrayList<Order> insertList=new ArrayList<>();//待插入订单列表-d个破坏提取出来的订单
    	   ArrayList<Order> remainList=new ArrayList<>();//剩余订单列表
    	   remainList=copyOrderList(optOrderList);
    	   for(int i=0;i<params[1];i++){//构建破坏订单集合和剩余订单列表集合
    		   int remainQty=jobQty-i;
    		   int midIdx=(int) (Math.random()*remainQty);
    		   insertList.add(remainList.get(midIdx));
    		   remainList.remove(midIdx);
    	   }
    	   //依次将破坏订单集合中的订单插入到剩余订单列表中的合适位置
    	   for(int i=0;i<params[1];i++){
    		   Order nowOr=insertList.get(i);
    		   //将各个订单的原始订单序号记录下来,并将订单列表中的订单编号修改为一定的顺序码,以便后续计算适应度函数值
    		   int remainQty=remainList.size();
    		   int[] orderIds=new int[remainQty+1];
    		   for(int j=0;j<remainQty;j++){
    			   orderIds[j]=remainList.get(j).getOrderId();
    			   remainList.get(j).setOrderId(j);
    		   }
    		   orderIds[remainQty]=nowOr.orderId;
    		   nowOr.setOrderId(remainQty);
    		   //执行当前订单插入最优位置搜索
    		   ArrayList<Order> midOptList=new ArrayList<>();//用于存储最优订单列表排序
    		   double midOptFit=0;
    		   for(int j=0;j<=remainQty;j++){
    			   ArrayList<Order> midList=new ArrayList<>();//用于存储插入不同位置后的订单列表
    			   midList=copyOrderList(remainList);
    			   midList.add(j, nowOr);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList=copyOrderList(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList=copyOrderList(midList);
        	    	   }
        	       }     			   
    		   }
    		   //进行订单编码的还原
    		   for(int j=0;j<=remainQty;j++){
    			   int idx=midOptList.get(j).orderId;
    			   midOptList.get(j).orderId=orderIds[idx];
    		   }
    		   //将当前订单插入后的最好排序订单列表替换为剩余订单列表
    		   remainList=copyOrderList(midOptList);
    	   }
    	   //计算重新构建的订单列表的总完工时间，查看是否比最优解小，以便替换最优解
	       double[][] nowSch=getSynPMSch(remainList, machQty);
	       double nowFit=Schedule.getSumLateTime(nowSch); 
	       //System.out.println("nowFit: "+nowFit+"  optFit: "+optFit);
	       if(nowFit<optFit){
	    	   optFit=nowFit;
	    	   optOrderList=copyOrderList(remainList);
	    	   improved=true;
	       }	       
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }
  

  /**利用第二类迭代贪婪算法[破坏和重构两个步骤，破坏过程提取随机d个作业，然后将d个作业按照NEH的方式再进行解的重构]进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 【模拟退火思想：重构后的新解如果不比原有最优解好，则按照一定概率接受其作为新的邻域】
   * 
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG1b(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;
       double temp=optFit;
       double belta=0.99;
       //Step3：运用迭代贪婪算法寻优       
       int gen=0;
       //贪婪迭代主循环
       while(gen<params[0]){
    	   ArrayList<Order> insertList=new ArrayList<>();//待插入订单列表-d个破坏提取出来的订单
    	   ArrayList<Order> remainList=new ArrayList<>();//剩余订单列表
    	   remainList=copyOrderList(orderList);
    	   for(int i=0;i<params[1];i++){//构建破坏订单集合和剩余订单列表集合
    		   int remainQty=jobQty-i;
    		   int midIdx=(int) (Math.random()*remainQty);
    		   insertList.add(remainList.get(midIdx));
    		   remainList.remove(midIdx);
    	   }
    	   //依次将破坏订单集合中的订单插入到剩余订单列表中的合适位置
    	   for(int i=0;i<params[1];i++){
    		   Order nowOr=insertList.get(i);
    		   //将各个订单的原始订单序号记录下来,并将订单列表中的订单编号修改为一定的顺序码,以便后续计算适应度函数值
    		   int remainQty=remainList.size();
    		   int[] orderIds=new int[remainQty+1];
    		   for(int j=0;j<remainQty;j++){
    			   orderIds[j]=remainList.get(j).getOrderId();
    			   remainList.get(j).setOrderId(j);
    		   }
    		   orderIds[remainQty]=nowOr.orderId;
    		   nowOr.setOrderId(remainQty);
    		   //执行当前订单插入最优位置搜索
    		   ArrayList<Order> midOptList=new ArrayList<>();//用于存储最优订单列表排序
    		   double midOptFit=0;
    		   for(int j=0;j<=remainQty;j++){
    			   ArrayList<Order> midList=new ArrayList<>();//用于存储插入不同位置后的订单列表
    			   midList=copyOrderList(remainList);
    			   midList.add(j, nowOr);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList=copyOrderList(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList=copyOrderList(midList);
        	    	   }
        	       }     			   
    		   }
    		   //进行订单编码的还原
    		   for(int j=0;j<=remainQty;j++){
    			   int idx=midOptList.get(j).orderId;
    			   midOptList.get(j).orderId=orderIds[idx];
    		   }
    		   //将当前订单插入后的最好排序订单列表替换为剩余订单列表
    		   remainList=copyOrderList(midOptList);
    	   }
    	   //计算重新构建的订单列表的总完工时间，查看是否比最优解小，以便替换最优解
	       double[][] newSch=getSynPMSch(remainList, machQty);
	       double newFit=Schedule.getSumLateTime(newSch); 
	       //System.out.println("nowFit: "+nowFit+"  optFit: "+optFit);
	       if(newFit<=optFit){
	    	   optFit=newFit;
	    	   optOrderList=copyOrderList(remainList);
	    	   if(newFit<optFit){
	    		   gen=0;
	    	   }
	       }
	       if(newFit<=nowFit){
	    	   nowFit=newFit;
	    	   orderList=copyOrderList(remainList);
//	    	   if(newFit<nowFit){
//	    		   gen=0;
//	    	   }
	       }else{
				  if(Math.random()<Math.exp((nowFit-newFit)/temp)*0.5){
					   nowFit=newFit;
					   orderList=copyOrderList(remainList);		
				  } 	    	   
	       }
	       temp=temp*belta;
	       gen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }
  
  
  /**利用第一类【即每个循环迭代过程中，随机将每个位置的作业提取出来再插入到n个不同位置搜索邻域】迭代贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG2a(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       //Step3：运用迭代贪婪算法寻优       
       
       boolean improved=true;
       //贪婪迭代主循环
       while(improved){
    	   improved=false;
    	   orderList.clear();
    	   orderList=copyOrderList(optOrderList);
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
    	   for(int i=0;i<params[1];i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
    		   double midOptFit=0;
    		   for(int j=0;j<jobQty;j++){
        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
        		   midList=copyOrderList(midOrderList);
        		   midList.add(j, or);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList=copyOrderList(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList.clear();
        	    		   midOptList=copyOrderList(midList);
        	    	   }
        	       }   			   
    		   }
    		   //System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit);
    		   if(midOptFit<optFit){
    			   optFit=midOptFit;
    			   optOrderList=copyOrderList(midOptList);
    			   improved=true;
    		   }   		   
    	   }
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  

 
  
  /**利用第一类【即每个循环迭代过程中，将每个位置的作业提取出来再插入到n个不同位置搜索邻域】迭代贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 【基于模拟退火思想，在迭代过程中按照概率接受临时劣解】
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG2b(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,double[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;//迭代循环中的当前解目标函数值
//       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       double temp=optFit;
       double gamma=params[2];
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=(int) params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){  	   
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
		   double midOptFit=-10;    	   
    	   for(int i=0;i<params[1];i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   for(int j=0;j<jobQty;j++){
    			   if(j!=posId){
	        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
	        		   midList=copyOrderList(midOrderList);
	        		   midList.add(j, or);
	        	       double[][] midSch=getSynPMSch(midList, machQty);
	        	       double midFit=Schedule.getSumLateTime(midSch); 
	        	       if(midOptFit==-10){
	        	    	   midOptFit=midFit;
	        	    	   midOptList=copyOrderList(midList);
	        	       }else{
	        	    	   if(midFit<midOptFit){
	        	    		   midOptFit=midFit;
	        	    		   midOptList=copyOrderList(midList);
	        	    	   }
	        	       }  
    			   }
    		   }  		   
    	   }//d个作业单点插入结束，获得单点插入后的最好解
		   //System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit);
		   if(midOptFit<optFit){//全局解的更新
			   optFit=midOptFit;
			   optOrderList=copyOrderList(midOptList);	 
			   nowGen=0;
		   } 
		   if(midOptFit<=nowFit){//局部解的更新
			   nowFit=midOptFit;
			   orderList=copyOrderList(midOptList);		   
		   }else{
			  if(Math.random()<Math.exp((nowFit-midOptFit)/temp)*0.5){
				   nowFit=midOptFit;
				   orderList=copyOrderList(midOptList);						  
			  } 
		   }
		   temp=temp*gamma;//温度更新
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  

  /**利用第一类【即每个循环迭代过程中，随机将每个位置的作业提取出来再插入到n个不同位置搜索邻域】迭代贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG3a(double[] ptimes,double[] rtimes,int machQty,double[] dtimes){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       //Step3：运用迭代贪婪算法寻优       
       
       boolean improved=true;
       //贪婪迭代主循环
       while(improved){
    	   improved=false;
    	   orderList=copyOrderList(optOrderList);
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
//    	   EasyArray.printArray(randSeq);
    	   for(int i=0;i<jobQty;i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
    		   double midOptFit=0;
    		   for(int j=0;j<jobQty;j++){
        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
        		   midList=copyOrderList(midOrderList);
        		   midList.add(j, or);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList=copyOrderList(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList=copyOrderList(midList);
        	    	   }
        	       }   			   
    		   }
    		   //System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit);
    		   if(midOptFit<optFit){
    			   optFit=midOptFit;
    			   optOrderList=copyOrderList(midOptList);
    			   improved=true;
    		   }   		   
    	   }
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  
   
  /**利用第一类【即每个循环迭代过程中，将每个位置的作业提取出来再插入到n个不同位置搜索邻域】迭代贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 【基于模拟退火思想，在迭代过程中按照概率接受临时劣解】
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-降温系数
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG3b(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,double[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;//迭代循环中的当前解目标函数值
//       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       double temp=optFit;
       double gamma=params[1];
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=(int) params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){  	   
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
  		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
  		   double midOptFit=-10;    	   
    	   for(int i=0;i<jobQty;i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   for(int j=0;j<jobQty;j++){
    			   if(j!=posId){
	        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
	        		   midList=copyOrderList(midOrderList);
	        		   midList.add(j, or);
	        	       double[][] midSch=getSynPMSch(midList, machQty);
	        	       double midFit=Schedule.getSumLateTime(midSch); 
	        	       if(midOptFit==-10){
	        	    	   midOptFit=midFit;
	        	    	   midOptList=copyOrderList(midList);
	        	       }else{
	        	    	   if(midFit<midOptFit){
	        	    		   midOptFit=midFit;
	        	    		   midOptList=copyOrderList(midList);
	        	    	   }
	        	       }  
    			   }
    		   }  		   
    	   }//d个作业单点插入结束，获得单点插入后的最好解
//		   System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit+"  nowFit: "+ nowFit);
  		   if(midOptFit<optFit){//全局解的更新
  			   optFit=midOptFit;
  			   optOrderList=copyOrderList(midOptList);
  			   nowGen=0;
  		   } 
  		   if(midOptFit<=nowFit){//局部解的更新
    			   nowFit=midOptFit;
    			   orderList=copyOrderList(midOptList);		   
  		   }else{
    			  if(Math.random()<Math.exp((nowFit-midOptFit)/temp)*0.5){
    				   nowFit=midOptFit;// nowFit increases
    				   orderList=copyOrderList(midOptList);						  
    			  } 
  		   }
		   temp=temp*gamma;//温度更新
//		   System.out.println("temp: "+temp+"  nowGen: "+nowGen+"  nowFit: "+ nowFit+"  midOptFit: "+ midOptFit+"  optFit: "+optFit);
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  
 
  
  /**利用第一类【即每个循环迭代过程中，将每个位置的作业提取出来再插入到n个不同位置搜索邻域】迭代贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 【基于模拟退火思想，在迭代过程中按照概率接受临时劣解】
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG3c(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;//迭代循环中的当前解目标函数值
//       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       double temp=optFit;
       double gamma=0.99;
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){  	   
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
		   double midOptFit=-10;    	   
    	   for(int i=0;i<jobQty;i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   for(int j=0;j<jobQty;j++){
    			   if(j!=posId){
	        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
	        		   midList=copyOrderList(midOrderList);
	        		   midList.add(j, or);
	        	       double[][] midSch=getSynPMSch(midList, machQty);
	        	       double midFit=Schedule.getSumLateTime(midSch); 
	        	       if(midOptFit==-10){
	        	    	   midOptFit=midFit;
	        	    	   midOptList=copyOrderList(midList);
	        	       }else{
	        	    	   if(midFit<midOptFit){
	        	    		   midOptFit=midFit;
	        	    		   midOptList=copyOrderList(midList);
	        	    	   }
	        	       }  
    			   }
    		   }  		   
    	   }//d个作业单点插入结束，获得单点插入后的最好解
		   //System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit);
		   if(midOptFit<=optFit){//全局解的更新
			   optFit=midOptFit;
			   optOrderList=copyOrderList(midOptList);
			   if(midOptFit<optFit){
			     nowGen=0;
			   }
		   } 
		   if(midOptFit<=nowFit){//局部解的更新
			   nowFit=midOptFit;
			   orderList=copyOrderList(midOptList);		   
		   }else{
			  if(Math.random()<Math.exp((nowFit-midOptFit)/temp)-0.2){
				   nowFit=midOptFit;
				   orderList=copyOrderList(midOptList);						  
			  }
   		   if(Math.random()<0.5){
    		   nowFit=optFit;
    		   orderList=copyOrderList(optOrderList);        			   
		   } 			  
		   }
		   temp=temp*gamma;//温度更新
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  
  
  
  /**利用第四类迭代贪婪算法【两点互换，无更新则终止】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 

   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
 * @throws CloneNotSupportedException 深度复制对象列表出现不支持的异常
   */
  public double[][] optSynRjLsumByIG4a(double[] ptimes,double[] rtimes,int machQty,double[] dtimes) throws CloneNotSupportedException{
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);      
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
//       System.out.println("start: "+ optFit);
	   //生成两点对的集合
	   //生成两点对的集合
	   int[][] pairs0=EasyMath.combin2(jobQty);  

	   int row=pairs0.length;  
       //Step3：运用迭代贪婪算法寻优       
       boolean improved=true;
       //贪婪迭代主循环
       while(improved){
    	  // System.out.println("loop...    "+optFit);
    	   improved=false;
    	   //点对互换，获取最优的解
    	   int[] seq=EasyMath.randPerm(jobQty);
    	   int[][] pairs=new int[row][2];
    	   for(int i=0;i<row;i++){
    		   pairs[i][0]=seq[pairs0[i][0]];
    		   pairs[i][1]=seq[pairs0[i][1]];
    	   }    
    	   //点对互换，获取最优的解
    	   ArrayList<Order> midOptList=new ArrayList<>();//本次迭代的最优解列表
    	   double midOptFit=-1;
    	   for(int i=0;i<row;i++){
    		   ArrayList<Order> midOrderList=new ArrayList<>();//本次成对互换列表
    		   midOrderList=copyOrderList(optOrderList);
    		   int[] nowPair=pairs[i];
    		   Order order1=(Order) midOrderList.get(nowPair[0]).clone();
    		   Order order2=(Order) midOrderList.get(nowPair[1]).clone();
    		   midOrderList.remove(nowPair[0]);
    		   midOrderList.add(nowPair[0],order2);
    		   midOrderList.remove(nowPair[1]);
    		   midOrderList.add(nowPair[1],order1);   
    	       double[][] midSch=getSynPMSch(midOrderList, machQty);
    	       double midFit=Schedule.getSumLateTime(midSch); 
    	       if(midOptFit==-1){   	    	   
    	    	   midOptList=copyOrderList(midOrderList);
    	    	   midOptFit=midFit;
    	       }else{
    	    	   if(midFit<midOptFit){
        	    	   midOptList=copyOrderList(midOrderList);
        	    	   midOptFit=midFit;    	    		   
    	    	   }
    	       }
    	   }//end for
    	   if(midOptFit<optFit){
    		   optFit=midOptFit;
    		   optOrderList=copyOrderList(midOptList);
    		   improved=true;
    	   }
       }//end while
//       System.out.println(optOrderList.size());
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }
 
  
  
  /**利用第三类迭代贪婪算法【两点互换,混合+模拟退火算法搜索机制和退出机制】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua etc., Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-降温系数
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
 * @throws CloneNotSupportedException 深度复制对象列表出现不支持的异常
   */
  
  public double[][] optSynRjLsumByIG4b(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,double[] params) throws CloneNotSupportedException{
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;
       double temp=nowFit;
//       System.out.println("start4b: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
	   //生成两点对的集合
	   int[][] pairs0=EasyMath.combin2(jobQty);   
	   int row=pairs0.length;
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=(int) params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   // System.out.println("loop.  "+nowGen+"  optFit: "+optFit+"   nowfit: "+nowFit);
    	   //点对互换，获取最优的解
    	   int[] seq=EasyMath.randPerm(jobQty);
    	   int[][] pairs=new int[row][2];
    	   for(int i=0;i<row;i++){
    		   pairs[i][0]=seq[pairs0[i][0]];
    		   pairs[i][1]=seq[pairs0[i][1]];
    	   }
    	   ArrayList<Order> midOptList=new ArrayList<>();//本次迭代的最优解列表
    	   double midOptFit=-1;
    	   for(int i=0;i<row;i++){
     		   ArrayList<Order> midOrderList=new ArrayList<>();//本次成对互换列表
     		   midOrderList=copyOrderList(orderList);       		   
    		   int[] nowPair=pairs[i];
    		   Order order1=(Order) midOrderList.get(nowPair[0]).clone();
    		   Order order2=(Order) midOrderList.get(nowPair[1]).clone();
    		   midOrderList.remove(nowPair[0]);
    		   midOrderList.add(nowPair[0],order2);
    		   midOrderList.remove(nowPair[1]);
    		   midOrderList.add(nowPair[1],order1);   
    	       double[][] midSch=getSynPMSch(midOrderList, machQty);
    	       double midFit=Schedule.getSumLateTime(midSch); 
//    	       System.out.println("i: "+midFit);
    	       if(midOptFit==-1){    	    	   
    	    	   midOptList=copyOrderList(midOrderList);
    	    	   midOptFit=midFit;
    	       }else{
    	    	   if(midFit<midOptFit){
        	    	   midOptList=copyOrderList(midOrderList);
        	    	   midOptFit=midFit;    	    		   
    	    	   }
    	       }
    	   }//end for
    	   if(midOptFit<optFit){//Global solution updating
    		   optFit=midOptFit;
    		   optOrderList=copyOrderList(midOptList);
    		   nowGen=0;
    	   }
    	   if(midOptFit<=nowFit){//local solution updating
    		   nowFit=midOptFit;
    		   orderList=copyOrderList(midOptList);    		   
    	   }else{
    		   //System.out.println("nowFit: "+nowFit+"  midOptFit: "+midOptFit+"  temp: "+temp+"  rate: "+Math.exp((nowFit-midOptFit)/temp));
    		   if(Math.random()<Math.exp((nowFit-midOptFit)/temp)*0.5){
        		   nowFit=midOptFit;
        		   orderList=copyOrderList(midOptList);        			   
    		   }   		   
    	   }   	   
    	   nowGen++;
    	   temp=temp*params[1];
//    	   System.out.println("gen: "+nowGen+" temp: "+temp);
//		   System.out.println("nowFit: "+nowFit+"  midOptFit: "+midOptFit+"  temp: "+temp+"  rate: "+Math.exp((nowFit-midOptFit)/temp));
    	   
       }//end while
//       System.out.println(optOrderList.size());
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }

  /**利用第三类迭代贪婪算法【两点互换,混合+模拟退火算法搜索机制+概率跳转会最优解和退出机制】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
 * @throws CloneNotSupportedException 深度复制对象列表出现不支持的异常
   */
  public double[][] optSynRjLsumByIG4c(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,double[] params) throws CloneNotSupportedException{
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;
       double temp=nowFit;
//       System.out.println("start4b: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
	   //生成两点对的集合
	   int[][] pairs0=EasyMath.combin2(jobQty);   
	   int row=pairs0.length;
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=(int) params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   // System.out.println("loop.  "+nowGen+"  optFit: "+optFit+"   nowfit: "+nowFit);
    	   //点对互换，获取最优的解
    	   int[] seq=EasyMath.randPerm(jobQty);
    	   int[][] pairs=new int[row][2];
    	   for(int i=0;i<row;i++){
    		   pairs[i][0]=seq[pairs0[i][0]];
    		   pairs[i][1]=seq[pairs0[i][1]];
    	   }
    	   ArrayList<Order> midOptList=new ArrayList<>();//本次迭代的最优解列表
    	   double midOptFit=-1;
    	   for(int i=0;i<10;i++){
     		   ArrayList<Order> midOrderList=new ArrayList<>();//本次成对互换列表
     		   midOrderList=copyOrderList(orderList);       		   
    		   int[] nowPair=pairs[i];
    		   Order order1=(Order) midOrderList.get(nowPair[0]).clone();
    		   Order order2=(Order) midOrderList.get(nowPair[1]).clone();
    		   midOrderList.remove(nowPair[0]);
    		   midOrderList.add(nowPair[0],order2);
    		   midOrderList.remove(nowPair[1]);
    		   midOrderList.add(nowPair[1],order1);   
    	       double[][] midSch=getSynPMSch(midOrderList, machQty);
    	       double midFit=Schedule.getSumLateTime(midSch); 
    	       if(midOptFit==-1){    	    	   
    	    	   midOptList=copyOrderList(midOrderList);
    	    	   midOptFit=midFit;
    	       }else{
    	    	   if(midFit<midOptFit){
    	    		   midOptList.clear();
        	    	   midOptList=copyOrderList(midOrderList);
        	    	   midOptFit=midFit;    	    		   
    	    	   }
    	       }
    	   }//end for
    	   if(midOptFit<optFit){//Global solution updating
    		   optFit=midOptFit;
    		   optOrderList=copyOrderList(midOptList);
    		   nowGen=0;
    	   }
    	   if(midOptFit<nowFit){//local solution updating
    		   nowFit=midOptFit;
    		   orderList=copyOrderList(midOptList);    		   
    	   }else{
    		   //System.out.println("nowFit: "+nowFit+"  midOptFit: "+midOptFit+"  temp: "+temp+"  rate: "+Math.exp((nowFit-midOptFit)/temp));
    		   if(Math.random()<Math.exp((nowFit-midOptFit)/temp)){
        		   nowFit=midOptFit;
        		   orderList=copyOrderList(midOptList);        			   
    		   }
    		   if(Math.random()<0.5){
        		   nowFit=optFit;
        		   orderList=copyOrderList(optOrderList);        			   
    		   }    		   
    	   }   	   
    	   nowGen++;
    	   temp=temp*0.99;
       }//end while
//       System.out.println(optOrderList.size());
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }

  /**利用第三类迭代贪婪算法【两点互换,混合+模拟退火算法搜索机制+概率{概率同劣化程度有关}跳转会最优解和退出机制】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
 * @throws CloneNotSupportedException 深度复制对象列表出现不支持的异常
   */
  public double[][] optSynRjLsumByIG4d(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,double[] params) throws CloneNotSupportedException{
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double nowFit=optFit;
       double temp=nowFit;
       //Step3：运用迭代贪婪算法寻优       
	   //生成两点对的集合
	   int[][] pairs0=EasyMath.combin2(jobQty);   
	   int row=pairs0.length;
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=(int) params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   // System.out.println("loop.  "+nowGen+"  optFit: "+optFit+"   nowfit: "+nowFit);
    	   //点对互换，获取最优的解
    	   int[] seq=EasyMath.randPerm(jobQty);
    	   int[][] pairs=new int[row][2];
    	   for(int i=0;i<row;i++){
    		   pairs[i][0]=seq[pairs0[i][0]];
    		   pairs[i][1]=seq[pairs0[i][1]];
    	   }
   	   
    	   
    	   ArrayList<Order> midOptList=new ArrayList<>();//本次迭代的最优解列表
    	   double midOptFit=-1;
    	   for(int i=0;i<10;i++){
     		   ArrayList<Order> midOrderList=new ArrayList<>();//本次成对互换列表
     		   midOrderList=copyOrderList(orderList);       		   
    		   int[] nowPair=pairs[i];
    		   //找到这两个作业所在的位置，而不是直接使用两个位置上的作业进行调换		   
    		   for(int j=0;j<jobQty;j++){
    			   Order o=midOrderList.get(j);
    			   if(o.orderId==nowPair[0]){
    				   nowPair[0]=j;
    			   }
    			   if(o.orderId==nowPair[1]){
    				   nowPair[1]=j;
    			   }    			   
    		   }     		   
    		   
    		   Order order1=(Order) midOrderList.get(nowPair[0]).clone();
    		   Order order2=(Order) midOrderList.get(nowPair[1]).clone();
    		   midOrderList.remove(nowPair[0]);
    		   midOrderList.add(nowPair[0],order2);
    		   midOrderList.remove(nowPair[1]);
    		   midOrderList.add(nowPair[1],order1);   
    	       double[][] midSch=getSynPMSch(midOrderList, machQty);
    	       double midFit=Schedule.getSumLateTime(midSch); 
    	       if(midOptFit==-1){    	    	   
    	    	   midOptList=copyOrderList(midOrderList);
    	    	   midOptFit=midFit;
    	       }else{
    	    	   if(midFit<midOptFit){
    	    		   midOptList.clear();
        	    	   midOptList=copyOrderList(midOrderList);
        	    	   midOptFit=midFit;    	    		   
    	    	   }
    	       }
    	   }//end for
    	   if(midOptFit<optFit){//Global solution updating
    		   optFit=midOptFit;
    		   optOrderList.clear();
    		   optOrderList=copyOrderList(midOptList);
    		   nowGen=0;
    	   }
    	   if(midOptFit<nowFit){//local solution updating
    		   nowFit=midOptFit;
    		   orderList.clear();
    		   orderList=copyOrderList(midOptList);    		   
    	   }else{
    		   //System.out.println("nowFit: "+nowFit+"  midOptFit: "+midOptFit+"  temp: "+temp+"  rate: "+Math.exp((nowFit-midOptFit)/temp));
    		   if(Math.random()<Math.exp((nowFit-midOptFit)/temp)){
        		   nowFit=midOptFit;
        		   orderList.clear();
        		   orderList=copyOrderList(midOptList);        			   
    		   }
    		   if(Math.random()<1-Math.exp((optFit-midOptFit)/midOptFit)){
        		   nowFit=optFit;
        		   orderList.clear();
        		   orderList=copyOrderList(optOrderList);        			   
    		   }    		   
    	   }   	   
    	   nowGen++;
    	   temp=temp*0.99;
       }//end while
//       System.out.println(optOrderList.size());
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }  

  /**利用第二类迭代贪婪算法【每个循环过程随机搜索d个作业，然后将这d个作业插入到n个不同位置进行领域寻优】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG2old(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   orderList.clear();
    	   orderList=copyOrderList(optOrderList);
    	   int[] randSeq=EasyMath.randPerm(jobQty);//生成0-[jobQty-1]区间的随机数列
    	   for(int i=0;i<params[1];i++){
    		   int posId=randSeq[i];
    		   Order or=orderList.get(posId);//重新排序的作业对象
    		   ArrayList<Order> midOrderList=new ArrayList<>();
    		   midOrderList=copyOrderList(orderList);
    		   midOrderList.remove(posId);
    		   ArrayList<Order> midOptList=new ArrayList<>();//当前订单插入不同位置所获得的最好排序
    		   double midOptFit=0;
    		   for(int j=0;j<jobQty;j++){
        		   ArrayList<Order> midList=new ArrayList<>();//当前处理订单插入不同位置获得的全排序
        		   midList=copyOrderList(midOrderList);
        		   midList.add(j, or);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList=copyOrderList(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList.clear();
        	    		   midOptList=copyOrderList(midList);
        	    	   }
        	       }   			   
    		   }
    		   //System.out.println("midOptFit: "+midOptFit+"   optFit:"+optFit);
    		   if(midOptFit<optFit){
    			   optFit=midOptFit;
    			   optOrderList=midOptList;
    			   nowGen=0;
    		   }   		   
    	   }
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }
  
  /**利用第二类迭代贪婪算法[破坏和重构两个步骤，破坏过程提取随机d个作业，然后将d个作业按照NEH的方式再进行解的重构]进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param params 算法参数，0-最大无更新代数,1-每次摧毁作业的数量d
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG3old(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int[] params){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     //进行具体的运算
     if(noError){
       //step1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       ArrayList<Order> optOrderList=new ArrayList<>();
       optOrderList=copyOrderList(orderList);
       double[][] optSch=getSynPMSch(optOrderList, machQty);
       double optFit=Schedule.getSumLateTime(optSch);
       double fit0=optFit;
       System.out.println("start: "+ optFit);
       //Step3：运用迭代贪婪算法寻优       
       
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=params[0];
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   ArrayList<Order> insertList=new ArrayList<>();//待插入订单列表-d个破坏提取出来的订单
    	   ArrayList<Order> remainList=new ArrayList<>();//剩余订单列表
    	   remainList.addAll(orderList);
    	   for(int i=0;i<params[1];i++){//构建破坏订单集合和剩余订单列表集合
    		   int remainQty=jobQty-i;
    		   int midIdx=(int) (Math.random()*remainQty);
    		   insertList.add(remainList.get(midIdx));
    		   remainList.remove(midIdx);
    	   }
    	   //依次将破坏订单集合中的订单插入到剩余订单列表中的合适位置
    	   for(int i=0;i<params[1];i++){
    		   Order nowOr=insertList.get(i);
    		   //将各个订单的原始订单序号记录下来,并将订单列表中的订单编号修改为一定的顺序码
    		   int remainQty=remainList.size();
    		   int[] orderIds=new int[remainQty+1];
    		   for(int j=0;j<remainQty;j++){
    			   orderIds[j]=remainList.get(j).getOrderId();
    			   remainList.get(j).setOrderId(j);
    		   }
    		   orderIds[remainQty]=nowOr.orderId;
    		   nowOr.setOrderId(remainQty);
    		   //执行当前订单插入最优位置搜索
    		   ArrayList<Order> midOptList=new ArrayList<>();//用于存储最优订单列表排序
    		   double midOptFit=0;
    		   for(int j=0;j<=remainQty;j++){
    			   ArrayList<Order> midList=new ArrayList<>();//用于存储插入不同位置后的订单列表
    			   midList.addAll(remainList);
    			   midList.add(j, nowOr);
        	       double[][] midSch=getSynPMSch(midList, machQty);
        	       double midFit=Schedule.getSumLateTime(midSch); 
        	       if(j==0){
        	    	   midOptFit=midFit;
        	    	   midOptList.addAll(midList);
        	       }else{
        	    	   if(midFit<midOptFit){
        	    		   midOptFit=midFit;
        	    		   midOptList.clear();
        	    		   midOptList.addAll(midList);
        	    	   }
        	       }     			   
    		   }
    		   //进行订单编码的还原
    		   for(int j=0;j<=remainQty;j++){
    			   int idx=midOptList.get(j).orderId;
    			   midOptList.get(j).orderId=orderIds[idx];
    		   }
    		   //将当前订单插入后的最好排序订单列表替换为剩余订单列表
    		   remainList.clear();
    		   remainList.addAll(midOptList);
    	   }
    	   //计算重新构建的订单列表的总完工时间，查看是否比最优解小，以便替换最优解
	       double[][] nowSch=getSynPMSch(remainList, machQty);
	       double nowFit=Schedule.getSumLateTime(nowSch); 
	       //System.out.println("nowFit: "+nowFit+"  optFit: "+optFit);
	       if(nowFit<optFit){
	    	   optFit=nowFit;
	    	   optOrderList.clear();
	    	   optOrderList.addAll(remainList);
	    	   nowGen=0;
	       }
	       if(nowFit<fit0){
	    	   fit0=nowFit;
	    	   orderList.clear();
	    	   orderList.addAll(remainList);
	       }else{
	    	   if(Math.random()<0.2){
		    	   fit0=nowFit;
		    	   orderList.clear();
		    	   orderList.addAll(remainList);	    		   
	    	   }
	       }	       
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optOrderList, machQty);
     }//end if-noError
     return sch;   
   }

    
  
  /**利用第四类迭代贪婪算法【分机器编码，单点插入】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param maxGen 最大无更新代数，一般为30,40,50等整数
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG5(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int maxGen){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     double maxReleaseTime=EasyMath.max(rtimes);//用于不能同步作业的排产惩罚
     //进行具体的运算
     if(noError){
       //step1-1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step1-2: 构建机器对象列表，便于后续操作
       ArrayList<Machine> machineList=new ArrayList<>();  
       for(int i=0;i<machQty;i++){
    	   Machine mach=new Machine();
    	   mach.setMachId(i);
    	   machineList.add(mach);
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       for(int i=0;i<jobQty;i++){
    	   Order or=orderList.get(i);
    	   int machIdx=i%machQty;
    	   machineList.get(machIdx).getOrderList().add(or);//将当前订单加入特定机器的订单列表
       }
       double[][] optSch=getSynPMSch(machineList);
       double optFit=Schedule.getSumLateTime(optSch,maxReleaseTime);
//       EasyArray.printArray(optSch);
       System.out.println("start IGA4: "+ optFit);
//       System.out.println("kkk    "+machineList.toString());
       //Step3：运用迭代贪婪算法寻优       
       ArrayList<Machine> optMachList=copyMachList(machineList);
       double temp=optFit;
       double nowOptFit=optFit;
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=maxGen;
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   //将最优解替换临时处理解集 
   // 	   machineList=copyMachList(optMachList); 
  //  	   System.out.println("dizhi: "+machineList);
    	   System.out.println("nowOpt: "+ nowOptFit);
    	   ArrayList<Machine> midOptMachList=copyMachList(machineList);//单点插入操作获得的最好排序列表
    	   double midOptFit=-1;
    	   //单点插入，每个机器的每个订单插入到不同机器上不同位置获得的最优结果
    	   for(int i=0;i<machQty;i++){
    		   Machine nowMach=machineList.get(i);
    		   List<Order> midOrList=nowMach.getOrderList();
    		   if(midOrList.size()>0){//则依次将该机器上的订单插入到其他机器上的不同位置，并获得最好的结果作为下一循环的输入
    			   for(int j=0;j<midOrList.size();j++){
    				   Order insertOrder=midOrList.get(j);//待插入新位置的订单对象
    				   //复制一个新机器列表
    				   ArrayList<Machine> midMachList=copyMachList(machineList);
    				   midMachList.get(i).getOrderList().remove(j);//将当前机器上当前订单删除
    				   for(int k=0;k<machQty;k++){
    					   if(i!=k){//必须不是同一台机器
    						   for(int t=0;t<=midMachList.get(k).getOrderList().size();t++){
    							   //插入不同位置，形成新的解，并进行最优判断
    							   ArrayList<Machine> newMachList=copyMachList(midMachList);
    							   newMachList.get(k).getOrderList().add(t, insertOrder);
    						       double[][] newSch=getSynPMSch(newMachList);
    						       double newFit=Schedule.getSumLateTime(newSch,maxReleaseTime); 
    						       if(midOptFit==-1){//第一个插入邻域解
    						    	   midOptFit=newFit;
    						    	   midOptMachList=copyMachList(newMachList);
    						       }else if(newFit<midOptFit){
    						    	   midOptFit=newFit;
    						    	   midOptMachList=copyMachList(newMachList);
    						       }
    						       
    						   }
    					   }
    				   }
    				   
    			   }
    		   }
    	   }
    	   //进行全局最优解判断和更新
    	   if(midOptFit>0 &&midOptFit<optFit){
    		   optFit=midOptFit;
    		   optMachList=copyMachList(midOptMachList);
    		   nowGen=0;
    	   }
		   System.out.println("exp:  dddopt: "+nowOptFit+"  midOptFit: "+midOptFit+"  temp: "+ temp+"  exp: "+Math.exp((optFit-midOptFit)/temp));
    	   
    	   if(midOptFit>0 &&midOptFit<nowOptFit){
    		   nowOptFit=midOptFit;
    		   machineList=copyMachList(midOptMachList);
    	   }else{//按照概率接受局部最优解为临时解
    		   System.out.println("exp:  opt: "+nowOptFit+"  midOptFit: "+midOptFit+"  temp: "+ temp+"  exp: "+Math.exp((optFit-midOptFit)/temp));
    		   if(Math.random()<Math.exp((nowOptFit-midOptFit)/temp)){
    			   System.out.println("kkkkkkk---------");
    			   nowOptFit=midOptFit;
    		     machineList=copyMachList(midOptMachList);
    		   }
    	   }    	   
    	   temp=temp*0.99;
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optMachList);
       //System.out.println(optMachList.toString());
     }//end if-noError
     return sch;   
   }

  /**利用第四类迭代贪婪算法【分机器编码，两点互换】进行同步启停单阶段并行机Pm|rj,syn|Lsum-TT问题的优化求解，
   * 算法参考：Wang Jianhua, Some iterated greedy algorithms for a synchronous parallel machine scheduling to minimize total tardiness.
   * @param ptimes 作业时间
   * @param rtimes 作业的释放时间 
   * @param dtimes 作业的交付时间  
   * @param machQty 并行机数量 
   * @param maxGen 最大无更新代数的整数值，一般可选30,40等
   * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
   *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
   *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
   */
  public double[][] optSynRjLsumByIG5C(double[] ptimes,double[] rtimes,int machQty,double[] dtimes,int maxGen){
	    //step1:输入参数判断
     double[][] sch=new double[2][2];
     int jobQty=rtimes.length;
     boolean noError=true;
     if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
       noError=false;
       System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
     }
     double maxReleaseTime=EasyMath.max(rtimes);//用于不能同步作业的排产惩罚
     //进行具体的运算
     if(noError){
       //step1-1: 先构建作业对象列表，便于后续操作
       ArrayList<Order> orderList=new ArrayList<>();
       for(int i=0;i<jobQty;i++){
         Order or=new Order();
         or.setOrderId(i);
         or.setReleaseTime(rtimes[i]);
         or.setDueTime(dtimes[i]);
         or.setProcTime(ptimes[i]);
         orderList.add(or);       
       }
       //step1-2: 构建机器对象列表，便于后续操作
       ArrayList<Machine> machineList=new ArrayList<>();  
       for(int i=0;i<machQty;i++){
    	   Machine mach=new Machine();
    	   mach.setMachId(i);
    	   machineList.add(mach);
       }
       //step2: 进行解的初始化
       //对orderList按照releaseTime升序排序，从而形成初始排序
       orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
       for(int i=0;i<jobQty;i++){
    	   Order or=orderList.get(i);
    	   int machIdx=i%machQty;
    	   machineList.get(machIdx).getOrderList().add(or);//将当前订单加入特定机器的订单列表
       }
       double[][] optSch=getSynPMSch(machineList);
       double optFit=Schedule.getSumLateTime(optSch,maxReleaseTime);
//       EasyArray.printArray(optSch);
       System.out.println("start IGA4: "+ optFit);
//       System.out.println("kkk    "+machineList.toString());
       //Step3：运用迭代贪婪算法寻优       
       ArrayList<Machine> optMachList=copyMachList(machineList);
       
       int nowGen=0;//累计无更新迭代代数
       int maxNoImproveGen=maxGen;
       //贪婪迭代主循环
       while(nowGen<maxNoImproveGen){
    	   //将最优解替换临时处理解集 
 //   	   machineList=copyMachList(optMachList); 
  //  	   System.out.println("dizhi: "+machineList);
    	   System.out.println("opt: "+ optFit);
    	   //单点插入，每个机器的每个订单插入到不同机器上不同位置获得的最优结果
    	   for(int i=0;i<machQty;i++){
    		   Machine nowMach=machineList.get(i);
    		   List<Order> midOrList=nowMach.getOrderList();
    		   if(midOrList.size()>0){//则依次将该机器上的订单插入到其他机器上的不同位置，并获得最好的结果作为下一循环的输入
    			   for(int j=0;j<midOrList.size();j++){
    				   Order insertOrder=midOrList.get(j);//待插入新位置的订单对象
    				   //复制一个新机器列表
    				   ArrayList<Machine> midMachList=copyMachList(machineList);
    				   midMachList.get(i).getOrderList().remove(j);//将当前机器上当前订单删除
    				   for(int k=0;k<machQty;k++){
    					   if(i!=k){//必须不是同一台机器
    						   for(int t=0;t<=midMachList.get(k).getOrderList().size();t++){
    							   //插入不同位置，形成新的解，并进行最优判断
    							   ArrayList<Machine> newMachList=copyMachList(midMachList);
    							   newMachList.get(k).getOrderList().add(t, insertOrder);
    						       double[][] newSch=getSynPMSch(newMachList);
    						       double newFit=Schedule.getSumLateTime(newSch,maxReleaseTime); 
    						       System.out.println("newFit: "+ newFit);
    						       if(newFit<optFit){
    						    	   optFit=newFit;
    						    	   optMachList=copyMachList(newMachList);
    						    	   nowGen=0;
    						       }
    						       
    						   }
    					   }
    				   }
    				   
    			   }
    		   }
    	   }
    	   
    	   nowGen++;
       }//end while
       sch=getSynPMSch(optMachList);
       //System.out.println(optMachList.toString());
     }//end if-noError
     return sch;   
   }
  
  
  /**
 * 将传入的机器对象列表深度复制一个相同的对象列表，使得输入对象列表和返回对象列表内存地址不同，即深度复制对象列表
 * @param inMachList 被复制的机器对象列表名称
 * @return 内容和输入机器对象列表完全相同，而内存地址不同的对象列表地址
 * */
   public ArrayList<Machine> copyMachList(ArrayList<Machine> inMachList){
	   ArrayList<Machine> outMachList=new ArrayList<>();
       for(int i=0;i<inMachList.size();i++){
    	   Machine mach=new Machine();
    	   ArrayList<Order> midOrderList=new ArrayList<>();
    	   for(Order o:inMachList.get(i).getOrderList()){    		   
    		   try {
				midOrderList.add((Order) o.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	   }
    	   mach.setOrderList(midOrderList);
    	   mach.setMachId(inMachList.get(i).getMachId());
    	   outMachList.add(mach);
       }	   
	return outMachList;
	   
   }
   /**
    * 将传入的作业列表深度复制一个相同的对象列表，使得输入对象列表和返回对象列表内存地址不同，即深度复制对象列表
 * @param inOrderList 被复制的作业对象列表名称
 * @return 内容和输入作业对象列表完全相同，而内存地址不同的对象列表地址
    * */
   public ArrayList<Order> copyOrderList(ArrayList<Order> inOrderList){
   	   ArrayList<Order> outOrderList=new ArrayList<>();
   	   for(Order o:inOrderList){    		   
   		   try {
			outOrderList.add((Order) o.clone());
		   } catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   	   }   
   	   return outOrderList;  	   
  }   
  
  /**利用改进贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解，
  * 算法参考：D. Baraz and G. Mosheiov, A note on a greedy heuristic for flow-shop makespan minimization with no machine idle-time, European Journal of Operational Research (2008), 810–813.
  * @param ptimes 作业时间
  * @param rtimes 作业的释放时间 
  * @param dtimes 作业的交付时间  
  * @param machQty 并行机数量 
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByIG(double[] ptimes,double[] rtimes,int machQty,double[] dtimes){
    //step1:
    double[][] sch=new double[1][2];
    int jobQty=rtimes.length;
    boolean noError=true;
    if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
      noError=false;
      System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
    }
    //进行具体的运算
    if(noError){
      //step1: 先构建作业对象列表，便于后续操作
      ArrayList<Order> orderList=new ArrayList<>();
      for(int i=0;i<jobQty;i++){
        Order or=new Order();
        or.setOrderId(i);
        or.setReleaseTime(rtimes[i]);
        or.setDueTime(dtimes[i]);
        orderList.add(or);       
      }
      //step2: 进行第一步的贪婪排序
      //对orderList按照releaseTime升序排序
      orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
      int[] sequence1=new int[1];//根据释放时间优先规则排第一个作业
      sequence1[0]=orderList.get(0).getOrderId();
      orderList.remove(0);
      //贪婪算法循环排序
      while(orderList.size()>0){
        int remainQty=orderList.size();
        double[][] midFits=new double[remainQty][2];//0-新的订单排序，1-新订单加入旧排序后的累计延误时长
        int seqQty=sequence1.length;
        for(int j=0;j<remainQty;j++){
          Order or=orderList.get(j);
          int orderId=or.getOrderId();//订单编号从1开始
          int[] midSeq=new int[seqQty+1];//存储的是原始作业编号
          System.arraycopy(sequence1, 0, midSeq, 0, seqQty);
          midSeq[seqQty]=orderId;
          double[] midRtimes=new double[seqQty+1];
          double[] midDtimes=new double[seqQty+1];
          double[] midPtimes=new double[seqQty+1];
          for(int i=0;i<=seqQty;i++){
            midRtimes[i]=rtimes[midSeq[i]];
            midDtimes[i]=dtimes[midSeq[i]];
            midPtimes[i]=ptimes[midSeq[i]];
          }
          int[] newSeq=new int[seqQty+1];//存储的是midSeq中的作业的编号，从1开始到seqQty+1结束
          for(int i=0;i<seqQty+1;i++){
            newSeq[i]=i+1;
          }
//          HmArray.printArray(newSeq);
//          System.out.println(" the new orderId "+ (orderId+1));
          //生成新的排产方案
          double[][] midSch=getSynPMSch(midPtimes, midDtimes, newSeq, machQty, midRtimes);
          //计算延误时间之和
          double sumLateTime=Schedule.getSumLateTime(midSch);
          midFits[j][0]=j;
          midFits[j][1]=sumLateTime;                
        }
          //排序后获得订单编码
          EasyMath.sortArray(midFits,new int[]{1});
//          System.out.println("------------------------midFit1----------- ");          
//          HmArray.printArray(midFits);
          int goodJobIdx=(int)midFits[0][0];//该数值从0开始编号  
          int[] midFinalSeq1=new int[seqQty+1];
          System.arraycopy(sequence1, 0, midFinalSeq1,0, seqQty);
          midFinalSeq1[seqQty]=orderList.get(goodJobIdx).getOrderId();
          sequence1=midFinalSeq1;//更新排序列表
          orderList.remove(goodJobIdx);//将此次排序的作业从作业列表中删除
      }
      int[] initSeq=new int[jobQty];
      for(int k=0;k<jobQty;k++){
        initSeq[k]=sequence1[k]+1;
      }
      //step3: 成对调换
      //step3.1 首先将全部的替换对找出来
      int[][] pairIdxs=EasyMath.combin2(jobQty);
//      HmArray.printArray(pairIdxs);
      //step3.2 依次替换成对作业，并确定是否用替换后的排序替代以前的排序
      int pairRows=pairIdxs.length;
      int[] firstSeq=new int[jobQty];
      for(int i=0;i<jobQty;i++){
        firstSeq[i]=sequence1[i]+1;
      }

      //生成新的排产方案
      double[][] midSch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);
      //计算延误时间之和
      double sumLateTime1=Schedule.getSumLateTime(midSch);      
      for(int i=0;i<pairRows;i++){
        int[] newSeq;
        newSeq=Arrays.copyOf(firstSeq, jobQty);
        int oneJobNo=firstSeq[pairIdxs[i][0]];
        int twoJobNo=firstSeq[pairIdxs[i][1]]; 
        newSeq[pairIdxs[i][0]]=twoJobNo;
        newSeq[pairIdxs[i][1]]=oneJobNo;        
        double[][] midSch2=getSynPMSch(ptimes, dtimes, newSeq, machQty, rtimes);
        //计算延误时间之和
        double sumLateTime2=Schedule.getSumLateTime(midSch2); 
//        HmArray.printArray(newSeq);
//        System.out.println("pairwise SDT: "+sumLateTime2);
        if(sumLateTime2<sumLateTime1){//替换一下
          sumLateTime1=sumLateTime2;
          firstSeq=newSeq;         
        }
      }
//          System.out.println("------------------------sequence by IG-----------");
//          HmArray.printArray(firstSeq);      
      //替换结束，获得最优的结果
      sch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);      
    }
    return sch;   
  }  

  /**利用成对交换多循环的改进【用FCFS产生初始解】贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解，
  * 算法参考：D. Baraz and G. Mosheiov, A note on a greedy heuristic for flow-shop makespan minimization with no machine idle-time, European Journal of Operational Research (2008), 810–813.
  * @param ptimes 作业时间
  * @param rtimes 作业的释放时间 
  * @param dtimes 作业的交付时间  
  * @param machQty 并行机数量 
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByMultiFIG(double[] ptimes,double[] rtimes,int machQty,double[] dtimes){
    //step1:
    double[][] sch=new double[1][2];
    int jobQty=rtimes.length;
    boolean noError=true;
    if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
      noError=false;
      System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
    }
    //进行具体的运算
    if(noError){
      //step1: 使用FCFS生成初始排序
      int[] sequence1=Rules.seqFCFS(rtimes);
      //step3: 成对调换
      int isImproved=1;//对替换是否有改善
      int loop=0;
      while(isImproved>0){
        isImproved=0;//初始化为没改善
        //step3.1 首先将全部的替换对找出来
        int[][] pairIdxs=EasyMath.combin2(jobQty);
        //step3.2 依次替换成对作业，并确定是否用替换后的排序替代以前的排序
        int pairRows=pairIdxs.length;
        int[] firstSeq=new int[jobQty];
        for(int i=0;i<jobQty;i++){
          firstSeq[i]=sequence1[i];
        }
        //生成新的排产方案
        double[][] midSch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);
        //计算延误时间之和
        double sumLateTime1=Schedule.getSumLateTime(midSch);      
        for(int i=0;i<pairRows;i++){
          int[] newSeq;
          newSeq=Arrays.copyOf(firstSeq, jobQty);
          int oneJobNo=firstSeq[pairIdxs[i][0]];
          int twoJobNo=firstSeq[pairIdxs[i][1]]; 
          newSeq[pairIdxs[i][0]]=twoJobNo;
          newSeq[pairIdxs[i][1]]=oneJobNo;        
          double[][] midSch2=getSynPMSch(ptimes, dtimes, newSeq, machQty, rtimes);
          //计算延误时间之和
          double sumLateTime2=Schedule.getSumLateTime(midSch2); 
          if(sumLateTime2<sumLateTime1){//替换一下
            sumLateTime1=sumLateTime2;
            firstSeq=newSeq;
            isImproved=1;//有改善，则后续还需要进行while循环
          }
        }
        sequence1=firstSeq;
        loop++;
        if(loop>100){
          isImproved=0;
          System.out.println("Error happen in MultiFIG..");
        }
      }
      //替换结束，获得最优的结果
//          System.out.println("------------------------sequence by FMIG-----------");
//          HmArray.printArray(sequence1);        
      sch=getSynPMSch(ptimes, dtimes, sequence1, machQty, rtimes);      
    }
    return sch;   
  }  
  
  
  /**利用成对交换多循环的贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解，
  * 算法参考：D. Baraz and G. Mosheiov, A note on a greedy heuristic for flow-shop makespan minimization with no machine idle-time, European Journal of Operational Research (2008), 810–813.
  * @param ptimes 作业时间
  * @param rtimes 作业的释放时间 
  * @param dtimes 作业的交付时间  
  * @param machQty 并行机数量 
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByMultiIG(double[] ptimes,double[] rtimes,int machQty,double[] dtimes){
    //step1:
    double[][] sch=new double[1][2];
    int jobQty=rtimes.length;
    boolean noError=true;
    if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
      noError=false;
      System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
    }
    //进行具体的运算
    if(noError){
      //step1: 先构建作业对象列表，便于后续操作
      ArrayList<Order> orderList=new ArrayList<>();
      for(int i=0;i<jobQty;i++){
        Order or=new Order();
        or.setOrderId(i);
        or.setReleaseTime(rtimes[i]);
        or.setDueTime(dtimes[i]);
        orderList.add(or);       
      }
      //step2: 进行第一步的贪婪排序
      //对orderList按照releaseTime升序排序
      orderList.sort(Comparator.comparingDouble(Order::getReleaseTime));
      int[] sequence1=new int[1];//根据释放时间优先规则排第一个作业
      sequence1[0]=orderList.get(0).getOrderId();
      orderList.remove(0);
      //贪婪算法循环排序
      while(orderList.size()>0){
        int remainQty=orderList.size();
        double[][] midFits=new double[remainQty][2];//0-新的订单排序，1-新订单加入旧排序后的累计延误时长
        int seqQty=sequence1.length;
        for(int j=0;j<remainQty;j++){
          Order or=orderList.get(j);
          int orderId=or.getOrderId();//订单编号从1开始
          int[] midSeq=new int[seqQty+1];//存储的是原始作业编号
          System.arraycopy(sequence1, 0, midSeq, 0, seqQty);
          midSeq[seqQty]=orderId;
          double[] midRtimes=new double[seqQty+1];
          double[] midDtimes=new double[seqQty+1];
          double[] midPtimes=new double[seqQty+1];
          for(int i=0;i<=seqQty;i++){
            midRtimes[i]=rtimes[midSeq[i]];
            midDtimes[i]=dtimes[midSeq[i]];
            midPtimes[i]=ptimes[midSeq[i]];
          }
          int[] newSeq=new int[seqQty+1];//存储的是midSeq中的作业的编号，从1开始到seqQty+1结束
          for(int i=0;i<seqQty+1;i++){
            newSeq[i]=i+1;
          }
          //生成新的排产方案
          double[][] midSch=getSynPMSch(midPtimes, midDtimes, newSeq, machQty, midRtimes);
          //计算延误时间之和
          double sumLateTime=Schedule.getSumLateTime(midSch);
          midFits[j][0]=j;
          midFits[j][1]=sumLateTime;                
        }
          //排序后获得订单编码
          EasyMath.sortArray(midFits,new int[]{1});
          int goodJobIdx=(int)midFits[0][0];//该数值从0开始编号  
          int[] midFinalSeq1=new int[seqQty+1];
          System.arraycopy(sequence1, 0, midFinalSeq1,0, seqQty);
          midFinalSeq1[seqQty]=orderList.get(goodJobIdx).getOrderId();
          sequence1=midFinalSeq1;//更新排序列表
          orderList.remove(goodJobIdx);//将此次排序的作业从作业列表中删除
      }
      //step3: 成对调换
      int loop=0;
      int isImproved=1;//对替换是否有改善
      while(isImproved>0){
        isImproved=0;//初始化为没改善
        //step3.1 首先将全部的替换对找出来
        int[][] pairIdxs=EasyMath.combin2(jobQty);
        //step3.2 依次替换成对作业，并确定是否用替换后的排序替代以前的排序
        int pairRows=pairIdxs.length;
        int[] firstSeq=new int[jobQty];
        for(int i=0;i<jobQty;i++){
          firstSeq[i]=sequence1[i]+1;
        }
        //生成新的排产方案
        double[][] midSch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);
        //计算延误时间之和
        double sumLateTime1=Schedule.getSumLateTime(midSch);      
        for(int i=0;i<pairRows;i++){
          int[] newSeq;
          newSeq=Arrays.copyOf(firstSeq, jobQty);
          int oneJobNo=firstSeq[pairIdxs[i][0]];
          int twoJobNo=firstSeq[pairIdxs[i][1]]; 
          newSeq[pairIdxs[i][0]]=twoJobNo;
          newSeq[pairIdxs[i][1]]=oneJobNo;        
          double[][] midSch2=getSynPMSch(ptimes, dtimes, newSeq, machQty, rtimes);
          //计算延误时间之和
          double sumLateTime2=Schedule.getSumLateTime(midSch2); 
          if(sumLateTime2<sumLateTime1){//替换一下
            sumLateTime1=sumLateTime2;
            firstSeq=newSeq;
            isImproved=1;//有改善，则后续还需要进行while循环
          }
        }
  
          for(int i=0;i<jobQty;i++){
            sequence1[i]=firstSeq[i]-1;
          }
        loop++;
        if(loop>100){
          isImproved=0;
          System.out.println("Error happen in MultiIG..");
        }
      }
      //替换结束，获得最优的结果
        for(int i=0;i<jobQty;i++){
          sequence1[i]++;
        }  
//          System.out.println("------------------------sequence by MIG-----------");
//          HmArray.printArray(sequence1);           
      sch=getSynPMSch(ptimes, dtimes, sequence1, machQty, rtimes);      
    }
    return sch;   
  }  
  
  
  
  /**利用改进贪婪算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解，
  * 算法参考：D. Baraz and G. Mosheiov, A note on a greedy heuristic for flow-shop makespan minimization with no machine idle-time, European Journal of Operational Research (2008), 810–813.
  * @param ptimes 作业时间
  * @param rtimes 作业的释放时间 
  * @param dtimes 作业的交付时间  
  * @param machQty 并行机数量 
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByIGonFCFS(double[] ptimes,double[] rtimes,int machQty,double[] dtimes){
    //step1:
    double[][] sch=new double[1][2];
    int jobQty=rtimes.length;
    boolean noError=true;
    if(ptimes.length!=rtimes.length||rtimes.length!=dtimes.length){
      noError=false;
      System.out.println("SynPMS.optSynRjLsumByIG()中输入的作业时间、释放时间、交付时间长度不相等");
    }
    //进行具体的运算
    if(noError){
      int[] sequence1=Rules.seqFCFS(rtimes);

      //step3: 成对调换
      //step3.1 首先将全部的替换对找出来
      int[][] pairIdxs=EasyMath.combin2(jobQty);
      //step3.2 依次替换成对作业，并确定是否用替换后的排序替代以前的排序
      int pairRows=pairIdxs.length;
      int[] firstSeq=new int[jobQty];
      for(int i=0;i<jobQty;i++){
        firstSeq[i]=sequence1[i];
      }
      //生成新的排产方案
      double[][] midSch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);
      //计算延误时间之和
      double sumLateTime1=Schedule.getSumLateTime(midSch);      
      for(int i=0;i<pairRows;i++){
        int[] newSeq;
        newSeq=Arrays.copyOf(firstSeq, jobQty);
        int oneJobNo=firstSeq[pairIdxs[i][0]];
        int twoJobNo=firstSeq[pairIdxs[i][1]]; 
        newSeq[pairIdxs[i][0]]=twoJobNo;
        newSeq[pairIdxs[i][1]]=oneJobNo;        
        double[][] midSch2=getSynPMSch(ptimes, dtimes, newSeq, machQty, rtimes);
        //计算延误时间之和
        double sumLateTime2=Schedule.getSumLateTime(midSch2); 
        if(sumLateTime2<sumLateTime1){//替换一下
          sumLateTime1=sumLateTime2;
          firstSeq=newSeq;
        }
      }
//          System.out.println("------------------------sequence by FIG-----------");
//          HmArray.printArray(firstSeq);           
      //替换结束，获得最优的结果
      sch=getSynPMSch(ptimes, dtimes, firstSeq, machQty, rtimes);      
    }
    return sch;   
  }    
  
   /**利用模拟退火算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间 
  * @param machQty 并行机数量
  * @param params 模拟退火算法的相关参数，0-降温速率，1-内循环次数，2-外循环次数，3-无更新终止代数
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumBySA(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params){
    double[][] sch;
    //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
    double[][] optSeries=new double[(int)params[2]][3];//
    
    //算法计算过程中存储的数值
    int codeLength=ptimes.length;
    //随机初始化第一个解
//    int[] sequence1=JMath.randPermStart1(codeLength);//生出初始作业的调度顺序
    int[] sequence1=Rules.seqFCFS(rtimes);

    double[][] schedule1=getSynPMSch(ptimes,dtimes,sequence1,machQty,rtimes);
    double fit1=Schedule.getSumLateTime(schedule1);
    //存储最优解的类容
    double optFit=fit1;//最优解
    double inOptFit;//内循环的最优解   
    int[] inOptSequence;//内循环最优解    
    int[] optSequence=sequence1;
    //定义一些用于存储中间解的变量
    int[] sequence2;
    double[][] schedule2;
    double fit2;
    double delta;  
    
      //(1）模拟退火主程序   
    //算法相关参数设定
    double temp=EasyMath.sum(ptimes),alpha=params[0];
    int inLoopNum=(int)params[1],outStop=0,noGoodLoop=0;
    int maxLoopNum=(int)params[2],nowLoop=0;
    //算法循环主程序
    while(outStop==0&&nowLoop<maxLoopNum){
      inOptFit=fit1;
      inOptSequence=Arrays.copyOf(sequence1, codeLength);       
      for(int i=0;i<inLoopNum;i++){
        sequence2=EasyMath.reverseArray(sequence1);//在既有最优解基础上通过逆序产生新解
        schedule2=getSynPMSch(ptimes, dtimes, sequence2, machQty,rtimes);
        fit2=Schedule.getSumLateTime(schedule2);        
        delta=fit2-fit1;
        if(delta<0){
          sequence1=Arrays.copyOf(sequence2, codeLength);  
          fit1=fit2;
          if(inOptFit>fit1){
            inOptFit=fit1;
            inOptSequence=Arrays.copyOf(sequence2, codeLength);         
          }
        }else{   
          if(Math.exp(-delta/temp)>Math.random()){
            sequence1=Arrays.copyOf(sequence2, codeLength);
            fit1=fit2;
          }
        }
      }
      temp=alpha*temp;//降温操作    

      if(inOptFit>=optFit){
        noGoodLoop++;
      }else{
        noGoodLoop=0;
        optFit=inOptFit;
        optSequence=Arrays.copyOf(inOptSequence, codeLength);
      }
      if(noGoodLoop==(int)params[3]){
        outStop=1;
      }
      optSeries[nowLoop][0]=nowLoop;
      optSeries[nowLoop][1]=optFit;
      optSeries[nowLoop][2]=inOptFit;     
      nowLoop++;
    }   
//    HmArray.printArray(optSeries);
    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes, dtimes, optSequence, machQty,rtimes);
    return sch;  
  }

/**利用遗传算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @param params 遗传算法的相关参数，0-迭代代数，1-种群规模，2-交叉概率，3-变异概率，5-无更新终止代数
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByGA(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params){
    double[][] sch;
    //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
    double[][] optSeries=new double[(int)params[0]][3];//
    
    //作业数量，也是代码长度
    int codeLength=ptimes.length;   
      //(1）遗传算法主程序   
    //算法相关参数设定
    int maxGeneration=(int)params[0],pop=(int)params[1],maxNoGoodLoop=(int)params[4];
    int nowGeneration=0,noGoodLoop=0;
    double crossRate=params[2],muteRate=params[3];
    //（2）初始化种群，编码从1开始编起
    // 增加EDD和FCFS两个规则生成排序
    int[] fcfsSeq=Rules.seqFCFS(rtimes);
    int[] eddSeq=Rules.seqEDD(dtimes);
    int[][] chromes=GA.initSequence1Chrome(pop, codeLength);
    //将EDD和FCFS规则生成的排序插入随机生成的排序
    chromes[1]=Arrays.copyOf(eddSeq, codeLength);
    chromes[0]=Arrays.copyOf(fcfsSeq, codeLength);
    chromes[4]=Arrays.copyOf(fcfsSeq, codeLength);
    chromes[7]=Arrays.copyOf(fcfsSeq, codeLength);
    chromes[8]=Arrays.copyOf(fcfsSeq, codeLength);    
    double[] nowFit =getSynRjSumLateTimes(chromes,ptimes,dtimes,rtimes,machQty);
    double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
    //排序获得最优解和最优适应度函数值
    int[] optSequence = new int[codeLength];//存储算法获得最优解是的作业排序
    double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
    System.arraycopy(chromes[0], 0, optSequence, 0, codeLength);    
    //算法循环主程序
    while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
      //step1: 选择
      chromes=GA.selectionElistMin(chromes, nowFit, 5);      
      //step2: 交叉
      chromes=GA.crossOX(chromes, crossRate);
      //step3: 变异
      chromes=GA.muteTwoPointReverse(chromes, muteRate);
      //计算交叉和变异后的染色体对应的适应度值
      nowFit =getSynRjSumLateTimes(chromes,ptimes,dtimes,rtimes,machQty);           
      for(int i=0;i<pop;i++){
        fitnessArr[i][0]=nowFit[i];
        fitnessArr[i][1]=i;
      }
      EasyMath.sortArray(fitnessArr, new int[] {0});
      double nowOptFit=fitnessArr[0][0];
      int nowOptId=(int) fitnessArr[0][1];
      //step4: 更新全局解
      
      if(nowOptFit>=optFitness){
        noGoodLoop++;
      }else{
        noGoodLoop=0;
        optFitness=nowOptFit;
        System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //复制最优染色体
      }
      optSeries[nowGeneration][0]=nowGeneration;
      optSeries[nowGeneration][1]=optFitness;
      optSeries[nowGeneration][2]=nowOptFit; 
      nowGeneration++;
    }
//    HmArray.printArray(optSeries);
    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes, dtimes, optSequence, machQty,rtimes);
    return sch;
    
  }

/**利用FCFS进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByFCFS(double[] ptimes,double[] dtimes,double[] rtimes,int machQty){
    double[][] sch;
    int[] fcfsSeq=Rules.seqFCFS(rtimes);
//    HmArray.printArray(fcfsSeq);
    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes, dtimes, fcfsSeq, machQty,rtimes);
    return sch;   
  }

/**利用FCFS+SPT（先到先服务的同时最短加工时间优先）进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByFSPT(double[] ptimes,double[] dtimes,double[] rtimes,int machQty){
    double[][] sch;
    int[] fcfsSeq=Rules.seqFSPT(ptimes,rtimes);
    sch=getSynPMSch(ptimes, dtimes, fcfsSeq, machQty,rtimes);
    return sch;   
  }
/**利用FCFS+LPT（先到先服务的同时最长加工时间优先）进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByFLPT(double[] ptimes,double[] dtimes,double[] rtimes,int machQty){
    double[][] sch;
    int[] fcfsSeq=Rules.seqFLPT(ptimes,rtimes);
    sch=getSynPMSch(ptimes, dtimes, fcfsSeq, machQty,rtimes);
    return sch;   
  }  
/**利用FCFS+EDD（先到先服务的同时最早交付时间优先）进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByFEDD(double[] ptimes,double[] dtimes,double[] rtimes,int machQty){
    double[][] sch;
    int[] fcfsSeq=Rules.seqFEDD(dtimes,rtimes);
    sch=getSynPMSch(ptimes, dtimes, fcfsSeq, machQty,rtimes);
    return sch;   
  }   
  
  
/**利用EDD进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByEDD(double[] ptimes,double[] dtimes,double[] rtimes,int machQty){
    double[][] sch;
    int[] eddSeq=Rules.seqEDD(dtimes);
    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes, dtimes, eddSeq, machQty,rtimes);
    return sch;
    
  }  
  
  /**利用蚁群算法进行同步启停单阶段并行机Pm|rj,syn|Lsum问题的优化求解
  * @param ptimes 作业时间
  * @param rtimes 作业的释放时间 
  * @param dtimes 作业的交付时间 
  * @param machQty 并行机数量 
  * @param params 遗传算法的相关参数，0-挥发系数，1-种群规模，2-迭代代数，3-无更新终止代数
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByACO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params){
    double[][] sch;
    //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
    double[][] optSeries=new double[(int)params[2]][3];//
    
    //作业数量，也是代码长度
    int codeLength=ptimes.length;   
      //(1）蚁群算法主程序   
    //算法相关参数设定
    int maxGeneration=(int)params[2],pop=(int)params[1],maxNoGoodLoop=(int)params[3];
    int nowGeneration=0,noGoodLoop=0;
    double rho=params[0];
    //（2）初始化种群，编码从1开始编起
    double[][] pherom=ACO.initPheromone(ptimes);
    int[][] chromes=ACO.createRoutes(pherom, pop);
    double[] nowFit =getSynRjSumLateTimes(chromes,ptimes,dtimes,rtimes,machQty);
    double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
    //排序获得最优解和最优适应度函数值
    int[] optSequence= new int[codeLength];//存储算法获得最优解是的作业排序
    double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
    for(int i=0;i<codeLength;i++){
      optSequence[i]=chromes[0][i];      
    }    
    //算法循环主程序
    while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
      //step1: 产生新解
      chromes=ACO.createRoutes(pherom, pop); 
      //step2: 判断是否有更好的解
      //计算新解对应的适应度值
      nowFit =getSynRjSumLateTimes(chromes,ptimes,dtimes,rtimes,machQty);           
      for(int i=0;i<pop;i++){
        fitnessArr[i][0]=nowFit[i];
        fitnessArr[i][1]=i;
      }
      EasyMath.sortArray(fitnessArr, new int[] {0});     
      double nowOptFit=fitnessArr[0][0];
      int nowOptId=(int) fitnessArr[0][1];
      //step3: 更新全局解      
      if(nowOptFit>=optFitness){
        noGoodLoop++;
      }else{
        noGoodLoop=0;
        optFitness=nowOptFit;
        System.arraycopy(chromes[nowOptId], 0, optSequence, 0, codeLength); //染色体编码是从0开始编的，但是排序从1开始编起
      }
      //step4:更新信息素
      pherom=ACO.updatePheromone(pherom, optSequence, optFitness, rho);     
      optSeries[nowGeneration][0]=nowGeneration;
      optSeries[nowGeneration][1]=optFitness;
      optSeries[nowGeneration][2]=nowOptFit; 
      nowGeneration++;
    }
    //将搜索曲线存入数据表
    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes, dtimes, optSequence, machQty,rtimes);
    return sch;
    
  }  
  
  /**利用粒子群算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量  
  * @param params 遗传算法的相关参数，0-速度惯性系数，1-个体学习因子，2-社会学习因子，3-种群规模，4-迭代代数，5-无更新终止代数
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByPSO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params){
    double[][] sch;
    //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
    double[][] optSeries=new double[(int)params[4]][3];//
    
    //作业数量，也是代码长度
    int codeLength=ptimes.length;   
      //(1）粒子群算法主程序   
    //算法相关参数设定
    int maxGeneration=(int)params[4],pop=(int)params[3],maxNoGoodLoop=(int)params[5];
    int nowGeneration=0,noGoodLoop=0;
    //（2）初始化种群，编码从0开始编起
    double[][] X=PSO.initX(pop, codeLength);
    double[][] V=PSO.initV(pop, codeLength); 
    int[][] intXs=PSO.parseInt(X);
    double[] nowFit =getSynRjSumLateTimes(intXs,ptimes,dtimes,rtimes,machQty);
    double[] singleBestFit=getSynRjSumLateTimes(intXs,ptimes,dtimes,rtimes,machQty);//粒子截止当前最优解
    double[][] singleBestFitX=X;//粒子截止当前最优解对应的变量值
    int idx=PSO.getMinFitIdx(nowFit);
    double groupBestFit=nowFit[idx];//种群截止当前最优解
    double[] groupBestFitX=new double[codeLength];//种群截止当前最优解对应的变量值
    System.arraycopy(X[idx], 0, groupBestFitX, 0, codeLength);     
    //算法循环主程序
    while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
      //step1: 更新速度
      V=PSO.updateV(X, V, singleBestFitX, groupBestFitX, params);      
      //step2: 更新变量值
      X=PSO.updateX(X, V, singleBestFitX, groupBestFitX, params);  
      //step3: 计算每个粒子的最好解和变量值
      intXs=PSO.parseInt(X);
      nowFit =getSynRjSumLateTimes(intXs,ptimes,dtimes,rtimes,machQty); 
      //依次判断是否为最好解
      noGoodLoop++;
      for(int i=0;i<pop;i++){
        if(nowFit[i]<singleBestFit[i]){
          singleBestFit[i]=nowFit[i];
          System.arraycopy(X[i],0,singleBestFitX[i],0,codeLength);
        }
        if(nowFit[i]<groupBestFit){
          groupBestFit=nowFit[i];
          System.arraycopy(X[i],0,groupBestFitX,0,codeLength);
          noGoodLoop=0;
        }        
      }
      //排序获得当前粒子群最优解
      int id=PSO.getMinFitIdx(nowFit);
      optSeries[nowGeneration][0]=nowGeneration;
      optSeries[nowGeneration][1]=groupBestFit;
      optSeries[nowGeneration][2]=nowFit[id]; 
      nowGeneration++;
    }

    //对最优解生成详细调度并返回
    int[] intX=PSO.parseInt(groupBestFitX);
    sch=getSynPMSch(ptimes,dtimes,intX,machQty,rtimes);
    return sch;
    
  }  
  /**利用教学算法进行同步启停单阶段并行机Pm|rj,syn|Lmax问题的优化求解
  * @param ptimes 作业时间
  * @param dtimes 作业的交付时间
  * @param rtimes 作业的释放时间
  * @param machQty 并行机数量   
  * @param params 遗传算法的相关参数，0-学生数量[种群规模]，1-迭代代数，2-无更新终止代数
  * @return 返回调度排序的二维数组，共十一列，第一列：作业编号，第二类：机器编号，第三列：工序编号
  *         第四列：开工时间，第五列：完工时间，第六列：交付时间，第七列：释放时间，第八列：setup开始，第九列：setup结束，
  *         第十列：setup时长，第十一列：同步开工的组号【同步启停并行机中，有组号为同步开工，0为不能同步开工】
  */
  public double[][] optSynRjLsumByTLBO(double[] ptimes,double[] dtimes,double[] rtimes,int machQty,double[] params){
    double[][] sch; 
    //作业数量，也是代码长度
    int jobQty=ptimes.length;   
      //(1）蚁群算法主程序   
    //算法相关参数设定
    int maxGeneration=(int)params[1],pop=(int)params[0],maxNoGoodLoop=(int)params[2];
    int nowGeneration=0,noGoodLoop=0;    
    //（2）初始化班级科目成绩
    double[][] scores=TLBO.initClass(pop, jobQty);
    int[] fcfsSeq=Rules.seqFCFS(rtimes); 
    //double num=1/jobQty;
    for(int i=0;i<jobQty;i++){
      int idx=fcfsSeq[i]-1;
      scores[1][idx]=i/jobQty;
      scores[0][idx]=i/jobQty;
      scores[4][idx]=i/jobQty;
      scores[7][idx]=i/jobQty;
      scores[8][idx]=i/jobQty;        
    }
    
    int[][] sequences=TLBO.getSequenceFromScores(scores);
    double[] nowFit =getSynRjSumLateTimes(sequences,ptimes,dtimes,rtimes,machQty);
    double[][] fitnessArr = new double[pop][2];//存储染色体对应解及其编号，0-解的值，1-解的编号
    //=======================排序获得最优解和最优适应度函数值
    int[] optSequence = new int[jobQty];//存储算法获得最优解时的作业排序
    double optFitness=nowFit[0];//存储算法获得的最小最大延误时长，算法开始就选初始种群中的第一条染色体的值作为最优解
    System.arraycopy(sequences[0], 0, optSequence, 0, jobQty); 
    
    //每一代的收敛曲线，0-代数，1-迄今为止最优目标函数值，2-当前代的最优解
    double[][] optSeries=new double[maxGeneration][3];//  
    
    double[] meanScores;//存储循环计算过程中的各门课程的平均成绩 
    double[] bestScores;//存储循环计算过程中班级同学综合评价最好的同学的各科成绩     
    //算法循环主程序
    while(noGoodLoop<maxNoGoodLoop&&nowGeneration<maxGeneration){
      //step1: 找出综合评价最好的学生及其各科成绩、各个科目的平均得分
      sequences=TLBO.getSequenceFromScores(scores);
      nowFit =getSynRjSumLateTimes(sequences,ptimes,dtimes,rtimes,machQty);   
      for(int i=0;i<pop;i++){
        fitnessArr[i][0]=nowFit[i];
        fitnessArr[i][1]=i;
      }
      EasyMath.sortArray(fitnessArr, new int[] {0});  //排序，第一个解为最好解   
      double nowOptFit=fitnessArr[0][0];
      int nowOptId=(int) fitnessArr[0][1];      
      meanScores=TLBO.getMeanScores(scores);
      bestScores=Arrays.copyOf(scores[nowOptId],jobQty);    
      //step2-1:进行全局最优解的处理，虽然逻辑流程将这个第二步放在最后，但是考虑到少计算一个最优解的过程，
      //       所以将全局解更新放在这里
    
      if(nowOptFit>=optFitness){
        noGoodLoop++;
      }else{
        noGoodLoop=0;
        optFitness=nowOptFit;
        System.arraycopy(sequences[nowOptId], 0, optSequence, 0, jobQty); 
      }    
      //step2-2:更新适应度曲线数据  
      optSeries[nowGeneration][0]=nowGeneration;
      optSeries[nowGeneration][1]=optFitness;
      optSeries[nowGeneration][2]=nowOptFit; 

      
      //step3: 教师学习阶段，对每个学生的科目成绩进行更新  
      double[][] newScores=TLBO.updateScores(scores, bestScores, meanScores);
      int[][] newSeq=TLBO.getSequenceFromScores(newScores);
      double[] newFits =getSynRjSumLateTimes(newSeq,ptimes,dtimes,rtimes,machQty);      
      for(int i=0;i<pop;i++){
        if(newFits[i]<nowFit[i]){
          scores[i]=Arrays.copyOf(newScores[i],jobQty);
        }
      }
      //step4:学生学习阶段
      int[] twoStudentIds=TLBO.getTwoDiffNum(pop);
      double[][] twoScores=new double[2][jobQty];
      for(int i=0;i<2;i++){//获得两个学生的科目成绩
        twoScores[i]=Arrays.copyOf(scores[twoStudentIds[i]],jobQty);
      }
      int[][] twoSeq=TLBO.getSequenceFromScores(twoScores);
      double[] twoFits =getSynRjSumLateTimes(twoSeq,ptimes,dtimes,rtimes,machQty);  
      boolean isgood=twoFits[0]<twoFits[1];
      double[][] twoNewScores=TLBO.updateOneScore(twoScores, isgood);
      int[][] twoNewSeq=TLBO.getSequenceFromScores(twoNewScores);
      double[] twoNewFits =getSynRjSumLateTimes(twoNewSeq,ptimes,dtimes,rtimes,machQty);
      if(twoNewFits[0]<twoFits[0]){
        scores[twoStudentIds[0]]=Arrays.copyOf(twoNewScores[0],jobQty);
      }
      //学生学习阶段结束
      nowGeneration++;
    }

    //对最优解生成详细调度并返回
    sch=getSynPMSch(ptimes,dtimes,optSequence,machQty,rtimes);
    return sch;
    
  }  
  
  
}
