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
	private void recvpackets() {
		for (NetworkPacket hs : packetTowrite) {
			hs.pkt.recievetime = CloudSim.clock();
			//write data on disk
			}
		
		packetTowrite.clear();
	}

	/**
	 * Sends packets checks whether a packet belongs to a local VM or to a 
         * VM hosted on other machine.
	 */
	private void sendpackets() {
		
		for (Vm vm : super.getVmList()) {
		//	 DCMngUtility.resultFile.println("packets sending from VM"+vm.getId());
                    for (Entry<Integer, List<HostPacket>> es : ((NetworkCloudletSpaceSharedScheduler) vm
                                    .getCloudletScheduler()).pkttosend.entrySet()) {
                        List<HostPacket> pktlist = es.getValue();
                        for (HostPacket pkt : pktlist) {
                        	int pkNo = 1;
                        	double remainData = pkt.data;
                        	if(pkt.data > NetworkConstants.MAX_PACKET_SIZE_MB)
                        		pkNo = (int) Math.ceil(pkt.data / NetworkConstants.MAX_PACKET_SIZE_MB);
                        	for(int i = 0 ; i<pkNo ; i++){
                        		HostPacket newPkt = pkt;
                        		if(i == pkNo-1){
                        			newPkt.data = remainData;
                        			newPkt.isLastPkt = 1;
                        		}
                        		else{
                        			newPkt.data = NetworkConstants.MAX_PACKET_SIZE_MB;
                        			remainData = remainData - NetworkConstants.MAX_PACKET_SIZE_MB;
                        		}
                                NetworkPacket hpkt = new NetworkPacket(getId(), newPkt, vm.getId(), newPkt.sender);
                                Vm vm2 = VmList.getById(this.getVmList(), hpkt.recievervmid);
                                if (vm2 == null) {
                                        packetTosend.add(hpkt);
                                        for(Vm gVm : super.getDatacenter().getVmList()){
                                        	if(gVm.getId() == hpkt.recievervmid){
                                        		hostOfsendGlobal.add((NetStorageHost) gVm.getHost());
                                        	}
                                        }
                                }
                        	}
                       }
                        pktlist.clear();
                    }
		}
		boolean flag = false;
		
		// Sending packet to other VMs therefore packet is forwarded to a Edge switch
		double avband = bandwidth / packetTosend.size();
		int hostIx = 0;
		
		for (NetworkPacket hs : packetTosend) {
			hs.stime = hs.rtime;
            hs.pkt.recievetime = CloudSim.clock();
         //   System.out.println("global sending start time : "+ hs.pkt.recievetime);
                    double delay = (1000 * hs.pkt.data) / avband;
                    
                    //System.out.println("size :"+hs.pkt.data+", from "+this.getId()+" to "+hostOfsendGlobal.get(hostIx).getId()+" with VM size "+hostOfsendGlobal.get(hostIx).getVmList().size());
                  //  delay = DCMngUtility.computeDelay(this,hostOfsendGlobal.get(hostIx),hs.pkt);
                  //  delay = delay * 1000; //convert s to ms
                    
                    if(sw.getId() != hostOfsendGlobal.get(hostIx).sw.getId())
                    	NetworkConstants.interRackDataTransfer += hs.pkt.data;
                    NetworkConstants.totaldatatransfer += hs.pkt.data;
                    NetworkConstants.totaldatatransferTime += delay;

                    //System.out.println("global sending delay time : "+ delay);
                    CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
                    // send to switch with delay
                    hostIx++;
    //                this.getBwProvisioner().deallocateBwForVm(VmList.getById(getVmList(), hs.pkt.sender));
            		}
		packetTosend.clear();
	//	System.out.println("global sending end time : "+ CloudSim.clock());
	}

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
					0,
					blk.vmId,
					blk.getData(),
					CloudSim.clock(),
					-1,
					0,
					blk.cloudletId); 
    		pkt.storageId = this.getId();
    		NetworkPacket npkt = new NetworkPacket(this.getId(),pkt,0,0);
    		npkt.stime = npkt.rtime;
    		npkt.pkt.recievetime = CloudSim.clock();
    		CloudSim.send(getDatacenter().getId(), sw.getId(), 0, CloudSimTags.Network_Event_UP, npkt);
        }
    }
}
