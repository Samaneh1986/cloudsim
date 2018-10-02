package org.cloudbus.cloudsim.network.exDatacenter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;

public class DCPerformance {
	//variables to compute switch performance
	private class SwitchParemeters{
		public int swId;
		public String swName;
		public int updCount = 0;
		public double mean_dwLink_busy_up=0.0;
		public double mean_dwLink_busy_dwn=0.0;
		public double mean_pkt_q_up=0.0;
		public double mean_pkt_q_dwn=0.0;
		public double mean_pkt_travel_time = 0.0;
		SwitchParemeters(int Id){ this.swId = Id;}
	}
	Map<Integer, SwitchParemeters> switchPerformance; // Integer is the switch Id
	
	//variables to compute Host performance
	
	
	public DCPerformance(){
		switchPerformance = new HashMap<Integer, SwitchParemeters>();
	}
	
	public void updateSwitchParams(Switch sw){
		int swId = sw.getId();
		int total_pkt_up = 0;
		double total_Byte_up = 0;
		int total_pkt_dw = 0;
		double total_Byte_dw = 0;
		double total_travel_time = 0.0;
			
		if(sw.packetTohost != null){
			List<NetworkPacket> pklist = new ArrayList<NetworkPacket>();
			for(int key : sw.packetTohost.keySet())
				pklist.addAll(sw.packetTohost.get(key));
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
			for(int key : sw.downlinkswitchpktlist.keySet())
				pklist.addAll(sw.downlinkswitchpktlist.get(key));
			for(NetworkPacket pk : pklist){
				total_pkt_dw = total_pkt_dw + 1;
				total_Byte_dw = total_Byte_dw + pk.pkt.data;
				total_travel_time = total_travel_time + (pk.rtime - pk.stime);
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
				/*if(sw.level == NetworkConstants.ROOT_LEVEL)
					swPrms.swLevel = "root";
				if(sw.level == NetworkConstants.Agg_LEVEL)
					swPrms.swLevel = "aggregation";
				if(sw.level == NetworkConstants.EDGE_LEVEL)
					swPrms.swLevel = "edge";*/
			}
			// update the parameters ......

			
			swPrms.updCount++;
			//swPrms.mean_pkt_q_up = ((swPrms.mean_pkt_q_up * (swPrms.updCount-1))+total_pkt_up)/swPrms.updCount;
			//swPrms.mean_pkt_q_dwn = ((swPrms.mean_pkt_q_dwn * (swPrms.updCount-1))+total_pkt_dw)/swPrms.updCount;
			swPrms.mean_pkt_q_up = swPrms.mean_pkt_q_up+total_pkt_up;
			swPrms.mean_pkt_q_dwn = swPrms.mean_pkt_q_dwn+total_pkt_dw;
			if(total_pkt_up>0){
				double link_busy_up = (8*(total_Byte_up/total_pkt_up))/sw.uplinkbandwidth;
				swPrms.mean_dwLink_busy_up = ((swPrms.mean_dwLink_busy_up * (swPrms.updCount-1))+link_busy_up)/swPrms.updCount;
			}
			//swPrms.mean_dwLink_busy = swPrms.mean_dwLink_busy + total_waited_bytes;
			if(total_pkt_dw>0){
				double mean_travel_time = total_travel_time / total_pkt_dw;
				swPrms.mean_pkt_travel_time = ((swPrms.mean_pkt_travel_time * (swPrms.updCount-1))+mean_travel_time)/swPrms.updCount;
				double link_busy_dw = (8*(total_Byte_dw/total_pkt_dw))/sw.downlinkbandwidth;
				swPrms.mean_dwLink_busy_dwn = ((swPrms.mean_dwLink_busy_dwn * (swPrms.updCount-1))+link_busy_dw)/swPrms.updCount;
			}
			// update the list ......
			switchPerformance.put(swId,swPrms);
		}
	}
	
	public void switchReport(){
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);
		df.applyPattern("#0.000000#");
		System.out.println("performance information for switchs (average values): ");
		String indent = "   ";
		Log.printLine("Switch ID" + indent+"Name"+ indent + "update_times" + indent + "packet_queue_up" + indent + "packet_queue_down" + indent +"busy_link_prc_up" + indent +"busy_link_prc_dwn"+ indent +"travel_time");
		indent = "      ";
		for(int id : switchPerformance.keySet()){
			SwitchParemeters swPrms = switchPerformance.get(id);
			Log.printLine("   "+id + indent+swPrms.swName+ indent+indent+swPrms.updCount+ indent+indent+(swPrms.mean_pkt_q_up)+ indent+indent+(swPrms.mean_pkt_q_dwn)+ indent+indent+df.format(swPrms.mean_dwLink_busy_up)+ indent+indent+df.format(swPrms.mean_dwLink_busy_dwn)+ indent+indent+swPrms.mean_pkt_travel_time);
			
		}
		
	}

}
