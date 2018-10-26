/**
 * 
 */
package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.exDatacenter.HostPacket;
import org.cloudbus.cloudsim.network.exDatacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;
import org.cloudbus.cloudsim.network.exDatacenter.Switch;

/**
 * @author samaneh
 *
 */
public class VmAllocationPolicyTDB extends VmAllocationPolicy {
	
	private NetworkDatacenter DC;  
	private List<NetworkHost> host_list;
	private int [] host_selected;
	private double [][] distance_matrix; 
	private Map<String, Host> vmTable; // VM Id to Host mapping
	
	
	public VmAllocationPolicyTDB(List<NetworkHost> list) {
		super(list);  
		this.host_list =  new ArrayList<NetworkHost>(); 
		
		setVmTable(new HashMap<String, Host>());
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
 	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		boolean result = true;
		int slctHostIx = -1;
		double minTrafficCost = Double.MAX_VALUE;
		double tmp = 0.0;
		for(int i =0; i<host_list.size(); i++){
			if(host_selected[i] == 0){
				if(host_list.get(i).isSuitableForVm(vm)){
					tmp = 0;
					for(int j =0; j<host_list.size(); j++){
						tmp = tmp + (distance_matrix[i][j] * host_selected[j]);
					}
					if(tmp < minTrafficCost){
						minTrafficCost = tmp;
						slctHostIx = i;
					}
				}
			}
		}
		if(slctHostIx == -1){
			result = false;
			return result;
		}
		host_selected[slctHostIx] = 1;
		host_list.get(slctHostIx).vmCreate(vm);
		getVmTable().put(vm.getUid(), host_list.get(slctHostIx));
	
	return result; 
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm, org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		// TODO Auto-generated method stub
		if (host.vmCreate(vm)){  // if vm has been successfully created in the host
			getVmTable().put(vm.getUid(), host);
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		if(DC == null)
			DC = (NetworkDatacenter) (this.getHostList().get(0)).getDatacenter(); 
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<NetworkVm> reqVMs = (List<NetworkVm>) vmList;
		
		System.out.println("request for next "+vmList.size()+" VMs from Id "+vmList.get(0).getId());
		Switch root_sw = phaseOne( reqVMs);
		create_distance_matrix(root_sw, reqVMs);
		boolean rslt = phaseTwo(reqVMs);
		
		/* In the case of SUCCESS */
		
		Map<String, Object> finalCnd =  new HashMap<String, Object>();
		if(rslt == true)
			finalCnd.put("OK","SUCCESS");
		else
			finalCnd.put("ER","FAILED");
		result.add(finalCnd);
		this.host_list.clear();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		try{
			Host host = getVmTable().remove(vm.getUid());   
			if (host != null) {
				host.vmDestroy(vm); 
			}
			}
			catch(NullPointerException e){
				e.printStackTrace();
			}
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public Host getHost(Vm vm) {
		// TODO Auto-generated method stub
		return getVmTable().get(vm.getUid());
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub
		
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/************* Other required functions for this placement policy **************/
	private Switch phaseOne ( List<NetworkVm> vmList){
	    int switch_id = DCMngUtility.findSuitableSwitchForVmList(DC , vmList);
		Switch sw = DC.Switchlist.get(switch_id);
		return sw;
	}
	private void create_distance_matrix(Switch rsw, List<NetworkVm> vmList){ 
		
		double min_mips = 0;
    	long min_storage = 0; 
    	int min_ram = 0;
    	int min_pesNumber = 0;  
 
    	/*for(NetworkVm vm : vmList ){
    		if ( min_mips > vm.getMips())
    			min_mips = vm.getMips();
    		if(min_storage > vm.getSize())
    			min_storage =  vm.getSize();
    		if(min_ram > vm.getRam())
    			min_ram =  vm.getRam();
    		if(min_pesNumber > vm.getNumberOfPes())
    			min_pesNumber =  vm.getNumberOfPes(); 
    	} */
    	if (rsw.level == NetworkConstants.ROOT_LEVEL){
		for(Switch sw : rsw.downlinkswitches){
			for(Switch edgsw : sw.downlinkswitches){
			//	if(!edgsw.EG_value.equals("INELGB")){
					for(NetworkHost host : edgsw.hostlist.values()){
					//	if(host.getAvailableMips()>= min_mips && 
					//	   host.getRamProvisioner().getAvailableRam()>= min_ram)
					//		if(host.isSuitableForVm(vmList.get(0)))
								host_list.add(host);
					}
			 //   }
			}
		}
    	}
    	else if (rsw.level == NetworkConstants.Agg_LEVEL){
    		for(Switch sw : rsw.downlinkswitches){
				//if(!sw.EG_value.equals("INELGB")){
	    			for(NetworkHost host : sw.hostlist.values()){
	    			//	if(host.getAvailableMips()>= min_mips && 
	    			//	   host.getRamProvisioner().getAvailableRam()>= min_ram)
	    			//		if(host.isSuitableForVm(vmList.get(0)))
	    						host_list.add(host);
	    			}
				//}
    		}
        }
    	else{
    		for(NetworkHost host : rsw.hostlist.values()){
			//	if(host.getAvailableMips()>= min_mips && 
			//	   host.getRamProvisioner().getAvailableRam()>= min_ram)
			//		if(host.isSuitableForVm(vmList.get(0)))
						host_list.add(host);
			}
    	}
		// fill distance_matrix
		distance_matrix = new double [host_list.size()][host_list.size()];
		host_selected = new int [host_list.size()];  
		for(int i =0; i<host_list.size(); i++){
			NetworkHost host1 = host_list.get(i) ;
			host_selected[i] = 0;
			
			for(int j =0; j<host_list.size(); j++){
				NetworkHost host2 = host_list.get(j) ;
				
				if(i==j){ distance_matrix[i][j] = 0.0;}
				if(i < j){
					distance_matrix[i][j] = weighted_distance(host1,host2);
					distance_matrix[j][i] = distance_matrix[i][j];
					}
			}
		}
		
	}
	private double weighted_distance(NetworkHost host1, NetworkHost host2){
		double distance = 0.0;
		//HostPacket testp = new HostPacket(0,0,1.0,0.0,0.0,0,0);
		//distance = DCMngUtility.computeDelay(host1, host2, testp);
		
		if(host1.sw.getId() == host2.sw.getId()) // hosts are connected to the same edge switch
		{
			double host1_link = host1.bytesTosendGlobalSize+1;
			double host2_link = host2.bytesTosendGlobalSize+1;
			if(host1.sw.bytesTohostSize!=null && host1.sw.bytesTohostSize.get(host1.getId())!=null)
				host1_link=host1_link+host1.sw.bytesTohostSize.get(host1.getId());
			if(host2.sw.bytesTohostSize!=null && host2.sw.bytesTohostSize.get(host2.getId())!=null)
				host2_link=host2_link+host2.sw.bytesTohostSize.get(host2.getId());
			
			distance = 6 * Math.pow((((host1_link/(host1.bandwidth))+
					(host2_link/(host2.bandwidth)))),6);
		//	distance = (2*(((host1_link/(host1.bandwidth))+
		//			(host2_link/(host2.bandwidth)))));
		}
		else{
			String edg1Name;
			String edg2Name;
			Switch aggSw = null;
			for(Switch ns1  : host1.sw.uplinkswitches){ 
				edg1Name = ns1.getName();
				for(Switch ns2  : host2.sw.uplinkswitches){ 
					edg2Name = ns2.getName();
					if(edg1Name.equalsIgnoreCase(edg2Name))
						aggSw = ns2;
				}
			}
			if(aggSw!=null)	
			{ // hosts are connected to the same aggregation switch
			double fraction =  host1.bandwidth / host1.sw.uplinkbandwidth;
			double packet_in_src_link = 1.0;
			double packet_in_dst_link = 1.0;
			double host1_link = host1.bytesTosendGlobalSize+1;
			double host2_link = host2.bytesTosendGlobalSize+1;
			if(host1.sw.bytesTohostSize!=null && host1.sw.bytesTohostSize.get(host1.getId())!=null)
				host1_link=host1_link+host1.sw.bytesTohostSize.get(host1.getId());
			if(host2.sw.bytesTohostSize!=null && host2.sw.bytesTohostSize.get(host2.getId())!=null)
				host2_link=host2_link+host2.sw.bytesTohostSize.get(host2.getId());
			
			if(aggSw.bytesToDownSwitchSize!=null && aggSw.bytesToDownSwitchSize.get(host1.sw.getId()) != null)
				packet_in_src_link=packet_in_src_link+ aggSw.bytesToDownSwitchSize.get(host1.sw.getId());
			if(aggSw.bytesToDownSwitchSize!=null && aggSw.bytesToDownSwitchSize.get(host2.sw.getId()) != null)
				packet_in_dst_link=packet_in_dst_link+ aggSw.bytesToDownSwitchSize.get(host2.sw.getId());
			if(host1.sw.bytesToUpSwitchSize!=null && host1.sw.bytesToUpSwitchSize.get(aggSw.getId())!=null)
				packet_in_src_link=packet_in_src_link+host1.sw.bytesToUpSwitchSize.get(aggSw.getId());
			if(host2.sw.bytesToUpSwitchSize!=null && host2.sw.bytesToUpSwitchSize.get(aggSw.getId())!=null)
				packet_in_dst_link=packet_in_dst_link+host2.sw.bytesToUpSwitchSize.get(aggSw.getId());
			
			distance = 12 * Math.pow((((host1_link/(host1.bandwidth))+
					((packet_in_src_link/(host1.sw.uplinkbandwidth))*fraction))+
					((packet_in_dst_link/((host2.sw.uplinkbandwidth))*fraction))+
					(host2_link/(host2.bandwidth))),3);
			/*distance = (4*(((host1_link/(host1.bandwidth))+
					((packet_in_src_link/(host1.sw.uplinkbandwidth))*fraction))+
					((packet_in_dst_link/((host2.sw.uplinkbandwidth))*fraction))+
					(host2_link/(host2.bandwidth))));*/
		}
			else{// hosts are connected throught the root
				Switch aggSw1 = host1.sw.uplinkswitches.get(0);
				Switch aggSw2 = host2.sw.uplinkswitches.get(0);
				Switch coreSw = host1.sw.uplinkswitches.get(0).uplinkswitches.get(0);
				double fraction =  host1.bandwidth / host1.sw.uplinkbandwidth;
				double fractionRoot =  host1.bandwidth / host1.sw.uplinkswitches.get(0).uplinkbandwidth;
				double packet_in_src_link = 1.0;
				double packet_in_dst_link = 1.0;
				double from_root_to_src_agg = 1.0;
				double from_root_to_dest_agg = 1.0;
				double host1_link = host1.bytesTosendGlobalSize+1;
				double host2_link = host2.bytesTosendGlobalSize+1;
				
				if(host1.sw.bytesTohostSize!=null && host1.sw.bytesTohostSize.get(host1.getId())!=null)
					host1_link=host1_link+host1.sw.bytesTohostSize.get(host1.getId());
				if(host2.sw.bytesTohostSize!=null && host2.sw.bytesTohostSize.get(host2.getId())!=null)
					host2_link=host2_link+host2.sw.bytesTohostSize.get(host2.getId());
				
				if(aggSw1.bytesToDownSwitchSize!=null && aggSw1.bytesToDownSwitchSize.get(host1.sw.getId()) != null)
					packet_in_src_link=packet_in_src_link+ aggSw1.bytesToDownSwitchSize.get(host1.sw.getId());
				if(aggSw2.bytesToDownSwitchSize!=null && aggSw2.bytesToDownSwitchSize.get(host2.sw.getId()) != null)
					packet_in_dst_link=packet_in_dst_link+ aggSw2.bytesToDownSwitchSize.get(host2.sw.getId());
				if(host1.sw.bytesToUpSwitchSize!=null && host1.sw.bytesToUpSwitchSize.get(aggSw1.getId())!=null)
					packet_in_src_link=packet_in_src_link+host1.sw.bytesToUpSwitchSize.get(aggSw1.getId());
				if(host2.sw.bytesToUpSwitchSize!=null && host2.sw.bytesToUpSwitchSize.get(aggSw2.getId())!=null)
					packet_in_dst_link=packet_in_dst_link+host2.sw.bytesToUpSwitchSize.get(aggSw2.getId());
				
				if(coreSw.bytesToDownSwitchSize!=null && coreSw.bytesToDownSwitchSize.get(host1.sw.uplinkswitches.get(0).getId())!=null)
					from_root_to_src_agg =from_root_to_src_agg+ coreSw.bytesToDownSwitchSize.get(host1.sw.uplinkswitches.get(0).getId());
				if(host1.sw.uplinkswitches.get(0).bytesToUpSwitchSize!=null && host1.sw.uplinkswitches.get(0).bytesToUpSwitchSize.get(coreSw.getId())!=null )
					from_root_to_src_agg = from_root_to_src_agg + host1.sw.uplinkswitches.get(0).bytesToUpSwitchSize.get(coreSw.getId());
				if(coreSw.bytesToDownSwitchSize!=null && coreSw.bytesToDownSwitchSize.get(host2.sw.uplinkswitches.get(0).getId())!=null)
					from_root_to_dest_agg = from_root_to_dest_agg+coreSw.bytesToDownSwitchSize.get(host2.sw.uplinkswitches.get(0).getId());
				if(host2.sw.uplinkswitches.get(0).bytesToUpSwitchSize!=null &&host2.sw.uplinkswitches.get(0).bytesToUpSwitchSize.get(coreSw.getId())!=null)	
					from_root_to_dest_agg = from_root_to_dest_agg + host2.sw.uplinkswitches.get(0).bytesToUpSwitchSize.get(coreSw.getId());
				
				
				distance = 18 * Math.pow((((host1_link/(host1_link+1))+
						((packet_in_src_link/(packet_in_src_link+1))*fraction))+
						((from_root_to_src_agg/(from_root_to_src_agg+1))*fractionRoot)+
						((from_root_to_dest_agg/(from_root_to_dest_agg+1))*fractionRoot)+
						((packet_in_dst_link/((packet_in_dst_link+1))*fraction))+
						(host2_link/(host2_link+1))),2);
				/*distance = 6 *((((host1_link/(host1_link+1))+
						((packet_in_src_link/(packet_in_src_link+1))*fraction))+
						((from_root_to_src_agg/(from_root_to_src_agg+1))*fractionRoot)+
						((from_root_to_dest_agg/(from_root_to_dest_agg+1))*fractionRoot)+
						((packet_in_dst_link/((packet_in_dst_link+1))*fraction))+
						(host2_link/(host2_link+1))));*/
			
			}
		}
		
	    return distance;
	}
	private boolean phaseTwo (List<NetworkVm> vmList){
		List<NetworkVm> remaine_vm = new ArrayList<NetworkVm>(vmList);
		boolean result = false;
		int slctHostIx = -1;
		double minTrafficCost = Double.MAX_VALUE;
		int minPe = Integer.MAX_VALUE;
		int minPeVm = Integer.MAX_VALUE;
		double tmp = 0.0;
		NetworkHost host = null;
		NetworkVm firstVm = null;
		int firstVmId = 0;
		for(int i =0; i< remaine_vm.size(); i++){
			if(remaine_vm.get(i).getNumberOfPes() < minPeVm){
				minPeVm = remaine_vm.get(i).getNumberOfPes() ;
				firstVm = remaine_vm.get(i);
				firstVmId = i;
			}
		}
		// find Host with minimum traffic 
		for(int i =0; i<host_list.size(); i++){
			tmp = 0;
			if(host_list.get(i).isSuitableForVm(firstVm) ){ 
				for(int j =0; j<host_list.size(); j++){
					tmp = tmp + distance_matrix[i][j];
				}
				if(tmp < minTrafficCost){
					minTrafficCost = tmp;
					slctHostIx = i;
				}
			}
		}
		// assign a suitable VM to host 
		if(slctHostIx<0){
			return false;
		}
		host = host_list.get(slctHostIx);
		result = host.vmCreate(firstVm);
		if(!result)
			System.out.println("The following placement failed");
		remaine_vm.remove(firstVmId);
		host_selected[slctHostIx] = 1;
		getVmTable().put(firstVm.getUid(), host);  
	    System.out.println("first placement : Vm Id , Host Id , UId :"+firstVm.getId()+","+host.getId()+","+firstVm.getUserId());
	    // find suitable hosts for other VMs
		minTrafficCost = Double.MAX_VALUE;
		minPe = Integer.MAX_VALUE;
		slctHostIx = -1;
		for(NetworkVm vm : remaine_vm){
			for(int i =0; i<host_list.size(); i++){ 

				if(host_selected[i] == 0){
			
					if(host_list.get(i).isSuitableForVm(vm)){
						tmp = 0;
						for(int j =0; j<host_list.size(); j++){
							tmp = tmp + (distance_matrix[i][j] * host_selected[j]);
						}
						if(tmp < minTrafficCost){
							minTrafficCost = tmp;
							slctHostIx = i;
						}
					}/*
					else{
						DCMngUtility.resultFile.println("host "+host_list.get(i).getId()+" not suitable for "+vm.getId()+"beacause :");
						if (host_list.get(i).getStorage() < vm.getSize()) {
							DCMngUtility.resultFile.println("[VmScheduler.vmCreate] Allocation of VM  failed by storage"); 
						}

						if (!host_list.get(i).getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
							DCMngUtility.resultFile.println("[VmScheduler.vmCreate] Allocation of VM  failed by RAM");
							DCMngUtility.resultFile.println("REQUESTED: "+vm.getRam()+"AVAILABLE: "+host_list.get(i).getRamProvisioner().getAvailableRam());
						}

						if (!host_list.get(i).getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
							DCMngUtility.resultFile.println("[VmScheduler.vmCreate] Allocation of VM  failed by BW"); 
						}

						if (!host_list.get(i).getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
							DCMngUtility.resultFile.println("[VmScheduler.vmCreate] Allocation of VM  failed by MIPS"); 
						}
					}*/
				}
			}
			if(slctHostIx == -1){
				result = false;
				for(NetworkVm dvm : vmList){
					if(dvm.getHost() != null){
						dvm.getHost().vmDestroy(dvm);}
				}
				return result;
			}
			host_selected[slctHostIx] = 1;
			host = host_list.get(slctHostIx);
			result = host.vmCreate(vm);
			if(!result)
				System.out.println("The following placement failed");
			getVmTable().put(vm.getUid(), host);
		    //System.out.println("host with min cost of :"+minTrafficCost);
		    System.out.println("The placement : Vm Id , Host Id,index :"+vm.getId()+","+host.getId()+","+slctHostIx);
		    minTrafficCost = Double.MAX_VALUE;
			slctHostIx = -1;
		}
		
		return result;
	}
	public Map<String, Host> getVmTable() {
		return vmTable;
	} 
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	} 
}
