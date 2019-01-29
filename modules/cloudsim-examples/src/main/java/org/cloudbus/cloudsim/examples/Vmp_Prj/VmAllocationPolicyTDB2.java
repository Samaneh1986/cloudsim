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
public class VmAllocationPolicyTDB2 extends VmAllocationPolicy {
	
	private NetworkDatacenter DC;  
	private List<NetworkHost> host_list;
	private int [] host_selected;
	private double [][] distance_matrix; 
	private Map<String, Host> vmTable; // VM Id to Host mapping
	
	
	public VmAllocationPolicyTDB2(List<NetworkHost> list) {
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
	   // Switch sw = DCMngUtility.findSuitableSwitchForVmList(DC , vmList);
		Switch sw = DCMngUtility.findLeafSwitchForVmList(DC , vmList);
		if(sw.getName().equals("dummy"))
			System.out.println("selected dummy switch with rack no :"+sw.downlinkswitches.size());
		//Switch sw = DC.Switchlist.get(switch_id);
		return sw;
	}
	private void create_distance_matrix(Switch rsw, List<NetworkVm> vmList){ 
		
    	if (rsw.getName().equals("dummy")){
    		for(Switch sw : rsw.downlinkswitches){ 
	    			for(NetworkHost host : sw.hostlist.values()){ 
	    				if(host.isSuitableForVm(vmList.get(0)))
	    						host_list.add(host);
	    			}
	    			sw.uplinkswitches.remove(rsw); 
    		}
    		rsw.downlinkswitches.clear(); 
    		
        }
    	else{
    		for(NetworkHost host : rsw.hostlist.values()){
    			if(host.isSuitableForVm(vmList.get(0)))
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
		
			double host1_link = host1.bytesTosendGlobalSize+1;
			double host2_link = host2.bytesTosendGlobalSize+1;
			if(host1.sw.bytesTohostSize!=null && host1.sw.bytesTohostSize.get(host1.getId())!=null)
				host1_link=host1_link+host1.sw.bytesTohostSize.get(host1.getId());
			if(host2.sw.bytesTohostSize!=null && host2.sw.bytesTohostSize.get(host2.getId())!=null)
				host2_link=host2_link+host2.sw.bytesTohostSize.get(host2.getId());
		if(host1.sw.getId() == host2.sw.getId()) // hosts are connected to the same edge switch
		{
				
			distance = (host1_link)+
					(host2_link);
		}
		else{
			distance = 4*((host1_link)+
					(host2_link));
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
			if(host_list.get(i).isSuitableForVm(firstVm) ){
				for(int j =0; j<host_list.size(); j++)
					System.out.print(this.distance_matrix[i][j]+"-");
				System.out.println();
			}
		}
		for(int i =0; i<host_list.size(); i++){
			tmp = 0;
			if(host_list.get(i).isSuitableForVm(firstVm) ){ 
				for(int j =0; j<host_list.size(); j++){
					tmp = tmp + distance_matrix[i][j];
				}
				//System.out.println("host , sw , cost :"+host_list.get(i).getId()+","+host_list.get(i).sw.getName()+","+tmp);
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
	    System.out.println("first placement : Vm Id , Host Id , SW :"+firstVm.getId()+","+host.getId()+","+host.sw.getName());
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
						System.out.println("host , sw , cost :"+host_list.get(i).getId()+","+host_list.get(i).sw.getName()+","+tmp);
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
		    System.out.println("The placement : Vm Id , Host Id,SW :"+vm.getId()+","+host.getId()+","+host.sw.getName());
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
