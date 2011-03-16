package org.ligi.android.bluetooth.bluetooth_device_picker;

import it.gerdavax.easybluetooth.RemoteDevice;
/**
 * class to represent a blueooth-device
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothDevice {
	private String addr="";
	private String friendly_name="";
	private int seen_in_round=-1; // not seen
	private boolean saved=false;
	
	public BluetoothDevice(RemoteDevice rd,int act_scan_round) {
		addr=rd.getAddress();
		setFriendlyName(rd.getFriendlyName());
		seen_in_round=act_scan_round;
	}
	
	public void setFriendlyName(String new_fn) {
		if (!new_fn.equals(""))
			friendly_name=new_fn;
		else
			if (friendly_name.equals(""))
				friendly_name=getAddr();
				
	}

	public BluetoothDevice(String friendly_name,String addr) {
		this.addr=addr;
		setFriendlyName(friendly_name);
	}
	
	public BluetoothDevice(String friendly_name,String addr,boolean saved) {
		this.addr=addr;
		setFriendlyName(friendly_name);
		setSaved(saved);
	}
	
	/**
	 * updates friendly name if not present info
	 * and ors recently seen with this
	 * 
	 * @param bd
	 */
	public void updateFriendlyAndSeen(BluetoothDevice bd,int act_scan_round) {
		if (this.getFriendlyName().equals(""))
			this.friendly_name=bd.getFriendlyName();
		seen_in_round=act_scan_round;
	}
	
	public String getAddr() {
		return addr;
	}
	
	public String getFriendlyName() {
		return friendly_name;
	}
	
	public int getSeenRound(){
		return seen_in_round;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public boolean isSaved() {
		return saved;
	}
}