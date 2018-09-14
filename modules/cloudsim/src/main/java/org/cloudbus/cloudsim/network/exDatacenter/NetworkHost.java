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
public class NetworkHost extends Host {
	
	public List<NetworkPacket> packetTosendLocal;

	public List<NetworkPacket> packetTosendGlobal;
	public List<NetworkHost> hostOfsendGlobal;

        /**
         * List of received packets.
         */
	public List<NetworkPacket> packetrecieved;

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
	public double usedbandwidthSend; 
	public double usedbandwidthRcv; 
	
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

	public NetworkHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

		packetrecieved = new ArrayList<NetworkPacket>();
		packetTosendGlobal = new ArrayList<NetworkPacket>();
		packetTosendLocal = new ArrayList<NetworkPacket>();
		hostOfsendGlobal = new ArrayList<NetworkHost>();
		
		utilizedCpu = 0.0;
		unusedCpu = 0.0;

	}
	  

	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		// insert in each vm packet recieved 
		recvpackets();
		for (Vm vm : super.getVmList()) {
			double time = ((NetworkVm) vm).updateVmProcessing(currentTime, getVmScheduler()
					.getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}
		// send the packets to other hosts/VMs
		sendpackets();
		

		return smallerTime;

	}

	@Override
	public boolean vmCreate(Vm vm) {
		// TODO Auto-generated method stub 
		boolean result = super.vmCreate(vm);
		//System.out.println("VM creating ......");
		double peUsedPrc = 0.0;
		double freeCpu = 0;
		double usedCpu = 0;
		for(Pe p : this.getPeList()){
			if(p.getPeProvisioner().getTotalAllocatedMips() == 0)
				freeCpu++;
			else{
				usedCpu++;
				peUsedPrc = peUsedPrc + p.getPeProvisioner().getUtilization();
			} 
			if(p.getPeProvisioner().getAvailableMips() == 0){
				//System.out.println("PE full");
				p.setStatusBusy();
			}else{ 
				p.setStatusFree();
			}
		}
		//System.out.println("free no: "+freeCpu+", used no: "+usedCpu);
		peUsedPrc = peUsedPrc / usedCpu;
		if(unusedCpu == 0 && utilizedCpu == 0){
			utilizedCpu = peUsedPrc;
			unusedCpu = (freeCpu / (freeCpu+usedCpu));
			//System.out.println("First VM : " + unusedCpu+","+(freeCpu / (freeCpu+usedCpu)));
		}
		else{
			utilizedCpu = (utilizedCpu+peUsedPrc)/2;
			unusedCpu = (unusedCpu+(freeCpu/(freeCpu+usedCpu)))/2;
			//System.out.println("Next VM : " + utilizedCpu+","+unusedCpu);
		}
		
		return result;
	}
	public int getCurrentFreeCpuNo(){
		int result = 0;
		for(Pe p : this.getPeList()){
			if(p.getPeProvisioner().getTotalAllocatedMips() == 0)
				result++;
		}
		return result;
	}
	/**
	 * Receives packets and forward them to the corresponding VM.
	 */
	private void recvpackets() {
		for (NetworkPacket hs : packetrecieved) {
			hs.pkt.recievetime = CloudSim.clock();
 
			Vm vm = VmList.getById(getVmList(), hs.pkt.reciever);  
             
			List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
					.get(hs.pkt.sender);

			if (pktlist == null) {
				pktlist = new ArrayList<HostPacket>();
				((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(
						hs.pkt.sender,
						pktlist);

			}
			pktlist.add(hs.pkt); 
			}
		
		packetrecieved.clear();
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
                                NetworkPacket hpkt = new NetworkPacket(getId(), pkt, vm.getId(), pkt.sender);
                                Vm vm2 = VmList.getById(this.getVmList(), hpkt.recievervmid);
                                if (vm2 != null) {
                                        packetTosendLocal.add(hpkt);
                                } else {
                                        packetTosendGlobal.add(hpkt);
                                        for(Vm gVm : super.getDatacenter().getVmList()){
                                        	if(gVm.getId() == hpkt.recievervmid){
                                        		hostOfsendGlobal.add((NetworkHost) gVm.getHost());
                                        	}
                                        }
                                }
                       }
                        pktlist.clear();
                    }
		}
		/*****
		 *** compute up-link filled percentage by sending data ***
		 *****/
		
		double totSendTraffic = 0.0;
		for( NetworkPacket p : packetTosendGlobal){
			totSendTraffic = totSendTraffic + p.pkt.data * 8; // convert Byte to Bit
		} 
		if (totSendTraffic > 0){
			usedbandwidthSend = (totSendTraffic / this.bandwidth) * 100; 
		}
		/********************************************************/
		boolean flag = false;
		

		for (NetworkPacket hs : packetTosendLocal) {
                    flag = true;
                    hs.stime = hs.rtime;
                    hs.pkt.recievetime = CloudSim.clock();
              //      System.out.println("local sending start time : "+ hs.pkt.recievetime);
                    // insertthe packet in recievedlist
                    Vm vm = VmList.getById(getVmList(), hs.pkt.reciever);

                    List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
                                    .get(hs.pkt.sender);
                    if (pktlist == null) {
                            pktlist = new ArrayList<HostPacket>();
                            ((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(
                                            hs.pkt.sender,
                                            pktlist);
                    }
                    pktlist.add(hs.pkt);
		}
		if (flag) {
                    for (Vm vm : super.getVmList()) {
                        vm.updateVmProcessing(CloudSim.clock(), getVmScheduler().getAllocatedMipsForVm(vm));
                    }
              //      System.out.println("local sending end time : "+ CloudSim.clock());
		}

		// Sending packet to other VMs therefore packet is forwarded to a Edge switch
		packetTosendLocal.clear();
		double avband = bandwidth / packetTosendGlobal.size();
		int hostIx = 0;
		
		for (NetworkPacket hs : packetTosendGlobal) {
			hs.stime = hs.rtime;
            hs.pkt.recievetime = CloudSim.clock();
         //   System.out.println("global sending start time : "+ hs.pkt.recievetime);
                    double delay = (1000 * hs.pkt.data) / avband;
                    
                    //System.out.println("size :"+hs.pkt.data+", from "+this.getId()+" to "+hostOfsendGlobal.get(hostIx).getId()+" with VM size "+hostOfsendGlobal.get(hostIx).getVmList().size());
                    delay = DCMngUtility.computeDelay(this,hostOfsendGlobal.get(hostIx),hs.pkt.data);
                    NetworkConstants.totaldatatransfer += hs.pkt.data;
                    NetworkConstants.totaldatatransferTime += delay;

                    System.out.println("global sending delay time : "+ delay);
                    CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
                    // send to switch with delay
                    hostIx++;
    //                this.getBwProvisioner().deallocateBwForVm(VmList.getById(getVmList(), hs.pkt.sender));
            		}
		packetTosendGlobal.clear();
	//	System.out.println("global sending end time : "+ CloudSim.clock());
	}

        /**
         * Gets the maximum utilization among the PEs of a given VM.
         * @param vm The VM to get its PEs maximum utilization
         * @return The maximum utilization among the PEs of the VM.
         */
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
	}
       /**
        * Get the mean of used CPU usage percentage.
        * @return
        */
		public double getUtilizedCpu() {
			return utilizedCpu;
		}
		/**
		 * get the mean number of unused CPU
		 * @return
		 */
	public double getUnusedCpu(){
		return unusedCpu;
	}


}
