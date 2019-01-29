package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.exDatacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
import org.cloudbus.cloudsim.network.exDatacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.RootSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.Switch;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class ManageSpineLeafDatacenter {
	private NetworkDatacenter datacenter;
	private String VmAllcPolicy;
	private VmAllocationPolicy VMPolicy;
	private String name;
	
	// Network structure variables
	// !!! all the Bandwidth are in MB, because of overflow!!!
	private int LeafSwitchPorts = 10;
	private int SpineSwitchPorts = 20;
	private int NumOfSpineSwitch = 4;
	private long BandWidthLeafHost = 10 * 1024; // 1gb , BW number is in MB
	private long BandWidthLeafSpine = 40 * 1024 ;// 10gb
	private long BandWidthAggRoot = 40 * 1024 ;// 10gb
	private double SwitchingDelayRoot = .00285; // ms
	private double SwitchingDelayAgg  = .00245; // ms
	private double SwitchingDelayEdge = .00157; // ms
	
	
	public ManageSpineLeafDatacenter(String dcName,String policy){
		datacenter = null;
		VMPolicy = null;
		VmAllcPolicy = policy;
		name = dcName;
	}
	public NetworkDatacenter createDatacenter() {
		List<NetworkHost> hostList = new ArrayList<NetworkHost>();  
		int mips = 500 ; 
		int ram = 8192; // host memory (MB) = 8GB
		long storage = 1000000; // host storage
		long bw = BandWidthLeafHost;
		int high_cpu_hosts = (int) (Math.round(LeafSwitchPorts * 0.6) * SpineSwitchPorts); // 32 CPU, 8GB Ram
		int hig_ram_hosts =  (int) (Math.round(LeafSwitchPorts * 0.3) * SpineSwitchPorts); // 16 CPU, 16GB Ram
		int mid_cpu_ram_hosts =  (int) ((LeafSwitchPorts - (Math.round(LeafSwitchPorts * 0.6) + Math.round(LeafSwitchPorts * 0.3))) * SpineSwitchPorts); // 8 CPU, 8GB Ram
		long total_hosts = high_cpu_hosts + hig_ram_hosts + mid_cpu_ram_hosts;
		System.out.println("config:"+total_hosts+"="+high_cpu_hosts +","+ hig_ram_hosts+"," + mid_cpu_ram_hosts);
		for (int i = 0; i <high_cpu_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(2, new PeProvisionerSimple(mips))); 
			peList.add(new Pe(3, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(4, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(5, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(6, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(7, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(8, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(9, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(10, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(11, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(12, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(13, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(14, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(15, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(16, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(17, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(18, new PeProvisionerSimple(mips))); 
			peList.add(new Pe(19, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(20, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(21, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(22, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(23, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(24, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(25, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(26, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(27, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(28, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(29, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(30, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(31, new PeProvisionerSimple(mips)));  
			
			hostList.add( new NetworkHost(
					NetworkConstants.currentHostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); 
			NetworkConstants.currentHostId++;
		}
		ram = 16384; // host memory (MB) = 16GB
		for (int i = 0; i <hig_ram_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(2, new PeProvisionerSimple(mips))); 
			peList.add(new Pe(3, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(4, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(5, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(6, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(7, new PeProvisionerSimple(mips)));    
			peList.add(new Pe(8, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(9, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(10, new PeProvisionerSimple(mips))); 
			peList.add(new Pe(11, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(12, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(13, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(14, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(15, new PeProvisionerSimple(mips)));    
			
			hostList.add( new NetworkHost(
					NetworkConstants.currentHostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); // This is our machine
			NetworkConstants.currentHostId++;
		}
		ram = 8192; // host memory (MB) = 8GB
		for (int i = 0; i <mid_cpu_ram_hosts; i++) { 
			List<Pe> peList = new ArrayList<Pe>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(2, new PeProvisionerSimple(mips))); 
			peList.add(new Pe(3, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(4, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(5, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(6, new PeProvisionerSimple(mips)));  
			peList.add(new Pe(7, new PeProvisionerSimple(mips)));   
			
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
				VMPolicy = new VmAllocationPolicyTDB2(hostList);
				//DCMngUtility.resultFile.println("VM palcement policy is TDB!");
			}else if(VmAllcPolicy.compareTo("GREEDY") == 0){
				VMPolicy = new VmAllocationPolicyGreedy2(hostList);
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
			
			datacenter.VmAllcPlcyTyp = DCMngUtility.VM_ALLC_PLCY_CLUSTER;
			
		

		} catch (Exception e) {
			e.printStackTrace();
		}
		// create intra-data center topology
		// create a root switche ...... should be removed later
		int switchId=0;
		RootSwitch rootsw = new RootSwitch("Root0",NetworkConstants.ROOT_LEVEL,datacenter);
		datacenter.Switchlist.put(rootsw.getId(), rootsw); 
		rootsw.downlinkbandwidth = BandWidthAggRoot;
		rootsw.switching_delay = SwitchingDelayRoot;
		rootsw.numport = NumOfSpineSwitch; 
		
		// create spine switches  
		 int numOfAggSwitch = NumOfSpineSwitch;
		 AggregateSwitch aggswitch[] = new AggregateSwitch[numOfAggSwitch];
		 for (int as = 0; as < numOfAggSwitch; as++) {
			    aggswitch[as] = new AggregateSwitch("spin" + as, NetworkConstants.Agg_LEVEL, datacenter);
			  //  aggswitch[as].id =switchId;
			//	switchId++;
			    aggswitch[as].switching_delay = SwitchingDelayAgg;
			    aggswitch[as].numport = SpineSwitchPorts;
			    datacenter.Switchlist.put(aggswitch[as].getId(), aggswitch[as]); 
				aggswitch[as].uplinkbandwidth = BandWidthAggRoot;
				aggswitch[as].uplinkswitches.add(rootsw); 
				aggswitch[as].downlinkbandwidth=BandWidthLeafSpine;
				rootsw.downlinkswitches.add(aggswitch[as]); 
				//rootsw.downlinkSwFullPrc.add(new Double(0.0));
		 }
		// create leaf switches and assign them to hosts
				int numOfEdgeSwitch =  SpineSwitchPorts; 
				int hostno=0;
				int highcpuhost = 0;
				int highramhost = high_cpu_hosts;
				int midramcpuhost = high_cpu_hosts+hig_ram_hosts;
				EdgeSwitch edgeswitch[] = new EdgeSwitch[numOfEdgeSwitch];
				for (int i = 0; i < numOfEdgeSwitch; i++) {
					edgeswitch[i] = new EdgeSwitch("leaf"+i, NetworkConstants.EDGE_LEVEL, datacenter);
					edgeswitch[i].switching_delay = SwitchingDelayEdge;
					edgeswitch[i].numport = LeafSwitchPorts;
					edgeswitch[i].uplinkbandwidth = BandWidthLeafSpine;
					edgeswitch[i].downlinkbandwidth = BandWidthLeafSpine;
					datacenter.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]);
				 	System.out.println("Switch edge created BW:"+edgeswitch[i].uplinkbandwidth+","+BandWidthLeafSpine);
					for(int agghostno=0; agghostno<LeafSwitchPorts;){
						if(agghostno < Math.round(LeafSwitchPorts * 0.6)){
							NetworkHost nhs = (NetworkHost) datacenter.getHostList().get(highcpuhost);
							if(nhs.sw == null){
								nhs.bandwidth = BandWidthLeafHost; 
								System.out.println("host ID :" + nhs.getId()+" index "+highcpuhost+"connect to switch :" + edgeswitch[i].getName());
								edgeswitch[i].hostlist.put(nhs.getId(), nhs);
								datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
								nhs.sw = edgeswitch[i];  
								agghostno++; 
							}
							highcpuhost++;
						}
						if(Math.round(LeafSwitchPorts * 0.6) <= agghostno && agghostno < Math.round(LeafSwitchPorts * 0.9)){
							NetworkHost nhs = (NetworkHost) datacenter.getHostList().get(highramhost);
							
							if(nhs.sw == null){
								nhs.bandwidth = BandWidthLeafHost; 
								System.out.println("host ID :" + nhs.getId()+" index "+highramhost+"connect to switch :" + edgeswitch[i].getName());
								edgeswitch[i].hostlist.put(nhs.getId(), nhs);
								datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
								nhs.sw = edgeswitch[i];  
								agghostno++; 
							}
							highramhost++;
						}
						if(Math.round(LeafSwitchPorts * 0.9) <= agghostno){
							NetworkHost nhs = (NetworkHost) datacenter.getHostList().get(midramcpuhost);
							
							if(nhs.sw == null){
								nhs.bandwidth = BandWidthLeafHost; 
								System.out.println("host ID :" + nhs.getId()+" index "+midramcpuhost+"connect to switch :" + edgeswitch[i].getName());
								edgeswitch[i].hostlist.put(nhs.getId(), nhs);
								datacenter.HostToSwitchid.put(nhs.getId(), edgeswitch[i].getId());
								nhs.sw = edgeswitch[i];  
								agghostno++; 
							}
							midramcpuhost++;
						}
				}
				  
		 }
		// connect all spine to all leaf
				 for (int as = 0; as < numOfAggSwitch; as++) {
					 int spineId = aggswitch[as].getId();
					 Switch curSpine = datacenter.Switchlist.get(spineId);
					 for (int i = 0; i < numOfEdgeSwitch; i++) {
						 int leafId = edgeswitch[i].getId();
						 Switch curLeaf= datacenter.Switchlist.get(leafId);
						 curSpine.downlinkswitches.add(curLeaf);
						 curLeaf.uplinkswitches.add(curSpine);
					 }
				 }
		////
		return datacenter;
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
}
