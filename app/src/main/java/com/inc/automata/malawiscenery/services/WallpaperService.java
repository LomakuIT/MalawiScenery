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
package com.inc.automata.malawiscenery.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.activities.SplashActivity;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.fragments.GridFragment;
import com.inc.automata.malawiscenery.util.ConnectionDetector;
import com.inc.automata.malawiscenery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Random;

@SuppressWarnings("deprecated")
public class WallpaperService extends GcmTaskService {

    public static final int NUM_SECONDS = 86400; //number of seconds in a day +"&imgmax=d"
    public static final int FLEX = 10800;//flex for when to set the wallpaper. 3 hours
    public static final int UNIQUE_ID = 1738;
    private static final String URL_BEST_OF = "https://picasaweb.google.com/data/feed/api/user/107165993798824467749/albumid/6297833507758039057?alt=json&imgmax=1600";//has best of pictures
    private static final String TAG = WallpaperService.class.getSimpleName();//name of class

    public WallpaperService() {
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d(TAG, "I am running");

        if(!new ConnectionDetector(getApplicationContext()).hasInternet()){//if it has no internet
            Log.d(TAG,"no internet detected");
            return ConnectionResult.NETWORK_ERROR;
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, URL_BEST_OF, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "best of json reponse: "
                        + response.toString());
                try {
                    // Parsing the json response
                    JSONArray entry = response.getJSONObject(
                            GridFragment.TAG_FEED).getJSONArray(GridFragment.TAG_ENTRY);

                    //randomly generate image to display as wallpaper
                    JSONObject photoObj = (JSONObject) entry
                            .get(new Random().nextInt(entry.length()));
                    JSONArray mediacontentArray = photoObj
                            .getJSONObject(GridFragment.TAG_MEDIA_GROUP)
                            .getJSONArray(GridFragment.TAG_MEDIA_CONTENT);

                    if (mediacontentArray.length() > 0) {
                        JSONObject mediaObj = (JSONObject) mediacontentArray
                                .get(0);

                        String url = mediaObj
                                .getString(GridFragment.TAG_IMG_URL);

                        // image full resolution widht and height
                        final int width = mediaObj.getInt(GridFragment.TAG_IMG_WIDTH);
                        final int height = mediaObj.getInt(GridFragment.TAG_IMG_HEIGHT);

                        Log.d(TAG, "Full resolution image. url: "
                                + url + ", w: " + width
                                + ", h: " + height);

                        Log.d(TAG, url);
                        try {
                            //make call to get image bitmap
                            Bitmap img = new setWallpaper().execute(url).get();

                            if (width == 0 || height == 0)
                                return;

                            int sHeight = 0;
                            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                            if (android.os.Build.VERSION.SDK_INT >= 13) {
                                Display display = manager.getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                sHeight = size.y;
                            } else {
                                Display display = manager.getDefaultDisplay();
                                sHeight = display.getHeight();
                            }

                            int new_width = (int) Math.floor((double) width * (double) sHeight
                                    / (double) height);

                            Log.d(TAG, "Fullscreen image new dimensions: w = " + new_width
                                    + ", h = " + sHeight);

                            //resize and set wallpaper
                            Bitmap newBitmap = getResizedBitmap(img, sHeight, new_width);
                            Log.d("dimensions", "height: " + newBitmap.getHeight() + " width:" + newBitmap.getWidth());
                            new Utils(getApplicationContext()).setAsWallpaper(newBitmap);

                            displayNotification();//display notification
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                            AppController.getInstance().trackException(ex);
                        }

                        Log.d(TAG, "wallpaper changed");
                    }

                } catch (JSONException e) {
                    AppController.getInstance().trackException(e);//track error
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppController.getInstance().trackException(error);//track error through analytics
                Log.e(TAG, "Volley Error: " + error.getMessage());
            }
        });

        if (AppController.getInstance().getRequestQueue().getCache() != null) {
            // remove the url from the cache
            AppController.getInstance().getRequestQueue().getCache().remove(URL_BEST_OF);
        }
        // disable cache for url so that if fetches updated json
        jsonObjReq.setShouldCache(false);

        // adding request to request cache
        AppController.getInstance().addToRequestQueue(jsonObjReq);

        return ConnectionResult.SUCCESS;
    }

    private void displayNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
        notification.setAutoCancel(true);//allow to be cancelled
        notification.setSmallIcon(R.drawable.ic_notification);//set small icon
        notification.setTicker("Wallpaper changed");//set ticker
        notification.setWhen(System.currentTimeMillis());//tells you when
        notification.setContentTitle("Malawi Scenery");//title of notification
        notification.setContentText("Wallpaper has been changed by Malawi Scenery");//content text of notification

        //set pending intent for notification click
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //set flags and show notification
        notification.build().flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(UNIQUE_ID, notification.build());
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;

        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation

        Matrix matrix = new Matrix();

        // resize the bit map

        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }

    private class setWallpaper extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                //make call to URL to fetch new image and set as wallpaper
                HttpURLConnection connection = (HttpURLConnection) new java.net.URL(params[0])
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                AppController.getInstance().trackException(e);
                return null;
            }
        }
    }
}
