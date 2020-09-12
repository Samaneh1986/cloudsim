package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set; 

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonDataset {
	private String datasetPath;
	
	//dataset variables ....
	public String appId;
	public String cloudletId;
	public long length;
	public long fileSize; // initial input read size in MB
	public long outputSize;// final output write size in MB
	public long memory;
	public int pesNumber;
	public String stageTyp;
//	public int satageNo;
	public double data_transfered;
	public int rcv_clId;
	public long exec_time;
	
	
	private  JSONObject dataOBJ;
	private Iterator<String> datasetKeys;
	private List sortedKeySet;
	
	JsonDataset(String datasetPathInput){
		this.datasetPath = datasetPathInput;
		
	//	openFile statements here .....
		JSONParser parser = new JSONParser();
		try {     
            Object obj = parser.parse(new FileReader(this.datasetPath));

            JSONObject rootJSON =  (JSONObject) obj;
            dataOBJ = (JSONObject) rootJSON.get("data");
            Set<String> keySet = dataOBJ.keySet(); 
            sortedKeySet = new ArrayList(keySet);
            Collections.sort(sortedKeySet);
            datasetKeys = sortedKeySet.iterator();
            
             
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
	}
	
	public boolean readNextLine(){
		if(datasetKeys.hasNext()){
			String key = datasetKeys.next();
			JSONObject row = (JSONObject) dataOBJ.get(key);
			//System.out.println(String.valueOf( row.get("appId")));
			
			this.appId = String.valueOf( row.get("appId"));
			this.cloudletId = String.valueOf( row.get("clId"));
			if(row.get("pe_no") != null){
			   this.pesNumber = Integer.valueOf(String.valueOf(row.get("pe_no"))).intValue();
			}
			else{ 
				this.pesNumber = 0;
			}
			if(row.get("mem_used")!= null){
			   double mem = Double.valueOf(String.valueOf(row.get("mem_used"))).doubleValue()/1000000;//in MB
			   this.memory = Math.round(mem);
			}
			else{
				this.memory = 0 ;
			}
			this.stageTyp = String.valueOf(row.get("typ"));
			//System.out.println(key+":"+key.charAt(6));
			//this.satageNo = Integer.valueOf(String.valueOf(key.charAt(6))).intValue();
			this.rcv_clId = Integer.valueOf(String.valueOf(row.get("rcv_clId"))).intValue();
			double data = Double.valueOf(String.valueOf(row.get("data"))).doubleValue()/1000000;//in MB
			this.data_transfered = data;
			double time = Double.valueOf(String.valueOf(row.get("exec_time"))).doubleValue();
			this.exec_time = Math.round(time);
				
			this.length=40000;
			if(row.get("input")!= null){
				   double input = Double.valueOf(String.valueOf(row.get("input"))).doubleValue()/10000;//in B
				   this.fileSize = Math.round(input);
				}
				else{
					//this.fileSize = Math.round(Math.random() * 500) ;
					this.fileSize = 0 ;
				}
			
			if(row.get("output")!= null){
				   double output = Double.valueOf(String.valueOf(row.get("output"))).doubleValue()/10000;//in B
				   this.outputSize = Math.round(output);
				}
				else{
					//this.outputSize = Math.round(Math.random() * 500) ;
					this.outputSize = 0 ;
				}
			//this.fileSize = 0 ;
			//this.outputSize = 0 ;
			
			return true;
		}
		return false;
	}
	
	public boolean resetValues(){
		appId = null;
		cloudletId= null;
		length= 0;
		fileSize= 0;
		outputSize= 0;
		memory= 0;
		pesNumber= 0;
		stageTyp= null;
		//satageNo= 0;
		data_transfered= 0;
		rcv_clId= 0;
		exec_time= 0;
		datasetKeys = sortedKeySet.iterator();
        
		return true;
	}

}
