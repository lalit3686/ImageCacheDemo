package com.demo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class DetailedGallery extends FragmentActivity{

	ViewPager mViewPager;
	MyAdapter myAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_layout);
        
        int position = getIntent().getIntExtra("position", -1);
        
        myAdapter = new MyAdapter(getSupportFragmentManager());
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(myAdapter);
        
        if(position != -1){
        	mViewPager.setCurrentItem(position, true);
        }
    }
    
    public void MyOnClick(View view) {}
    
    static class MyAdapter extends FragmentStatePagerAdapter
    {
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return DetailedFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return Constants.imageUrls.length;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
    }
}
