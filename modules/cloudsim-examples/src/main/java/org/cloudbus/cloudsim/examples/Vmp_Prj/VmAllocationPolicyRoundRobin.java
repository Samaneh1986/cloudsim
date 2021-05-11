package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkHost;

public class VmAllocationPolicyRoundRobin extends VmAllocationPolicy {

	private Map<String, List<String>> usedHosts; // Broker Id to Host Id mapping
	private Map<String, Host> vmTable; // VM Id to Host mapping
	
	private int lastHostIndex;

	public VmAllocationPolicyRoundRobin(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
		usedHosts = new HashMap<String, List<String>>();
		setVmTable(new HashMap<String, Host>());
		lastHostIndex = 0;
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		int requiredPes = vm.getNumberOfPes();
		List<Host> l_host = this.getHostList();
		int min_pes = Integer.MAX_VALUE;
		NetworkHost selectedHost = null;
		boolean result = false;
		boolean repeatedHost = false;
		for (Host host : l_host) {
			repeatedHost = false;
			if (usedHosts.get(String.valueOf(vm.getUserId())) != null
					&& usedHosts.get(String.valueOf(vm.getUserId())).contains(String.valueOf(host.getId()))) {
				repeatedHost = true;
			}
			if (repeatedHost == false && requiredPes <= host.getNumberOfFreePes()) {
				if (host.getNumberOfFreePes() < min_pes && host.isSuitableForVm(vm)) {
					min_pes = host.getNumberOfFreePes();
					selectedHost = (NetworkHost) host;
					
				}
			}
		}
		result = selectedHost.vmCreate(vm);
		if (usedHosts.get(String.valueOf(vm.getUserId())) != null) {
			usedHosts.get(String.valueOf(vm.getUserId())).add(String.valueOf(selectedHost.getId()));
		} else {
			usedHosts.put(String.valueOf(vm.getUserId()), new ArrayList<String>());
			usedHosts.get(String.valueOf(vm.getUserId())).add(String.valueOf(selectedHost.getId()));
		}

		getVmTable().put(vm.getUid(), selectedHost);
		return result;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		// TODO Auto-generated method stub
		if (host.vmCreate(vm)) { // if vm has been successfully created in the host
			usedHosts.get(String.valueOf(vm.getUserId())).add(String.valueOf(host.getId()));
			getVmTable().put(vm.getUid(), host);
			return true;
		}

		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		boolean result = false;
		List<Host> l_host = this.getHostList();
		usedHosts.clear();
		for (Vm vm : vmList) {
			int maxTry = l_host.size();
			int requiredPes = vm.getNumberOfPes();
			NetworkHost selectedHost = null;
			boolean repeatedHost = false;
			for (int i = 0; i<maxTry; i++) {
				Host host = l_host.get(lastHostIndex);
				lastHostIndex = ++lastHostIndex%l_host.size();
				repeatedHost = false;
				if (usedHosts.get(String.valueOf(vm.getUserId())) != null
						&& usedHosts.get(String.valueOf(vm.getUserId())).contains(String.valueOf(host.getId()))) {
					repeatedHost = true;
				}
				if (repeatedHost == false && requiredPes <= host.getNumberOfFreePes()) {
					if (host.isSuitableForVm(vm)) {
						selectedHost = (NetworkHost) host;
						break;
					}
				}
			}
			if (selectedHost != null) {
				result = selectedHost.vmCreate(vm);
				getVmTable().put(vm.getUid(), selectedHost);
				if (usedHosts.get(String.valueOf(vm.getUserId())) != null) {
					usedHosts.get(String.valueOf(vm.getUserId())).add(String.valueOf(selectedHost.getId()));
				} else {
					usedHosts.put(String.valueOf(vm.getUserId()), new ArrayList<String>());
					usedHosts.get(String.valueOf(vm.getUserId())).add(String.valueOf(selectedHost.getId()));
				}
			} else {
				result = false;
				break;
			}
		}
		Map<String, Object> finalCnd = new HashMap<String, Object>();
		List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>();
		if (result == true)
			finalCnd.put("OK", "SUCCESS");
		else {
			for (Vm vm : vmList) {
				if (vm.getHost() != null)
					vm.getHost().vmDestroy(vm);
			}
			finalCnd.put("ER", "FAILED");
		}
		finalResult.add(finalCnd);
		return finalResult;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		Host host = getVmTable().remove(vm.getUid());
		try {
			if (host != null) {
				host.vmDestroy(vm);
				usedHosts.remove(String.valueOf(vm.getUserId()), String.valueOf(host.getId()));
				}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Host getHost(Vm vm) {
		// TODO Auto-generated method stub
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub

		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

}
