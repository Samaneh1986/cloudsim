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
public class StorageSwitch extends Switch {

	/**
	 * Instantiates a EdgeSwitch specifying switches that are connected to its downlink
	 * and uplink ports, and corresponding bandwidths. 
         * In this switch, downlink ports aren't connected to other switch but to hosts.
	 * 
	 * @param name Name of the switch
	 * @param level At which level the switch is with respect to hosts.
	 * @param dc The Datacenter where the switch is connected to
	 */

    
    
	public StorageSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		uplinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		packetTohost = new HashMap<Integer, List<NetworkPacket>>();
		bytesTohostSize = new HashMap<Integer,Double>();
		uplinkswitches = new ArrayList<Switch>();
		storagelist = new HashMap<Integer, NetStorageHost>();
	}

	@Override
	protected void processpacket_up(SimEvent ev) {
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		
		int recvVMid = hspkt.pkt.reciever;
		NetworkConstants.totaldatatransferTime += switching_delay;
		//CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);
		int hostid = dc.VmtoHostlist.get(recvVMid);
		hspkt.recieverhostid = hostid;
		Switch sw = uplinkswitches.get(0);
		List<NetworkPacket> pktlist = uplinkswitchpktlist.get(sw.getId());
		if (pktlist == null) {
			pktlist = new ArrayList<NetworkPacket>();
			uplinkswitchpktlist.put(sw.getId(), pktlist);
		} 
		pktlist.add(hspkt);

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
					//	if(hspkt.pkt.hashCode()==504527234)
					//		System.out.println("packet 504527234 in switch "+this.getName());
						this.send(tosend, delay, CloudSimTags.Network_Event_UP, hspkt);
					}
					hspktlist.clear();
				}
			}
		}

	}

	
}
