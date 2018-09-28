package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.network.exDatacenter.AppCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudletSpaceSharedScheduler;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkVm;
import org.cloudbus.cloudsim.network.exDatacenter.TaskStage;

public class InfluxdbWorkload {
	private String datasetPath;  ///jsonDS/result1.json
	private InfluxdbDataset dataset;
	private List<AppCloudlet> applicatonList;
	
	// These Ids are used to manage if data belongs to a new cloudlet or application
	private String oldAppId;  //NetworkConstants.currentAppId
	private String oldCldId; //NetworkConstants.currentCloudletId
	//private int stageId;
	private int cldNO;
	
	private int brokerId;
	
	public InfluxdbWorkload(String datasetPathInput){
		this.datasetPath = datasetPathInput;
		dataset = new InfluxdbDataset(datasetPath);
		applicatonList = new ArrayList<AppCloudlet>();
	}
	
	//.........function to create a list of application..............
	public  List<AppCloudlet> createWorkload(int curBrokerId){
		this.brokerId = curBrokerId;
		AppCloudlet curApp = null;
		NetworkCloudlet curCl = null; 
		oldAppId = "9999";
		oldCldId = "99";
		
		while(this.readDatasetNextLine()){
		//	System.out.println("compare "+dataset.appId+" with "+oldAppId+" is "+(dataset.cloudletId == oldCldId));
		if(dataset.appId.equals(oldAppId)){
			//Data belongs to current application
			if(dataset.cloudletId.equals(oldCldId)) {
				//Data belongs to current cloudlet
				//create a new stage
				//System.out.println("new stage "+dataset.stageTyp+" no"+dataset.satageNo+",data:"+dataset.data_transfered+",time:"+dataset.exec_time);
				boolean result = manageStages(curApp, curCl);
				//stageId++;
			}
			else{
				//create a new cloudlet along with its first stage
				//stageId = 0;
				if(cldNO > 0){
					curApp.addCloudletToList(curCl);
				}
				cldNO++;
				curCl=newCloudlet();
				curCl.appId = curApp.appID;
				
				if(curApp.SendDataTo.get(cldNO-1)!=null) 
					curCl.numStage =curApp.SendDataTo.get(cldNO-1).size()+1;
				
				boolean result = manageStages(curApp, curCl);
				//stageId++;
				//System.out.println("new cloudlet with Id "+dataset.cloudletId +", cpu:"+dataset.pesNumber+",mem:"+dataset.memory);
				oldCldId=dataset.cloudletId;
				}
		}
		else{
			//create a new application along with its first cloudlet
			// and its first stage
			//stageId = 0;
			cldNO = 0;
			if(oldAppId.equals("9999") == false){
				curApp.addCloudletToList(curCl); // the last cloudlet of application
				curApp.numbervm = curApp.clist.size();
				applicatonList.add(curApp);
				NetworkConstants.currentAppId++;
			}
			curApp = newApplication();
			cldNO++;
			curCl=newCloudlet();
			curCl.appId = curApp.appID;
			boolean result = manageStages(curApp, curCl);
			//stageId++;
			//System.out.println("new app with Id "+dataset.appId);
			oldAppId = dataset.appId;
			oldCldId=dataset.cloudletId;
		}
		}
		curApp.addCloudletToList(curCl); // the last cloudlet of the last application
		curApp.numbervm = curApp.clist.size();
		applicatonList.add(curApp); // the last application
		return applicatonList;
	}
	//.............................................................
	private boolean readDatasetNextLine(){
		return dataset.readNextLine();
	}
	
	private boolean manageStages(AppCloudlet app, NetworkCloudlet cl){
		// create new stage based on dataset info
		//for execution satge we add a stage
		if(this.dataset.stageTyp.equals("exec_time")){
			cl.stages.add(new TaskStage(NetworkConstants.EXECUTION, 0, Math.round(this.dataset.exec_time),cl.numStage, 0, 0, 0));
			cl.numStage++;
			return true;
		}
		
		//for data transferring stage we add a record in application map
		int resiverId=this.dataset.rcv_clId;
		double dataMB = this.dataset.data_transfered;
		if(dataMB>0){
			if(app.SendDataTo.get(cldNO-1)==null){ // first data transferring stage for current cloudlet
				Map<int[],Double> info = new HashMap<int[],Double>();
				info.put(new int[]{(int) cl.numStage,resiverId}, dataMB); 
			    app.SendDataTo.put((cldNO-1),info);
			}
			else{
				app.SendDataTo.get(cldNO-1).put(new int[]{(int) cl.numStage,resiverId}, dataMB);
			}
			cl.numStage++;
			//create a data recieve stage
			if(app.clist.size() > resiverId){
				NetworkCloudlet rcp_cl = app.clist.get(resiverId);
				app.SendDataTo.get(resiverId).put(new int[]{(int) rcp_cl.numStage,(cldNO-1)}, (-1 * dataMB));
				rcp_cl.numStage++;
			}
			else
			{
				if(app.SendDataTo.get(resiverId)==null){ // first data transferring stage for current cloudlet
					Map<int[],Double> info = new HashMap<int[],Double>();
					info.put(new int[]{ 1, (cldNO-1)},  (-1 * dataMB)); 
				    app.SendDataTo.put(resiverId,info);
				}
				else{
					int lastStage = app.SendDataTo.get(resiverId).size() + 1;
					app.SendDataTo.get(resiverId).put(new int[]{(int) lastStage,(cldNO-1)}, (-1 * dataMB));
					
				} 
			
			}
		}
		return true;
	}
	
	private NetworkCloudlet newCloudlet(){
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
		NetworkCloudlet cl = new NetworkCloudlet(
				NetworkConstants.currentCloudletId, 
				this.dataset.length,
				this.dataset.pesNumber,
				this.dataset.fileSize,
				this.dataset.outputSize,
				this.dataset.memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		// create new cloudlet based on dataset info
		cl.stages.add(new TaskStage(NetworkConstants.EXECUTION, 0, 500, 0, 10, 0, 0));
		cl.numStage = 1;
		return cl;
	}
	
	private AppCloudlet newApplication(){
		AppCloudlet app = new AppCloudlet(AppCloudlet.APP_BigData, NetworkConstants.currentAppId, 0, 0, this.brokerId);;
		app.is_proccessed = 0;
		//app.SendDataTo = new ArrayList<Map<int[],Double>>();  
		// create new applicatin based on dataset info
		return app;
	}


	public  List<NetworkVm>  createVMs(int brokerId, List<AppCloudlet> appList){
		List<NetworkVm> vmlist = new ArrayList<NetworkVm>();
		int mips = 100;
    	long size = 300; //image size (MB)
    	int ram = 0; //vm memory (MB)
    	long bw = 5; // BW in MB  ---> no effect
    	int pesNumber = 0; //number of cpus
    	String vmm = "Xen"; //VMM name  ---> no effect
    	
    	int totalVMs = 0;
    	for(AppCloudlet app : appList){
    		for(NetworkCloudlet cl : app.clist){
    			totalVMs++;
    			if(cl.memory > ram)
    				ram = Math.round(cl.memory);
    			if(cl.getNumberOfPes() > pesNumber)
    				pesNumber = cl.getNumberOfPes();
    		}
    	}
    	ram = ram + 10;
    	System.out.println("total cloudlet and VMs is :"+totalVMs);
    	for(int i = 0; i<totalVMs ; i++){
    		NetworkVm vm = new NetworkVm(NetworkConstants.currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new NetworkCloudletSpaceSharedScheduler());
    		vmlist.add(vm);
    		NetworkConstants.currentVmId++;
		}
		return vmlist;
	}
}
