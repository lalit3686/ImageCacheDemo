package com.demo.ui;

import com.demo.imageloader.MyImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends Activity implements OnItemClickListener{

	private GridView mGridView;
	private MyAdapter adapter;
	private ViewStub stub;
	private MyImageLoader myImageLoader;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setUpViewStub();
        mGridView = (GridView) findViewById(R.id.gridView);
        SetAdapter(Constants.imageUrls);
        
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                	myImageLoader.setPauseWork(true);
                } else {
                	myImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
    }
    
    private void SetAdapter(String[] imageUrls) {
    	
    	 if(imageUrls.length > 0){
			 stub.setVisibility(View.GONE);
         }
         else{
        	 stub.setVisibility(View.VISIBLE);
         }
    	 
    	adapter = new MyAdapter(this, imageUrls);
        mGridView.setAdapter(adapter);
    }
    
    private void setUpViewStub() {
    	stub = (ViewStub) findViewById(R.id.stub);
        stub.inflate();
    	stub.setVisibility(View.GONE);
	}
    
    public void MyOnClick(View view) {
    	int id = view.getId();
    	switch (id) {
		case R.id.download:
			SetAdapter(Constants.imageUrls);
			break;

		case R.id.blank:
			SetAdapter(new String[]{});
			break;
		case R.id.clear_cache:
			adapter.clearCache();
			SetAdapter(new String[]{});
			break;
			
		}
	}

	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Intent intent = new Intent(view.getContext(), DetailedGallery.class);
		intent.putExtra("position", position);
		startActivity(intent);
	}
	
	
	class MyAdapter extends BaseAdapter{

		private LayoutInflater inflater;
		
		private String imageUrls[];
		private int longestSize;

		public MyAdapter(Activity activity, String imageUrls[]) {
			this.imageUrls = imageUrls;
			inflater = LayoutInflater.from(activity);
			
			myImageLoader = new MyImageLoader(activity);
			
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        int screenHeight = displayMetrics.heightPixels;
	        int screenWidth = displayMetrics.widthPixels;
	        
	        longestSize = (screenHeight > screenWidth ? screenHeight : screenWidth) / 2;
	        if(Constants.DEBUG){
	        	Log.e("Screen Height & width", screenHeight+" "+screenWidth);
	        	Log.e("longest image size", longestSize+" ");
	        }
		}
		
		public int getCount() {
			return imageUrls.length;
		}

		public Object getItem(int position) {
			return imageUrls[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			ImageView mImageView = null;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.row, null);
			}
			mImageView = (ImageView) convertView.findViewById(R.id.imageview);
			
			myImageLoader.setReqiredWidth(longestSize, 100);
			myImageLoader.setRequiredHeight(longestSize, 100);
			myImageLoader.ExecuteLoading(imageUrls[position], mImageView);
			return convertView;
		}
		
		public void clearCache() {
			myImageLoader.clearCacheMemory();
			myImageLoader.clearDiskMemory();
			notifyDataSetChanged();
		}
	}

}
