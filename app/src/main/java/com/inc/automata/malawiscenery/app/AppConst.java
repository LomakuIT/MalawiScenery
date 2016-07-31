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
package com.inc.automata.malawiscenery.app;

public class AppConst {

    // Number of columns of Grid View
    // by default 2 but user can configure this in settings activity
    public static final int NUM_OF_COLUMNS = 2;

    // Gridview image padding
    public static final int GRID_PADDING = 4; // in dp

    // Gallery directory name to save wallpapers
    public static final String SDCARD_DIR_NAME = "Malawi_Scenery";

    // Picasa/Google web album username
    public static final String PICASA_USER = "107165993798824467749";

    // Public albums list url
    public static final String URL_PICASA_ALBUMS = "https://picasaweb.google.com/data/feed/api/user/_PICASA_USER_?kind=album&alt=json";

    // Picasa album photos url
    public static final String URL_ALBUM_PHOTOS = "https://picasaweb.google.com/data/feed/api/user/_PICASA_USER_/albumid/_ALBUM_ID_?alt=json";

    public static final String SHARE_INFO = "Witness the beautiful scenery from Malawi...download Malawi Scenery app from the Play Store for free " +
            "at this link: https://goo.gl/Du2jEE";

    public static final String URL_PRIVACY_POLICY="http://lomakuit.com/privacy_policy.html";

    //default notification service
    public static final int SERVICE_NOTIFICATION_INACTIVE = 0;
    public static final int SERVICE_NOTIFICATION_ACTIVE = 1;

    //first run
    public static final int FIRST_RUN = 0;
    public static final int NOT_FIRST_RUN = 1;

    //values for whether wallpaper is running or not
    public static final int WALLPAPER_ACTIVE = 1;
    public static final int WALLPAPER_INACTIVE = 0;


    //default number of days
    public static final int NUMBER_OF_DAYS = 7;
    //keys for the POST variables being received in PHP script
    public static final String KEY_EMAIL = "email";
    public static final String KEY_DATE_NOW = "datenow";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_SERIAL_NUMBER = "serial_number";
}
