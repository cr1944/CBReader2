package cheng.app.cnbeta;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;

public class OpenSourceLicensesFragment extends DialogFragment {
    public OpenSourceLicensesFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        WebView webView = new WebView(getActivity());
        webView.loadUrl("file:///android_asset/licenses.html");

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.about_licenses)
                .setView(webView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }
}
