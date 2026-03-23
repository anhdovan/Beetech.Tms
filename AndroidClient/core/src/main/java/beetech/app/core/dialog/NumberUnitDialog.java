package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.unitech.lib.v2.lib.util.StringUtil;
import com.unitech.lib.v2.lib.util.diagnotics.EALog;

import java.util.Locale;

import beetech.app.rfidreader.R;

public class NumberUnitDialog extends IntegerDialog {

	private static final String TAG = NumberUnitDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private String mUnit;
	
	public NumberUnitDialog(String unit) {
		super();
		mUnit = unit;
	}

	public NumberUnitDialog(TextView view, String unit) {
		super(view);
		mUnit = unit;
	}

	public void setUnit(String unit) {
		mUnit = unit;
	}

	@Override
	public void display() {

		if (txtValue == null)
			return;

		txtValue.setText(String.format(Locale.US, "%d %s", mValue, mUnit));
	}

	@Override
	public void showDialog(Context context, String title, final IValueChangedListener changedListener,
			final ICancelListener cancelListener) {

		if (txtValue != null) {
			if (!txtValue.isEnabled())
				return;
		}

		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context, R.layout.dialog_num_unit, null);
		final EditText edtVal = (EditText) root.findViewById(R.id.value);
		TextView txtUnit = (TextView) root.findViewById(R.id.unit);
		txtUnit.setText(mUnit);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(StringUtil.isNullOrEmpty(title) ? "" : title);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				String value = edtVal.getText().toString();
				if (!StringUtil.isNullOrEmpty(value)) {
					mValue = StringUtil.toInteger(value);
					display();
					if (changedListener != null) {
						changedListener.onValueChanged(NumberUnitDialog.this);
					}					
				} else {
					EALog.e(TAG, "ERROR. showDialog().$PositiveButton.onClick() - Input value is unknown");
					if (cancelListener != null) {
						cancelListener.onCanceled(NumberUnitDialog.this);
					}	
				}

				EALog.i(TAG, INFO, "INFO. showDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				display();
				if (cancelListener != null) {
					cancelListener.onCanceled(NumberUnitDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
			}
		});

		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				display();
				if (cancelListener != null) {
					cancelListener.onCanceled(NumberUnitDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				edtVal.setText(String.format(Locale.US, "%d", mValue));
				edtVal.selectAll();
				edtVal.requestFocus();
				imm.showSoftInput(edtVal, InputMethodManager.SHOW_FORCED);
				EALog.i(TAG, INFO, "INFO. showDialog().onShow()");
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		EALog.i(TAG, INFO, "INFO. showDialog()");
	}
}
