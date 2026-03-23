package beetech.app.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.unitech.lib.v2.transport.types.ConnectType;

import java.util.ArrayList;

import beetech.app.core.data.DeviceItem;
import beetech.app.core.utils.ResUtil;
import beetech.app.rfidreader.R;

public class DeviceListBluetoothAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<DeviceItem> mList;
	
	public DeviceListBluetoothAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<DeviceItem>();
	}

	public synchronized void add(ConnectType type, String name, String address) {
		DeviceItem item = new DeviceItem(type, name, address);
		if (mList.contains(item))
			return;
		mList.add(item);
		notifyDataSetChanged();
	}
	
	public synchronized void clear() {
		mList.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		int size = 0;
		synchronized(this) {
			size = mList.size(); 
		}
		return size;
	}

	@Override
	public DeviceItem getItem(int position) {
		DeviceItem item = null;
		synchronized(this){
			item = mList.get(position);
		}
		return item;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		DeviceListViewHolder holder;
		DeviceItem item = null;
		
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_device_bluetooth, parent, false);
			holder = new DeviceListViewHolder(convertView);
		} else {
			holder = (DeviceListViewHolder)convertView.getTag();
		}
		
		synchronized(this){
			item = mList.get(position);
		}
		holder.displayItem(item);	
		
		return convertView;
	}

	// ------------------------------------------------------------------------
	// DeviceListViewHolder
	// ------------------------------------------------------------------------

	private class DeviceListViewHolder {
	
		private ImageView imgType;
		private TextView txtName;
		private TextView txtAddress;
		
		private DeviceListViewHolder(View parent) {
			
			imgType = (ImageView)parent.findViewById(R.id.device_type);
			txtName = (TextView)parent.findViewById(R.id.device_name);
			txtAddress = (TextView)parent.findViewById(R.id.device_address);

			parent.setTag(this);
		}
		
		private void displayItem(DeviceItem item) {
			if(item != null) {
				imgType.setImageResource(ResUtil.getProductImage(item.getType()));
				if(item.getName() != null)
					txtName.setText(item.getName());
				if(item.getAddress() != null)
					txtAddress.setText(item.getAddress());
			}
		}
	}
}
