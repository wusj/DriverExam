package org.wolink.m.android.driverexam;

import net.youmi.android.AdManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class DriverExam extends Activity implements OnClickListener{
	net.youmi.android.AdView adView;

	static{
		//第一个参数为您的应用发布Id
		//第二个参数为您的应用密码
		//第三个参数是请求广告的间隔，有效的设置值为30至200，单位为秒
		//第四个参数是设置测试模式，设置为true时，可以获取测试广告，正式发布请设置此参数为false
		AdManager.init("09376322e1b9b3b5", "5de21b027dad7cbf", 30, false);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        
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
}