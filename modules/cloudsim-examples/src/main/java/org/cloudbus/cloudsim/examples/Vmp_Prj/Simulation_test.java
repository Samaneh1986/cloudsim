package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.network.exDatacenter.AppCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
import org.cloudbus.cloudsim.network.exDatacenter.DCPerformance;
import org.cloudbus.cloudsim.network.exDatacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;

public class Simulation_test {
	private static Map<Integer, List<NetworkVm>> vmlistReq; 
	private static List<AppCloudlet> appList;
	private static ManageDatacenter mDc ;
	private static NetDatacenterBroker broker;
	private static int brokerId;
	private static Calendar calendar;
	//private static List<Cloudlet> allClList;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting project simulation...");
		
		try {
			// Initialize the CloudSim library 
			int num_user = 5;   
			calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events 
			CloudSim.init(num_user, calendar, trace_flag); 
			DCMngUtility.dcPerformance = new  DCPerformance();
			String rsltFileName ="Sim_result_"+calendar.getTime().toString();
			//DCMngUtility.performanceFile= new PrintWriter("perform"+calendar.getTime().toString());
			// DCMngUtility.resultFile = new PrintWriter(rsltFileName);
			// Create data center
			mDc = new ManageDatacenter("Datacenter_001","RANDOM");
			//mDc = new ManageDatacenter("Datacenter_001","TDB");
			//mDc = new ManageDatacenter("Datacenter_001","GREEDY");
			//mDc = new ManageDatacenter("Datacenter_001","BFT");
			NetworkDatacenter datacenter001 = mDc.createDatacenter();
			// Create Broker
			NetDatacenterBroker.setLinkDC(datacenter001);
			broker = mDc.createBroker("broker_001");
			brokerId = broker.getId();
			//
			String workingDirectory = System.getProperty("user.dir");
			InfluxdbWorkload ds = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet_hbase.json");
			appList = ds.createWorkload(brokerId);
			vmlistReq = ds.createVMs(brokerId,appList);
			for(int appId : vmlistReq.keySet()) {
				List<NetworkVm> vmlist = vmlistReq.get(appId);
				broker.CreateCustomVMs(datacenter001.getId(),vmlist);
			}
			broker.getAppCloudletList().addAll(appList); 
			
			
			CloudSim.startSimulation();
			
			List<Cloudlet> newList = broker.getCloudletSubmittedList();
			
			CloudSim.stopSimulation();

			// Print results when simulation is over
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Cached "
					+ NetDatacenterBroker.cachedcloudlet + " Data transfered "
					+ NetworkConstants.totaldatatransfer + " during Time "+NetworkConstants.totaldatatransferTime);
			System.out.println("Inter Racks Data transfered amount is "+NetworkConstants.interRackDataTransfer);
			System.out.println(" ** HOSTS INFORMATION ** ");
			int totUsdHst = 0;
			int allHst = 0;
			double usagemean = 0.0;
			for(Host hs :datacenter001.getHostList()){
				 NetworkHost nvh =  (NetworkHost) hs;
				 if(nvh.getUtilizedCpu() != 0){
					 System.out.println("Host Id"+nvh.getId()+" -> used cpu prc:" +(1-nvh.getUnusedCpu())+" , mean usage: "+nvh.getUtilizedCpu());
					 totUsdHst++;
					 usagemean = usagemean + (1- nvh.getUnusedCpu());
				 } 
				allHst++;
				 }
			usagemean = usagemean / totUsdHst;
			System.out.println("Total "+totUsdHst+" hosts from "+allHst+" availbles hosts is used with mean prodactivity "+usagemean);
			
			Log.printLine("Project simulation finished!");
		}
	    catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen!!");
	    }
	}
	
	

	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		String jobFileName = "Cloudlet_info__"+calendar.getTime().toString();
		PrintWriter jobFile =  new PrintWriter(jobFileName);
		int size = list.size();
		Cloudlet cloudlet;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== Simulation results ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
			else{
				Log.print("FAILED");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
				
			}
		}
		jobFile.close();

	}

}
