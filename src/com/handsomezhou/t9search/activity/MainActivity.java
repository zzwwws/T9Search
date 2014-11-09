package com.handsomezhou.t9search.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.handsomezhou.t9search.R;
import com.handsomezhou.t9search.View.T9TelephoneDialpadView;
import com.handsomezhou.t9search.View.T9TelephoneDialpadView.OnT9TelephoneDialpadView;

public class MainActivity extends Activity implements OnT9TelephoneDialpadView{
	private Context mContext;
	private ListView mContactsLv;
	private  T9TelephoneDialpadView mT9TelephoneDialpadView;
	private Button mDialpadOperationBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext=this;
		initData();
		initView();
		initListener();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initData() {

	}

	private void initView() {
		mContactsLv=(ListView) findViewById(R.id.contacts_list_view);
		mT9TelephoneDialpadView=(T9TelephoneDialpadView) findViewById(R.id.t9_telephone_dialpad_layout);
		mT9TelephoneDialpadView.setOnT9TelephoneDialpadView(this);

		mDialpadOperationBtn = (Button) findViewById(R.id.dialpad_operation_btn);
		mDialpadOperationBtn.setText(R.string.hide_keyboard);

	}

	private void initListener() {
		mDialpadOperationBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clickDialpad();
			}
		});
	}

	private void clickDialpad() {
		if (mT9TelephoneDialpadView.getT9TelephoneDialpadViewVisibility() == View.VISIBLE) {
			mT9TelephoneDialpadView.hideT9TelephoneDialpadView();
			mDialpadOperationBtn.setText(R.string.display_keyboard);
		} else {
			mT9TelephoneDialpadView.showT9TelephoneDialpadView();
			mDialpadOperationBtn.setText(R.string.hide_keyboard);
		}
	}

	@Override
	public void onAddDialCharacter(String addCharacter) {
		//Toast.makeText(mContext, "onAddDialCharacter"+"("+addCharacter+")", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeleteDialCharacter(String deleteCharacter) {
		//Toast.makeText(mContext, "onDeleteDialCharacter"+"("+deleteCharacter+")", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDialInputTextChanged(String curCharacter) {
		//Toast.makeText(mContext, "onDialInputTextChanged"+"("+curCharacter+")", Toast.LENGTH_SHORT).show();
		
	}
}
