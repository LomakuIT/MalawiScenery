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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.app.AppConst;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.services.WallpaperService;
import com.inc.automata.malawiscenery.util.PrefManager;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    EditText inputGooglerUsername, inputGalleryFolder, inputColumns, inputDays;
    SwitchCompat switchNotifications, switchWallpaper;
    Button btnSave;
    PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pref = new PrefManager(getApplicationContext());//get shared preference manager
        assignLayouts();//assign xml layouts
        getValues();//get values from shared preferences
        hideSoftKeyboard();//hide keyboard on launch
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//set home button
        } catch (NullPointerException npEx) {
            Log.e(TAG, npEx.getMessage());
            AppController.getInstance().trackException(npEx);
        }
    }

    private void assignLayouts() {
        inputGooglerUsername = (EditText) findViewById(R.id.input_google_username);
        inputGooglerUsername.setEnabled(false);//make google username unchangeable
        inputGalleryFolder = (EditText) findViewById(R.id.input_gallery_folder);
        inputColumns = (EditText) findViewById(R.id.input_num_columns);
        inputDays = (EditText) findViewById(R.id.input_num_days);

        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        //make sure we find the view, check its settings then set listener to avoid unnecessary re processing of statements
        switchNotifications = (SwitchCompat) findViewById(R.id.switchNotifications);
        switchNotifications.setChecked((pref.getNotifications() == AppConst.SERVICE_NOTIFICATION_ACTIVE) ? true : false);
        switchNotifications.setOnCheckedChangeListener(this);
        switchWallpaper = (SwitchCompat) findViewById(R.id.switchWallpaper);
        switchWallpaper.setChecked((pref.getWallpaperService() == AppConst.WALLPAPER_ACTIVE) ? true : false);
        switchWallpaper.setOnCheckedChangeListener(this);
    }

    private void getValues() {
        inputGooglerUsername.setText(pref.getGoogleUserName());
        inputGalleryFolder.setText(pref.getGalleryName());
        inputColumns.setText(String.valueOf(pref.getNoOfGridColumns()));
        inputDays.setText(String.valueOf(pref.getNumOfDays()));
    }

    public void checkWallPaperService() throws GooglePlayServicesNotAvailableException {

        PrefManager pref = new PrefManager(getApplicationContext());

        if (pref.getWallpaperService() == AppConst.WALLPAPER_ACTIVE) {//if wallpaper is to be activated then activate if not already activated

            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {//now that GP services are available start task
                //define the task and its properties
                Task wallpaperTask = new PeriodicTask.Builder().setService(WallpaperService.class).setPeriod(new PrefManager(getApplicationContext()).getNumOfDays() * WallpaperService.NUM_SECONDS).setFlex(WallpaperService.FLEX).setTag(PrefManager.KEY_WALLPAPER_SERVICE).setPersisted(true).build();
                GcmNetworkManager.getInstance(getApplicationContext()).schedule(wallpaperTask);//schedule the wallpaper task to run
                AppController.getInstance().trackEvent(WallpaperService.class.getSimpleName(), "start", "wallpaper");//track in analytics
                Log.d("wallpaper service", "started");
            } else {
                //throw exception that google play services are not available
                throw new GooglePlayServicesNotAvailableException(2);
            }

        } else if (pref.getWallpaperService() == AppConst.WALLPAPER_INACTIVE) {
            //cancel all scheduled tasks
            GcmNetworkManager.getInstance(getApplicationContext()).cancelAllTasks(WallpaperService.class);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.switchWallpaper:
                Log.d(TAG, "switchWallpaper" + " : " + String.valueOf(isChecked));
                //change value in shared preference
                if (isChecked) {
                    pref.setWallpaperService(AppConst.WALLPAPER_ACTIVE);
                    inputDays.setEnabled(true);//enable the number of days
                } else {
                    pref.setWallpaperService(AppConst.WALLPAPER_INACTIVE);
                    inputDays.setEnabled(false);//disable the number of days
                }
                //check status based on what has been changed
                try {
                    checkWallPaperService();
                } catch (GooglePlayServicesNotAvailableException gpEx) {
                    AppController.getInstance().trackException(gpEx);//track exception
                }
                break;
            case R.id.switchNotifications:
                Log.d(TAG, "switchNotifications" + " : " + String.valueOf(isChecked));
                //change status based on what has been received
                if (isChecked) {
                    pref.setNotifications(AppConst.SERVICE_NOTIFICATION_ACTIVE);//activate notifications
                } else {
                    pref.setNotifications(AppConst.SERVICE_NOTIFICATION_INACTIVE);//deactivate notifications
                }
                break;
        }

    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //check for home button press
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();//call backpres
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                //try to save values

                try {
                    //check if number of days is correct
                    if (inputDays.getText().toString().length() == 0 || inputDays.getText().toString().equals("0")) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_enter_num_of_days),
                                Toast.LENGTH_LONG).show();
                        return;//do nothing
                    } else {
                        //save and start wallpaper service
                        pref.setNumOfDays(Integer.valueOf(inputDays.getText().toString()));
                        checkWallPaperService();
                    }
                    //check if gallery is correct
                    if (inputGalleryFolder.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_enter_google_username),
                                Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        //save new folder
                        pref.setGalleryname(inputGalleryFolder.getText().toString());
                    }
                    //check number of grid columns allowed
                    if (inputColumns.getText().toString().length() == 0 || inputColumns.getText().toString().equals("0")) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_enter_valid_grid_columns),
                                Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        pref.setNoOfGridColumns(Integer.valueOf(inputColumns.getText().toString()));
                    }

                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
                    onBackPressed();//call backpress

                } catch (Exception ex) {
                    AppController.getInstance().trackException(ex);
                    Log.e(TAG, ex.getMessage());
                }
                break;
        }
    }
}
