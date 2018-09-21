package org.cloudbus.cloudsim.examples.Vmp_Prj;

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
	
	InfluxdbDataset(String datasetPathInput){
		this.datasetPath = datasetPathInput;
		
		//openFile statements here .....
	}
	
	public boolean readNextLine(){
		return false;
	}
	
	public boolean resetValues(){
		return false;
	}

}
