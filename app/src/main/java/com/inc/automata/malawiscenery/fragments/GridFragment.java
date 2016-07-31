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
package com.inc.automata.malawiscenery.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.activities.FullScreenActivity;
import com.inc.automata.malawiscenery.app.AppConst;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.helper.GridViewAdapter;
import com.inc.automata.malawiscenery.model.Wallpaper;
import com.inc.automata.malawiscenery.util.PrefManager;
import com.inc.automata.malawiscenery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class GridFragment extends Fragment {

    private static final String ALBUM_ID = "album_id";//tag for the albumId being passed
    private static final String TAG = GridFragment.class.getSimpleName();
    private Utils utils;
    private GridViewAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    private String selectedAlbumId;
    private List<Wallpaper> photosList;
    private ProgressBar pbLoader;
    private PrefManager pref;

    // picasa JSON response node keys
    public static final String TAG_FEED = "feed", TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url",
            TAG_IMG_WIDTH = "width", TAG_IMG_HEIGHT = "height", TAG_ID = "id",
            TAG_T = "$t";

    private OnFragmentInteractionListener mListener;

    public GridFragment() {
        // Required empty public constructor
    }

    public static GridFragment newInstance(String albumId) {
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putString(ALBUM_ID, albumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        photosList = new ArrayList<>();
        pref = new PrefManager(getActivity());

        //getting album ID of the item in navigation drawer, if null then it was recently added
        if (getArguments().getString(ALBUM_ID) != null) {
            selectedAlbumId = getArguments().getString(ALBUM_ID);
            Log.d(TAG, "selected album id: " + getArguments().getString(ALBUM_ID));
        }

        //prepare URL
        String url = null;

        // Selected an album, replace the Album Id in the url
        url = AppConst.URL_ALBUM_PHOTOS.replace("_PICASA_USER_",
                pref.getGoogleUserName()).replace("_ALBUM_ID_",
                selectedAlbumId);

        Log.d(TAG, "Final request url: " + url);

        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        //hide the grid view and showing loader image before making http request
        gridView = (GridView) rootView.findViewById(R.id.grid_view);
        gridView.setVisibility(View.GONE);
        pbLoader = (ProgressBar) rootView.findViewById(R.id.pbLoader);
        pbLoader.setVisibility(View.VISIBLE);

        utils = new Utils(getActivity());

        //check to see if device has internet
        if (hasInternet(getActivity())) {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "List of photos json reponse: "
                            + response.toString());
                    try {
                        // Parsing the json response
                        JSONArray entry = response.getJSONObject(
                                TAG_FEED).getJSONArray(TAG_ENTRY);

                        // looping through each photo and adding it to
                        // list
                        // data set
                        for (int i = 0; i < entry.length(); i++) {
                            JSONObject photoObj = (JSONObject) entry
                                    .get(i);
                            JSONArray mediacontentArray = photoObj
                                    .getJSONObject(TAG_MEDIA_GROUP)
                                    .getJSONArray(TAG_MEDIA_CONTENT);

                            if (mediacontentArray.length() > 0) {
                                JSONObject mediaObj = (JSONObject) mediacontentArray
                                        .get(0);

                                String url = mediaObj
                                        .getString(TAG_IMG_URL);

                                String photoJson = photoObj
                                        .getJSONObject(TAG_ID)
                                        .getString(TAG_T)
                                        + "&imgmax=d";

                                Log.d(TAG + " json", photoJson.toString());

                                int width = mediaObj
                                        .getInt(TAG_IMG_WIDTH);
                                int height = mediaObj
                                        .getInt(TAG_IMG_HEIGHT);

                                Wallpaper p = new Wallpaper(photoJson,
                                        url, width, height);

                                // Adding the photo to list data set
                                photosList.add(p);

                                Log.d(TAG, "Photo: " + url + ", w: "
                                        + width + ", h: " + height);
                            }
                        }

                        // Notify list adapter about dataset changes. So
                        // that it renders grid again
                        adapter.notifyDataSetChanged();

                        // Hide the loader, make grid visible
                        pbLoader.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        AppController.getInstance().trackException(e);//track error
                        e.printStackTrace();
                        Toast.makeText(
                                getActivity(),
                                "Response Listener: "
                                        + getString(R.string.msg_unknown_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppController.getInstance().trackException(error);//track error through analytics

                    Log.e(TAG, "Error: " + error.getMessage());
                    // unable to fetch wallpapers
                    // either google username is wrong or
                    // devices doesn't have internet connection
                    Toast.makeText(
                            getActivity(),
                            "onErrorResponse() : "
                                    + getString(R.string.msg_wall_fetch_error),
                            Toast.LENGTH_LONG).show();

                }
            });
            if (AppController.getInstance().getRequestQueue().getCache() != null) {
                // remove the url from the cache
                AppController.getInstance().getRequestQueue().getCache().remove(url);
            }
            // disable cache for url so that if fetches updated json
            jsonObjReq.setShouldCache(false);

            // adding request to request cache
            AppController.getInstance().addToRequestQueue(jsonObjReq);

            // initilizing grid view
            InitilizeGridLayout();

            // gridview adapter
            adapter = new GridViewAdapter(getActivity(), photosList, columnWidth);

            // setting grid view adapter
            gridView.setAdapter(adapter);

            // grid item select listener
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {

                    // pass selected image to fullscreen activity
                    Wallpaper photo = photosList.get(position);

                    //launch fullscreen activity
                    startActivity(new Intent(getActivity(), FullScreenActivity.class).putExtra(FullScreenActivity.TAG_SEL_IMAGE, photo));
                }
            });

            return rootView;
        }

        Log.d(TAG, "Fetching photos. Maybe internet connectivity");
        Toast.makeText(getActivity(),
                "Error fetching images. Check internet connectivity",
                Toast.LENGTH_SHORT).show();
        return null;// return nothing if no images were fetched

    }

    //method to check whether we have internet connectivity or whether we are connecting or not
    private boolean hasInternet(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void InitilizeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConst.GRID_PADDING, r.getDisplayMetrics());

        // Column width
        columnWidth = (int) ((utils.getScreenWidth() - ((pref
                .getNoOfGridColumns() + 1) * padding)) / pref
                .getNoOfGridColumns());

        // Setting number of grid columns
        gridView.setNumColumns(pref.getNoOfGridColumns());
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);

        // Setting horizontal and vertical padding
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
