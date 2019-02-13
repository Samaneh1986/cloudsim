package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class NetStorageManager extends SimEntity {
	
	private List<NetStoragePool> storagePoolList;
	private int global_block_index;
	private static int max_block_index = 4096;
	public static NetworkDatacenter linkDC;

	public NetStorageManager(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
		global_block_index = 0;
		storagePoolList = new ArrayList<NetStoragePool>();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printConcatLine(getName(), " is starting...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getTag()) { 
			// to assign a space
			//case CloudSimTags.Storage_Space_Assignment:
			//	assignStorageToApp(ev);
			//	break; 
			case CloudSimTags.Storage_Input_read:
				processReadInput(ev);
				break;
			// to write output data
			case CloudSimTags.Storge_Output_Write:
				processWriteOutput(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	public boolean assignStorageToApp(AppCloudlet app, String poolName) {
		//create data block for cloudlets of app
		int blkId=0;
		for(NetworkCloudlet cl : app.clist){
			long tot_input=cl.getCloudletFileSize();
			while(tot_input > NetworkConstants.MAX_DATA_BLK_SIZE_MB){
				tot_input = tot_input - NetworkConstants.MAX_DATA_BLK_SIZE_MB;
				NetStorageBlock newBlock = new NetStorageBlock("blk"+blkId, NetworkConstants.MAX_DATA_BLK_SIZE_MB,"file_"+cl.getCloudletId());
				newBlock.setIndex(global_block_index);
				newBlock.cloudletId = cl.getCloudletId();
				cl.inputData.add(newBlock);
				global_block_index++;
				if (global_block_index > max_block_index)
					global_block_index = 0;
				blkId++;
				//if(cl.getCloudletId()==2)
	    		//	System.out.println("send packet for cl2 size : "+newBlock.getData()+" block "+newBlock.getBlockId());
	    		}
			NetStorageBlock lastBlock = new NetStorageBlock("blk"+blkId, tot_input,"file_"+cl.getCloudletId());
			lastBlock.setIndex(global_block_index);
			lastBlock.cloudletId = cl.getCloudletId(); 
			cl.inputData.add(lastBlock);
			global_block_index++;
			blkId++;
		} 
		double ioTime = 0.0;
		//finding requested pool
		NetStoragePool selectedPool = null;
		for(NetStoragePool pool :storagePoolList){
			if(pool.getName().equalsIgnoreCase(poolName)){
				selectedPool = pool;
				break;
			}
		}
		//storing block list 
		for(NetworkCloudlet cl : app.clist){
			ioTime = ioTime + selectedPool.storeBlockList(cl.inputData);
		}
		return true;
	}

	private void processOtherEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		
	}

	private void processWriteOutput(SimEvent ev) {
		// TODO Auto-generated method stub
		
	}

	private void processReadInput(SimEvent ev) {
		// TODO Auto-generated method stub
		List<NetStorageBlock> inputRequest = (List<NetStorageBlock>)ev.getData();
		//System.out.println("input reading event processing for cloudlet "+inputRequest.get(0).cloudletId);
		//System.out.println("BLOCK ID:Drive IDs:Host ID");
		Map<Integer, List<NetStorageBlock>> storageData = new HashMap<Integer, List<NetStorageBlock>>();
		for(NetStorageBlock blk : inputRequest){
			//if(blk.cloudletId==2)
    		//	System.out.println("process for cl2 size : "+blk.getData()+" block "+blk.getBlockId());
    		 List<NetStorageBlock> curList = storageData.get(blk.getStorageHostId().get(0));
			 if(curList == null){
				 curList = new ArrayList<NetStorageBlock>();
			 }
			 curList.add(blk);
			 storageData.put(blk.getStorageHostId().get(0), curList);
		}
		for(int storageId : storageData.keySet()){
			NetStorageHost hs = linkDC.Storagelist.get(storageId);
			hs.readData(storageData.get(storageId));
		}
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printConcatLine(getName(), " is shutting down...");
	}

	public List<NetStoragePool> getStoragePoolList() {
		return storagePoolList;
	}

	public void setStoragePoolList(List<NetStoragePool> storagePoolList) {
		this.storagePoolList = storagePoolList;
	}

    public static void setLinkDC(NetworkDatacenter alinkDC) {
		linkDC = alinkDC;
	}
	
}
