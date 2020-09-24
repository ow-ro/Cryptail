package ca.owro.cryptail;

import android.os.Bundle;

import ca.owro.cryptail.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("About");

    }

    @Override
    int getLayoutId() {
        return R.layout.about_main;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.action_about;
    }
}
