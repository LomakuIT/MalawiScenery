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

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.activities.SplashActivity;
import com.inc.automata.malawiscenery.app.AppController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Detox on 19-Jun-16.
 */
public class SubmitFeedbackFragment extends DialogFragment implements View.OnClickListener
{
    private final String TAG=this.getClass().getSimpleName();
    private final String TAG_EMAIL="email";
    private final String TAG_FEEDBACK="feedback";
    private final String TAG_DATETIME="datenow";

    private final String FEEDBACK_URL="http://apps.lomakuit.com/malawi_scenery/add_feedback.php";
    private String feedback;
    public static  SubmitFeedbackFragment newInstance(){
        SubmitFeedbackFragment f=new SubmitFeedbackFragment();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//create alert dialog builder

        final View scrollable = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback,null);//create and set layout
        final Button btnSubmit =(Button) scrollable.findViewById(R.id.btnSubmit);btnSubmit.setOnClickListener(this);//find submit button
        final Button btnCancel = (Button) scrollable.findViewById(R.id.btnCancel);btnCancel.setOnClickListener(this);//find cancel button
      final  EditText txtFeedback = (EditText)scrollable.findViewById(R.id.txtFeedback);//find edittext
        txtFeedback.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);//set scrollbars for edit text

        builder.setView(scrollable);//set view
        builder.setIcon(R.mipmap.ic_launcher);//set icon
        builder.setTitle(Html.fromHtml("<font color='#F44336'>" + getString(R.string.app_name) + "</font>"));//set title with app colour
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Resources res =getResources();
        // Title divider
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = getDialog().findViewById(titleDividerId);
        if (titleDivider != null) {
            //change color of divider
            titleDivider.setBackgroundColor(Color.parseColor("#F44336"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSubmit:
                StringRequest stringRequest = new StringRequest(Request.Method.POST, FEEDBACK_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,"response: "+response);
                        if(response.contains("success")){
                            Toast.makeText(getActivity().getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();//show toast
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),"Fail, try later",Toast.LENGTH_SHORT).show();//show toast
                        }
                        getDialog().dismiss();//dismiss dialog
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppController.getInstance().trackException(error);
                        Log.e(TAG,"volley: "+error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put(TAG_EMAIL, SplashActivity.getEmail(getActivity().getApplicationContext()));//get email
                        params.put(TAG_FEEDBACK,((EditText)getDialog().findViewById(R.id.txtFeedback)).getText().toString());//get feedback
                        params.put(TAG_DATETIME,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));//get date
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(stringRequest);//send request
                break;
            case R.id.btnCancel:
                this.dismiss();//dismiss dialog
                break;
        }
    }
}
