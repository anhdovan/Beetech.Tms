package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.unitech.lib.v2.lib.util.StringUtil;
import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.params.PowerRange;

import java.util.ArrayList;
import java.util.Locale;

import beetech.app.rfidreader.R;

public class PowerGainDialog extends IntegerDialog {

	private static final String TAG = PowerGainDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private PowerRange mRange;
	private String mUnit;

	public PowerGainDialog() {
		super();
		mRange = null;
		mUnit = "dBm";
	}

	public PowerGainDialog(TextView view) {
		super(view);
		mRange = null;
		mUnit = "dBm";
	}

	public void setPowerGainRange(PowerRange range) {
		mRange = new PowerRange(range.getMin(), range.getMax());
	}
	
	public void setUnit(String unit) {
		mUnit = unit;
	}
	
	@Override
	public void display() {
		if (txtValue == null) {
			return;
		}
		txtValue.setText(String.format(Locale.US, "%.1f %s", (double) mValue / 10.0, mUnit));
	}

	@Override
	public void showDialog(Context context, String title, final IValueChangedListener changedListener,
			final ICancelListener cancelListener) {

		if (txtValue != null) {
			if (!txtValue.isEnabled())
				return;
		}

		LinearLayout root = (LinearLayout) LinearLayout.inflate(context, R.layout.dialog_list_view, null);
		final ListView list = (ListView) root.findViewById(R.id.list);
		final PowerRangeAdapter adapter = new PowerRangeAdapter(context, mRange);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(StringUtil.isNullOrEmpty(title) ? "" : title);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = list.getCheckedItemPosition();
				mValue = adapter.getItemValue(position);
				display();
				if (changedListener != null) {
					changedListener.onValueChanged(PowerGainDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (cancelListener != null) {
					cancelListener.onCanceled(PowerGainDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
			}
		});

		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (cancelListener != null) {
					cancelListener.onCanceled(PowerGainDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getItemPosition(mValue);
				list.setItemChecked(position, true);
				list.setSelectionFromTop(position, 0);
				EALog.i(TAG, INFO, "INFO. showDialog().onShow()");
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		EALog.i(TAG, INFO, "INFO. showDialog()");
	}

	// ------------------------------------------------------------------------
	// Declare Class PowerRange Adapter
	// ------------------------------------------------------------------------

	private class PowerRangeAdapter extends BaseAdapter {
		private static final int MAX_LIST_COUNT = 20;

		private LayoutInflater mInflater;
		private ArrayList<PowerRangeItem> mList;

		// --------------------------------------------------------------------
		// Constructor
		// --------------------------------------------------------------------

		public PowerRangeAdapter(Context context, PowerRange value) {
			super();

			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mList = new ArrayList<PowerRangeItem>();
			int count = 0;

			for (int i = value.getMax(); i >= value.getMin() && count < MAX_LIST_COUNT; i -= 10, count++) {
				mList.add(new PowerRangeItem(i));
			}
		}

		public int getItemValue(int position) {
			return mList.get(position).getValue();
		}

		public int getItemPosition(int value) {
			for (int i = 0; i < mList.size(); i++) {
				if (mList.get(i).getValue() == value) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position).getValue();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PowerRangeViewHolder holder = null;

			if (null == convertView) {
				convertView = mInflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
				holder = new PowerRangeViewHolder(convertView);
			} else {
				holder = (PowerRangeViewHolder) convertView.getTag();
			}
			holder.setItem(mList.get(position));
			return convertView;
		}
		// ------------------------------------------------------------------------
		// Internal PowerRangViewHolder Class
		// ------------------------------------------------------------------------
		private class PowerRangeViewHolder {
			private TextView txtValue;

			public PowerRangeViewHolder(View parent) {
				txtValue = (TextView) parent.findViewById(android.R.id.text1);
				parent.setTag(this);
			}

			public void setItem(PowerRangeItem item) {
				txtValue.setText(item.toString());
			}
		}

		// ------------------------------------------------------------------------
		// Internal PowerRangItem Class
		// ------------------------------------------------------------------------
		private class PowerRangeItem {
			private int mValue;

			private PowerRangeItem(int value) {
				mValue = value;
			}

			public int getValue() {
				return mValue;
			}

			public String toString() {
				return String.format(Locale.US, "%.1f %s", mValue / 10.0F, mUnit);
			}
		}

	}
}
