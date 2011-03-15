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
	
	public BluetoothDevice(RemoteDevice rd,int act_scan_round) {
		addr=rd.getAddress();
		friendly_name=rd.getFriendlyName();
		seen_in_round=act_scan_round;
	}
	
	public BluetoothDevice(String friendly_name,String addr) {
		this.friendly_name=friendly_name;
		this.addr=addr;
	}
	
	/**
	 * updates friendly name if not present info
	 * and ors recently seen with this
	 * 
	 * @param bd
	 */
	public void updateFriendlyAndSeen(BluetoothDevice bd,int act_scan_round) {
		if (this.getFriendlyName()=="")
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
}