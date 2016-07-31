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

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	//flag for GPS status
	boolean isGPSEnabled=false;

	//flag for network status
	boolean isNetworkEnabled = false;

	boolean canGetLocation=false;

	Location location;//location
	double latitude;//latitude
	double longitude;//longitude

	//minimum distance to change updates in metres
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES=10;

	//minimum time between updates
	private static final long MIN_TIME_BW_UPDATES=1000*60*1;//1min

	//location manager
	protected LocationManager locationManager;

	public GPSTracker(Context context){
		this.mContext=context;
		getLocation();
	}

	public Location getLocation() {

		try{

			locationManager=(LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			 // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if(!isGPSEnabled && !isNetworkEnabled){
				//no provider
				Toast.makeText(getApplicationContext(), "No service", Toast.LENGTH_SHORT).show();
			}
			else{
				this.canGetLocation=true;
				if(isNetworkEnabled){

					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

					Log.d("Network", "Network");
				}
				if(locationManager !=null){
					location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					
					if(location !=null){
						latitude=location.getLatitude();
						longitude=location.getLongitude();
					}
				}
				
			}
			//if GPS already enabled get lat/long using gps services

			if(isGPSEnabled){
				
				if(location==null){
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
							MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("GPS Enabled","GPS Enabled");
					if(locationManager !=null){
						location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(location!=null){
							latitude=location.getLatitude();
							longitude=location.getLongitude();
						}
					}
				}
			}
		}catch(Exception e){
			Log.e("GPS getLocation error", e.toString());
		}
		return location;
	}
	
	public double getLatitude(){
		if(location !=null){
			latitude=location.getLatitude();
		}
		return latitude;
	}
	
	public double getLongitude(){
		if(location !=null){
			longitude=location.getLongitude();
		}
		return longitude;
	}

	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		
		//setting dialog title
		alertDialog.setTitle("GPS settings");
		
		//setting dialog message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		
		//on pressing settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent= new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
				
			}
		});
		
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			dialog.cancel();	
			}
		});
		
		//show alert dialog
		alertDialog.show();
	}
	
	public void stopUsingGPS(){
		if(locationManager!=null){
			locationManager.removeUpdates(GPSTracker.this);
		}
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		
	}

}
