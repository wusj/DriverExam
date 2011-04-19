package org.wolink.m.android.driverexam;

import java.util.Calendar;

import net.youmi.android.AdManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.casee.adsdk.CaseeAdView;

public class DriverExam extends Activity implements OnClickListener, CaseeAdView.AdListener{
	net.youmi.android.AdView adView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        try{
        	String version = this.getPackageManager().
				getPackageInfo("org.wolink.m.android.driverexam", 0).versionName;
        	AdManager.init("09376322e1b9b3b5", "5de21b027dad7cbf", 30, false, version);  
        }
        catch (Throwable t) {
        	
        }
        
        setContentView(R.layout.main);
        findViewById(R.id.btn_chapter).setOnClickListener(this);
        //findViewById(R.id.btn_random).setOnClickListener(this);
        findViewById(R.id.btn_intensify).setOnClickListener(this);
        findViewById(R.id.btn_exam).setOnClickListener(this);
        findViewById(R.id.btn_help).setOnClickListener(this);
        adView = (net.youmi.android.AdView) this.findViewById(R.id.adView);
        
        TextView txtv_main_title = (TextView)findViewById(R.id.txtv_main_title);
        try {
            txtv_main_title.setText(String.format(getString(R.string.app_title), 
            		getString(R.string.app_name),
            		this.getPackageManager().getPackageInfo("org.wolink.m.android.driverexam", 0).versionName));
        }
        catch (Throwable t) {
        	
        }
   }
    
	public void onPause() {
		super.onPause();
	}

	public void onStop() {
		super.onStop();
	}

	public void onStart() {
		super.onStart();
	}

	public void onResume() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ads = settings.getBoolean("ads", false);
        if (ads) {
        	int year = settings.getInt("year", 2000);
        	int month = settings.getInt("month", 1);
        	int day = settings.getInt("day", 1);
            final Calendar c = Calendar.getInstance();
            int curYear = c.get(Calendar.YEAR); //获取当前年份
            int curMonth = c.get(Calendar.MONTH);//获取当前月份
            int curDay = c.get(Calendar.DAY_OF_MONTH);//获取当前月份的日期号码
            if (year == curYear && month == curMonth && day == curDay){
            	ads = true;
            } else {
            	ads = false;
            }
        }
        if (ads) {
        	//title_bar.removeViewAt(1);
        	adView.setVisibility(View.INVISIBLE);
        	//have_ad = true;
        }
		super.onResume();
	}    
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_chapter:
			intent = new Intent(this, ChapterList.class);
			startActivity(intent);
			break;
		//case R.id.btn_random:
			//intent = new Intent(this, QuestionView.class);
			//intent.putExtra(QuestionView.KEY_QUESTION_OFFSET, 28);
			//intent.putExtra(QuestionView.KEY_QUESTION_COUNT, 28);
			//intent.putExtra(QuestionView.KEY_QUESTION_ORDER, QuestionView.ORDER_RANDOM);
			//startActivity(intent);		
			//break;
		case R.id.btn_intensify:
			intent = new Intent(this, QuestionView.class);
			intent.putExtra(QuestionView.KEY_MODE, QuestionView.MODE_STRONG);
			intent.putExtra(QuestionView.KEY_TITLE, getString(R.string.intensify_exercise));
			startActivity(intent);			
			break;
		case R.id.btn_exam:
			intent = new Intent(this, QuestionView.class);
			intent.putExtra(QuestionView.KEY_MODE, QuestionView.MODE_TEST_EXAM);
			intent.putExtra(QuestionView.KEY_TITLE, getString(R.string.practice_exam));
			startActivity(intent);
			break;
		case R.id.btn_help:
			intent = new Intent(this, About.class);
			startActivity(intent);
			break;
		}
	}
	
	public void onFailedToReceiveAd(CaseeAdView cav) {
		// TODO Auto-generated method stub
	}

	public void onFailedToReceiveRefreshAd(CaseeAdView cav) {
		// TODO Auto-generated method stub
		
	}

	public void onReceiveAd(CaseeAdView cav) {
		// TODO Auto-generated method stub		
	}

	public void onReceiveRefreshAd(CaseeAdView cav) {
		// TODO Auto-generated method stub
		
	}
}