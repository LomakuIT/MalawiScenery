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
package com.inc.automata.malawiscenery.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.app.AppController;
import com.inc.automata.malawiscenery.model.Wallpaper;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private Activity _activity;
    private LayoutInflater inflater;
    private List<Wallpaper> wallpapersList = new ArrayList<Wallpaper>();
    private int imageWidth;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public GridViewAdapter(Activity activity, List<Wallpaper> wallpapersList,
                           int imageWidth) {

        this._activity = activity;
        this.wallpapersList = wallpapersList;
        this.imageWidth = imageWidth;
    }

    @Override
    public int getCount() {

        return this.wallpapersList.size();
    }

    @Override
    public Object getItem(int position) {

        return this.wallpapersList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) _activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.grid_item_photo, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        // grid thumbnail image view
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);

        Wallpaper p = wallpapersList.get(position);

        thumbNail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbNail.setLayoutParams(new RelativeLayout.LayoutParams(imageWidth,
                imageWidth));
        thumbNail.setImageUrl(p.getUrl(), imageLoader);

        return convertView;
    }

}
