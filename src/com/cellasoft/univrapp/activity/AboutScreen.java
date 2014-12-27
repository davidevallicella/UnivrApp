package com.cellasoft.univrapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.UIUtils;

public class AboutScreen extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        init();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
        super.onPostCreate(savedInstanceState);
    }

    private void init() {
        UIUtils.keepScreenOn(this, true);
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.about_menu, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_github:
                showSource();
                return true;
            default:
                return (super.onOptionsItemSelected(item));
        }
    }

    private void showSource() {
        UIUtils.safeOpenLink(this, new Intent(Intent.ACTION_VIEW, Uri.parse(Config.Links.GITHUB)));
    }
}