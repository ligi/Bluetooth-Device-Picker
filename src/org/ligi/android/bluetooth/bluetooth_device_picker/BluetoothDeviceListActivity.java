package org.ligi.android.bluetooth.bluetooth_device_picker;

import java.util.HashMap;

import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.ReadyListener;
import it.gerdavax.easybluetooth.RemoteDevice;
import it.gerdavax.easybluetooth.ScanListener;

import org.ligi.tracedroid.Log;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity to scan for bluetooth devices and list them
 * 
 * TODO: - persist found ones  
 *       - sort by FoundCount
 *       - mark found ones in list vs persisted ones
 *       - mark recently found ones found ones
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothDeviceListActivity extends ListActivity implements OnCancelListener, OnClickListener,  OnItemClickListener {
	
	private myArrayAdapter arrayAdapter;
	private ProgressDialog progress_dialog;
	public final static String RESULT_TAG_BT_ADDR="ADDR";
	public final static String RESULT_TAG_BT_FRIENDLYNAME="FRIENDLYNAME";
	private boolean stopped=false;

	public int scan_round=1;
	
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
	
	private class myArrayAdapter extends ArrayAdapter<BluetoothDevice>{

		private HashMap<String,Integer> mac2id; // String is mac
		
		private Context context;
		
		public myArrayAdapter(Context context, int textViewResourceId) {
		
			super(context, textViewResourceId);
			mac2id=new HashMap<String,Integer>();
			this.context=context;
		}

		@Override
		public void add(BluetoothDevice object) {
			if (mac2id.containsKey(object.getAddr())) {
				getItem(mac2id.get(object.getAddr())).updateFriendlyAndSeen(object,scan_round);
				super.notifyDataSetChanged();
			}
			else			
				super.add(object);
			
			mac2id.put(object.getAddr(),this.getCount()-1);
			
		}

		@Override
		public BluetoothDevice getItem(int position) {
			// TODO Auto-generated method stub
			return super.getItem(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=vi.inflate(android.R.layout.two_line_list_item, null); 
            TextView label=(TextView)row.findViewById(android.R.id.text1); 
			label.setText(getItem(position).getFriendlyName() );
			label.setTextSize(TypedValue.COMPLEX_UNIT_MM , 12f); // 1.2cm
            label=(TextView)row.findViewById(android.R.id.text2);
            label.setTextSize(TypedValue.COMPLEX_UNIT_MM , 6.0f); // 6mm -> so ~2cm together -> easy touchable 
			label.setText(getItem(position).getAddr() + "--" + getItem(position).getSeenRound());
	        return row;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.i("starting scan activity");

	    
	    
		progress_dialog=new ProgressDialog(this);
		progress_dialog.setMessage("Switching on Bluetooth ..");
		progress_dialog.setTitle("Bluetooth");
		progress_dialog.setCancelable(false);
		progress_dialog.setOnCancelListener(this);
		progress_dialog.setButton("Cancel", this);
		progress_dialog.show();

		LocalDevice.getInstance().init(this, new myReadyListener());
		
		arrayAdapter = new myArrayAdapter(this, android.R.layout.simple_list_item_1);
		
		ListView lv = this.getListView();
		lv.setAdapter(arrayAdapter);
		lv.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		Intent result_intent=new Intent();
		result_intent.putExtra(RESULT_TAG_BT_ADDR, ""+((RemoteDevice)(arg0.getAdapter().getItem(pos))).getAddress());
		result_intent.putExtra(RESULT_TAG_BT_FRIENDLYNAME, ""+((RemoteDevice)(arg0.getAdapter().getItem(pos))).getFriendlyName());
		this.setResult(Activity.RESULT_OK, result_intent);
		finish();
	}

	 @Override
	protected void onDestroy() {
		 stopped=true; // to prevent scan restart 
		 LocalDevice.getInstance().stopScan();
		 progress_dialog.dismiss();
		 super.onDestroy();
	}
		
	private class myReadyListener extends ReadyListener {
		@Override
		public void ready() {
			LocalDevice.getInstance().scan(new myScanListener());
			progress_dialog.setMessage("Waiting for at least one device");
		}
	}
	
	private class myScanListener extends ScanListener {

		@Override
		public void deviceFound(RemoteDevice remote_device) {
			progress_dialog.hide();
			Log.i("Bluetooth Device found: friendly_name=" + remote_device.getFriendlyName() + " / mac=" + remote_device.getAddress() + " / rssi: " + remote_device.getRSSI() );
			arrayAdapter.add(new BluetoothDevice(remote_device,scan_round));;
		}

		@Override
		public void scanCompleted() {
			Log.i("Bluetooth Scan Completed");
			if (stopped) {
				Toast.makeText(BluetoothDeviceListActivity.this, "Disable Bluetooth Scan", Toast.LENGTH_SHORT).show(); 	
			} else {
				Toast.makeText(BluetoothDeviceListActivity.this, "New Scan", Toast.LENGTH_SHORT).show();
				if (arrayAdapter.getCount()==0) 
					progress_dialog.setMessage("No Device found - trying again");
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