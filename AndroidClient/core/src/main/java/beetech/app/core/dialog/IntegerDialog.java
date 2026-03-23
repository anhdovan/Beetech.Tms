package beetech.app.core.dialog;

import android.widget.TextView;

import java.util.Locale;

public abstract class IntegerDialog extends BaseDialog {

	protected int mValue;
	private int mOldValue;
	
	public IntegerDialog() {
		super();
		mValue = 0;
	}
	
	public IntegerDialog(TextView view) {
		super(view);
		mValue = 0;
	}
	
	public int getValue() {
		return mValue;
	}
	
	public void setValue(int value) {
		mValue = value;
		mOldValue = value;
	}

	public void restoreValue() {
		mValue = mOldValue;
		display();
	}
	
	@Override
	public void display() {
		
		if (txtValue == null)
			return;
		
		txtValue.setText(String.format(Locale.US, "%d", mValue));
	}
}
