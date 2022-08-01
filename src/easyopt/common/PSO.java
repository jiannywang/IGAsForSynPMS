
package easyopt.common;

/**
 * 蚁群算法运算过程中涉及的相关方法
 * @author PeterWang at WOR
 */
public class PSO {
  
  /**根据一维数组获得最小解对应的数组下标，一般传入的数组为不同粒子对应的适应度函数值
   *@param fits 一维数组
   *@return 返回整数下标，为【0,数组长度-1】之间的一个数
   */
  public static int getMinFitIdx(double[] fits){
    int num=fits.length;   
    double[][] midFits=new double[num][2];//0列为原始fits值，1列为排序后的序号
    for(int j=0;j<num;j++){
    	midFits[j][0]=fits[j];
    	midFits[j][1]=j;
    }
    //排序获得整数解
    EasyMath.sortArray(midFits, new int[] {0});       
    return (int)midFits[0][1];    
  }  
    
  
  
  /**将多个粒子位置向量构成的实数二维数组X转变为可表示作业排序的二维整数向量,每一行整数为区间【1,变量个数】之间所有整数的乱序
   *@param X 表示粒子群各个粒子位置的二维实数向量，行为粒子个数，列为变量个数
   *@return 返回表示作业排序的整数二维向量，行为粒子个数，列为变量个数，每行内容为【1,变量个数】之间的整数的乱序
   */
  public static int[][] parseInt(double[][] X){
    int pop=X.length;
    int xQty=X[0].length;    
    int[][] newX=new int[pop][xQty];
    for(int i=0;i<pop;i++){
      double[][] midX=new double[xQty][2];//0列为X值，1列为排序后的序号
      for(int j=0;j<xQty;j++){
        midX[j][0]=X[i][j];
        midX[j][1]=j+1;
      }
      //排序获得整数解
      EasyMath.sortArray(midX, new int[] {0});
      //将整数解写入newX
      for(int j=0;j<xQty;j++){
        newX[i][j]=(int)midX[j][1];
      }      
    }   
    return newX;    
  }  

  /**将单个粒子的实数型的一维位置向量X转变为整数的一维向量，以表示一种作业排序的解,整数解为区间【1,变量个数】之间所有整数的乱序
   *@param X 一维实数向量，一般为一个粒子的位置向量，列为变量个数
   *@return 返回整数粒子群X，一维数组，列为变量个数，内容为【1,变量个数】之间的整数
   */
  public static int[] parseInt(double[] X){
    int xQty=X.length;    
    int[] newX=new int[xQty];

    double[][] midX=new double[xQty][2];//0列为X值，1列为排序后的序号
    for(int j=0;j<xQty;j++){
      midX[j][0]=X[j];
      midX[j][1]=j+1;
    }
    //排序获得整数解
    EasyMath.sortArray(midX, new int[] {0});
    //将整数解写入newX
    for(int j=0;j<xQty;j++){
      newX[j]=(int)midX[j][1];
    }      
 
    return newX;    
  }   
  
  /**利用【0-1】随机分布初始化算法位置向量：
   * 根据粒子数量、变量数量初始化粒子位置向量X，每一行表示一个粒子，每一列表示该粒子在对应变量维度上的位置
   *@param pop 粒子数量
   *@param xQty 变量数量
   *@return 返回粒子群中每个粒子在xQty维度上的位置向量X，二维数组，行为粒子个数，列为变量个数
   */
  public static double[][] initX(int pop,int xQty){
    double[][] newX=new double[pop][xQty];
    for(int i=0;i<pop;i++){
      for(int j=0;j<xQty;j++){
        newX[i][j]=Math.random();
      }
    }   
    return newX;    
  }
  
  /**利用【0-1】随机分布初始化算法速度向量：
   * 根据粒子数量、变量数量初始化粒子速度向量V，每一行表示一个粒子，每一列表示该粒子在对应变量维度上的速度
   *@param pop 粒子数量
   *@param xQty 变量数量
   *@return 返回初始速度V，二维数组，行为粒子个数，列为变量个数
   */
  public static double[][] initV(int pop,int xQty){
    double[][] newV=new double[pop][xQty];
    for(int i=0;i<pop;i++){
      for(int j=0;j<xQty;j++){
        newV[i][j]=Math.random();
      }
    }   
    return newV;    
  }  
    
  
  /**根据当前解、速度、个体最优解、群体最优解、算法参数更新解集的值X
   *@param X 当前粒子群的解集，行为粒子个数，列为变量个数
   *@param V 当前粒子群对应的速度，行为粒子个数，列为变量个数
   *@param singleBest 当前粒子群各个粒子的最好解，行为粒子个数，列为变量个数
   *@param groupBest 当前种群最好解,一维数组，长度为变量个数
   *@param params 算法参数，0-惯性系数w，1-个体学习因子c1,2-社会学习因子c2
   *@return 返回新的粒子群X，二维数组，行为粒子个数，列为变量个数
   */
  public static double[][] updateX(double[][] X,double[][] V,double[][] singleBest,double[] groupBest,double[] params){
    int pop=X.length;
    int xQty=X[0].length;
    double[][] newX=new double[pop][xQty];
    double[][] newV=updateV(X,V,singleBest,groupBest,params);
    for(int i=0;i<pop;i++){
      for(int j=0;j<xQty;j++){
        newX[i][j]=X[i][j]+newV[i][j];
      }
    }   
    return newX;    
  }
  
  /**根据当前解、速度、个体最优解、群体最优解、算法参数更新解集各粒子的速度向量V
   *@param X 当前粒子群的解集，行为粒子个数，列为变量个数
   *@param V 当前粒子群对应的速度，行为粒子个数，列为变量个数
   *@param singleBest 当前粒子群各个粒子的最好解，行为粒子个数，列为变量个数
   *@param groupBest 当前种群最好解,一维数组，长度为变量个数
   *@param params 算法参数，0-惯性系数w，1-个体学习因子c1,2-社会学习因子c2 
   *@return 返回新的速度V，二维数组，行为粒子个数，列为变量个数
   */
  public static double[][] updateV(double[][] X,double[][] V,double[][] singleBest,double[] groupBest,double[] params){
    int pop=X.length;
    int xQty=X[0].length;
    double[][] newV=new double[pop][xQty];
    for(int i=0;i<pop;i++){
      for(int j=0;j<xQty;j++){
        newV[i][j]=params[0]*V[i][j]+params[1]*Math.random()*(singleBest[i][j]-X[i][j])+params[2]*Math.random()*(groupBest[j]-X[i][j]);
      }
    } 
    return newV;    
  }  
  
}
