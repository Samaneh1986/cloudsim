package org.cloudbus.cloudsim.network.exDatacenter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.UniformDistr;



public class DCMngUtility {
	public static final int LOW_RESOURCES = 0;
	public static final int ENOUGH_RESOURCES = 1;
	public static final int EXTRA_RESOURCES = 9;
	/***************************************/
	public static final int VM_ALLC_PLCY_CLUSTER = 0;
	public static final int VM_ALLC_PLCY_SINGLE = 1;
	/***************************************/
	//public static PrintWriter resultFile;
	/***************************************/
	
	public static void defineStagesOfTable(AppCloudlet app){
	//	System.out.println("in defineStagesOfTable .....");
		ArrayList<Map<int[], Double>> dataTable = app.SendDataTo;
	//	System.out.println("data table size :" +  dataTable.size());
	/**	double time = 0;
		UniformDistr ufrnd = new UniformDistr(2, 8);
		for (int i = 0; i < app.clist.size() ; i++) {
			time = Math.round(100 * ufrnd.sample());
			app.clist.get(i).numStage = 0;
			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  time, 0, 150, 0, 0));
			app.clist.get(i).numStage++;
		}**/
		for (int i = 0; i < dataTable.size() ; i++) {
		//	System.out.println("first reciever no :"+dataTable.get(i).get(0));
			Map<int[], Double> cur_cloudlet = dataTable.get(i); 
		    for (int[] stg_vm : dataTable.get(i).keySet()){ 
		    	if(stg_vm[0]!= -1){
		    		int vmId = app.clist.get(stg_vm[1]).getVmId();
		    		int clId = app.clist.get(stg_vm[1]).getCloudletId();
		    		double data = cur_cloudlet.get(stg_vm);
		    		if(data > 0){
		    		//	System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(i).getCloudletId()+" on VM"+app.clist.get(i).getVmId()
		    		//			+" Sending "+data+" to CL "+clId+" on VM "+vmId);
		    			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.WAIT_SEND, data,  0, stg_vm[0], 150, vmId,clId));
		    		}else{
		    			data = Math.abs(data);
		    		//	System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(i).getCloudletId()+" on VM"+app.clist.get(i).getVmId()
		    		//			+" receiving "+data+" from CL "+clId+" on VM "+vmId);
		    			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.WAIT_RECV, data,  0, stg_vm[0], 150, vmId,clId));
			    		}
		    		app.clist.get(i).numStage++;
		    		}
		    }
		    }
		 
		/**for (int i = 0; i < app.clist.size() ; i++) { 
			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  500, app.clist.get(i).numStage, 150, 0, 0));
		}**/
	 }

    public static int findSuitableSwitchForVmList(NetworkDatacenter DC, List<NetworkVm> vmList){ 
    	
    	List<NetworkHost> hostList = DC.getHostList(); 
    	List<NetworkHost> availableHostList = new ArrayList<NetworkHost>(); 
    	
    	int nu_req_VM = vmList.size();
    	
    	//calculate minimum required resources for a VM
    	double min_mips = 0;
    	long min_storage = 0; 
    	int min_ram = 0;
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
    	for(NetworkHost host : hostList){
    		if(host.getMaxAvailableMips() >= min_mips &&
    				host.getStorage() >= min_storage &&
    				host.getRamProvisioner().getAvailableRam() >= min_ram &&
    				host.getNumberOfPes() >= min_pesNumber){
    			availableHostList.add(host);
    			
    			esw =  (EdgeSwitch) host.sw; 
    			if(esw.has_EG_value == 1){
    				esw.EG_NS = 0;
    				esw.EG_APE = 0;
    				esw.EG_UHS = 0;
    				esw.EG_ARA = 0;
    				esw.EG_ALK = 0;
    				esw.EG_SCORE = 0;
    				esw.EG_value = null;
    				esw.has_EG_value = 0;
    				esw.tot_free_pe = 0;
    				esw.tot_pe = 0;
    			}
    			if(esw.uplinkswitches.get(0).has_EG_value == 1){
    				esw.uplinkswitches.get(0).has_EG_value = 0;
    				esw.uplinkswitches.get(0).EG_NS = 0;
    				esw.uplinkswitches.get(0).EG_SCORE = 0;
    				esw.uplinkswitches.get(0).EG_value = null;
    			}
    				
    			esw.EG_NS ++;
    			if(host.getVmList().size() > 0)
    				esw.EG_UHS ++;
    			esw.tot_free_pe = esw.tot_free_pe + host.getCurrentFreeCpuNo();
    			esw.tot_pe = esw.tot_pe + host.getNumberOfPes(); 
				esw.EG_ARA = esw.EG_ARA + host.getRamProvisioner().getAvailableRam();
				if(host.packetTosendGlobal.size() == 0)
					esw.EG_ALK = esw.EG_ALK + (host.bandwidth);
				else
					esw.EG_ALK = esw.EG_ALK + (host.bandwidth/host.packetTosendGlobal.size());
    			
    		}
    	}
    	System.out.println("The number of available hosts is :"+availableHostList.size() );
    	
    	// calculate the eligibility of each edgeSwitch
    	double max_eg = 0.00; 
    	AggregateSwitch max_agg_sw = null;
    	Map<Integer,Switch> edgSwitchList = DC.getEdgeSwitch();
    	for (Entry<Integer, Switch> es : edgSwitchList.entrySet()) {
    		EdgeSwitch edgSW;
			edgSW = (EdgeSwitch)es.getValue();
			edgSW.has_EG_value = 1;
			edgSW.EG_APE = (edgSW.tot_free_pe ) / edgSW.tot_pe;   
			edgSW.EG_UHS = edgSW.EG_UHS / edgSW.EG_NS;
			edgSW.EG_ARA = edgSW.EG_ARA / edgSW.EG_NS;
			edgSW.EG_ALK = 1-((edgSW.EG_ALK / edgSW.EG_NS)/edgSW.downlinkbandwidth);
			
			edgSW.uplinkswitches.get(0).EG_NS = edgSW.uplinkswitches.get(0).EG_NS + edgSW.EG_NS;
			
			 //********* Eligibility Calculation  **********//
			
				double temp = 0;
				double rule = 0;
				// ..... fuzzy operations ......
				// Rule 1: If APE is overloaded or ARA is unqualified or ALK is high THEN EG is ineligible
				rule = APE_membership(edgSW,"OVRLD"); 
				temp = ARA_membership(edgSW,(total_req_ram/nu_req_VM),"UNQLFY");  
				rule = Math.max(rule, temp);
				temp = ALK_membership(edgSW, "HIGH"); 
				rule = Math.max(rule, temp);

				//System.out.println("RULE 1 : "+rule + ">"+edgSW.EG_SCORE);
				if(rule > edgSW.EG_SCORE){
					edgSW.EG_SCORE = rule;
					edgSW.EG_value = "INELGB";
				}
				// RUEL 2: IF APE is normal and ARA is normal and ALK is not high THEN EG is eligible.
				rule = APE_membership(edgSW,"NRML"); 
				temp = ARA_membership(edgSW,(total_req_ram/nu_req_VM),"NRML"); 
				rule = Math.min(rule, temp); 
				temp = 1- ALK_membership(edgSW, "HIGH"); 
				rule = Math.min(rule, temp); 
				//System.out.println("RULE 2 : "+rule + ">="+edgSW.EG_SCORE);
				if(rule >= edgSW.EG_SCORE){
					edgSW.EG_SCORE = rule;
					edgSW.EG_value = "ELGB";
				}
				// RUEL 3: IF APE is normal ARA is normal and UHS is not unutilized and and ALK is low THEN EG is highly eligible
				rule = APE_membership(edgSW,"NRML");
				temp = ARA_membership(edgSW,(total_req_ram/nu_req_VM),"NRML");
				rule = Math.min(rule, temp);
				temp = 1- UHS_membership(edgSW, "IDLE");
				rule = Math.min(rule, temp);
				temp = ALK_membership(edgSW, "LOW");
				rule = Math.min(rule, temp);
				//System.out.println("RULE 3 : 1.6 * "+rule + ">= "+edgSW.EG_SCORE);
				if((1.6*rule) >= edgSW.EG_SCORE){
					System.out.println(" for switch "+edgSW.getName()+" HELGB score :"+ rule);
					edgSW.EG_SCORE = rule;
					edgSW.EG_value = "HELGB";
				}
				 
			
			//**********************************************//
			//System.out.println("For Switch ID :" + edgSW.getName() +" score :"+edgSW.EG_SCORE + "for "+edgSW.EG_value);
			//System.out.println("NS="+edgSW.EG_NS+", APE="+edgSW.EG_APE+", ARA="+edgSW.EG_ARA+", ALK="+edgSW.EG_ALK);
			//	System.out.println(" for switch "+edgSW.uplinkswitches.get(0).getName()+" from "
			//			+edgSW.uplinkswitches.get(0).EG_value+" and "+edgSW.uplinkswitches.get(0).EG_SCORE);
			if(edgSW.EG_value == "HELGB"){
				if(edgSW.uplinkswitches.get(0).EG_value=="HELGB"){
					if (edgSW.EG_SCORE > edgSW.uplinkswitches.get(0).EG_SCORE)
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
						
				}
				else{
					edgSW.uplinkswitches.get(0).EG_value = "HELGB";
					edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE;
					edgSW.uplinkswitches.get(0).has_EG_value = 1;
				}
			} 
			if(edgSW.EG_value == "ELGB"){
				if(edgSW.uplinkswitches.get(0).EG_value == "ELGB"){
					if (edgSW.EG_SCORE > edgSW.uplinkswitches.get(0).EG_SCORE)
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
						
				}
				else{
					if(edgSW.uplinkswitches.get(0).EG_value != "HELGB"){
						edgSW.uplinkswitches.get(0).EG_value = "ELGB";
						edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE;
						edgSW.uplinkswitches.get(0).has_EG_value = 1;
					}
				}
			}
			if(edgSW.EG_value == "INELGB"){
				if(edgSW.uplinkswitches.get(0).EG_value == null){
					edgSW.uplinkswitches.get(0).EG_value = "INELGB";
					edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE;
					edgSW.uplinkswitches.get(0).has_EG_value = 1;
						
				}
				else{
					if(edgSW.uplinkswitches.get(0).EG_value == "INELGB"){
						if (edgSW.EG_SCORE < edgSW.uplinkswitches.get(0).EG_SCORE)
							edgSW.uplinkswitches.get(0).EG_SCORE = edgSW.EG_SCORE; 
					}
				}
			}
		//	System.out.println(" To "
		//			+edgSW.uplinkswitches.get(0).EG_value+" and "+edgSW.uplinkswitches.get(0).EG_SCORE);
    	} 
    	// find suitable aggregation switch
    	max_eg = 0.00;
    	double max_heg = 0.00;
    	AggregateSwitch heglb_agg_sw = null;
    	double min_eg = 1.00;
    	AggregateSwitch ineglb_agg_sw = null;
    	Map<Integer,Switch> aggSwitchList = DC.getAggSwitch();
    	for (Entry<Integer, Switch> es : aggSwitchList.entrySet()) {
			AggregateSwitch aggSW;
			aggSW = (AggregateSwitch) es.getValue();
			if(aggSW.EG_NS >= nu_req_VM){
				if (aggSW.EG_value =="HELGB" && aggSW.EG_SCORE > max_heg){
					max_heg = aggSW.EG_SCORE;
					heglb_agg_sw = aggSW;
				}
				if (aggSW.EG_value !="INELGB" && aggSW.EG_SCORE > max_eg){
					max_eg = aggSW.EG_SCORE;
					max_agg_sw = aggSW;
				}
				if (aggSW.EG_value =="INELGB" && aggSW.EG_SCORE <= min_eg){
					min_eg = aggSW.EG_SCORE;
					ineglb_agg_sw = aggSW;
				}
			}
    	}
    	if(max_heg > 0)
    		max_agg_sw = heglb_agg_sw;
    	if(max_heg == 0 && max_eg == 0)
    		max_agg_sw = ineglb_agg_sw;
		System.out.println("selected Switch name :" + max_agg_sw.getName() +"with class " + max_agg_sw.EG_value +" and score "+max_agg_sw.EG_SCORE);
    	return max_agg_sw.getId();
    } 

    private static double APE_membership(EdgeSwitch edgSW, String value){
    	double APE_mf = 0; 
    	//// 
    	if(value == "OVRLD"){
    		if(edgSW.EG_APE <= 0.25)
    			APE_mf = 1;
    		else{
    			if(edgSW.EG_APE > 0.35)
    				APE_mf = 0;
    			else
    				APE_mf = (-10 * edgSW.EG_APE) + 3.5;
    		}
    	}
    	////
    	if(value == "NRML"){
    		if(edgSW.EG_APE > 0.35)
    			APE_mf = 1;
    		else
    			if(edgSW.EG_APE <= 0.25)
    				APE_mf = 0;
    			else
    				APE_mf = (10 * edgSW.EG_APE) - 2.5;
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

    private static double ARA_membership(EdgeSwitch edgSW, double avg_ram, String value){
    	double ARA_mf = 0;
    	if(value == "UNQLFY"){
    		if(edgSW.EG_ARA <= avg_ram)
        		ARA_mf = 1;
    		else
    			if(edgSW.EG_ARA  > (1.5 * avg_ram))
    				ARA_mf = 0;
    			else
    			 ARA_mf = (-2*(edgSW.EG_ARA/avg_ram))+3 ; 
    	}
    	///
    	if(value == "NRML"){
    		if(edgSW.EG_ARA > (1.5 * avg_ram))
        		ARA_mf = 1;
    		else
    			if(edgSW.EG_ARA <= avg_ram)
            		ARA_mf = 0;
    			else
    				ARA_mf = (2*(edgSW.EG_ARA/avg_ram))-2;
    	}
    	return ARA_mf;
    }//................ END of ARA membership .................//

    private static double ALK_membership(EdgeSwitch edgSW , String value){
    	double ALK_mf = 0;
    	////
    	if(value == "LOW"){
    		if(edgSW.EG_ALK <= 0.2)
    			ALK_mf = 1;
    		else{
    			if(edgSW.EG_ALK > 0.3)
    				ALK_mf = 0;
    			else
    				ALK_mf = ((-10 * edgSW.EG_ALK) + 3);
    		}
    	}
    	////
    	if(value == "MID"){
    		if(edgSW.EG_ALK >= 0.3 & edgSW.EG_ALK <= 0.6)
    			ALK_mf = 1;
    		else
    			if(edgSW.EG_ALK < 0.2 || edgSW.EG_ALK > 0.7)
    				ALK_mf = 0;
    			else{
    				if(edgSW.EG_ALK < 0.3)
    					ALK_mf = ((10 * edgSW.EG_ALK) - 2);

    				if(edgSW.EG_ALK > 0.6)
    					ALK_mf = (-10 * edgSW.EG_ALK) + 7;
    			}
    	}
    	////
    	if(value == "HIGH"){
    		if(edgSW.EG_ALK > 0.7)
    			ALK_mf = 1;
    		else
    			if(edgSW.EG_ALK <= 0.6)
    				ALK_mf = 0;
    			else
    				ALK_mf = (10 * edgSW.EG_ALK) - 6;
    	}
    	return ALK_mf;
    }//................ END of ALK membership .................//
  

    public static double computeDelay_2(NetworkHost srcHost,NetworkHost destHost, double dataTransfer ){
    	double delay = 0.0; 		
		String upsw1 = srcHost.sw.getName();
		String upsw2 = destHost.sw.getName();
		if(upsw1.equalsIgnoreCase(upsw2)) // Hosts are in the same rack
		{
			//System.out.println("Hosts are in the same rack."+upsw1+","+upsw2);
			System.out.println((srcHost.usedbandwidthRcv + srcHost.usedbandwidthSend) + "," + (destHost.usedbandwidthRcv + destHost.usedbandwidthSend));
			delay = dataTransfer * (
			 (8/(srcHost.bandwidth * ( 1- ((srcHost.usedbandwidthRcv + srcHost.usedbandwidthSend)/100))))
			+ srcHost.sw.switching_delay
			+ (8/(destHost.bandwidth * ( 1- ((destHost.usedbandwidthRcv + destHost.usedbandwidthSend)/100))))
			);
			return delay;
		}else{
			String upsw11 = null;
			String upsw22 = null;
			for(Switch ns1  : srcHost.sw.uplinkswitches){ 
				 upsw11 = ns1.getName();
				for(Switch ns2  : destHost.sw.uplinkswitches){ 
					 upsw22 = ns2.getName();
					if(upsw11.equalsIgnoreCase(upsw22)) // Hosts are not in the same rack but same aggSwitch
					{

						//DCMngUtility.resultFile.println("----Hosts are under the same aggregation switch");
						delay = dataTransfer * (
								(8/(srcHost.bandwidth * ( 1- ((srcHost.usedbandwidthRcv + srcHost.usedbandwidthSend)/100))))
								+ srcHost.sw.switching_delay
								+ (8/(srcHost.sw.uplinkbandwidth*(1-((srcHost.sw.uplinkSwRcv + srcHost.sw.uplinkSwSend)/100))))
								+ ns2.switching_delay
								+ (8/(destHost.sw.uplinkbandwidth*(1-((destHost.sw.uplinkSwRcv + destHost.sw.uplinkSwSend)/100))))
								+ destHost.sw.switching_delay
								+ (8/(destHost.bandwidth * ( 1- ((destHost.usedbandwidthRcv + destHost.usedbandwidthSend)/100))))
								);
						NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
						return delay;
					}
				}
			}
			// Hosts are neither in the same rack nore in the same aggSwitch. 
			// find aggSwitch with minimum packets to transfer

			///DCMngUtility.resultFile.println("----Hosts are communicating using root switch");
			Switch coreSw =  srcHost.sw.uplinkswitches.get(0).uplinkswitches.get(0);
			
			delay = dataTransfer * (
					(8/(srcHost.bandwidth * ( 1- ((srcHost.usedbandwidthRcv + srcHost.usedbandwidthSend)/100))))// host to edge
					+ srcHost.sw.switching_delay
					+ (8/(srcHost.sw.uplinkbandwidth*(1-((srcHost.sw.uplinkSwRcv + srcHost.sw.uplinkSwSend)/100))))// edge to agg
					+ srcHost.sw.uplinkswitches.get(0).switching_delay
					+ (8/(coreSw.downlinkbandwidth/((coreSw.pktlist.size()==0)?1:(coreSw.pktlist.size()/2)))) // agg to core
					+ coreSw.switching_delay
					+ (8/(coreSw.downlinkbandwidth/((coreSw.pktlist.size()==0)?1:(coreSw.pktlist.size()/2)))) // core to agg
					+ destHost.sw.uplinkswitches.get(0).switching_delay
					+ (8/(destHost.sw.uplinkbandwidth*(1-((destHost.sw.uplinkSwRcv + destHost.sw.uplinkSwSend)/100)))) // agg to edge
					+ destHost.sw.switching_delay
					+ (8/(destHost.bandwidth * ( 1- ((destHost.usedbandwidthRcv + destHost.usedbandwidthSend)/100)))) // edge to host
					);
		}
		NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
		return delay;
	}
    
    public static double computeDelay(NetworkHost srcHost,NetworkHost destHost, double dataTransfer ){
		double delay = 0.0; 		
		String upsw1 = srcHost.sw.getName();
		String upsw2 = destHost.sw.getName();
		if(upsw1.equalsIgnoreCase(upsw2)) // Hosts are in the same rack
		{
			//System.out.println("Hosts are in the same rack."+upsw1+","+upsw2);
			delay = dataTransfer * (
			 (8/(srcHost.sw.downlinkbandwidth/((srcHost.packetTosendGlobal.size()==0)?1:srcHost.packetTosendGlobal.size())))
			+ srcHost.sw.switching_delay
			+ (8/(destHost.sw.downlinkbandwidth/((destHost.packetTosendGlobal.size()==0)?1:destHost.packetTosendGlobal.size())))
			);
			return delay;
		}else{
			String upsw11 = null;
			String upsw22 = null;
			for(Switch ns1  : srcHost.sw.uplinkswitches){ 
				 upsw11 = ns1.getName();
				for(Switch ns2  : destHost.sw.uplinkswitches){ 
					 upsw22 = ns2.getName();
					if(upsw11.equalsIgnoreCase(upsw22)) // Hosts are not in the same rack but same aggSwitch
					{
						//System.out.println("Hosts are not in the same rack but same aggSwitch."+upsw1+","+upsw2);
						//System.out.println("agg switch Name :"+ upsw11);
						delay = dataTransfer * (
								 (8/(srcHost.sw.downlinkbandwidth/((srcHost.packetTosendGlobal.size()==0)?1:srcHost.packetTosendGlobal.size())))
								+ srcHost.sw.switching_delay
								+ (8/(ns2.downlinkbandwidth/((ns2.pktlist.size()==0)?1:(ns2.pktlist.size()/2))))
								+ ns2.switching_delay
								+ (8/(ns2.downlinkbandwidth/((ns2.pktlist.size()==0)?1:(ns2.pktlist.size()/2))))
								+ destHost.sw.switching_delay
								+ (8/(destHost.sw.downlinkbandwidth/((destHost.packetTosendGlobal.size()==0)?1:destHost.packetTosendGlobal.size())))
								);

						NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
						return delay;
					}
				}
			}
			//System.out.println("Hosts are neither in the same rack nore in the same aggSwitch.");
			// find aggSwitch with minimum packets to transfer
			int nupsw11 = 0;
			int nupsw22 = 0;
			for(Switch ns1  : srcHost.sw.uplinkswitches){
				if(srcHost.sw.uplinkswitches.get(nupsw11).pktlist.size() > ns1.pktlist.size())
			       nupsw11 = srcHost.sw.uplinkswitches.indexOf(ns1);
			}
			for(Switch ns2  : destHost.sw.uplinkswitches){
				if(srcHost.sw.uplinkswitches.get(nupsw22).pktlist.size() > ns2.pktlist.size())
					nupsw22 = destHost.sw.uplinkswitches.indexOf(ns2);
			}
			Switch coreSw = null;
			int upsw111 = 0;
			for(Switch ns11  : srcHost.sw.uplinkswitches.get(nupsw11).uplinkswitches){
				if(srcHost.sw.uplinkswitches.get(nupsw11).uplinkswitches.get(upsw111).pktlist.size() > ns11.pktlist.size())
					upsw111 = srcHost.sw.uplinkswitches.get(nupsw11).uplinkswitches.indexOf(ns11);
			}
			coreSw = srcHost.sw.uplinkswitches.get(nupsw11).uplinkswitches.get(upsw111);
			delay = dataTransfer * (
					 (8/(srcHost.sw.downlinkbandwidth/((srcHost.packetTosendGlobal.size()==0)?1:srcHost.packetTosendGlobal.size())))
					+ srcHost.sw.switching_delay
					+ (8/(srcHost.sw.uplinkbandwidth/((srcHost.sw.uplinkswitches.get(nupsw11).pktlist.size()==0)?1:(srcHost.sw.uplinkswitches.get(nupsw11).pktlist.size()/2))))
					+ srcHost.sw.uplinkswitches.get(nupsw11).switching_delay
					+ (8/(coreSw.downlinkbandwidth/((coreSw.pktlist.size()==0)?1:(coreSw.pktlist.size()/2))))
					+ coreSw.switching_delay
					+ (8/(coreSw.downlinkbandwidth/((coreSw.pktlist.size()==0)?1:(coreSw.pktlist.size()/2))))
					+ destHost.sw.uplinkswitches.get(nupsw22).switching_delay
					+ (8/(destHost.sw.uplinkbandwidth/((destHost.sw.uplinkswitches.get(nupsw22).pktlist.size()==0)?1:(destHost.sw.uplinkswitches.get(nupsw22).pktlist.size()/2))))
					+ destHost.sw.switching_delay
					+ (8/(destHost.sw.downlinkbandwidth/((destHost.packetTosendGlobal.size()==0)?1:destHost.packetTosendGlobal.size())))
					);
		}
		NetworkConstants.interRackDataTransfer = NetworkConstants.interRackDataTransfer + dataTransfer;
		return delay;
	}
}
