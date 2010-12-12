package org.wolink.m.android.driverexam;

import java.io.InputStream;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ChapterList extends ListActivity  implements OnClickListener, OnItemClickListener{
	private SectionItem[] section_list;
	private SectionItem cur_item; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.chapterlist);
        
        if (!getSectionList()) {
	    	Toast
				.makeText(this, R.string.error_no_data, Toast.LENGTH_LONG)
				.show();
	    	finish();
	    	return;
        } 
        
        
        this.setListAdapter(new SectionAdapter(this, section_list));
        
        this.getListView().setOnItemClickListener(this);
    }

	public void onClick(View v) {
		switch (v.getId()) {
		}
	}	

	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		cur_item = (SectionItem)v.getTag();
		if (cur_item.count != 0) {
			new AlertDialog.Builder(this)
			.setTitle(R.string.select_order)
			.setItems(R.array.exam_order,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						Intent intent = new Intent(ChapterList.this, QuestionView.class);
						intent.putExtra(QuestionView.KEY_QUESTION_OFFSET, cur_item.offset);
						intent.putExtra(QuestionView.KEY_QUESTION_COUNT, cur_item.count);
						intent.putExtra(QuestionView.KEY_QUESTION_ORDER, i);
						intent.putExtra(QuestionView.KEY_TITLE, cur_item.title);
						startActivity(intent);			
					}
				}
			)
			.show();	
		}
		
	}
	
	private boolean getSectionList() {
        try {
        	InputStream in = getResources().openRawResource(R.raw.sections);
        	
        	if (in != null) {
        		byte[] buf;
        		
        		buf = new byte[4];
        		if (in.read(buf, 0, 4) != 4) return false;
        		if ((buf[0] != (byte)0xFF) || (buf[1] != (byte)0xFF)) return false;
        		int count = byteToInt(buf[2], buf[3]);
        		section_list = new SectionItem[count + 1];
        		buf = new byte[6];
        		for (int i = 1; i < section_list.length; i++) {
        			if (in.read(buf, 0, 6) !=6) return false;
        			int offset = byteToInt(buf[0], buf[1]);
        			int questions = byteToInt(buf[2], buf[3]);
        			int length = byteToInt(buf[4],buf[5]);
        			byte[] strbuf = new byte[length];
        			if (in.read(strbuf, 0, length) != length) return false;
        			String title = new String(strbuf, "UTF-8");
        			section_list[i] = new SectionItem(title,offset,questions);
        		}
        		in.close();
        		
        		int total_of_all = 0;
        		for (int i = 1; i < section_list.length; i++) {
        			total_of_all += section_list[i].count;
        			if (section_list[i].count == 0) {
        				int total = 0;
        				for (int j = i + 1; j < section_list.length; j++) {
        					if (section_list[j].count == 0) 
        						break;
        					total += section_list[j].count;
        				}
        				section_list[i].count = total;
        				section_list[i].level = 1;
        			}
        		}
        		section_list[0] = new SectionItem(getString(R.string.all_of_questions), 
        				section_list[1].offset, total_of_all);
        		section_list[0].level = 0;
        		
        		return true;
        	}
        	
        	return false;
        }
        catch (Throwable t) {
        	return false;
        }		
	}
	
	private int byteToInt(byte bh, byte bl) {
		int s = 0;
		
		if (bh >= 0)
			s = bh;
		else
			s = 256 + bh;
		s *= 256;
		
		if (bl >= 0)
			s += bl;
		else
			s = s + 256 + bl;
		
		return s;
	}
	
	class SectionAdapter extends ArrayAdapter<SectionItem> {
		public SectionAdapter(Context context, SectionItem[] objects) {
			super(context, R.layout.chapterrow, objects);
		}
		
		public View getView(int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater=getLayoutInflater();
			View row=inflater.inflate(R.layout.chapterrow, parent, false);
			TextView title = (TextView)row.findViewById(R.id.title);
			SectionItem item = this.getItem(position);
			if (item.level == 0) {
				title.setText(item.title + " " + String.format(getString(R.string.section_list_qeuestions), item.count));
				title.setTextSize(22);
				//title.setTypeface(title.getTypeface(), Typeface.ITALIC);
			}
			else if (item.level == 1) {
				title.setText("  " + item.title + " " + String.format(getString(R.string.section_list_qeuestions), item.count));
				title.setTextSize(20);
				title.setTypeface(title.getTypeface(), Typeface.ITALIC);
			}
			else {
				title.setText("    " + item.title + " " + String.format(getString(R.string.section_list_qeuestions), item.count));
				title.setTextSize(18);
			}
			
			row.setTag(item);
			return(row);			
		}
	}
	
	private class SectionItem {
		public String title;
		public int offset;
		public int count;
		public int level;
		
		public SectionItem(String title, int offset, int count) {
			this.title = title;
			this.offset = offset;
			this.count = count;
			this.level = 2;
		}
	}
}
