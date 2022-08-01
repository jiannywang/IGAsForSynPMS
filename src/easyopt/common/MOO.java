/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.common;

import java.util.Arrays;

/**
 * 多目标优化中共用的程序部分，主要是解集的非支配排序Non-dominated Sorted Rank
 * 和每个解的拥挤距离Crowd-Distance的计算
 * @author PeterWang at WOR
 */
public class MOO {
  
  
  /**根据输入的单个层级的多目标函数非支配解集，获取每个解的拥挤距离
   * @param solutions 多目标函数的一个非支配解集的相关数据，多行多列二维数组，
   * 每一行代表一个解，列0：存储解序号，列1、....列n-1为对应解的各个目标函数值
   * @return 返回多行两列的二维数组，第一列为解序号，第二列为解的拥挤距离
   */
  public static double[][] oneRankCrowdDistance(double[][] solutions){
    //step1: 首先将非支配解集按照特定目标函数排序，
    EasyMath.sortArray(solutions, new int[] {1});//按照第二列（第一个目标函数）升序排列
    //step2: 进行每个解的拥挤距离计算
    int soluQty=solutions.length;//解的数量
    int objQty=solutions[0].length-1;//目标函数的数量
    double[][] cd=new double[soluQty][2];//存储解编号及其在该非支配解集中的拥挤距离
    for(int i=1;i<soluQty-1;i++){
      double midVal=0;
      for(int j=0;j<objQty;j++){
        midVal+=Math.abs(solutions[i+1][j+1]-solutions[i-1][j+1]);
      }
      cd[i][1]=midVal/objQty;
      cd[i][0]=solutions[i][0];
    }
    //对解集中第一个和最后一个解的拥挤距离进行处理
    double sumObj=EasyMath.sum(solutions);
    for(int i=0;i<soluQty;i++){//将解的序号减掉，仅保留目标函数值的和
      sumObj-=solutions[i][0];
    }
    cd[0][0]=solutions[0][0];
    cd[soluQty-1][0]=solutions[soluQty-1][0];
    cd[0][1]=sumObj;
    cd[soluQty-1][1]=sumObj;    
    //非支配解集拥挤距离计算结束
    
    return cd;
  }

  /**根据指定的非支配解集和编码种群，提取对应的编码种群个体形成集合返回
   * @param rank0Fit 带有编号的多目标函数的非支配解集，其中第一列为对应解的行号，第2、3、....列为各个目标函数值
   * @param inArray 非支配解的编码种群，每一行为一个解【GA算法中的一条染色体、Jaya算法中一个同学的各科成绩】
   * @return rank0Fit中各个行号所对应的inArray中的各行数据形成的二维数组
   * */
  public static double[][] getElite(double[][] rank0Fit,double[][] inArray){
    int eliteQty=rank0Fit.length;
    int cols=inArray[0].length;
    double[][] elites=new double[eliteQty][cols];
    for(int i=0;i<eliteQty;i++){
      int nowRowId=(int) rank0Fit[i][0];
      for(int j=0;j<cols;j++){
        elites[i][j]=inArray[nowRowId][j];
      }
    }
    return elites;
  }  
  
  /**根据输入的全部层级的多目标函数非支配解集及其层级值，获取每个解的拥挤距离
   * @param solutions 多目标函数一个种群的解集，多行多列二维数组，
   * 每一行代表一个解，列1：存储解序号，列2、....列n为对应解的各个目标函数值
   * @param ranks 各个解对应的层级号，列1：解序号，列2：层级号
   * @return 返回多行三列的二维数组，第一列为解序号，第二列为解的层级编号，第三列为解的拥挤距离
   */
  public static double[][] allRankCrowdDistance(double[][] solutions,int[][] ranks){
    //step1: 首先将ranks排序，并获得最大的层级，以便后续进行拥挤距离计算
   EasyMath.sortArray(ranks, new int[] {1});//按照第二列（层级）升序排列    
   int soluQty=ranks.length;
   int maxRank=ranks[soluQty-1][1];
   int objQty=solutions[0].length-1;//目标函数的数量 
   double[][] rankCDs=new double[soluQty][3]; //0-解序号，1-层级号，2-拥挤距离
   //step2: 对每个层级的解进行拥挤距离计算
   int rankIdx=0;//用于最后写入rankCDs中的行控制
   for(int i=0;i<=maxRank;i++){
     int nowRankQty=0;
     for(int j=0;j<soluQty;j++){
       if(ranks[j][1]==i){
         nowRankQty++;
       }else{
         if(nowRankQty>0){
           break;
         }
       }
     }
     //定义一个存储当前层级目标函数值的临时数组
     double[][] midFits=new double[nowRankQty][objQty+1];
     int nowRankIdx=0;
     for(int j=0;j<soluQty;j++){
       if(ranks[j][1]==i){
         midFits[nowRankIdx]=Arrays.copyOf(solutions[ranks[j][0]], objQty+1);
         nowRankIdx++;
       }else{
         if(nowRankIdx>0){
           break;
         }
       }
     }
     //对临时数组进行拥挤距离计算
     double[][] cd=oneRankCrowdDistance(midFits);
     //将计算结果写入到最后的数组中去
     int midRow=cd.length;
     for(int j=0;j<midRow;j++){
       int soluId=(int)cd[j][0];//解序号
       rankCDs[rankIdx][0]=soluId;
       rankCDs[rankIdx][1]=0;//层级编号最后同意处理，这里不处理了；
       rankCDs[rankIdx][2]=cd[j][1];
       rankIdx++;
     }
   }
   //将ranks按照解序号排序
   EasyMath.sortArray(ranks, new int[] {0});
   for(int i=0;i<soluQty;i++){
     int soluId=(int)rankCDs[i][0];//解序号   
     rankCDs[i][1]=ranks[soluId][1];
   }    
    return rankCDs;
  }
  
  
  /**获取多目标函数解集中的非支配解，目标函数均为越小越好
   * @param solutions 多目标函数的一个非支配解集的相关数据，多行多列二维数组，
   * 每一行代表一个解，列1：存储解序号，列2、....列n为对应解的各个目标函数值
   * @return 返回一维数组，该解为非支配解若为1，否则为-1；
   */
  public static int[] getNonDominatedSetSimple(double[][] solutions){
    int soluQty=solutions.length;//解的数量
    int objQty=solutions[0].length-1;//目标函数的数量
    int[] isDominated=new int[soluQty];//存储解编号及其在该解在当前解集中是否为非支配解，若为1则为非支配解，0为被支配解
    for(int i=0;i<soluQty;i++){
      isDominated[i]=1; //初始化解均为非支配解
    }
    for(int i=0;i<soluQty-1;i++){//将第i个解同其他解对比，判断其是否为支配解
        for(int j=i+1;j<soluQty;j++){
            int goodQty=0;//解i比解j目标函数好的数量
            int badQty=0;//解i比解j目标函数差的数量
            for(int k=1;k<=objQty;k++){
              if(solutions[i][k]>solutions[j][k]){
                badQty++;
              }
              if(solutions[i][k]<solutions[j][k]){
                goodQty++;
              }          
            }
            //如果两个解彼此都有比对方好的目标函数，两者为非支配
            if(badQty>0&&goodQty>0){//两者互为非支配，不处理
            }
            //如果解i有比对方好，但没有比对方差的目标函数，i支配j
            if(badQty==0&&goodQty>=0){
              isDominated[j]=0;
            }  
            //如果解i有比对方差，但没有比对方好的目标函数，i被j支配
            if(badQty>0&&goodQty==0){
              isDominated[i]=0;
              break;
            }             
      }
    }
    //当前解集中isDominated[i]=1的解均为非支配解    
    return isDominated;
  }
  
  
  /**获取多目标函数解集中的非支配解，目标函数均为越小越好
   * @param solutions 多目标函数的一个非支配解集的相关数据，多行多列二维数组，
   * 每一行代表一个解，列1：存储解序号，列2、....列n为对应解的各个目标函数值
   * @return 返回多行两列的二维数组，第一列为对应解的序号【从0依次编号】，第二列为该解是否为非支配解，若是则为1，否则为-1；
   */
  public static int[][] getNonDominatedSet(double[][] solutions){
    int soluQty=solutions.length;//解的数量
    int objQty=solutions[0].length-1;//目标函数的数量
    int[][] isDominated=new int[soluQty][2];//存储解编号及其在该解在当前解集中是否为非支配解
    for(int i=0;i<soluQty;i++){
      isDominated[i][0]=i;
      isDominated[i][1]=1; //初始化解均为非支配解
    }
    for(int i=0;i<soluQty-1;i++){//将第i个解同其他解对比，判断其是否为支配解
        for(int j=i+1;j<soluQty;j++){
            int goodQty=0;//解i比解j目标函数好的数量
            int badQty=0;//解i比解j目标函数差的数量
            for(int k=1;k<=objQty;k++){
              if(solutions[i][k]>solutions[j][k]){
                badQty++;
              }
              if(solutions[i][k]<solutions[j][k]){
                goodQty++;
              }          
            }
            //如果两个解彼此都有比对方好的目标函数，两者为非支配
            if(badQty>0&&goodQty>0){//两者互为非支配，不处理
            }
            //如果解i有比对方好，但没有比对方差的目标函数，i支配j
            if(badQty==0&&goodQty>=0){
              isDominated[j][1]=-1;
            }  
            //如果解i有比对方差，但没有比对方好的目标函数，i被j支配
            if(badQty>0&&goodQty==0){
              isDominated[i][1]=-1;
              break;
            }             
      }
    }
    //当前解集中isDominated[i][1]=1的解均为非支配解    
    return isDominated;
  }
  /**根据输入的目标函数数组和非支配分级数组，提取最优等级的目标函数
   * @param fitnesses  多行多列二维数组，其中第一列为解的序号，第二列及其之后的列为各个目标函数的值
   * @param ranks 多行两列数组，其中第一列为解的序号，第二列为分级序号，而且该数组按照第二列进行升序排序，
   *      分级序号从0开始
   * @return 将ranks中分级序号为0的行对应的解序号的fitnesses值提取出来，即从多级非支配解集中，提取最优的非支配解集
   * 
   * */
  public static double[][] getFirstRankSolution(double[][] fitnesses,int[][] ranks){
    int bestNum=0;
    int row=ranks.length;
    for(int i=0;i<row;i++){
      if(ranks[i][1]==0){
        bestNum++;
      }else{
        break;
        
      }
    }
    int cols=fitnesses[0].length;
    double[][] bestFitnesses=new double[bestNum][cols];
    int idx=0;
    for(int i=0;i<row;i++){
      if(ranks[i][1]==0){
        for(int j=0;j<cols;j++){
          bestFitnesses[idx][j]=fitnesses[ranks[i][0]][j];
        }
        idx++;
      }else{
        break;
      }
    }    
    return bestFitnesses;
  }
  /**根据指定的非支配解集和染色体种群，提取对应的序染色体保留下来
   * @param rankFit 多目标算法排序后的解集，其中第一列为对应染色体种群【编码种群】的序号，
   *      第二列及其之后的列数值为对应编码的各个目标函数值
   * @param chrome  染色体种群或编码种群
   * @return 返回包含在rankFit中第一列序号的chrome子集
   * */
  public static int[][] getRankedChrome(double[][] rankFit,int[][] chrome){
    int rankedQty=rankFit.length;
    int cols=chrome[0].length;
    int[][] elites=new int[rankedQty][cols];
    for(int i=0;i<rankedQty;i++){
      int nowRowId=(int) rankFit[i][0];
      for(int j=0;j<cols;j++){
        elites[i][j]=chrome[nowRowId][j];
      }
    }
    return elites;
  } 
  
  /**根据指定的非支配解集和染色体种群，提取对应的序染色体保留下来
   * @param rankFit 多目标算法排序后的解集，其中第一列为对应染色体种群【编码种群】的序号，
   *      第二列及其之后的列数值为对应编码的各个目标函数值
   * @param chrome  染色体种群或编码种群
   * @return 返回包含在rankFit中第一列序号的chrome子集
   * */
  public static double[][] getRankedChrome(double[][] rankFit,double[][] chrome){
    int rankedQty=rankFit.length;
    int cols=chrome[0].length;
    double[][] elites=new double[rankedQty][cols];
    for(int i=0;i<rankedQty;i++){
      int nowRowId=(int) rankFit[i][0];
      for(int j=0;j<cols;j++){
        elites[i][j]=chrome[nowRowId][j];
      }
    }
    return elites;
  }  
  
  /**对多目标函数解集中的每个解进行非支配解层级标注【目标函数均为越小越好】
   * @param solutions 多目标函数的一个非支配解集的相关数据，多行多列二维数组，
   * 每一行代表一个解，列1：存储解序号，列2、....列n为对应解的各个目标函数值
   * @return 返回多行两列的二维数组，第一列为解序号，第二列为解的层级序号，
   *                            层级0最大，依次增加，数值越大，层级越低，解越差
   */
  public static int[][] getNonDominatedRank(double[][] solutions){
    int soluQty=solutions.length;//解的数量
    int objQty=solutions[0].length-1;//目标函数的数量
    int[][] ranks=new int[soluQty][2];//存储解编号及其在该解在当前解集中的非支配解层级号
    int rankNum=0;
    int remainSoluQty=soluQty;
    int soluIdx=0;
    double[][] midSolutions=new double[soluQty][objQty+1];
    for(int i=0;i<soluQty;i++){
      midSolutions[i]=Arrays.copyOf(solutions[i],objQty+1);
    }
    while(remainSoluQty>0){
      int[][] isDominatedSet=getNonDominatedSet(midSolutions);
      EasyMath.sortArray(isDominatedSet,new int[] {-1});
      int domiQty=0;//非支配解数量
      for(int i=0;i<remainSoluQty;i++){
        if(isDominatedSet[i][1]==1){
          ranks[soluIdx][0]=(int)midSolutions[isDominatedSet[i][0]][0];
          ranks[soluIdx][1]=rankNum;
          soluIdx++;
          domiQty++;
        }else{
          break;
        }
      }
      rankNum++;
      //将剩下的劣解保留下来，继续循环处理
//      System.out.println("domiQty:  "+domiQty+"  remainSoluQty: "+remainSoluQty);
      remainSoluQty-=domiQty;
      if(remainSoluQty>0){
        double[][] midSolutions2=new double[remainSoluQty][objQty+1];
        for(int i=0;i<remainSoluQty;i++){
          int nowIdx=isDominatedSet[i+domiQty][0];
          midSolutions2[i]=Arrays.copyOf(midSolutions[nowIdx],objQty+1);
        } 
        midSolutions=midSolutions2;
      }
//      HmArray.printArray(midSolutions);
      
    }
    return ranks;
  }
  
  
  
}
