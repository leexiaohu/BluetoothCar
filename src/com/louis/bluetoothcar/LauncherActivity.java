package com.louis.bluetoothcar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class LauncherActivity extends Activity {
	private RelativeLayout mRelativeLayout;
	private Animation fadeIn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_launcher);
		
		mRelativeLayout=(RelativeLayout) findViewById(R.id.launch);
		initAnim();
		setListener();
	}
	private void setListener() {
		// TODO Auto-generated method stub
		fadeIn.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setClass(LauncherActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	private void initAnim() {
		// TODO Auto-generated method stub
		fadeIn=AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in);
		fadeIn.setFillAfter(true);
		mRelativeLayout.startAnimation(fadeIn);
	}

}
