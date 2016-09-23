package greenwich.pointr;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;

public class SettingsDialogError extends DialogFragment {

    public void emptyList(Context context){

            new AlertDialog.Builder(context)
                    .setTitle("Invalid Selection")
                    .setMessage("Please select at least one category")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_delete)
                    .setCancelable(false)
                    .show();
        }


}
