

import java.util.Map;

import easyopt.common.EasyArray;
import easyopt.common.EasyMath;
import easyopt.common.EasyRand;
import easyopt.shopSch.Schedule;
import easyopt.shopSch.pms.SynPMS;

public class Experiments_IGA_SynPMS {
/**测试求解同步并行机迭代贪婪算法的性能
 * @throws CloneNotSupportedException 
 * */
	public static void main(String[] args) throws CloneNotSupportedException {
				expriment();		
		
	}
	/**同步并行机最小总延误时间迭代贪婪算法实验
	 * 每运行程序一次，可以对指定数量并行机的算例进行10次实验，所以每次实验需要修改变量machQty的数值，其他参数均不变
	 * 
	 * */
	public static void expriment() throws CloneNotSupportedException{
		// 并行机数量m依次为2、4、6、8、10、12，作业数量n依次为40、60、80、100和120
		//    每种配置下实验10次，所以记录的数组长度为4[算法种类]*5【机器数量】*5【作业数量】*10【实验次数】
		int machKind=1;		
		int jobKind=1;	
		int expKind=10;//实验次数
		int algorithmKind=8;//算法数量
		int dKind=1;
		double[][] results_d=new double[machKind*jobKind*expKind*algorithmKind][6];
		//统计结果存储，0-机器数量，1-作业数量,2-d的数值，
		//    3-算法类型【1-1a，2-1b，3-2a，4-2b】，4-实验结果TT，5-计算时间s		
		double maxGen=50;
		double belta=0.995;
		int resultIdx=0;//统计结果指针-行数

		//算法参数设定
		int d=6;
		int machQty=4;//
		long startTimem=System.currentTimeMillis();
		System.out.println("machQty: "+ machQty);
		for(int n=1;n<=jobKind;n++){//作业数量循环
			//进行实验数据初始化
			int jobQty=20*n*2;
			double[] dTimes=new double[jobQty];
			double[] rTimes=new double[jobQty];
			double[] pTimes=new double[jobQty];	
			pTimes=EasyRand.uniform(jobQty, 17383, 10, 100);
			rTimes=EasyRand.uniform(jobQty, 13457, 10, 10*jobQty/machQty);
			for(int i=0;i<jobQty;i++){
				pTimes[i]=Math.round(pTimes[i]);
				rTimes[i]=Math.round(rTimes[i]);
				dTimes[i]=rTimes[i]+2*pTimes[i];
			}	
			//算法参数和对象初始化
			SynPMS synpms=new SynPMS();
			long startTime,endTime,runTime;

			for(int k=1;k<=expKind;k++){//每种实验配置多次实验
				long startTimeLoop=System.currentTimeMillis();

				int[] params1={(int) maxGen,d};	
				double[] para2b=new double[]{maxGen,d,belta};				
				double[] para4=new double[]{maxGen,belta};
				startTime=System.currentTimeMillis();
				double[][] sch1=synpms.optSynRjLsumByIG1a(pTimes, rTimes, machQty, dTimes, params1);
				double fit1=Schedule.getSumLateTime(sch1);	
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;
				//统计结果存储，0-机器数量，1-作业数量,2-d的数值，
				//    3-算法类型【1-1a，2-1b，3-2a，4-2b】，4-实验结果TT，5-计算时间s
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;
				results_d[resultIdx][3]=1;
				results_d[resultIdx][4]=fit1;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				
				startTime=System.currentTimeMillis();
				double[][] sch1b=synpms.optSynRjLsumByIG1b(pTimes, rTimes, machQty, dTimes, params1);
				double fit1b=Schedule.getSumLateTime(sch1b);
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;						
				//System.out.println("IGA1b..TotalTardiness: "+fit1b+"  runTime: "+runTime);	
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=2;
				results_d[resultIdx][4]=fit1b;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				
				startTime=System.currentTimeMillis();
				double[][] sch2=synpms.optSynRjLsumByIG2a(pTimes, rTimes, machQty, dTimes, params1);
				double fit2=Schedule.getSumLateTime(sch2);
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2a..TotalTardiness: "+fit2+"  runTime: "+runTime);	
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=3;
				results_d[resultIdx][4]=fit2;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				
				startTime=System.currentTimeMillis();
				double[][] sch2b=synpms.optSynRjLsumByIG2b(pTimes, rTimes, machQty, dTimes, para2b);
				double fit2b=Schedule.getSumLateTime(sch2b);	
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2b..TotalTardiness: "+fit2b+"  runTime: "+runTime);
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=4;
				results_d[resultIdx][4]=fit2b;
				results_d[resultIdx][5]=runTime;
				resultIdx++;						
				
				startTime=System.currentTimeMillis();
				double[][] sch3=synpms.optSynRjLsumByIG3a(pTimes, rTimes, machQty, dTimes);
				double fit3=Schedule.getSumLateTime(sch3);
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2a..TotalTardiness: "+fit2+"  runTime: "+runTime);	
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=5;
				results_d[resultIdx][4]=fit3;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				
				startTime=System.currentTimeMillis();
				double[][] sch3b=synpms.optSynRjLsumByIG3b(pTimes, rTimes, machQty, dTimes, para4);
				double fit3b=Schedule.getSumLateTime(sch3b);	
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2b..TotalTardiness: "+fit2b+"  runTime: "+runTime);
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=6;
				results_d[resultIdx][4]=fit3b;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				
				startTime=System.currentTimeMillis();
				double[][] sch4=synpms.optSynRjLsumByIG4a(pTimes, rTimes, machQty, dTimes);
				double fit4=Schedule.getSumLateTime(sch4);
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2a..TotalTardiness: "+fit2+"  runTime: "+runTime);	
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=7;
				results_d[resultIdx][4]=fit4;
				results_d[resultIdx][5]=runTime;
				resultIdx++;
				

				startTime=System.currentTimeMillis();
				double[][] sch4b=synpms.optSynRjLsumByIG4b(pTimes, rTimes, machQty, dTimes, para4);
				double fit4b=Schedule.getSumLateTime(sch4b);	
				endTime=System.currentTimeMillis();	
				runTime=(endTime-startTime)/1000;		
				//System.out.println("IGA2b..TotalTardiness: "+fit2b+"  runTime: "+runTime);
				results_d[resultIdx][0]=machQty;
				results_d[resultIdx][1]=jobQty;
				results_d[resultIdx][2]=d;						
				results_d[resultIdx][3]=8;
				results_d[resultIdx][4]=fit4b;
				results_d[resultIdx][5]=runTime;
				resultIdx++;				
				long endTimeLoop=System.currentTimeMillis();
				System.out.println("one loop time(s): "+(endTimeLoop-startTimeLoop)/1000+"  second.");
        System.out.println("IGA1a..TotalTardiness: "+fit1+"  IGA1B TT: "+fit1b
            + "  IGA2A TT: "+fit2+ "  IGA2B TT: "+fit2b
            + "  IGA3A TT: "+fit3+ "  IGA3B TT: "+fit3b
            + "  IGA4A TT: "+fit4+ "  IGA4B TT: "+fit4b);
			}//endfor k-expriment times

		}//endfor n
		long endTimem=System.currentTimeMillis();
		System.out.println("a machine's run Time: "+(endTimem-startTimem)/1000+"  second.");


		EasyArray.printArray(results_d);
		System.out.println("  -------------statistical report-------------------");		
		double[][] stat=statReport4Simple(results_d, machKind, jobKind, dKind, expKind);
		EasyArray.printArray(stat);
		
	}
	
	
	/**根据传入的二维数组进行算法性能统计：变动数字为d的值，
	 * 返回的数组包含0-机器数量，1-作业数量，2-d参数的值,3-算法，4-TT最大值，5-TT最小值，6-TT平均值，
	 * 7-TT均方差，8-RT【runTime运行时间】最大值，9-RT最小值，10-RT平均值，11-RT均方差，12-TT的RDP相对偏差百分数，13-TT的ARDP
	 * 14-RT的RDP，15-RT的ARDP
	 * @param results 算例实验的运算结果二维数组，0-机器数量，1-作业数量,2-d的数值，
	 *        3-算法类型【1-1a，2-1b，3-2a，4-2b】，4-实验结果TT，5-计算时间s
	 * */
	static double[][] statReport4d(double[][] results,int machKind,int jobKind,int dKind,int expQty){
		int row=results.length;
		int col=results[0].length;
		int algorithmQty=4;
		//首先将具体的算法场景标识搜索出来
		double[][] expData=new double[dKind*machKind*jobKind][6];//存储各种实验配置
        //0-机器数量，1-作业数量，2-d参数的值,3-算法类型的数值,4-最小TT，5-最小RT			
		double[][] report=new double[dKind*machKind*jobKind*algorithmQty][16];//存储各种实验配置
        //0-机器数量，1-作业数量，2-d参数的值,3-算法类型的数值,其他的参看方法说明		
		expData[0]=new double[]{results[0][0],results[0][1],results[0][2],0,0,0};
		report[0][0]=results[0][0];
		report[0][1]=results[0][1];
		report[0][2]=results[0][2];
		report[0][3]=results[0][3];
		int expIdx=1;	
		int rptIdx=1;
		for(int i=1;i<row;i++){
			boolean notIn=true;
			for(int j=0;j<expData.length;j++){
				if(results[i][0]==expData[j][0]&&results[i][1]==expData[j][1]
						&&results[i][2]==expData[j][2]){
					notIn=false;
					break;
				}
			}
			if(notIn){
				expData[expIdx][0]=results[i][0];
				expData[expIdx][1]=results[i][1];
				expData[expIdx][2]=results[i][2];
				expIdx++;
			}
			//
			notIn=true;
			for(int j=0;j<report.length;j++){
				if(results[i][0]==report[j][0]&&results[i][1]==report[j][1]
						&&results[i][2]==report[j][2]&&results[i][3]==report[j][3]){
					notIn=false;
					break;
				}
			}
			if(notIn){
				report[rptIdx][0]=results[i][0];
				report[rptIdx][1]=results[i][1];
				report[rptIdx][2]=results[i][2];
				report[rptIdx][3]=results[i][3];
				rptIdx++;
			}			
			
		}	
		//将results中不同机器数量、作业数量和d数值的多次实验结果挑出来，获取算法寻优的常规统计量
		//step1：将运算结果results按照算法参数【d】、机器数量、作业数量和算法类型排序，以便后续能够进行区分
		//step2:然后将该算例的最优TT和RT找到
		//step3:再将每个算法的多次实验结果的数学统计数据运算出来
		         //step1: ------------排序
		EasyMath.sortArray(results, new int[]{2,0,1,3});
		         //step2:
		System.out.println("-----------------expData------------");
		for(int i=0;i<expData.length;i++){
			double[][] allResult=new double[algorithmQty*expQty][col];
			int idx=0;
			for(int j=0;j<row;j++){
				if(results[j][0]==expData[i][0]&&results[j][1]==expData[i][1]
						&&results[j][2]==expData[i][2]){
					allResult[idx]=results[j];
					idx++;
				}				
			}
			//获得了指定机器数量、作业数量和参数d的值的特定算例的全部实验结果数据allResult,后续需要从这allResult中找出TT最小值和RT最小值
			double minTT=Double.MAX_VALUE,minRT=Double.MAX_VALUE;
			for(int j=0;j<allResult.length;j++){
				minTT=Math.min(minTT, allResult[j][4]);
				minRT=Math.min(minRT, allResult[j][5]);
			}	
//			System.out.println("i: "+i +"  expDataLength: "+expData.length);
			expData[i][4]=minTT;
			expData[i][5]=minRT;
		}
		EasyArray.printArray(expData);
		//获取每个算法每种场景的统计数据，并记录到report中
//		 * 返回的数组包含0-机器数量，1-作业数量，2-d参数的值,3-算法，4-TT最大值，5-TT最小值，6-TT平均值，
//		 * 7-TT均方差，8-RT【runTime运行时间】最大值，9-RT最小值，10-RT平均值，11-RT均方差，12-TT的RDP相对偏差百分数，13-TT的ARDP
//		 * 14-RT的RDP，15-RT的ARDP,16-该数据是否被处理了，17-处理的序号	
		for(int i=0;i<report.length;i++){
			//step1获取每种配置下每种算法的多次运算结果
			double[] allTardy=new double[expQty];
			double[] allRunTimes=new double[expQty];			
			int idx=0;
			for(int j=0;j<row;j++){
				if(results[j][0]==report[i][0]&&results[j][1]==report[i][1]
						&&results[j][2]==report[i][2]&&results[j][3]==report[i][3]){
					allTardy[idx]=results[j][4];
					allRunTimes[idx]=results[j][5];
					idx++;
				}				
			}
			//根据allTardy和allRunTimes数组，计算填充report后续的内容
			Map<String, Double>  mapTT=EasyMath.commonStat(allTardy);
			report[i][4]=mapTT.get("max");
			report[i][5]=mapTT.get("min");
			report[i][6]=mapTT.get("mean");
			report[i][7]=mapTT.get("sigma");
			//根据allRunTimes数组，计算填充report后续的内容
			Map<String, Double>  mapRT=EasyMath.commonStat(allRunTimes);
			report[i][8]=Math.round(100*mapRT.get("max"))/100.0;
			report[i][9]=Math.round(100*mapRT.get("min"))/100.0;
			report[i][10]=Math.round(100*mapRT.get("mean"))/100.0;
			report[i][11]=Math.round(100*mapRT.get("sigma"))/100.0;
			//---计算RDP和ARDP
			//  --首先要获取该算法对应场景的全部算法的目标函数最小值
			double myMinTT=0,myMinRT=0;
			for(int j=0;j<expData.length;j++){
				if(expData[j][0]==report[i][0]&&expData[j][1]==report[i][1]
						&&expData[j][2]==report[i][2]){
					myMinTT=expData[j][4];
					myMinRT=expData[j][5];
					break;
				}				
			}
			//   计算RPD和ARDP
			double rpdTT=Math.round(1000*(report[i][5]-myMinTT)/Math.max(1, myMinTT))/10.0;
			double arpdTT=Math.round(1000*(report[i][6]-myMinTT)/Math.max(1, myMinTT))/10.0;
			double rpdRT=Math.round(1000*(report[i][9]-myMinRT)/Math.max(1, myMinRT))/10.0;
			double arpdRT=Math.round(1000*(report[i][10]-myMinRT)/Math.max(1, myMinRT))/10.0;	
			report[i][12]=rpdTT;
			report[i][13]=arpdTT;
			report[i][14]=rpdRT;
			report[i][15]=arpdRT;
			
		}		
		return report;
	}

	/**根据传入的二维数组进行算法性能统计,就是唯一的算例的八种算法的结果进行分析，
	 * 返回的数组包含0-机器数量，1-作业数量，2-d参数的值,3-算法，4-TT最大值，5-TT最小值，6-TT平均值，
	 * 7-TT均方差，8-RT【runTime运行时间】最大值，9-RT最小值，10-RT平均值，11-RT均方差，12-TT的RDP相对偏差百分数，13-TT的ARDP
	 * 14-RT的RDP，15-RT的ARDP
	 * @param results 算例实验的运算结果二维数组，0-机器数量，1-作业数量,2-d的数值，
	 *        3-算法类型【1-1a，2-1b，3-2a，4-2b】，4-实验结果TT，5-计算时间s
	 * */
	static double[][] statReport4Simple(double[][] results,int machKind,int jobKind,int dKind,int expQty){
		int row=results.length;
		int col=results[0].length;
		int algorithmQty=8;
		//首先将具体的算法场景标识搜索出来
		double[][] expData=new double[dKind*machKind*jobKind][6];//存储各种实验配置
        //0-机器数量，1-作业数量，2-d参数的值,3-算法类型的数值,4-最小TT，5-最小RT			
		double[][] report=new double[dKind*machKind*jobKind*algorithmQty][16];//存储各种实验配置
        //0-机器数量，1-作业数量，2-d参数的值,3-算法类型的数值,其他的参看方法说明		
		expData[0]=new double[]{results[0][0],results[0][1],results[0][2],0,0,0};
		report[0][0]=results[0][0];
		report[0][1]=results[0][1];
		report[0][2]=results[0][2];
		report[0][3]=results[0][3];
		int expIdx=1;	
		int rptIdx=1;
		for(int i=1;i<row;i++){
			boolean notIn=true;
			for(int j=0;j<expData.length;j++){
				if(results[i][0]==expData[j][0]&&results[i][1]==expData[j][1]
						&&results[i][2]==expData[j][2]){
					notIn=false;
					break;
				}
			}
			if(notIn){
				expData[expIdx][0]=results[i][0];
				expData[expIdx][1]=results[i][1];
				expData[expIdx][2]=results[i][2];
				expIdx++;
			}
			//
			notIn=true;
			for(int j=0;j<report.length;j++){
				if(results[i][0]==report[j][0]&&results[i][1]==report[j][1]
						&&results[i][2]==report[j][2]&&results[i][3]==report[j][3]){
					notIn=false;
					break;
				}
			}
			if(notIn){
				report[rptIdx][0]=results[i][0];
				report[rptIdx][1]=results[i][1];
				report[rptIdx][2]=results[i][2];
				report[rptIdx][3]=results[i][3];
				rptIdx++;
			}			
			
		}	
		//将results中不同机器数量、作业数量和d数值的多次实验结果挑出来，获取算法寻优的常规统计量
		//step1：将运算结果results按照算法参数【d】、机器数量、作业数量和算法类型排序，以便后续能够进行区分
		//step2:然后将该算例的最优TT和RT找到
		//step3:再将每个算法的多次实验结果的数学统计数据运算出来
		         //step1: ------------排序
		EasyMath.sortArray(results, new int[]{2,0,1,3});
		         //step2:
		System.out.println("-----------------expData------------");
		for(int i=0;i<expData.length;i++){
			double[][] allResult=new double[algorithmQty*expQty][col];
			int idx=0;
			for(int j=0;j<row;j++){
				if(results[j][0]==expData[i][0]&&results[j][1]==expData[i][1]
						&&results[j][2]==expData[i][2]){
					allResult[idx]=results[j];
					idx++;
				}				
			}
			//获得了指定机器数量、作业数量和参数d的值的特定算例的全部实验结果数据allResult,后续需要从这allResult中找出TT最小值和RT最小值
			double minTT=Double.MAX_VALUE,minRT=Double.MAX_VALUE;
			for(int j=0;j<allResult.length;j++){
				minTT=Math.min(minTT, allResult[j][4]);
				minRT=Math.min(minRT, allResult[j][5]);
			}	
//			System.out.println("i: "+i +"  expDataLength: "+expData.length);
			expData[i][4]=minTT;
			expData[i][5]=minRT;
		}
		EasyArray.printArray(expData);
		//获取每个算法每种场景的统计数据，并记录到report中
//		 * 返回的数组包含0-机器数量，1-作业数量，2-d参数的值,3-算法，4-TT最大值，5-TT最小值，6-TT平均值，
//		 * 7-TT均方差，8-RT【runTime运行时间】最大值，9-RT最小值，10-RT平均值，11-RT均方差，12-TT的RDP相对偏差百分数，13-TT的ARDP
//		 * 14-RT的RDP，15-RT的ARDP,16-该数据是否被处理了，17-处理的序号	
		for(int i=0;i<report.length;i++){
			//step1获取每种配置下每种算法的多次运算结果
			double[] allTardy=new double[expQty];
			double[] allRunTimes=new double[expQty];			
			int idx=0;
			for(int j=0;j<row;j++){
				if(results[j][0]==report[i][0]&&results[j][1]==report[i][1]
						&&results[j][2]==report[i][2]&&results[j][3]==report[i][3]){
					allTardy[idx]=results[j][4];
					allRunTimes[idx]=results[j][5];
					idx++;
				}				
			}
			//根据allTardy和allRunTimes数组，计算填充report后续的内容
			Map<String, Double>  mapTT=EasyMath.commonStat(allTardy);
			report[i][4]=mapTT.get("max");
			report[i][5]=mapTT.get("min");
			report[i][6]=mapTT.get("mean");
			report[i][7]=mapTT.get("sigma");
			//根据allRunTimes数组，计算填充report后续的内容
			Map<String, Double>  mapRT=EasyMath.commonStat(allRunTimes);
			report[i][8]=Math.round(100*mapRT.get("max"))/100.0;
			report[i][9]=Math.round(100*mapRT.get("min"))/100.0;
			report[i][10]=Math.round(100*mapRT.get("mean"))/100.0;
			report[i][11]=Math.round(100*mapRT.get("sigma"))/100.0;
			//---计算RDP和ARDP
			//  --首先要获取该算法对应场景的全部算法的目标函数最小值
			double myMinTT=0,myMinRT=0;
			for(int j=0;j<expData.length;j++){
				if(expData[j][0]==report[i][0]&&expData[j][1]==report[i][1]
						&&expData[j][2]==report[i][2]){
					myMinTT=expData[j][4];
					myMinRT=expData[j][5];
					break;
				}				
			}
			//   计算RPD和ARDP
			double rpdTT=Math.round(1000*(report[i][5]-myMinTT)/Math.max(1, myMinTT))/10.0;
			double arpdTT=Math.round(1000*(report[i][6]-myMinTT)/Math.max(1, myMinTT))/10.0;
			double rpdRT=Math.round(1000*(report[i][9]-myMinRT)/Math.max(1, myMinRT))/10.0;
			double arpdRT=Math.round(1000*(report[i][10]-myMinRT)/Math.max(1, myMinRT))/10.0;	
			report[i][12]=rpdTT;
			report[i][13]=arpdTT;
			report[i][14]=rpdRT;
			report[i][15]=arpdRT;
			
		}		
		return report;
	}
	
	
}
