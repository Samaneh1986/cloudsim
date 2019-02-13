package org.cloudbus.cloudsim.network.exDatacenter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.HarddriveStorage;

public class NetStoragePoolRuleSimple extends NetStoragePoolRule {
	private Map<Integer, NetStorageHost> storageList;
	private static int riplicas = 3;
	private String mapper;
	
	/*there are 300 hard drives in 15 storage hosts in current DC config*/
	private static int[][] distribution ;
	
	public NetStoragePoolRuleSimple(Map<Integer, NetStorageHost> storageList, String mapperPath){
		this.storageList = storageList;
		this.mapper = mapperPath;
		readMapperFile();
	}

	@Override
	public  List<Map<NetHarddriveStorage,Integer>> selectStorage(int blockIndex) {
		// TODO Auto-generated method stub
		int rowIndex = blokIndexMapping(blockIndex);
		int[] hardDriveList = distribution[rowIndex];
		List<Map<NetHarddriveStorage,Integer>> storageFinalList = new ArrayList<Map<NetHarddriveStorage,Integer>>();

		for(int i = 0; i< hardDriveList.length ; i++){
		for(NetStorageHost hs : storageList.values()){
			for(NetHarddriveStorage hdd : hs.storageDriveList){
					if(hdd.getIndex() == hardDriveList[i]){
						Map<NetHarddriveStorage,Integer> osd = new HashMap<NetHarddriveStorage,Integer>();
						osd.put(hdd, new Integer(hs.getId()));
						storageFinalList.add(osd);
					}
				}
			}
		}
		return storageFinalList;
	}
	
	private int blokIndexMapping(int blockIndex){
		int distIndex = 0;
		distIndex = blockIndex % 1024;
		return distIndex;
	}
	
	private void readMapperFile(){
		distribution = new int[1024][3];
		String csvFile = mapper;
		String line = "";
	    String cvsSplitBy = ",";
	    BufferedReader br = null;
	    int index = 0;
	    try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] osdList = line.split(cvsSplitBy);
                distribution [index][0]=Integer.parseInt(osdList[1]);
                distribution [index][1]=Integer.parseInt(osdList[2]);
                distribution [index][2]=Integer.parseInt(osdList[3]);
                index++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
