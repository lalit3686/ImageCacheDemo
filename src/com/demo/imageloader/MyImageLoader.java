package com.demo.imageloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.demo.ui.R;

public class MyImageLoader {

	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "DiskImageCache";
	private DiskLruImageCache mDiskLruImageCache;
	private ExecutorService executorService;
	private LruCache<String, Bitmap> mMemoryCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private int byteCounts;
	private int requiredHeight = 100, requiredWidth = 100; // setting default height & width as 100
	private final int default_icon = R.drawable.empty_photo;
	protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

	public MyImageLoader(Context context) {
		
		executorService = Executors.newFixedThreadPool(5);
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = 1024 * 1024 * memClass / 8;
		if(com.demo.ui.Constants.DEBUG){
			Log.e("memory for this application", memClass + " MB");
		}

		mDiskLruImageCache = new DiskLruImageCache(context, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE, CompressFormat.PNG, 70);
		
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				byteCounts = bitmap.getRowBytes() * bitmap.getHeight();
				return byteCounts;
			}
		};
	}
	
	public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }
	
	public void ExecuteLoading(String urlString, ImageView mImageView) {
		
		imageViews.put(mImageView, urlString);
		Bitmap bitmap = getBitmapFromMemCache(urlString);
		
		if (bitmap != null){
			mImageView.setImageBitmap(bitmap);
			
			if(com.demo.ui.Constants.DEBUG){
				Log.e("bitmap", "from cache");
			}
		}
		else {
			executorService.submit(new LoadImages(urlString, mImageView));
			mImageView.setImageResource(default_icon);
		}
	}
	
	 boolean ImageViewReused(String urlString, ImageView mImageView){
	        String tag=imageViews.get(mImageView);
	        if(tag==null || !tag.equals(urlString))
	            return true;
	        return false;
	    }

	class LoadImages implements Runnable {
		String urlString;
		ImageView mImageView;
		DisplayImages images;
		
		public LoadImages(String urlString, ImageView mImageView) {
			this.urlString = urlString;
			this.mImageView = mImageView;
		}

		public void run() {
			
			synchronized (mPauseWorkLock) {
	               while (mPauseWork) {
	                   try {
	                       mPauseWorkLock.wait();
	                   } catch (InterruptedException e) {}
	               }
	           }
			
			if(!ImageViewReused(urlString, mImageView)){
				Bitmap bitmap = DownloadFromUrl(urlString);
				addBitmapToDiskCache(urlString, bitmap);
				
				if(com.demo.ui.Constants.DEBUG){
					Log.e("bitmap", "downloaded");
				}
				
				DisplayImages images = new DisplayImages(urlString, mImageView, bitmap);
				((Activity) mImageView.getContext()).runOnUiThread(images);
			}
		}
	}

	class DisplayImages implements Runnable {
		Bitmap bitmap;
		String urlString;
		ImageView mImageView;

		public DisplayImages(String urlString, ImageView mImageView, Bitmap bitmap) {
			this.urlString = urlString;
			this.mImageView = mImageView;
			this.bitmap = bitmap;
		}

		public void run() {
			
			if(!ImageViewReused(urlString, mImageView)){
				if (bitmap != null)
					mImageView.setImageBitmap(bitmap);
				else
					mImageView.setImageResource(default_icon);
			}
		}
	}

	private Bitmap DownloadFromUrl(String urlString) {
		return decodeBitmapFromStream(urlString, getReqiredWidth(), getRequiredHeight());
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		synchronized (mMemoryCache) {
			if (mMemoryCache.get(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}
	}
	private Bitmap getBitmapFromMemCache(String key) {
		Bitmap bitmap = mMemoryCache.get(key);
		if(bitmap == null){
			bitmap = getBitmapFromDiskCache(key);
		}
		return bitmap;
	}
	
	private void addBitmapToDiskCache(String key, Bitmap bitmap) {
		synchronized (mDiskLruImageCache) {
			if (!mDiskLruImageCache.containsKey(String.valueOf(key.hashCode()))) {
				mDiskLruImageCache.put(String.valueOf(key.hashCode()), bitmap);
				addBitmapToMemoryCache(key, bitmap);
			}
		}
	}
	
	private Bitmap getBitmapFromDiskCache(String key) {
		return mDiskLruImageCache.getBitmap(String.valueOf(key.hashCode()));
	}

	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if(com.demo.ui.Constants.DEBUG){
			Log.e("downloaded Image height & Width", height+" "+width);
			Log.e("required Image height & Width", reqHeight+" "+reqWidth);
		}
		
		inSampleSize = Math.min(width/reqWidth, height/reqHeight);
		
		if(com.demo.ui.Constants.DEBUG){
			Log.e("inSampleSize", inSampleSize+"");
		}
		return inSampleSize;
	}
	
	private static Bitmap decodeBitmapFromStream(String urlString, int reqWidth, int reqHeight) {

		Bitmap bitmap;
		URL url = null;
		InputStream is = null;
		try {
			url = new URL(urlString);
			is = (InputStream) url.getContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(is, null, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // As InputStream can be used only once we have to regenerate it again.
	    try {
			is = (InputStream) url.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    bitmap = BitmapFactory.decodeStream(is, null, options);
	    try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return bitmap;
	}

	public int getRequiredHeight() {
		return requiredHeight;
	}

	public void setRequiredHeight(int longest, int requiredHeight) {
		this.requiredHeight = requiredHeight > longest ? longest : requiredHeight;
	}

	public int getReqiredWidth() {
		return requiredWidth;
	}

	public void setReqiredWidth(int longest, int requiredWidth) {
		this.requiredWidth = requiredWidth > longest ? longest : requiredWidth; 
	}
	
	public void clearCacheMemory() {
		if(com.demo.ui.Constants.DEBUG){
			Log.e("size of LruCache", mMemoryCache.size()+"");
		}
		if(mMemoryCache.size() > 0){
			mMemoryCache.evictAll();
		}
	}
	
	public void clearDiskMemory() {
		mDiskLruImageCache.clearCache();
	}
}
