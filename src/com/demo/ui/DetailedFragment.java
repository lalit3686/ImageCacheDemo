package com.demo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.imageloader.MyImageLoader;

public class DetailedFragment extends Fragment{

	private int number, longestSize;
	static MyImageLoader myImageLoader;

	static DetailedFragment newInstance(int number) {
		DetailedFragment f = new DetailedFragment();
		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("number", number);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		myImageLoader = new MyImageLoader(getActivity());
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        
        longestSize = (screenHeight > screenWidth ? screenHeight : screenWidth) / 2;
        
        if(Constants.DEBUG){
        	Log.e("Screen Height & width", screenHeight+" "+screenWidth);
        	Log.e("longest image size", longestSize+" ");
        }
		
		number = getArguments() != null ? getArguments().getInt("number") : 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_pager_list, container, false);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		TextView textView = (TextView) view.findViewById(R.id.header_text);
		textView.setText("Item "+(number+1)+" out of "+Constants.imageUrls.length);
		
		myImageLoader.setReqiredWidth(longestSize, 1000);
		myImageLoader.setRequiredHeight(longestSize, 1000);
		myImageLoader.ExecuteLoading(Constants.imageUrls[number], imageView);
		
		 if(Constants.DEBUG){
			 Log.e("number", number+"");
		 }
		return view;
	}

}
