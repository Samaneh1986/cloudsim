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
public class VmAllocationPolicyBfGreedy extends VmAllocationPolicy {
	private Map<String, Host> vmTable; // VM Id to Host mapping
	private List<NetworkHost> host_list;
	private int [] host_selected;
	private double [][] distance_matrix;
	
	
	public VmAllocationPolicyBfGreedy(List<? extends Host> list) {
		super(list);

		setVmTable(new HashMap<String, Host>());
		
		this.host_list =  new ArrayList<NetworkHost>();
		
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
		create_distance_matrix(); // distance is the number of hops
		//System.out.println("TOTAL DC HOSTS:"+super.getHostList().size());
		boolean result = false;
		int hostid = -1;
		NetworkHost selectedHost = null;
		int max_pes = 0; 
		Vm vm0 = vmList.get(0);
		int requiredPes = vm0.getNumberOfPes();
		// select the best fit host for VM0
		List<Host> l_host = this.getHostList();
		for (Host host : l_host) {
			if (requiredPes <= host.getNumberOfFreePes()) {
				if (host.getNumberOfFreePes() > max_pes && host.isSuitableForVm(vm0)) {
					max_pes = host.getNumberOfFreePes();
					selectedHost = (NetworkHost) host;
				}
			}
		}
		int selHostId = selectedHost.getId();
		if (selectedHost != null) {
			result = selectedHost.vmCreate(vm0);
			getVmTable().put(vm0.getUid(), selectedHost);
		}else {
			result = false;
			return null;
		}
		//System.out.println("First placement: VM Id , Host Id: "+vmList.get(0).getId()+" , "+selHostId+" on switch "+host.sw.getId());
		
		for(int i = 0; i<host_list.size(); i++){
			if(host_list.get(i).getId() == selHostId) {
				host_selected[i] = 1;
				//System.out.println("First placement host index: " +i);
			}
			else
				host_selected[i] = 0;
		}
		// select neaarest host to selected one for VM 1-N
		double minDistance = Double.MAX_VALUE;
		double tmp = 0;
		int slctHostIx = -1;
		for(int vi =1; vi < vmList.size();vi++){
			Vm vm = vmList.get(vi);
			if(!selectedHost.isSuitableForVm(vm)) {
				//need to finde a new host
				max_pes = 0;
			  for(int i =0; i<host_list.size(); i++){
				if(host_selected[i] == 0){
					if(host_list.get(i).isSuitableForVm(vm)){
						tmp =  0;
						for(int j =0; j<host_list.size(); j++){
							tmp = tmp + (distance_matrix[i][j] * host_selected[j]);
						}
						if(tmp < minDistance){
							minDistance = tmp;
							slctHostIx = i;
							max_pes = host_list.get(i).getCurrentFreeCpuNo();
						}
						if(tmp == minDistance && max_pes < host_list.get(i).getCurrentFreeCpuNo()) {
							slctHostIx = i;
							max_pes = host_list.get(i).getCurrentFreeCpuNo();
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
			selectedHost = host_list.get(slctHostIx);
			}
			}
			result = selectedHost.vmCreate(vm);
			getVmTable().put(vm.getUid(), selectedHost);
			//System.out.println("host with min distance of :"+minDistance);
		    //System.out.println("The placement : Vm Id , Host Id :"+vm.getId()+","+host.getId()+" on switch "+host.sw.getId());
		    minDistance = Double.MAX_VALUE;
			slctHostIx = -1;
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
private void create_distance_matrix(){ 
		if(distance_matrix == null) {
		host_list.clear();
		for(Host host : super.getHostList())
				host_list.add((NetworkHost)host);
		
		// fill distance_matrix
		distance_matrix = new double [host_list.size()][host_list.size()];
		host_selected = new int [host_list.size()];  
		for(int i =0; i<host_list.size(); i++){
			NetworkHost host1 = host_list.get(i) ;
			
			for(int j =0; j<host_list.size(); j++){
				NetworkHost host2 = host_list.get(j) ;
				
				if(i==j){ distance_matrix[i][j] = 0.0;}
				if(i < j){
					distance_matrix[i][j] = distance(host1,host2);
					distance_matrix[j][i] = distance_matrix[i][j];
				}
			}
		}
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
