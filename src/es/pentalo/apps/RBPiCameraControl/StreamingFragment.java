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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;
import es.pentalo.apps.RBPiCameraControl.API.Command;
import es.pentalo.apps.RBPiCameraControl.API.RBPiCamera;

public class StreamingFragment  extends Fragment implements SurfaceHolder.Callback, IVideoPlayer {
	
    private final String TAG = getClass().getSimpleName();
	private View myView;
	private final List<Command> lCommands = new ArrayList<Command>();
	
	private RBPiCamera rbpiCamera;
	 
	private final int REQUEST_CODE_VLC = 100001;
	
	 // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    
    private boolean isVLCApp = false;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.streaming, container, false);
		
		Button btStart = (Button) myView.findViewById(R.id.btStartStreamingVLC);
		btStart.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				isVLCApp = true;
				startStreaming();
			}
		});
		
		Button btStart2 = (Button) myView.findViewById(R.id.btStartStreaming);
		btStart2.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				isVLCApp = false;
				startStreaming();
			}
		});
		
		mSurface = (SurfaceView) myView.findViewById(R.id.surface);
        holder = mSurface.getHolder();
        holder.addCallback(this);
        
        mSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String urlCam = prefs.getString(Constants.KEY_PREF_RBPI_URL, null);
		rbpiCamera = new RBPiCamera(urlCam);
		
		return myView;
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
	public void onResume() {
        super.onResume();
       
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
        stopStreaming();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer(); 
    }
    	
	
	private void startStreaming()
	{
		
			
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		lCommands.clear();
		Map<String,?> keys = prefs.getAll();		

		for(Map.Entry<String,?> entry : keys.entrySet())
		{
			Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());

			if (entry.getKey().startsWith("video_"))
			{
				Command c = new Command();
				c.name = entry.getKey().split("_")[1];
				c.argument = entry.getValue().toString();

				lCommands.add(c);
			}

		}

		new GetStreamingURL().execute();		
		
	}
	
	private void stopStreaming()
	{
		Thread th = new Thread() {				
			@Override
			public void run() {
				Boolean res = rbpiCamera.stopStreaming();
				Log.d(TAG, "StopStreaming = " + res.toString());				
			}
		};
		
		th.start();
		
	}
	
		
	
	public class GetStreamingURL extends AsyncTask<Void, String, String>
	{
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			pd = ProgressDialog.show(getActivity(), getActivity().getString(R.string.pd_title_streaming), getActivity().getString(R.string.pd_message_streaming));
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			pd.setMessage(values[0]);
			
		}
		
		@Override
		protected String doInBackground(Void... params) {
						
			String url =  rbpiCamera.startStreaming(lCommands);
			if (url != null)
			{
				publishProgress(getString(R.string.pd_message_streaming_wait));
				
				try {
					Thread.sleep(9000);
				} catch (InterruptedException e) {					
					Log.e(TAG, e.getMessage());
				}
			}
			
			return url;
		}
		
		@Override
		protected void onPostExecute(String result) {			
			super.onPostExecute(result);
			
			if (pd.isShowing())
				pd.dismiss();
				
			if (result != null)
			{
				Log.d(TAG, result);
				if (isVLCApp)
					launchVLC(result);
				{
					 createPlayer(result);
				}
			}
			else
				Toast.makeText(getActivity(), getString(R.string.toast_error_streaming), Toast.LENGTH_SHORT).show();
		}
	}
		
	
	private void launchVLC (String url)
	{
		try{
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setComponent(new ComponentName("org.videolan.vlc.betav7neon", "org.videolan.vlc.betav7neon.gui.video.VideoPlayerActivity"));			
			i.setData(Uri.parse(url));			
			startActivityForResult(i, REQUEST_CODE_VLC);
		} 
		catch (ActivityNotFoundException e){
			Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=org.videolan.vlc.betav7neon");
			Intent intent = new Intent (Intent.ACTION_VIEW, uri); 
			startActivity(intent);
		}		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CODE_VLC)
		{
			stopStreaming();			
		}
	}
	


	
	 private void setSize(int width, int height) {
	        mVideoWidth = width;
	        mVideoHeight = height;
	        if (mVideoWidth * mVideoHeight <= 1)
	            return;

	        // get screen size
	        int w = getActivity().getWindow().getDecorView().getWidth();
	        int h = getActivity().getWindow().getDecorView().getHeight();

	        // getWindow().getDecorView() doesn't always take orientation into
	        // account, we have to correct the values
	        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	        if (w > h && isPortrait || w < h && !isPortrait) {
	            int i = w;
	            w = h;
	            h = i;
	        }

	        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
	        float screenAR = (float) w / (float) h;

	        if (screenAR < videoAR)
	            h = (int) (w / videoAR);
	        else
	            w = (int) (h * videoAR);

	        // force surface buffer size
	        holder.setFixedSize(mVideoWidth, mVideoHeight);

	        // set display size
	        LayoutParams lp = mSurface.getLayoutParams();
	        lp.width = w;
	        lp.height = h;
	        mSurface.setLayoutParams(lp);
	        mSurface.invalidate();
	    }
	 
	 
	@Override
	public void setSurfaceSize(int width, int height, int visible_width,
			int visible_height, int sar_num, int sar_den) {
		
		Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	     if (libvlc != null)
	            libvlc.attachSurface(holder.getSurface(), this);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	
	 /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(getActivity(), "Streaming: " + media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setIomx(false);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            LibVLC.restart(getActivity());
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<StreamingFragment> mOwner;

        public MyHandler(StreamingFragment owner) {
            mOwner = new WeakReference<StreamingFragment>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	StreamingFragment player = mOwner.get();

        	// SamplePlayer events
        	if (msg.what == VideoSizeChanged) {
        		player.setSize(msg.arg1, msg.arg2);
        		return;
        	}

        	// Libvlc events
        	Bundle b = msg.getData();
        	switch (b.getInt("event")) {
        	case EventHandler.MediaPlayerEndReached:
        		player.releasePlayer();
        		break;
        	case EventHandler.MediaPlayerPlaying:
        	case EventHandler.MediaPlayerPaused:
        	case EventHandler.MediaPlayerStopped:
        	default:
        		break;
        	}
        }
    }

}
