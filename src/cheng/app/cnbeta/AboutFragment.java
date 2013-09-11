
package cheng.app.cnbeta;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cheng.app.cnbeta.util.HelpUtils;

public class AboutFragment extends DialogFragment {
    private static final String VERSION_NA = "N/A";

    public AboutFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (NameNotFoundException e) {
            versionName = VERSION_NA;
        }

        SpannableStringBuilder aboutBody = new SpannableStringBuilder();
        aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));

        SpannableString licensesLink = new SpannableString(getString(R.string.about_licenses));
        licensesLink.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                HelpUtils.showOpenSourceLicenses(getActivity());
            }
        }, 0, licensesLink.length(), 0);
        aboutBody.append("\n\n");
        aboutBody.append(licensesLink);

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_about, null);
        aboutBodyView.setText(aboutBody);
        aboutBodyView.setMovementMethod(new LinkMovementMethod());

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.action_abouts)
                .setView(aboutBodyView)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create();
    }
}
