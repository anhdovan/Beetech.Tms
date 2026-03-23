package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.types.BankType;

import beetech.app.rfidreader.R;

public class MaskBankDialog extends BaseDialog {

	private static final String TAG = MaskBankDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private final String[] mBanks;
	private BankType mBank;

	public MaskBankDialog() {
		super();
		mBank = BankType.EPC;
		mBanks = new String[] { BankType.EPC.toString(), BankType.TID.toString(), BankType.User.toString() };
	}

	public MaskBankDialog(TextView view) {
		super(view);
		mBank = BankType.EPC;
		mBanks = new String[] { BankType.EPC.toString(), BankType.TID.toString(), BankType.User.toString() };
	}

	public BankType getBank() {
		return mBank;
	}

	public void setBank(BankType bank) {
		mBank = bank;
	}

	@Override
	public void display() {
		if (txtValue == null)
			return;

		txtValue.setText(mBank.toString());
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
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_single_choice, mBanks);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = list.getCheckedItemPosition();
				mBank = BankType.valueOf(position + 1);
				display();
				if (changedListener != null) {
					changedListener.onValueChanged(MaskBankDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (cancelListener != null) {
					cancelListener.onCanceled(MaskBankDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().$NegativeButton.onClick()");
			}
		});

		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (cancelListener != null) {
					cancelListener.onCanceled(MaskBankDialog.this);
				}
				EALog.i(TAG, INFO, "INFO. showDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = mBank.getCode() - 1;
				if (position < 0)
					position = 0;
				list.setItemChecked(position, true);
				list.setSelectionFromTop(position, 0);
				EALog.i(TAG, INFO, "INFO. showDialog().onShow()");
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		EALog.i(TAG, INFO, "INFO. showDialog()");
	}
}
