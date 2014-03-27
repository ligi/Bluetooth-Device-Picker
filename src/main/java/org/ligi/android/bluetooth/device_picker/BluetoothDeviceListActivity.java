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

import org.ligi.android.bluetooth.device_picker.R;
import org.ligi.tracedroid.TraceDroid;
import org.ligi.tracedroid.logging.Log;
import org.ligi.tracedroid.sending.TraceDroidEmailSender;

import com.actionbarsherlock.app.SherlockListActivity;

import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.ReadyListener;
import it.gerdavax.easybluetooth.RemoteDevice;
import it.gerdavax.easybluetooth.ScanListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity to scan for bluetooth devices and list them
 * 
 * TODO: - store SelectedCount & sort by SelectedCount
 *       - drop devices ( long press )
 *       - help menu
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothDeviceListActivity extends SherlockListActivity implements OnCancelListener, OnClickListener,  OnItemClickListener {
	
	//private BluetoothArrayAdapter arrayAdapter;
	private ProgressDialog progress_dialog;
	public final static String RESULT_TAG_BT_ADDR="ADDR";
	public final static String RESULT_TAG_BT_FRIENDLYNAME="FRIENDLYNAME";
	private boolean stopped=false;
	public int scan_round=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Log.setTAG("BluetoothDevicePicker");
	    TraceDroid.init(this);
	    TraceDroidEmailSender.sendStackTraces("ligi@ligi.de", this);
	    
	    Log.i("starting scan activity");
	    
		progress_dialog=new ProgressDialog(this);
		progress_dialog.setMessage(getString(R.string.switching_on_bt));
		progress_dialog.setTitle(R.string.bluetooth);
		progress_dialog.setCancelable(false);
		progress_dialog.setOnCancelListener(this);
		progress_dialog.setButton(DialogInterface.BUTTON_NEUTRAL,getString(R.string.Cancel), this);

		try {
			LocalDevice.getInstance().init(this, new myReadyListener());
		} catch (Exception e) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.bt_error_topic)
				.setMessage(R.string.bt_err_txt)
				.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
					}
				})
				.show();
			
			return;
		}
		
		BluetoothArrayAdapter.construct_instance(this, android.R.layout.simple_list_item_1);
		this.getListView().setAdapter(BluetoothArrayAdapter.getInstance());
		
		this.getListView().setOnItemClickListener(this);
		
		for (String key : getSavedDevicesSharedPreferences().getAll().keySet())
			BluetoothArrayAdapter.getInstance().add(new BluetoothDevice(getSavedDevicesSharedPreferences().getString(key,""),key,true));
		
		if (BluetoothArrayAdapter.getInstance().getCount()<1)
			progress_dialog.show();
	}

	public SharedPreferences getSavedDevicesSharedPreferences() {
		return this.getSharedPreferences("saved_bt_devices", 0);
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		stopped=true; // to prevent scan restart 
		BluetoothDevice bd=(BluetoothDevice)arg0.getAdapter().getItem(pos);
		SharedPreferences sp=getSavedDevicesSharedPreferences();
		if (!sp.contains(bd.getAddr()))
			sp.edit().putString(bd.getAddr(), bd.getFriendlyName()).commit();
		
		Intent result_intent=new Intent();
		result_intent.putExtra(RESULT_TAG_BT_ADDR, bd.getAddr());
		result_intent.putExtra(RESULT_TAG_BT_FRIENDLYNAME, bd.getFriendlyName());
		this.setResult(Activity.RESULT_OK, result_intent);
		finish();
	}

	 @Override
	protected void onStop() {
		 stopped=true; // to prevent scan restart
		 //TODO investigate why this caused problems with leaking rec's - imho bug in lib .. 
		 //LocalDevice.getInstance().stopScan(); 
		 progress_dialog.dismiss();
		 super.onStop();
	}
		
	private class myReadyListener extends ReadyListener {
		@Override
		public void ready() {
			LocalDevice.getInstance().scan(new myScanListener());
			progress_dialog.setMessage(getString(R.string.initial_wait));
		}
	}
	
	private class myScanListener extends ScanListener {

		@Override
		public void deviceFound(RemoteDevice remote_device) {
			progress_dialog.hide();
			Log.i("Bluetooth Device found - friendly_name=" + remote_device.getFriendlyName() + " / mac=" + remote_device.getAddress() + " / rssi: " + remote_device.getRSSI() );
			BluetoothArrayAdapter.getInstance().add(new BluetoothDevice(remote_device,scan_round));;
		}

		@Override
		public void scanCompleted() {
			Log.i("Bluetooth Scan Completed");
			if (stopped) {
				Toast.makeText(BluetoothDeviceListActivity.this, R.string.disable_bt_scan, Toast.LENGTH_SHORT).show(); 	
			} else {
				Toast.makeText(BluetoothDeviceListActivity.this, R.string.new_scan, Toast.LENGTH_SHORT).show();
				if (BluetoothArrayAdapter.getInstance().getCount()==0) 
					progress_dialog.setMessage(getString(R.string.no_device_found));
				LocalDevice.getInstance().scan(new myScanListener());
				scan_round++;
			}
		}
	}
	
	public void onCancel(DialogInterface dialog) {
		finish();
	}

	public void onClick(DialogInterface dialog, int which) {
		finish();
	}
}