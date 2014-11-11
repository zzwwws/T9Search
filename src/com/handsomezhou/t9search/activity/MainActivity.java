package com.handsomezhou.t9search.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handsomezhou.t9search.R;
import com.handsomezhou.t9search.adapter.ContactsAdapter;
import com.handsomezhou.t9search.model.PinyinUnit;
import com.handsomezhou.t9search.util.ContactsHelper;
import com.handsomezhou.t9search.util.ContactsHelper.OnContactsLoad;
import com.handsomezhou.t9search.view.T9TelephoneDialpadView;
import com.handsomezhou.t9search.view.T9TelephoneDialpadView.OnT9TelephoneDialpadView;

/**
 * @description Main activity
 * @author handsomezhou
 * @date 2014.11.09
 */
public class MainActivity extends Activity implements OnT9TelephoneDialpadView,
		OnContactsLoad {
	private static final String TAG = "MainActivity";
	private static final int DIAL_INPUT_INIT_CAPACITY = 128;
	private Context mContext;
	private ListView mContactsLv;
	private View mLoadContactsView;
	private TextView mSearchResultPromptTv;
	private T9TelephoneDialpadView mT9TelephoneDialpadView;
	private Button mDialpadOperationBtn;

	private ContactsAdapter mContactsAdapter;
	private StringBuffer mDialInputStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		initView();
		initData();
		initListener();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		mContactsLv = (ListView) findViewById(R.id.contacts_list_view);
		mLoadContactsView = findViewById(R.id.load_contacts);
		mSearchResultPromptTv = (TextView) findViewById(R.id.search_result_prompt_text_view);
		mT9TelephoneDialpadView = (T9TelephoneDialpadView) findViewById(R.id.t9_telephone_dialpad_layout);
		mT9TelephoneDialpadView.setOnT9TelephoneDialpadView(this);

		mDialpadOperationBtn = (Button) findViewById(R.id.dialpad_operation_btn);
		mDialpadOperationBtn.setText(R.string.hide_keyboard);

		showView(mContactsLv);
		hideView(mLoadContactsView);
		hideView(mSearchResultPromptTv);

	}

	private void initData() {
		ContactsHelper.getInstance().setOnContactsLoad(this);
		boolean startLoad = ContactsHelper.getInstance().startLoadContacts(
				mContext);
		if (true == startLoad) {
			showView(mLoadContactsView);
		}
		mContactsAdapter = new ContactsAdapter(mContext,
				R.layout.contacts_list_item, ContactsHelper.getInstance()
						.getBaseContacts());
		mContactsLv.setAdapter(mContactsAdapter);
		mDialInputStr = new StringBuffer(DIAL_INPUT_INIT_CAPACITY);
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
		// Toast.makeText(mContext, "onAddDialCharacter"+"("+addCharacter+")",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeleteDialCharacter(String deleteCharacter) {
		// Toast.makeText(mContext,
		// "onDeleteDialCharacter"+"("+deleteCharacter+")",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDialInputTextChanged(String curCharacter) {
		// Toast.makeText(mContext,
		// "onDialInputTextChanged"+"("+curCharacter+")",
		// Toast.LENGTH_SHORT).show();
		mDialInputStr.delete(0, mDialInputStr.length());
		mDialInputStr.append(curCharacter);

		Toast.makeText(
				mContext,
				"onDialInputTextChanged" + "(" + mDialInputStr.toString() + ")",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onContactsLoadSuccess() {
		hideView(mLoadContactsView);
		showView(mContactsLv);
		
		int contactsCount=ContactsHelper.getInstance().getBaseContacts().size();
		for(int i=0; i<contactsCount; i++){
			String name=ContactsHelper.getInstance().getBaseContacts().get(i).getName();
			Log.i(TAG,"iiiiiiiiiiiiiiiiiiiiiiiiiiiname:["+name+"]"+"++++++++++++++++++++++++++++++");
			List<PinyinUnit> pinyinUnit=ContactsHelper.getInstance().getBaseContacts().get(i).getNamePinyinUnits();
			int pinyinUnitCount=pinyinUnit.size();
			for(int j=0; j<pinyinUnitCount; j++){
				PinyinUnit pyUnit=pinyinUnit.get(j);
				Log.i(TAG,"jjjjjjjjjjjjjjjjjjjjjjjjj="+j+"isPinyin["+pyUnit.isPinyin()+"]" );
				List<String> stringIndex=pyUnit.getStringIndex();
				int stringIndexLength=stringIndex.size();
				for(int k=0; k<stringIndexLength; k++){
					Log.i(TAG,"kkkkkkkkkkkkkkkkkkkkkkkkkkk="+k+"["+stringIndex.get(k)+"]" );
				}
				
			}
			
			
		}
		
	}

	@Override
	public void onContactsLoadFailed() {

		hideView(mLoadContactsView);
		showView(mContactsLv);
	}

	private void hideView(View view) {
		if (null == view) {
			return;
		}
		if (View.GONE != view.getVisibility()) {
			view.setVisibility(View.GONE);
		}

		return;
	}

	private int getViewVisibility(View view) {
		if (null == view) {
			return View.GONE;
		}

		return view.getVisibility();
	}

	private void showView(View view) {
		if (null == view) {
			return;
		}

		if (View.VISIBLE != view.getVisibility()) {
			view.setVisibility(View.VISIBLE);
		}
	}

}
