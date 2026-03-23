package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.types.AlgorithmType;

import beetech.app.rfidreader.R;

public class InventoryAlgorithmDialog implements OnClickListener {

	private static final String TAG = InventoryAlgorithmDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private Context mContext;

	private LinearLayout linearMinMaxQ;
	
	private TextView txtAlgorithm;
	private TextView txtStartQ;
	private TextView txtMinQ;
	private TextView txtMaxQ;

	private EnumListDialog dlgAlgorithm;
	private NumberDialog dlgStartQ;
	private NumberDialog dlgMinQ;
	private NumberDialog dlgMaxQ;

	private AlgorithmType mAlgorithm;
	private int mStartQ;
	private int mMinQ;
	private int mMaxQ;

	public InventoryAlgorithmDialog() {
		mAlgorithm = AlgorithmType.FixedQ;
		mStartQ = 0;
		mMinQ = 0;
		mMaxQ = 15;
	}

	public AlgorithmType getAlgorithm() {
		return mAlgorithm;
	}

	public void setAlgorithm(AlgorithmType algorithm) {
		mAlgorithm = algorithm;
	}

	public int getStartQ() {
		return mStartQ;
	}

	public void setStartQ(int value) {
		mStartQ = value;
	}

	public int getMinQ() {
		return mMinQ;
	}

	public void setMinQ(int value) {
		mMinQ = value;
	}

	public int getMaxQ() {
		return mMaxQ;
	}

	public void setMaxQ(int value) {
		mMaxQ = value;
	}

	public void showDialog(Context context, final IValueChangedListener changedListener, final BaseDialog.ICancelListener cancelListener) {

		mContext = context;
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context, R.layout.dialog_inventory_algorithm, null);

		linearMinMaxQ = (LinearLayout) root.findViewById(R.id.linear_min_max_q);
		
		txtAlgorithm = (TextView) root.findViewById(R.id.algorithm);
		txtAlgorithm.setOnClickListener(this);
		txtStartQ = (TextView) root.findViewById(R.id.start_q);
		txtStartQ.setOnClickListener(this);
		txtMinQ = (TextView) root.findViewById(R.id.min_q);
		txtMinQ.setOnClickListener(this);
		txtMaxQ = (TextView) root.findViewById(R.id.max_q);
		txtMaxQ.setOnClickListener(this);

		dlgAlgorithm = new EnumListDialog(txtAlgorithm, AlgorithmType.values());
		dlgAlgorithm.setValue(mAlgorithm);
		dlgStartQ = new NumberDialog(txtStartQ);
		dlgStartQ.setValue(mStartQ);
		dlgMinQ = new NumberDialog(txtMinQ);
		dlgMinQ.setValue(mMinQ);
		dlgMaxQ = new NumberDialog(txtMaxQ);
		dlgMaxQ.setValue(mMaxQ);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.inventory_algorithm);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mAlgorithm = (AlgorithmType) dlgAlgorithm.getValue();
				mStartQ = dlgStartQ.getValue();
				mMinQ = dlgMinQ.getValue();
				mMaxQ = dlgMaxQ.getValue();

				if (changedListener != null) {
					changedListener.onValueChanged(InventoryAlgorithmDialog.this);
				}
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

				dlgAlgorithm.display();
				dlgStartQ.display();
				dlgMinQ.display();
				dlgMaxQ.display();

				if(dlgAlgorithm.getValue() == AlgorithmType.FixedQ) {
					linearMinMaxQ.setVisibility(View.INVISIBLE);
				} else {
					linearMinMaxQ.setVisibility(View.VISIBLE);
				}
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		EALog.i(TAG, INFO, "INFO. showDialog()");
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.algorithm) {
			dlgAlgorithm.showDialog(mContext, R.string.algorithm, new BaseDialog.IValueChangedListener() {
				@Override
				public void onValueChanged(BaseDialog dialog) {
					if (dlgAlgorithm.getValue() == AlgorithmType.FixedQ) {
						linearMinMaxQ.setVisibility(View.INVISIBLE);
					} else {
						linearMinMaxQ.setVisibility(View.VISIBLE);
					}
				}
			});
		} else if (id == R.id.start_q) {
			dlgStartQ.showDialog(mContext, R.string.start_q);
		} else if (id == R.id.min_q) {
			dlgMinQ.showDialog(mContext, R.string.min_q);
		} else if (id == R.id.max_q) {
			dlgMaxQ.showDialog(mContext, R.string.max_q);
		}
	}

	// ------------------------------------------------------------------------
	// Declare Interface IValueChangedListener
	// ------------------------------------------------------------------------

	public interface IValueChangedListener {
		void onValueChanged(InventoryAlgorithmDialog dialog);
	}
}
