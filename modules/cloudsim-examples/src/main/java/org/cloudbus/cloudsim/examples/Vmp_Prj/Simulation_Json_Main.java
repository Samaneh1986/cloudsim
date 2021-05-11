package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import org.cloudbus.cloudsim.network.exDatacenter.Switch;

public class Simulation_Json_Main {
	// private static Map<Integer, List<NetworkVm>> vmlistReq;
	private static List<AppCloudlet> appList;
	//simulation config
	private static String cur_schdlr = "Random"; //BFT , GREEDY , TDB, BFGR, RR, Random
	private static String cur_tplgy = "_8ary_"; //_8ary_ , _SPLF_
	private static ManageDatacenter mDc ;
	//private static ManageSpineLeafDatacenter mDc;
	
	private static NetDatacenterBroker broker1;
	private static int broker1Id;
	private static NetDatacenterBroker broker2;
	private static int broker2Id;
	private static Calendar calendar;

	private static String workingDirectory = System.getProperty("user.dir")
			+ "/src/main/java/org/cloudbus/cloudsim/examples/Vmp_Prj";
	private static String[] datasets = { "/jsonDS/dataSet_hive_IO.json", "/jsonDS/dataSet_SWIM_IO.json",
			"/jsonDS/dataSet_BIGBENCH_IO.json", "/jsonDS/dataSet_hbase.json", "/jsonDS/dataSet1.json",
			"/jsonDS/dataSet2.json", "/jsonDS/dataSet3.json" };
	// the total submitted VMs will be more or equal to this value
	private static int total_VM = 4000; 
	private static int deployed_VM = 0;
	// private static List<Cloudlet> allClList;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting project simulation...");

		try {
			// Initialize the CloudSim library
			int num_user = 2;
			calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events
			CloudSim.init(num_user, calendar, trace_flag);
			DCMngUtility.dcPerformance = new DCPerformance();
			String rsltFileName = cur_schdlr + cur_tplgy + calendar.getTime().toString()+".txt";
			// DCMngUtility.performanceFile= new
			// PrintWriter("perform"+calendar.getTime().toString());
			DCMngUtility.resultFileHostNo = new PrintWriter("Host_"+rsltFileName);
			DCMngUtility.resultFileCpu = new PrintWriter("Cpu_"+rsltFileName);
			DCMngUtility.resultFileTraffic = new PrintWriter("Traffic_"+rsltFileName);
			// Create data center
			mDc = new ManageDatacenter("Datacenter_001",cur_schdlr);
			// mDc = new ManageSpineLeafDatacenter("Datacenter_001",cur_schdlr);

			NetworkDatacenter datacenter001 = mDc.createDatacenter();
			NetDatacenterBroker.setLinkDC(datacenter001);
			NetStorageManager.setLinkDC(datacenter001);
			//
			// String mapperPath = workingDirectory
			// +"/placement_information.csv";
			// NetStorageManager storageMgr =
			// mDc.createStorageMgr("CEPH001",datacenter001.Storagelist,mapperPath);
			// datacenter001.storageManagerId =storageMgr.getId();
			// Create Broker

			broker1 = mDc.createBroker("broker_001");
			broker1Id = broker1.getId();
			broker2 = mDc.createBroker("broker_002");
			broker2Id = broker2.getId();
			// create initial VMs

			JsonWorkloadGenerator ds = new JsonWorkloadGenerator(workingDirectory + "/jsonDS/dataSet1.json");
			appList = ds.createWorkload(broker1Id);
			// for(AppCloudlet app : appList){
			// storageMgr.assignInitStorageToApp(app, "pool001");
			// }

			Map<Integer, List<NetworkVm>> vmlistReq = ds.createVMs(broker1Id, appList);
			deployed_VM = vmlistReq.size();
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
						if (CloudSim.isPaused()) {
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
					int dataset_index = -1;
					long pauseTime = 80;
					while (deployed_VM < total_VM) {
						// List<AppCloudlet> applist2 = new
						// ArrayList<AppCloudlet>();
						// Map<Integer, List<NetworkVm>> vmlistReq2 = new
						// HashMap<Integer,List<NetworkVm>>();;
						dataset_index = Math.floorMod((dataset_index + 1), 7);
						JsonWorkloadGenerator ds2 = new JsonWorkloadGenerator(
								workingDirectory + datasets[dataset_index]);
						List<AppCloudlet> applist2 = ds2.createWorkload(broker2Id);
						Map<Integer, List<NetworkVm>> vmlistReq2 = ds2.createVMs(broker2Id, applist2);
						// for (int appId : vmlistReq2.keySet()) {
						for (int appIndex = 0; appIndex < applist2.size(); appIndex++) {
							int appId = applist2.get(appIndex).appID;
							List<NetworkVm> vmlist2 = vmlistReq2.get(appId);
							deployed_VM = deployed_VM + vmlist2.size();
							broker2.CreateCustomVMs(datacenter001.getId(), vmlist2);
							broker2.getAppCloudletList().add(applist2.get(appIndex));
							broker2.schedule(broker2Id, 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
							Log.printLine(CloudSim.clock() + "<<<<<--- user 2 workload sent --->>>>>");
							CloudSim.resumeSimulation();
							//Log.printLine(CloudSim.clock()+" RESUMED NOW!!");
							if ((appIndex < applist2.size()-1) || (deployed_VM < total_VM)) {
							/*Log.printLine("CONDITION: app index "+appIndex+
										" of "+ applist2.size()+" , and VMs "+ deployed_VM
										+" of "+total_VM);
								Log.printLine("order to pause at: "+pauseTime);*/
								CloudSim.pauseSimulation(pauseTime);
								while (true) {
									// System.out.println("next paus time is
									// "+pauseTime+" cur
									// time:"+CloudSim.clock());
									if (CloudSim.isPaused()) {
										//Log.printLine(CloudSim.clock()+" Sim PAUSED!!!");
										break;
									}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							pauseTime = pauseTime + 100;
						}
						pauseTime = pauseTime + 100;
						/*Log.printLine(CloudSim.clock() + " NEXT DATASET " + datasets[dataset_index] + ", VMs: "
								+ deployed_VM+" of total "+total_VM);
						if (deployed_VM < total_VM) {
							if (!CloudSim.isPaused()) {
								Log.printLine("order to pause at :"+pauseTime);
								CloudSim.pauseSimulation(pauseTime);
							}
							while (true) {
								// System.out.println("next paus time is
								// "+pauseTime+" cur time:"+CloudSim.clock());
								if (CloudSim.isPaused()) {
									Log.printLine(CloudSim.clock()+" SIM PAUSED AGAIN!");
									break;
								}
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						else{//end of sending VMs
							while (true) {
								if (CloudSim.isPaused()) {
									break;
								}
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							CloudSim.resumeSimulation();
							Log.printLine(CloudSim.clock()+" now FINAL resume!!! ");
						}*/
					}
					Log.printLine(CloudSim.clock()+"  : total workload : "+deployed_VM);
				}
			};

			// dynamically add workload user 3

			new Thread(user2).start();

			// Starts the simulation
			// DCMngUtility.resultFile.println("Simulation started at
			// "+calendar.getTime().toString());
			// DCMngUtility.resultFile.println("CloudSim clock is
			// "+CloudSim.clock());

			CloudSim.startSimulation();

			List<NetworkCloudlet> newList = broker1.getCloudletSubmittedList();

			CloudSim.stopSimulation();

			DCMngUtility.resultFileHostNo.close();
			DCMngUtility.resultFileCpu.close();
			DCMngUtility.resultFileTraffic.close();
			// DCMngUtility.performanceFile.close();

			newList.addAll(broker2.getCloudletSubmittedList());

			// Print results when simulation is over
			// printCloudletList(newList);
			
			for(int swId : datacenter001.Switchlist.keySet()) {
				Switch sw = datacenter001.Switchlist.get(swId);
				System.out.println("Switch "+ sw.getName() + ", traffic" + sw.maxLinkTraffic);
			}

			System.out.println("numberofcloudlet " + newList.size() + " Cached " + NetDatacenterBroker.cachedcloudlet
					+ " Data transfered " + NetworkConstants.totaldatatransfer + " during Time (ms)"
					+ NetworkConstants.totaldatatransferTime + " input read time:"
					+ NetworkConstants.totalInputReadTime);
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
			 DCMngUtility.dcPerformance.switchReport();
			// DCMngUtility.dcPerformance.hostReport();

			// storageMgr.reportHardDriveStatus();

			Log.printLine("Project simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen!!");
		}
	}

	private static void printCloudletList(List<NetworkCloudlet> list) throws IOException {
		// String jobFileName = "Cloudlet_info__"+calendar.getTime().toString();
		// PrintWriter jobFile = new PrintWriter(jobFileName);
		int size = list.size();
		double mean_time = 0.0;
		NetworkCloudlet cloudlet;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== Simulation results ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time" + indent + "Input Size(MB)" + indent
				+ "Output Size(MB)");

		// jobFile.println("Cloudlet ID" + indent + "STATUS" + indent + "Data
		// center ID" + indent + "VM ID"
		// + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			// jobFile.print(indent + cloudlet.getCloudletId() + indent +
			// indent);
			mean_time = mean_time + cloudlet.getActualCPUTime();
			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				// jobFile.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()) + indent + indent + cloudlet.getCloudletFileSize()
						+ indent + indent + cloudlet.getCloudletOutputSize());
				/*
				 * jobFile.println(indent + indent + cloudlet.getResourceId() +
				 * indent + indent + indent + cloudlet.getVmId() + indent +
				 * indent + dft.format(cloudlet.getActualCPUTime()) + indent +
				 * indent + dft.format(cloudlet.getExecStartTime()) + indent +
				 * indent + dft.format(cloudlet.getFinishTime()));
				 */
			} else {
				Log.print("FAILED");
				// jobFile.print("FAILED");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()) + indent + indent + cloudlet.getCloudletFileSize()
						+ indent + indent + cloudlet.getCloudletOutputSize());
				/*
				 * jobFile.println(indent + indent + cloudlet.getResourceId() +
				 * indent + indent + indent + cloudlet.getVmId() + indent +
				 * indent + dft.format(cloudlet.getActualCPUTime()) + indent +
				 * indent + dft.format(cloudlet.getExecStartTime()) + indent +
				 * indent + dft.format(cloudlet.getFinishTime()));
				 */
			}
		}
		mean_time = mean_time / size;
		System.out.println("mean cpu execute time : " + mean_time);
		// jobFile.close();

	}

}
