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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.app.AppConst;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.model.Category;
import com.inc.automata.malawiscenery.services.WallpaperService;
import com.inc.automata.malawiscenery.util.GPSTracker;
import com.inc.automata.malawiscenery.util.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    //tags to retrieve JSON data
    private static final String TAG_FEED = "feed", TAG_ENTRY = "entry", TAG_GPHOTO_ID = "gphoto$id", TAG_T = "$t", TAG_ALBUM_TITLE = "title", TAG_NO_PHOTOS = "gphoto$numphotos";

    private static final int IMG_WIDTH = 350;
    private static final int IMG_HEIGHT = 350;

    //GPS tracker
    GPSTracker gps;

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // get primary email
    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            return "undefined";
        } else {
            return account.name;
        }
    }


    // get google email
    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if version is greater than Jelly Bean then hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);//set fullscreen activity by removing status bar colour
        }

        setContentView(R.layout.activity_splash);

        try {
            getSupportActionBar().hide();//hide action bar
        } catch (NullPointerException npEx) {
            Log.e(TAG, npEx.getMessage());
            AppController.getInstance().trackException(npEx);
        }
        //setting splash image
        ImageView imgSplash = (ImageView) findViewById(R.id.imgSplash);

        imgSplash.setImageBitmap(decodeSampledBitmapFromResource(getApplicationContext().getResources(), R.drawable.splash_screen2, IMG_WIDTH, IMG_HEIGHT));
        imgSplash.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //start tracing location
        gps = new GPSTracker(this);

        //Picasa request to get list of albums
        String url = AppConst.URL_PICASA_ALBUMS.replace("_PICASA_USER_", AppController.getInstance().getPrefManager().getGoogleUserName());//replace the picasa user string with an actual username

        Log.d(TAG, "Albums request url: " + url);//log request, remove for production

        //JSON object request via Volley
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Albums Response: " + response.toString()); //remove for production
                List<Category> albums = new ArrayList<>(); //create a list of albums
                try {
                    //parsing through the json response
                    JSONArray entry = response.getJSONObject(TAG_FEED).getJSONArray(TAG_ENTRY);

                    //loop through album nodes and add the to album list
                    for (int i = 0; i < entry.length(); i++) {
                        JSONObject albumObj = (JSONObject) entry.get(i);
                        //get album id
                        String albumId = albumObj.getJSONObject(TAG_GPHOTO_ID).getString(TAG_T);

                        //get album title
                        String albumTitle = albumObj.getJSONObject(TAG_ALBUM_TITLE).getString(TAG_T);

                        //number of photos in album
                        String albumNoOfPhotos = albumObj.getJSONObject(TAG_NO_PHOTOS).getString(TAG_T);

                        Category album = new Category();
                        album.setId(albumId);
                        album.setTitle(albumTitle);
                        album.setPhotoNo(albumNoOfPhotos);

                        //add album to list
                        albums.add(album);

                        Log.d(TAG, "Album Id: " + albumId + ", Album Title: " + albumTitle + ", Number: " + albumNoOfPhotos);
                    }//end for

                    //store albums in shared pref through appController
                    AppController.getInstance().getPrefManager().storeCategories(albums);

                    //start the main activity and call the finish method
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    //close the splash activity
                    finish();

                } catch (JSONException jEx) {
                    //add to google analytic
                    AppController.getInstance().trackException(jEx);
                    //show toast message
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_unknown_error), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Error: " + error.getMessage());
                AppController.getInstance().trackException(error);//add google analytic
                Toast.makeText(getApplicationContext(), getString(R.string.splash_error), Toast.LENGTH_LONG).show();//show toast

                //unable to fetch albums
                //check for existing Albums data in shared preferences
                if (AppController.getInstance().getPrefManager().getCategories() != null && AppController.getInstance().getPrefManager().getCategories().size() > 0) {
                    //start the main activity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    //close splash activity
                    finish();
                } else {
                    // Albums data not present in the shared preferences
                    // Launch settings activity, so that user can modify
                    // the settings

                    startActivity(new Intent(SplashActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
            }
        });
        // disable the cache for this request, so that it always fetches updated json
        jsonObjectReq.setShouldCache(false);

        // Making the request
        AppController.getInstance().addToRequestQueue(jsonObjectReq);
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish()");
        //add to data usage statistics
        String USAGE_URL = "http://apps.lomakuit.com/malawi_scenery/add_log.php";
        StringRequest usageRequest = new StringRequest(Request.Method.POST, USAGE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "usageResponse: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "usageError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //add variables for post
                Map<String, String> params = new HashMap<>();
                params.put(AppConst.KEY_EMAIL, getEmail(getApplicationContext()));
                params.put(AppConst.KEY_COORDINATES, getCoordinates());
                params.put(AppConst.KEY_DATE_NOW, newDate());
                params.put(AppConst.KEY_SERIAL_NUMBER, getUID());
                return params;
            }
        };
        //make request
        String KEY_USAGE_REQUEST = "usage_request";
        AppController.getInstance().addToRequestQueue(usageRequest, KEY_USAGE_REQUEST);

        //check if wallpaper service is running
        try {
            checkWallPaperService();
        } catch (GooglePlayServicesNotAvailableException gpsEx) {
            AppController.getInstance().trackException(gpsEx);
            Log.e(TAG, "google play services not available " + gpsEx.getMessage());
        } catch (Exception ex) {
            AppController.getInstance().trackException(ex);
            Log.e(TAG, "service start exception " + ex.getMessage());
        }
        super.finish();
    }

    //get coordinates
    private String getCoordinates() {

        if (gps.canGetLocation()) {

            double longitude = gps.getLongitude();
            double latitude = gps.getLatitude();

            return String.format("%f,%f",
                    longitude, latitude);
        }

        return "undefined";
    }

    //retrieve device UID
    private String getUID() {

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    //retrieve date.now
    private String newDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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

        }
    }
}
