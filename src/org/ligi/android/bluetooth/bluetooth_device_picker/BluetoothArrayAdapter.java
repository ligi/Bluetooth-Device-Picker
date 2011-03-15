package org.ligi.android.bluetooth.bluetooth_device_picker;

import java.util.HashMap;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * class to Adapter for the Bluetooth Device ListView
 * 
 * @author ligi ( aka: Marcus Bueschleb | mail: ligi at ligi dot de )
 *
 * Licence: GPLv3  
 */
public class BluetoothArrayAdapter extends ArrayAdapter<BluetoothDevice>{

		private static BluetoothArrayAdapter instance=null;
		private HashMap<String,Integer> mac2id; // String is mac
		private Context context;
		
		public BluetoothArrayAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mac2id=new HashMap<String,Integer>();
			this.context=context;
		}
		
		public static BluetoothArrayAdapter getInstance() {
			return instance;
		}

		public static void construct_instance(Context c,int resid) {
			if (instance==null)
				instance=new BluetoothArrayAdapter(c,resid);
		}

		@Override
		public void add(BluetoothDevice object) {
			if (mac2id.containsKey(object.getAddr())) {
				getItem(mac2id.get(object.getAddr())).updateFriendlyAndSeen(object,object.getSeenRound());
				super.notifyDataSetChanged();
			}
			else			
				super.add(object);
			
			mac2id.put(object.getAddr(),this.getCount()-1);
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
	