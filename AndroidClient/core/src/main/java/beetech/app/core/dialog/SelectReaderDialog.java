package beetech.app.core.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import beetech.app.rfidreader.R;


public class SelectReaderDialog extends StringDialog {

    public SelectReaderDialog(TextView view) {
        super(view);
    }

    @Override
    public void showDialog(Context context, String title,
                           final IValueChangedListener changedListener,
                           final ICancelListener cancelListener) {

        String[] readerOptions = context.getResources().getStringArray(R.array.suppotedReaderClasses);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, readerOptions);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mValue = readerOptions[which];
                mOldValue = mValue;
                display();

                if (changedListener != null) {
                    changedListener.onValueChanged(SelectReaderDialog.this);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (cancelListener != null) {
                cancelListener.onCanceled(SelectReaderDialog.this);
            }
        });

        builder.show();
    }
}
