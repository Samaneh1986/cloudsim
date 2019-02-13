/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * This class represents an Edge Switch in a Datacenter network. 
 * It interacts with other switches in order to exchange packets.
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
public class EdgeSwitch extends Switch {

	/**
	 * Instantiates a EdgeSwitch specifying switches that are connected to its downlink
	 * and uplink ports, and corresponding bandwidths. 
         * In this switch, downlink ports aren't connected to other switch but to hosts.
	 * 
	 * @param name Name of the switch
	 * @param level At which level the switch is with respect to hosts.
	 * @param dc The Datacenter where the switch is connected to
	 */

    
    public double EG_APE = 0; // the average percent of available processors per server 
    public double EG_UHS = 0; // the percentage of used hosts connected to this switch
    public double EG_ARA = 0; // the average of available ram per server
    public double EG_ALK = 0; // the average of links’ occupied percentage
    
    public double tot_ram = 0; // the total ram of connected hosts
    public double tot_pe = 0; // the total process of connected hosts
    public double tot_free_pe = 0; // the total free process of connected hosts
	
	public EdgeSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		uplinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		packetTohost = new HashMap<Integer, List<NetworkPacket>>();
		bytesTohostSize = new HashMap<Integer,Double>();
		uplinkswitches = new ArrayList<Switch>();
		hostlist = new HashMap<Integer, NetworkHost>();
	}

	@Override
	protected void processpacket_up(SimEvent ev) {
		// packet coming from down level router/host.
		// has to send up
		// check which switch to forward to
		// add packet in the switch list
		//
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.reciever;
		NetworkConstants.totaldatatransferTime += switching_delay;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);
		

		// packet is recieved from host
		// packet is to be sent to aggregate level or to another host in the same level
		int hostid = dc.VmtoHostlist.get(recvVMid);
		NetworkHost hs = hostlist.get(hostid);
		hspkt.recieverhostid = hostid;

		// packet needs to go to a host which is connected directly to switch
		if (hs != null) {
			// packet to be sent to host connected to the switch
			List<NetworkPacket> pktlist = packetTohost.get(hostid);
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				packetTohost.put(hostid, pktlist);
			}
			pktlist.add(hspkt);
			Double bytes_sum = 0.0;
			for(NetworkPacket p : pktlist)
				bytes_sum = bytes_sum+p.pkt.data;
			bytesTohostSize.put(hostid, bytes_sum);
			return;

		}
		// otherwise
		// packet is to be sent to upper switch
		// ASSUMPTION EACH EDGE is Connected to one aggregate level switch
		// if there are more than one Aggregate level switch one need to modify following code

		Switch sw = uplinkswitches.get(0);
		List<NetworkPacket> pktlist = uplinkswitchpktlist.get(sw.getId());
		if (pktlist == null) {
			pktlist = new ArrayList<NetworkPacket>();
			uplinkswitchpktlist.put(sw.getId(), pktlist);
		}
		pktlist.add(hspkt);
		return;

	}

	@Override
	protected void processpacketforward(SimEvent ev) {
		// search for the host and packets..send to them
		if (uplinkswitchpktlist != null) {
			for (Entry<Integer, List<NetworkPacket>> es : uplinkswitchpktlist.entrySet()) {
				int tosend = es.getKey();
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					// sharing bandwidth between packets
					double avband = uplinkbandwidth / hspktlist.size();
					Iterator<NetworkPacket> it = hspktlist.iterator();
					while (it.hasNext()) {
						NetworkPacket hspkt = it.next();
						double delay = 1000 * hspkt.pkt.data / avband;
						NetworkConstants.totaldatatransferTime += delay;
						this.send(tosend, delay, CloudSimTags.Network_Event_UP, hspkt);
					}
					hspktlist.clear();
				}
			}
		}
		if (packetTohost != null) {
			for (Entry<Integer, List<NetworkPacket>> es : packetTohost.entrySet()) {
				
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					double avband = downlinkbandwidth / hspktlist.size();
					Iterator<NetworkPacket> it = hspktlist.iterator();
					while (it.hasNext()) {
						NetworkPacket hspkt = it.next(); 
						//if(hspkt.pkt.hashCode()==504527234)
						//	System.out.println("packet 504527234 in switch "+this.getName());
						double delay = 1000 * hspkt.pkt.data / avband;
						NetworkConstants.totaldatatransferTime += delay;
						this.send(getId(), delay, CloudSimTags.Network_Event_Host, hspkt);
					}
					hspktlist.clear();
				}
			}
		}

	}
	
}
