/**
 * 
 */
package easyopt.common;

import java.util.Arrays;


/**
 * 遗传算法运算过程中涉及的相关方法
 * @author PeterWang at WOR
 */
public class GA {

	/**
	 * 根据种群规模和基因长度初始化二进制染色体种群
	 * @param popSize 种群规模，染色体数量
	 * @param geneLength 单条染色体基因长度
	 * @return chromosomes[][] 0-1变量组成的数组，染色体种群，每行为一条染色体，
	 */
	public static int[][] initBitChrome(int popSize,int geneLength){
		int[][] chromosomes=new int[popSize][geneLength];
		for(int i=0;i<popSize;i++){
			for(int j=0;j<geneLength;j++){
				chromosomes[i][j]=(int) Math.round(Math.random());
			}
		}
		return chromosomes;
	}
	
    /**
	 * 根据种群规模和基因长度初始化0-[geneLength-1]之间整数乱序的染色体种群，主要用于解决
	 * 车间调度、TSP问题和VRP问题方面
	 * @param popSize 为种群规模，染色体数量，正整数
	 * @param geneLength 单条染色体基因长度，染色体中最大数字为[geneLength-1]
	 * @return chromosomes[][] 为0-[geneLength-1]变量组成的二维数组，染色体种群，每行为一条染色体，
	 */
	public static int[][] initSequenceChrome(int popSize,int geneLength){
		int[][] chromosomes=new int[popSize][geneLength];
		for(int i=0;i<popSize;i++){
			int[] midArray=EasyMath.randPerm(geneLength);
			for(int j=0;j<geneLength;j++){
				chromosomes[i][j]=midArray[j];
			}
		}
		return chromosomes;
	}
	
    /**
	 * 对输入的初始编码进行随机乱序，进而构建染色体种群，主要用于解决
	 * 车间调度、TSP问题和VRP问题方面
	 * @param popSize 为种群规模，染色体数量，正整数
	 * @param baseSequence 基准编码，种群中每条编码均为基准编码的乱序
	 * @return chromosomes[][] 为染色体种群的二维数组，每行为一条染色体，
	 */
	public static int[][] initSequenceChrome(int popSize,int[] baseSequence){
		int geneLength=baseSequence.length;
		int[][] chromosomes=new int[popSize][geneLength];
		for(int i=0;i<popSize;i++){
			int[] midArray=EasyMath.randPerm(geneLength);
			for(int j=0;j<geneLength;j++){
				chromosomes[i][j]=baseSequence[midArray[j]];
			}
		}
		return chromosomes;
	}	
	/**
	 * 根据种群规模和基因长度初始化1-geneLength之间整数乱序的染色体种群，主要用于解决
	 * 车间调度、TSP问题和VRP问题方面
	 * @param popSize 为种群规模，染色体数量，正整数
	 * @param geneLength 单条染色体基因长度，染色体中最大数字为geneLength
	 * @return chromosomes[][] 为0-[geneLength-1]变量组成的二维数组，染色体种群，每行为一条染色体，
	 */
	public static int[][] initSequence1Chrome(int popSize,int geneLength){
		int[][] chromosomes=new int[popSize][geneLength];
		for(int i=0;i<popSize;i++){
			int[] midArray=EasyMath.randPermStart1(geneLength);
			for(int j=0;j<geneLength;j++){
				chromosomes[i][j]=midArray[j];
			}
		}
		return chromosomes;
	}

	/**
	 * 根据种群规模、作业数量、机器数量初始化machQty个[1-jobQty]之间整数乱序的染色体种群，主要用于解决
	 * 作业车间调度问题
	 * @param popSize 为种群规模，染色体数量，正整数
	 * @param jobQty 基本JSP问题中作业的数量
     * @param machQty 基本JSP问题中机器的数量，也为工序的数量
	 * @return chromosomes[][] 为machQty个[1-jobQty]变量组成的二维数组，染色体种群，每行为一条染色体，
	 */
	public static int[][] initJSP1Chrome(int popSize,int jobQty,int machQty){
		int[][] chromosomes=new int[popSize][jobQty*machQty];
		for(int i=0;i<popSize;i++){
			int[] midArray=EasyMath.randPermStart1(jobQty,machQty);
			for(int j=0;j<jobQty*machQty;j++){
				chromosomes[i][j]=midArray[j];
			}
		}
		return chromosomes;
	}
        
    
	/**
	 * 针对最小化目标函数，基于轮盘赌策略进行染色体选择，从而形成下代种群
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param fitnesses 每条染色体对应的适应度值
	 * @return 为输入种群中所选择染色体组成的新种群，
	 */
	public static int[][] selectionRoulletMin(int[][] inChrome,double[] fitnesses){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		//step1: 首先将适应度值转换为3列数据，便于后续操作，0-适应度值，1-原编号，2-累计概率值
		double[][] newFitness=new double[popSize][3];
    //找出适应度函数中的最大值
		double maxFit=fitnesses[0];
		for(int i=1;i<popSize;i++){
			maxFit=Math.max(maxFit, fitnesses[i]);
		}		
		maxFit=maxFit*1.2;    
		double newSumFit=0.0;
		for(int i=0;i<popSize;i++){
			newFitness[i][0]=maxFit-fitnesses[i];
			newFitness[i][1]=i;
			newFitness[i][2]=0;
			newSumFit+=newFitness[i][0];
		}
		//填充newFitness第三列的累计概率值
		double sumFit=newSumFit;	
    newFitness[0][2]=newFitness[0][0]/sumFit;
    for(int i=1;i<popSize;i++){
    	newFitness[i][2]=newFitness[i-1][2]+newFitness[i][0]/sumFit;
    }    
    EasyArray.printArray(newFitness);
    //System.out.println("+++++++++++++++++");
    
		//step2: 进行轮盘赌选择,形成新的种群
    int[][] newChrome=new int[popSize][geneLength];
    for(int i=0;i<popSize;i++){
    	double a=Math.random();
    	for(int j=0;j<popSize;j++){
    		if(a<=newFitness[j][2]){
    			for(int k=0;k<geneLength;k++){
    				newChrome[i][k]=inChrome[(int) newFitness[j][1]][k];
    			}
    			break;
    		}
    	}
    }

		return newChrome;
	}

	/**
	 * 针对最大化目标函数，基于轮盘赌策略进行染色体选择，从而形成下代种群
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param fitnesses 每条染色体对应的适应度值
	 * @return 为输入种群中所选择染色体组成的新种群，
	 */
	public static int[][] selectionRoulletMax(int[][] inChrome,double[] fitnesses){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		//step1: 首先将适应度值转换为3列数据，便于后续操作，0-适应度值，1-原编号，2-累计概率值
		double[][] newFitness=new double[popSize][3];
		double sumFit=EasyMath.sum(fitnesses);//求和
    for(int i=0;i<popSize;i++){
    	newFitness[i][0]=fitnesses[i];
    	newFitness[i][1]=i;
    	newFitness[i][2]=0;
    }
    //填充newFitness第三列的累计概率值
    newFitness[0][2]=newFitness[0][0]/sumFit;
    for(int i=1;i<popSize;i++){
    	newFitness[i][2]=newFitness[i-1][2]+newFitness[i][0]/sumFit;
    }    
    EasyArray.printArray(newFitness);
    //System.out.println("+++++++++++++++++");
    
		//step2: 进行轮盘赌选择,形成新的种群
    int[][] newChrome=new int[popSize][geneLength];
    for(int i=0;i<popSize;i++){
    	double a=Math.random();
    	for(int j=0;j<popSize;j++){
    		if(a<=newFitness[j][2]){
    			for(int k=0;k<geneLength;k++){
    				newChrome[i][k]=inChrome[(int) newFitness[j][1]][k];
    			}
    			break;
    		}
    	}
    }

		return newChrome;
	}
		
	
	/**
	 * 针对最小化目标函数优化问题，基于精英保留策略进行染色体选择，从而形成下代种群
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param fitnesses 每条染色体对应的适应度值
	 * @param elistQty 精英保留数量
	 * @return 为输入种群中所选择染色体组成的新种群，
	 */
	public static int[][] selectionElistMin(int[][] inChrome,double[] fitnesses,int elistQty){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		//step1: 首先将适应度值转换为3列数据，便于后续操作，0-适应度值，1-原编号，2-累计概率值
    		//  由于目标函数为最小化，注意将最小值的适应度转换为较大的适应度，引入了maxFit这个变量    
		double[][] newFitness=new double[popSize][3];
		    //找出适应度函数中的最大值
		double maxFit=fitnesses[0];
        for(int i=1;i<popSize;i++){
            maxFit=Math.max(maxFit, fitnesses[i]);
        }		
        maxFit=maxFit*1.2;   
        //有时获得的适应度值均为0，这时maxFit为0，fitnesses中的值全为0，这时就进行随机处理吧，
        //给每个fitnesses一个很小的值
        if(maxFit<=0){
          for(int i=1;i<popSize;i++){
            fitnesses[i]=Math.random();
            maxFit=Math.max(maxFit,fitnesses[i]);
          }
          maxFit=maxFit*1.2;
        }
        double newSumFit=0.0;
        for(int i=0;i<popSize;i++){
            newFitness[i][0]=maxFit-fitnesses[i];
            newFitness[i][1]=i;
            newFitness[i][2]=0;
            newSumFit+=newFitness[i][0];
        }
        EasyMath.sortArray(newFitness, new int[] {0});//升序排序，精英为数组后部
        //填充newFitness第三列的累计概率值
        double sumFit=newSumFit;	
        newFitness[0][2]=newFitness[0][0]/sumFit;
        for(int i=1;i<popSize;i++){
            newFitness[i][2]=newFitness[i-1][2]+newFitness[i][0]/sumFit;
        }   

//    HmArray.printArray(newFitness);
//    System.out.println("+++++++++++++++");
		//step2: 进行轮盘赌选择,形成新种群精英保留策略之外的个体
    int[][] newChrome=new int[popSize][geneLength];
    for(int i=elistQty;i<popSize;i++){
    	double a=Math.random();
    	for(int j=0;j<popSize;j++){
    		if(a<=newFitness[j][2]){
    			for(int k=0;k<geneLength;k++){
    				newChrome[i][k]=inChrome[(int) newFitness[j][1]][k];
    			}
    			break;
    		}
    	}
    }
    //step3: 将精英个体保留下来
    for(int i=0;i<elistQty;i++){
        int elistId=popSize-i-1;//因为精英在数组后部
        for(int k=0;k<geneLength;k++){
            newChrome[i][k]=inChrome[(int) newFitness[elistId][1]][k];
        }
    }    

		return newChrome;
	}
	
	/**
	 * 按照一定概率进行种群变异操作，【0-1变异】
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param muteRate 变异概率
	 * @return 为变异后的新种群，
	 */
	public static int[][] muteBit(int[][] inChrome,double muteRate){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length-1;
		for(int i=0;i<popSize;i++){
			if(Math.random()<muteRate){
				int idx=(int) Math.round(1.0*geneLength*Math.random());
				inChrome[i][idx]=1-inChrome[i][idx];
				//System.out.println("i :" + i +" idx  " + idx);
			}
		}
		return inChrome;
	}
	/**
	 * 按照一定概率进行种群变异操作，【两点互换变异】
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param muteRate 变异概率
	 * @return 为变异后的新种群，
	 */
	public static int[][] muteTwoPointSwap(int[][] inChrome,double muteRate){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		for(int i=0;i<popSize;i++){
			if(Math.random()<muteRate){
				//选两点
				int idx1=(int) Math.floor(1.0*geneLength*Math.random());
				int idx2=(int) Math.floor(1.0*geneLength*Math.random());
				//执行互换
				int midVal=inChrome[i][idx1];
				inChrome[i][idx1]=inChrome[i][idx2];
				inChrome[i][idx2]=midVal;
			}
		}
		return inChrome;
	}
	/**
	 * 按照一定概率进行种群变异操作，两点间基因片段【逆序变异】
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param muteRate 变异概率
	 * @return 为变异后的新种群，
	 */
	public static int[][] muteTwoPointReverse(int[][] inChrome,double muteRate){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		for(int i=0;i<popSize;i++){
			if(Math.random()<muteRate){
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
					segment[j]=inChrome[i][idx1+j];
				}
				for(int j=0;j<changeQty;j++){
					inChrome[i][idx2-j]=segment[j];
				}				

			}
		}
		return inChrome;
	}		

	/**
	 * 遗传算法交叉操作：按照一定概率进行种群交叉操作操作，两点间基因片段【次序交叉】，具体规则参考
	 * “次序交叉”或“Order Crossover”相关资料
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param crossRate 交叉概率
	 * @return 为交叉后的新种群，
	 */
	public static int[][] crossOX(int[][] inChrome,double crossRate){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		for(int i=0;i<popSize-1;i+=2){
			if(Math.random()<crossRate){
				//step1:选两点以确定交叉基因片段的起点和终点位置
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

				//step2 将两个父代提取出来
				int[] father1=new int[geneLength];
				int[] father2=new int[geneLength];
				for(int j=0;j<geneLength;j++){
					father1[j]=inChrome[i][j];
					father2[j]=inChrome[i+1][j];
				}
				//step3 执行部分映射交叉操作
				int changeQty=idx2-idx1+1;			
				int[] seg1=new int[changeQty];//存储父代1染色体交叉基因片段，最后会交叉给子代2
				int[] seg2=new int[changeQty];//存储父代2染色体交叉基因片段，最后会交叉给子代1				
				for(int j=0;j<changeQty;j++){
					seg1[j]=inChrome[i][idx1+j];
					seg2[j]=inChrome[i+1][idx1+j];
				}	
//				HmArray.printArray(seg1);
//				HmArray.printArray(seg2);
				//step3.1 定义两个子代
				int[] son1=new int[geneLength];
				int[] son2=new int[geneLength];		
				int[] isChosen=new int[geneLength];//锚定交叉过来的片段在本片段中的位置，是为1，否则为0					
				//----step3.2-----------子代2的产生-----------------//
				//对seg1中的数字出现在father2中的位置进行标定
				for(int j=0;j<changeQty;j++){
					int nowNum=seg1[j];
					for(int k=0;k<geneLength;k++){
						if(nowNum==father2[k]){
							isChosen[k]=1;
							break;
						}
					}
				}
				//将没有在seg1中出现的字码放入son2中
				int startK=0;
				for(int j=0;j<geneLength;j++){
					if(j<idx1||j>idx2){//子代中非交换片段的位置
						for(int k=startK;k<geneLength;k++){
							if(isChosen[k]==0){
								son2[j]=father2[k];
								isChosen[k]=1;	
								startK=k+1;
								break;
							}
						}
					}	
				}
				//将seg1放到son2中
				for(int j=0;j<changeQty;j++){
					son2[idx1+j]=seg1[j];
				}	
				//------step3.3---------子代1的产生-----------------//
				//对seg2中的数字出现在father1中的位置进行标定
				int[] isChosen1=new int[geneLength];//锚定交叉过来的片段在本片段中的位置，是为1，否则为0						
				for(int j=0;j<changeQty;j++){
					int nowNum=seg2[j];
					for(int k=0;k<geneLength;k++){
						if(nowNum==father1[k]){
							isChosen1[k]=1;
							break;
						}
					}
				}
				//将father1中没有在seg2中出现的字码放入son1中
				int startK1=0;
				for(int j=0;j<geneLength;j++){
					if(j<idx1||j>idx2){//子代中非交换片段的位置
						for(int k=startK1;k<geneLength;k++){
							if(isChosen1[k]==0){
								son1[j]=father1[k];
								isChosen1[k]=1;	
								startK1=k+1;
								break;
							}
						}
					}	
				}
				//将seg2放到son1中
				for(int j=0;j<changeQty;j++){
					son1[idx1+j]=seg2[j];
				}					
				//step4 用交叉后的子代替换原种群中的父代
				for(int j=0;j<geneLength;j++){
					inChrome[i][j]=son1[j];
					inChrome[i+1][j]=son2[j];					
				}				
			}
		}
		return inChrome;
	}		

	/**实数编码染色体种群两点交叉,直接传址操作，修改输入的chrome
	 * @param chrome 二维实数数组，为遗传算法的染色体种群
	 * @param crossRate 交叉概率
	 * */
	  public static void twoPointCross(double[][] chrome,double crossRate){
	    int pop=chrome.length;
	    int col=chrome[0].length;
	    for(int i=0;i<pop;i=i+2){
	      if(Math.random()<crossRate){
	        int[] twoPoints=EasyMath.getTwoDistinctNum(col);
	        for(int j=twoPoints[0];j<=twoPoints[1];j++){
	          double midVal=chrome[i][j];
	          chrome[i][j]=chrome[i+1][j];
	          chrome[i+1][j]=midVal;
	        }
	      }
	    }
	  }
	  
	  /**实数编码染色体种群两点之间的代码使用【0-1】区间随机数随机更新，达成变异，直接传址操作，修改输入的chrome
   * @param chrome 二维实数数组，为遗传算法的染色体种群
   * @param muteRate 变异概率
	   * */
    public static void twoPointMuteRand(double[][] chrome,double muteRate){
      int pop=chrome.length;
      int col=chrome[0].length;
      for(int i=0;i<pop;i++){
        if(Math.random()<muteRate){
          int[] twoPoints=EasyMath.getTwoDistinctNum(col);
          for(int j=twoPoints[0];j<=twoPoints[1];j++){
            chrome[i][j]=Math.random();
          }
        }
      }
    }  
	  
	
	/**
	 * 遗传算法交叉操作：按照一定概率进行种群交叉操作操作，两点间基因片段【基于位置的交叉】，具体规则参考
	 * “基于位置交叉”或“Position-based Crossover”相关资料
	 * @param inChrome 为当前种群数组，每行为一条染色体
	 * @param crossRate 交叉概率
	 * @return 为交叉后的新种群，
	 */
	public static int[][] crossPBX(int[][] inChrome,double crossRate){
		int popSize=inChrome.length;
		int geneLength=inChrome[0].length;
		for(int i=0;i<popSize-1;i+=2){
			if(Math.random()<crossRate){
				//step1:确定交叉的随机位置个数
				int crossPointQty=(int) Math.floor(1.0*geneLength*Math.random())+1;//至少一个，之多geneLength个
				int[] crossPoints=new int[crossPointQty];//存储随机位置编号
				int[] allRandP=EasyMath.randPerm(geneLength);
				crossPoints=Arrays.copyOfRange(allRandP, 0, crossPointQty);
				//System.out.println("PBX: " + Arrays.toString(crossPoints));
			//step2 将两个父代提取出来
				int[] father1=new int[geneLength];
				int[] father2=new int[geneLength];
				for(int j=0;j<geneLength;j++){
					father1[j]=inChrome[i][j];
					father2[j]=inChrome[i+1][j];
				}
				//step3  定义两个子代，并交叉赋值
				int[] son1=new int[geneLength];
				int[] son2=new int[geneLength];	
				for(int j=0;j<geneLength;j++){//初始化负值，便于后续判断是否设定了交叉值
					son1[j]=-1;
					son2[j]=-1;
				}
				
				//----step3.1-----------子代2的产生-----------------//
				int[] isChosen=new int[geneLength];//锚定交叉过来的片段在本片段中的位置，是为1，否则为0					
				//对seg1中的数字出现在father2中的位置进行标定
				for(int j=0;j<crossPointQty;j++){
					int idx1=crossPoints[j];					
					int nowNum=inChrome[i][idx1];
					son2[idx1]=nowNum;
					for(int k=0;k<geneLength;k++){
						if(nowNum==father2[k]){
							isChosen[k]=1;
							break;
						}
					}
				}
				//将father2中没有在seg1中出现的字码放入son2中
				int startK=0;
				for(int j=0;j<geneLength;j++){
					if(son2[j]<0){//子代中非交换片段的位置
						for(int k=startK;k<geneLength;k++){
							if(isChosen[k]==0){
								son2[j]=father2[k];
								isChosen[k]=1;	
								startK=k+1;
								break;
							}
						}
					}	
				}
				//----step3.2-----------子代2的产生-----------------//
				int[] isChosen1=new int[geneLength];//锚定交叉过来的片段在本片段中的位置，是为1，否则为0					
				//对seg2中的数字出现在father1中的位置进行标定，并将seg2中的数字交叉放到son1中对应位置
				for(int j=0;j<crossPointQty;j++){
					int idx1=crossPoints[j];					
					int nowNum=inChrome[i+1][idx1];
					son1[idx1]=nowNum;
					for(int k=0;k<geneLength;k++){
						if(nowNum==father1[k]){
							isChosen1[k]=1;
							break;
						}
					}
				}
				//将father1中没有在seg2中出现的字码放入son1中
				int startK1=0;
				for(int j=0;j<geneLength;j++){
					if(son1[j]<0){//子代中非交换片段的位置
						for(int k=startK1;k<geneLength;k++){
							if(isChosen1[k]==0){
								son1[j]=father1[k];
								isChosen1[k]=1;	
								startK1=k+1;
								break;
							}
						}
					}	
				}				
				//step4 用交叉后的子代替换原种群中的父代
				for(int j=0;j<geneLength;j++){
					inChrome[i][j]=son1[j];
					inChrome[i+1][j]=son2[j];					
				}	
			}
		}
		return inChrome;
	}		
			
}
