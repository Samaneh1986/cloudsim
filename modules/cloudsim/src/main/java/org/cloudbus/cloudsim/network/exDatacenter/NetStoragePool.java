package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;

public class NetStoragePool {
	private String name; 
	private int riplicas;
	public NetStoragePoolRule rule;
	
	public NetStoragePool(String name,int riplicas, NetStoragePoolRule storageRule){ 
		setName(name);
		setRiplicas(riplicas);
		this.rule = storageRule;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getRiplicas() {
		return riplicas;
	}
	public void setRiplicas(int riplicas) {
		this.riplicas = riplicas;
	}

	public double storeBlockList(ArrayList<NetStorageBlock> inputData ) {
		double ioTime = 0.0;
		List<Map<NetHarddriveStorage,Integer>> selectedDtorage = null;
		for(NetStorageBlock data : inputData){
			selectedDtorage = rule.selectStorage(data.getIndex());
			for(int i=0; i<this.riplicas; i++){
				NetHarddriveStorage targetStorage = selectedDtorage.get(i).keySet().iterator().next();
				int hostId = selectedDtorage.get(i).get(targetStorage);
				data.getStorageDriveName().add(targetStorage.getName());
				data.getStorageHostId().add(hostId);
				ioTime = ioTime + targetStorage.addBlock(data);
			}
		}
		return ioTime;
	}
}
