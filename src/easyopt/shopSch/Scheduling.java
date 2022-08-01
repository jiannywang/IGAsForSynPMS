package easyopt.shopSch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import easyopt.model.Order;
import easyopt.model.Process;
import easyopt.model.Task;

/**
 * 主要是复杂车间【混合流水车间、作业车间】调度中涉及的方法集合类，例如初始化订单列表和任务列表，或在运算中进行订单或任务列表处理的方法
 * @author PeterWang, ShowClean Optimization Corporation.
 * @version 1.0
 */
public class Scheduling {

  /** 根据作业工时和每道工序机器数量初始化混合流水车间的订单对象列表
   * @param ptimes 车间作业的作业工时数据，每一行为一个作业的工时，每一列为对应行该工序的作业时间【行-作业，列-工序】，
   * @param machQtys 各道工序所具备的全部机器数量，一维整数数组，主要用于控制全部机器的编号，最后形成的机器编号从0开始；
   * @return 返回一个初始订单对象列表，包含了每个作业的详细工艺数据，订单编号和机器编号都从0开始
  */
  public static List<Order> initOrderListHFSP(double[][] ptimes,int[] machQtys){
    List<Order> initOrders=new ArrayList<>();
    int orderQty=ptimes.length;
    int procQty=ptimes[0].length;
    int[] procStartIdx=new int[procQty];//每道工序开始机器的编号，从0开始编号
    for(int i=1;i<procQty;i++){
      procStartIdx[i]=procStartIdx[i-1]+machQtys[i-1];
    }    
    for(int i=0;i<orderQty;i++){
      Order or=new Order();
      or.setOrderId(i);
      List<Process> procLists=new ArrayList<>();
      for(int j=0;j<procQty;j++){
        Process pr=new Process();
        int[] myMachIds=new int[machQtys[j]];
        for(int k=0;k<machQtys[j];k++){
          myMachIds[k]=procStartIdx[j]+k;
        }
        pr.setMachIndex(myMachIds);
        pr.setMachQty(machQtys[j]);
        pr.setProcessId(j);
        pr.setCycleTime(ptimes[i][j]);
        procLists.add(pr);
      }
      or.setProcessList(procLists);//将该工序的详细信息列表加入当前订单对象
      initOrders.add(or);//将当前订单对象加入初始化订单列表
    }
    return initOrders;
  }

  /** 根据作业工时初始化作业车间调度问题中的订单对象列表
   * @param procData 作业的工艺数据二维数组，行为作业、【2i-1】列为工序i所用机器编码，【2i】列为工序i所用时间
   * @return 返回一个初始订单对象列表，包含了每个作业的详细工艺数据
  */
  public static List<Order> initOrderListJSP(double[][] procData){
    List<Order> initOrders=new ArrayList<>();
    int orderQty=procData.length;
    int procQty=procData[0].length/2;  
    for(int i=0;i<orderQty;i++){
      Order or=new Order();
      or.setOrderId(i);
      List<Process> procLists=new ArrayList<>();
      for(int j=0;j<procQty;j++){
        Process pr=new Process();
        int[] myMachIds=new int[1];
        myMachIds[0]=(int)procData[i][2*j]-1;
        pr.setMachIndex(myMachIds);
        pr.setMachQty(1);
        pr.setProcessId(j);
        pr.setCycleTime(procData[i][2*j+1]);
        procLists.add(pr);
      }
      or.setProcessList(procLists);//将该工序的详细信息列表加入当前订单对象
      initOrders.add(or);//将当前订单对象加入初始化订单列表
    }
    return initOrders;
  }
 

  /** 根据作业车间调度的工艺参数【procData】获取每个作业各道工序所用设备编码，设备编码从0开始，
   *  同一个作业同一个工序只有一台机器
   * @param procData 作业的工艺数据二维数组，行为作业、【2i-1】列为工序i所用机器编码，【2i】列为工序i所用时间
   * @return 返回各个作业每道工序所使用的设备编码，二维向量，每一行表示一项作业各工序的设备
  */
  public static int[][] getProcMachIds(double[][] procData){
    int orderQty=procData.length;
    int procQty=procData[0].length/2;  
    int[][] machIds=new int[orderQty][procQty];
    for(int i=0;i<orderQty;i++){
      for(int j=0;j<procQty;j++){
        machIds[i][j]=(int)procData[i][2*j]-1;
      }
    }
    return machIds;
  }
  
  /** 根据作业车间调度的工艺参数和排序获得排序中每个位置作业的工序号，工序编号从0开始编码
   * @param procData 作业的工艺数据二维数组，行为作业、【2i-1】列为工序i所用机器编码，从1开始编码，
   *                            【2i】列为工序i所用时间
   * @param seqByMach 每台机器上开展工作的排序对应的作业编号，行为机器数量，列为作业塑料，作业编码从1开始
   * @return 返回机器排序对应的作业工序编码，二维向量，每一行表示一台设备上前后作业对应的在该设备上的
   * 工序编号，返回工序号从0开始编号
  */
  public static int[][] getSeqJobProcIds(double[][] procData,int[][] seqByMach){
    int orderQty=procData.length;
    int procQty=procData[0].length/2; 
    int[][] procMachIds=getProcMachIds(procData);//获得各项作业每道工序对应的机器编码，从0开始
    int[][] seqJobProcIds=new int[procQty][orderQty];//存储从seqByMach转换过来的各设备操作对应的作业的工序编码
    for(int i=0;i<procQty;i++){//对应machId
      for(int j=0;j<orderQty;j++){
        //对seqByMach[i][j]中的数字进行判定并填充seqJobProcIds[i,j]
        int nowJobId=seqByMach[i][j]-1;//该操作的作业编号，从0开始
        for(int k=0;k<procQty;k++){
          if(procMachIds[nowJobId][k]==i){
            seqJobProcIds[i][j]=k;
          }
        }
      }
    }
    return seqJobProcIds;
  }  
  
  /** 根据作业车间调度的工艺参数获取每个作业各道工序所用作业时间
   * @param procData 作业的工艺数据二维数组，行为作业、【2i-1】列为工序i所用机器编码，【2i】列为工序i所用时间
   * @return 返回各个作业每道工序所使用的加工时间，二维向量，每一行表示一项作业各工序的作业时间
  */
  public static double[][] getProcTimes(double[][] procData){
    int orderQty=procData.length;
    int procQty=procData[0].length/2;  
    double[][] pTimes=new double[orderQty][procQty];
    for(int i=0;i<orderQty;i++){
      for(int j=0;j<procQty;j++){
        pTimes[i][j]=procData[i][2*j+1];
      }
    }
    return pTimes;
  }  
    
  
  /** 根据作业工时、每道工序机器数量和作业释放时间初始化混合流水车间的订单对象列表
   * @param ptimes 车间作业的作业工时数据，每一行为一个作业的工时，每一列为对应行该工序的作业时间【行-作业，列-工序】，
   * @param machQtys 各道工序所具备的全部机器数量，一维整数数组，主要用于控制全部机器的编号，最后形成的机器编号从0开始；
   * @param rtimes 每项作业的释放时间
   * @return 返回一个初始订单对象列表，包含了每个作业的详细工艺数据，订单编号和机器编号都从0开始
  */
  public static List<Order> initOrderListHFSP(double[][] ptimes,int[] machQtys,double[] rtimes){
    List<Order> initOrders=new ArrayList<>();
    int orderQty=ptimes.length;
    int procQty=ptimes[0].length;
    int[] procStartIdx=new int[procQty];//每道工序开始机器的编号，从0开始编号
    for(int i=1;i<procQty;i++){
      procStartIdx[i]=procStartIdx[i-1]+machQtys[i-1];
    }    
    for(int i=0;i<orderQty;i++){
      Order or=new Order();
      or.setOrderId(i);
      or.setReleaseTime(rtimes[i]);
      List<Process> procLists=new ArrayList<>();
      for(int j=0;j<procQty;j++){
        Process pr=new Process();
        int[] myMachIds=new int[machQtys[j]];
        for(int k=0;k<machQtys[j];k++){
          myMachIds[k]=procStartIdx[j]+k;
        }
        pr.setMachIndex(myMachIds);
        pr.setMachQty(machQtys[j]);
        pr.setProcessId(j);
        pr.setCycleTime(ptimes[i][j]);
        procLists.add(pr);
      }
      or.setProcessList(procLists);//将该工序的详细信息列表加入当前订单对象
      initOrders.add(or);//将当前订单对象加入初始化订单列表
    }
    return initOrders;
  }

  /** 根据作业工时、作业释放时间、交付时间初始化并行机调度问题中的订单对象列表
   * @param ptimes 并行机调度中每项作业的加工时间，为一维向量，长度为作业的数量，
   * @param rtimes 每项作业的释放时间，为一维向量，长度为作业的数量
   * @param dueTimes 每项作业的交付时间，为一维向量，长度为作业的数量
   * @return 返回一个初始订单对象列表，包含了每个作业的详细时间数据
  */
  public static List<Order> initOrderListPMS(double[] ptimes,double[] rtimes,double[] dueTimes){
    List<Order> initOrders=new ArrayList<>();
    int orderQty=ptimes.length;  
    for(int i=0;i<orderQty;i++){
      Order or=new Order();
      or.setOrderId(i);
      or.setReleaseTime(rtimes[i]);
      or.setDueTime(dueTimes[i]);
      or.setProcTime(ptimes[i]);
      initOrders.add(or);//将当前订单对象加入初始化订单列表
    }
    return initOrders;
  }
  
    /** 根据作业工时、作业释放时间初始化并行机调度问题中的订单对象列表
   * @param ptimes 并行机调度中每项作业的加工时间，为一维向量，长度为作业的数量，
   * @param rtimes 每项作业的释放时间，为一维向量，长度为作业的数量
   * @return 返回一个初始订单对象列表，包含了每个作业的详细时间数据
  */
  public static List<Order> initOrderListPMS(double[] ptimes,double[] rtimes){
    List<Order> initOrders=new ArrayList<>();
    int orderQty=ptimes.length;  
    for(int i=0;i<orderQty;i++){
      Order or=new Order();
      or.setOrderId(i);
      or.setReleaseTime(rtimes[i]);
      or.setProcTime(ptimes[i]);
      initOrders.add(or);//将当前订单对象加入初始化订单列表
    }
    return initOrders;
  }
    
  
  /** 根据maxTime对全部machQty安排一个gap从0开始到maxTime结束，作业从maxTime到maxTime+1结束的任务，
   * 形成调度问题的初始任务列表
   * @param machQty 车间调度问题中全部机器的数量
   * @param maxTime 调度问题最后完工时间不可能超过的数值
   * @return 返回一个初始任务列表，包含了每个机器一个任务
  */
  public static List<Task> initTaskList(int machQty,double maxTime){
    List<Task> initTasks=new ArrayList<>();
    for(int i=0;i<machQty;i++){
      Task tk=new Task(i,maxTime,maxTime+1,-1);
      tk.setGap(maxTime);
      tk.setGapStartTime(0);
      tk.setGapEndTime(maxTime);
      initTasks.add(tk);
    }
    return initTasks;
  }
  
  /**按工序进行任务列表的更新：根据订单的排序【seq】和已经排定的任务列表【inTaskList】，将全部订单当前工序【procNo】的作业任务排定到任务列表中，从而形成新的任务列表。
     *  具体来说，在多工序排产运算中，需要根据工序依次对全部订单进行排定，即每个订单每个工序的任务排定在某台机器的特定时段：指定开工时间和完工时间。
    *  
   * @param seq 订单的顺序列表，从0开始至【订单数量-1】为止的整数数组
   * @param procNo 当前排产的工序编号，从0开始编号
   * @param inOrderList 待排产全部订单的对象列表，【作业对象列表按照订单编号升序排序】
   * @param inTaskList 已排产任务的对象列表，若某一机器设备m没有安排任务，则需要给m设备安排一个前面gap无穷大的Task
   * @return 将inOrderList全部排入inTaskList之后形成的新排产任务对象列表
   **/
  public static List<Task> getJobSchedule(int[] seq,int procNo,List<Order> inOrderList,List<Task> inTaskList){
    //基本思路是根据seq中各个作业出现的先后顺序，从inOrderList中找到对应编号的作业noOrder，
    //然后再找出该作业中工序编号为procNo的工序列表，该列表中有能够使用的机器编号集合machSet和作业时间cycleTime
    //再从inTaskList中找出machSet中各个机器的任务列表，然后按照时间升序排序，再然后找出最先能够安排下该作业的机器
    //生成新的任务加入原任务列表
    List<Task> tk;
    int jobQty=seq.length;
    for(int i=0;i<jobQty;i++){
      int nowOrderId=seq[i];//订单编号
      Order nowOrd=inOrderList.get(seq[i]);//依照seq中的订单顺序，找出对应的订单对象
      if(nowOrd.getOrderId()!=nowOrderId){
        System.out.println("error is happenning in Scheduling.getJobSchedule....");
      }else{
        //查看该订单在任务列表中是否存在，如果不存在其可开工时间即为0，否则为任务列表中的最大值
        double myPreEndTime=nowOrd.getReleaseTime();
        int taskQty=inTaskList.size();
        for(int j=0;j<taskQty;j++){
          if(inTaskList.get(j).getOrderId()==nowOrderId){
            myPreEndTime=Math.max(myPreEndTime,inTaskList.get(j).getEndTime());
          }
        }
        //准备给该作业安排个机器
        List<Process> procList=nowOrd.getProcessList();//获得该订单的全部工序信息列表
        int procQty=procList.size();
        for(int j=0;j<procQty;j++){//找到该订单该工序的信息，并进行排产
          if(procList.get(j).getProcessId()==procNo){
            Process nowProc=procList.get(j);
            int[] myMachs=nowProc.getMachIndex();//获得该订单对应工序的全部可用机器设备编号集合
            List<Task> nowTaskList=new ArrayList<>();
            for(int k=0;k<nowProc.getMachQty();k++){//从已排任务列表中将该工序涉及到的机器的任务全提取出来
              int nowMachId=myMachs[k]; 
              taskQty=inTaskList.size();
              for(int h=taskQty-1;h>=0;h--){
                if(inTaskList.get(h).getMachId()==nowMachId){
                  nowTaskList.add(inTaskList.get(h));
                  inTaskList.remove(h);
                }
              }
            }//提取结束
            //对nowTaskList按照gapStartTime升序排序
                //System.out.println(nowTaskList.toString());
            nowTaskList.sort(Comparator.comparingDouble(Task::getGapStartTime));
            //然后查找能够最早安排该作业的任务区间，将作业安排进去
            int nowTaskSize=nowTaskList.size();
            for(int t=0;t<nowTaskSize;t++){
              double canStartTime=Math.max(myPreEndTime,nowTaskList.get(t).getGapStartTime());
              int machId=nowTaskList.get(t).getMachId();
              double canEndTime=canStartTime+nowProc.getCycleTime();
              if(canEndTime<=nowTaskList.get(t).getGapEndTime()){//可以排产到该任务的空闲区间
                  Task midTk=new Task(machId,canStartTime,canEndTime,nowOrderId);
                  midTk.setProcessId(j);
                  midTk.setGapStartTime(nowTaskList.get(t).getGapStartTime());
                  midTk.setGapEndTime(canStartTime);
                  midTk.setGap(midTk.getGapEndTime()-midTk.getGapStartTime()); 
                  midTk.setReleaseTime(nowOrd.getReleaseTime());
                  //新的任务对象设定完毕，等将旧的任务对象改变之后，将该任务对象添加到nowTaskList中
                  //下面对原有任务列表进行更新，主要是空闲的gap进行更新
                  nowTaskList.get(t).setGapStartTime(canEndTime);
                  nowTaskList.get(t).setGap(nowTaskList.get(t).getStartTime()-canEndTime);
                  //将midTk加入到nowTaskList中
                  nowTaskList.add(midTk); 
                  break;//安排结束，跳出for循环
              } 
            }
            //将nowTaskList加入到inTaskList中，以便下一作业进行任务安排
            for(int k=0;k<nowTaskList.size();k++){
              inTaskList.add(nowTaskList.get(k));
            }
            break;//调度当前作业的工序循环查找
          }
        }
      }
    }
    tk=inTaskList;
    return tk;
  }
 
  /**按订单进行任务列表的更新：将订单列表【inOrderList】中的指定订单【orderId】排入到既有任务列表【inTaskList】以形成新的任务列表。
   * 
   * @param orderId 待排产订单的编号，从0开始编号
   * @param inOrderList 待排产全部订单的对象列表，【作业对象列表按照订单编号升序排序】
   * @param inTaskList 已排产任务的对象列表，若某一机器设备m没有安排任务，则需要给m设备安排一个前面gap无穷大的Task
   * @return 将inOrderList全部排入inTaskList之后形成的新排产任务对象列表
   **/
  public static List<Task> getJobSchedule(int orderId,List<Order> inOrderList,List<Task> inTaskList){
    //基本思路是根据seq中各个作业出现的先后顺序，从inOrderList中找到对应编号的作业noOrder，
    //然后再找出该作业中工序编号为procNo的工序列表，该列表中有能够使用的机器编号集合machSet和作业时间cycleTime
    //再从inTaskList中找出machSet中各个机器的任务列表，然后按照时间升序排序，再然后找出最先能够安排下该作业的机器
    //生成新的任务加入原任务列表
    List<Task> tk;
    Order nowOrd=inOrderList.get(orderId);//找出对应的订单对象
    if(nowOrd.getOrderId()!=orderId){
      System.out.println("error is happenning in Scheduling.getJobSchedule....");
    }else{
      //找到该订单前一操作的完工时间
      double myPreEndTime=Math.max(nowOrd.getReleaseTime(),nowOrd.getPreEndTime());
      //找出该订单该操作对应所用的机器编码
      int myProcId=nowOrd.getNextProcId();//拟排产的工序编码，从0开始编码
      List<Process> procList=nowOrd.getProcessList();//获得该订单的全部工序信息列表
      Process nowProc=procList.get(myProcId);
      int[] myMachs=nowProc.getMachIndex();//获得该订单对应工序的全部可用机器设备编号集合
      List<Task> nowTaskList=new ArrayList<>();
      for(int k=0;k<nowProc.getMachQty();k++){//从已排任务列表中将该工序涉及到的机器的任务全提取出来
        int nowMachId=myMachs[k]; 
        int taskQty=inTaskList.size();
        for(int h=taskQty-1;h>=0;h--){
          if(inTaskList.get(h).getMachId()==nowMachId){
            nowTaskList.add(inTaskList.get(h));
            inTaskList.remove(h);
          }
        }
      }//提取结束
      //对nowTaskList按照gapStartTime升序排序
          //System.out.println(nowTaskList.toString());
      nowTaskList.sort(Comparator.comparingDouble(Task::getGapStartTime));
      //然后查找能够最早安排该作业的任务区间，将作业安排进去
      int nowTaskSize=nowTaskList.size();
      for(int t=0;t<nowTaskSize;t++){
        double canStartTime=Math.max(myPreEndTime,nowTaskList.get(t).getGapStartTime());
        int machId=nowTaskList.get(t).getMachId();
        double canEndTime=canStartTime+nowProc.getCycleTime();
        if(canEndTime<=nowTaskList.get(t).getGapEndTime()){//可以排产到该任务的空闲区间
            Task midTk=new Task(machId,canStartTime,canEndTime,orderId);
            midTk.setProcessId(myProcId);
            midTk.setGapStartTime(nowTaskList.get(t).getGapStartTime());
            midTk.setGapEndTime(canStartTime);
            midTk.setGap(midTk.getGapEndTime()-midTk.getGapStartTime()); 
            midTk.setReleaseTime(nowOrd.getReleaseTime());
            //新的任务对象设定完毕，等将旧的任务对象改变之后，将该任务对象添加到nowTaskList中
            //下面对原有任务列表进行更新，主要是空闲的gap进行更新
            nowTaskList.get(t).setGapStartTime(canEndTime);
            nowTaskList.get(t).setGap(nowTaskList.get(t).getStartTime()-canEndTime);
            //将midTk加入到nowTaskList中
            nowTaskList.add(midTk); 
            //更新订单列表中该订单的信息
            inOrderList.get(orderId).setNextProcId(myProcId++);
            inOrderList.get(orderId).setPreEndTime(canEndTime);              
            break;//安排结束，跳出for循环
        } 
      }
      //将nowTaskList加入到inTaskList中，以便下一作业进行任务安排
      for(int k=0;k<nowTaskList.size();k++){
        inTaskList.add(nowTaskList.get(k));
      }
    }
    tk=inTaskList;
    return tk;
  }
    
  
}
