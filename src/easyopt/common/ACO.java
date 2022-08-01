/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 蚁群算法运算过程中涉及的相关方法
 * @author PeterWang at WOR
 */
public class ACO {

/**
 * 初始化信息素，针对jsp问题
 * @param procData 作业的工艺数据二维数组，行为作业、【2i-1】列为工序i所用机器编码，【2i】列为工序i所用时间
 * @return 信息素二维数组，行列长度均为作业数量+2，因为各个作业编码表示路径中的节点，
 * 还需要添加一个起点0和终点0表示蚂蚁的巢穴
 */
public static double[][] initPheromone2(double[][] procData){
  int jobQty=procData.length;
  int machQty=procData[0].length/2;
  int pheromQty=jobQty*machQty+2;

  double[][] pherom=new double[pheromQty][pheromQty];
  for(int i=0;i<pheromQty;i++){
    for(int j=0;j<pheromQty;j++){
      pherom[i][j]=1.0/jobQty;
    }
  }   
  return pherom;
}  
  
/**
 *初始化信息素
 * @param ptimes 各个作业在各道工序的加工时间，行为作业数量，列为设备数量
 * @return 信息素二维数组，行列长度均为作业数量+2，因为各个作业编码表示路径中的节点，
 * 还需要添加一个起点0和终点0表示蚂蚁的巢穴
 */
public static double[][] initPheromone(double[][] ptimes){
  int jobQty=ptimes.length;
  int pheromQty=jobQty+2;
  double[][] pherom=new double[pheromQty][pheromQty];
  for(int i=0;i<pheromQty;i++){
    for(int j=0;j<pheromQty;j++){
      pherom[i][j]=1.0/jobQty;
    }
  }   
  return pherom;
}

/**
 *初始化信息素
 * @param ptimes 单阶段并行机调度中各个作业的作业时间
 * @return 信息素二维数组，行列长度均为作业数量+2，因为各个作业编码表示路径中的节点，
 * 还需要添加一个起点0和终点0表示蚂蚁的巢穴
 */
public static double[][] initPheromone(double[] ptimes){
  int jobQty=ptimes.length;
  int pheromQty=jobQty+2;
  double[][] pherom=new double[pheromQty][pheromQty];
  for(int i=0;i<pheromQty;i++){
    for(int j=0;j<pheromQty;j++){
      pherom[i][j]=1.0/jobQty;
    }
  }   
  return pherom;
}
 
/**
 *根据当前点间信息素强度、截止目前最好解对应的路径、截止当前最好解的值和信息素挥发系数更新点间的信息素
 * @param pherom 作业之间链接弧的信息素，行列相等，均为节点数量+2
 * @param bestRoute 迭代过程中获得的最优解，一维数组，数组个数为为节点数量，【注意】编号从1开始
 * @param bestFit 最优解的值
 * @param rho 信息素挥发系数
 * @return 信息素二维数组，行列均为节点数量+2
 */
public static double[][] updatePheromone(double[][] pherom,int[] bestRoute,double bestFit,double rho){
  int pheromQty=pherom.length;
  int[] moreBestRoute=new int[pheromQty];
  System.arraycopy(bestRoute,0, moreBestRoute, 1,pheromQty-2);
  moreBestRoute[0]=0;
  moreBestRoute[pheromQty-1]=0;  

  //至此，最优路径的编号从0开始
  double[][] newPherom=new double[pheromQty][pheromQty];
  for(int i=0;i<pheromQty;i++){
    for(int j=0;j<pheromQty;j++){
        boolean isInBest=false;
      //判断节点对(i,j)是否在最优路径中
      for(int k=0;k<pheromQty-1;k++){
        if(moreBestRoute[k]==i&&moreBestRoute[k+1]==j){
          isInBest=true;
          break;        
        }
      }
//      rho=0.1;
      if(isInBest){
        newPherom[i][j]=(1-rho)*pherom[i][j]+rho/bestFit*1000;    
      }else{
        newPherom[i][j]=(1-rho)*pherom[i][j];
      }   
//      if(isInBest){
//        newPherom[i][j]=pherom[i][j]+rho/bestFit;    
//      }else{
//        newPherom[i][j]=pherom[i][j];
//      }      
    }  
  } 
  return newPherom;
}




/**
 *根据信息素即蚂蚁行走选择规则生成一条随机的路径，若信息素有N行N列，其中除了起点和终点【一般为蚂蚁巢穴】，
 *则路径中节点编号为从1到N-2【节点数量】之间的整数值，路径为这些整数的一种排序
 * @param pherom 作业之间链接弧的信息素，行列相等，均为N=节点数量+2
 * @return 一条根据信息素强度形成的随机路径节点排序【排序起点和终点编号0】，长度为N-2，内容为1到N-2之间自然数的一种排序
 */
public static int[] createRoute(double[][] pherom){
  int jobQty=pherom.length-2;
  int[] finalRoute=new int[jobQty+2];
  int[] minRoute=new int[jobQty];
  List<Integer> remainCities=new ArrayList<Integer>(); //剩余节点集合
  for(int i=1;i<=jobQty;i++){
    remainCities.add(i);
  }
  finalRoute[0]=0;
  //下面依次安排第1到第jobQty个点
  for(int i=1;i<=jobQty;i++){
    int preId=finalRoute[i-1];  //已排路径中的最后一个点编码
    //计算前一个点preId到其他剩余点之间的总的信息素
    double sumPhero=0;
    int remainQty=remainCities.size();
    for(int j=0;j<remainQty;j++){
      sumPhero+=pherom[preId][(int)remainCities.get(j)];
    }
    //计算各条可行弧上的概率
    double[] prop=new double[remainQty];
    double sumProp=0;
    for(int j=0;j<remainQty;j++){
      prop[j]=pherom[preId][(int)remainCities.get(j)]/sumPhero;
      sumProp+=prop[j];
    } 
    //概率归一化处理
    for(int j=0;j<remainQty;j++){
      prop[j]=prop[j]/sumProp;
    }
    //System.out.println("===========================================");
    //转换为累计概率
    for(int j=1;j<remainQty;j++){
      prop[j]+=prop[j-1];
    }
    //HmArray.printArray(prop);    
    //选择下一节点
    double randVal=Math.random();
    int getId=0;
    for(int j=0;j<remainQty;j++){
      if(randVal<=prop[j]){
        getId=j;
        break;
      }
    }
    //System.out.println(getId+"  "+remainCities.get(getId));
    //进行接的更新
    finalRoute[i]=(int)remainCities.get(getId);
    remainCities.remove(getId);   
  }
  //将路径设置为1开始
  System.arraycopy(finalRoute,1, minRoute, 0, jobQty);  
  return minRoute;
}


/**
 *根据信息素和蚂蚁数量为每只蚂蚁构建一条其行走路径，从而形成多条路径的二维数组
 * @param pherom 作业之间链接弧的信息素，行列相等，均为N=节点数量+2
 * @param pop 蚂蚁数量
 * @return 多条路径，每行代表一只蚂蚁的当前路径，每条路径内容为1到节点数量【N-2】之间的整数排序
 */
public static int[][] createRoutes(double[][] pherom,int pop){
  int jobQty=pherom.length-2;
  int[][] finalRoute=new int[pop][jobQty];
  for(int i=0;i<pop;i++){
    finalRoute[i]=ACO.createRoute(pherom);
  }
  return finalRoute;
}


}
