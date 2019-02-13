package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.List;

public class NetStorageBlock {
	private String blockId;
	private int index;
	private String orginFileName;
	private List<Integer> storageHostId;
	private List<String> storageDriveName;
	private double data;
	private double IOTime;
	public int cloudletId;
	public int vmId;
	NetStorageBlock(String id, double data, String fileName){
		setBlockId(id);
		setData(data);
		setOrginFileName(fileName);
		storageHostId = new ArrayList <Integer>();
		storageDriveName = new ArrayList <String>();
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int blockIndex) {
		this.index = blockIndex;
	}
	public String getOrginFileName() {
		return orginFileName;
	}
	public void setOrginFileName(String orginFileName) {
		this.orginFileName = orginFileName;
	}
	public List<Integer> getStorageHostId() {
		return storageHostId;
	}
	public void setStorageHostId(List<Integer> storageHostId) {
		this.storageHostId = storageHostId;
	}
	public double getData() {
		return data;
	}
	public void setData(double data) {
		this.data = data;
	}
	public List<String> getStorageDriveName() {
		return storageDriveName;
	}
	public void setStorageDriveName(List<String> storageDriveName) {
		this.storageDriveName = storageDriveName;
	}
	public double getIOTime() {
		return IOTime;
	}
	public void setIOTime(double IOTime) {
		this.IOTime = IOTime;
	}
	public String getBlockId() {
		return blockId;
	}
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
}
