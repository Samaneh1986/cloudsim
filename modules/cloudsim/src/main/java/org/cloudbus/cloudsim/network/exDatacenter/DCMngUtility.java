package org.cloudbus.cloudsim.network.exDatacenter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.UniformDistr;



public class DCMngUtility {
	//public static final int LOW_RESOURCES = 0;
	//public static final int ENOUGH_RESOURCES = 1;
	//public static final int EXTRA_RESOURCES = 9;
	/***************************************/
	public static final int VM_ALLC_PLCY_CLUSTER = 0;
	public static final int VM_ALLC_PLCY_SINGLE = 1;
	/***************************************/
	//public static PrintWriter resultFile;
	//public static PrintWriter performanceFile;
	/***************************************/
	public static DCPerformance dcPerformance;
	
	public static double max_alk = 0.0;
	public static double min_alk = 100.0;
	
	public static Map<Integer, ArrayList<Integer>> appClistIndex;
	
	public static void defineStagesOfTable(AppCloudlet app){
	//	System.out.println("in defineStagesOfTable for app "+app.appID);
		Map<Integer,Map<int[],Double>> dataTable = app.SendDataTo;
		ArrayList<Integer> clistIndex = appClistIndex.get(app.appID);
		System.out.println("data table size :" +  dataTable.size()+" for app "+app.appID);
	/**	double time = 0;
		UniformDistr ufrnd = new UniformDistr(2, 8);
		for (int i = 0; i < app.clist.size() ; i++) {
			time = Math.round(100 * ufrnd.sample());
			app.clist.get(i).numStage = 0;
			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  time, 0, 150, 0, 0));
			app.clist.get(i).numStage++;
		}**/
		for (int i : dataTable.keySet()) {//for all cloudlets in app
			Map<int[], Double> cur_cloudlet = dataTable.get(i); 
			int tot_stage = 0;
			int clIndex = -1;
    		for(int ix=0 ; ix<clistIndex.size(); ix++){
    			if(clistIndex.get(ix)==i)
    				clIndex = ix;
    		}
			if(dataTable.get(i)!=null)
			   tot_stage = dataTable.get(i).keySet().size();//all send/receive stages of current cloudlet
			for (int s = 1 ; s < tot_stage+1 ; s++){ // stage 0 is an execution stage
			    for (int[] stg_vm : dataTable.get(i).keySet()){  // an extra 'for' to add the stages in an ordered way
			    	if(stg_vm[0]== s){
			    		int peerClIndex = -1;
			    		for(int ix=0 ; ix<clistIndex.size(); ix++){
			    			if(clistIndex.get(ix)==stg_vm[1]) // receiver VM
			    				peerClIndex = ix;
			    		}
			    		int vmId = app.clist.get(peerClIndex).getVmId();
			    		int clId = app.clist.get(peerClIndex).getCloudletId();
			    		double data = cur_cloudlet.get(stg_vm);
			    		if(data > 0){
			    			System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(clIndex).getCloudletId()+" on VM"+app.clist.get(clIndex).getVmId()
			    					+" Sending "+data+" to CL "+clId+" on VM "+vmId);
			    			app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.WAIT_SEND, data,  0, stg_vm[0], 150, vmId,clId));
			    		}else{
			    			data = Math.abs(data);
			    			System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(clIndex).getCloudletId()+" on VM"+app.clist.get(clIndex).getVmId()
			    					+" receiving "+data+" from CL "+clId+" on VM "+vmId);
			    			app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.WAIT_RECV, data,  0, stg_vm[0], 150, vmId,clId));
				    		}
			    		}
			    }
		    }
		}
		 
		/**for (int i = 0; i < app.clist.size() ; i++) { 
			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  500, app.clist.get(i).numStage, 150, 0, 0));
		}**/
	 }

    public static int findSuitableSwitchForVmList(NetworkDatacenter DC, List<NetworkVm> vmList){ 
    	//DCMngUtility.resultFile.println("at time "+CloudSim.clock()+" for VM request "+vmList.get(0).getId()+" to "+vmList.get(vmList.size()-1).getId());
    	//List<NetworkHost> hostList = DC.getHostList(); 
    	List<NetworkHost> availableHostList = new ArrayList<NetworkHost>(); 
    	
    	int nu_req_VM = vmList.size();
    	
    	//calculate minimum required resources for a VM
    	double min_mips = 0;
    	long min_storage = 0; 
    	int min_ram = Integer.MAX_VALUE;
    	int min_pesNumber = 0; 
    	
    	double total_req_ram = 0;
    	double tot_pesNumber = 0; 
    	
    	EdgeSwitch esw = null;

    	for(NetworkVm vm : vmList ){
    		if ( min_mips > vm.getMips())
    			min_mips = vm.getMips();
    		if(min_storage > vm.getSize())
    			min_storage =  vm.getSize();
    		if(min_ram > vm.getRam())
    			min_ram =  vm.getRam();
    		if(min_pesNumber > vm.getNumberOfPes())
    			min_pesNumber =  vm.getNumberOfPes();
    		
    		total_req_ram = total_req_ram + vm.getRam(); 
    		tot_pesNumber = tot_pesNumber + vm.getNumberOfPes();
    	}  
    	
    	// calculate the list of available hosts
    	//for(NetworkHost host : hostList){
    	for(int sww : DC.getEdgeSwitch().keySet()){
    		EdgeSwitch edgesww = (EdgeSwitch) DC.getEdgeSwitch().get(sww);
    		if(edgesww.has_EG_value == 1){
    			edgesww.EG_NS = 0;
    			edgesww.EG_APE = 0;
    			edgesww.EG_UHS = 0;
    			edgesww.EG_ARA = 0;
    			edgesww.EG_ALK = 0;
    			edgesww.EG_SCORE = 0;
    			edgesww.EG_value = null;
    			edgesww.has_EG_value = 0;
    			edgesww.tot_free_pe = 0;
    			edgesww.tot_pe = 0;
    			edgesww.tot_ram = 0;
			}
    		if(edgesww.uplinkswitches.get(0).has_EG_value == 1){
    			edgesww.uplinkswitches.get(0).has_EG_value = 0;
    			edgesww.uplinkswitches.get(0).EG_NS = 0;
    			edgesww.uplinkswitches.get(0).EG_SCORE = 0;
    			edgesww.uplinkswitches.get(0).EG_value = null;
			}
    		for(int hostId : edgesww.hostlist.keySet()){
    			NetworkHost host = edgesww.hostlist.get(hostId);
    			esw =  edgesww;
    			double byt_que_size = 0.0;
				if(esw.bytesTohostSize!=null && esw.bytesTohostSize.get(host.getId())!= null )
					byt_que_size = esw.bytesTohostSize.get(host.getId());
				byt_que_size = byt_que_size + host.bytesTosendGlobalSize;	
				//esw.EG_ALK = esw.EG_ALK + (pkt_que_size/host.bandwidth);
				esw.EG_ALK = esw.EG_ALK + (byt_que_size*8);

				esw.EG_ARA = esw.EG_ARA + host.getRamProvisioner().getAvailableRam();
				esw.tot_ram = esw.tot_ram + host.getRam();
					
    			if(host.getMaxAvailableMips() >= min_mips &&
    				host.getStorage() >= min_storage &&
    				host.getRamProvisioner().getAvailableRam() >= min_ram &&
    				host.getNumberOfPes() >= min_pesNumber){
    			availableHostList.add(host);
    			
    			esw.EG_NS ++;
    			if(host.getVmList().size() > 0)
    				esw.EG_UHS ++;
    			esw.tot_free_pe = esw.tot_free_pe + host.getNumberOfFreePes();
    			esw.tot_pe = esw.tot_pe + host.getNumberOfPes(); 
    			
			}
    	}
    	}
    	System.out.println("The number of available hosts is :"+availableHostList.size() );
    	
    	// calculate the eligibility of each edgeSwitch
    	double max_eg = 0.00; 
    	String eg_class = null;
    	Switch best_sw = null;
    	Map<Integer,Switch> edgSwitchList = DC.getEdgeSwitch();
    	for (Entry<Integer, Switch> es : edgSwitchList.entrySet()) {
    		EdgeSwitch edgSW;
			edgSW = (EdgeSwitch)es.getValue();
			edgSW.has_EG_value = 1;
			edgSW.EG_APE = (edgSW.tot_free_pe ) / edgSW.tot_pe;   
			edgSW.EG_UHS = edgSW.EG_UHS / edgSW.EG_NS;
			edgSW.EG_ARA = edgSW.EG_ARA / edgSW.tot_ram;
			//edgSW.EG_ALK = 1-((edgSW.EG_ALK / edgSW.hostlist.size())/edgSW.downlinkbandwidth);
			edgSW.EG_ALK = (edgSW.EG_ALK / edgSW.hostlist.size());
			edgSW.EG_ALK  = edgSW.EG_ALK  / edgSW.downlinkbandwidth; 
			
			edgSW.uplinkswitches.get(0).EG_NS = edgSW.uplinkswitches.get(0).EG_NS + edgSW.EG_NS;
			edgSW.uplinkswitches.get(0).EG_MAX_NS = Math.max(edgSW.uplinkswitches.get(0).EG_MAX_NS, edgSW.EG_NS);
				
			 //********* Eligibility Calculation  **********//
			
				double temp = 0;
				double rule = 0;
				// ..... fuzzy operations ......
				// Rule 1: If APE is overloaded or ARA is unqualified or ALK is high THEN EG is ineligible
				rule = APE_membership(edgSW,"OVRLD"); 
				temp = ARA_membership(edgSW,"UNQLFY");  
				rule = Math.max(rule, temp);
				temp = ALK_membership(edgSW, "HIGH"); 
				rule = Math.max(rule, temp);
				//System.out.println("RULE 1 : "+rule + ">"+edgSW.EG_SCORE);
				if(edgSW.EG_NS < nu_req_VM) 
					rule = 1;
				if(rule > edgSW.EG_SCORE){
					edgSW.EG_SCORE = rule;
					edgSW.EG_value = "INELGB";
				}
				// RUEL 2: IF APE is normal and ARA is not unqulify and ALK is not high THEN EG is eligible.
					rule = APE_membership(edgSW,"NRML"); 
					temp = 1 - ARA_membership(edgSW,"UNQLFY"); 
					rule = Math.min(rule, temp); 
					temp = ALK_membership(edgSW, "LOW"); 
					rule = Math.min(rule, temp); 
					//System.out.println("RULE 2 : "+rule + ">="+edgSW.EG_SCORE);
					if(rule >= edgSW.EG_SCORE){
						edgSW.EG_SCORE = rule;
						edgSW.EG_value = "ELGB";
					}
				// RUEL 3: IF APE is normal ARA is normal and UHS is not unutilized and and ALK is low THEN EG is highly eligible
					rule = APE_membership(edgSW,"NRML");
					temp = ARA_membership(edgSW,"NRML");
					rule = Math.min(rule, temp);
					temp = 1- UHS_membership(edgSW, "IDLE");
					rule = Math.min(rule, temp);
					temp = ALK_membership(edgSW, "LOW");
					rule = Math.min(rule, temp);
					//System.out.println("RULE 3 : 1.6 * "+rule + ">= "+edgSW.EG_SCORE);
					if((1.4*rule) >= edgSW.EG_SCORE){
						System.out.println(" for switch "+edgSW.getName()+" HELGB score :"+ rule);
						edgSW.EG_SCORE = rule;
						edgSW.EG_value = "HELGB";
					}
				
			
			//**********************************************//
			//System.out.println("For Switch ID :" + edgSW.getName() +" score :"+edgSW.EG_SCORE + "for "+edgSW.EG_value);
			//System.out.println("NS="+edgSW.EG_NS+", APE="+edgSW.EG_APE+", ARA="+edgSW.EG_ARA+", ALK="+edgSW.EG_ALK);
			//	System.out.println(" for switch "+edgSW.uplinkswitches.get(0).getName()+" from "
			//			+edgSW.uplinkswitches.get(0).EG_value+" and "+edgSW.uplinkswitches.get(0).EG_SCORE);
			if(edgSW.EG_value.equals("HELGB")){
				if(edgSW.uplinkswitches.get(0).EG_value == null || edgSW.uplinkswitches.get(0).EG_value.equals("HELGB")){
					if (edgSW.EG_SCORE > edgSW.uplinkswitches.get(0).EG_SCORE){
						edgSW.uplinkswitches.get(0).EG_value = "HELGB";
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
					}
				}
				else{
					edgSW.uplinkswitches.get(0).EG_value = "HELGB";
					edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
				}
			} 
			if(edgSW.EG_value.equals("ELGB")){
				if(edgSW.uplinkswitches.get(0).EG_value == null || edgSW.uplinkswitches.get(0).EG_value.equals("ELGB")){
					if (edgSW.EG_SCORE > edgSW.uplinkswitches.get(0).EG_SCORE){
						edgSW.uplinkswitches.get(0).EG_value = "ELGB";
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
					}
				}
				else{
					if(!edgSW.uplinkswitches.get(0).EG_value.equals("HELGB")){
						edgSW.uplinkswitches.get(0).EG_value = "ELGB";
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
					}
				}
			}
			if(edgSW.EG_value.equals("INELGB")){
				if(edgSW.uplinkswitches.get(0).EG_value == null){
					edgSW.uplinkswitches.get(0).EG_value = "INELGB";
					edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
						
				}
				else{
					if(edgSW.uplinkswitches.get(0).EG_value.equals("INELGB")){
						if (edgSW.EG_SCORE < edgSW.uplinkswitches.get(0).EG_SCORE)
							edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
					}
				}
			}
		//	System.out.println(" To "
		//			+edgSW.uplinkswitches.get(0).EG_value+" and "+edgSW.uplinkswitches.get(0).EG_SCORE);
		if(eg_class == null && !edgSW.EG_value.equals("INELGB")){
	    	eg_class = edgSW.EG_value;
	    	max_eg = edgSW.EG_SCORE;
	    	best_sw = edgSW; 
	    }
	    else{
	    	if(edgSW.EG_value.equals("HELGB")){
	    		if(!eg_class.equals("HELGB") || edgSW.EG_SCORE > max_eg){
	    			eg_class = edgSW.EG_value;
	    	    	max_eg = edgSW.EG_SCORE;
	    	    	best_sw = edgSW; 
	    		}
	    	}
	    	if(edgSW.EG_value.equals("ELGB")){
	    		if(!eg_class.equals("HELGB") && edgSW.EG_SCORE > max_eg){
	    			eg_class = edgSW.EG_value;
	    	    	max_eg = edgSW.EG_SCORE;
	    	    	best_sw = edgSW; 
	    		}
	    	}
	    }
    	} 
    	if(eg_class != null && !eg_class.equals("INELGB") )
    		return best_sw.getId();
    	// find suitable aggregation switch
    	int max_available_host = 0;
    	AggregateSwitch max_hosts_agg_sw = null;
    	max_eg = 0.00;
    	double max_ieg = 0.00;
    	double min_ieg = 9.00;
    	AggregateSwitch ineglb_agg_sw = null;
    	//
    	Map<Integer,Switch> aggSwitchList = DC.getAggSwitch();
    	for (Entry<Integer, Switch> es : aggSwitchList.entrySet()) {
			AggregateSwitch aggSW;
			aggSW = (AggregateSwitch) es.getValue();
			aggSW.has_EG_value = 1;
			//System.out.println("agg "+aggSW.id+", NS :"+aggSW.EG_NS+ ", req VMs:"+nu_req_VM);
			if( aggSW.EG_NS >= nu_req_VM){
				if(aggSW.EG_SCORE < min_ieg){
					min_ieg = aggSW.EG_SCORE;
					ineglb_agg_sw = aggSW;
				}
				if(aggSW.EG_SCORE > max_ieg)
					max_ieg = aggSW.EG_SCORE;
				if(aggSW.EG_MAX_NS > max_available_host){
					max_available_host = aggSW.EG_MAX_NS;
					max_hosts_agg_sw = aggSW;
				}
			}
    	}
   
    	if(min_ieg < 9 ){ // there is at leas one agg switch with enough resources
    		if( min_ieg == max_ieg) // all aggs are equal in quality
    			best_sw = max_hosts_agg_sw;
    		else
    			best_sw = ineglb_agg_sw;
    	}
    	if(best_sw == null) // no agg switch can serve the VMs alone and root switch is returned
    		best_sw = DC.getRootSwitch().values().iterator().next();
    
    	
    	return best_sw.getId();
    } 

    private static double APE_membership(EdgeSwitch edgSW, String value){
    	double APE_mf = 0; 
    	//// 
    	if(value == "OVRLD"){
    		if(edgSW.EG_APE <= 0.3)
    			APE_mf = 1;
    		else{
    			if(edgSW.EG_APE > 0.4)
    				APE_mf = 0;
    			else
    				APE_mf = (-10 * edgSW.EG_APE) + 4;
    		}
    	}
    	////
    	if(value == "NRML"){
    		if(edgSW.EG_APE > 0.4)
    			APE_mf = 1;
    		else
    			if(edgSW.EG_APE <= 0.3)
    				APE_mf = 0;
    			else
    				APE_mf = (10 * edgSW.EG_APE) - 3;
    	}
    	return APE_mf;
    }//................ END of APE membership .................//
    private static double UHS_membership(EdgeSwitch edgSW, String value){
    	double UHS_mf = 0; 
    	////

		//System.out.println("UHS VLUE : "+edgSW.EG_UHS);
    	if(value == "IDLE"){
    		if(edgSW.EG_UHS <= 0.1)
    			UHS_mf = 1;
    		else{
    			if(edgSW.EG_UHS > 0.25)
    				UHS_mf = 0;
    			else
    				UHS_mf = (((-20 * edgSW.EG_UHS) + 5)/3);
    		}
    	}
    	////
    	if(value == "NRML"){
    		if(edgSW.EG_UHS >= 0.25 )
    			UHS_mf = 1;
    		else
    			if(edgSW.EG_UHS < 0.1 )
    				UHS_mf = 0;
    			else{
    				if(edgSW.EG_UHS < 0.25)
    					UHS_mf = (((20 * edgSW.EG_UHS) - 2)/3);

    				if(edgSW.EG_UHS > 0.4)
    					UHS_mf = (-10 * edgSW.EG_UHS) + 5;
    			}
    	} 
    	return UHS_mf;
    }//................ END of UHS membership .................//

    private static double ARA_membership(EdgeSwitch edgSW, String value){
    	double ARA_mf = 0;
    	
    	if(value == "UNQLFY"){
    		if(edgSW.EG_ARA <= 0.25)
    			ARA_mf = ((-4 * edgSW.EG_ARA) + 1);
    		else
    			ARA_mf = 0;  
    	}
    	///
    	if(value == "NRML"){
    		if(edgSW.EG_ARA <= 0.5)
    			ARA_mf = 2 * edgSW.EG_ARA;
    		else
    			ARA_mf = -2 * edgSW.EG_ARA+2;
    	}
    	if(value == "FREE"){
    		if(edgSW.EG_ARA <= 0.75)
    			ARA_mf = 0;
    		else
    			ARA_mf = (4 * edgSW.EG_ARA)-3;
    	}
    	return ARA_mf;
    }//................ END of ARA membership .................//

    private static double ALK_membership(EdgeSwitch edgSW , String value){
    	// change it to triangle one !!!!
    	double ALK_mf = 0;
    	double balanced_prc = edgSW.EG_ALK + 0.3; // 0.3 of bandwidth getten by others
    	////
    	if(value == "LOW"){
    		if(balanced_prc <= 0.75)
    			ALK_mf = ((-4 * balanced_prc)/3 + 1);
    		else
    			ALK_mf = 0;
    	}
    	////
    	if(value == "MID"){
    		if(balanced_prc <= 0.5)
    			ALK_mf = 2 * balanced_prc;
    		else
    			ALK_mf = -2 * balanced_prc+2;
    	}
    	////
    	if(value == "HIGH"){
    		if(balanced_prc> 0.25)
    			ALK_mf = ((4 * balanced_prc)-1)/3;
    		else
    			ALK_mf = 0;
    	}
    	return ALK_mf;
    }//................ END of ALK membership .................//
  
    public static double computeDelay(NetworkHost srcHost,NetworkHost destHost, HostPacket pk ){
    	//DCMngUtility.resultFile.println("at time "+CloudSim.clock()+" delay from host "+srcHost.getId()+" to "+destHost.getId()+" queu at src:"+srcHost.packetTosendGlobal.size()+","+srcHost.packetTosendGlobalSize);
    	
    		
    	double dataTransfer = pk.data;
		double delay = 0.0; 		
		String upsw1 = srcHost.sw.getName();
		String upsw2 = destHost.sw.getName();
		if(upsw1.equalsIgnoreCase(upsw2)) // Hosts are in the same rack
		{
			delay = host_to_edge_delay(srcHost,dataTransfer)+ host_to_edge_delay(destHost,dataTransfer);
			delay = delay+ (srcHost.sw.switching_delay*dataTransfer); 
			return delay;
		}
		else{
			String upsw11 = null;
			String upsw22 = null;
			for(Switch ns1  : srcHost.sw.uplinkswitches){ 
				 upsw11 = ns1.getName();
				for(Switch ns2  : destHost.sw.uplinkswitches){ 
					 upsw22 = ns2.getName();
					if(upsw11.equalsIgnoreCase(upsw22)) // Hosts are not in the same rack but same aggSwitch
					{
						//double packet_in_src_link = 0.0;
						//double packet_in_dsc_link = 0.0;
						//if(ns2.downlinkswitchpktlist.get(srcHost.sw.getId()) != null)
						//	packet_in_src_link= ns2.downlinkswitchpktlist.get(srcHost.sw.getId()).size();
						//if(ns2.downlinkswitchpktlist.get(destHost.sw.getId()) != null)
						//	packet_in_dsc_link= ns2.downlinkswitchpktlist.get(destHost.sw.getId()).size();
						delay=    host_to_edge_delay(srcHost,dataTransfer)
								+ (srcHost.sw.switching_delay * dataTransfer)
								+ edg_to_agg_delay(srcHost.sw,ns1,dataTransfer)
								+ (ns2.switching_delay * dataTransfer)
								+ edg_to_agg_delay(destHost.sw,ns2,dataTransfer)
								+ (destHost.sw.switching_delay * dataTransfer)
								+ host_to_edge_delay(destHost,dataTransfer);
						
						/*delay = dataTransfer * (
								 (8/(srcHost.sw.downlinkbandwidth/((srcHost.packetTosendGlobal.size()==0)?1:srcHost.packetTosendGlobal.size())))
								+ srcHost.sw.switching_delay
								+ (8/(ns2.downlinkbandwidth/((packet_in_src_link==0)?1:(packet_in_src_link))))
								+ ns2.switching_delay
								+ (8/(ns2.downlinkbandwidth/((packet_in_dsc_link==0)?1:(packet_in_dsc_link))))
								+ destHost.sw.switching_delay
								+ (8/(destHost.sw.downlinkbandwidth/((destHost.packetTosendGlobal.size()==0)?1:destHost.packetTosendGlobal.size())))
								);*/


						NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
						return delay;
					}
				}
			}
			// Hosts do not have the same aggSwitch, packets should pass from root
			//System.out.print("pckets through root from vm "+pk.sender+" to vm "+pk.reciever);
			Switch srcAgg  = srcHost.sw.uplinkswitches.get(0);
			Switch destAgg = destHost.sw.uplinkswitches.get(0);
			Switch root = srcHost.sw.uplinkswitches.get(0).uplinkswitches.get(0);
			delay=    host_to_edge_delay(srcHost,dataTransfer)
					+ (srcHost.sw.switching_delay * dataTransfer)
					+ edg_to_agg_delay(srcHost.sw,srcAgg,dataTransfer)
					+ (srcAgg.switching_delay * dataTransfer)
					+ agg_to_root_delay(srcAgg,root,dataTransfer)
					+ (root.switching_delay * dataTransfer)
					+ agg_to_root_delay(destAgg,root,dataTransfer)
					+ (destAgg.switching_delay * dataTransfer)
					+ edg_to_agg_delay(destHost.sw,destAgg,dataTransfer)
					+ (destHost.sw.switching_delay * dataTransfer)
					+ host_to_edge_delay(destHost,dataTransfer);
			
			
		}
		NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
		return delay;
	}
    private static double host_to_edge_delay(NetworkHost host, double data){
    	// "bytesTosendGlobalSize" includes "data"
    	//fraction is = this_data/all_data
    	//available bandwidth is "whole bandwidth" * fraction
    	return ((data*8) / (host.sw.downlinkbandwidth * (((host.bytesTosendGlobalSize==0)?1:data/host.bytesTosendGlobalSize))));
    }
    private static double edg_to_agg_delay(Switch edgSw, Switch aggSw, double data){
    	// "bytesTosendGlobalSize" includes "data"
    	//fraction is = this_data/all_data
    	//available bandwidth is "whole bandwidth" * fraction
    	double byts_on_link = 0.0;
    	if(edgSw.bytesToUpSwitchSize != null && edgSw.bytesToUpSwitchSize.get(aggSw.getId()) != null)
    		byts_on_link = byts_on_link + edgSw.bytesToUpSwitchSize.get(aggSw.getId());
    	
    	if(aggSw.bytesToDownSwitchSize != null &&  aggSw.bytesToDownSwitchSize.get(edgSw.getId()) != null)
    		byts_on_link = byts_on_link + aggSw.bytesToDownSwitchSize.get(edgSw.getId());
    	
    	return ((data*8) / (edgSw.uplinkbandwidth * (((byts_on_link==0)?1:data/byts_on_link))));
    }
    private static double agg_to_root_delay(Switch aggSw, Switch rootSw, double data){
    	// "bytesTosendGlobalSize" includes "data"
    	//fraction is = this_data/all_data
    	//available bandwidth is "whole bandwidth" * fraction
    	double byts_on_link = 0.0;
    	if(aggSw.bytesToUpSwitchSize != null && aggSw.bytesToUpSwitchSize.get(rootSw.getId()) != null)
    		byts_on_link = byts_on_link + aggSw.bytesToUpSwitchSize.get(rootSw.getId());
    	
    	if(rootSw.bytesToDownSwitchSize != null &&  rootSw.bytesToDownSwitchSize.get(aggSw.getId()) != null)
    		byts_on_link = byts_on_link + rootSw.bytesToDownSwitchSize.get(aggSw.getId());
    	
    	return ((data*8) / (aggSw.uplinkbandwidth * (((byts_on_link==0)?1:data/byts_on_link))));
    }
}
