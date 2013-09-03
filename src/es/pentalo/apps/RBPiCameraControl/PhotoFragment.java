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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import es.pentalo.apps.RBPiCameraControl.API.Command;
import es.pentalo.apps.RBPiCameraControl.API.RBPiCamera;

public class PhotoFragment extends Fragment {
	
	private final String TAG = getClass().getSimpleName();
	
	private View myView;
	private ImageView ivPhoto;
	final List<Command> lCommands = new ArrayList<Command>();
	private Bitmap mBitmap;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);	
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myView = inflater.inflate(R.layout.photo, container, false);
		
		ivPhoto = (ImageView) myView.findViewById(R.id.ivPhoto);
				
		Button btImage = (Button) myView.findViewById(R.id.btShot);
		btImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				takePicture();
			}
		});
		
		ivPhoto.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				File sdCardDirectory = Environment.getExternalStorageDirectory();
				File image = new File(sdCardDirectory, "rbpi.png");
				FileOutputStream outStream;
			    try {

			        outStream = new FileOutputStream(image);
			        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			        outStream.flush();
			        outStream.close();
			        
			    } catch (FileNotFoundException e) {
			        e.printStackTrace();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(sdCardDirectory.toURI().toString() + "/rbpi.png"), "image/*");
				startActivity(intent);
			}
		});
		
		return myView;
	}
	
	private void takePicture ()
	{
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		lCommands.clear();
		Map<String,?> keys = prefs.getAll();		
		
		for(Map.Entry<String,?> entry : keys.entrySet())
		{
			Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
			
			if (entry.getKey().startsWith("photo_"))
			{
				Command c = new Command();
				c.name = entry.getKey().split("_")[1];
				c.argument = entry.getValue().toString();
								
				lCommands.add(c);
			}
			
		}
		
		new GetPhotoTask().execute();
	
	}
	
	public class GetPhotoTask extends AsyncTask<Void, Void, Bitmap>
	{
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			pd = ProgressDialog.show(getActivity(), getActivity().getString(R.string.pd_title_photo), getActivity().getString(R.string.pd_message_photo));
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String urlCam = prefs.getString(Constants.KEY_PREF_RBPI_URL, null);
			final RBPiCamera rbpiCamera = new RBPiCamera(urlCam);
			return rbpiCamera.shotPhoto(lCommands);
			
		}
	
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if (pd.isShowing())
				pd.dismiss();
			
			if (result != null)
			{
				ivPhoto.setImageBitmap(result);
				mBitmap = result;
			}
			else
				Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_photo), Toast.LENGTH_LONG).show();
				
		}
	}

	
}
