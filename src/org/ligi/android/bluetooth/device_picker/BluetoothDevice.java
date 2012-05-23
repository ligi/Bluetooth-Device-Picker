/*                                                                                                                          
 * This software is free software; you can redistribute it and/or modify                                                     
 * it under the terms of the GNU General Public License as published by                                                     
 * the Free Software Foundation; either version 3 of the License, or                                                        
 * (at your option) any later version.                                                                                      
 *                                                                                                                          
 * This program is distributed in the hope that it will be useful, but                                                      
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY                                               
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License                                                  
 * for more details.                                                                                                        
 *                                                                                                                          
 * You should have received a copy of the GNU General Public License along                                                  
 * with this program; if not, write to the Free Software Foundation, Inc.,                                                  
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                                                    
 */
package org.ligi.android.bluetooth.device_picker;

import it.gerdavax.easybluetooth.RemoteDevice;
/**
 * class to represent a blueooth-device
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothDevice {
	
	private String addr=""; // mac address
	private String friendly_name=""; // human readable name
	private int seen_in_round=-1; // -1 -> not seen
	private boolean saved=false; // remember if it was saved 
	
	public BluetoothDevice(RemoteDevice rd,int act_scan_round) {
		if (rd!=null) {
			addr=""+rd.getAddress();
			setFriendlyName(rd.getFriendlyName());
		} else {
			addr="null";
			setFriendlyName("no name"); 
		}
		seen_in_round=act_scan_round;
	}
	
	public void setFriendlyName(String new_fn) {
		if (!((new_fn==null)||new_fn.equals("")))
			friendly_name=new_fn;
		else
		    friendly_name="no name (mac="+getAddr()+")";
				
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
	 * and updates seen_in_round if new val is bigger
	 * 
	 * @param bd
	 */
	public void updateFriendlyAndSeen(BluetoothDevice bd,int act_scan_round) {
		if (this.getFriendlyName().equals(""))
			this.friendly_name=bd.getFriendlyName();
		seen_in_round=Math.max(seen_in_round,act_scan_round);
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