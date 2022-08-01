/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.common;

import java.util.Arrays;

/**
 * 教学算法运算过程中涉及的相关方法
 * @author PeterWang at WOR
 */
public class TLBO {
  /**
   * 根据班级学生数量，科目数量初始化成绩集合，初始成绩随机分布在[0,100]之间
   * @param popSize 学生数量，群体规模
   * @param subjectQty 科目数量，即变量数量
   * @return 二维数组，一行代表一个学生各个科目的成绩，一列代表某个科目全部学生的成绩
   */
  	public static double[][] initClass(int popSize,int subjectQty){
		double[][] classScores=new double[popSize][subjectQty];
		for(int i=0;i<popSize;i++){
			for(int j=0;j<subjectQty;j++){
				classScores[i][j]=Math.round(100*Math.random());
			}
		}
		return classScores;
	}
    
  /**
   * 对每个学生各个科目成绩进行排序，然后生成排序顺序序号数组，即车间调度的作业排序【作业编号从1开始】
   * @param scores 学生科目的成绩
   * @return 二维数组，一行代表一个解所对应的作业排序编码
   */
  	public static int[][] getSequenceFromScores(double[][] scores){
      int studentQty=scores.length;
      int subjectQty=scores[0].length;
      int[][] sequences=new int[studentQty][subjectQty];
      for(int i=0;i<studentQty;i++){
        double[][] myScores=new double[subjectQty][2];//0-科目成绩，1-科目编号【从1开始】
        for(int j=0;j<subjectQty;j++){
            myScores[j][0]=scores[i][j];
            myScores[j][1]=j+1;
        }
        //数组排序获得作业排序-按照成绩升序排序
        EasyMath.sortArray(myScores, new int[] {0});
        for(int j=0;j<subjectQty;j++){
            sequences[i][j]=(int)myScores[j][1];
        }        
      }
		return sequences;
	}    

  /**
   * 对单个学生各个科目成绩进行排序，然后生成该学生成绩对应的排序顺序序号数组，即车间调度的作业排序【作业编号从1开始】
   * @param scores 学生科目的成绩
   * @return 一维数组，全部作业的调度排序编码
   */
  	public static int[] getSequenceFromOneScore(double[] scores){
      int subjectQty=scores.length;
      int[] sequences=new int[subjectQty];

      double[][] myScores=new double[subjectQty][2];//0-科目成绩，1-科目编号【从1开始】
      for(int j=0;j<subjectQty;j++){
          myScores[j][0]=scores[j];
          myScores[j][1]=j+1;
      }
      //数组排序获得作业排序-按照成绩升序排序
      EasyMath.sortArray(myScores, new int[] {0});
      for(int j=0;j<subjectQty;j++){
          sequences[j]=(int)myScores[j][1];
      }        
	return sequences;
	}    

  /**
   * 根据两个学生各个科目的成绩，更新第一个学生的成绩
   * @param scores 当前班级各个同学的成绩
   * @param isFirstGood 第一个学生综合评价是否优于第二个学生
   * @return 两行多列的二维数组，每行代表一个学生，每列代表一个科目，即返回两个学生的各科目成绩
   */
  	public static double[][] updateOneScore(double[][] scores,boolean isFirstGood){
      int studentQty=scores.length;
      int subjectQty=scores[0].length;
      double[][] newScores=new double[studentQty][subjectQty];
      if(isFirstGood){
        for(int j=0;j<subjectQty;j++){
          newScores[0][j]=scores[0][j]+Math.random()*(scores[1][j]-scores[0][j]);
        }
      }else{
        for(int j=0;j<subjectQty;j++){
          newScores[0][j]=scores[0][j]-Math.random()*(scores[1][j]-scores[0][j]);
        }      
      }
      newScores[1]=Arrays.copyOf(scores[1],subjectQty);
	return newScores;
	}    
      
    
  /**
   * 根据当前班级同学成绩、最好同学成绩和班级平均成绩更新班级同学的成绩
   * @param scores 当前班级各个同学的成绩
   * @param bestScores 班级最好同学各科成绩
   * @param meanScores 班级各个科目的平均成绩
   * @return 二维数组，一行代表一个学生各个科目的成绩，一列代表某个科目全部学生的成绩
   */
  	public static double[][] updateScores(double[][] scores,double[] bestScores,double[] meanScores){
      int studentQty=scores.length;
      int subjectQty=scores[0].length;
      double[][] newScores=new double[studentQty][subjectQty];
      for(int i=0;i<studentQty;i++){
        for(int j=0;j<subjectQty;j++){
          newScores[i][j]=scores[i][j]+Math.random()*(bestScores[j]-Math.round(1+Math.random())*meanScores[j]);
        }
      }
      return newScores;
	}    

    /**
     * 根据当前班级同学成绩、最好同学成绩和班级平均成绩更新班级同学的成绩，并使得同学成绩落在【0-1】区间
     * @param scores 当前班级各个同学的成绩
     * @param bestScores 班级最好同学各科成绩
     * @param meanScores 班级各个科目的平均成绩
     * @return 二维数组，一行代表一个学生各个科目的成绩，一列代表某个科目全部学生的成绩
     */
      public static double[][] updateScores01(double[][] scores,double[] bestScores,double[] meanScores){
        int studentQty=scores.length;
        int subjectQty=scores[0].length;
        double min=Double.MAX_VALUE;
        double max=Double.MIN_VALUE;
        double[][] newScores=new double[studentQty][subjectQty];
        for(int i=0;i<studentQty;i++){
          for(int j=0;j<subjectQty;j++){
            newScores[i][j]=scores[i][j]+Math.random()*(bestScores[j]-Math.round(1+Math.random())*meanScores[j]);
            min=Math.min(newScores[i][j], min);
            max=Math.max(newScores[i][j], max); 
          }
        }
        double baSpan=max-min;
        if (baSpan==0){baSpan=1;}
        //归一化处理
        for(int i=0;i<studentQty;i++){
          for(int j=0;j<subjectQty;j++){
            newScores[i][j]=(newScores[i][j]-min)/baSpan;
          }
        }        
        return newScores;
    }   	
  	
  /**
   * 获取区间[0,num-1]这num个数字中的两个不同数字
   * @param num 一个正整数
   * @return 一行两列数组，分别存储上述两个数字
   */
  	public static int[] getTwoDiffNum(int num){
      int idx1=(int) Math.floor(1.0*num*Math.random());
      int idx2=(int) Math.floor(1.0*num*Math.random());
      //判断两点大小，并将小的作为第一个点，大的作为第2个点
      if(idx1==idx2){
        if(idx1<=num/2){//两点落在染色体的前半部分
              int moveLength=Math.max(1, (int)(1.0*num/2.0*Math.random()));
              idx2+=moveLength;
          }else{//两点落在染色体的后半部分
              int moveLength=Math.max(1, (int)(1.0*num/2.0*Math.random()));
              idx1-=moveLength;						
          }					
      }
      int[] twoPoints=new int[2];
      twoPoints[0]=idx1;
      twoPoints[1]=idx2;
	return twoPoints;
	}    
     
  /**
   * 对全部学生各个科目成绩计算出平均成绩
   * @param scores 学生科目的成绩
   * @return 一维数组，长度为scores的列数，即变量个数
   */
  	public static double[] getMeanScores(double[][] scores){
      int studentQty=scores.length;
      int subjectQty=scores[0].length;
      double[] sumScores=new double[subjectQty];
      double[] meanScores=new double[subjectQty];      
      for(int j=0;j<subjectQty;j++){
        for(int i=0;i<studentQty;i++){
            sumScores[j]+=scores[i][j];
        }
        meanScores[j]=sumScores[j]/studentQty;
      }
      return meanScores;
	}    
    
  
}
