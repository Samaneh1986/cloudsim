/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * NetworkHost class extends {@link Host} to support simulation of networked datacenters. It executes
 * actions related to management of packets (sent and received) other than that of virtual machines
 * (e.g., creation and destruction). A host has a defined policy for provisioning memory and bw, as
 * well as an allocation policy for PE's to virtual machines.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetStorageHost extends Host {
	
	public List<NetHarddriveStorage> storageDriveList;
	
	public List<NetworkPacket> packetTosend;
	public List<NetStorageHost> hostOfsendGlobal;

        /**
         * List of received packets.
         */
	public List<NetworkPacket> packetTowrite;

        /**
         * @todo the attribute is not being used 
         * and is redundant with the ram capacity defined in {@link Host#ramProvisioner}
         */
	public double memory;

        /** 
         * Edge switch in which the Host is connected. 
         */
	public Switch sw; 

        /**
         * @todo What exactly is this bandwidth?
         * Because it is redundant with the bw capacity defined in {@link Host#bwProvisioner}
         */
	public double bandwidth;  
	
	private double utilizedCpu;
	private double unusedCpu;

	/** Time when last job will finish on CPU1. 
         * @todo it is not being used.
         **/
	public List<Double> CPUfinTimeCPU = new ArrayList<Double>();

	/** 
         * @todo it is not being used.
         **/
	public double fintime = 0;

	public NetStorageHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			List<NetHarddriveStorage> hardList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

		packetTowrite = new ArrayList<NetworkPacket>();
		packetTosend = new ArrayList<NetworkPacket>();
		hostOfsendGlobal = new ArrayList<NetStorageHost>();
		
		utilizedCpu = 0.0;
		unusedCpu = 0.0; 
		
		this.storageDriveList = hardList;
	}


	/**
	 * Receives packets and forward them to the corresponding VM.
	 */

    public double getTotalFreeStorage(){
    	double freeStorage=0.0;
    	for(HarddriveStorage hs : storageDriveList){
    		freeStorage = freeStorage + hs.getCurrentSize();
    	}
    	return freeStorage;
    }
    public void readData(List<NetStorageBlock> blockList){
    	//System.out.println("storage "+this.getId()+" recieved a list of "+blockList.size()+" blocks");
    	//List<HostPacket> pktlist =  new ArrayList<HostPacket>();
    	for(NetStorageBlock blk : blockList){
    		HostPacket pkt = new HostPacket(
					-1,
					blk.vmId,
					blk.getData(),
					CloudSim.clock(),
					-1,
					-1,
					blk.cloudletId); 
    		pkt.storageId = this.getId();
    		NetworkPacket npkt = new NetworkPacket(this.getId(),pkt,0,0);
    		npkt.stime = npkt.rtime;
    		npkt.pkt.recievetime = CloudSim.clock();
    		//System.out.println( blk.getIOTime());
    		CloudSim.send(getDatacenter().getId(), sw.getId(), blk.getIOTime(), CloudSimTags.Network_Event_UP, npkt);
    		//System.out.println("storage host sending packet ");
    		}
    }

	public void writeData(NetworkPacket hpkt){
		int clId = hpkt.pkt.virtualsendid;
		int replicas = (int) hpkt.pkt.data;
		int BlkIndex = hpkt.pkt.reciever;
		//System.out.println("packet infos :"+hpkt.pkt.sender+","+hpkt.pkt.virtualsendid);
		for(Vm vm : this.getDatacenter().getVmList()){
			//System.out.println("VM Id "+vm.getId());
			if(vm.getId() == hpkt.pkt.sender){
				//System.out.println("cloudlet size : "+ (hpkt.pkt.virtualsendid));
				for (ResCloudlet rcl : ((NetworkCloudletSpaceSharedScheduler)vm.getCloudletScheduler()).getCloudletExecList()){
					NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
					if(cl.getCloudletId() == clId){
						int writeIndex = cl.outDataindex.get(replicas);
							for(NetStorageBlock blk : cl.outputData){
								if(blk.getIndex() == BlkIndex){
									for(NetHarddriveStorage hdd : this.storageDriveList){
										String drivename = blk.getStorageDriveName().get(replicas);
										if(hdd.getName().equals(drivename)){
											cl.outDataindex.set(replicas, (writeIndex+1));
											hdd.addBlock(blk);
											System.out.print("write block for cl "+cl.getCloudletId()+" size "+cl.outputData.size());
											System.out.println(" in index "+writeIndex+" for replica "+replicas+"next index "+cl.outDataindex.get(replicas));
											return;
										}
									}
								}
						}
					}
				}
			}
		}
		
	}
}
