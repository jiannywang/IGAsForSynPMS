package easyopt.common;

public class EasyRand {

	/**根据种子数生成一定数量落入[low,high]之间的均匀整数
	 * @param numQty 生成的随机数的数量
	 * @param seed  随机数种子数，一般是比较大的数字特殊的数字，例如777,1777,277333等
	 * @param low  生成随机数的最小值
	 * @param high  生成随机数的最大值
	 * @return 长度为numQty的[low,high]之间的随机整数数组
	 * */
	public static double[] uniform(int numQty,int seed,int low,int high){
		double[] randNum=new double[numQty];
		int m=2147483647,a=16807,b=127773;
		randNum[0]=seed;
		for(int i=1;i<numQty;i++){
			randNum[i]=(randNum[i-1]*a+b)%m;
		}
		for(int i=0;i<numQty;i++){
			randNum[i]=low+1.0*(high-low)/m*randNum[i];
		}
		return randNum;
		
	}
	
  /**根据输入的数值数量生成[0,1]区间的随机实数数组
   * @param numQty 生成的随机数的数量
   * @return 长度为numQty的[0,1]之间的随机实数数组
   * */
  public static double[] randArray(int numQty){
    double[] randNum=new double[numQty];
    for(int i=0;i<numQty;i++){
      randNum[i]=Math.random();
    }
    return randNum;    
  }

  /**随机返回pop行和cols列的0-1区间随机实数二维数组
   * @param pop 数组的行数
   * @param cols 数组的列数
   * @return 返回的pop行和cols列的0-1区间随机实数二维数组2022-05-15
   */  
  public static double[][] randPerm(int pop, int cols){
    double[][] chrome=new double[pop][cols];
    for(int i=0;i<pop;i++){
      for(int j=0;j<cols;j++){
        chrome[i][j]=Math.random();
      }
    }
    return chrome;
  }    
  
}
