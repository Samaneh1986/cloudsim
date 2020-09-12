package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.exDatacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
import org.cloudbus.cloudsim.network.exDatacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.exDatacenter.NetHarddriveStorage;
import org.cloudbus.cloudsim.network.exDatacenter.NetStorageHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetStorageManager;
import org.cloudbus.cloudsim.network.exDatacenter.NetStoragePool;
import org.cloudbus.cloudsim.network.exDatacenter.NetStoragePoolRuleSimple;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.RootSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.StorageSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.Switch;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class ManageDatacenter {
	private NetworkDatacenter datacenter;
	private String VmAllcPolicy;
	private VmAllocationPolicy VMPolicy;
	private String name;
	
	// Network structure variables
	// !!! all the Bandwidth are in MB, because of overflow!!!
	private int FatTreeK = 8;
	private int PodNumber = FatTreeK;
	private int EdgeSwitchPort = FatTreeK / 2; //5
	private int AggSwitchPort = FatTreeK / 2;  //5
	private int RootSwitchPort = FatTreeK;
	private int NumOfRootSwitch = (FatTreeK/2) * (FatTreeK/2);  //25
	private int NumOfTotalHost = (FatTreeK * FatTreeK * FatTreeK) / 4; //250
	//private int NumSrorageInRack = 5;
	private long BandWidthEdgeStorage = 40 * 1024; // 1gb , BW number is in MB
	private long BandWidthEdgeHost = 10 * 1024; // 1gb , BW number is in MB
	private long BandWidthEdgeAgg = 40 * 1024 ;// 10gb
	private long BandWidthAggRoot = 40 * 1024 ;// 10gb
	private double SwitchingDelayRoot = .00285; // ms
	private double SwitchingDelayAgg  = .00245; // ms
	private double SwitchingDelayEdge = .00157; // ms
	
	
	public ManageDatacenter(String dcName,String policy){
		datacenter = null;
		VMPolicy = null;
		VmAllcPolicy = policy;
		name = dcName;
	}
	public NetworkDatacenter createDatacenter() {
		List<NetworkHost> hostList = new ArrayList<NetworkHost>();  
		int mips = 5200 ; 
		int ram = 394240; // host memory (MB) = 128GB
		int logicalCores = 80;
		long storage = 1000000; // host storage
		long bw = BandWidthEdgeHost;
		//int high_cpu_hosts = (int) (Math.round(EdgeSwitchPort * 0.6) * AggSwitchPort * (RootSwitchPort/2)); // 32 CPU, 8GB Ram
		int large_hosts = (int) Math.round( NumOfTotalHost* 0.6);
		//int hig_ram_hosts =  (int) (Math.round(EdgeSwitchPort * 0.3) * AggSwitchPort * (RootSwitchPort/2)); // 16 CPU, 16GB Ram
		int median_hosts =  (int) Math.round( NumOfTotalHost* 0.2);
		//int mid_cpu_ram_hosts =  (int) ((EdgeSwitchPort - (Math.round(EdgeSwitchPort * 0.6) + Math.round(EdgeSwitchPort * 0.3))) * AggSwitchPort * (RootSwitchPort/2)); // 8 CPU, 8GB Ram
		int low_hosts =  NumOfTotalHost - (large_hosts + median_hosts);
		//long total_hosts = high_cpu_hosts + hig_ram_hosts + mid_cpu_ram_hosts;
		//System.out.println("config:"+total_hosts+"="+high_cpu_hosts +","+ hig_ram_hosts+"," + mid_cpu_ram_hosts);
		for (int i = 0; i <large_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			for(int coreId = 0; coreId<logicalCores; coreId++)
				peList.add(new Pe(coreId, new PeProvisionerSimple(mips)));  
			
			hostList.add( new NetworkHost(
					NetworkConstants.currentHostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); 
			NetworkConstants.currentHostId++;
		}
		ram = 528384; // host memory (MB) = 64GB
		logicalCores = 64;
		mips = 4600;
		for (int i = 0; i <median_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			for(int coreId = 0; coreId<logicalCores; coreId++)
				peList.add(new Pe(coreId, new PeProvisionerSimple(mips)));      
			
			hostList.add( new NetworkHost(
					NetworkConstants.currentHostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); // This is our machine
			NetworkConstants.currentHostId++;
		}
		ram = 262144; // host memory (MB) = 32GB
		logicalCores = 64;
		mips = 4600;
		for (int i = 0; i <low_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			for(int coreId = 0; coreId<logicalCores; coreId++)
				peList.add(new Pe(coreId, new PeProvisionerSimple(mips)));  
			
			hostList.add( new NetworkHost(
					NetworkConstants.currentHostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); // This is our machine
			NetworkConstants.currentHostId++;
		}

		
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();  

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);
		
		try {
			if(VmAllcPolicy.compareTo("TDB") == 0){
				VMPolicy = new VmAllocationPolicyNew(hostList);
				//DCMngUtility.resultFile.println("VM palcement policy is TDB!");
			}else if(VmAllcPolicy.compareTo("GREEDY") == 0){
				VMPolicy = new VmAllocationPolicyGreedy(hostList);
				//DCMngUtility.resultFile.println("VM palcement policy is Greedy!");
			}else if(VmAllcPolicy.compareTo("BFT") == 0){
				VMPolicy = new VmAllocationPolicyBestFit(hostList);
			}else {
				VMPolicy = new VmAllocationPolicyRandom(hostList);
				//DCMngUtility.resultFile.println("VM palcement policy is Random!");
			}
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					VMPolicy,
					storageList,
					0);
			datacenter.total_zone = PodNumber/2;
			//datacenter.total_zone = PodNumber;
			datacenter.VmAllcPlcyTyp = NetworkConstants.VM_ALLC_PLCY_CLUSTER;
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		// create intra-data center topology
		// create root switches
		RootSwitch[] rootswList = new RootSwitch[NumOfRootSwitch];
		for(int i = 0 ; i < NumOfRootSwitch ; i++){
			rootswList[i] = new RootSwitch("Root"+i,NetworkConstants.ROOT_LEVEL,datacenter);
			rootswList[i].downlinkbandwidth = BandWidthAggRoot;
			rootswList[i].switching_delay = SwitchingDelayRoot;
			rootswList[i].numport = RootSwitchPort; 
			datacenter.Switchlist.put(rootswList[i].getId(), rootswList[i]); 
		}
		 int zone = -1;
		for (int pod=0 ; pod<PodNumber ; pod++){
		 	zone = zone + 1;
		 	// create aggregation switches inside a pod  
		 	int numOfAggSwitch = FatTreeK / 2; 
		 	AggregateSwitch aggswitch[] = new AggregateSwitch[numOfAggSwitch];
		 	int lastRootSwitch = 0;
		 	for (int as = 0; as < numOfAggSwitch; as++) {
			    aggswitch[as] = new AggregateSwitch("Agg"+ pod+ as, NetworkConstants.Agg_LEVEL, datacenter);
			    aggswitch[as].switching_delay = SwitchingDelayAgg;
			    aggswitch[as].numport = AggSwitchPort;
			    datacenter.Switchlist.put(aggswitch[as].getId(), aggswitch[as]); 
				aggswitch[as].uplinkbandwidth = BandWidthAggRoot;
				// all 4 root switches are connected to all 8 agg. switches
				for(int i = lastRootSwitch ; i < (lastRootSwitch+(FatTreeK / 2)) ; i++){
					aggswitch[as].uplinkswitches.add(rootswList[i]); 
					rootswList[i].downlinkswitches.add(aggswitch[as]);
				}
		 
				// For first agg. switch, create edge switches and assign them to hosts
				// For the rest agg. switch, define the links
				if(as == 0){ // each 5 agg. switches are connected to the same 5 edge switches
					int numOfEdgeSwitch =  FatTreeK / 2; 
					int highcpuhost = 0;
					int highramhost = large_hosts;
					int midramcpuhost = large_hosts+median_hosts;
					EdgeSwitch edgeswitch[] = new EdgeSwitch[numOfEdgeSwitch];
					for (int i = 0; i < numOfEdgeSwitch; i++) {
						edgeswitch[i] = new EdgeSwitch("Edge"+pod+as+i, NetworkConstants.EDGE_LEVEL, datacenter);
						edgeswitch[i].zone_number = Math.floorDiv(zone, 2);
						//edgeswitch[i].zone_number = zone;
						edgeswitch[i].switching_delay = SwitchingDelayEdge;
						edgeswitch[i].numport = EdgeSwitchPort;
						datacenter.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]);
						edgeswitch[i].downlinkbandwidth = BandWidthEdgeHost;
						edgeswitch[i].uplinkswitches.add(aggswitch[as]);
						edgeswitch[i].uplinkbandwidth = BandWidthEdgeAgg;
					 	//System.out.println("Switch edge created BW:"+edgeswitch[i].uplinkbandwidth+","+BandWidthEdgeAgg);
						aggswitch[as].downlinkswitches.add(edgeswitch[i]);
						aggswitch[as].downlinkbandwidth = BandWidthEdgeAgg;  
						
						for(int agghostno=0; agghostno<EdgeSwitchPort;){
							if(agghostno < Math.floor(EdgeSwitchPort * 0.6)){
								NetworkHost nhs = null;
								if(highcpuhost <  datacenter.getHostList().size()){
									nhs = (NetworkHost) datacenter.getHostList().get(highcpuhost);
									highcpuhost ++;
								}
								else{
									if(highramhost <  datacenter.getHostList().size()){
										nhs = (NetworkHost) datacenter.getHostList().get(highramhost);
										highramhost++;
									}
									else{
										nhs = (NetworkHost) datacenter.getHostList().get(midramcpuhost);
										midramcpuhost++;
									}
								}
								if(nhs.sw == null){
									nhs.bandwidth = BandWidthEdgeHost; 
									System.out.println("host ID :" + nhs.getId()+" index "+highcpuhost+" connect to switch :" + edgeswitch[i].getName()+" ID "+edgeswitch[i].getId());
									edgeswitch[i].hostlist.put(nhs.getId(), nhs);
									datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
									nhs.sw = edgeswitch[i];  
									agghostno++; 
								}
								//highcpuhost++;
							}
							if(Math.floor(EdgeSwitchPort * 0.6) <= agghostno && agghostno < Math.floor(EdgeSwitchPort * 0.9)){
								NetworkHost nhs = null;//(NetworkHost) datacenter.getHostList().get(highramhost);
								if(highramhost <  datacenter.getHostList().size()){
									nhs = (NetworkHost) datacenter.getHostList().get(highramhost);
									highramhost ++;
								}
								else{
									if(highcpuhost <  datacenter.getHostList().size()){
										nhs = (NetworkHost) datacenter.getHostList().get(highcpuhost);
										highcpuhost++;
									}
									else{
										nhs = (NetworkHost) datacenter.getHostList().get(midramcpuhost);
										midramcpuhost++;
									}
								}
								if(nhs.sw == null){
									nhs.bandwidth = BandWidthEdgeHost; 
									System.out.println("host ID :" + nhs.getId()+" index "+highramhost+" connect to switch :" + edgeswitch[i].getName());
									edgeswitch[i].hostlist.put(nhs.getId(), nhs);
									datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
									nhs.sw = edgeswitch[i];  
									agghostno++; 
								}
								//highramhost++;
							}
							if(Math.floor(EdgeSwitchPort * 0.9) <= agghostno){
								NetworkHost nhs = null;//(NetworkHost) datacenter.getHostList().get(midramcpuhost);
								if(midramcpuhost <  datacenter.getHostList().size()){
									nhs = (NetworkHost) datacenter.getHostList().get(midramcpuhost);
									midramcpuhost ++;
								}
								else{
									if(highcpuhost <  datacenter.getHostList().size()){
										nhs = (NetworkHost) datacenter.getHostList().get(highcpuhost);
										highcpuhost++;
									}
									else{
										nhs = (NetworkHost) datacenter.getHostList().get(highramhost);
										highramhost++;
									}
								}
								if(nhs.sw == null){
									nhs.bandwidth = BandWidthEdgeHost; 
									System.out.println("host ID :" + nhs.getId()+" index "+midramcpuhost+"connect to switch :" + edgeswitch[i].getName());
									edgeswitch[i].hostlist.put(nhs.getId(), nhs);
									datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
									nhs.sw = edgeswitch[i];  
									agghostno++; 
								}
								//midramcpuhost++;
							}
					}
					} 
				}
				else{
					AggregateSwitch previousSW = aggswitch[as-1];
					aggswitch[as].downlinkbandwidth = BandWidthEdgeAgg;
					for(int i =0; i<previousSW.downlinkswitches.size(); i++){
						aggswitch[as].downlinkswitches.add(previousSW.downlinkswitches.get(i));
						previousSW.downlinkswitches.get(i).uplinkswitches.add(aggswitch[as]);
						
					}
				}
		 }

			//**************define storages*******
			 // each two agg switch have one storage rack
		/*	 List<NetStorageHost> storageHostList = new ArrayList<NetStorageHost>();
			for (int as = 0; as < numOfAggSwitch; as+=2) { 
				StorageSwitch storageSW = new StorageSwitch("Storage"+as+1, NetworkConstants.STRG_LEVEL, datacenter);
			    storageSW.switching_delay = SwitchingDelayEdge;
			    storageSW.numport = NumSrorageInRack;
				storageSW.downlinkbandwidth = BandWidthEdgeStorage;
				storageSW.uplinkbandwidth = BandWidthEdgeAgg;
				for(int s=0; s<NumSrorageInRack;s++){
					NetStorageHost storageHost = createStorageHost();
					storageHost.sw = storageSW;
					storageHost.setDatacenter(datacenter);
					storageSW.storagelist.put(storageHost.getId(), storageHost);
					datacenter.Storagelist.put(storageHost.getId(), storageHost);
					storageHostList.add(storageHost);
					System.out.println("storage ID :" + storageHost.getId()+"connect to switch :" + storageSW.getName());
					}
				storageSW.uplinkswitches.add(aggswitch[as]);
				aggswitch[as].downlinkswitches.add(storageSW);
				aggswitch[as].numport++;
				datacenter.Switchlist.put(storageSW.getId(), storageSW);
			}*/
		}
			//*************************************
		return datacenter;
	}
	public NetStorageManager createStorageMgr(String name,Map<Integer, NetStorageHost> storageHostList,String mapperPath){
		DCMngUtility.HasStorageArea = true;
		NetStorageManager storageManager = null;
		List<NetStoragePool> poolList = new ArrayList<NetStoragePool>();
		try {
			storageManager = new NetStorageManager(name);
			// define storage pool and rule
			NetStoragePoolRuleSimple rlueSimple = new NetStoragePoolRuleSimple(storageHostList, mapperPath);
			NetStoragePool pool01 = new NetStoragePool("pool001",3,rlueSimple);
			poolList.add(pool01);
			storageManager.setStoragePoolList(poolList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return storageManager;
	}
	public NetDatacenterBroker createBroker(String brokerName) {
		NetDatacenterBroker broker = null;
		try {
			broker = new NetDatacenterBroker(brokerName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	

	private  NetStorageHost createStorageHost(){
		List<Pe> peList = new ArrayList<Pe>();
		int mips = 500 ; 
		peList.add(new Pe(0, new PeProvisionerSimple(mips)));
		int ram = 8192; // host memory (MB) = 8GB
		long totstorage = 0; // host storage
		long bw = BandWidthEdgeStorage;
		List<NetHarddriveStorage> storageDriveList = new ArrayList<NetHarddriveStorage>();
		double hardCapasity = 500000; // hard drive capacity in MB = 500GB
		double avgSeekTime = 0.09;
		for(int i=0; i<20 ; i++){
			try {
				storageDriveList.add(new NetHarddriveStorage(NetworkConstants.currentHardIdndex,("OSD_"+NetworkConstants.currentHardIdndex),hardCapasity,avgSeekTime));
				NetworkConstants.currentHardIdndex++;
				totstorage = (long) (totstorage + hardCapasity);
			} catch (ParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		NetStorageHost storage = new NetStorageHost(
				NetworkConstants.currentStorageId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				totstorage,
				peList,
				storageDriveList,
				new VmSchedulerTimeShared(peList));
		NetworkConstants.currentStorageId++;
		
		return storage;
	}
}
