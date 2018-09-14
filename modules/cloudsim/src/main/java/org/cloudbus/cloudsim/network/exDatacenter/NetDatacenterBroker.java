/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.exDatacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * NetDatacentreBroker represents a broker acting on behalf of Datacenter provider. It hides VM
 * management, as vm creation, submission of cloudlets to these VMs and destruction of VMs. <br/>
 * <tt>NOTE</tt>: This class is an example only. It works on behalf of a provider not for users. 
 * One has to implement interaction with user broker to this broker.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 * @todo The class is not a broker acting on behalf of users, but on behalf
 * of a provider. Maybe this distinction would be explicit by 
 * different class hierarchy, such as UserDatacenterBroker and ProviderDatacenterBroker.
 */
public class NetDatacenterBroker extends SimEntity {

	// TODO: remove unnecessary variables

	/** The list of submitted VMs. */
	private List<? extends Vm> vmList;
	private static List<Integer> usedVmList;

	/** The list of created VMs. */
	private List<? extends Vm> vmsCreatedList;

	/** The list of submitted {@link NetworkCloudlet NetworkCloudlets}. */
	private List<? extends NetworkCloudlet> cloudletList;

	/** The list of submitted {@link AppCloudlet AppCloudlets}. */
	private List<? extends AppCloudlet> appCloudletList;

	/** The list of submitted {@link AppCloudlet AppCloudlets}.
         * @todo attribute appears to be redundant with {@link #appCloudletList}
         */
	private final Map<Integer, Integer> appCloudletRecieved;

	/** The list of submitted {@link Cloudlet Cloudlets}.
         */
	private List<? extends Cloudlet> cloudletSubmittedList;

	/** The list of received {@link Cloudlet Cloudlets}.
         * @todo attribute appears to be redundant with {@link #cloudletSubmittedList}
         */
	private List<? extends Cloudlet> cloudletReceivedList;

	/** The number of submitted cloudlets. */
	private int cloudletsSubmitted;

	/** The number of VMs requested. */
	private int vmsRequested;

	/** The acks sent to VMs. */
	private int vmsAcks;

	/** The number of VMs destroyed. */
	private int vmsDestroyed;

	/** The list of datacenter IDs. */
	private List<Integer> datacenterIdsList;

	/** The datacenter requested IDs list. 
         * @todo attribute appears to be redundant with {@link #datacenterIdsList}
         */
	private List<Integer> datacenterRequestedIdsList;

	/** The VMs to datacenters map where each key is a VM id
         * and the corresponding value is the datacenter where the VM is placed. */
	private Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics map where each key 
         * is the datacenter id and each value is the datacenter itself. */
	private Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	public static NetworkDatacenter linkDC;

	public boolean createvmflag = true;

	public static int cachedcloudlet = 0;

	/**
	 * Creates a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity
	 * 
	 * @throws Exception the exception
	 * 
	 * @pre name != null
	 * @post $none
	 */
	public NetDatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<NetworkVm>());
		setVmsCreatedList(new ArrayList<NetworkVm>());
		setCloudletList(new ArrayList<NetworkCloudlet>());
		setAppCloudletList(new ArrayList<AppCloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		appCloudletRecieved = new HashMap<Integer, Integer>();

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		
		usedVmList = new ArrayList<Integer>();

	}

	/**
	 * Sends to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * Sends to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends NetworkCloudlet> list) {
		getCloudletList().addAll(list);
	}

	public static void setLinkDC(NetworkDatacenter alinkDC) {
		linkDC = alinkDC;
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
		//	case CloudSimTags.RESOURCE_CHARACTERISTICS_DELAY:
		//		processResourceCharacteristicsDelay(ev);
		//		break;
			// VM Creation answer
			/*case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;*/
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			case CloudSimTags.NextCycle:
				if (NetworkConstants.BASE) {
				//	createVmsInDatacenterBase(linkDC.getId());
					createVmsInDatacenter(linkDC.getId());
				}

				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Processes the return of a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
		//	createVmsInDatacenterBase(getDatacenterIdsList().get(0));
	    	createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}
	protected void processResourceCharacteristicsDelay(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
		//	createVmsInDatacenterBase(getDatacenterIdsList().get(0));
	    	createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}
	/**
	 * For each AppCloudlet in Datacenter, randomly selects a group of VMs in Datacenter and
	 * assigns them to the Cloudlets. After determining which VMs ID belongs to which cloudlet,
	 * it call a function to define the stages of cloudlet.
	 * 
	 * @param datacenterId , it is not used! 
	 */

	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		createvmflag = false; 
		
		int strIndex = 0;
		for (AppCloudlet app : this.getAppCloudletList()) {
			//cloudletToVmRandomAssign(app);
			
			cloudletToVmSequenceAssign(app,strIndex);
			strIndex = strIndex + app.numbervm;
			/*  Define the stages of the cloudlets belong to the current AppCloudlet.
			 *  The related function is called heir, because it needs assigned VMs Id
			 *  for defining send/receive stage.
			 */
			DCMngUtility.defineStagesOfTable(app);
		}
		setVmsRequested(0);
		setVmsAcks(0);
	}

	/**
	 * Processes a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */

	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloud Resource List received with ",
				getDatacenterIdsList().size(), " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Processes the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */

	/**
	 * Processes a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		NetworkCloudlet cloudlet = (NetworkCloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		cloudletsSubmitted--;  
		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getAppCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				//createVmsInDatacenterBase(0); 
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Processes non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null.");
			return;
		}

		Log.printConcatLine(getName(), ".processOtherEvent(): ",
				"Error - event unknown by this DatacenterBroker.");
	}

    
	/**
     * assign Created virtual machines to a datacenter, Which can be called in
     * Simulation class to assign VMs object to the data center.
     * @param datacenterId The id of the datacenter where to create the VMs.
     * @param NewVmlist The list of the VMs which are created in simulation class
     */
	public void CreateCustomVMs(int datacenterId, List<NetworkVm> NewVmlist) {
		//System.out.println("CALLED CREATE CUSTOM VM....");
		// if the VM allocation policy needs whole cluster of VMs
		if(linkDC.VmAllcPlcyTyp == DCMngUtility.VM_ALLC_PLCY_CLUSTER){
			System.out.println("***** CLUSTER VM ALLOCATION POLICY *****");
			linkDC.processVmListCreateNetwork(NewVmlist);  
			for (NetworkVm vm : NewVmlist) { 
				// add the VM to the vmList
				getVmList().add(vm);
				getVmsToDatacentersMap().put(vm.getId(), datacenterId);
				getVmsCreatedList().add(VmList.getById(getVmList(), vm.getId()));
			}
		}
		else{
			System.out.println("***** SINGLE VM ALLOCATION POLICY *****");
		for (NetworkVm vm : NewVmlist) { 
			linkDC.processVmCreateNetwork(vm);
			// add the VM to the vmList
			getVmList().add(vm);
			getVmsToDatacentersMap().put(vm.getId(), datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vm.getId()));
		}
		}
		createvmflag = false;
	}

	/**
	 * Sends request to destroy all VMs running on the datacenter.
	 * 
	 * @pre $none
	 * @post $none /** Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) { 
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Destroying VM #", vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
			}

		getVmsCreatedList().clear();
	}

	/**
	 * Sends an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	private void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	@Override
	public void shutdownEntity() {
		Log.printConcatLine(getName(), " is shutting down...");
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends NetworkCloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends NetworkCloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	@SuppressWarnings("unchecked")
	public <T extends AppCloudlet> List<T> getAppCloudletList() {
		return (List<T>) appCloudletList;
	}

	public <T extends AppCloudlet> void setAppCloudletList(List<T> appCloudletList) {
		this.appCloudletList = appCloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	public Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	protected void cloudletToVmRandomAssign(AppCloudlet app){

		int numStrVms =  this.vmList.get(0).getId(); 
		int numEndVms =  numStrVms + this.vmList.size()-1;
		List<Integer> vmids = new ArrayList<Integer>(); 
		//System.out.println("size of vm list :"+this.vmList.size()+" from "+numStrVms+" to " +numEndVms);
		UniformDistr ufrnd = new UniformDistr(numStrVms, numEndVms);
		for (int i = 0; i < app.numbervm; i++) {
			int vmid = (int) ufrnd.sample();
			if (usedVmList != null) {
			while(usedVmList.indexOf(vmid) >=0){
				vmid = (int) ufrnd.sample();
			    }
			}
			System.out.println("selected vm: "+ vmid);
			vmids.add(vmid);
			usedVmList.add(vmid);

		}

		if (vmids != null) {
			if (!vmids.isEmpty()) {

				for (int i = 0; i < app.numbervm; i++) {
					app.clist.get(i).setUserId(getId()); 
					app.clist.get(i).setVmId(vmids.get(i));
					appCloudletRecieved.put(app.appID, app.numbervm);
					this.getCloudletSubmittedList().add(app.clist.get(i));
					cloudletsSubmitted++;
						
					// Sending cloudlet
					sendNow(
							getVmsToDatacentersMap().get(this.vmList.get(0).getId()),
							CloudSimTags.CLOUDLET_SUBMIT,
							app.clist.get(i));
				} 
				app.is_proccessed = 1;
			}
		}
	}
	protected void cloudletToVmSequenceAssign(AppCloudlet app, int startIx){
		int vmid = this.vmList.get(startIx).getId();
		usedVmList.add(vmid);
		for (int i = 0; i < app.numbervm; i++) {
					app.clist.get(i).setUserId(getId()); 
					app.clist.get(i).setVmId(this.vmList.get(startIx+i).getId());
					appCloudletRecieved.put(app.appID, app.numbervm);
					this.getCloudletSubmittedList().add(app.clist.get(i));
					cloudletsSubmitted++;
						
					// Sending cloudlet
					sendNow(
							getVmsToDatacentersMap().get(this.vmList.get(0).getId()),
							CloudSimTags.CLOUDLET_SUBMIT,
							app.clist.get(i));
				} 
				app.is_proccessed = 1;
			
	}

}
