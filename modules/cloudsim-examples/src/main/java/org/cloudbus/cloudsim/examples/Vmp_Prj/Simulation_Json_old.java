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
import org.cloudbus.cloudsim.network.exDatacenter.NetStorageHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetStorageManager;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;

public class Simulation_Json_old {
	//private static Map<Integer, List<NetworkVm>> vmlistReq; 
	private static List<AppCloudlet> appList;
	//private static ManageDatacenter mDc ;
	private static ManageSpineLeafDatacenter mDc ;
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
	private static NetDatacenterBroker broker7;
	private static int broker7Id;
	private static NetDatacenterBroker broker8;
	private static int broker8Id;
	private static NetDatacenterBroker broker9;
	private static int broker9Id;
	private static NetDatacenterBroker broker10;
	private static int broker10Id;
	private static Calendar calendar;
	// private static List<Cloudlet> allClList;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting project simulation...");
		String workingDirectory = System.getProperty("user.dir")+"/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj";

		try {
			// Initialize the CloudSim library
			int num_user = 7;
			calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events
			CloudSim.init(num_user, calendar, trace_flag);
			DCMngUtility.dcPerformance = new DCPerformance();
			 String rsltFileName ="Sim_result_"+calendar.getTime().toString();
			// DCMngUtility.performanceFile= new PrintWriter("perform"+calendar.getTime().toString());
			 DCMngUtility.resultFile = new PrintWriter(rsltFileName);
			// Create data center
			 //mDc = new ManageDatacenter("Datacenter_001","RANDOM");
			// mDc = new ManageDatacenter("Datacenter_001","BFT");
			//mDc = new ManageDatacenter("Datacenter_001", "GREEDY");
			  // mDc = new ManageDatacenter("Datacenter_001","TDB");
			
			 //mDc = new ManageSpineLeafDatacenter("Datacenter_001","BFT");
			 // mDc = new ManageSpineLeafDatacenter("Datacenter_001","GREEDY");
			mDc = new ManageSpineLeafDatacenter("Datacenter_001","TDB");
			 
			NetworkDatacenter datacenter001 = mDc.createDatacenter();
			NetDatacenterBroker.setLinkDC(datacenter001);
			NetStorageManager.setLinkDC(datacenter001);
			//
			//String mapperPath = workingDirectory +"/placement_information.csv";
			//NetStorageManager storageMgr = mDc.createStorageMgr("CEPH001",datacenter001.Storagelist,mapperPath);
			//datacenter001.storageManagerId =storageMgr.getId(); 
			// Create Broker
			
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
			broker6Id = broker6.getId();
			broker7 = mDc.createBroker("broker_007");
			broker7Id = broker7.getId();
			broker8 = mDc.createBroker("broker_008");
			broker8Id = broker8.getId();
			broker9 = mDc.createBroker("broker_009");
			broker9Id = broker9.getId();
			broker10 = mDc.createBroker("broker_010");
			broker10Id = broker10.getId();
			// create initial VMs
			
			JsonWorkloadGenerator ds = new JsonWorkloadGenerator(workingDirectory + "/jsonDS/dataSet1.json");
			appList = ds.createWorkload(broker1Id);
			//for(AppCloudlet app : appList){
			//	storageMgr.assignInitStorageToApp(app, "pool001");
			//}
			
			Map<Integer, List<NetworkVm>> vmlistReq = ds.createVMs(broker1Id, appList);
			// assign VMs to broker related variable
			for (int appId : vmlistReq.keySet()) {
				List<NetworkVm> vmlist = vmlistReq.get(appId);
				broker1.CreateCustomVMs(datacenter001.getId(), vmlist);
			}
			// create initial cloudletApp
			broker1.getAppCloudletList().addAll(appList);

			// dynamically add workload user 2

			Runnable user2 = new Runnable() {
				@Override
				public void run() {

					CloudSim.pauseSimulation(50);
					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() < 100) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds2 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_hive_IO.json");
					List<AppCloudlet> applist2 = ds2.createWorkload(broker2Id);
					//for(AppCloudlet app : applist2){
					//	storageMgr.assignInitStorageToApp(app, "pool001");
					//}
					Map<Integer, List<NetworkVm>> vmlistReq2 = ds2.createVMs(broker2Id, applist2);
					long pauseTime=70;
					//for (int appId : vmlistReq2.keySet()) {
					for(int appIndex=0; appIndex<applist2.size(); appIndex++ ){
						int appId = applist2.get(appIndex).appID;
						List<NetworkVm> vmlist2 = vmlistReq2.get(appId);
						broker2.CreateCustomVMs(datacenter001.getId(), vmlist2);
						broker2.getAppCloudletList().add(applist2.get(appIndex));
						broker2.schedule(broker2Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 2 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist2.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
							//	System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 1330) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+20;
					}
					CloudSim.pauseSimulation(1340); // time to run user3 thread
				}
			};

			// dynamically add workload user 3

			Runnable user3 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 1330) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds3 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_SWIM_IO.json");
					List<AppCloudlet> applist3 = ds3.createWorkload(broker3Id);
					//for(AppCloudlet app : applist3){
					//	storageMgr.assignInitStorageToApp(app, "pool001");
					//}
					Map<Integer, List<NetworkVm>> vmlistReq3 = ds3.createVMs(broker3Id, applist3);
					//for (int appId : vmlistReq3.keySet()) {
					long pauseTime=1350;
					for(int appIndex=0; appIndex<applist3.size(); appIndex++ ){
						int appId = applist3.get(appIndex).appID;
						List<NetworkVm> vmlist3 = vmlistReq3.get(appId);
						broker3.CreateCustomVMs(datacenter001.getId(), vmlist3);
						broker3.getAppCloudletList().add(applist3.get(appIndex));
						broker3.schedule(broker3Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 3 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist3.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
							//	System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 2060) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+15;
					}
					CloudSim.pauseSimulation(2070); // time to run user4 thread
				}
			};

			// dynamically add workload user 4

			Runnable user4 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 2060) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds4 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_BIGBENCH_IO.json");
					List<AppCloudlet> applist4 = ds4.createWorkload(broker4Id);
					Map<Integer, List<NetworkVm>> vmlistReq4 = ds4.createVMs(broker4Id, applist4);
					long pauseTime=2090;
					//for (int appId : vmlistReq4.keySet()) {
					for(int appIndex=0; appIndex<applist4.size(); appIndex++ ){
						int appId = applist4.get(appIndex).appID;
						List<NetworkVm> vmlist4 = vmlistReq4.get(appId);
						broker4.CreateCustomVMs(datacenter001.getId(), vmlist4);
						broker4.getAppCloudletList().add(applist4.get(appIndex));
						broker4.schedule(broker4Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 4 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist4.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
							//	System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 2350) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+20;


					}

					CloudSim.pauseSimulation(2360); // time to run user5 thread
				}
			};
			// dynamically add workload user 5

			Runnable user5 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 2350) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds5 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_hbase.json");
					List<AppCloudlet> applist5 = ds5.createWorkload(broker5Id);
					Map<Integer, List<NetworkVm>> vmlistReq5 = ds5.createVMs(broker5Id, applist5);
					long pauseTime=2375;
					//for (int appId : vmlistReq5.keySet()) {
					for(int appIndex=0; appIndex<applist5.size(); appIndex++ ){
						int appId = applist5.get(appIndex).appID;
						List<NetworkVm> vmlist5 = vmlistReq5.get(appId);
						broker5.CreateCustomVMs(datacenter001.getId(), vmlist5);
						broker5.getAppCloudletList().add(applist5.get(appIndex));
						broker5.schedule(broker5Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 5 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist5.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
							//	System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 2500) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+15;

					}
					CloudSim.pauseSimulation(2510); // time to run user6 thread
				}
			};
			// dynamically add workload user 6

			Runnable user6 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 2500) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds6 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet2.json");
					List<AppCloudlet> applist6 = ds6.createWorkload(broker6Id); 
					Map<Integer, List<NetworkVm>> vmlistReq6 = ds6.createVMs(broker6Id, applist6);
					long pauseTime=2530;
					//for (int appId : vmlistReq6.keySet()) {
					for(int appIndex=0; appIndex<applist6.size(); appIndex++ ){
						int appId = applist6.get(appIndex).appID;
						List<NetworkVm> vmlist6 = vmlistReq6.get(appId);
						broker6.CreateCustomVMs(datacenter001.getId(), vmlist6);
						broker6.getAppCloudletList().add(applist6.get(appIndex));
						broker6.schedule(broker6Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 6 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist6.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
								//System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 2700) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+20; 
					}
					CloudSim.pauseSimulation(2720); // time to run user7 thread
				}
			};
			
			Runnable user7 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 2710) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds7 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet3.json");
					List<AppCloudlet> applist7 = ds7.createWorkload(broker7Id); 
					
					Map<Integer, List<NetworkVm>> vmlistReq7 = ds7.createVMs(broker7Id, applist7);
					
					long pauseTime=2735;
					//Series 1 workload
					for(int appIndex=0; appIndex<applist7.size(); appIndex++ ){
						int appId = applist7.get(appIndex).appID;
						List<NetworkVm> vmlist7 = vmlistReq7.get(appId);
						broker7.CreateCustomVMs(datacenter001.getId(), vmlist7);
						broker7.getAppCloudletList().add(applist7.get(appIndex));
						broker7.schedule(broker7Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 7 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist7.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
								//System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 3040) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+15; 
					}
					CloudSim.pauseSimulation(3045); // time to run user8 thread
				}
			};
			
			Runnable user8 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 3040) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds8 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_hive_IO.json");
					List<AppCloudlet> applist8 = ds8.createWorkload(broker8Id); 
					
					Map<Integer, List<NetworkVm>> vmlistReq8 = ds8.createVMs(broker8Id, applist8);
					
					long pauseTime=3060;
					//Series 1 workload
					for(int appIndex=0; appIndex<applist8.size(); appIndex++ ){
						int appId = applist8.get(appIndex).appID;
						List<NetworkVm> vmlist8 = vmlistReq8.get(appId);
						broker8.CreateCustomVMs(datacenter001.getId(), vmlist8);
						broker8.getAppCloudletList().add(applist8.get(appIndex));
						broker8.schedule(broker8Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 8 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist8.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
								//System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 4000) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+15; 
					}
					CloudSim.pauseSimulation(4050); // time to run user9 thread
				}
			};
			

			Runnable user9 = new Runnable() {
				@Override
				public void run() {

					while (true) {
						if (CloudSim.isPaused() && CloudSim.clock() > 4010) {
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
						e.printStackTrace();
					}

					Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
					JsonWorkloadGenerator ds9 = new JsonWorkloadGenerator(workingDirectory
							+ "/jsonDS/dataSet_BIGBENCH_IO.json");
					List<AppCloudlet> applist9 = ds9.createWorkload(broker9Id); 
					
					Map<Integer, List<NetworkVm>> vmlistReq9 = ds9.createVMs(broker9Id, applist9);
					
					long pauseTime=4070;
					//Series 1 workload
					for(int appIndex=0; appIndex<applist9.size(); appIndex++ ){
						int appId = applist9.get(appIndex).appID;
						List<NetworkVm> vmlist9 = vmlistReq9.get(appId);
						broker9.CreateCustomVMs(datacenter001.getId(), vmlist9);
						broker9.getAppCloudletList().add(applist9.get(appIndex));
						broker9.schedule(broker9Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
						Log.printLine(CloudSim.clock() + "<<<<<--- user 9 workload sent --->>>>>");
						CloudSim.resumeSimulation();
						if(appIndex < (applist9.size()-1)){
							CloudSim.pauseSimulation(pauseTime);
							while (true) {
								//System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
									if (CloudSim.isPaused() && CloudSim.clock() < 4400) {
									break;
								}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							}
						}
						pauseTime=pauseTime+20; 
					}
					CloudSim.pauseSimulation(4450); // time to run user10 thread
				}
			};
			
			// dynamically add workload user 10

						Runnable user10 = new Runnable() {
							@Override
							public void run() {

								while (true) {
									if (CloudSim.isPaused() && CloudSim.clock() > 4400) {
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
									e.printStackTrace();
								}

								Log.printLine(CloudSim.clock() + "<<<<<--- New Workload Added --->>>>>");
								JsonWorkloadGenerator ds10 = new JsonWorkloadGenerator(workingDirectory
										+ "/jsonDS/dataSet_SWIM_IO.json");
								List<AppCloudlet> applist10 = ds10.createWorkload(broker10Id);
								
								Map<Integer, List<NetworkVm>> vmlistReq10 = ds10.createVMs(broker10Id, applist10);
								//for (int appId : vmlistReq3.keySet()) {
								long pauseTime=4500;
								for(int appIndex=0; appIndex<applist10.size(); appIndex++ ){
									int appId = applist10.get(appIndex).appID;
									List<NetworkVm> vmlist10 = vmlistReq10.get(appId);
									broker10.CreateCustomVMs(datacenter001.getId(), vmlist10);
									broker10.getAppCloudletList().add(applist10.get(appIndex));
									broker10.schedule(broker10Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
									Log.printLine(CloudSim.clock() + "<<<<<--- user 10 workload sent --->>>>>");
									CloudSim.resumeSimulation();
									if(appIndex < (applist10.size()-1)){
										CloudSim.pauseSimulation(pauseTime);
										while (true) {
										//	System.out.println("next paus time is "+pauseTime+" cur time:"+CloudSim.clock());
												if (CloudSim.isPaused() && CloudSim.clock() < 10000) {
												break;
											}
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
										}
									}
									pauseTime=pauseTime+15;
								}
								
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
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			new Thread(user7).start(); 
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			new Thread(user8).start(); 
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			new Thread(user9).start(); 
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			new Thread(user10).start(); /**/
			
			// Starts the simulation
			// DCMngUtility.resultFile.println("Simulation started at
			// "+calendar.getTime().toString());
			// DCMngUtility.resultFile.println("CloudSim clock is
			// "+CloudSim.clock());

			CloudSim.startSimulation();

			List<NetworkCloudlet> newList = broker1.getCloudletSubmittedList();

			CloudSim.stopSimulation();
			
			DCMngUtility.resultFile.close();
			//DCMngUtility.performanceFile.close();
			
			newList.addAll(broker2.getCloudletSubmittedList());
			newList.addAll(broker3.getCloudletSubmittedList());
			newList.addAll(broker4.getCloudletSubmittedList());
			newList.addAll(broker5.getCloudletSubmittedList());
			newList.addAll(broker6.getCloudletSubmittedList());
			newList.addAll(broker7.getCloudletSubmittedList());
			newList.addAll(broker8.getCloudletSubmittedList());
			newList.addAll(broker9.getCloudletSubmittedList());
			newList.addAll(broker10.getCloudletSubmittedList());

			// Print results when simulation is over
			//printCloudletList(newList);
			
			System.out.println("numberofcloudlet " + newList.size() + " Cached " + NetDatacenterBroker.cachedcloudlet
					+ " Data transfered " + NetworkConstants.totaldatatransfer + " during Time (ms)"
					+ NetworkConstants.totaldatatransferTime+" input read time:"+
					NetworkConstants.totalInputReadTime);
			System.out.println("Inter Racks Data transfered amount is " + NetworkConstants.interRackDataTransfer);
			// System.out.println(" ** HOSTS INFORMATION ** ");
			int totUsdHst = 0;
			int allHst = 0;
			double usagemean = 0.0;
			for (Host hs : datacenter001.getHostList()) {
				NetworkHost nvh = (NetworkHost) hs;
				if (nvh.getUtilizedCpu() != 0) {
					// System.out.println("Host Id"+nvh.getId()+" -> used cpu
					// prc:" +(1-nvh.getUnusedCpu())+" , mean usage:
					// "+nvh.getUtilizedCpu());
					totUsdHst++;
					usagemean = usagemean + (1 - nvh.getUnusedCpu());
				}
				allHst++;
			}
			usagemean = usagemean / totUsdHst;
			System.out.println("Total " + totUsdHst + " hosts from " + allHst
					+ " availbles hosts is used with mean prodactivity " + usagemean);
			//DCMngUtility.dcPerformance.switchReport();
			//DCMngUtility.dcPerformance.hostReport();
			
			//storageMgr.reportHardDriveStatus();

			Log.printLine("Project simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen!!");
		}
	}
	
	

	private static void printCloudletList(List<NetworkCloudlet> list) throws IOException {
		//String jobFileName = "Cloudlet_info__"+calendar.getTime().toString();
		//PrintWriter jobFile =  new PrintWriter(jobFileName);
		int size = list.size();
		double mean_time = 0.0;
		NetworkCloudlet cloudlet;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== Simulation results ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Input Size(MB)"+ indent +"Output Size(MB)");

		//jobFile.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
		//		+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			//jobFile.print(indent + cloudlet.getCloudletId() + indent + indent);
			mean_time = mean_time+ cloudlet.getActualCPUTime();
			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				//jobFile.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime())+ indent+ indent+ cloudlet.getCloudletFileSize()+ indent+ indent+cloudlet.getCloudletOutputSize());
				/*jobFile.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));*/
			}
			else{
				Log.print("FAILED");
			//	jobFile.print("FAILED");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime())+ indent+indent+cloudlet.getCloudletFileSize()+ indent+ indent+cloudlet.getCloudletOutputSize());
			/*	jobFile.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));*/
			}
		}
		mean_time = mean_time / size;
		System.out.println("mean cpu execute time : "+mean_time);
		//jobFile.close();

	}

}
