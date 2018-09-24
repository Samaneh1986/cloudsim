package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.network.exDatacenter.AppCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.exDatacenter.NetworkConstants;

public class InfluxdbWorkload {
	private String datasetPath;  ///jsonDS/result1.json
	private InfluxdbDataset dataset;
	private List<AppCloudlet> applicatonList;
	
	// These Ids are used to manage if data belongs to a new cloudlet or application
	private String oldAppId;  //NetworkConstants.currentAppId
	private String oldCldId; //NetworkConstants.currentCloudletId
	private int stageId;
	private int cldNO;
	
	public InfluxdbWorkload(String datasetPathInput){
		this.datasetPath = datasetPathInput;
		dataset = new InfluxdbDataset(datasetPath);
		applicatonList = new ArrayList<AppCloudlet>();
	}
	
	//.........function to create a list of application..............
	public  List<AppCloudlet> createWorkload(){
		AppCloudlet curApp = null;
		NetworkCloudlet curCl = null;
		
		while(this.readDatasetNextLine()){
		//	System.out.println("compare "+dataset.appId+" with "+oldAppId+" is "+(dataset.cloudletId == oldCldId));
		if(dataset.appId.equals(oldAppId)){
			//Data belongs to current application
			if(dataset.cloudletId.equals(oldCldId)) {
				//Data belongs to current cloudlet
				//create a new stage
				System.out.println("new stage .... ");
				//boolean result = manageStages(curApp, curCl);
				stageId++;
			}
			else{
				//create a new cloudlet along with its first stage
				stageId = 0;
				cldNO++;
				//curCl=newCloudlet();
				System.out.println("new cloudlet with Id "+dataset.cloudletId +", cpu:"+dataset.pesNumber+",mem:"+dataset.memory);
				//curApp.addCloudletToList(curCl);
				oldCldId=dataset.cloudletId;
			}
		}
		else{
			//create a new application along with its first cloudlet
			// and its first stage
			stageId = 0;
			cldNO = 0;
			//curApp = newApplication();
			System.out.println("new app with Id "+dataset.appId);
			//applicatonList.add(curApp);
			oldAppId = dataset.appId;
		}
		}
		return applicatonList;
	}
	//.............................................................
	private boolean readDatasetNextLine(){
		return dataset.readNextLine();
	}
	
	private boolean manageStages(AppCloudlet app, NetworkCloudlet cl){
		// create new stage based on dataset info
		//for execution satge we add a stage
		
		//for data transferring stage we add a record in application map
		int resiverId=0;
		double dataMB = 0.0;
		if(app.SendDataTo == null){
			Map<int[],Double> info = new HashMap<int[],Double>();
			info.put(new int[]{stageId,resiverId}, dataMB); 
		    app.SendDataTo.add(info);
		}
		else{
			app.SendDataTo.get(cldNO).put(new int[]{stageId,resiverId}, dataMB);
		}
		return false;
	}
	
	private NetworkCloudlet newCloudlet(){
		NetworkCloudlet cl = null;
		// create new cloudlet based on dataset info
		return cl;
	}
	
	private AppCloudlet newApplication(){
		AppCloudlet app = null;
		// create new applicatin based on dataset info
		return app;
	}

}
