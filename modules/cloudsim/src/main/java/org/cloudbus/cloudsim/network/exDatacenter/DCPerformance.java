package org.cloudbus.cloudsim.network.exDatacenter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.core.CloudSim;

public class DCPerformance {
	//variables to compute switch performance
	private class SwitchParemeters{
		public int swId;
		public String swName;
		public double setTime = 0.0;
		public int updCount = 0;
		public double mean_dwLink_busy_up=0.0;
		public Map<Integer,Double> mean_dwLink_busy_dwn=null;
		public double mean_byt_q_up=0.0;
		public Map<Integer,Double> mean_byt_q_dwn=null;
		public double mean_pkt_travel_time = 0.0;
		SwitchParemeters(int Id){ this.swId = Id;}
	}
	Map<Integer, SwitchParemeters> switchPerformance; // Integer is the switch Id
	
	//variables to compute Host performance
	private class HostParemeters{
		public int hostId;
		public int updCount = 0;
		public long total_VM_served=0;
		public double mean_mem_used=0.0;
		public double mean_cpu_used=0.0;
		public double max_mem_used=0.0;
		public double max_cpu_used = 0.0;
		public double total_ram;
		public double  total_mips;
		HostParemeters(int Id){ this.hostId = Id;}
	}
	Map<Integer, HostParemeters> hostPerformance; // Integer is the host Id
	
	public DCPerformance(){
		switchPerformance = new HashMap<Integer, SwitchParemeters>();
		hostPerformance = new HashMap<Integer, HostParemeters>();
	}
	/*
	public void updateSwitchParams_old(Switch sw){
		int swId = sw.getId();
		int total_pkt_up = 0;
		double total_Byte_up = 0;
		int total_pkt_dw = 0;
		double total_Byte_dw = 0;
		double total_travel_time = 0.0;
		int down_link = 0;
			
		if(sw.packetTohost != null){
			List<NetworkPacket> pklist = new ArrayList<NetworkPacket>();
			for(int key : sw.packetTohost.keySet()){
				pklist.addAll(sw.packetTohost.get(key));
				if(sw.packetTohost.get(key).size()>0)
					down_link++;
			}
			for(NetworkPacket pk : pklist){
				total_pkt_dw = total_pkt_dw + 1;
				total_Byte_dw = total_Byte_dw + pk.pkt.data;
				total_travel_time = total_travel_time + (pk.rtime - pk.stime);
			}
			pklist.clear();
		}
		if(sw.uplinkswitchpktlist != null){ 
			List<NetworkPacket> pklist = new ArrayList<NetworkPacket>();
			for(int key : sw.uplinkswitchpktlist.keySet())
				pklist.addAll(sw.uplinkswitchpktlist.get(key));
			for(NetworkPacket pk : pklist){
				total_pkt_up = total_pkt_up + 1;
				total_Byte_up = total_Byte_up + pk.pkt.data;
			}
			pklist.clear();
		}
		if(sw.downlinkswitchpktlist != null){
			List<NetworkPacket> pklist = new ArrayList<NetworkPacket>();
			for(int key : sw.downlinkswitchpktlist.keySet()){
				pklist.addAll(sw.downlinkswitchpktlist.get(key));
				if(sw.downlinkswitchpktlist.get(key).size()>0)
					down_link++;
			}
			for(NetworkPacket pk : pklist){
				total_pkt_dw = total_pkt_dw + 1;
				total_Byte_dw = total_Byte_dw + pk.pkt.data;
				total_travel_time = total_travel_time + (pk.pkt.recievetime - pk.pkt.sendtime);
			}
			pklist.clear();
		}
		
		if((total_pkt_up+total_pkt_dw)>0){
			//if there is new data
			SwitchParemeters swPrms = null;
			if(switchPerformance.containsKey(swId)){
				swPrms = switchPerformance.get(swId);
			}
			else{
				swPrms = new SwitchParemeters(swId);
				swPrms.swName = sw.getName(); 
			}
			// update the parameters ......

			
			swPrms.updCount++;
			//swPrms.mean_pkt_q_up = ((swPrms.mean_pkt_q_up* (swPrms.updCount-1))+total_pkt_up)/swPrms.updCount;
			swPrms.mean_byt_q_up = Math.max(swPrms.mean_byt_q_up , total_Byte_dw);
			//if(total_pkt_dw>0)
			//	swPrms.mean_pkt_q_dwn = (((swPrms.mean_pkt_q_dwn* (swPrms.updCount-1))+(total_pkt_dw)/down_link))/swPrms.updCount;
			swPrms.mean_byt_q_dwn = Math.max(swPrms.mean_byt_q_dwn, total_pkt_dw);
			if(total_pkt_up>0){
				double link_busy_up = (8*(total_Byte_up/total_pkt_up))/sw.uplinkbandwidth;
				swPrms.mean_dwLink_busy_up = ((swPrms.mean_dwLink_busy_up * (swPrms.updCount-1))+link_busy_up)/swPrms.updCount;
			}
			if(total_pkt_dw>0){
				double mean_travel_time = total_travel_time / total_pkt_dw;
				swPrms.mean_pkt_travel_time = ((swPrms.mean_pkt_travel_time * (swPrms.updCount-1))+mean_travel_time)/swPrms.updCount;
				double link_busy_dw = ((8*(total_Byte_dw/total_pkt_dw)))/sw.downlinkbandwidth;
				//double link_busy_dw = ();
				swPrms.mean_dwLink_busy_dwn = ((swPrms.mean_dwLink_busy_dwn * (swPrms.updCount-1))+link_busy_dw)/swPrms.updCount;
			}
			// update the list ......
			switchPerformance.put(swId,swPrms);
		}
	}
	*/
	public void updateSwitchParams(Switch sw, String typ, NetworkPacket PK ){
		int swId = sw.getId();
		SwitchParemeters swPrms = null;
		//System.out.println(" runned for switch "+sw.getName());
		if(switchPerformance.containsKey(swId)){
			swPrms = switchPerformance.get(swId);
		}
		else{
			swPrms = new SwitchParemeters(swId);
			swPrms.swName = sw.getName(); 
			swPrms.mean_dwLink_busy_dwn = new HashMap<Integer,Double>();
			swPrms.mean_byt_q_dwn = new HashMap<Integer,Double>();
		}
		if(CloudSim.clock() - swPrms.setTime > 10000){
			double timeInterval = CloudSim.clock() - swPrms.setTime;
			timeInterval = Math.round(timeInterval/1000);
			swPrms.setTime = CloudSim.clock();
			if(swPrms.mean_byt_q_dwn.get(PK.recieverhostid)!=null){
				double dataByNow = 0.0;
				dataByNow = swPrms.mean_byt_q_dwn.get(PK.recieverhostid);
				dataByNow = dataByNow / timeInterval; // bit per second
				dataByNow = dataByNow / 10240;// divided by bandwidth
				double oldValue = 0.0;
				if(swPrms.mean_dwLink_busy_dwn.get(PK.recieverhostid)!=null)
					oldValue = swPrms.mean_dwLink_busy_dwn.get(PK.recieverhostid);
				swPrms.mean_dwLink_busy_dwn.put(PK.recieverhostid, Math.max(dataByNow,oldValue));
				swPrms.mean_byt_q_dwn.put(PK.recieverhostid, 0.0);
			}
		}
		if(typ.equals("dataOnDownLink")){
			//System.out.println("packet sent to down");
			double data = 0.0;
			if(swPrms.mean_byt_q_dwn.get(PK.recieverhostid)!=null)
				data = swPrms.mean_byt_q_dwn.get(PK.recieverhostid);
			data = data +  (PK.pkt.data * 8);
			swPrms.mean_byt_q_dwn.put(PK.recieverhostid, data);
		}
		if(typ.equals("dataOnUpLink")){
			//System.out.println("packet sent to up");
			swPrms.mean_byt_q_up = swPrms.mean_byt_q_up + (PK.pkt.data * 8);
		}
		switchPerformance.put(swId,swPrms);
	}
	
	public void switchReport(){
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);
		df.applyPattern("#0.000000#");
		System.out.println("performance information for switchs (average values): ");
		String indent = "   ";
		Log.printLine("Switch ID" + indent+"Name"+ indent + "bytes_queue_up" + indent + "bytes_queue_down" + indent +"busy_link_prc_up" + indent +"busy_link_prc_dwn"+ indent +"travel_time");
		indent = "      ";
		for(int id : switchPerformance.keySet()){
			SwitchParemeters swPrms = switchPerformance.get(id);
			double maxBusy = 0.0;
			double lastBusy = 0.0;
			for(int hostLinks : swPrms.mean_dwLink_busy_dwn.keySet()){
				maxBusy = Math.max(maxBusy, swPrms.mean_dwLink_busy_dwn.get(hostLinks));
				lastBusy = Math.max(maxBusy, swPrms.mean_byt_q_dwn.get(hostLinks));
			}
			maxBusy = maxBusy + 0.3; // assume 30% of links is for other kinds of jobs
			Log.printLine("   "+id +","+swPrms.updCount+ indent+swPrms.swName+ indent+indent+df.format(swPrms.mean_byt_q_up)+ indent+indent+df.format(lastBusy)+ indent+indent+df.format(swPrms.mean_dwLink_busy_up)+ indent+indent+df.format(maxBusy)+ indent+indent+swPrms.mean_pkt_travel_time);
			
		}
		
	}

	public void updateHostParams(NetworkHost host, int inVmCreate){
		// this function is called on each host when a new VM is created
		int hostId = host.getId();
		HostParemeters hostPrms = null;
		if(hostPerformance.containsKey(hostId)){
			hostPrms = hostPerformance.get(hostId);
		}
		else{
			hostPrms = new HostParemeters(hostId);
		}
		hostPrms.updCount++;
		if(inVmCreate == 1)
			hostPrms.total_VM_served++;
		double peUsedPrc = 0.0;
		double totalPe = 0.0;
		for(Pe p : host.getPeList()){
				peUsedPrc = peUsedPrc + p.getPeProvisioner().getTotalAllocatedMips();
				totalPe = totalPe + p.getPeProvisioner().getMips();
			} 
		peUsedPrc = peUsedPrc / totalPe;
		hostPrms.total_mips = totalPe;
		if(hostPrms.max_cpu_used < peUsedPrc)
			hostPrms.max_cpu_used = peUsedPrc;
		hostPrms.mean_cpu_used = ((hostPrms.mean_cpu_used * (hostPrms.updCount-1))+peUsedPrc)/hostPrms.updCount;
		double memused = host.getRamProvisioner().getUsedRam();
		if(hostPrms.max_mem_used < memused)
			hostPrms.max_mem_used = memused;
		hostPrms.mean_mem_used = ((hostPrms.mean_mem_used * (hostPrms.updCount-1))+memused)/hostPrms.updCount;
		hostPrms.total_ram = host.getRamProvisioner().getRam();
		hostPerformance.put(hostId, hostPrms);
	}
	public void hostReport(){
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);
		df.applyPattern("#0.000000#");
		System.out.println("performance information for hosts (average and total values): ");
		String indent = "   ";
		Log.printLine("Host ID" + indent+"total_Ram" + indent+"total_Mips"+indent+"total_VMs"+ indent + "MAX_CPU" + indent + "Mean_CPU" + indent + "Max_Ram" + indent +"Mean_Ram" );
		indent = "      ";
		for(int id : hostPerformance.keySet()){
			HostParemeters hsPrms = hostPerformance.get(id);
			Log.printLine("   "+id +indent+hsPrms.total_ram+indent+hsPrms.total_mips+ indent+hsPrms.total_VM_served+ indent+hsPrms.max_cpu_used+ indent+df.format(hsPrms.mean_cpu_used)+indent+(hsPrms.max_mem_used)+ indent+df.format(hsPrms.mean_mem_used));
			
		}
		
	}
}
