package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

public class Simulation_influxdb {
	private static List<NetworkVm> vmlist; 
	private static List<AppCloudlet> appList;
	private static ManageDatacenter mDc ;
	private static NetDatacenterBroker broker1;
	private static int broker1Id;
	private static NetDatacenterBroker broker2;
	private static int broker2Id;
	private static NetDatacenterBroker broker3;
	private static int broker3Id;
	private static NetDatacenterBroker broker4;
	private static int broker4Id;
	private static NetDatacenterBroker broker5;
	private static int broker5Id;
	private static NetDatacenterBroker broker6;
	private static int broker6Id;
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
			DCMngUtility.dcPerformance = new DCPerformance();
			//String rsltFileName = "Sim_result_"+calendar.getTime().toString();
			//DCMngUtility.resultFile =  new PrintWriter(rsltFileName);
			// Create data center
			//mDc = new ManageDatacenter("Datacenter_001","RANDOM");
			//mDc = new ManageDatacenter("Datacenter_001","TDB");
			mDc = new ManageDatacenter("Datacenter_001","GREEDY");
			//mDc = new ManageDatacenter("Datacenter_001","BFT");
			NetworkDatacenter datacenter001 = mDc.createDatacenter();
			// Create Broker
			NetDatacenterBroker.setLinkDC(datacenter001);
			broker1 = mDc.createBroker("broker_001");
			broker1Id = broker1.getId();
			broker2 = mDc.createBroker("broker_002");
			broker2Id = broker2.getId();
			broker3 = mDc.createBroker("broker_003");
			broker3Id = broker3.getId();
			broker4 = mDc.createBroker("broker_004");
			broker4Id = broker4.getId();
			broker5 = mDc.createBroker("broker_005");
			broker5Id = broker5.getId();
			broker6 = mDc.createBroker("broker_006");
			broker6Id = broker5.getId();
			// create initial VMs
			String workingDirectory = System.getProperty("user.dir");
			InfluxdbWorkload ds = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet1.json");
			appList = ds.createWorkload(broker1Id);
			vmlist = ds.createVMs(broker1Id,appList);
			// assign VMs to broker related variable
			broker1.CreateCustomVMs(datacenter001.getId(),vmlist);
			// create initial cloudletApp 
			broker1.getAppCloudletList().addAll(appList); 
			
			// dynamically add workload user 2

			Runnable user2 = new Runnable() {
				@Override
				public void run() { 
					
					CloudSim.pauseSimulation(200);
					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock()< 300 ) {
							break;
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} 

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();}
					
				    Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
				    InfluxdbWorkload ds2 = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet3.json");
				    List<AppCloudlet>  applist2 = ds2.createWorkload(broker2Id);
					List<NetworkVm> vmlist2 = ds2.createVMs(broker2Id,applist2);

					broker2.CreateCustomVMs(datacenter001.getId(),vmlist2);
					broker2.getAppCloudletList().addAll(applist2);
					broker2.schedule(broker2Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
					Log.printLine(CloudSim.clock() + "<<<<<--- user 1 workload sent --->>>>>");
					
					CloudSim.resumeSimulation();
					CloudSim.pauseSimulation(800); // time to run user3 thread 
				}
			};
			
			// dynamically add workload user 3

						Runnable user3 = new Runnable() {
							@Override
							public void run() { 
								 
								while (true) {
									if (CloudSim.isPaused() && CloudSim.clock()>700 ) {
										break;
									}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} 

								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();}
								
							    Log.printLine(CloudSim.clock() +"<<<<<--- New Workload Added --->>>>>");
							    InfluxdbWorkload ds3 = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet2.json");
							    List<AppCloudlet>  applist3 = ds3.createWorkload(broker3Id);
								List<NetworkVm> vmlist3 = ds3.createVMs(broker3Id,applist3);
								broker3.CreateCustomVMs(datacenter001.getId(),vmlist3);
								broker3.getAppCloudletList().addAll(applist3);
								broker3.schedule(broker3Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
								Log.printLine(CloudSim.clock() + "<<<<<--- user 2 workload sent --->>>>>");
								
								CloudSim.resumeSimulation();
								CloudSim.pauseSimulation(1200); // time to run user4 thread 
							}
						};

						// dynamically add workload user 4

									Runnable user4 = new Runnable() {
										@Override
										public void run() {  
											 
											while (true) {
												if (CloudSim.isPaused() && CloudSim.clock()>1100 ) {
													break;
												}
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											} 

											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();}
											
										    Log.printLine(CloudSim.clock() +"<<<<<--- New Workload Added --->>>>>");
										    InfluxdbWorkload ds4 = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet1.json");
										    List<AppCloudlet>  applist4 = ds4.createWorkload(broker4Id);
											List<NetworkVm> vmlist4 = ds4.createVMs(broker4Id,applist4);

										    broker4.CreateCustomVMs(datacenter001.getId(),vmlist4);
											broker4.getAppCloudletList().addAll(applist4);
											broker4.schedule(broker4Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
											Log.printLine(CloudSim.clock() + "<<<<<--- user 4 workload sent --->>>>>");
											
											CloudSim.resumeSimulation(); 
											CloudSim.pauseSimulation(1450); // time to run user5 thread 
										}
									};
					// dynamically add workload user 5

									Runnable user5 = new Runnable() {
										@Override
										public void run() {  
											 
											while (true) {
												if (CloudSim.isPaused() && CloudSim.clock()>1400 ) {
													break;
												}
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											} 

											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();}
											
										    Log.printLine(CloudSim.clock() +"<<<<<--- New Workload Added --->>>>>");
										    InfluxdbWorkload ds5 = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet3.json");
										    List<AppCloudlet>  applist5 = ds5.createWorkload(broker5Id);
											List<NetworkVm> vmlist5 = ds5.createVMs(broker5Id,applist5);

										    broker5.CreateCustomVMs(datacenter001.getId(),vmlist5);
											broker5.getAppCloudletList().addAll(applist5);
											broker5.schedule(broker5Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
											Log.printLine(CloudSim.clock() + "<<<<<--- user 5 workload sent --->>>>>");
											
											CloudSim.resumeSimulation(); 
											CloudSim.pauseSimulation(1550); // time to run user6 thread 
										}
									};
									// dynamically add workload user 6

									Runnable user6 = new Runnable() {
										@Override
										public void run() {  
											 
											while (true) {
												if (CloudSim.isPaused() && CloudSim.clock()>1500 ) {
													break;
												}
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											} 

											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();}
											
										    Log.printLine(CloudSim.clock() +"<<<<<--- New Workload Added --->>>>>");
										    InfluxdbWorkload ds6 = new InfluxdbWorkload(workingDirectory+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj/jsonDS/dataSet2.json");
										    List<AppCloudlet>  applist6 = ds6.createWorkload(broker6Id);
											List<NetworkVm> vmlist6 = ds6.createVMs(broker6Id,applist6);

										    broker6.CreateCustomVMs(datacenter001.getId(),vmlist6);
											broker6.getAppCloudletList().addAll(applist6);
											broker6.schedule(broker6Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
											Log.printLine(CloudSim.clock() + "<<<<<--- user 5 workload sent --->>>>>");
											
											CloudSim.resumeSimulation(); 
										}
									};
									
	    
		new Thread(user2).start();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			new Thread(user3).start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
				
		new Thread(user4).start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new Thread(user5).start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new Thread(user6).start(); 
			// Starts the simulation
			//DCMngUtility.resultFile.println("Simulation started at "+calendar.getTime().toString());
			//DCMngUtility.resultFile.println("CloudSim clock is "+CloudSim.clock());
			
			CloudSim.startSimulation();
			
			List<Cloudlet> newList = broker1.getCloudletSubmittedList();
			
			CloudSim.stopSimulation();

			newList.addAll( broker2.getCloudletSubmittedList());
			newList.addAll( broker3.getCloudletSubmittedList());
			newList.addAll( broker4.getCloudletSubmittedList());
			newList.addAll( broker5.getCloudletSubmittedList());
			newList.addAll( broker6.getCloudletSubmittedList());

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
			DCMngUtility.dcPerformance.switchReport();
			
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

		//jobFile.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
		//		+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			jobFile.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				jobFile.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
				jobFile.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
			else{
				Log.print("FAILED");
				jobFile.print("FAILED");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
				jobFile.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
		jobFile.close();

	}

}
