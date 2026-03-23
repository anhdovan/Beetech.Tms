package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.unitech.lib.v2.lib.util.diagnotics.EALog;

import beetech.app.rfidreader.R;

public class HexStringDialog extends StringDialog {

	private static final String TAG = HexStringDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	public HexStringDialog() {
		super();
	}

	public HexStringDialog(TextView view) {
		super(view);
	}

	@Override
	public void showDialog(Context context, String title, final IValueChangedListener changedListener,
			final ICancelListener cancelListener) {

		if (txtValue != null) {
			if (!txtValue.isEnabled())
				return;
		}

		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context, R.layout.dialog_hex_string, null);
		final EditText edtVal = (EditText) root.findViewById(R.id.value);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				mValue = edtVal.getText().toString();
				display();
				if (changedListener != null) {
					changedListener.onValueChanged(HexStringDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				if (cancelListener != null) {
					cancelListener.onCanceled(HexStringDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
			}
		});

		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				imm.hideSoftInputFromWindow(edtVal.getWindowToken(), 0);
				if (cancelListener != null) {
					cancelListener.onCanceled(HexStringDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				edtVal.setText(mValue);
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
