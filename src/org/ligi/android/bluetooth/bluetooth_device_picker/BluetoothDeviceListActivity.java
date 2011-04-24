package org.ligi.android.bluetooth.bluetooth_device_picker;

import org.ligi.tracedroid.TraceDroid;
import org.ligi.tracedroid.logging.Log;
import org.ligi.tracedroid.sending.TraceDroidEmailSender;

import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.ReadyListener;
import it.gerdavax.easybluetooth.RemoteDevice;
import it.gerdavax.easybluetooth.ScanListener;
import android.app.Activity;
import android.app.ListActivity;
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
public class BluetoothDeviceListActivity extends ListActivity implements OnCancelListener, OnClickListener,  OnItemClickListener {
	
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
		progress_dialog.setButton(getString(R.string.Cancel), this);

		LocalDevice.getInstance().init(this, new myReadyListener());
		
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
		 LocalDevice.getInstance().stopScan();
		 progress_dialog.dismiss();
		 super.onDestroy();
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