package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.network.exDatacenter.AppCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudletSpaceSharedScheduler;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;
import org.cloudbus.cloudsim.network.exDatacenter.TaskStage; 

public class ManageWorkload {
	
	public ManageWorkload(){}
	
	public static List<NetworkVm> create_5member_VMlist(int brokerId, int jobtyp){
		List<NetworkVm> vmlist = new ArrayList<NetworkVm>();
		//int brokerId = brk.getId();
		//VM description 
    	
    	int mips = 0;
    	long size = 0; //image size (MB)
    	int ram = 0; //vm memory (MB)
    	long bw = 0; // BW in MB
    	int pesNumber = 0; //number of cpus
    	String vmm = null; //VMM name
    	
    	if(jobtyp==1){//create 8 VMs suitable for jobtyp 1 (userId 8,13)
    		mips = 10;
        	size = 600;  
        	ram = 200;  
        	bw = 5;  
        	pesNumber = 24; 
        	vmm = "Xen";
        	for(int i = 0; i<8 ; i++){
        		NetworkVm vm = new NetworkVm(NetworkConstants.currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new NetworkCloudletSpaceSharedScheduler());
        		vmlist.add(vm);
        		NetworkConstants.currentVmId++;
    		}
    	}
    	
    	if(jobtyp==2){//create 9 VMs suitable for jobtyp 2 (userId 2 status 1)
    		mips = 15;
        	size = 480;  
        	ram = 60;  
        	bw = 5;  
        	pesNumber = 18; 
        	vmm = "Xen";
        	for(int i = 0; i<9 ; i++){
        		NetworkVm vm = new NetworkVm(NetworkConstants.currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new NetworkCloudletSpaceSharedScheduler());
        		vmlist.add(vm);
        		NetworkConstants.currentVmId++;
    		}
    	}
         
        return vmlist;
    	}
	
	
 public static List<AppCloudlet> createCloudlet(int brokerId, int jobTyp){

		// creating 2 apps correspond to jobTyp variable
	 	// jobTyp == 1 : App1 -> UserId 13, App2 -> UserId 8
	 	// jobTyp == 2 : App1 -> UserId 2 (status 1)
	 
	List<AppCloudlet> applist = new ArrayList<AppCloudlet>();
	AppCloudlet app1;
	AppCloudlet app2;
	UtilizationModel utilizationModel = new UtilizationModelFull(); 
	long length = 0;
	long fileSize = 0;
	long outputSize = 0;
	long memory = 0;
	int pesNumber = 0;
	 
		System.out.println("creating app cloudlet ...");  
		// for App1  
		if(jobTyp == 1){
			app1 = new AppCloudlet(AppCloudlet.APP_BigData, NetworkConstants.currentAppId, 0, 5, brokerId);
			app1.is_proccessed = 0;
			app1.SendDataTo = new ArrayList<Map<int[],Double>>();   
			
			length = 27020;
			fileSize = 3;
			outputSize= 43;
			memory= 1;
			pesNumber=1;
				NetworkCloudlet cl0 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl0.appId = NetworkConstants.currentAppId;
				cl0.numStage = 0;
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 500, 0, 1, 0, 0));// stage 0
				cl0.numStage++;																// stage 1,2 : sending data
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 200, 3, 1, 0, 0));//stage 3
				cl0.numStage++;
				app1.addCloudletToList(cl0); 
				
			length = 201600;
			fileSize = 27;
			outputSize= 10;
			memory= 1;
			pesNumber=24;
				NetworkCloudlet cl1 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl1.appId = NetworkConstants.currentAppId; 
				cl1.numStage = 0;
				cl1.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 50, 0, 1, 0, 0));// stage 0
				cl1.numStage++;											 					// stage 1 : receiving data
				cl1.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 20, 2, 1, 0, 0));// stage 2
				cl1.numStage++;											 					// stage 3 : sending data
				app1.addCloudletToList(cl1); 
					
			length = 1399200;
			fileSize = 172;
			outputSize= 9348;
			memory= 23;
			pesNumber=24;
				NetworkCloudlet cl2 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl2.appId = NetworkConstants.currentAppId; 
				cl2.numStage = 0;
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 120, 0, 1, 0, 0));//stage 0
				cl2.numStage++;											 					// stage 1 : receiving data
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 50, 2, 1, 0, 0));//stage 2
				cl2.numStage++;											 					// stage 3 : receiving data
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 100, 4, 1, 0, 0));//stage 4
				cl2.numStage++;
				app1.addCloudletToList(cl2); 
				
			length = 72110;
			fileSize = 559;
			outputSize= 9075;
			memory= 47;
			pesNumber=18;
				NetworkCloudlet cl3 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl3.appId = NetworkConstants.currentAppId; 
				cl3.numStage = 0;
				cl3.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 1500, 0, 1, 0, 0));//stage 0
				cl3.numStage++;											 					// stage 1 : sending data
				cl3.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 500, 2, 1, 0, 0));//stage 2
				cl3.numStage++;
				app1.addCloudletToList(cl3); 
							
			length = 1437600;
			fileSize = 3;
			outputSize= 43;
			memory= 1;
			pesNumber=1;
				NetworkCloudlet cl4 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl4.appId = NetworkConstants.currentAppId; 
				cl4.numStage = 0;
				cl4.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 300,0, 1, 0, 0));//stage 0
				cl0.numStage++;											 					// stage 1 : receiving data
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 800, 2, 1, 0, 0));//stage 2
				cl0.numStage++;
				app1.addCloudletToList(cl4); 
				 
			
			/// app 1 cloudlet 0 ............................. these numbers are indexes not Ids
				Map<int[],Double> cl_0 = new HashMap<int[],Double>();
				cl_0.put(new int[]{1,1}, 10.0);
				cl_0.put(new int[]{2,2}, 90.0);
			app1.SendDataTo.add(cl_0);
			/// app 1 cloudlet 1 
				Map<int[],Double> cl_1 = new HashMap<int[],Double>();
				cl_1.put(new int[]{1,0}, -10.0);
				cl_1.put(new int[]{3,2}, 40.0);
			app1.SendDataTo.add(cl_1);
			/// app 1 cloudlet 2 
				Map<int[],Double> cl_2 = new HashMap<int[],Double>();
				cl_2.put(new int[]{1,0}, -90.0);
				cl_2.put(new int[]{3,1}, -40.0);
			app1.SendDataTo.add(cl_2);
			/// app 1 cloudlet 3 
				Map<int[],Double> cl_3 = new HashMap<int[],Double>();
				cl_3.put(new int[]{1,4}, 45.0);
			app1.SendDataTo.add(cl_3);
			/// app 1 cloudlet 4
				Map<int[],Double> cl_4 = new HashMap<int[],Double>(); 
				cl_4.put(new int[]{1,3}, -45.0);
			app1.SendDataTo.add(cl_4);
			applist.add(app1);
			NetworkConstants.currentAppId++;
			
		
			app2 = new AppCloudlet(AppCloudlet.APP_BigData, NetworkConstants.currentAppId, 0, 3, brokerId);
			app2.is_proccessed = 0;
			app2.SendDataTo = new ArrayList<Map<int[],Double>>();  
			
			length = 296000;
			fileSize = 1;
			outputSize= 246;
			memory= 157;
			pesNumber=12;
				NetworkCloudlet cl5 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl5.appId = NetworkConstants.currentAppId;
				cl5.numStage = 0;
				cl5.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 1200, 0, 1, 0, 0));// stage 0
				cl5.numStage++;																// stage 1 : sending data
				cl5.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 300, 2, 1, 0, 0));//stage 3
				cl5.numStage++;
				app2.addCloudletToList(cl5); 
				
			length = 295900;
			fileSize = 5;
			outputSize= 265;
			memory= 186;
			pesNumber=12;
				NetworkCloudlet cl6 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl6.appId = NetworkConstants.currentAppId; 
				cl6.numStage = 0;
				cl6.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 1250, 0, 1, 0, 0));// stage 0
				cl6.numStage++;											 					// stage 1 : sending data
				cl6.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 280, 2, 1, 0, 0));// stage 2
				cl6.numStage++;											 					// stage 3 : sending data
				app2.addCloudletToList(cl6); 
					
			length = 101420;
			fileSize = 1;
			outputSize= 156;
			memory= 11;
			pesNumber=12;
				NetworkCloudlet cl7 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl7.appId = NetworkConstants.currentAppId; 
				cl7.numStage = 0;
				cl7.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 80, 0, 1, 0, 0));//stage 0
				cl7.numStage++;											 					// stage 1 : receiving data
				cl7.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 50, 2, 1, 0, 0));//stage 2
				cl7.numStage++;											 					// stage 3 : receiving data
				cl7.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 320, 4, 1, 0, 0));//stage 4
				cl7.numStage++;
				app2.addCloudletToList(cl7); 
			
				/// app 2 cloudlet 0
				Map<int[],Double> cl_5 = new HashMap<int[],Double>();
				cl_5.put(new int[]{1,2}, 60.0); 
				app2.SendDataTo.add(cl_5);
				/// app 2 cloudlet 1
				Map<int[],Double> cl_6 = new HashMap<int[],Double>();
				cl_6.put(new int[]{1,2}, 65.0);
				app2.SendDataTo.add(cl_6);
				/// app 2 cloudlet 2
				Map<int[],Double> cl_7 = new HashMap<int[],Double>();
				cl_7.put(new int[]{1,0}, -60.0);
				cl_7.put(new int[]{3,1}, -65.0);
				app2.SendDataTo.add(cl_7); 
			
			applist.add(app2);
			NetworkConstants.currentAppId++;
		} 
		if(jobTyp == 2){ // userId 5 status 1
			app1 = new AppCloudlet(AppCloudlet.APP_BigData, NetworkConstants.currentAppId, 0, 9, brokerId);
			app1.is_proccessed = 0;
			app1.SendDataTo = new ArrayList<Map<int[],Double>>();   
			
			length = 1096340;
			fileSize = 126;
			outputSize= 136;
			memory= 50;
			pesNumber=10;
				NetworkCloudlet cl0 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl0.appId = NetworkConstants.currentAppId;
				cl0.numStage = 0;
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 400, 0, 1, 0, 0));// stage 0
				cl0.numStage++;																// stage 1 : sending data
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 80, 2, 1, 0, 0));//stage 2
				cl0.numStage++;
				app1.addCloudletToList(cl0); 
				
			length = 989100;
			fileSize = 61;
			outputSize= 32;
			memory= 44;
			pesNumber=10;
				NetworkCloudlet cl1 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl1.appId = NetworkConstants.currentAppId; 
				cl1.numStage = 0;
				cl1.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 350, 0, 1, 0, 0));// stage 0
				cl1.numStage++;											 					// stage 1 : sending data
				cl1.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 77, 2, 1, 0, 0));// stage 2
				cl1.numStage++;											 					 
				app1.addCloudletToList(cl1); 
					
			length = 1808240;
			fileSize = 301;
			outputSize= 199;
			memory= 15;
			pesNumber=18;
				NetworkCloudlet cl2 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl2.appId = NetworkConstants.currentAppId; 
				cl2.numStage = 0;
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 50, 0, 1, 0, 0));//stage 0
				cl2.numStage++;											 					// stage 1 : receiving data
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 380, 2, 1, 0, 0));//stage 2
				cl2.numStage++;											 					// stage 3,4,5 : sending data
				cl2.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 84, 6, 1, 0, 0));//stage 6
				cl2.numStage++;
				app1.addCloudletToList(cl2); 
				
			length = 2929290;
			fileSize = 466;
			outputSize= 352;
			memory= 11;
			pesNumber=18;
				NetworkCloudlet cl3 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl3.appId = NetworkConstants.currentAppId; 
				cl3.numStage = 0;
				cl3.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 95, 0, 1, 0, 0));//stage 0
				cl3.numStage++;											 					// stage 1 : receiving data
				cl3.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 310, 2, 1, 0, 0));//stage 2
				cl3.numStage++;										 						// stage 3,4 : sending data
				cl3.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 165, 5, 1, 0, 0));//stage 5
				cl3.numStage++;
				app1.addCloudletToList(cl3); 
							
			length = 2465460;
			fileSize = 127;
			outputSize= 443;
			memory= 24;
			pesNumber=10;
				NetworkCloudlet cl4 = new NetworkCloudlet(
						NetworkConstants.currentCloudletId, 
						length,
						pesNumber,
						fileSize,
						outputSize,
						memory,
						utilizationModel,
						utilizationModel,
						utilizationModel);
				cl4.appId = NetworkConstants.currentAppId; 
				cl4.numStage = 0;
				cl4.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 110, 0, 1, 0, 0));//stage 0
				cl0.numStage++;											 					// stage 1 : receiving data
				cl0.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 420, 2, 1, 0, 0));//stage 2
				cl0.numStage++;
				app1.addCloudletToList(cl4); 
				 
				length = 1636680;
				fileSize = 61;
				outputSize= 436;
				memory= 31;
				pesNumber=10;
					NetworkCloudlet cl5 = new NetworkCloudlet(
							NetworkConstants.currentCloudletId, 
							length,
							pesNumber,
							fileSize,
							outputSize,
							memory,
							utilizationModel,
							utilizationModel,
							utilizationModel);
					cl5.appId = NetworkConstants.currentAppId;
					cl5.numStage = 0;
					cl5.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 92, 0, 1, 0, 0));// stage 0
					cl5.numStage++;																// stage 1 : receiving data
					cl5.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 280, 2, 1, 0, 0));//stage 3
					cl5.numStage++;
					app1.addCloudletToList(cl5); 
					
				length = 1577770;
				fileSize = 261;
				outputSize= 427;
				memory= 33;
				pesNumber=10;
					NetworkCloudlet cl6 = new NetworkCloudlet(
							NetworkConstants.currentCloudletId, 
							length,
							pesNumber,
							fileSize,
							outputSize,
							memory,
							utilizationModel,
							utilizationModel,
							utilizationModel);
					cl6.appId = NetworkConstants.currentAppId; 
					cl6.numStage = 0;
					cl6.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 90, 0, 1, 0, 0));// stage 0
					cl6.numStage++;											 					// stage 1 : receiving data
					cl6.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 260, 2, 1, 0, 0));// stage 2
					cl6.numStage++;											 					 
					app1.addCloudletToList(cl6); 
					
				length = 1601480;
				fileSize = 263;
				outputSize= 419;
				memory= 34;
				pesNumber=10;
					NetworkCloudlet cl7 = new NetworkCloudlet(
							NetworkConstants.currentCloudletId, 
							length,
							pesNumber,
							fileSize,
							outputSize,
							memory,
							utilizationModel,
							utilizationModel,
							utilizationModel);
					cl7.appId = NetworkConstants.currentAppId; 
					cl7.numStage = 0;
					cl7.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 91, 0, 1, 0, 0));// stage 0
					cl7.numStage++;											 					// stage 1 : receiving data
					cl7.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 260, 2, 1, 0, 0));// stage 2
					cl7.numStage++;											 					 
					app1.addCloudletToList(cl7); 
						
				length = 3197970;
				fileSize = 62;
				outputSize= 452;
				memory= 19;
				pesNumber=10;
					NetworkCloudlet cl8 = new NetworkCloudlet(
							NetworkConstants.currentCloudletId, 
							length,
							pesNumber,
							fileSize,
							outputSize,
							memory,
							utilizationModel,
							utilizationModel,
							utilizationModel);
					cl8.appId = NetworkConstants.currentAppId; 
					cl8.numStage = 0;
					cl8.stages.add(new TaskStage(NetworkConstants.EXECUTION, 2, 110, 0, 1, 0, 0));// stage 0
					cl8.numStage++;											 					// stage 1 : receiving data
					cl8.stages.add(new TaskStage(NetworkConstants.EXECUTION, 1, 410, 2, 1, 0, 0));// stage 2
					cl8.numStage++;								 
					app1.addCloudletToList(cl8); 
					
			/// app 1 cloudlet 0 ............................. these numbers are indexes not Ids
				Map<int[],Double> cl_0 = new HashMap<int[],Double>();
				cl_0.put(new int[]{1,2}, 80.0); 
			app1.SendDataTo.add(cl_0);
			/// app 1 cloudlet 1 
				Map<int[],Double> cl_1 = new HashMap<int[],Double>(); 
				cl_1.put(new int[]{1,3}, 76.0);
			app1.SendDataTo.add(cl_1);
			/// app 1 cloudlet 2 
				Map<int[],Double> cl_2 = new HashMap<int[],Double>();
				cl_2.put(new int[]{1,0}, -80.0);
				cl_2.put(new int[]{3,4},  40.0);
				cl_2.put(new int[]{4,6},  55.0);
				cl_2.put(new int[]{5,8},  45.0);
			app1.SendDataTo.add(cl_2);
			/// app 1 cloudlet 3 
				Map<int[],Double> cl_3 = new HashMap<int[],Double>();
				cl_3.put(new int[]{1,1}, -76.0);
				cl_3.put(new int[]{3,5},  40.0);
				cl_3.put(new int[]{4,7},  55.0);
			app1.SendDataTo.add(cl_3);
			/// app 1 cloudlet 4
				Map<int[],Double> cl_4 = new HashMap<int[],Double>(); 
				cl_4.put(new int[]{1,2}, -40.0);
			app1.SendDataTo.add(cl_4);
			/// app 1 cloudlet 5
			Map<int[],Double> cl_5 = new HashMap<int[],Double>();
			cl_5.put(new int[]{1,3}, -40.0); 
			app1.SendDataTo.add(cl_5);
			/// app 1 cloudlet 6
			Map<int[],Double> cl_6 = new HashMap<int[],Double>();
			cl_6.put(new int[]{1,2}, -55.0);
			app1.SendDataTo.add(cl_6); 
			/// app 1 cloudlet 7
			Map<int[],Double> cl_7 = new HashMap<int[],Double>();
			cl_7.put(new int[]{1,3}, -55.0); 
			app1.SendDataTo.add(cl_7);
			/// app 1 cloudlet 8
			Map<int[],Double> cl_8 = new HashMap<int[],Double>();
			cl_8.put(new int[]{1,2}, -45.0);
			app1.SendDataTo.add(cl_8);

			applist.add(app1);
			NetworkConstants.currentAppId++;
		}
		return applist;
 }
}
