package me.acristoffers.tracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


/*
this dummy activity provides an icon for the app
and allows PackageListActivity to use another icon
in the ActionBar (Problem solved: the app icon was
being used in the ActionBar when SearchWidget was active.
The ic_stat was desired. This class does just that. Most
code for it to work lives in the Manifest)
*/
public class Main extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, PackageListActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }
}
