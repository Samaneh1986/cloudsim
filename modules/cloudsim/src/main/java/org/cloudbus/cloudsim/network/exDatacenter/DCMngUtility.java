package org.cloudbus.cloudsim.network.exDatacenter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.UniformDistr;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import com.quantego.clp.CLP.STATUS;



public class DCMngUtility {
	public static PrintWriter resultFile;
	//public static PrintWriter performanceFile;
	/***************************************/
	public static DCPerformance dcPerformance;
	public static boolean HasStorageArea = false;
	
	
	public static Map<Integer, ArrayList<Integer>> appClistIndex;
	
	public static void defineStagesOfTable(AppCloudlet app){ 
		//app.SendDataTo contains the id of cloudlet in data set not real id
		Map<Integer,Map<int[],Double>> dataTable = app.SendDataTo;
		//clistIndex: list of cloudlet id which is used in stage table (different from real cloudlet id)
		ArrayList<Integer> clistIndex = appClistIndex.get(app.appID);
		//System.out.println(dataTable.keySet());
		for (int i : dataTable.keySet()) {//for all cloudlets in app
			Map<int[], Double> cur_cloudlet = dataTable.get(i); 
			int tot_stage = 0;
			int clIndex = -1;
    		for(int ix=0 ; ix<clistIndex.size(); ix++){
    			//System.out.println(" cl "+i+" is equal to ? "+clistIndex.get(ix));
    			if(clistIndex.get(ix)==i){
    			//	System.out.println("matched!!!");
    				clIndex = ix;
    			}
    		}
    		if(dataTable.get(i)!=null)
			   tot_stage = dataTable.get(i).keySet().size();//all send/receive stages of current cloudlet
			int base = app.clist.get(clIndex).stages.size();
			//System.out.println(" cl "+clIndex+" has total comm. stage "+tot_stage);
			for (int s = base ; s < tot_stage+base ; s++){ // stage 0 is an execution stage and 1,2 is input read
			    for (int[] stg_vm : dataTable.get(i).keySet()){  // an extra 'for' to add the stages in an ordered way
			    	if(stg_vm[0]== s){
			    		if(stg_vm[1] > -1){
				    		int peerClIndex = -1;
				    		for(int ix=0 ; ix<clistIndex.size(); ix++){
				    		//	System.out.println("peer cl "+clistIndex.get(ix)+" is equal to ? "+stg_vm[1]);
			    				if(clistIndex.get(ix)==stg_vm[1]){ // receiver VM
				    			//	System.out.println("matched!!");
				    				peerClIndex = ix;
				    			}
				    		}
				    		int vmId = app.clist.get(peerClIndex).getVmId();
				    		int clId = app.clist.get(peerClIndex).getCloudletId();
				    		double data = cur_cloudlet.get(stg_vm);
				    		if(data > 0){
				    		//	System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(clIndex).getCloudletId()+" on VM"+app.clist.get(clIndex).getVmId()
				    		//			+" Sending "+data+" to CL "+clId+" on VM "+vmId);
				    			app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.WAIT_SEND, data,  0, stg_vm[0], 150, vmId,clId));
				    		}else{
				    			data = Math.abs(data);
				    		//	System.out.println("Ad  Stage "+stg_vm[0]+" for CL "+app.clist.get(clIndex).getCloudletId()+" on VM"+app.clist.get(clIndex).getVmId()
				    		//			+" receiving "+data+" from CL "+clId+" on VM "+vmId);
				    			app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.WAIT_RECV, data,  0, stg_vm[0], 150, vmId,clId));
					    		}
			    		}
			    		else{
			    			double data = cur_cloudlet.get(stg_vm);
			    			app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  data, 0, 0, 0,0));
				    	}
			    	}
			    }
		    }
			if(HasStorageArea && app.clist.get(clIndex).getCloudletOutputSize()>0){
				app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.OUTPUT_STORAGE_REQ, app.clist.get(clIndex).getCloudletOutputSize(),  0, app.clist.get(clIndex).numStage, 0, 0,0));
				app.clist.get(clIndex).numStage++;
				app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.OUTPUT_WRITE, app.clist.get(clIndex).getCloudletOutputSize(),  0, app.clist.get(clIndex).numStage, 0, 0,0));
				app.clist.get(clIndex).numStage++;
			}
			//app.clist.get(clIndex).stages.add(new TaskStage(NetworkConstants.FINISH, 0,  0, app.clist.get(clIndex).numStage, 0, 0,0));
			//app.clist.get(clIndex).numStage++;
    		
		}
		 
		/**for (int i = 0; i < app.clist.size() ; i++) { 
			app.clist.get(i).stages.add(new TaskStage(NetworkConstants.EXECUTION, 0,  500, app.clist.get(i).numStage, 150, 0, 0));
		}**/
		//assign VM id to data block for routing
		for(NetworkCloudlet cl:app.clist){
			for(NetStorageBlock blk:cl.inputData){
				blk.vmId = cl.getVmId();
				//if(cl.getCloudletId() == 2)
				//	System.out.println("block list cl 2 :"+blk.getBlockId()+" size "+blk.getData());
			}
		}
	 }


    private static double APE_membership(EdgeSwitch edgSW, String value){
    	double APE_mf = 0; 
    	//// 
    	/*if(value == "OVRLD"||value == "LOW"){
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
    	if(value == "NRML"||value == "HIGH"){
    		if(edgSW.EG_APE > 0.35)
    			APE_mf = 1;
    		else
    			if(edgSW.EG_APE <= 0.25)
    				APE_mf = 0;
    			else
    				APE_mf = (10 * edgSW.EG_APE) - 2.5;
    	}*/
    	if(value == "LOW"){
    		if(edgSW.EG_APE <= 0.3)
    			APE_mf = 1;
    		else{
    			if(edgSW.EG_APE > 0.7)
    				APE_mf = 0;
    			else
    				APE_mf = ((-2.5 * edgSW.EG_APE) + 1.75);
    		}
    	}
    	////
    	if(value == "HIGH"){
    		if(edgSW.EG_APE >= 0.7 )
    			APE_mf = 1;
    		else
    			if(edgSW.EG_APE < 0.3 )
    				APE_mf = 0;
    			else{
    				APE_mf = (2.5 * edgSW.EG_APE) - 0.75 ;
    			}
    	} 
    	return APE_mf;
    }//................ END of APE membership .................//
    private static double UHS_membership(EdgeSwitch edgSW, String value){
    	double UHS_mf = 0; 
    	////

		//System.out.println("UHS VLUE : "+edgSW.EG_UHS);
    	if(value == "IDLE"||value == "LOW"){
    		if(edgSW.EG_UHS <= 0.1)
    			UHS_mf = 1;
    		else{
    			if(edgSW.EG_UHS > 0.5)
    				UHS_mf = 0;
    			else
    				UHS_mf = ((-2.5 * edgSW.EG_UHS) + 1.25);
    		}
    	}
    	////
    	if(value == "NRML"||value == "HIGH"){
    		if(edgSW.EG_UHS >= 0.8 )
    			UHS_mf = 1;
    		else
        		UHS_mf = (1.25 * edgSW.EG_UHS) ;
    			//if(edgSW.EG_UHS < 0.1 )
    			//	UHS_mf = 0;
    			//else{
    				//UHS_mf = (1.25 * edgSW.EG_UHS)-0.125 ;
    				/*if(edgSW.EG_UHS < 0.25)
    					UHS_mf = (((20 * edgSW.EG_UHS) - 2)/3);

    				if(edgSW.EG_UHS > 0.4)
    					UHS_mf = (-10 * edgSW.EG_UHS) + 5;*/
    			//}
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
    	double balanced_prc = edgSW.EG_ALK ; //+ 0.3; // 0.3 of bandwidth getten by others
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
    
    private static double OutPut_membership(double score, String value){
    	//// 
    	double output = 0.0;
    	if(value == "LOW"){
    		if(score >= 1)
    			output = 0.3;
    		else{// y = -10 x + 4  => x= (y - 4) / -10
    			output = (score - 4) * (-0.1);
    		}
    	}
    	////
    	if(value == "HIGH"){
    		if(score >= 1)
    			output = 0.3;
    		else// y = 10 x - 6 => x = (y + 6) / 10
    			output = 1 - ((score + 6) * (0.1)) ;
    	}
    	////
    	if(value == "MID"){
    		if(score >= 1)
    			output = 0.2;
    		else// y = 10 x - 3 => x = (y + 3) / 10
    			output = 0.2 + (2 * (0.4 - ((score + 3) * 0.1)));
    	}
    	return output;
    }//................ END of OutPut membership .................//
    
    public static Switch findLeafSwitchForVmList(NetworkDatacenter DC, List<NetworkVm> vmList){ 
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
			
			//edgSW.uplinkswitches.get(0).EG_NS = edgSW.uplinkswitches.get(0).EG_NS + edgSW.EG_NS;
			//edgSW.uplinkswitches.get(0).EG_MAX_NS = Math.max(edgSW.uplinkswitches.get(0).EG_MAX_NS, edgSW.EG_NS);
			System.out.println(edgSW.getName()+" Fuzzy value : "+edgSW.EG_NS+","+edgSW.EG_APE+","+edgSW.EG_ARA+","+edgSW.EG_ALK+","+edgSW.EG_UHS);	
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
				System.out.println("RULE 1 : "+rule + ">"+edgSW.EG_SCORE);
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
					System.out.println("RULE 2 : "+rule + ">="+edgSW.EG_SCORE);
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
					System.out.println("RULE 3 : 1.6 * "+rule + ">= "+edgSW.EG_SCORE);
					if((1.4*rule) >= edgSW.EG_SCORE){
						//System.out.println(" for switch "+edgSW.getName()+" HELGB score :"+ rule);
						edgSW.EG_SCORE = rule;
						edgSW.EG_value = "HELGB";
					}
				
			
			//**********************************************//
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
    		return best_sw;
    	// find suitable leaf switch list
    	AggregateSwitch dummy_agg_sw = new AggregateSwitch("dummy", NetworkConstants.Agg_LEVEL, DC);
    	//dummy_agg_sw.switching_delay = .00245;
    	//dummy_agg_sw.downlinkbandwidth=40 * 1024;
    	//DC.Switchlist.put(dummy_agg_sw.getId(),dummy_agg_sw);
    	//
    	dummy_agg_sw.EG_NS = 0;
    	Map<Integer,Switch> leafSwitchList = DC.getEdgeSwitch();
    	while(dummy_agg_sw.EG_NS < nu_req_VM){
    		int min_diff = 99;
    		EdgeSwitch maxHostSW = null;
	    	for (Entry<Integer, Switch> leaf : leafSwitchList.entrySet()) {
				EdgeSwitch lfSW;
				lfSW = (EdgeSwitch) leaf.getValue();
				if( Math.abs(dummy_agg_sw.EG_NS + lfSW.EG_NS - nu_req_VM) <= min_diff && !dummy_agg_sw.downlinkswitches.contains(lfSW)){
					min_diff = Math.abs(dummy_agg_sw.EG_NS + lfSW.EG_NS - nu_req_VM);
					maxHostSW = lfSW;
				}
	    	}
	    	dummy_agg_sw.EG_NS = dummy_agg_sw.EG_NS + maxHostSW.EG_NS;
	    	dummy_agg_sw.downlinkswitches.add(maxHostSW);
	    	maxHostSW.uplinkswitches.add(dummy_agg_sw);
	    	System.out.println("leaf added : "+maxHostSW.getName()+" with available hosts "+maxHostSW.EG_NS);
    	}
   
    	best_sw = dummy_agg_sw;
    
    	
    	return best_sw;
    } 

    public static List<NetworkHost> findSuitableHostGroup(NetworkDatacenter DC, List<NetworkVm> vmList){ 
    	List<NetworkHost> selectedHostList = new ArrayList<NetworkHost>();
    	int num_req_VM = vmList.size();
    	
    	NetworkVm sampleVM = vmList.get(0);
    	for(int sww : DC.getEdgeSwitch().keySet()){
    		EdgeSwitch edgesww = (EdgeSwitch) DC.getEdgeSwitch().get(sww);
    		if(edgesww.has_EG_value == 1){
    			edgesww.EG_NS = 0;
    			edgesww.EG_APE = 0;
    			edgesww.EG_UHS = 0;
    			//edgesww.EG_ARA = 0;
    			edgesww.EG_ALK = 0;
    			edgesww.EG_SCORE = 0;
    			//edgesww.EG_value = null;
    			edgesww.has_EG_value = 0;
    			edgesww.tot_free_pe = 0;
    			edgesww.tot_pe = 0;
    			//edgesww.tot_ram = 0;
    		}
    		for(int hostId : edgesww.hostlist.keySet()){
    			NetworkHost host = edgesww.hostlist.get(hostId);
    			double byt_que_size = 0.0;
    			//compute the traffic on link between the edge switch and each host
				if(edgesww.bytesTohostSize!=null && edgesww.bytesTohostSize.get(host.getId())!= null )
					byt_que_size = edgesww.bytesTohostSize.get(host.getId());
				byt_que_size = byt_que_size + host.bytesTosendGlobalSize;
				edgesww.EG_ALK = edgesww.EG_ALK + (byt_que_size*8);
				//compute host utilization
				if(host.getVmList().size() > 0)
					edgesww.EG_UHS ++;
				//compute free cpu number
				edgesww.tot_free_pe = edgesww.tot_free_pe + host.getNumberOfFreePes();
    			edgesww.tot_pe = edgesww.tot_pe + host.getNumberOfPes(); 
				//compute the number of hosts with enough resources
    			if(host.isSuitableForVm(sampleVM)){
    				edgesww.EG_NS ++;
    			}
    		}
    		edgesww.has_EG_value = 1;
    		edgesww.EG_APE = edgesww.tot_free_pe / edgesww.tot_pe; //cpu free prc
    		edgesww.EG_ALK = edgesww.EG_ALK / edgesww.hostlist.size();//link saturation prc
    		edgesww.EG_ALK = edgesww.EG_ALK  / edgesww.downlinkbandwidth;
    		//System.out.println("Rack ID:"+edgesww.getId());
    		edgesww.EG_UHS = edgesww.EG_UHS / edgesww.hostlist.size();//host utilization prc
    	
	    	//applying fuzzy operations to score each rack (edge switch) 
	    	//APE & UHS & ALK & Result
	    	double temp = 0;
			double opr1,opr2,opr3,opr4,opr5,opr6,opr7,opr8 = 0;
			//opr 1 : high & high & high => mid
			opr1 = APE_membership(edgesww,"HIGH");
			temp = UHS_membership(edgesww, "HIGH");
			opr1 = Math.min(opr1, temp);
			temp = ALK_membership(edgesww, "HIGH");
			opr1 = Math.min(opr1, temp);
			//opr 2 : high & high & low => high
			opr2 = APE_membership(edgesww,"HIGH");
			temp = UHS_membership(edgesww, "HIGH");
			opr2 = Math.min(opr2, temp);
			temp = ALK_membership(edgesww, "LOW");
			opr2 = Math.min(opr2, temp);
			//opr 3 : high & low & high => mid
			opr3 = APE_membership(edgesww,"HIGH");
			temp = UHS_membership(edgesww, "LOW");
			opr3 = Math.min(opr3, temp);
			temp = ALK_membership(edgesww, "HIGH");
			opr3 = Math.min(opr3, temp);
			//opr 4 : high & low & low => mid
			opr4 = APE_membership(edgesww,"HIGH");
			temp = UHS_membership(edgesww, "LOW");
			opr4 = Math.min(opr4, temp);
			temp = ALK_membership(edgesww, "LOW");
			opr4 = Math.min(opr4, temp);
			//opr 5 : low & high & high => low
			opr5 = APE_membership(edgesww,"LOW");
			temp = UHS_membership(edgesww, "HIGH");
			opr5 = Math.min(opr5, temp);
			temp = ALK_membership(edgesww, "HIGH");
			opr5 = Math.min(opr5, temp);
			//opr 6 : low & high & low => low
			opr6 = APE_membership(edgesww,"LOW");
			temp = UHS_membership(edgesww, "HIGH");
			opr6 = Math.min(opr6, temp);
			temp = ALK_membership(edgesww, "LOW");
			opr6 = Math.min(opr6, temp);
			//opr 7 : low & low & high => low
			opr7 = APE_membership(edgesww,"LOW");
			temp = UHS_membership(edgesww, "LOW");
			opr7 = Math.min(opr7, temp);
			temp = ALK_membership(edgesww, "HIGH");
			opr7 = Math.min(opr7, temp);
			//opr 8 : low & low & low => low
			opr8 = APE_membership(edgesww,"LOW");
			temp = UHS_membership(edgesww, "LOW");
			opr8 = Math.min(opr8, temp);
			temp = ALK_membership(edgesww, "LOW");
			opr8 = Math.min(opr8, temp);
			
			double low = Math.max(opr1, opr5);
			low = Math.max(low, opr6);
			low = Math.max(low, opr7);
			low = Math.max(low, opr8);
			
			double mid = Math.max(opr3,opr4);
			
			double high = opr2; 

			//defuzzification using centroid
			double upEdge1 = OutPut_membership(low,"LOW");
			double area1Mean = 0.2;
			double upEdge2 = OutPut_membership(mid,"MID");
			double area2Mean = 0.5;
			double upEdge3 = OutPut_membership(high,"HIGH");
			double area3Mean = 0.8;
			double cos = ((0.5 * low * (upEdge1 + 0.4))*area1Mean) + ((0.5 *mid* (upEdge2 + 0.4))*area2Mean) + ((0.5 *high* (upEdge3 + 0.4))*area3Mean) ;
			cos = cos /((0.5 * low * (upEdge1 + 0.4))+(0.5 * mid * (upEdge2 + 0.4))+(0.5 * high * (upEdge3 + 0.4)));
			edgesww.EG_SCORE = cos;
			System.out.println("Rank Info R_"+edgesww.getId()+", Rank:"+cos+", low:"+low+", mid:"+mid+", high:"+high+",APE:"+edgesww.EG_APE+",UHS:"+edgesww.EG_UHS+",ALK:"+edgesww.EG_ALK);
    	}
    	
    	//select racks basd on ILP
    	int RackNumber = DC.getEdgeSwitch().size();
    	int index = 0;
    	double[] ZoneRank = new double [DC.total_zone];
    	for(int z=0; z<DC.total_zone ; z++)
    		ZoneRank[z] = 0.00;
    	int[] RackIDs = new int [RackNumber];
    	int[] RackZone = new int [RackNumber];
    	int[] RackHosts = new int [RackNumber]; 
    	int[] RackVMs = new int [RackNumber]; 
    	int[] SelectableRacks = new int [RackNumber]; 
    	double[] RackRank = new double [RackNumber];
    	double[] RackFreeBW = new double [RackNumber];
    	double[] RackExcpBW = new double [RackNumber]; 
    	for(Switch sw : DC.getEdgeSwitch().values()){
    		RackIDs[index]=sw.getId();
    		RackHosts[index]=sw.EG_NS; 
    		RackRank[index] = sw.EG_SCORE;
    		//RackFreeBW[index]=upLinkFreeBW(sw);
    		RackFreeBW[index]=upLinkUsedBWPRC(sw);
    		RackZone[index] = sw.zone_number;
    		ZoneRank[sw.zone_number] = (ZoneRank[sw.zone_number] + sw.EG_SCORE);
    		
    		for(NetworkHost hs: sw.hostlist.values())
    			RackVMs[index]= RackVMs[index] + hs.getVmList().size();
    		
    		double upTotalLinkBW = 0;
    		for(Switch upsw : sw.uplinkswitches)
    			upTotalLinkBW = upTotalLinkBW + upsw.downlinkbandwidth;
    		RackExcpBW[index]=(upTotalLinkBW/(RackVMs[index]+1))*0.8;
    		index++;
    	}
    	
    	//specify the selectable racks (rack in the zone)
    	
    	int best_zone_index=0;
    	double best_zone_racnk=0;
    	for(int z=0; z<DC.total_zone ; z++){
    		if(ZoneRank[z] > best_zone_racnk){
    			best_zone_racnk = ZoneRank[z];
    			best_zone_index = z;
    		}
    	}
    	//System.out.println("selected zone :"+best_zone_index);
    	for(int i = 0; i< index; i++){
    		if(RackZone[i] == best_zone_index){
    			SelectableRacks[i] = 1;
    			System.out.println("zone contains switchs :"+RackIDs[i]);
    		}
    		else
    			SelectableRacks[i] = 0;
    	}
    	
		CLP clp = new CLP();
		CLPVariable[] S = new CLPVariable[RackNumber]; 
		//for each rack consider a variable S[i] which could be between 0 and 1
		//1 means the rack is selected
		for(int i=0 ; i<RackNumber ; i++){
			S[i] = clp.addVariable().obj((1-RackRank[i]));
			S[i].name("R_"+RackIDs[i]);
			clp.setVariableBounds(S[i], 0,SelectableRacks[i]);
		}
		//expr1 : selected hosts No >= requested VMs No
		CLPExpression expr1 = clp.createExpression();
		for(int i=0 ; i<RackNumber ; i++){
			expr1.add(RackHosts[i],S[i]); 
		}
		expr1.geq(num_req_VM);
		//expr1.5 : selected hosts No <= requested VMs No 
		//CLPExpression expr15 = clp.createExpression();
		//for(int i=0 ; i<RackNumber ; i++){
		//	expr15.add(RackHosts[i],S[i]); 
		//}
		//double frac = Math.ceil(maxRackHost/num_req_VM);
		//expr15.leq(1.1*num_req_VM);
		
		// select only the selectable racks 
		//CLPExpression expr2 = clp.createExpression();
		//for(int i=0 ; i<RackNumber ; i++){
			//clp.createExpression().add(S[i]).leq(SelectableRacks[i]);
			//clp.createExpression().add(S[i]).geq(0);
		//} 
		//expr3 : traffic distance + traffic distance <= 2 * threshold T (0.7) 
		/*for(int i=0 ; i<RackNumber ; i++){ 
			double multpl = (RackExcpBW[i]*RackHosts[i])/(RackVMs[i]+1);
			double added = (RackExcpBW[i]*RackVMs[i])/(RackVMs[i]+1);
			clp.createExpression().add(multpl,S[i]).add(added).leq(RackFreeBW[i]); 
		}*/
		//epr3 : traffic < threshold (0.7)
		for(int i=0 ; i<RackNumber ; i++){ 
			clp.createExpression().add((0.7 - RackFreeBW[i] ),S[i]).geq(0); 
		}
		
		STATUS result = clp.minimize();
		//System.out.println("LP STATUS:"+result.name());
		//clp.printModel();
		for(int i=0 ; i<RackNumber ; i++){
			//System.out.println("LP selected rack: "+RackIDs[i]+ ", rate "+S[i].getSolution());
			int swId = RackIDs[i];
			EdgeSwitch selectedSW = (EdgeSwitch) DC.getEdgeSwitch().get(swId);
			if(S[i].getSolution() > 0){
				selectedHostList.addAll(selectedSW.hostlist.values());
			}
			//	System.out.println("Rack "+selectedSW.getName()+", value:"+
			//	S[i].getSolution()+", rank:"+RackRank[i]+", hosts:"+RackHosts[i]);
			
		}
		if(selectedHostList.size() == 0){
			System.out.println("ERROR: No Host Available!!!");
		}
    	return selectedHostList;
    }
    
    private static double upLinkFreeBW(Switch sw){
		double actualFreeBW = 0.0;
		double traffic = 0;
		double totBW = 0;
		for(Switch upsw  : sw.uplinkswitches){ 
			if(sw.bytesToUpSwitchSize == null || sw.bytesToUpSwitchSize.size() == 0 || sw.bytesToUpSwitchSize.get(upsw.getId())==null){
				traffic = traffic + 0;
			}
			else{
				traffic = traffic + sw.bytesToUpSwitchSize.get(upsw.getId()); 
			}
			if(upsw.bytesToDownSwitchSize == null || upsw.bytesToDownSwitchSize.size() == 0 || upsw.bytesToDownSwitchSize.get(sw.getId()) == null){
				traffic = traffic + 0; 
			}
			else{
				 traffic = traffic + upsw.bytesToDownSwitchSize.get(sw.getId());
			}
			totBW = totBW + upsw.downlinkbandwidth;
		}
		actualFreeBW = totBW - (traffic * 8)  ;
		if(actualFreeBW < 0)
			actualFreeBW = 0;
		
		return actualFreeBW;
    }
    private static double upLinkUsedBWPRC(Switch sw){
		double usedBW = 0.0;
		double traffic = 0;
		double totBW = 0;
		for(Switch upsw  : sw.uplinkswitches){ 
			if(sw.bytesToUpSwitchSize == null || sw.bytesToUpSwitchSize.size() == 0 || sw.bytesToUpSwitchSize.get(upsw.getId())==null){
				traffic = traffic + 0;
			}
			else{
				traffic = traffic + sw.bytesToUpSwitchSize.get(upsw.getId()); 
			}
			if(upsw.bytesToDownSwitchSize == null || upsw.bytesToDownSwitchSize.size() == 0 || upsw.bytesToDownSwitchSize.get(sw.getId()) == null){
				traffic = traffic + 0; 
			}
			else{
				 traffic = traffic + upsw.bytesToDownSwitchSize.get(sw.getId());
			}
			totBW = totBW + upsw.downlinkbandwidth;
		}
		usedBW =  (traffic * 8)  ;
		usedBW = (usedBW / totBW);
		if(usedBW > 1 )
			usedBW = 1;
		return usedBW;
    }
}
