/*
 * Copyright 2017 Lomaku Technologies.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inc.automata.malawiscenery.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inc.automata.malawiscenery.R;
import com.inc.automata.malawiscenery.app.AppConst;

public class PolicyActivity extends AppCompatActivity {
    private static String TAG = PolicyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        try {
            getSupportActionBar().setTitle("Privacy Policy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            initPolicyWebView();
        }catch (NullPointerException npEx){
                Log.e(TAG,"onCreate() npEx: "+npEx.toString());
        } catch (Exception ex) {
            Log.e(TAG, "onCreate(): " + ex.toString());
        } finally {
            initPolicyWebView();//show web view
        }
    }

    private void initPolicyWebView() {
        WebView policyView = (WebView) findViewById(R.id.web_policy);

        policyView.setWebChromeClient(new MyWebChromeClient(this));
        policyView.setWebViewClient(new WebViewClient());
        policyView.clearCache(true);
        policyView.clearHistory();
        policyView.setHorizontalScrollBarEnabled(false);
        policyView.loadUrl(AppConst.URL_PRIVACY_POLICY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();//go back to app

        return super.onOptionsItemSelected(item);
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }
}