
package cheng.app.cnbeta;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View v = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView versionText = (TextView) v.findViewById(R.id.dialog_about_version);
        LinearLayout author = (LinearLayout) v.findViewById(R.id.author);
        versionText.setText(versionName);
        author.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://plus.google.com/u/0/114474183775310756510"));
                startActivity(i);
            }
        });
        TextView github = (TextView) v.findViewById(R.id.github);
        github.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/cr1944/CBReader2"));
                startActivity(i);
            }
        });
        TextView opensource = (TextView) v.findViewById(R.id.opensource);
        opensource.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                HelpUtils.showOpenSourceLicenses(getActivity());
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create();
    }
}
