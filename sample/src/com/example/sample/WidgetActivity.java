package com.example.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.guo.android_extend.widget.VerticalSeekBar;

public class WidgetActivity extends Activity {
	@SuppressWarnings("unused")
	private final String TAG = this.getClass().toString();

	VerticalSeekBar seekbar;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_widget);
		
		seekbar = (VerticalSeekBar) this.findViewById(R.id.vsb);
		seekbar.setProgress(80);
		
		CheckBox box = (CheckBox) this.findViewById(R.id.checkBox1);
		box.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					seekbar.setProgress(80);
				} else {
					seekbar.setProgress(11);
				}
			}
			
		});
	}
}
