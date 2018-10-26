/**
 * 
 */
package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.network.exDatacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
import org.cloudbus.cloudsim.network.exDatacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;
import org.cloudbus.cloudsim.network.exDatacenter.Switch;

/**
 * @author samaneh
 *
 */
public class VmAllocationPolicyGreedy extends VmAllocationPolicy {
	private Map<String, Host> vmTable; // VM Id to Host mapping
	private List<NetworkHost> host_list;
	private int [] host_selected;
	private double [][] distance_matrix;
	
	public VmAllocationPolicyGreedy(List<? extends Host> list) {
		super(list);

		setVmTable(new HashMap<String, Host>());
		this.host_list =  new ArrayList<NetworkHost>();
		//this.host_list = (List<NetworkHost>) list;
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
		UniformDistr ufrnd = new UniformDistr(0, super.getHostList().size()); 
		boolean result = false;
		int hostid = -1;
		NetworkHost host = null;
		// randomly select the first host for VM0
		while (!result ){
			hostid = (int) ufrnd.sample(); 
			host = (NetworkHost) super.getHostList().get(hostid);
			result = host.vmCreate(vmList.get(0));
		}
		int selHostId = host.getId();
		getVmTable().put(vmList.get(0).getUid(), host);
		System.out.println("First placement: VM Id , Host Id: "+vmList.get(0).getId()+" , "+selHostId);
		EdgeSwitch esw = (EdgeSwitch)host.sw;
		AggregateSwitch asw = null;
		System.out.println("VMs requested : " + vmList.size()+ " available no in rack "+esw.getName()+" is "+esw.hostlist.size());
		int available_hosts=0;
		for(NetworkHost curhost : esw.hostlist.values()){
			if(curhost.isSuitableForVm(vmList.get(0)))
				available_hosts++;
		}
		if(vmList.size() > available_hosts){
			asw = (AggregateSwitch) esw.uplinkswitches.get(0);
			int available_agg_hosts=0;
			for(Switch csw : asw.downlinkswitches){
				for(NetworkHost curhost : csw.hostlist.values()){
					if(curhost.isSuitableForVm(vmList.get(0)))
						available_agg_hosts++;
				}
			}
			if(vmList.size() > available_agg_hosts)
				create_distance_matrix(asw.uplinkswitches.get(0));
			else
				create_distance_matrix(asw);
			}
		else{
			create_distance_matrix(esw);
		}
		for(int i = 0; i<host_list.size(); i++){
			if(host_list.get(i).getId() == selHostId)
				host_selected[i] = 1;
		}
		// select neaarest host to selected one for VM 1-N
		double minDistance = Double.MAX_VALUE;
		double tmp = 0;
		int slctHostIx = -1;
		for(int vi =1; vi < vmList.size();vi++){
			Vm vm = vmList.get(vi);
			for(int i =0; i<host_list.size(); i++){
				if(host_selected[i] == 0){
					if(host_list.get(i).isSuitableForVm(vm)){
						tmp = 0;
						for(int j =0; j<host_list.size(); j++){
							tmp = tmp + (distance_matrix[i][j] * host_selected[j]);
						}
						if(tmp < minDistance){
							minDistance = tmp;
							slctHostIx = i;
						}
					}
				}
			}
			if(slctHostIx == -1){
				result = false;
				for(Vm dvm : vmList){
					if(dvm.getHost() != null)
						dvm.getHost().vmDestroy(dvm);
				} 
				break;
			}
			else{
			host_selected[slctHostIx] = 1;
			host = host_list.get(slctHostIx);
			result = host.vmCreate(vm);
			getVmTable().put(vm.getUid(), host);
			System.out.println("host with min distance of :"+minDistance);
		    System.out.println("The placement : Vm Id , Host Id :"+vm.getId()+","+host.getId());
		    minDistance = Double.MAX_VALUE;
			slctHostIx = -1;
			}
		}
		List<Map<String, Object>> final_result = new ArrayList<Map<String, Object>>();
		Map<String, Object> finalCnd =  new HashMap<String, Object>();
		if(result == true)
			finalCnd.put("OK","SUCCESS");
		else
			finalCnd.put("ER","FAILED");
		final_result.add(finalCnd);
		
		return final_result;
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
		Host hs = getVmTable().get(vm.getUid());
		if(hs == null)
			System.out.println("No host for VM "+ vm.getId());
		return hs;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub
		Host hs = getVmTable().get(Vm.getUid(userId, vmId));
		if(hs == null)
			System.out.println("No host for VM "+ vmId);
		return hs;
	}

	public Map<String, Host> getVmTable() {
		return vmTable;
	} 
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	} 
private void create_distance_matrix(Switch rsw){ 
		
		host_list.clear();
		if (rsw.level == NetworkConstants.ROOT_LEVEL){
			for(Switch sw : rsw.downlinkswitches){
				for(Switch edgsw : sw.downlinkswitches){ 
					for(NetworkHost host : edgsw.hostlist.values()){ 
							host_list.add(host);
					}
			    }
			}
	    	}
	    	else if (rsw.level == NetworkConstants.Agg_LEVEL){
		for(Switch sw : rsw.downlinkswitches){
			for(NetworkHost host : sw.hostlist.values()){
					host_list.add(host);
			}
		}
    	}
    	else{
    		for(NetworkHost host : rsw.hostlist.values()){ 
					host_list.add(host);
			//		System.out.print("candidate hosts: "+host.getId()+", ");
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
					distance_matrix[i][j] = distance(host1,host2);
					distance_matrix[j][i] = distance_matrix[i][j];
				}
			j++;	
			}
			i++;
		}
		
	}
private double distance(NetworkHost host1, NetworkHost host2){
	double distance = 0.0;
	if(host1.sw.getId() == host2.sw.getId()) // hosts are connected to the same edge switch
	{
		distance = 2;
	}
	else{ // hosts are connected to the same aggregation switch
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
			distance = 4;
		else 
			distance = 6;
	}
    return distance;
}
}
