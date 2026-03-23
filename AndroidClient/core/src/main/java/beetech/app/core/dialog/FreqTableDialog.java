package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.params.FreqTableList;
import com.unitech.lib.v2.rfid.types.GlobalBandType;

import java.util.ArrayList;

import beetech.app.rfidreader.R;

public class FreqTableDialog {

	private static final String TAG = FreqTableDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private FreqTableList mTable;
	private GlobalBandType mGlobalBand;
	
	private ListView mList;
	private FreqTableAdapter mAdapter;
	
	public FreqTableDialog() {
		mTable = new FreqTableList(0, 0);
		mGlobalBand = GlobalBandType.Unknown;
	}

	public FreqTableDialog(GlobalBandType globalBand) {
		mTable = new FreqTableList(0, 0);
		mGlobalBand = globalBand;
	}
	
	public void setGlobalBand(GlobalBandType globalBand) {
		mGlobalBand = globalBand;
	}
	
	public FreqTableList getTable() {
		return mTable;
	}

	public void setTable(FreqTableList table) {
		mTable = table;
	}

	private boolean isRegionJapan(GlobalBandType global) {
		return ( (mGlobalBand == GlobalBandType.Japan125mW)
				|| (mGlobalBand == GlobalBandType.Japan250mW) 
				|| (mGlobalBand == GlobalBandType.Japan1W));
	}
	
	public void showDialog(Context context, final IValueChangedListener changedlistener, final BaseDialog.ICancelListener cancelListener) {
		
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context, R.layout.dialog_list_view, null);
		mList = (ListView) root.findViewById(R.id.list);

		boolean isCheckEnabled = false;
		if (isRegionJapan(mGlobalBand)) {
			isCheckEnabled = true;
		} 
		
		mAdapter = new FreqTableAdapter(context, mTable.getFreqNames(), isCheckEnabled);  
		mList.setAdapter(mAdapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.frequency);
		builder.setView(root);
		
		if (isRegionJapan(mGlobalBand)) {
			builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					for (int i = 0; i < mTable.getCount(); i++) {
						mTable.setUsed(i, mAdapter.getChecked(i));
					}
					
					if (changedlistener != null) {
						changedlistener.onValueChanged(FreqTableDialog.this);
					}
					EALog.i(TAG, INFO, "INFO. showDialog().$PositiveButton.onClick()");
				}
			});

			builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (cancelListener != null) {
						cancelListener.onCanceled(null);
					}
					EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
				}
			});
		} else {
			builder.setNegativeButton(R.string.action_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (cancelListener != null) {
						cancelListener.onCanceled(null);
					}
					EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
				}
			});
		}

		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (cancelListener != null) {
					cancelListener.onCanceled(null);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().onCancel()");
			}
		});

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				for (int i = 0; i < mTable.getCount(); i++) {
					mAdapter.setChecked(i, mTable.isUsed(i));
				}

				mAdapter.notifyDataSetChanged();
				EALog.i(TAG, INFO, "INFO. showDialog().onShow()");
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		EALog.i(TAG, INFO, "INFO. showDialog()");
	}

	private class FreqTableAdapter extends BaseAdapter {

		private boolean mIsCheckEnabled;
		private LayoutInflater mInflater;
		private ArrayList<FreqItem> mItems;
		
		private FreqTableAdapter(Context context, String[] names, boolean isCheckEnabled) {
			super();
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mItems = new ArrayList<FreqItem>();
			for (int i = 0; i < names.length; i++)
				mItems.add(new FreqItem(names[i]));
			mIsCheckEnabled = isCheckEnabled;
		}

		public void setChecked(int position, boolean enabled) {
			mItems.get(position).IsUsed = enabled;
		}

		public boolean getChecked(int position) {
			return mItems.get(position).IsUsed;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public String getItem(int position) {
			return mItems.get(position).Name;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FreqViewHolder holder = null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_frequency_list, parent, false);
				holder = new FreqViewHolder(convertView , mIsCheckEnabled);
			} else {
				holder = (FreqViewHolder)convertView.getTag();
			}
			holder.display(position, mItems.get(position));
			return convertView;
		}

		private class FreqViewHolder implements OnCheckedChangeListener {
			private int mPosition;
			private CheckBox chkUsed;
			private TextView txtFreqName;

			private FreqViewHolder(View parent) {
				mPosition = -1;
				chkUsed = (CheckBox) parent.findViewById(R.id.used);
				chkUsed.setEnabled(true);
				chkUsed.setOnCheckedChangeListener(this);
				txtFreqName = (TextView) parent.findViewById(R.id.freq_name);
				txtFreqName.setEnabled(true);
				parent.setTag(this);
			}
			
			private FreqViewHolder(View parent , boolean isEnabled) {
				mPosition = -1;
				chkUsed = (CheckBox) parent.findViewById(R.id.used);
				chkUsed.setEnabled(isEnabled);
				chkUsed.setOnCheckedChangeListener(this);
				txtFreqName = (TextView) parent.findViewById(R.id.freq_name);
				txtFreqName.setEnabled(isEnabled);
				parent.setTag(this);
			}

			private void display(int position, FreqItem item) {
				mPosition = position;
				chkUsed.setChecked(item.IsUsed);
				txtFreqName.setText(item.Name);
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mItems.get(mPosition).IsUsed = isChecked;
				//EALog.i(TAG, INFO, "INFO. $FreqViewHolder.onCheckedChanged() - [%d, %s]", mPosition, isChecked);
			}
		}

		private class FreqItem {
			public String Name;
			public boolean IsUsed;

			private FreqItem(String name) {
				Name = name;
			}
		}

	}
	
	// ------------------------------------------------------------------------
	// Declare Interface IValueChangedListener
	// ------------------------------------------------------------------------

	public interface IValueChangedListener {
		void onValueChanged(FreqTableDialog dialog);
	}
}
