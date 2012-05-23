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

import java.util.HashMap;

import org.ligi.android.bluetooth.device_picker.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
		private int last_seen_round=-1;
		
		public BluetoothArrayAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mac2id=new HashMap<String,Integer>();
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
			last_seen_round=Math.max(last_seen_round, object.getSeenRound());
			if (mac2id.containsKey(object.getAddr())) {
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
			
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout row=(LinearLayout)inflater.inflate(R.layout.row, null);
			
			TextView friendly_name_tv=(TextView)row.findViewById(android.R.id.text1);
			friendly_name_tv.setText(bd.getFriendlyName() );
			
			TextView addr_tv=(TextView)row.findViewById(android.R.id.text2);
			addr_tv.setText(bd.getAddr());

			
			if (!bd.isSaved())
				row.findViewById(R.id.saved).setVisibility(View.GONE);
			
			if (bd.getSeenRound()<=Math.max(last_seen_round-2,0))
				row.findViewById(R.id.visible).setVisibility(View.GONE);
			
	        return row;
		}
	}
	