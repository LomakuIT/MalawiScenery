/*
 * Copyright 2016 Lomaku Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inc.automata.malawiscenery.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.app.AppConst;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.fragments.GridFragment;
import com.inc.automata.malawiscenery.fragments.SubmitFeedbackFragment;
import com.inc.automata.malawiscenery.model.Category;
import com.inc.automata.malawiscenery.util.PrefManager;

import java.lang.reflect.Field;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GridFragment.OnFragmentInteractionListener {

    DrawerLayout drawer;//main app drawer
    NavigationView navigationView;//drawer list
    List<Category> albumsList;//list of albums plus IDs
    private static final String TAG = MainActivity.class.getSimpleName();//tag for logging
    private static final int MENU_GROUP = 0;//menu group ID
    //array for web links
    final String[] weblinksArray = {"http://www.kayamawa.com",
            "http://www.robinpopesafaris.net/malawi.php", "http://cawsmw.com",
            "http://www.amluking.com", "http://www.androidhive.info","http://www.mountmulanje.org.mw/"};

    //array for names for web links
    final String[] placeNamesArray = {"Kaya Mawa", "Robin Pope Safaris",
            "Central Africa Wilderness Safaris", "Otim Media Group (app icon)", "AndroidHive","Mulanje Mountain Conservation Trust"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        forceOverFlowMenu();//force overflow menu
        albumsList = AppController.getInstance().getPrefManager().getCategories();//populate list of albums

        //find navigation view and set listener
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu myMenu = navigationView.getMenu();
        for (int i = 0; i < albumsList.size(); i++) {
            //add menu items from results
            myMenu.add(MENU_GROUP, i, i, albumsList.get(i).getTitle()).setIcon(R.drawable.ic_location);
        }

        //find drawer layout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            onNavigationItemSelected(navigationView.getMenu().getItem(0));//select best of if app launched for first time
        }

        if (new PrefManager(this).getFirstRun() == AppConst.FIRST_RUN) { //if not first run the show message
            String message = "Welcome to Malawi Scenery. Enjoy app and make use of Settings and please submit feedback";
            new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(message).setIcon(R.mipmap.ic_launcher).
                    setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new PrefManager(getApplicationContext()).setFirstRun(AppConst.NOT_FIRST_RUN);//change to acknowledge first run
                            dialog.dismiss();
                        }
                    }).show();//show alert
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle overflow menu item clicks
        switch (item.getItemId()) {
            case R.id.action_email_us:
                showEmailUs();//show email intenet
                break;
            case R.id.action_links:
                String neutralButton = "Dismiss";

                AlertDialog.Builder alertLinks = new AlertDialog.Builder(this);

                // set title to links
                alertLinks
                        .setTitle(getResources().getString(R.string.action_links));
                // set neutral button
                alertLinks.setNeutralButton(neutralButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); // dismiss the dialog
                    }
                });

                // on click listener for the dialog list
                alertLinks.setItems(placeNamesArray, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // open the link
                        openLink(which);
                    }
                });

                alertLinks.show();// show the alert dialog

                break;
            case R.id.action_photo_creds:
                showAlertPhotoCreds();//show photo credits
                break;
            case R.id.action_share:
                showShare();//show share intent
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));//start settings activity
                break;

            case R.id.action_feeback:
                SubmitFeedbackFragment feedbackFragment = SubmitFeedbackFragment.newInstance();
                feedbackFragment.show(getSupportFragmentManager(), TAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {//implement all drawer item selected action here
        Log.d("NavigationItem ID: ", String.valueOf(item.getItemId()));
        Fragment fragment = fragment = GridFragment.newInstance(albumsList.get(item.getItemId()).getId());//get ID of clicked item
        setBarTitle(albumsList.get(item.getItemId()).getTitle());//set bar title according to album selected
        AppController.getInstance().trackEvent("album", "click", albumsList.get(item.getItemId()).getTitle());//track album that was selected

        if (fragment != null) {

            //transition to new fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void forceOverFlowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // presumably, not relevant
            //log in google analytics
            AppController.getInstance().trackException(e);
        }
    }

    private void showShare() {
        // intent to share
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // information to be shared
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                AppConst.SHARE_INFO);
        // choose sharing method
        startActivity(Intent.createChooser(shareIntent,
                "Choose how you want to share Malawi Scenery..."));
    }

    private void showEmailUs() {
        // start an email intent and feed it with email address and subject
        // only
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{getString(R.string.developer_email)});// developer email,please keep it as an array

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));

        try {
            // start the email activity
            startActivity(Intent.createChooser(emailIntent, "Email " + getString(R.string.app_name)
                    + "..."));

        } catch (android.content.ActivityNotFoundException e) {

            Toast.makeText(getApplicationContext(),
                    "No email clients found/configured: " + e.toString(),
                    Toast.LENGTH_LONG).show();
            Log.e("Email " + getString(R.string.app_name), "No email clients: " + e.toString());
            //log in google analytics
            AppController.getInstance().trackException(e);

        }
    }

    // show alert dialog for photo credits
    private void showAlertPhotoCreds() {

        // variable to hold information to be displayed on the alert dialog
        String photoCredsMessage = String
                .format("Kaya Mawa Lodge: %s\n\n"
                                + "Robin Pope Safaris: %s\n\n"
                                + "Central Africa Wilderness Safaris: %s\n\n"
                                + "Isaac Otim (icon rendering): %s\n\n Mulanje Mountain Conservation Trust: %s\n\n [Visit Links For Direct Page]",
                        "james@kayamawa.com", "info@robinpopesafaris.net",
                        "info@cawsmw.com", "izakotim@gmail.com","mmct@sdnp.org.mw");
        String neutralAlertTitle = "Dismiss";

        AlertDialog.Builder alertPhotoCreds = new AlertDialog.Builder(this);
        // set title,message and dialog button text
        alertPhotoCreds
                .setTitle(getResources().getString(R.string.action_photo_creds))
                .setMessage(photoCredsMessage)
                .setNeutralButton(neutralAlertTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();// dismiss the dialog
                            }
                        }

                );
        alertPhotoCreds.show();// show the alert dialog
    }

    protected void openLink(int position) {

        try {

            Intent openLink = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(weblinksArray[position]));// open
            // link
            // corresponding to the position

            startActivity(Intent.createChooser(openLink, "Open Link..."));// start
            // activity
            // chooser

        } catch (android.content.ActivityNotFoundException ex) {
            // no activity found to open link error
            Toast.makeText(getApplication(),
                    "Cannot open link: " + ex.toString(), Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "No activity for opening link");

        } catch (Exception ex) {
            // exception with opening link
            Toast.makeText(getApplication(),
                    "Cannot open link: " + ex.toString(), Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Cannot open link");

        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
