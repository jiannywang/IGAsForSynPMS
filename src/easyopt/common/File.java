package easyopt.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import easyopt.model.HFSPIdentical;

/**对一些用于读取算例数据文件的方法进行封装的类
 * */
public class File {

  /**读取输入数据文件路径中的数据，形成对应的同质并行机混合流水车间调度算例对象
   * 输入文件中数据的模板参看"Design of a testbed for hybrid flow shop scheduling with identical machines"文中给出的算例文件，
   * txt数据格式网址为：www.iescm.com/instances
   * 或者http://grupo.us.es/oindustrial/en/research/results
   * */
  public static HFSPIdentical readFileHFSP(String txtPath) {
    HFSPIdentical hfsp=new HFSPIdentical();
    String filePath = txtPath;
    FileInputStream fin=null;
    try {
      fin = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    InputStreamReader reader = new InputStreamReader(fin);
    BufferedReader buffReader = new BufferedReader(reader);
    List<String> strList=new ArrayList<String>();
    String strTmp = "";
    try {
      while((strTmp = buffReader.readLine())!=null){
        strList.add(strTmp);
//        System.out.println(strTmp);
      }     
      //拆分并进行数据提取--作业数量和工序数量
      String[] firstRow=strList.get(0).trim().split("\\s+");
      hfsp.jobQty=Integer.parseInt(firstRow[0]);
      hfsp.processQty=Integer.parseInt(firstRow[1]);
      //拆分并进行数据提取--各道工序中机器的数量      
      String[] secondRow=strList.get(1).trim().split("\\s+");
      int[] machQtys=new int[hfsp.processQty];
      for(int m=0;m<hfsp.processQty;m++){
        machQtys[m]=Integer.parseInt(secondRow[m]);
      }
      hfsp.procMachQty=machQtys;
      //拆分并进行数据提取--每个作业在各道工序的工时
      double[][] processTimes=new double[hfsp.jobQty][hfsp.processQty];
      for(int j=2;j<strList.size();j++){
        String[] midStr=strList.get(j).trim().split("\\s+");
        if(midStr.length!=hfsp.jobQty){
          System.out.println(" input data has error, the jobQty is not equal to the processTimes's column");
        }
        for(int k=0;k<hfsp.jobQty;k++){
          processTimes[k][j-2]=Double.parseDouble(midStr[k]);
        }
      }
      hfsp.processTimes=processTimes;
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      buffReader.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    
    return hfsp;
  }
  
}
