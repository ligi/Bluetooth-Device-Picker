package org.ligi.android.bluetooth.bluetooth_device_picker;

import java.util.HashMap;

import org.ligi.tracedroid.logging.Log;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		private Context myContext;
		private int last_seen_round=-1;
		
		public BluetoothArrayAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mac2id=new HashMap<String,Integer>();
			this.myContext=context;
		}
		
		public static BluetoothArrayAdapter getInstance() {
			return instance;
		}

		public static void construct_instance(Context c,int resid) {
			if (instance==null)
				instance=new BluetoothArrayAdapter(c,resid);
		}

		public void dump_hash(HashMap<String,Integer> hm) {
			for (String key : hm.keySet())
				Log.i("found " + key + "->" + hm.get(key).intValue());
		}
		
		@Override
		public void add(BluetoothDevice object) {
			last_seen_round=Math.max(last_seen_round, object.getSeenRound());
			if (mac2id.containsKey(object.getAddr())) {
				Log.i("found update" + mac2id.get(object.getAddr()));
				dump_hash(mac2id);
				getItem(mac2id.get(object.getAddr())).updateFriendlyAndSeen(object,object.getSeenRound());
				super.notifyDataSetChanged();
			}
			else {
				super.add(object);
				mac2id.put(object.getAddr(),this.getCount()-1);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BluetoothDevice bd=getItem(position);
			
			LinearLayout row=new LinearLayout(myContext);
			row.setOrientation(LinearLayout.VERTICAL);
			
			
			LinearLayout name_and_icon=new LinearLayout(myContext);
			name_and_icon.setOrientation(LinearLayout.HORIZONTAL);
			
			TextView friendly_name_tv=new TextView(myContext);
			
			friendly_name_tv.setText(bd.getFriendlyName() );
			friendly_name_tv.setTextSize(TypedValue.COMPLEX_UNIT_MM , 12f); // 1.2cm
			name_and_icon.addView(friendly_name_tv);
			ImageView view_img=new ImageView(myContext);
			view_img.setImageResource(android.R.drawable.ic_menu_view);
			
			if (bd.getSeenRound()>last_seen_round-1)
				name_and_icon.addView(view_img);
			
			row.addView(name_and_icon);
			TextView addr_tv=new TextView(myContext);
			
			addr_tv.setText(bd.getAddr());
			addr_tv.setTextSize(TypedValue.COMPLEX_UNIT_MM , 6f); // 1.2cm
			
			row.addView(addr_tv);
			
	        return row;
		}
	}
	