package easyopt.common;

import java.util.Arrays;

/**封装优化过程中所使用的各种编码方法
 * */
public class Encode {
	/**使用单一编码方式生成订单可拆分并行机调度优化中编码生成
	 * @param unitQtyInJobs 一维正整数数组，每个元素的值不能小于等于0，表示每个作业任务中的单位作业数量
	 * @param paralleMachQty 并行机数量，为不小于2的正整数
	 * @return 返回由unitQtyInJobs[i]个i和parallelMachQty-1个0组成的随机乱码数组，作业i从1开始编码
	 * */
	public static int[] singleEncoding4SPMS(int[] unitQtyInJobs, int paralleMachQty) {
		int jobQty=unitQtyInJobs.length;
		int sumUnitQty=EasyMath.sum(unitQtyInJobs);
		int[] code=new int[sumUnitQty+paralleMachQty-1];
		int idx=0;
		for(int i=0;i<jobQty;i++) {
			for(int j=0;j<unitQtyInJobs[i];j++) {
				code[idx]=i+1;
				idx++;
			}
		}
		for(int i=sumUnitQty;i<code.length;i++) {
			code[idx]=0;
			idx++;
		}
		//乱序处理一下
		int[] randIdx=EasyMath.randPerm(code.length);
		int[] randCode=new int[code.length];
		for(int i=0;i<code.length;i++) {
			randCode[i]=code[randIdx[i]];
		}
		return randCode;
		
	}

	/**根据2-opt原则生成输入一维数组的众多组合形式，并排序交换位置上的数字一样的解
	 * @param inArray 一维整数数组
	 * @return 二维整数数组，每一行均为输入数组inArray中两个位置值互换的形式
	 * */
	public static int[][] greedyArraysBy2opt(int[] inArray){
		int cols=inArray.length;
		int[][] opt2=EasyMath.combin2(cols-1);
		int rows=opt2.length;
		int[][] cumbSolutions=new int[rows][cols];
		int rowIdx=0;
		for(int i=0;i<rows;i++){
			int changePos1=opt2[i][0];
			int changePos2=opt2[i][1];
			if(inArray[changePos1]!=inArray[changePos2]){//The value of the two change positions are not equal
				cumbSolutions[rowIdx]=Arrays.copyOf(inArray, cols);
				cumbSolutions[rowIdx][changePos1]=inArray[changePos2];
				cumbSolutions[rowIdx][changePos2]=inArray[changePos1];
				rowIdx++;
			}
		}
		int[][] outArray=new int[rowIdx][cols];	
		for(int i=0;i<rowIdx;i++){
			outArray[i]=cumbSolutions[i];
		}
		return outArray;
		
	}
	
}
