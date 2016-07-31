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
package com.inc.automata.malawiscenery.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.inc.automata.malawiscenery.app.AppConst;
import com.inc.automata.malawiscenery.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PrefManager {

    private static final String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Shared Pref Editor
    Editor editor;

    // Context
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // sharedpref file name
    private static final String PREF_NAME = "Malawi Scenery";

    // Google's username
    private static final String KEY_GOOGLE_USERNAME = "google_username";

    // no of grid columns
    private static final String KEY_NO_OF_COLUMNS = "no_of_columns";

    // gallery directory name
    private static final String KEY_GALLERY_NAME = "gallery name";

    // gallery albums key
    private static final String KEY_ALBUMS = "albums";

    //key for recent pics
    private static final String KEY_RECENT_PICS = "recent_pics";

    //key for wallpaper service
    public static final String KEY_WALLPAPER_SERVICE = "wallpaper_service";

    //key for notifications
    private static final String KEY_NOTIFICATIONS = "notifications";

    //key for first run
    private static final String KEY_FIRST_RUN = "first_run";

    //key for number of days until update of wallpaper
    private static final String KEY_NUMBER_OF_DAYS = "num_of_days";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    // get set google username
    public void setGoogleUsername(String googleUsername) {
        editor = pref.edit();

        editor.putString(KEY_GOOGLE_USERNAME, googleUsername);

        // commit changes
        editor.commit();

    }

    public String getGoogleUserName() {
        return pref.getString(KEY_GOOGLE_USERNAME, AppConst.PICASA_USER);
    }

    // get set number of grid columns
    public void setNoOfGridColumns(int columns) {
        editor = pref.edit();

        editor.putInt(KEY_NO_OF_COLUMNS, columns);

        // save changes
        editor.commit();
    }

    public int getNoOfGridColumns() {
        return pref.getInt(KEY_NO_OF_COLUMNS, AppConst.NUM_OF_COLUMNS);
    }

    // get set gallery name
    public void setGalleryname(String galleryName) {
        editor = pref.edit();

        editor.putString(KEY_GALLERY_NAME, galleryName);

        // save changes
        editor.commit();

    }

    public String getGalleryName() {
        return pref.getString(KEY_GALLERY_NAME, AppConst.SDCARD_DIR_NAME);

    }

    // storing albums in shared preferences

    public void storeCategories(List<Category> albums) {
        editor = pref.edit();
        Gson gson = new Gson();

        Log.d(TAG, "Albums: " + gson.toJson(albums));

        editor.putString(KEY_ALBUMS, gson.toJson(albums));

        // save changes
        editor.commit();

    }

    // fetch albums from shared preferences,albums return in alphabetical order

    public List<Category> getCategories() {

        List<Category> albums = new ArrayList<Category>();

        if (pref.contains(KEY_ALBUMS)) {
            String json = pref.getString(KEY_ALBUMS, null);
            Gson gson = new Gson();
            Category[] albumArry = gson.fromJson(json, Category[].class);

            albums = Arrays.asList(albumArry);
            albums = new ArrayList<Category>(albums);
        } else
            return null;

        List<Category> allAlbums = albums;

        // sort the albums in alphabetical order
        Collections.sort(allAlbums, new Comparator<Category>() {
            public int compare(Category a1, Category a2) {
                return a1.getTitle().compareToIgnoreCase(a2.getTitle());
            }

        });
        return allAlbums;
    }

    public void setRecentPics(String recentPics) {
        editor = pref.edit();

        editor.putString(KEY_RECENT_PICS, recentPics);

        editor.commit();
    }

    public String getRecentPics() {
        return pref.getString(KEY_RECENT_PICS, null);
    }

    public void setWallpaperService(int wallpaperService) {
        editor = pref.edit();

        editor.putInt(KEY_WALLPAPER_SERVICE, wallpaperService);

        editor.commit();
    }

    public int getWallpaperService() {
        return pref.getInt(KEY_WALLPAPER_SERVICE, AppConst.WALLPAPER_ACTIVE);
    }

    public void setNotifications(int notifications) {
        editor = pref.edit();

        editor.putInt(KEY_NOTIFICATIONS, notifications);

        editor.commit();
    }

    public int getNotifications() {
        return pref.getInt(KEY_NOTIFICATIONS, AppConst.SERVICE_NOTIFICATION_ACTIVE);
    }

    public int getFirstRun() {
        return pref.getInt(KEY_FIRST_RUN, AppConst.FIRST_RUN);
    }

    public void setFirstRun(int run) {
        editor = pref.edit();

        editor.putInt(KEY_FIRST_RUN, run);

        editor.commit();
    }

    public int getNumOfDays() {
        return pref.getInt(KEY_NUMBER_OF_DAYS, AppConst.NUMBER_OF_DAYS);
    }

    public void setNumOfDays(int days) {
        editor = pref.edit();

        editor.putInt(KEY_NUMBER_OF_DAYS, days);

        editor.commit();
    }

    // compare album titles for
    public class CustomComparator implements Comparator<Category> {
        @Override
        public int compare(Category c1, Category c2) {
            return c1.getTitle().compareTo(c2.getTitle());
        }

    }

}
