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


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import es.pentalo.apps.RBPiCameraControl.Preferences.RBPreferenceFragment;
import es.pentalo.apps.RBPiCameraControl.Preferences.RBPreferenceFragment.RBPFeatures;

public class MyFragmentPagerAdapterMobile extends FragmentPagerAdapter {

   private String[] pageTitle = {
           "ImageConfig", "Image", "VideoConfig", "Streaming"
   };

   public MyFragmentPagerAdapterMobile(FragmentManager fragmentManager) {
       super(fragmentManager);
       
   } 

   @Override
   public Fragment getItem(int position) {
	   
	   
	   Log.d("My", String.valueOf(position));
	   	   
	   
	   if (position == 0)
	   {
		   RBPreferenceFragment ipFragment = new RBPreferenceFragment();		
		   ipFragment.setFeature(RBPFeatures.PHOTO);
		   
		   return ipFragment;
					   
	   }
	   else if (position ==1)
	   {
		   return new PhotoFragment();		   		   
	   }
	   else if (position == 2)
	   { 
		   RBPreferenceFragment videoFragment = new RBPreferenceFragment();	
		   videoFragment.setFeature(RBPFeatures.VIDEO);
		   
		   return videoFragment;
	   }
	   
	   else if (position == 3)
	   {		   
		   return new StreamingFragment();
	   }
	   
	   
	   return null;		   
	   
   }

   @Override
   public int getCount() {
       return pageTitle.length;
   }

   @Override
   public CharSequence getPageTitle(int position) {
       return pageTitle[position];
   }

}