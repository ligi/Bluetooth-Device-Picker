package org.ligi.android.bluetooth.bluetooth_device_picker;

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
import android.view.Menu;
import android.view.MenuItem;
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
 *       - Continuous scan with marking the recent found ones
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothDeviceListActivity extends ListActivity implements OnCancelListener, OnClickListener,  OnItemClickListener {
	
	private static final int MENU_SCAN = 0;
	private myArrayAdapter arrayAdapter;
	private ProgressDialog progress_dialog;
	private boolean scanning=false;
	public final static String RESULT_TAG_BT_ADDR="ADDR";
	public final static String RESULT_TAG_BT_FRIENDLYNAME="FRIENDLYNAME";
	
	private class myArrayAdapter extends ArrayAdapter<RemoteDevice>{

		private Context context;
		
		public myArrayAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.context=context;
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
			label.setText(getItem(position).getAddress() + getItem(position).getRSSI());
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
		LocalDevice.getInstance().stopScan();
		progress_dialog.dismiss();
		super.onDestroy();
	}
		
	private class myReadyListener extends ReadyListener {
		@Override
		public void ready() {
			scanning=true;
			LocalDevice.getInstance().scan(new myScanListener());
			progress_dialog.setMessage("Waiting for at least one device");
		}
	}

	/* Creates the menu items */
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
	    
		if (!scanning) 
			menu.add(0,MENU_SCAN,0,"Scan again").setIcon(android.R.drawable.ic_menu_rotate);
		else 
			menu.add(0,MENU_SCAN,0,"Stop Scan").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case MENU_SCAN:
	    		if (scanning) 
	    			LocalDevice.getInstance().stopScan();
	    		
	    		progress_dialog.show();
	    		arrayAdapter.clear();
	    		scanning=true;
	    		LocalDevice.getInstance().scan(new myScanListener());	
	    		break;
	    }
	    return false;
	}

	private class myScanListener extends ScanListener {

		@Override
		public void deviceFound(RemoteDevice remote_device) {
			progress_dialog.hide();
			Log.i("Bluetooth Device found: friendly_name=" + remote_device.getFriendlyName() + " / mac=" + remote_device.getAddress() + " / rssi: " + remote_device.getRSSI() );
			arrayAdapter.add(remote_device);
		}

		@Override
		public void scanCompleted() {
			Log.i("Bluetooth Scan Completed");
			Toast.makeText(BluetoothDeviceListActivity.this, "Scan Completed", Toast.LENGTH_SHORT).show(); 

			if (arrayAdapter.getCount()>0) {
				scanning=false;
			}
			else {
				progress_dialog.setMessage("No Device found - trying again");
				LocalDevice.getInstance().scan(new myScanListener());	
			}
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		finish();
	}
}