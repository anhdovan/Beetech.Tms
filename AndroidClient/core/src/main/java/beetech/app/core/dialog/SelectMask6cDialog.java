package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.rfid.params.SelectMask6cParam;
import com.unitech.lib.v2.rfid.types.Mask6cTarget;

import beetech.app.rfidreader.R;

public class SelectMask6cDialog implements OnClickListener {

	private static final String TAG = SelectMask6cDialog.class.getSimpleName();
	private static final int INFO = EALog.L2;

	private static final int NIBLE_SIZE = 4;
	
	private SelectMask6cParam mItem;

	private Context mContext;
	
	private TextView txtTarget;
	private TextView txtAction;
	private TextView txtBank;
	private TextView txtOffset;
	private TextView txtPattern;
	private TextView txtLength;
	
	private EnumListDialog dlgTarget;
	private MaskActionDialog dlgAction;
	private MaskBankDialog dlgBank;
	private NumberUnitDialog dlgOffset;
	private HexStringDialog dlgPattern;
	private NumberUnitDialog dlgLength;
	
	public SelectMask6cDialog() {
		
		mItem = new SelectMask6cParam();
	}
	
	public SelectMask6cParam getItem() {
		return mItem;
	}
	
	public void setItem(SelectMask6cParam item) {
		if(item == null) {
			EALog.e(TAG, "ERROR. setItem() - item is null !!!!");
			return;
		}
		mItem.copy(item);
	}

	public void showDialog(Context context, final IValueChangedListener listener) {
		
		mContext = context;
		String unit = context.getResources().getString(R.string.unit_bit);
		
		LinearLayout root = (LinearLayout)LinearLayout.inflate(context, R.layout.dialog_select_mask_6c, null);
		
		txtTarget = (TextView)root.findViewById(R.id.target);
		txtTarget.setOnClickListener(this);
		txtAction = (TextView)root.findViewById(R.id.action);
		txtAction.setOnClickListener(this);
		txtBank = (TextView)root.findViewById(R.id.bank);
		txtBank.setOnClickListener(this);
		txtOffset = (TextView)root.findViewById(R.id.offset);
		txtOffset.setOnClickListener(this);
		txtPattern = (TextView)root.findViewById(R.id.pattern);
		txtPattern.setOnClickListener(this);
		txtLength = (TextView)root.findViewById(R.id.length);
		txtLength.setOnClickListener(this);
		
		dlgTarget = new EnumListDialog(txtTarget, Mask6cTarget.values());
		dlgAction = new MaskActionDialog(txtAction);
		dlgBank = new MaskBankDialog(txtBank);
		dlgOffset = new NumberUnitDialog(txtOffset, unit);
		dlgPattern = new HexStringDialog(txtPattern);
		dlgLength = new NumberUnitDialog(txtLength, unit);

		dlgTarget.setValue(mItem.getTarget());
		dlgAction.setTarget((Mask6cTarget)dlgTarget.getValue());
		dlgAction.setAction(mItem.getAction());
		dlgBank.setBank(mItem.getBank());
		dlgOffset.setValue(mItem.getOffset());
		dlgPattern.setValue(mItem.getPattern());
		dlgLength.setValue(mItem.getLength());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.title_select_mask_6c_item);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				mItem.setTarget((Mask6cTarget)dlgTarget.getValue());
				mItem.setAction(dlgAction.getAction());
				mItem.setBank(dlgBank.getBank());
				mItem.setOffset(dlgOffset.getValue());
				mItem.setPattern(dlgPattern.getValue());
				mItem.setLength(dlgLength.getValue());
				
				if (listener != null) {
					listener.onValueChanged(SelectMask6cDialog.this);
				}
			}
		});
		builder.setNegativeButton(R.string.action_cancel, null);
		builder.setCancelable(false);
//		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				
				dlgTarget.display();
				dlgAction.display();
				dlgBank.display();
				dlgOffset.display();
				dlgPattern.display();
				dlgLength.display();
				
			}
		});
		dialog.show();
		
		EALog.i(TAG, INFO, "INFO. showDialog()");
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.target) {
			dlgTarget.showDialog(mContext, R.string.mask_target, new BaseDialog.IValueChangedListener() {
				@Override
				public void onValueChanged(BaseDialog dialog) {
					dlgAction.setTarget((Mask6cTarget) dlgTarget.getValue());
					dlgAction.display();
				}
			});
		} else if (id == R.id.action) {
			dlgAction.setTarget((Mask6cTarget) dlgTarget.getValue());
			dlgAction.showDialog(mContext, R.string.mask_action);
		} else if (id == R.id.bank) {
			dlgBank.showDialog(mContext, R.string.bank);
		} else if (id == R.id.offset) {
			dlgOffset.showDialog(mContext, R.string.offset);
		} else if (id == R.id.pattern) {
			dlgPattern.showDialog(mContext, R.string.mask_pattern, new BaseDialog.IValueChangedListener() {
				@Override
				public void onValueChanged(BaseDialog dialog) {
					String pattern = dlgPattern.getValue();
					dlgLength.setValue(pattern.length() * NIBLE_SIZE);
					dlgLength.display();
				}
			});
		} else if (id == R.id.length) {
			dlgLength.showDialog(mContext, R.string.length);
		}
	}


	// ------------------------------------------------------------------------
	// Declare Interface IValueChangedListener
	// ------------------------------------------------------------------------
	
	public interface IValueChangedListener {
		void onValueChanged(SelectMask6cDialog dialog);
	}
}
