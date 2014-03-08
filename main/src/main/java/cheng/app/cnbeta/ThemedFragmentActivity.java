package cheng.app.cnbeta;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import cheng.app.cnbeta.util.Utils;

public class ThemedFragmentActivity extends ActionBarActivity {

    private int theme = 0;


    @Override
    protected void onResume() {
        super.onResume();


        if (theme == Utils.getAppTheme(getApplicationContext())) {

        } else {
            reload();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            theme = Utils.getAppTheme(getApplicationContext());
        } else {
            theme = savedInstanceState.getInt("theme");
        }
        setTheme(theme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void reload() {

        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
