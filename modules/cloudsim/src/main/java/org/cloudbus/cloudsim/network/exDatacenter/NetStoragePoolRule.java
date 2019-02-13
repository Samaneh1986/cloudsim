package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.HarddriveStorage;

public abstract class NetStoragePoolRule { 
	// Integer is the Id of the Storage Host
	public abstract List<Map<NetHarddriveStorage,Integer>> selectStorage(int blockIndex);
}
