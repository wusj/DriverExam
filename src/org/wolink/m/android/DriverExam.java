package org.wolink.m.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class DriverExam extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.main);
        findViewById(R.id.btn_chapter).setOnClickListener(this);
        findViewById(R.id.btn_random).setOnClickListener(this);
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
		case R.id.btn_random:
			intent = new Intent(this, QuestionView.class);
			//intent.putExtra(QuestionView.KEY_QUESTION_OFFSET, 28);
			//intent.putExtra(QuestionView.KEY_QUESTION_COUNT, 28);
			//intent.putExtra(QuestionView.KEY_QUESTION_ORDER, QuestionView.ORDER_RANDOM);
			startActivity(intent);		
			break;
		}
	}
}