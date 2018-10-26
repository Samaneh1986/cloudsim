/**
 * 
 */
package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.function.Log;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;

/**
 * @author samaneh
 *
 */
public class VmAllocationPolicyRandom extends VmAllocationPolicy {
	private Map<String, List<String>> usedHosts; // Broker Id to Host Id mapping
	private Map<String, Host> vmTable; // VM Id to Host mapping
	private Map<String, Integer> usedPes; // VM ID and allocated PEs number  
	
	public VmAllocationPolicyRandom(List<? extends Host> list) {
		super(list);
		// init variables 
		usedHosts = new HashMap<String, List<String>>();
		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		//System.out.println("VM placement in random mode .... ");
		int requiredPes = vm.getNumberOfPes();
		boolean result = false; 
		boolean repeatedHost = false;
		if (!getVmTable().containsKey(vm.getUid())) {
			UniformDistr ufrnd = new UniformDistr(0, super.getHostList().size()); 
			int hostid = (int) ufrnd.sample();
			//for (Host host : getHostList()) {
			//for (int i =0; i < getHostList().size(); i++ ){
			while (!result ){
				hostid = (int) ufrnd.sample();
				repeatedHost = false;
				NetworkHost host = (NetworkHost) super.getHostList().get(hostid);
				
				if(usedHosts.get(String.valueOf(vm.getUserId()))!= null && usedHosts.get(String.valueOf(vm.getUserId())).contains(String.valueOf(host.getId()))){
					repeatedHost = true;
				}
					
				if(host.getNumberOfFreePes() >= requiredPes && repeatedHost == false){
					// assign VM to Host :
					result = host.vmCreate(vm);
				}
				if (result) { // if vm were successfully created in the host
					System.out.println("VM "+vm.getId()+" is placed on host " + host.getId() );
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes); 
					if(usedHosts.get(String.valueOf(vm.getUserId())) != null){
						usedHosts.get(String.valueOf(vm.getUserId())).add( String.valueOf(host.getId()));
					}
					else{
						usedHosts.put(String.valueOf(vm.getUserId()), new ArrayList<String>());
						usedHosts.get(String.valueOf(vm.getUserId())).add( String.valueOf(host.getId()));
					}
					result = true;
					break;
				} 
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm, org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		// TODO Auto-generated method stub
		if (host.vmCreate(vm)) { // if vm has been successfully created in the host
			getVmTable().put(vm.getUid(), host);
			int requiredPes = vm.getNumberOfPes();
			getUsedPes().put(vm.getUid(), requiredPes);  
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
		boolean result = false; 
		usedHosts.clear();
		UniformDistr ufrnd = new UniformDistr(0, super.getHostList().size()); 
		for(Vm vm : vmList){
			result = false;  
			int requiredPes = vm.getNumberOfPes();
			boolean repeatedHost = false;
			if (!getVmTable().containsKey(vm.getUid())) { 
				int hostid = (int) ufrnd.sample(); 
				while (!result ){
					hostid = (int) ufrnd.sample(); 
					repeatedHost = false;
					NetworkHost host = (NetworkHost) super.getHostList().get(hostid);
					
					if(usedHosts.get(String.valueOf(vm.getUserId()))!= null && usedHosts.get(String.valueOf(vm.getUserId())).contains(String.valueOf(host.getId()))){
						repeatedHost = true;
					}
						
					if(host.getNumberOfFreePes() >= requiredPes && repeatedHost == false){
						// assign VM to Host :
						result = host.vmCreate(vm);
					}
					if (result) { // if vm were successfully created in the host
						System.out.println("VM "+vm.getId()+" is placed on host " + host.getId() );
						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes); 
						if(usedHosts.get(String.valueOf(vm.getUserId())) != null){
							usedHosts.get(String.valueOf(vm.getUserId())).add( String.valueOf(host.getId()));
						}
						else{
							usedHosts.put(String.valueOf(vm.getUserId()), new ArrayList<String>());
							usedHosts.get(String.valueOf(vm.getUserId())).add( String.valueOf(host.getId()));
						}
					} 
				}
			}
		}
		Map<String, Object> finalCnd =  new HashMap<String, Object>();
		List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>();
		if(result == true)
			finalCnd.put("OK","SUCCESS");
		else
			finalCnd.put("ER","FAILED");
		finalResult.add(finalCnd);
		return finalResult;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		try{
		Host host = getVmTable().remove(vm.getUid()); 
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm); 
			System.out.println("VM "+vm.getId()+" is removed from Host "+host.getId());
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
	// ********* setter and getter of variables *********
	public Map<String, Host> getVmTable() {
		return vmTable;
	} 
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	} 
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	} 
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}  
	//******************************************************

}
