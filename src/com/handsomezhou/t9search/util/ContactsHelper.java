package com.handsomezhou.t9search.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.provider.ContactsContract;
import android.util.Log;

import com.handsomezhou.t9search.model.Contacts;
import com.handsomezhou.t9search.model.PinyinUnit;

public class ContactsHelper {
	private static final String TAG="ContactsHelper";
	private static ContactsHelper mInstance = null;
	private List<Contacts> mBaseContacts = null;	//The basic data used for the search
	private List<Contacts> mSearchContacts=null;	//The search results from the basic data
	/*save the first input string which search no result.
		mFirstNoSearchResultInput.size<=0, means that the first input string which search no result not appear.
		mFirstNoSearchResultInput.size>0, means that the first input string which search no result has appeared, 
		it's  mFirstNoSearchResultInput.toString(). 
		We can reduce the number of search basic data by the first input string which search no result.
	*/
	private StringBuffer  mFirstNoSearchResultInput=null;
	private AsyncTask<Object, Object, List<Contacts>> mLoadTask = null;
	private OnContactsLoad mOnContactsLoad = null;

	public interface OnContactsLoad {
		void onContactsLoadSuccess();

		void onContactsLoadFailed();
	}

	private ContactsHelper() {
		initContactsHelper();
	}

	public static ContactsHelper getInstance() {
		if (null == mInstance) {
			mInstance = new ContactsHelper();
		}

		return mInstance;
	}

	public List<Contacts> getBaseContacts() {
		return mBaseContacts;
	}

	// public void setBaseContacts(List<Contacts> baseContacts) {
	// mBaseContacts = baseContacts;
	// }
	
	public List<Contacts> getSearchContacts() {
		return mSearchContacts;
	}

//	public void setSearchContacts(List<Contacts> searchContacts) {
//		mSearchContacts = searchContacts;
//	}

	public OnContactsLoad getOnContactsLoad() {
		return mOnContactsLoad;
	}

	public void setOnContactsLoad(OnContactsLoad onContactsLoad) {
		mOnContactsLoad = onContactsLoad;
	}

	/**
	 * Provides an function to start load contacts
	 * 
	 * @return start load success return true, otherwise return false
	 */
	public boolean startLoadContacts(final Context context) {
		if (true == isSearching()) {
			return false;
		}

		initContactsHelper();

		mLoadTask = new AsyncTask<Object, Object, List<Contacts>>() {

			@Override
			protected List<Contacts> doInBackground(Object... params) {
				return loadContacts(context);
			}

			@Override
			protected void onPostExecute(List<Contacts> result) {
				parseContacts(result);
				super.onPostExecute(result);
				mLoadTask = null;
			}
		}.execute();

		return true;
	}

	
	/**
	 * @description search base data according to string parameter
	 *  search process:
	 *  1:Search by phone number	('0'~'9','*','#')
	 *  2:Search by name
	 *  (1)Search by org name		('0'~'9','*','#')
	 *  (2)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9')
	 * @param search (valid characters include:'0'~'9','*','#')
	 *
	 * 
	 */
	public void parseT9InputSearchContacts(String search){
		if(null==search){//add all base data to search
			mSearchContacts.addAll(mBaseContacts);
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			Log.i(TAG,"null==search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length());
			return;
		}
		
		if(mFirstNoSearchResultInput.length()>0){
			if(search.contains(mFirstNoSearchResultInput.toString())){
				Log.i(TAG,"no need  to search,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+search.length()+"["+search+"]");
				return;
			}else{
				Log.i(TAG,"delete  mFirstNoSearchResultInput, null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+search.length()+"["+search+"]");
				mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			}
		}
		
		if(null==mSearchContacts){
			mSearchContacts=new ArrayList<Contacts>();
		}else{
			mSearchContacts.clear();	
		}
		
		int contactsCount=mBaseContacts.size();
		for(int i=0; i<contactsCount; i++){
			if(mBaseContacts.get(i).getPhoneNumber().contains(search)){	//search by phone number
				mSearchContacts.add(mBaseContacts.get(i));
				continue;
			}else{
				if(mBaseContacts.get(i).getName().contains(search)){//search by org name;
					mSearchContacts.add(mBaseContacts.get(i));
					continue;
				}else{
				
					List<PinyinUnit> pinyinUnits=mBaseContacts.get(i).getNamePinyinUnits();
					if(true==matchPinyinUnits(pinyinUnits,search)){
						mSearchContacts.add(mBaseContacts.get(i));
						continue;
					}
				}
			}
		}
		
		if(mSearchContacts.size()<=0){
			if(mFirstNoSearchResultInput.length()<=0){
				mFirstNoSearchResultInput.append(search);
				Log.i(TAG,"no search result,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+search.length()+"["+search+"]");
			}else{
				
			}
		}
		
	}
	
	
	private void initContactsHelper(){
		if (null == mBaseContacts) {
			mBaseContacts = new ArrayList<Contacts>();
		} else {
			mBaseContacts.clear();
		}
		
		if(null==mSearchContacts){
			mSearchContacts=new ArrayList<Contacts>();
		}else{
			mSearchContacts.clear();
		}
		
		if(null==mFirstNoSearchResultInput){
			mFirstNoSearchResultInput=new StringBuffer();
		}else{
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
		}
	}
	
	private boolean isSearching() {
		return (mLoadTask != null && mLoadTask.getStatus() == Status.RUNNING);
	}

	private List<Contacts> loadContacts(Context context) {

		List<Contacts> contacts = new ArrayList<Contacts>();
		Contacts cs = null;
		Cursor cursor = null;
		try {

			cursor = context.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, "sort_key");
			//Reference:	http://blog.chinaunix.net/uid-26930580-id-4137246.html
			// cursor = context.getContentResolver().query(
			// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]
			// { "display_name", "sort_key", "contact_id",
			// "data1" },
			// null, null, "sort_key");
			// String displayName =cursor.getString(0);
			// String phoneNumber = cursor.getString(1);

			while (cursor.moveToNext()) {
				String displayName = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String phoneNumber = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				
				cs = new Contacts(displayName, phoneNumber);
				PinyinUtil.chineseStringToPinyinUnit(cs.getName(), cs.getNamePinyinUnits());
				
				contacts.add(cs);
			}
		} catch (Exception e) {

		} finally {
			if (null != cursor) {
				cursor.close();
				cursor = null;
			}
		}

		return contacts;
	}

	private void parseContacts(List<Contacts> contacts) {
		if (null == contacts || contacts.size() < 1) {
			if (null != mOnContactsLoad) {
				mOnContactsLoad.onContactsLoadFailed();
			}
			return;
		}

		for (Contacts contact : contacts) {
			if (!mBaseContacts.contains(contact)) {
				mBaseContacts.add(contact);
			}
		}

		if (null != mOnContactsLoad) {
			parseT9InputSearchContacts(null);
			mOnContactsLoad.onContactsLoadSuccess();
		}

		return;
	}
	
	private boolean matchPinyinUnits(final List<PinyinUnit> pinyinUnits, String search){
		
		return false;
	}
	

}
