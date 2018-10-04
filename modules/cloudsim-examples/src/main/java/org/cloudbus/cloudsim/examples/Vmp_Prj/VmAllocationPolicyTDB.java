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
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.network.exDatacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.exDatacenter.DCMngUtility;
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
	//private List<Switch> swList;
	private Map<String, Host> vmTable; // VM Id to Host mapping
	
	
	public VmAllocationPolicyTDB(List<? extends Host> list) {
		super(list);
		this.host_list =  new ArrayList<NetworkHost>();
		//swList = new ArrayList<Switch>();
		
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
			DC = (NetworkDatacenter) this.getHostList().get(0).getDatacenter();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<NetworkVm> reqVMs = (List<NetworkVm>) vmList;
		
		System.out.println("request for next "+vmList.size()+" VMs from Id "+vmList.get(0).getId());
		Switch root_sw = phaseOne(reqVMs);
		System.out.println("selected switch "+root_sw.getName());
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
	private Switch phaseOne (List<NetworkVm> vmList){
	    int switch_id = DCMngUtility.findSuitableSwitchForVmList(DC , vmList);
		Switch sw = DC.Switchlist.get(switch_id);
		return sw;
	}
	private void create_distance_matrix(Switch rsw, List<NetworkVm> vmList){ 
		
		double min_mips = 0;
    	long min_storage = 0; 
    	int min_ram = 0;
    	int min_pesNumber = 0;  
 
    	for(NetworkVm vm : vmList ){
    		if ( min_mips > vm.getMips())
    			min_mips = vm.getMips();
    		if(min_storage > vm.getSize())
    			min_storage =  vm.getSize();
    		if(min_ram > vm.getRam())
    			min_ram =  vm.getRam();
    		if(min_pesNumber > vm.getNumberOfPes())
    			min_pesNumber =  vm.getNumberOfPes(); 
    	} 
    	if (rsw.level < 2){
		for(Switch sw : rsw.downlinkswitches){
			for(NetworkHost host : sw.hostlist.values()){
				if(host.getAvailableMips()>= min_mips && 
				   host.getRamProvisioner().getAvailableRam()>= min_ram)
					host_list.add(host);
			}
		}
    	}
    	else{
    		for(NetworkHost host : rsw.hostlist.values()){
				if(host.getAvailableMips()>= min_mips && 
				   host.getRamProvisioner().getAvailableRam()>= min_ram)
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
			j++;	
			}
			i++;
		}
		
	}
	private double weighted_distance(NetworkHost host1, NetworkHost host2){
		double distance = 0.0;
		if(host1.sw.getId() == host2.sw.getId()) // hosts are connected to the same edge switch
		{
			//distance = 2 + (2 *( host1.usedbandwidthSend + host1.usedbandwidthRcv + host2.usedbandwidthSend+ host2.usedbandwidthRcv)) ;
			distance = 2*((host1.packetTosendGlobal.size()/(host1.packetTosendGlobal.size()+1))+
					(host2.packetTosendGlobal.size()/(host2.packetTosendGlobal.size()+1)));
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
			double fraction = host1.sw.uplinkbandwidth / host1.bandwidth;
			double packet_in_src_link = 0.0;
			double packet_in_dst_link = 0.0;
			if(aggSw.downlinkswitchpktlist.get(host1.sw.getId()) != null)
				packet_in_src_link= aggSw.downlinkswitchpktlist.get(host1.sw.getId()).size();
			if(aggSw.downlinkswitchpktlist.get(host2.sw.getId()) != null)
				packet_in_dst_link= aggSw.downlinkswitchpktlist.get(host2.sw.getId()).size();
			//distance = 4+(4 *( ( host1.usedbandwidthSend + host1.usedbandwidthRcv + host2.usedbandwidthSend+ host2.usedbandwidthRcv) +
			//		(host1.sw.uplinkSwSend + host1.sw.uplinkSwRcv + host2.sw.uplinkSwSend+ host2.sw.uplinkSwRcv )));
			distance = 4*((host1.packetTosendGlobal.size()/(host1.packetTosendGlobal.size()+1))+
					(packet_in_src_link/((packet_in_src_link+1)/fraction))+
					(packet_in_dst_link/((packet_in_dst_link+1)/fraction))+
					(host2.packetTosendGlobal.size()/(host2.packetTosendGlobal.size()+1)));
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
			if(host_list.get(i).isSuitableForVm(firstVm)){
				for(int j =0; j<host_list.size(); j++){
					tmp = tmp + distance_matrix[i][j];
				}
				if(tmp < minTrafficCost){
					minTrafficCost = tmp;
					slctHostIx = i;
				}
				/*if(tmp == minTrafficCost){
					if(host_list.get(i).getNumberOfFreePes() < minPe && host_list.get(i).getNumberOfFreePes() < host_list.get(i).getNumberOfPes()){
						System.out.println("equal tr but let pe :"+minTrafficCost+","+host_list.get(i).getNumberOfFreePes() );
						minPe = host_list.get(i).getNumberOfFreePes();
						slctHostIx = i;
					}
				}*/
			}
		}
		// assign a suitable VM to host 
		host = host_list.get(slctHostIx);
		result = host.vmCreate(firstVm);
		if(!result)
			System.out.println("The following placement failed");
		remaine_vm.remove(firstVmId);
		host_selected[slctHostIx] = 1;
		getVmTable().put(firstVm.getUid(), host);  
	    System.out.println("first placement : Vm Id , Host Id , UId :"+firstVm.getId()+","+host.getId()+","+firstVm.getUserId());
	    
		/*for(NetworkVm vm : remaine_vm){
			host = host_list.get(slctHostIx);
		    result = host.vmCreate(vm);
		if(result == true){
			remaine_vm.remove(vm);
			host_selected[slctHostIx] = 1;
			getVmTable().put(vm.getUid(), host);
		    //System.out.println("host with min cost of :"+minTrafficCost);
		    System.out.println("first placement : Vm Id , Host Id :"+vm.getId()+","+host.getId());
			break;
			}
		}*/
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
						if(vm.getId() == 282) 
							System.out.println("host is suit and traffic : "+tmp);
						if(tmp < minTrafficCost){
							minTrafficCost = tmp;
							slctHostIx = i;
						}
						/*if(tmp == minTrafficCost){
							if(host_list.get(i).getNumberOfFreePes() < minPe && host_list.get(i).getNumberOfFreePes() < host_list.get(i).getNumberOfPes()){
								System.out.println("equal tr but let pe :"+minTrafficCost+","+host_list.get(i).getNumberOfFreePes() );
								minPe = host_list.get(i).getNumberOfFreePes();
								slctHostIx = i;
							}
						}*/
					}
				}
			}
			if(slctHostIx == -1){
				result = false;
				for(NetworkVm dvm : vmList){
					if(dvm.getHost() != null){
						System.out.println("destroying VMs ...... because of VM "+vm.getId());
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
