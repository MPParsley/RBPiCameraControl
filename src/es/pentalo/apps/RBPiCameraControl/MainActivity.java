/*
 *
 *  Copyright (C) Roberto Calvo Palomino
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *
 *  Author : Roberto Calvo Palomino <rocapal at gmail dot com>
 *
 */

package es.pentalo.apps.RBPiCameraControl;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import es.rocapal.utils.Download.DownloadTextFileAsyncTask;
import es.rocapal.utils.Download.IDownloadTextFileAsyncTask;



public class MainActivity extends Activity implements IDownloadTextFileAsyncTask {

	
	private String TAG = getClass().getSimpleName(); 
	private SharedPreferences prefs;
	
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showAlertDialog(true);
    }
    
    
    public void showAlertDialog (Boolean showAlways)
    {
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ipCam = prefs.getString(Constants.KEY_PREF_RBPI_IP, null);
        if (ipCam != null)
        	Log.d(TAG, ipCam);
        
        if (ipCam == null || showAlways)
        {
        	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        	final EditText input = new EditText(this);
        	if (ipCam != null)
        		input.setText(ipCam);
        	
        	alertDialogBuilder.setView(input);
  
        	alertDialogBuilder.setTitle(getString(R.string.dialog_title_rbpi_ip));

        	alertDialogBuilder        	
        	.setMessage(getString(R.string.dialog_message_rbpi_ip))
        	.setCancelable(false)
        	.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			
        			Editor edit = prefs.edit();
        			edit.putString(Constants.KEY_PREF_RBPI_IP,  input.getText().toString() );
        			edit.putString(Constants.KEY_PREF_RBPI_URL, "http://" + input.getText().toString() + "/");
        			edit.commit();
        			
        			checkConnection();
        		}
        	})
        	.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog,int id) {
        
        			dialog.cancel();
        			MainActivity.this.finish();
        		}
        	});

  
        	AlertDialog alertDialog = alertDialogBuilder.create();
        	alertDialog.show();        
        }
        else
        	initApp();
        
    }
    
    private void checkConnection()
    {
    	String urlCam = prefs.getString(Constants.KEY_PREF_RBPI_URL, null);
    	
    	DownloadTextFileAsyncTask myTask = 
				new DownloadTextFileAsyncTask(this, getString(R.string.pd_title_check), getString(R.string.pd_message_check));
		myTask.setListener(this);
		myTask.execute(Uri.parse(urlCam + "api/photo/params/"));
    }
    
    
    private void initApp() 
    {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scaleFactor = metrics.density;
        
        int widthInPixels = metrics.widthPixels;
        int heightInPixels = metrics.heightPixels;
        
        float widthDp = widthInPixels / scaleFactor;
        float heightDp = heightInPixels / scaleFactor;
        
        float smallestWidth = Math.min(widthDp, heightDp);
        
        if (smallestWidth < 600) {
        	
        	MyFragmentPagerAdapterMobile fragmentPagerAdapter = new MyFragmentPagerAdapterMobile(getFragmentManager());
            
            viewPager.setAdapter(fragmentPagerAdapter);
            viewPager.setOffscreenPageLimit(5);
            pagerTabStrip.setDrawFullUnderline(true);
            pagerTabStrip.setTabIndicatorColor(Color.DKGRAY);           
        }
        else
        {        	                        
            MyFragmentPagerAdapter fragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
            
            viewPager.setAdapter(fragmentPagerAdapter);
            viewPager.setOffscreenPageLimit(5);
            pagerTabStrip.setDrawFullUnderline(true);
            pagerTabStrip.setTabIndicatorColor(Color.DKGRAY);
        }

    }

	@Override
	public void downloadedSuccessfully(Object data) {
		initApp();
		
	}

	@Override
	public void downloadFailed(Object response) {
		Toast.makeText(this, getString(R.string.toast_error_check_connection), Toast.LENGTH_SHORT).show();
		showAlertDialog(true);
	}
	

    
    
    
    
}

