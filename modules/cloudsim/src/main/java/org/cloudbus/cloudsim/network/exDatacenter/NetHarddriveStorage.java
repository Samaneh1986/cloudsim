package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;

public class NetHarddriveStorage extends HarddriveStorage {
	
	private List<NetStorageBlock> blockList;
	private NetHarddriveSeekTime netGen;
	private int index;

	/**
	 * Creates a new hard drive storage with a given name and capacity.
	 * 
	 * @param name the name of the new hard drive storage
	 * @param capacity the capacity in MByte
	 * @param avgSeekTime the average hard drive seeking time in second
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public NetHarddriveStorage(int index, String name, double capacity, double avgSeekTime) throws ParameterException {
		super(name, capacity);
		// TODO Auto-generated constructor stub
		blockList = new ArrayList<NetStorageBlock>();
		netGen = new NetHarddriveSeekTime(avgSeekTime);
		super.setAvgSeekTime(avgSeekTime, netGen);
		setIndex(index);
	}
	public NetHarddriveStorage(String name, double capacity) throws ParameterException {
		super(name, capacity);
		// TODO Auto-generated constructor stub
		blockList = new ArrayList<NetStorageBlock>();
		netGen = new NetHarddriveSeekTime(0.009);
		super.setAvgSeekTime(0.009, netGen);
	}
	public NetHarddriveStorage(double capacity) throws ParameterException {
		super(capacity);
		// TODO Auto-generated constructor stub
		blockList = new ArrayList<NetStorageBlock>();
		netGen = new NetHarddriveSeekTime(0.009);
		super.setAvgSeekTime(0.009, netGen);
	}
	
	public int getNumStoredBlock() {
		return blockList.size();
	}
	public NetStorageBlock getBlock(String blockId) {
		NetStorageBlock obj = null;
		if (blockId == null || blockId.length() == 0) {
			Log.printConcatLine(blockId, ".getBlockId(): Warning - invalid " + "block Id.");
			return obj;
		}
		Iterator<NetStorageBlock> it = blockList.iterator();
		double size = 0; // size in Mega byte
		int index = 0;
		boolean found = false;
		NetStorageBlock tempBlock = null;
		// find the block in the disk
		while (it.hasNext()) {
			tempBlock = it.next();
			size += tempBlock.getData(); 
			if (tempBlock.getBlockId().equals(blockId)) {
				found = true;
				obj = tempBlock;
				break;
			}

			index++;
		}
		// if the file is found, then determine the time taken to get it
		if (found) {
			obj = blockList.get(index);
			double seekTime = getSeekTime(size);
			double transferTime = getTransferTime(obj.getData());

			// total time for this operation in second
			obj.setIOTime(seekTime + transferTime);
		}
		return obj;
	}
	public List<String> getBlockNameList() {
		return nameList;
	}
	public double addBlock(NetStorageBlock block) {
		double result = 0.0;
		if(block == null)
			return result;
		// check the capacity
		if (block.getData() + currentSize > capacity) {
			Log.printConcatLine(getName(), ".addBlock(): Warning - not enough space to store ", block.getBlockId());
			return result;
		}
		// check if the same block Id is already taken
		if (!contains(block.getBlockId())) {
			double seekTime = getSeekTime(block.getData());
			double transferTime = getTransferTime(block.getData());

			blockList.add(block);               // add the block into the HD
			nameList.add(block.getBlockId());     // add the name to the name list
			currentSize += block.getData();    // increment the current HD size
			result = seekTime + transferTime;  // add total time
		}
		block.setIOTime(result);
		return result;
	}
	public double addBlock(List<NetStorageBlock> blockList) {
		double result = 0.0;
		if (blockList == null || blockList.isEmpty()) {
			Log.printConcatLine(getName(), ".addFile(): Warning - list is empty.");
			return result;
		}
		Iterator<NetStorageBlock> it = blockList.iterator();
		NetStorageBlock block = null;
		while (it.hasNext()) {
			block = it.next();
			result += addBlock(block);    // add each block in the list
		}
		return result;
	}
	public double deleteBlock(String blockId) {
		double result = 0.0;
		NetStorageBlock obj = null;
		if (blockId == null || blockId.length() == 0) {
			return result;
		}
		Iterator<NetStorageBlock> it = blockList.iterator();
		while (it.hasNext()) {
			obj = it.next();
			String id = obj.getBlockId();
			if(id.equals(blockId)){
				double seekTime = getSeekTime(obj.getData());
				double transferTime = getTransferTime(obj.getData());
				blockList.remove(obj);
				nameList.remove(obj.getBlockId());
				currentSize -= obj.getData();
				result = seekTime + transferTime;
				obj.setIOTime(result);
			}
		}
		return result;
	}
	private double getSeekTime(double blockSize) {
		double result = 0;

		if (netGen != null) {
			result += netGen.sample();
		}

		if (blockSize > 0 && capacity != 0) {
			result += (blockSize / capacity);
		}

		return result;
	}
	private double getTransferTime(double blockSize) {
		double result = 0;
		if (blockSize > 0 && capacity != 0) {
			result = (blockSize * getMaxTransferRate()) / capacity;
		}

		return result;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
