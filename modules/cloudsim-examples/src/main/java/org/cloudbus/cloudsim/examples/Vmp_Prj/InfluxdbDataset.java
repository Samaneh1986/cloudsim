package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set; 

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InfluxdbDataset {
	private String datasetPath;
	
	//dataset variables ....
	public String appId;
	public String cloudletId;
	public long length;
	public long fileSize;
	public long outputSize;
	public long memory;
	public int pesNumber;
	
	
	private  JSONObject dataOBJ;
	private Iterator<String> datasetKeys;
	
	InfluxdbDataset(String datasetPathInput){
		this.datasetPath = datasetPathInput;
		
	//	openFile statements here .....
		JSONParser parser = new JSONParser();
		try {     
            Object obj = parser.parse(new FileReader(this.datasetPath));

            JSONObject rootJSON =  (JSONObject) obj;
            dataOBJ = (JSONObject) rootJSON.get("data");
            Set<String> keySet = dataOBJ.keySet(); 
            List sortedKeySet = new ArrayList(keySet);
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
			   double mem = Double.valueOf(String.valueOf(row.get("mem_used"))).doubleValue()/1000;
			   this.memory = Math.round(mem);
			}
			else{
				this.memory = 0 ;
			}
				
			this.length=100;
			this.fileSize=500;
			this.outputSize=500;
			
			return true;
		}
		return false;
	}
	
	public boolean resetValues(){
		return false;
	}

}
