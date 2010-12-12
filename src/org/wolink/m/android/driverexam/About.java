package org.wolink.m.android.driverexam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class About extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
        TextView txtv_main_title = (TextView)findViewById(R.id.txtv_main_title);
        try {
        txtv_main_title.setText(String.format(getString(R.string.app_title), 
        		getString(R.string.app_name),
        		this.getPackageManager().getPackageInfo("org.wolink.m.android.driverexam", 0).versionName));
        }
        catch (Throwable t) {
        	
        }

	}
}
