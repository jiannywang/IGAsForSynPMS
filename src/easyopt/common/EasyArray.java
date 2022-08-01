package easyopt.common;

import java.util.Arrays;

/**
 * 数组计算过程中可能涉及的各类处理方法
 * @author PeterWang at WOR
 */

public class EasyArray { 
  
 /**返回一维数组中的最大值
  * @param inArray 一维数组
  * @return 输入行向量中的最大值
  * */
  public static int getMaxValue(int[] inArray){
    int n=inArray.length;
    int maxVal=0;
    for(int i=0;i<n;i++){
      maxVal=Math.max(maxVal, inArray[i]);
    }
    return maxVal;
  }
  /**对二维数组进行转置操作
   * @param inArray 二维数组
   * @return 转置后的二维数组
   * */
   public static int[][] transpose(int[][] inArray){
     int row=inArray.length;
     int col=inArray[0].length;
     int[][] outArray=new int[col][row];
     for(int i=0;i<row;i++){
       for(int j=0;j<col;j++){
         outArray[i][j]=inArray[j][i];
       }
     }
     return outArray;
   }  
   /**对二维数组进行转置操作
    * @param inArray 二维数组
    * @return 转置后的二维数组
    * */
    public static double[][] transpose(double[][] inArray){
      int row=inArray.length;
      int col=inArray[0].length;
      double[][] outArray=new double[col][row];
      for(int i=0;i<row;i++){
        for(int j=0;j<col;j++){
          outArray[j][i]=inArray[i][j];
        }
      }
      return outArray;
    }    

    /**将二维实数数组转化为二维整数数组
     * @param inArray 二维数组
     * @return 转换为整数的二维数组
     * */
     public static int[][] double2Int(double[][] inArray){
       int row=inArray.length;
       int col=inArray[0].length;
       int[][] outArray=new int[row][col];
       for(int i=0;i<row;i++){
         for(int j=0;j<col;j++){
           outArray[i][j]=(int) inArray[i][j];
         }
       }
       return outArray;
     }     
  
  /**返回二维数组中特定行向量的最大值
   * @param inArray 二维整数数组
   * @param rowNum 指定行号，方法返回二维数组中这一行行向量的最大值【从0开始编号】
   * @return 二维数组指定行的最大值
   * */
   public static int getMaxValue(int[][] inArray, int rowNum){
     int col=inArray[0].length;
     int[] myArray=new int[col];
     for(int i=0;i<col;i++){
       myArray[i]=inArray[rowNum][i];
     }
     int maxVal=getMaxValue(myArray);
     return maxVal;
   }  
  
  /**
   * 将字符串转变为二维实数数组，数值之间由分号和空格分割开
   * @param str 由分号和空格分割开的实数序列字符串
   * @return 二维实数数组
   * */
  public static double[] changeStr2Double1(String str){
    //使用空格将字符串分割成字符串数组
    String[] first=str.trim().split("\\s+");
    //定义存储实数的一维数组
    double[] data=new double[first.length]; 
    //Step1: 依次判断是否有非数字的字符
    try{
      for(int j=0;j<first.length;j++){
          data[j]=Double.parseDouble(first[j]);
      } 
    } catch (NumberFormatException e) {
        System.out.println("Error happen in WorArray.changeStr2Double\n"+e);
        data[0]=-1;
    }
    //Step2: 是否存在负数的判断
    if(data[0]>=0){//判断是否有负数，如果有负数也返回错误的提示
      for(int i=0;i<data.length;i++){
          if(data[i]<0){
            data[0]=-1;
            break;
          }
      }
    }
    return data;
  } 
  
  /**
   * 将字符串转变为二维实数数组，数值之间由分号和空格分割开
   * @param str 由分号和空格分割开的实数序列字符串
   * @return 二维实数数组
   * */
  public static double[][] changeStr2Double2(String str){
    //先从分号中分割
    String[] first=str.trim().split(";");
    //分配的二位数组
    double[][] d=new double[first.length][]; 
    //Step1: 是否有非数字的字符串判断
    try {      
      for(int i=0;i<first.length;i++){        
          String[] second=first[i].trim().split("\\s+");
          d[i]=new double[second.length];
          for(int j=0;j<second.length;j++){
              d[i][j]=Double.parseDouble(second[j]);
          } 
      }
    } catch (NumberFormatException e) {
        System.out.println("Error happen in WorArray.changeStr2Double\n"+e);
        d[0][0]=-1;
    }
    //Step2: 是否存在负数的判断
    if(d[0][0]>=0){//判断是否有负数，如果有负数也返回错误的提示
      for(int i=0;i<d.length;i++){
        if(d[i].length!=d[0].length){
          d[0][0]=-(i+1);
          break;
        }
        for(int j=0;j<d[0].length;j++){
          if(d[i][j]<0){
            d[0][0]=-1;
            break;
          }
        }
      }
    }
    return d;
  }

  /**
   * 将字符串转变为一维正整数数组,并对非数字字符进行负数化处理
   * @param str 由空格分割开的整数序列字符串
   * @return 一维整数数组，如果转换成功则返回一维整数数组，
   *     若存在非数字字符则数组第一个值返回-1，若存在非正整数【0或负数】的数字则返回-2；
   * */
  public static int[] changeStr2Int3(String str){
    //Step1: 是否有非数字的字符串判断
    String[] strArray=str.trim().split("\\s+"); 
    int[] a=new int[strArray.length];
    try {      
      for(int j=0;j<strArray.length;j++){
          a[j]=Integer.parseInt(strArray[j]);
      } 
    } catch (NumberFormatException e) {
        System.out.println("Error happen in HmArray.changeStr2DoubleSingle\n"+e);
        a[0]=-1;
    }
    //Step2: 是否存在负数的判断
    if(a[0]>0){//判断是否有负数，如果有负数也返回错误的提示
      for(int i=0;i<a.length;i++){
          if(a[i]<0){
            a[0]=-2;
            break;
          }
      }
    }
    return a;
  }
  /**将一个整数值插入到一个整数数组指定位置构成一个新的一维整数数组
   * @param originArr 原始一维整数数组
   * @param newVal 被插入的整数值
   * @param pos 插入的位置，大于等于0，小于等于原始数组长度的整数值
   * @return 新的整数数组
   * */
  public static int[] arrayInsert(int[] originArr,int newVal,int pos){
	  int cols=originArr.length;
	  int[] newArray=new int[cols+1];
	  pos=Math.min(pos, cols);//防止超过原始数组长度的位置
	  pos=Math.max(0, pos);//防止小于0的位置出现
	  for(int i=0;i<pos;i++){
		  newArray[i]=originArr[i];
	  }
	  newArray[pos]=newVal;
	  for(int i=pos+1;i<cols+1;i++){
		  newArray[i]=originArr[i-1];
	  }  
	  return newArray;
  }
  
  /**
   * 实现将第二个数组接到第一个数组后面形成一个新的数组
   * @param array1 被连接数组
   * @param array2 连接数组
   * @return 二维实数数组
   * */
  public static double[][] arrayAdd(double[][] array1,double[][] array2){
    int cols1=array1[0].length;
    int cols2=array2[0].length;
    int row1=array1.length;
    int row2=array2.length;
    double[][] newArray=new double[row1+row2][cols1];
    if(cols1!=cols2){
      System.out.println("arrayAdd has error: the input two arrays don't have the same column");
    }else{
      for(int i=0;i<row1;i++){
        newArray[i]=Arrays.copyOf(array1[i],cols1);
      }
      for(int i=0;i<row2;i++){
        newArray[i+row1]=Arrays.copyOf(array2[i], cols1);
      }
    }
    return newArray;
  }
  /**
   * 实现将第二个数组接到第一个数组后面形成一个新的数组
   * @param array1 被连接数组
   * @param array2 连接数组
   * @return 二维实数数组
   * */
  public static int[][] arrayAdd(int[][] array1,int[][] array2){
    int cols1=array1[0].length;
    int cols2=array2[0].length;
    int row1=array1.length;
    int row2=array2.length;
    int[][] newArray=new int[row1+row2][cols1];
    if(cols1!=cols2){
      System.out.println("arrayAdd has error: the input two arrays don't have the same column");
    }else{
      for(int i=0;i<row1;i++){
        newArray[i]=Arrays.copyOf(array1[i],cols1);
      }
      for(int i=0;i<row2;i++){
        newArray[i+row1]=Arrays.copyOf(array2[i], cols1);
      }
    }
    return newArray;
  }  

  /**实数数组深度复制
   * @param inArray 输入的二维数组
   * @return 返回深度复制的二维数组，内容同输入的二维数组一样，但是内存地址不同
   * */
  public static double[][] deepCopy(double[][] inArray){
    int row=inArray.length;
    int col=inArray[0].length;
    double[][] outArray=new double[row][col];
    for(int i=0;i<row;i++){
      for(int j=0;j<col;j++){
        outArray[i][j]=inArray[i][j];
      }
    }
    return outArray;
  }
  /**整数二维数组深度复制
   * @param inArray 输入的二维数组
   * @return 返回深度复制的二维数组，内容同输入的二维数组一样，但是内存地址不同
   * */
  public static int[][] deepCopy(int[][] inArray){
    int row=inArray.length;
    int col=inArray[0].length;
    int[][] outArray=new int[row][col];
    for(int i=0;i<row;i++){
      for(int j=0;j<col;j++){
        outArray[i][j]=inArray[i][j];
      }
    }
    return outArray;
  }   
  /**将二维数组中的两行进行互换
   * @param inArray 进行两行互换的二维数组
   * @param rowId1 互换的第一个行号【从1开始编号】
   * @param rowId2 互换的第二个行号【从1开始编号】
   * @return 互换了rowId1和rowId2两行数据的inArray数组
   * */
  public static int[][] exchangeTwoRow(int[][] inArray,int rowId1,int rowId2){
    int[][] outArray=deepCopy(inArray);
    int[] rowData1=outArray[rowId1-1];
    outArray[rowId1-1]=outArray[rowId2-1];
    outArray[rowId2-1]=rowData1;
    return outArray; 
  }
  

  
  
  /**判断两个二维数组的值是否相同
   * @param array1 二维整数数组
   * @param array2 二维整数数组
   * @return 如果两个输入的二维数组在任何对应的位置的值均相等，则返回true；否则返回false
   * */
  public static boolean isSame(int[][] array1, int[][] array2){
    int row1=array1.length;
    int col1=array1[0].length;
    int row2=array2.length;
    int col2=array2[0].length;
    boolean same=true;
    if(row1!=row2||col1!=col2){
      same=false;
    }else{
      for(int i=0;i<row1;i++){
        for(int j=0;j<col1;j++){
          if(array1[i][j]!=array2[i][j]){
            same=false;
            break;
          }
        }
        if(!same){
          break;
        }
      }
    }
    return same;
  }
  
  /**整数三维数组深度添加一个新的二维数组，以扩充三维数组，三维数组第一个纬度长度加1,
   * 注意必须保持原始二维数组第二个纬度和第三个纬度长度要与新增加的二维数组的第一个纬度和第二个纬度的长度依次相等，否则返回null
   * @param originArray 原始三维数组
   * @param addArray 新增加的二维数组
   * @return 返回深度添加了一个二维数组后的三维数组，增加的二维数组处于该三维数组的最后【第一个纬度】
   * */
  public static int[][][] deepAdd(int[][][] originArray,int[][] addArray){
    int pop=originArray.length;
    int row1=originArray[0].length;
    int col1=originArray[0][0].length;
    int row2=addArray.length;
    int col2=addArray[0].length;
    int[][][] newArray=new int[pop+1][row1][col1];
    if(row1==row2&&col1==col2){
      for(int p=0;p<pop;p++){
        newArray[p]=deepCopy(originArray[p]);
      }
      for(int i=0;i<row1;i++){
        for(int j=0;j<col1;j++){
          newArray[pop][i][j]=addArray[i][j];
        }
      }
      return newArray;
    }else{
      System.out.println("Error happens in EasyArray.deepAdd(), the dimension lengths have error");
      return null;
    }
  }  
  
  /**整数三维数组深度添加一个新的二维数组，以扩充三维数组，三维数组第一个纬度长度加1,
   * 注意必须保持原始二维数组第二个纬度和第三个纬度长度要与新增加的二维数组的第一个纬度和第二个纬度的长度依次相等，否则返回null
   * @param originArray 原始三维数组
   * @param addArray 新增加的二维数组
   * @return 返回深度添加了一个二维数组后的三维数组，增加的二维数组处于该三维数组的最后【第一个纬度】
   * */
  public static int[][][] deepAdd(int[][][] originArray,int[][][] addArray){
    int pop1=originArray.length;
    int row1=originArray[0].length;
    int col1=originArray[0][0].length;
    int pop2=addArray.length;
    int row2=addArray[0].length;
    int col2=addArray[0][0].length;
    int[][][] newArray=new int[pop1+pop2][row1][col1];
    if(row1==row2&&col1==col2){
      for(int p=0;p<pop1;p++){
        newArray[p]=deepCopy(originArray[p]);
      }
      for(int p=0;p<pop2;p++){
        newArray[pop1+p]=deepCopy(addArray[p]);
      }
      return newArray;
    }else{
      System.out.println("Error happens in EasyArray.deepAdd(), the dimension lengths have error");
      return null;
    }
  }    
  
  /**整数三维数组深度复制
   * @param inArray 输入的三维数组
   * @return 返回深度复制的三维数组，内容同输入的三维数组一样，但是内存地址不同
   * */
  public static int[][][] deepCopy(int[][][] inArray){
    int pop=inArray.length;
    int row=inArray[0].length;
    int col=inArray[0][0].length;
    int[][][] outArray=new int[pop][row][col];
    for(int p=0;p<pop;p++){
      for(int i=0;i<row;i++){
        for(int j=0;j<col;j++){
          outArray[p][i][j]=inArray[p][i][j];
        }
      }
    }
    return outArray;
  }   
  
  /**
   * 根据第二个参数中的值是否为1决定是否保留第一个参数中的行向量
   * @param array1 被删除的数组
   * @param isRetainArr 对应行是否保留，若为1则第一个数组对应的行保留，否则删除
   * @return 二维实数数组
   * */
  public static double[][] arrayDelete(double[][] array1,int[] isRetainArr){
    int cols1=array1[0].length;
    int row1=array1.length;
    int row2=isRetainArr.length;
    int remainQty=0;
    double[][] newArray=new double[row1][cols1];
    if(row1!=row2){
      System.out.println("the second array don't has the same rows as the first");
    }else{
      for(int i=0;i<row1;i++){
        if(isRetainArr[i]==1){
          newArray[remainQty]=Arrays.copyOf(array1[i],cols1);
          remainQty++;
        }
      }
    }
    double[][] newArray2=new double[remainQty][cols1];    
    for(int i=0;i<remainQty;i++){
      newArray2[i]=Arrays.copyOf(newArray[i],cols1);
    }
    return newArray2;
  }  

  /**
   * 根据第二个参数中的值是否为1决定是否保留第一个参数中的行向量
   * @param array1 被删除的数组
   * @param isRetainArr 对应行是否保留，若为1则第一个数组对应的行保留，否则删除
   * @return 二维实数数组
   * */
  public static int[][] arrayDelete(int[][] array1,int[] isRetainArr){
    int cols1=array1[0].length;
    int row1=array1.length;
    int row2=isRetainArr.length;
    int remainQty=0;
    int[][] newArray=new int[row1][cols1];
    if(row1!=row2){
      System.out.println("the second array don't has the same rows as the first");
    }else{
      for(int i=0;i<row1;i++){
        if(isRetainArr[i]==1){
          newArray[remainQty]=Arrays.copyOf(array1[i],cols1);
          remainQty++;
        }
      }
    }
    int[][] newArray2=new int[remainQty][cols1];    
    for(int i=0;i<remainQty;i++){
      newArray2[i]=Arrays.copyOf(newArray[i],cols1);
    }
    return newArray2;
  }  
  
  
  /**
   * 将字符串转变为二维整数数组，数值之间由分号和空格分割开
   * @param str 由分号和空格分割开的实数序列字符串
   * @return 二维整数数组
   * */
  public static int[][] changeStr2Int2(String str){
    //先从分号中分割
    String[] first=str.trim().split(";");
    //分配的二位数组
    int[][] d=new int[first.length][];
    //Step1: 是否有非数字的字符串判断
    try {      
      for(int i=0;i<first.length;i++){        
          String[] second=first[i].trim().split("\\s+");
          d[i]=new int[second.length];
          for(int j=0;j<second.length;j++){
              d[i][j]=Integer.parseInt(second[j]);
          } 
      }
    } catch (NumberFormatException e) {
        System.out.println("Error happen in WorArray.changeStr2Int2D\n"+e);
        d[0][0]=-1;
    }
    //Step2: 是否存在负数的判断
    if(d[0][0]>=0){//判断是否有负数，如果有负数也返回错误的提示
      for(int i=0;i<d.length;i++){
        if(d[i].length!=d[0].length){
          d[0][0]=-(i+1);
          break;
        }
        for(int j=0;j<d[0].length;j++){
          if(d[i][j]<0){
            d[0][0]=-1;
            break;
          }
        }
      }
    }
    return d;
  }

   
    
    /**
	 * 将字符串转变为一维整数数组，数值之间由空格分割开
     * @param str 由空格分割开的整数序列字符串
     * @return 一位整数数组
	 * */
	public static int[] changeStr2Int1(String str){
	  String s[]=str.trim().split("\\s+");;
	  int[] d=new int[s.length];
	  try {
		  for(int j=0;j<s.length;j++){
	      d[j]=Integer.parseInt(s[j]);
	    } 			
		} catch (Exception e) {
          d[0]=-1;
		  System.out.println("Error happen in HmArray.changeStr2Int\n"+e);
		}

		return d;
  }	
  
  /**
   * 根据cycleTimeStr中的数据获取作业的数量，数值之间由分号和/或空格分割开
   * 对于并行机问题来说，全部的作业时间为一行字符串，数字之间用空格分隔开；
   * 对于流水车间或作业车间调度问题来说，不同作业的工序加工时间之间用分号；隔开；
   * 根据cycleTimeStr这个字符串来获得作业的数量
   * @param cycleTimeStr 由分号和/或空格分割开的实数序列字符串
   * @return 作业数量整数值
   * */
  public static int getJobQty(String cycleTimeStr){
	  int jobQty;
	  //先从分号中分割
	  String[] dStr=cycleTimeStr.trim().split(";");
	  if(dStr.length>1) {//流水车间或作业车间
		  jobQty=dStr.length;
	  }else {
		  String[] eStr=dStr[0].trim().split("\\s+");
		  jobQty=eStr.length;		  
	  }
    return jobQty;
  }

   
	
	/**
	 * 将二维数组打印出来，符合制作甘特图的形式
     * @param schedule 调度方案二维数组
	 * */
	public static void printSchedule(int[][] schedule) {
		int rows=schedule.length;
		int cols=schedule[0].length;
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				int val=0;
				if(j<2){
					val=1;		//将机器编码和作业编码设定为从1开始编码		   
				}
				System.out.print(schedule[i][j]+val+" ");
			}
			System.out.println(";");
		}
	}

	/**
	 * 将二维数组打印出来，符合制作甘特图的形式
     * @param array 二维整数数组
	 * */
	public static void printArray(int[][] array) {
		int rows=array.length;
		int cols=array[0].length;
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				System.out.print(array[i][j]+" ");
			}
			System.out.println(";");
		}
	}
	
/**将二维数组的两列数值互换
 * @param inArray 准备互换的二维数组
 * @param col1 互换的一列列序号，从1开始编号；
 * @param col2 互换的另一列列序号，从1开始编号；
 * @return 互换两列数值后的数组
 * */
	public static double[][] changeTwoColumn(double[][] inArray,int col1,int col2){
	  double[][] outArray=EasyArray.deepCopy(inArray);
	  for(int i=0;i<inArray.length;i++){
	    outArray[i][col1-1]=inArray[i][col2-1];
	    outArray[i][col2-1]=inArray[i][col1-1];
	  }
	  return outArray;
	}
	
  /**
   * 指定独立的维数将三维数组打印出来，即如果指定的维数为1，则将第2和3维数据按照二维数组方式打印出来；
   * 如果指定的维数为2，则将第1和3维数据按照二维数组方式打印出来；
   * 如果指定的维数为3，则将第1和2维数据按照二维数组方式打印出来；
     * @param array 三维整数数组
     * @param uniqueDimension 1-3之间的整数
   * */
  public static void printArray(int[][][] array, int uniqueDimension) {
    if(uniqueDimension==1){
      for(int i=0;i<array.length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array[0].length;j++){
          for(int k=0;k<array[0][0].length;k++){
            System.out.print(array[i][j][k]+"  ");
          }
          System.out.println("");
        }
      }
    }else if(uniqueDimension==2){
      for(int i=0;i<array[0].length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array.length;j++){
          for(int k=0;k<array[0][0].length;k++){
            System.out.print(array[j][i][k]+"  ");
          }
          System.out.println("");
        }
      }
    }else if(uniqueDimension==3){
      for(int i=0;i<array[0][0].length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array.length;j++){
          for(int k=0;k<array[0].length;k++){
            System.out.print(array[j][k][i]+"  ");
          }
          System.out.println("");
        }
      }      
      
    }else{
      System.err.println("输入的参数uniqueDimension需为1、2或3");
    }
  } 
	
  /**
   * 指定独立的维数将三维数组打印出来，即如果指定的维数为1，则将第2和3维数据按照二维数组方式打印出来；
   * 如果指定的维数为2，则将第1和3维数据按照二维数组方式打印出来；
   * 如果指定的维数为3，则将第1和2维数据按照二维数组方式打印出来；
     * @param array 三维整数数组
     * @param uniqueDimension 1-3之间的整数
   * */
  public static void printArray(double[][][] array, int uniqueDimension) {
    if(uniqueDimension==1){
      for(int i=0;i<array.length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array[0].length;j++){
          for(int k=0;k<array[0][0].length;k++){
            System.out.print(array[i][j][k]+"  ");
          }
          System.out.println("");
        }
      }
    }else if(uniqueDimension==2){
      for(int i=0;i<array[0].length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array.length;j++){
          for(int k=0;k<array[0][0].length;k++){
            System.out.print(array[j][i][k]+"  ");
          }
          System.out.println("");
        }
      }
    }else if(uniqueDimension==3){
      for(int i=0;i<array[0][0].length;i++){
        System.out.println("--------------分类纬度编号为："+(i+1)+"  的二维数组值---------");        
        for(int j=0;j<array.length;j++){
          for(int k=0;k<array[0].length;k++){
            System.out.print(array[j][k][i]+"  ");
          }
          System.out.println("");
        }
      }      
      
    }else{
      System.err.println("输入的参数uniqueDimension需为1、2或3");
    }
  }	
	
	/**
	 * 将二维数组打印出来
     * @param array 二维实数数组
	 * */
	public static void printArray(double[][] array) {
		int rows=array.length;
		int cols=array[0].length;
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				System.out.print(array[i][j]+" ");
			}
			System.out.println(";");
		}
	}

	/**
	 * 将一维数组打印出来
     * @param array 一维实数数组 
	 * */
	public static void printArray(double[] array) {
		int rows=array.length;
		for(int i=0;i<rows;i++){
          System.out.print(array[i]+"   ");
		}
    System.out.println("");
	}
	public static void printArray(int[] array) {
		int rows=array.length;
		for(int i=0;i<rows;i++){
          System.out.print(array[i]+"   ");
		}
        System.out.println("");
	}
	
	/**将第一个三维数组中的第一个纬度的位置p的元素删除
	 * @param array 三维整数数组
	 * @param id 第一个纬度的id号，为正整数，从1开始编号
	 * @return 返回array中被删除了第id位置元素后剩余的三维数组
	 * */
  public static int[][][] arrayDelete1(int[][][] array, int id) {
    int pop=array.length;
    int row=array[0].length;
    int col=array[0][0].length;
    int[][][] newArray=new int[pop-1][row][col];
    for(int p=1;p<id;p++){
      newArray[p-1]=deepCopy(array[p-1]);
    }
    for(int p=id;p<pop;p++){
      newArray[p-1]=deepCopy(array[p]);
    }
    return newArray;
  }    
	    
    
}
