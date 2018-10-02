package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;

public class DCPerformance {
	//variables to compute switch performance
	private class SwitchParemeters{
		public int swId;
		public int updCount = 0;
		public double mean_dwLink_busy=0.0;
		public double mean_pkt_q=0.0;
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
		SwitchParemeters swPrms = null;
		if(switchPerformance.containsKey(swId)){
			swPrms = switchPerformance.get(swId);
		}
		else{
			swPrms = new SwitchParemeters(swId);
		}
		// update the parameters ......
		swPrms.updCount++;
		swPrms.mean_pkt_q = ((swPrms.mean_pkt_q * (swPrms.updCount-1))+sw.pktlist.size())/swPrms.updCount;
		double total_waited_bytes = 0.0;
		double total_travel_time = 0.0;
		for(NetworkPacket pk : sw.pktlist){
			total_waited_bytes = total_waited_bytes + pk.pkt.data;
			total_travel_time = total_travel_time + (pk.rtime - pk.stime);
		}
		double link_busy = (8*(total_waited_bytes/sw.pktlist.size()))/sw.downlinkbandwidth;
		swPrms.mean_dwLink_busy = ((swPrms.mean_dwLink_busy * (swPrms.updCount-1))+link_busy)/swPrms.updCount;
		
		double mean_travel_time = total_travel_time / sw.pktlist.size();
		swPrms.mean_pkt_travel_time = ((swPrms.mean_pkt_travel_time * (swPrms.updCount-1))+mean_travel_time)/swPrms.updCount;
		
		// update the list ......
		switchPerformance.put(swId,swPrms);
	}
	
	public void switchReport(){
		System.out.println("performance information for switchs: ");
		String indent = "  ";
		Log.printLine("Switch ID" + indent + "update_times" + indent + "packet_queue" + indent +"busy_link_prc"+ indent +"travel_time");
		indent = "          ";
		for(int id : switchPerformance.keySet()){
			SwitchParemeters swPrms = switchPerformance.get(id);
			Log.printLine("   "+id + indent+indent+swPrms.updCount+ indent+indent+swPrms.mean_pkt_q+ indent+indent+swPrms.mean_dwLink_busy+ indent+indent+swPrms.mean_pkt_travel_time);
			
		}
		
	}

}
