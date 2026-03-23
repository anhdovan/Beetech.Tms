package beetech.app.core.dialog;

import android.widget.TextView;

public abstract class StringDialog extends BaseDialog {

	protected String mValue;
	protected String mOldValue;
	
	public StringDialog() {
		super();
		mValue = "";
	}
	
	public StringDialog(TextView view) {
		super(view);
		mValue = "";
	}

	public String getValue() {
		return mValue;
	}
	
	public void setValue(String value) {
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
		txtValue.setText(mValue);
	}
}
