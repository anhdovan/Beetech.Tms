package beetech.app.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.params.SelectMask6cParam;
import com.unitech.lib.v2.rfid.types.Mask6cTarget;

import java.util.ArrayList;
import java.util.Locale;

import beetech.app.rfidreader.R;

public class SelectMask6cAdapter extends BaseAdapter {

	private static final String TAG = SelectMask6cAdapter.class.getSimpleName();

	private static final int MAX_INDEX = 8;

	private final String[] mSelectAction;
	private final String[] mSessionAction;

	private LayoutInflater mInflater;
	private ArrayList<SelectMask6cParam> mList;

	public SelectMask6cAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<SelectMask6cParam>();
		for (int i = 0; i < MAX_INDEX; i++) {
			mList.add(new SelectMask6cParam());
		}
		mSelectAction = context.getResources().getStringArray(R.array.mask_6c_select_action);
		mSessionAction = context.getResources().getStringArray(R.array.mask_6c_session_action);
	}
	
	public synchronized void update(int index, SelectMask6cParam item) {
		if (index < 0 || index >= MAX_INDEX) {
			EALog.e(TAG, "ERROR. update(%d, [%s]) - Failed to out of range index");
			return;
		}
		mList.get(index).copy(item);
	}

	public synchronized void clear() {
		for (int i = 0; i < MAX_INDEX; i++) {
			mList.get(i).clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int size = 0;
		synchronized(this){
			size = mList.size();
		}
		return size;
	}

	@Override
	public SelectMask6cParam getItem(int position) {
		SelectMask6cParam param = null;
		synchronized(this){
			param = mList.get(position);
		}
		return param;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SelectMask6cViewHolder holder = null;

		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_select_mask_6c, parent, false);
			holder = new SelectMask6cViewHolder(convertView);
		} else {
			holder = (SelectMask6cViewHolder) convertView.getTag();
		}
		
		synchronized(mList){
			holder.display(position);	
		}
		
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Class SelectMask6cViewHolder
	// ------------------------------------------------------------------------

	private class SelectMask6cViewHolder implements OnCheckedChangeListener {

		private int mPosition;
		private CheckBox chkUsed;
		private TextView txtTarget;
		private TextView txtAction;
		private TextView txtBank;
		private TextView txtOffset;
		private TextView txtLength;
		private TextView txtPattern;

		public SelectMask6cViewHolder(View parent) {

			mPosition = -1;
			chkUsed = (CheckBox) parent.findViewById(R.id.used);
			chkUsed.setOnCheckedChangeListener(this);
			
			txtTarget = (TextView) parent.findViewById(R.id.target);
			txtAction = (TextView) parent.findViewById(R.id.action);
			txtBank = (TextView) parent.findViewById(R.id.bank);
			txtOffset = (TextView) parent.findViewById(R.id.offset);
			txtLength = (TextView) parent.findViewById(R.id.length);
			txtPattern = (TextView) parent.findViewById(R.id.pattern);

			parent.setTag(this);
		}

		public void display(int position) {
			mPosition = position;
			SelectMask6cParam item = mList.get(mPosition);
			chkUsed.setChecked(item.isUsed());
			txtTarget.setText(item.getTarget().toString());
			if (item.getTarget() == Mask6cTarget.SL)
				txtAction.setText(mSelectAction[item.getAction().getCode()]);
			else
				txtAction.setText(mSessionAction[item.getAction().getCode()]);
			txtBank.setText(item.getBank().toString());
			txtOffset.setText(String.format(Locale.US, "%d bit", item.getOffset()));
			txtLength.setText(String.format(Locale.US, "%d bit", item.getLength()));
			txtPattern.setText(item.getPattern());
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			SelectMask6cParam item = mList.get(mPosition);
			item.setUsed(isChecked);
		}
	}

}
