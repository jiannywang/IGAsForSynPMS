/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyopt.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 运算过程中可能涉及到的数学处理方法
 * @author PeterWang, easy optimizer.
 */
public class EasyMath {
	
	/**获取一维实数数组常见统计量，包括：最小值，最大值，均值和标准差
	 * @param arr 一维实数数组
	 * @return 一个Map对象，包含的标签分别为max、min、sum、mean和sigma
	 * */
    public static Map<String, Double> commonStat(double[] arr) {
	
	    Map<String, Double> map = new HashMap<>();
	    double max = Double.MIN_VALUE;
	    double min = Double.MAX_VALUE;
	    double sum=0.0,mean,sigma;
	    for (int i = 0; i < arr.length; i++) {
	        sum+=arr[i];
	        if (arr[i] > max) {
	            max = arr[i];
	        }
	        if (arr[i] < min) {
	            min = arr[i];
	        }
	    }
	    mean=sum/arr.length;
	    map.put("max", max);
	    map.put("min", min);
	    map.put("mean",mean);
	    Double squareSum=0.0;
	    for (int i = 0; i < arr.length; i++) {
	        squareSum+=Math.pow((arr[i]-mean),2.0);
	    } 
	    sigma=Math.pow(squareSum/arr.length,0.5);
	    map.put("sigma",sigma);
	    return map;
  } 	
	
  public static String getTomorrow(){
      SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
      Date today=new Date();
      double tm=today.getTime()+24*60*1000*60;
      String tomorrow=sdf.format(tm);
      return tomorrow; 
  }

  /**if the input parameter is integer, return a value without decimal
   * else, return a value with one place decimal
   * @param val a double value
   * @return a new double
   * */
  public static double reduceDecimal(double val){
	  double v;
	  if(Math.round(val)==val) {
		  v=val;
	  }else {
		  v=Math.round(val*10)/10.0;
	  }

      return v; 
  }    
  
  public static String getToday(){
      SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
      Date today=new Date();
      String todayStr=sdf.format(today);
      return todayStr; 
  }  
  
  /**Compare whether the first date is later than the second date
   * @return 0: the two dates are equal;1: the first date is later than the second date;
   *         -1: the first date is earlier than the second date
   */
  
  public static int compareTwoDates(){
      SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
      Date today=new Date();
      Date johnDate=new Date();
     try {
    	 johnDate=sdf.parse("2032-03-31");
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
     
	  int isLater=0;
	  if(today.after(johnDate)) {
		  isLater=1;
	  }else if(today.before(johnDate)){
		  isLater=-1;
	  }else {
		  isLater=0;
	  }
	  //before the return, write this running information into our table
//	  DBUtil db=new DBUtil();
//	  String thisPcName = Host.getHostName();
//	  String[] thisPCMacs = Host.getMacAddresses();
//	  String thisIP = Host.getIpAddress();
//	  String goodMac="";
//	  for(int i=0;i<thisPCMacs.length;i++) {
//		  if(thisPCMacs[i].length()<19) {
//			  goodMac =thisPCMacs[i];
//		  }
//	  }
//	  String sql="insert into easyoptusing (mac,ip,pcname) values ('"+goodMac+"','"
//	      +thisIP+"','"+thisPcName+"')";
//	  if(db.getConn()!=null) {
//		  db.executeUpdate(sql);
//	  }
      return isLater; 
  }  
  
  public static double[][] calDist(double[] xPos,double[] yPos){
    int xQty=xPos.length;
    int yQty=yPos.length;

    double[][] dist=new double[xQty][yQty];
    
    double midVal;
    for(int i=0;i<xQty;i++){
      for(int j=0;j<xQty;j++){
        midVal=Math.sqrt(Math.pow(xPos[i]-xPos[j],2)+Math.pow(yPos[i]-yPos[j], 2));
        midVal=Math.round(midVal*100)/100.0;
        dist[i][j]=midVal;dist[j][i]=midVal;
      }
    }
    return dist;
  }

/**根据传入的整数n生成[0,n-1]这n个整数的全排列数组
 * @param n 正整数
 * @return 二维数组,即数值[0,n-1]这n个数字的全排列数组
 */
	public static int[][] permute0(int n){
		int permQty=fact(n);
		int[][] finalArray =new int[permQty][n];
		int[] arr= new int[n];
		for(int i=0;i<n;i++) {
			arr[i]=i;
		}
		getPermute(finalArray,0,arr.length-1,arr);
		return finalArray;
	}
	/**根据传入的整数n生成[1,n]这n个整数的全排列数组
	 * @param n 正整数
	 * @return 二维数组,即数值[1,n]这n个数字的全排列数组
	 */
		public static int[][] permute1(int n){
			int permQty=fact(n);
			int[][] finalArray =new int[permQty][n];
			int[] arr= new int[n];
			for(int i=0;i<n;i++) {
				arr[i]=i+1;
			}
			getPermute(finalArray,0,arr.length-1,arr);
			return finalArray;
		}	

	  /**将输入的实数四舍五入
	   * @param x 实数
	   * @param decimalQty 小数位数
	   * @return 四舍五入后保留特定位数的实数
	   */
	    public static double round(double x,int decimalQty){
	      double y;
	      y=Math.round(x*Math.pow(10, decimalQty))/Math.pow(10, decimalQty);
	      return y;
	    } 		
	/*
	 * 配合permute
	 * */
	private static void getPermute(int[][] farr,int from, int to,int[] arr) {
		if(from==to) {//当from=to，即2时才执行这一句，将arr添加进去
			int idx2=0;
			for(int i=0;i<farr.length;i++) {
				int sum=0;
				for(int j=0;j<arr.length;j++){
					sum+=farr[i][j];
				}
				if(sum==0) {
					break;
				}else {
					idx2=i+1;
				}
			}
			for(int i=0;i<arr.length;i++) {
				farr[idx2][i]=arr[i];
			}
		}
		for(int i=from;i<=to;i++) {
			swap(arr,i,from);
			getPermute(farr,from+1,to,arr);
			swap(arr,i,from);		
		}		
	}
	/*
	 * 配合getPermute
	 * */	
	private static void swap(int[] arr,int i, int j) {
		int temp=arr[i];
		arr[i]=arr[j];
		arr[j]=temp;		
	}	
	
  /**数组归一化处理
   * */
  public static double[][] normalization(double[][] inArray){
    int row=inArray.length;
    int col=inArray[0].length;
    double[][] outArray=new double[row][col];
    double min=Double.MAX_VALUE;
    double max=Double.MIN_VALUE;
    for(int i=0;i<row;i++){
      for(int j=0;j<col;j++){
        min=Math.min(min, inArray[i][j]);
        max=Math.max(max, inArray[i][j]);
      }
    }
    double baSpan=max-min;
    if (baSpan==0){baSpan=1;}
    //归一化处理
    for(int i=0;i<row;i++){
      for(int j=0;j<col;j++){
        outArray[i][j]=(inArray[i][j]-min)/baSpan;
      }
    }       
    return outArray;
  }	
	
	/*
	 * 获取0-[n-1]之间的两个不同数值，按升序排列
	 * */	
	public static int[] getTwoDistinctNum(int n) {
		int[] twoNum=new int[2];
		int num1=(int)(n*Math.random());
		int num2=(int)(n*Math.random());
		if(num2==num1){
			if(num2<=n/2){
				num2=num2+1+(int)((n-1)*Math.random()/2);
			}else if(num2>n/2){
				num2=num2-(int)((n-1)*Math.random()/2)-1;
			}
		}
		twoNum[0]=Math.min(num1, num2);
		twoNum[1]=Math.max(num1, num2);
		return twoNum;
		
		
	}	
/**生成从[0,n-1]个整数之间取2个数值的组合结果
 * @param n 正整数
 * @return 二维数组,为从数值[0,m-1]中取2个数值形成的组合数组，为n*(n-1)/2行、两列的数组
 */
  public static int[][] combin2(int n){
    int rows=n*(n-1)/2;
    int[][] comArray=new int[rows][2];
    int idx=0;
    for(int i=0;i<n-1;i++){
      for(int j=i+1;j<n;j++){
        comArray[idx][0]=i;
        comArray[idx][1]=j;
        idx++;
      }
    }
    return comArray;
  }   

/**生成整数n的阶乘n!
 * @param n 正整数
 * @return 整数值
 */
  public static int fact(int n){
    int product=1;
    for(int i=1;i<=n;i++){
      product*=i;
    }   
    return product;
  } 
  
  /**m为1、2、...、n这有序的数列中的一个数值，将m与数列中非m的其他n-1个数值进行位置互换形成的两个数字对，例如n=4，m=2，则会
   * 形成2、1,2、3,2、4这三个数对
   * @param n 比m大的正整数
   * @param m 比n小的正整数
   * @return 多行两列的二维数组
  */
  public static int[][] twoNumPairs(int n,int m){
    int[][] pairs=new int[n-1][2];
    int idx=0;
    for(int i=0;i<n;i++){
      if(i!=m-1){
        pairs[idx][0]=m;
        pairs[idx][1]=i+1;
        idx++;
      }
    }
    return pairs;
  }
  
  
  /**从1到n这n个数值中选择m个不同的数值
   * @param n 不小于m的正整数
   * @param m 不大于n的正整数
   * @return 1-n这n数中的m个数值形成的数组
   */
    public static int[] choose(int n,int m){
      int[] subArray=new int[m];
      int[] nArray=new int[n];
      for(int i=1;i<=n;i++){
        nArray[i-1]=i;
      }
      subArray=getPartialArray(nArray,m);
      return subArray;
    }   
    /**从1到n这n个数值中选择1个数值
     * @param n 大于m的正整数
     * @return 1-n这n数中的1个数值
     */
      public static int choose1(int n){
        int[] array=randPerm(n-1); 
        return array[0]+1;
      }      
  
  /**get a partial array with outQty elements from the inArray
   * @param outQty positive integer, the quantity of the out element, 
   * must less than the length of the inArray
   * @param inArray one-dimension integer array
   * @return one-dimension array, a sub set of the inArray with outQty element
   */
    public static int[] getPartialArray(int[] inArray,int outQty){
      int[] subArray=new int[outQty];
      int[] randArray=randPerm(inArray.length);
      for(int i=0;i<outQty;i++){
        subArray[i]=inArray[randArray[i]];
      }   
      return subArray;
    }   
  
  /**产生特定整数数组的乱序排列
   * @param arr 整数数组
   * @return 返回arr的乱序数组
   */
  public static int[]  randPerm(int[] arr) {  
    int len=arr.length;
  	int solutionArr[] = new int[len];
    int ranIndex,remainQty=len;
    Random random = new Random();
    for (int i = 0; i <len; i++) {
        ranIndex = random.nextInt(remainQty);
        solutionArr[i]=arr[ranIndex];
        for(int j=ranIndex;j<len-1;j++){
          arr[j]=arr[j+1];        
        }
        remainQty--;
    }
    return solutionArr;
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
  
  
  /**随机返回0-[len-1]之间整数的乱序
   * @param len 数组的长度
   * @return 返回的为0-[len-1]之间数字乱序的行向量
   */
  public static int[]  randPerm(int len) { 
	  int solutionArr[] = new int[len];
    int oldArray[]=new int[len];
    for(int i=0;i<len;i++){
      oldArray[i]=i;
    }
    int ranIndex,remainQty=len;
    Random random = new Random();
    for (int i = 0; i <len; i++) {
        ranIndex = random.nextInt(remainQty);
        solutionArr[i]=oldArray[ranIndex];
        for(int j=ranIndex;j<len-1;j++){
          oldArray[j]=oldArray[j+1];        
        }
        remainQty--;
    }
    return solutionArr;
  }   
  /**随机返回1-len之间整数的乱序
   * @param len 数组的长度
   * @return 返回的为10-len之间数字乱序的行向量
   */
  public static int[]  randPermStart1(int len) { 
	  int solutionArr[] = new int[len];
    int oldArray[]=new int[len];
    for(int i=0;i<len;i++){
      oldArray[i]=i+1;
    }
    int ranIndex,remainQty=len;
    Random random = new Random();
    for (int i = 0; i <len; i++) {
        ranIndex = random.nextInt(remainQty);
        solutionArr[i]=oldArray[ranIndex];
        for(int j=ranIndex;j<len-1;j++){
          oldArray[j]=oldArray[j+1];        
        }
        remainQty--;
    }
    return solutionArr;
  }   
  /**随机返回1-jobQty*procQty之间整数的乱序
   * @param jobQty 作业数量
   * @param procQty 工序数量
   * @return 返回的为10-len之间数字乱序的行向量
   */
  public static int[]  randPermStart1(int jobQty,int procQty) { 
    int len=jobQty*procQty;
	int solutionArr[] = new int[len];
    int oldArray[]=new int[len];
    int idx=0;
    for(int i=0;i<jobQty;i++){
      for(int j=0;j<procQty;j++){
         oldArray[idx]=i+1;
         idx++;
      }
    }
    int ranIndex,remainQty=len;
    Random random = new Random();
    for (int i = 0; i <len; i++) {
        ranIndex = random.nextInt(remainQty);
        solutionArr[i]=oldArray[ranIndex];
        for(int j=ranIndex;j<len-1;j++){
          oldArray[j]=oldArray[j+1];        
        }
        remainQty--;
    }
    return solutionArr;
  }   
       
  /*
   * create one random solution by Parameter:len
   * */
  public static double[]  randDoublePerm(int len) { 
		double solutionArr[] = new double[len];
		for(int i=0;i<len;i++){
			solutionArr[i]=Math.random();
		}
	  return solutionArr;
  }     

  
  
  /**
   * 随机生成len个0-1数字组成的一维数组，常用于各类算法中的0-1编码初始化
   * @param len 数组的长度
   * @return 长度为len的0-1组成的一维数组
   * */
  public static int[]  rand0_1Perm(int len) { 
  	int solutionArr[] = new int[len];
		for(int i=0;i<len;i++){
			solutionArr[i]=(int) Math.round(Math.random());
		}
	  return solutionArr;
  }     
  
  /**
   * 将由0和1组成的一维数组视为二进制数字，将其转化为十进制实数
   * @param bitArray 二进制数组
   * @return 一个实数
   * */
  public static double  bit2double(int[] bitArray) { 
  	int len = bitArray.length;
  	double result=0.0;
		for(int i=0;i<len;i++){
			result+=bitArray[len-i-1]*Math.pow(2.0, i);
		}
		result=result/Math.pow(2.0, len);
	  return result;
  }  
 
  /**
   * 一维实数数组升序排序
   * @param in 一维实数数组
   * @return 按照升序排序的一维实数数组
   * */
  public static int[]  sortArray(double[] in) { 
  	int len = in.length;
  	int[][] in2=new int[len][2];
		for(int i=0;i<len;i++){
			in2[i][0]=(int) (in[i]*10000);
			in2[i][1]=i;
		}
    Arrays.sort(in2, new Comparator<int[]>() {
      @Override
      public int compare(int[] o1, int[] o2) {
          for(int i=0;i<o1.length;++i)
          {
              if(o1[i]<o2[i])
                  return -1;
              else if(o1[i]>o2[i])
                  return 1;
              else
                  continue;
          }
          return 0;
      }
    });		
    int[] outSeq=new int[len];
    for(int i=0;i<len;i++){
    	outSeq[i]=in2[i][1];
    }
	  return outSeq;
  }  
  
    
  
  /**对二维数组进行指定按照一列或多列升序排序,不返回值，直接传址操作
   * @param ob 二维整数数组
   * @param order 一维数组，指定排序的列，列标号从0开始,其中对第0列只能升序排列，其他列编号给予负值则可以降序排列
   */
  public static void sortArray(int[][] ob, final int[] order) {    
    int[] orderSign=new int[order.length];
    for(int i=0;i<order.length;i++){
      if(order[i]<0){
        orderSign[i]=-1;
      }else{
        orderSign[i]=1;
      }
      order[i]=Math.abs(order[i]);
    }
    Arrays.sort(ob, new Comparator<Object>() {    
        public int compare(Object o1, Object o2) {   
            int[] one = (int[]) o1;    
            int[] two = (int[]) o2;             
            for (int i = 0; i < order.length; i++) {    
                int k = order[i];    
                if (one[k] > two[k]) {    
                    return 1*orderSign[i];    
                } else if (one[k] < two[k]) {    
                    return -1*orderSign[i];    
                } else {    
                    continue;  //如果按一条件比较结果相等，就使用第二个条件进行比较。  
                }    
            }    
            return 0;    
        }    
    }); 
    
    
    
} 
  
  /**对二维数组进行指定按照一列或多列进行升序排序,不返回值，直接传址操作
   * @param ob 二维实数数组
   * @param order 一维数组，指定排序的列，列标号从0开始，如果要降序排序，则在列表号前加符号，例如{1,-2}实现按照第二列升序和第三列降序的方式排序数组
   */
  public static void sortArray(double[][] ob, final int[] order) {   
    int[] orderSign=new int[order.length];
    for(int i=0;i<order.length;i++){
      if(order[i]<0){
        orderSign[i]=-1;
      }else{
        orderSign[i]=1;
      }
      order[i]=Math.abs(order[i]);
    }
    Arrays.sort(ob, new Comparator<Object>() {    
        public int compare(Object o1, Object o2) {   
            double[] one = (double[]) o1;    
            double[] two = (double[]) o2;             
            for (int i = 0; i < order.length; i++) {    
                int k = order[i];    
                if (one[k] > two[k]) {    
                    return 1*orderSign[i];    
                } else if (one[k] < two[k]) {    
                    return -1*orderSign[i];    
                } else {    
                    continue;  //如果按一条件比较结果相等，就使用第二个条件进行比较。  
                }    
            }    
            return 0;    
        }    
    }); 

  } 
  
  /** 根据种群规模和每条染色体长度生成一个种群
   * @param pop 种群数量
   * @param len 每个种群中个体的数量���
   * @return�二维种群数组，该数组有pop行，每一行为0到len-1这len个整数的乱序������� 
   */
  public static int[][]  createPopulation(int pop,int len) { 
    int[][] population=new int[pop][len];
    for(int p=0;p<pop;p++){
      int[] x=randPerm(len);
      for(int i=0;i<len;i++){
        population[p][i]=x[i];
      }
    }
    return population;
  }  
  /**
   * 计算一维数组之和
   * @param inArr 一维输入数组�����
   * @return 输入数组的和� 
   */
  public static double sum(double[] inArr){
    int len=inArr.length;
    double sumVal=0.0;
    for(int i=0;i<len;i++){
      sumVal=sumVal+inArr[i];
    }
    return sumVal;
  }
  /**
   * 计算一维整数数组之和
   * @param inArr 一维输入整数数组
   * @return 输入数组的和
   */
  public static int sum(int[] inArr){
    int len=inArr.length;
    int sumVal=0;
    for(int i=0;i<len;i++){
      sumVal=sumVal+inArr[i];
    }
    return sumVal;
  }  

  /**
 * 获取输入数值中的最大值
 * @param inArr 二维输入数组
 * @return 输入数组中的最大值
 */
public static double max(double[][] inArr){
  int rows=inArr.length;
  int cols=inArr[0].length;
  double maxVal=Double.MIN_VALUE;
  for(int i=0;i<rows;i++){
    for(int j=0;j<cols;j++){      
    	maxVal=Math.max(maxVal, inArr[i][j]);
    }
  }
  return maxVal;
}   

/**
* 获取输入数值中的最大值
* @param inArr 二维输入数组
* @return 输入数组中的最大值
*/
public static double max(double[] inArr){
	int rows=inArr.length;
	double maxVal=Double.MIN_VALUE;
	for(int i=0;i<rows;i++){   
	  	maxVal=Math.max(maxVal, inArr[i]);
	}
	return maxVal;
}  

    /**
   * 计算二维实数数组全部数值之和
   * @param inArr 二维输入数组
   * @return 输入数组的全部数组之和
   */
  public static double sum(double[][] inArr){
    int rows=inArr.length;
    int cols=inArr[0].length;
    double sumVal=0.0;
    for(int i=0;i<rows;i++){
      for(int j=0;j<cols;j++){      
        sumVal=sumVal+inArr[i][j];
      }
    }
    return sumVal;
  } 
    /**
   * 计算二维整数数组全部数值之和
   * @param inArr 二维输入数组
   * @return 输入数组的全部数组之和
   */
  public static double sum(int[][] inArr){
    int rows=inArr.length;
    int cols=inArr[0].length;
    int sumVal=0;
    for(int i=0;i<rows;i++){
      for(int j=0;j<cols;j++){      
        sumVal=sumVal+inArr[i][j];
      }
    }
    return sumVal;
  } 
    /**
   * 对一维数组进行逆序
   * @param inArr 一维整数数组
   * @return 输入一维整数数组中两个随机位置之间的内容逆序排列形成的邻域解
   */
  public static int[] reverseArray(int[] inArr){
    //对一维数组进行逆序生成邻域解
    int len=inArr.length;
    int[] newArray=new int[len];    
    for(int i=0;i<len;i++){
      newArray[i]=inArr[i];
    }
    Random random = new Random();
    int ranIdx1 = random.nextInt(len);
    int ranIdx2 = random.nextInt(len);
    if(ranIdx1>ranIdx2){
      int midIdx=ranIdx1;
      ranIdx1=ranIdx2;
      ranIdx2=midIdx;
    }
    for(int i=0;i<=ranIdx2-ranIdx1;i++){
      newArray[ranIdx1+i]=inArr[ranIdx2-i];
    }
    return newArray;
  }  

 
  
  /**
 * get this Monday Date and next Monday Date of today
 * @return thisMonday and nextMonday's string array 
 */  
public static String[] getThisWeekInterval() { 
       SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
       Calendar cal = Calendar.getInstance(); 
       Date date=new Date();
       cal.setTime(date);  
       // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了  
       int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
       if (1 == dayWeek) {  
          cal.add(Calendar.DAY_OF_MONTH, -1);  
       }  
//       System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期  
       // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一  
       cal.setFirstDayOfWeek(Calendar.MONDAY);  
       // 获得当前日期是一个星期的第几天  
       int day = cal.get(Calendar.DAY_OF_WEEK);  
       // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值  
       cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);  

       String thisMonday = sdf.format(cal.getTime());  
//       System.out.println("所在周星期一的日期：" + weekBegin);  
       cal.add(Calendar.DATE, 7);  
       String nextMonday = sdf.format(cal.getTime());  
//       System.out.println("所在周星期日的日期：" + weekEnd);  
       String[] thisWeek={thisMonday,nextMonday};
       return thisWeek;  
    }
  /**
 * get last Monday Date and this Monday Date based on today
 * @return lastMonday and  thsi Monday's string array 
 */ 
public static String[] getLastWeekInterval() {  
   SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
   Calendar calendar1 = Calendar.getInstance();  
   Calendar calendar2 = Calendar.getInstance();  
   int dayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK) - 1;  
   int offset1 = 1 - dayOfWeek;  
   int offset2 = 8 - dayOfWeek;  
   calendar1.add(Calendar.DATE, offset1 - 7);  
   calendar2.add(Calendar.DATE, offset2 - 7);  
   // System.out.println(sdf.format(calendar1.getTime()));// last Monday  
   String lastMonday = sdf.format(calendar1.getTime());  
   // System.out.println(sdf.format(calendar2.getTime()));// last Sunday  
   String thisMonday = sdf.format(calendar2.getTime()); 
   String[] lastWeek={lastMonday,thisMonday};
   return lastWeek;  
}

  /**
 * get 30days around today: 15days before and 15days after today
 * @return the date string of 15 days before today and 15 days after today string array 
 */ 
public static String[] getMonthIntervalAroundToday() {  
   Date d=new Date();   
   SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");   
   double today=d.getTime();
   double addTime=15 * 24 * 60 * 60 * 1000.0;
   String firstDay=df.format(today-addTime);
   String thirtiethDay=df.format(today-addTime);
   String[] aroundMonth={firstDay,thirtiethDay};
   
   return aroundMonth;  
}


  /**
 * get 30 days before today
 * @return the date string of 30 days before today and just today string array 
 */ 
public static String[] getMonthIntervalPreToday() {  
   Date d=new Date();   
   SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");   
   double today=d.getTime();
   double addTime=30 * 24 * 60 * 60 * 1000.0;
   double addOneDay=1 * 24 * 60 * 60 * 1000.0;   
   String firstDay=df.format(today-addTime);
   String thirtiethDay=df.format(today+addOneDay);
   String[] preMonth={firstDay,thirtiethDay};
   
   return preMonth;  
}

  /**
 * get each day's string between dates[0] and dates[1]
 * @param dates the first day and the last day string of an interval
 * @return each day string of an interval 
 */ 
public static String[] getEachDate(String[] dates)  
{  
    String start_time=dates[0];//时段第一天
    String end_time=dates[1];  //时段最后一天           //格式化日期     
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
    Date dBegin=new Date(),dEnd=new Date();
    try {
      dBegin = sdf.parse(start_time);
      dEnd = sdf.parse(end_time); 
    } catch (ParseException ex) {
      Logger.getLogger(EasyMath.class.getName()).log(Level.SEVERE, null, ex);
    }
    List<Date> lDate = EasyMath.findDates(dBegin, dEnd);//获取这周所有date 
    int rows=lDate.size();
    String[] theDates=new String[rows];
    for(int i=0;i<rows;i++){
      theDates[i]=sdf.format(lDate.get(i));
    }
    return theDates;  
}

public static List<Date> findDates(Date dBegin, Date dEnd)  
{  
    List<Date> lDate = new ArrayList<Date>();  
    lDate.add(dBegin);  
    Calendar calBegin = Calendar.getInstance();  
    // 使用给定的 Date 设置此 Calendar 的时间  
    calBegin.setTime(dBegin);  
    Calendar calEnd = Calendar.getInstance();  
    // 使用给定的 Date 设置此 Calendar 的时间  
    calEnd.setTime(dEnd);  
    // 测试此日期是否在指定日期之后  
    while (dEnd.after(calBegin.getTime()))  
    {  
     // 根据日历的规则，为给定的日历字段添加或减去指定的时间量  
     calBegin.add(Calendar.DAY_OF_MONTH, 1);  
     lDate.add(calBegin.getTime());  
    }  
    return lDate;  
}
  
}
