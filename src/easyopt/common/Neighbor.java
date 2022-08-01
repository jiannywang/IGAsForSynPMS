package easyopt.common;
/**The common methods of getting neighbors for optimization algorithms 
 * */
public class Neighbor {
	/**
	 * pairwiseSwap: swap the positions of two points chosed randomly of the input code
	 * @param inCode the current solution's code
	 * @return the neighbor code of the inCode，
	 */
	public static int[] pairwiseSwap(int[] inCode){
		int geneLength=inCode.length;
		//选两点
		int idx1=(int) Math.floor(1.0*geneLength*Math.random());
		int idx2=(int) Math.floor(1.0*geneLength*Math.random());
		//执行互换
		int midVal=inCode[idx1];
		inCode[idx1]=inCode[idx2];
		inCode[idx2]=midVal;
		return inCode;
	} 
	/**
	 * pairwiseReverse: reverse the codes between two points chosed randomly of the input code
	 * @param inCode the current solution's code
	 * @return the neighbor code of the inCode，
	 */
	public static int[] pairwiseReverse(int[] inCode){
		int geneLength=inCode.length;
		//选两点
		int idx1=(int) Math.floor(1.0*geneLength*Math.random());
		int idx2=(int) Math.floor(1.0*geneLength*Math.random());
		//判断两点大小，并将小的作为第一个点，大的作为第2个点
		if(idx1==idx2){
            if(idx1<=geneLength/2){//两点落在染色体的前半部分
				int moveLength=Math.max(1, (int)(1.0*geneLength/2.0*Math.random()));
				idx2+=moveLength;
			}else{//两点落在染色体的后半部分
				int moveLength=Math.max(1, (int)(1.0*geneLength/2.0*Math.random()));
				idx1-=moveLength;						
			}					
		}else{
			int a=Math.min(idx1, idx2);
			int b=Math.max(idx1, idx2);
			idx1=a;
			idx2=b;
		}

		//执行数组逆序操作
		int changeQty=idx2-idx1+1;
		int[] segment=new int[changeQty];
		for(int j=0;j<changeQty;j++){
			segment[j]=inCode[idx1+j];
		}
		for(int j=0;j<changeQty;j++){
			inCode[idx2-j]=segment[j];
		}
		return inCode;
	} 	
	
	/**
	 * pairwiseReverse: reverse the codes between two points chosed randomly of the input code
	 * @param inCode the current solution's code
	 * @param twoPosition the two positions deciding the reverse segment of the inCode, 
	 *            the reverse segment is the part between [twoPosition[0],twoPosition[1]] of the inCode
	 * @return the neighbor code of the inCode，
	 */
	public static int[] pairwiseReverse(int[] inCode,int[] twoPosition){
		int geneLength=inCode.length;
		//选两点
		int idx1=twoPosition[0];
		int idx2=twoPosition[1];
		//判断两点大小，并将小的作为第一个点，大的作为第2个点
		if(idx1==idx2){
            if(idx1<=geneLength/2){//两点落在染色体的前半部分
				int moveLength=Math.max(1, (int)(1.0*geneLength/2.0*Math.random()));
				idx2+=moveLength;
			}else{//两点落在染色体的后半部分
				int moveLength=Math.max(1, (int)(1.0*geneLength/2.0*Math.random()));
				idx1-=moveLength;						
			}					
		}else{
			int a=Math.min(idx1, idx2);
			int b=Math.max(idx1, idx2);
			idx1=a;
			idx2=b;
		}

		//执行数组逆序操作
		int changeQty=idx2-idx1+1;
		int[] segment=new int[changeQty];
		for(int j=0;j<changeQty;j++){
			segment[j]=inCode[idx1+j];
		}
		for(int j=0;j<changeQty;j++){
			inCode[idx2-j]=segment[j];
		}
		return inCode;
	}	

}
