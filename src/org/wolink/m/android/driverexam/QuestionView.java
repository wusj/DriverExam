package org.wolink.m.android.driverexam;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.casee.adsdk.CaseeAdView;

public class QuestionView extends Activity implements OnClickListener, OnCheckedChangeListener{
	private static final String TAG = "DriverExam";
	
	private static final String STRONG_FILE = "wrong_question";
	
	public static final String KEY_QUESTION_OFFSET = "org.wolink.m.andorid.questionview.key_question_offset";
	public static final String KEY_QUESTION_COUNT = "org.wolink.m.andorid.questionview.key_question_count";
	public static final String KEY_QUESTION_ORDER = "org.wolink.m.android.questionview.key_question_order";
	public static final String KEY_MODE = "org.wolink.m.android.questionview.mode";
	public static final String KEY_TITLE = "org.wolink.m.android.driverexam.title";
	
	public static final int ORDER_SEQUENTIAL = 0;
	public static final int ORDER_RANDOM = 1;
	
	public static final int MODE_NORMAL = 0;
	public static final int MODE_VIEW_WRONG = 1;
	public static final int MODE_TEST_EXAM = 2;
	public static final int MODE_STRONG = 3;
	
	QuestionItem[] question_items; 
	int[] question_list;
	int[] answer_list;
	int[] picsrc_list;
	int[] wrong_list;
	int current_question;
	int current_wrong;
	int mode;
	TextView tv_question_title;
	TextView tv_total_question;
	TextView tv_current_question;
	TextView tv_right_answer_label;
	TextView tv_your_answer;
	TextView tv_right_answer;
	TextView txtv_time;
	RadioGroup rg_choice;
	RadioGroup rg_trueorfalse;
	CaseeAdView cav;
	
	Button btn_next_question;
	Button btn_prev_question;
	Button btn_over;
	
	ImageView imgv_picture;
	
	/* time process */
	private Handler mHandler = new Handler();
	long mStartTime = 0L; 
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
		       final long start = mStartTime;
		       long millis = System.currentTimeMillis() - start;
		       int seconds = (int) (millis / 1000);
		       int minutes = seconds / 60;
		       int hours   = minutes / 60;
		       seconds     = seconds % 60;
		       minutes 	   = minutes % 60;

		       if (hours > 0) {
		    	   txtv_time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
		       }
		       else {
		    	   txtv_time.setText(String.format("%02d:%02d", minutes, seconds));
		       }
		     
		       mHandler.postDelayed(mUpdateTimeTask, 500);
		   }
	};
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        Log.d(TAG, "onCreate");
        
        setContentView(R.layout.questionview);
         
        tv_question_title = (TextView)findViewById(R.id.question);
        tv_total_question = (TextView)findViewById(R.id.tv_total_question);
        tv_current_question = (TextView)findViewById(R.id.tv_current_question);
        tv_right_answer_label = (TextView)findViewById(R.id.tv_right_answer_label);
        tv_your_answer = (TextView)findViewById(R.id.tv_your_answer);
        tv_right_answer = (TextView)findViewById(R.id.tv_right_answer);
        txtv_time = (TextView)findViewById(R.id.txtv_time);
        
        rg_choice = (RadioGroup)findViewById(R.id.radiogroup_choice);
        rg_trueorfalse = (RadioGroup)findViewById(R.id.radiogroup_trueorfalse);
        btn_next_question = (Button)findViewById(R.id.btn_next_question);
        btn_prev_question = (Button)findViewById(R.id.btn_prev_question);
        btn_over = (Button)findViewById(R.id.btn_over);
        imgv_picture = (ImageView)findViewById(R.id.imgv_picture);
        
        cav = (CaseeAdView) this.findViewById(R.id.caseeAdView);
        
        btn_next_question.setOnClickListener(this);
        btn_prev_question.setOnClickListener(this);
        btn_over.setOnClickListener(this);
        
        rg_choice.setOnCheckedChangeListener(this);
        rg_trueorfalse.setOnCheckedChangeListener(this);
         
        if (!getQuestionList()) {
	    	Toast
				.makeText(this, R.string.error_no_data, Toast.LENGTH_LONG)
				.show();
	    	finish();
	    	return;
        }
        initImageSrcList();
		
        int offset = getIntent().getIntExtra(KEY_QUESTION_OFFSET, 0);
        int count = getIntent().getIntExtra(KEY_QUESTION_COUNT, question_items.length - offset);
        int random = getIntent().getIntExtra(KEY_QUESTION_ORDER, ORDER_SEQUENTIAL);
        mode = getIntent().getIntExtra(KEY_MODE, MODE_NORMAL);
        this.setTitle(getIntent().getStringExtra(KEY_TITLE));
        
        if (mode == MODE_TEST_EXAM) {
        	count = 100;
        }
        question_list = new int[count];
        answer_list = new int[count];
        for(int i = 0; i < count; i++) {
        	question_list[i] = i + offset;
        	answer_list[i] = -1;
        }
        current_question = 0;
        
        if (mode == MODE_TEST_EXAM) {
        	// random 60 choices and 40 true or false 
        	genTestQuestionList();
        	btn_over.setText(R.string.jiaojuan);
        }
        else if (mode == MODE_STRONG) {
        	if (genStrongQuestionList() == false) {
        		Toast
					.makeText(this, R.string.no_wrong, Toast.LENGTH_SHORT)
					.show();
	    		finish();
    			return;
        	}
       }
        else if (random == ORDER_RANDOM) {
        	genRandomQuestionsList();
        }
        
        if (mStartTime == 0L) {
            mStartTime = System.currentTimeMillis();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }    
        updateQuestionView();
    }

	public void onPause() {
		if (cav != null) {
			cav.onUnshown();
		}
		super.onPause();
	}

	public void onStop() {
		if (cav != null) {
			cav.onUnshown();
		}
		super.onStop();
	}

	public void onStart() {
		if (cav != null) {
			cav.onShown();
		}
		super.onStart();
	}

	public void onResume() {
		if (cav != null) {
			cav.onShown();
		}
		super.onResume();
	}    
   
	public void OnDestroy() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		super.onDestroy();
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next_question:
			if (mode == MODE_VIEW_WRONG) {
				if (current_wrong < wrong_list.length - 1) {
					current_wrong++;
					current_question = wrong_list[current_wrong];
					updateQuestionView();
				}
			}
			else {
				if (current_question < question_list.length - 1) {
					current_question++;
					updateQuestionView();
				}		
			}
			break;
		case R.id.btn_prev_question:
			if (mode == MODE_VIEW_WRONG) {
				if (current_wrong > 0) {
					current_wrong--;
					current_question = wrong_list[current_wrong];
					updateQuestionView();
				}
			}
			else {
				if (current_question > 0) {
					current_question--;
					updateQuestionView();
				}
			}
			break;
		case R.id.btn_over:
			int have_answer = 0;
			int right_answer = 0;
			for(int i = 0; i < answer_list.length; i++) {
				if (answer_list[i] != -1) 
					have_answer++;
				
				if (answer_list[i] == question_items[question_list[i]].answer)
					right_answer++;
			}
			
			if (mode == MODE_TEST_EXAM) {
				AlertDialog.Builder builder= new AlertDialog.Builder(this);
				builder
					.setTitle(R.string.jiaojuan)
					.setMessage(R.string.jiaojuan_ack)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
								QuestionView.this.disScoreDialog();
							}
						})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
							}
						})
					.setCancelable(true)
					.show();					
				break;
			}
			
			saveWrongQuestions();
			mHandler.removeCallbacks(mUpdateTimeTask);

			if (have_answer != answer_list.length) {
				if (right_answer == have_answer) {
					AlertDialog.Builder builder= new AlertDialog.Builder(this);
					builder
						.setTitle(R.string.finish_exercise)
						.setMessage(String.format(getString(R.string.unfinish_right_prompt), answer_list.length, have_answer))
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
								QuestionView.this.finish();
								}
							})
						.setCancelable(false)
						.show();					
				}
				else {
					AlertDialog.Builder builder= new AlertDialog.Builder(this);
					builder
						.setTitle(R.string.finish_exercise)
						.setMessage(String.format(getString(R.string.unfinish_wrong_prompt), answer_list.length, have_answer, 
								right_answer, have_answer - right_answer))
						.setPositiveButton(R.string.view_wrong, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
									enterViewWrongMode();
								}
							})
						.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
									QuestionView.this.finish();
								}
							})
						.setCancelable(false)
						.show();
				}
			}
			else if (right_answer == answer_list.length) {
				AlertDialog.Builder builder= new AlertDialog.Builder(this);
				builder
					.setTitle(R.string.finish_exercise)
					.setMessage(String.format(getString(R.string.finish_right_prompt), right_answer))
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
								QuestionView.this.finish();
							}
						})
					.setCancelable(false)
					.show();
			}
			else {
				AlertDialog.Builder builder= new AlertDialog.Builder(this);
				builder
					.setTitle(R.string.finish_exercise)
					.setMessage(String.format(getString(R.string.finish_wrong_prompt), answer_list.length, right_answer, 
							answer_list.length - right_answer))
					.setPositiveButton(R.string.view_wrong, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
								enterViewWrongMode();
							}
						})
					.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
								QuestionView.this.finish();
							}
						})
					.setCancelable(false)
					.show();				
			}
			break;
		}
	}	

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(updateView) return;
		
		switch (checkedId) {
		case R.id.radio_A:
		case R.id.radio_right:
			answer_list[current_question] = 0;
			break;
		case R.id.radio_B:
		case R.id.radio_wrong:
			answer_list[current_question] = 1;
			break;
		case R.id.radio_C:
			answer_list[current_question] = 2;
			break;
		case R.id.radio_D:
			answer_list[current_question] = 3;
			break;
		}
		
		if (answer_list[current_question] != -1 && current_question != question_list.length - 1) {
			btn_next_question.setEnabled(true);
		} 
		else if ((current_question == question_list.length - 1) && (mode != MODE_TEST_EXAM)) {
			onClick(btn_over);
		}
	}
	
	public void onBackPressed() {
		if (mode == MODE_NORMAL) {
			AlertDialog.Builder builder= new AlertDialog.Builder(this);
			builder
				.setTitle(R.string.exit)
				.setMessage(R.string.exit_ack)
				.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
							QuestionView.this.finish();
						}
					})
				.setNegativeButton(R.string.continue_exercise, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
						}
					})
				.setCancelable(false)
				.show();
		}
		else {
			QuestionView.this.finish();
		}
	}
		
	private void updateQuestionView() {
		QuestionItem question_item = question_items[question_list[current_question]];
		updateView = true;
		
		/* Update title and all of TextView */
		tv_total_question.setText(String.format(getString(R.string.total_question), 
				(mode == MODE_VIEW_WRONG)?wrong_list.length:question_list.length));
		tv_current_question.setText(String.format(getString(R.string.current_question), 
				(mode == MODE_VIEW_WRONG)?current_wrong + 1 : current_question + 1));
		tv_question_title.setText("    " + (current_question + 1) + ". " + question_item.title);
		
		/* Update radio group */
		if (mode == MODE_VIEW_WRONG) {
			rg_choice.setVisibility(View.INVISIBLE);
			rg_trueorfalse.setVisibility(View.INVISIBLE);	
			btn_over.setVisibility(View.INVISIBLE);
			tv_right_answer_label.setVisibility(View.VISIBLE);
			if (question_item.flag == QuestionItem.TRUE_OR_FALSE) {
				tv_your_answer.setText(trueorfalse[answer_list[current_question]]);
				tv_right_answer.setText(trueorfalse[question_item.answer]);				
			}
			else {
				tv_your_answer.setText(choice[answer_list[current_question]]);
				tv_right_answer.setText(choice[question_item.answer]);
			}
			txtv_time.setVisibility(View.INVISIBLE);
		}
		else {
			if (question_item.flag == QuestionItem.CHOICE_QUESTION) {
				btn_next_question.setEnabled(true);
				switch(answer_list[current_question]) {
				case 0: rg_choice.check(R.id.radio_A); break;
				case 1: rg_choice.check(R.id.radio_B); break;
				case 2: rg_choice.check(R.id.radio_C); break;
				case 3: rg_choice.check(R.id.radio_D); break;
				default: rg_choice.clearCheck(); break;
				}
				rg_choice.setVisibility(View.VISIBLE);
				rg_trueorfalse.setVisibility(View.INVISIBLE);
			}
			else {
				switch(answer_list[current_question]) {
				case 0: rg_trueorfalse.check(R.id.radio_right); break;
				case 1: rg_trueorfalse.check(R.id.radio_wrong); break;
				default: rg_trueorfalse.clearCheck(); break;
				}
				rg_choice.setVisibility(View.INVISIBLE);
				rg_trueorfalse.setVisibility(View.VISIBLE);		
			}
		}
		
		/* Update button */
		if ((mode == MODE_VIEW_WRONG && current_question == wrong_list[0]) || current_question == 0) {
			btn_prev_question.setEnabled(false);
		}
		else {
			btn_prev_question.setEnabled(true);
		}
		
		if ((mode == MODE_VIEW_WRONG && current_question == wrong_list[wrong_list.length - 1]) || 
				answer_list[current_question] == -1 || current_question == question_list.length - 1) {
			btn_next_question.setEnabled(false);
		}
		else {
			btn_next_question.setEnabled(true);
		}
		
		/* Update question image */
		if (question_item.src != 0) { /* have a image */
			imgv_picture.setImageResource(picsrc_list[question_item.src]);
		} else {
			imgv_picture.setImageDrawable(null);
		}
		
		updateView = false;
	}
	
	private void enterViewWrongMode() {
		int wrong_answer = 0;
		for(int i = 0; i < answer_list.length; i++) {
			if (answer_list[i] != -1 && answer_list[i] != question_items[question_list[i]].answer)
				wrong_answer++;
		}
		wrong_list = new int[wrong_answer];
		wrong_answer = 0;
		for(int i = 0; i < answer_list.length; i++) {
			if (answer_list[i] != -1 && answer_list[i] != question_items[question_list[i]].answer) {
				wrong_list[wrong_answer] = i;
				wrong_answer++;
			}
		}
		current_wrong = 0;
		current_question = wrong_list[current_wrong];
		mode = MODE_VIEW_WRONG;
		
		updateQuestionView();
	}
	
	private void disScoreDialog() {
		int right_answer = 0;
		for(int i = 0; i < answer_list.length; i++) {
			if (answer_list[i] == question_items[question_list[i]].answer)
				right_answer++;
		}
		
		saveWrongQuestions();
		mHandler.removeCallbacks(mUpdateTimeTask);
		
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.score_layout, null);
		TextView txtv_score = (TextView)view.findViewById(R.id.txtv_score);
		txtv_score.setText("" + right_answer);
		
		//get a builder and set the view
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.jiaojuan);
		builder.setView(view);
		builder.setPositiveButton(R.string.view_wrong, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int sumthin) {
					enterViewWrongMode();
				}
			});
		builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int sumthin) {
					QuestionView.this.finish();
				}
			});
		builder.setCancelable(false);
		builder.show();
	}
	
	private void genRandomQuestionsList() {
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < question_list.length; i++) {
			int pos = rand.nextInt(question_list.length);
			int temp = question_list[i];
			question_list[i] = question_list[pos];
			question_list[pos] = temp;
		}
	}
	
	private void genTestQuestionList() {
		Random rand = new Random(System.currentTimeMillis());
		int choice = 0;
		for (int i = 0; i < question_items.length; i++) {
			if (question_items[i].flag == QuestionItem.CHOICE_QUESTION)
				choice++;
		}

		int select = 60;
		int remaining = choice;
		int index = 0;
		for (int i = 0, j = 0; i < choice; i++) {
			while(question_items[index].flag != QuestionItem.CHOICE_QUESTION)
				index++;
			if (rand.nextInt(remaining) < select) {
				// 第i个选择题被选中
				question_list[j] = index;
				j++;
				select--;
			}
			remaining--;
			index++;
		}
		
		int trueorfalse = question_items.length - choice;
		select = 40;
		remaining = trueorfalse;
		index = 0;
		for (int i = 0, j = 60; i < trueorfalse; i++) {
			while(question_items[index].flag != QuestionItem.TRUE_OR_FALSE)
				index++;
			if (rand.nextInt(remaining) < select) {
				// 第i个判断题被选中
				question_list[j] = index;
				j++;
				select--;
			}
			remaining--;
			index++;
		}	
		
	}
	
	private boolean genStrongQuestionList() {
		try {
			FileInputStream fs = openFileInput(STRONG_FILE);
			byte[] wrong = new byte[question_items.length];
			fs.read(wrong);
			fs.close();
			
			int count = 0;
			for (int i = 0; i < wrong.length; i++) {
				if (wrong[i] == 1) {
					count++;
				}
			}
			
			if (count == 0) return false;
			
			question_list = new int[count];
			answer_list = new int[count];
			count = 0;
			for (int i = 0; i < wrong.length; i++) {
				if (wrong[i] == 1) {
					question_list[count] = i;
					answer_list[count] = -1;
					count++;
				}
			}
			
			return true;
		}
		catch (Throwable t) {
			return false;
		}
	}
	
	private void saveWrongQuestions() {
		byte[] wrong = new byte[question_items.length];
		try {
			FileInputStream fis = openFileInput(STRONG_FILE);
			if (fis != null) {
				fis.read(wrong);
				fis.close();
			}			
		}
		catch (Throwable t) {
		
		}		
		
		for (int i = 0; i < question_list.length; i++) {
			if ((answer_list[i] != -1) && (answer_list[i] != question_items[question_list[i]].answer)) {
				wrong[question_list[i]] = 1;
			}
			else if (answer_list[i] == question_items[question_list[i]].answer) {
				wrong[question_list[i]] = 0;
			}
		}
		
		try {
			FileOutputStream fos = openFileOutput(STRONG_FILE, Context.MODE_PRIVATE);
			fos.write(wrong);
			fos.close();
		}
		catch (Throwable t) {
			
		}
	}
	
	private boolean getQuestionList() {
        try {
        	InputStream in = getResources().openRawResource(R.raw.questions);
        	
        	if (in != null) {
        		byte[] buf;
        		
        		buf = new byte[4];
        		if (in.read(buf, 0, 4) != 4) return false;
        		if ((buf[0] != (byte)0xFF) || (buf[1] != (byte)0xFF)) return false;
        		int count = byteToInt(buf[2], buf[3]);
        		question_items = new QuestionItem[count];
        		buf = new byte[count * 10];
        		if (in.read(buf, 0, count * 10) != count * 10) return false;
        		for (int i = 0; i < count; i++) {
        			//int offset = (buf[i*10+0]<<24)|(buf[i*10+1]<<16)|(buf[i*10+2]<<8)|buf[i*10+3];
        			int length = byteToInt(buf[i*10+4],buf[i*10+5]);
        			int answer = buf[i*10+6];
        			int flag = buf[i*10+7];
        			int src = byteToInt(buf[i*10+8],buf[i*10+9]);
        			byte[] strbuf = new byte[length];
        			if (in.read(strbuf, 0, length) != length) return false;
        			String title = new String(strbuf, "UTF-8");
        			question_items[i] = new QuestionItem(title,answer,src, flag);
        		}
        		
        		in.close();
        		return true;
        	}
        	
        	return false;
        }
        catch (Throwable t) {
        	return false;
        }		
	}
	
	private void initImageSrcList() {
		int[] src = {
			0,
			R.drawable.pic1,
			R.drawable.pic2,
			R.drawable.pic3,
			R.drawable.pic4,
			R.drawable.pic5,
			R.drawable.pic6,
			R.drawable.pic7,
			R.drawable.pic8,
			R.drawable.pic9,
			R.drawable.pic10,
			R.drawable.pic11,
			R.drawable.pic12,
			R.drawable.pic13,
			R.drawable.pic14,
			R.drawable.pic15,
			R.drawable.pic16,
			R.drawable.pic17,
			R.drawable.pic18,
			R.drawable.pic19,
			R.drawable.pic20,
			R.drawable.pic21,
			R.drawable.pic22,
			R.drawable.pic23,
			R.drawable.pic24,
			R.drawable.pic25,
			R.drawable.pic26,
			R.drawable.pic27,
			R.drawable.pic28,
			R.drawable.pic29,
			R.drawable.pic30,
			R.drawable.pic31,
			R.drawable.pic32,
			R.drawable.pic33,
			R.drawable.pic34,
			R.drawable.pic35,
			R.drawable.pic36,
			R.drawable.pic37,
			R.drawable.pic38,
			R.drawable.pic39,
			R.drawable.pic40,
			R.drawable.pic41,
			R.drawable.pic42,
			R.drawable.pic43,
			R.drawable.pic44,
			R.drawable.pic45,
			R.drawable.pic46,
			R.drawable.pic47,
			R.drawable.pic48,
			R.drawable.pic49,
			R.drawable.pic50,
			R.drawable.pic51,
			R.drawable.pic52,
			R.drawable.pic53,
			R.drawable.pic54,
			R.drawable.pic55,
			R.drawable.pic56,
			R.drawable.pic57,
			R.drawable.pic58,
			R.drawable.pic59,
			R.drawable.pic60,
			R.drawable.pic61,
			R.drawable.pic62,
			R.drawable.pic63,
			R.drawable.pic64,
			R.drawable.pic65,
			R.drawable.pic66,
			R.drawable.pic67,
			R.drawable.pic68,
			R.drawable.pic69,
			R.drawable.pic70,
			R.drawable.pic71,
			R.drawable.pic72,
			R.drawable.pic73,
			R.drawable.pic74,
			R.drawable.pic75,
			R.drawable.pic76,
			R.drawable.pic77,
			R.drawable.pic78,
			R.drawable.pic79,
			R.drawable.pic80,
			R.drawable.pic81,
			R.drawable.pic82,
			R.drawable.pic83,
			R.drawable.pic84,
			R.drawable.pic85,
			R.drawable.pic86,
			R.drawable.pic87,
			R.drawable.pic88,
			R.drawable.pic89,
			R.drawable.pic90,
			R.drawable.pic91,
			R.drawable.pic92,
			R.drawable.pic93,
			R.drawable.pic94,
			R.drawable.pic95,
			R.drawable.pic96,
			R.drawable.pic97,
			R.drawable.pic98,
			R.drawable.pic99,
			R.drawable.pic100,
			R.drawable.pic101,
			R.drawable.pic102,
			R.drawable.pic103,
			R.drawable.pic104,
			R.drawable.pic105,
			R.drawable.pic106,
			R.drawable.pic107,
			R.drawable.pic108,
			R.drawable.pic109,
			R.drawable.pic110,
			R.drawable.pic111,
			R.drawable.pic112,
			R.drawable.pic113,
			R.drawable.pic114,
			R.drawable.pic115,
			R.drawable.pic116,
			R.drawable.pic117,
			R.drawable.pic118,
			R.drawable.pic119,
			R.drawable.pic120,
			R.drawable.pic121,
			R.drawable.pic122,
			R.drawable.pic123,
			R.drawable.pic124,
			R.drawable.pic125,
			R.drawable.pic126,
			R.drawable.pic127,
			R.drawable.pic128,
			R.drawable.pic129,
			R.drawable.pic130,
			R.drawable.pic131,
			R.drawable.pic132,
			R.drawable.pic133,
			R.drawable.pic134,
			R.drawable.pic135,
			R.drawable.pic136,
			R.drawable.pic137,
			R.drawable.pic138,
			R.drawable.pic139,
			R.drawable.pic140,
			R.drawable.pic141,
			R.drawable.pic142,
			R.drawable.pic143,
			R.drawable.pic144,
			R.drawable.pic145,
			R.drawable.pic146,
			R.drawable.pic147,
			R.drawable.pic148,
			R.drawable.pic149,
			R.drawable.pic150,
			R.drawable.pic151,
			R.drawable.pic152,
			R.drawable.pic153,
			R.drawable.pic154,
			R.drawable.pic155,
			R.drawable.pic156,
			R.drawable.pic157,
		};
		picsrc_list = src; 
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
	
	private boolean updateView;
	private static final int[] choice = {
		R.string.a,
		R.string.b,
		R.string.c,
		R.string.d
	};
	private static final int[] trueorfalse = {
		R.string.right,
		R.string.wrong
	};
	private class QuestionItem {
		public String title;
		public int answer;
		public int src;
		public int flag;
		
		public static final int CHOICE_QUESTION = 0;
		public static final int TRUE_OR_FALSE = 1;
		
		public QuestionItem(String title, int answer, int src, int flag) {
			this.title = title;
			this.answer = answer;
			this.src = src;
			this.flag = flag;
		}
	}
}
