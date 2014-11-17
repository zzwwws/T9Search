package com.handsomezhou.t9search.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.handsomezhou.t9search.main.T9SearchApplication;
import com.handsomezhou.t9search.model.Contacts;
import com.handsomezhou.t9search.model.Contacts.SearchByType;
import com.handsomezhou.t9search.model.PinyinUnit;
import com.handsomezhou.t9search.model.T9PinyinUnit;

public class ContactsHelper {
	private static final String TAG="ContactsHelper";
	private Context mContext;
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
	private OnContactsChanged mOnContactsChanged=null;
	private ContentObserver mContentObserver;
	private boolean mContactsChanged = true;
	private Handler mContactsHandler=new Handler();

	public interface OnContactsLoad {
		void onContactsLoadSuccess();

		void onContactsLoadFailed();
	}
	
	public interface OnContactsChanged{
		void onContactsChanged();
	}

	private ContactsHelper() {
		initContactsHelper();
		//registerContentObserver();
	}

	public static ContactsHelper getInstance() {
		if (null == mInstance) {
			mInstance = new ContactsHelper();
		}
		
		return mInstance;
	}

	public void destroy(){
		if(null!=mInstance){
			//unregisterContentObserver();
			mInstance=null;//the system will free other memory. 
		}
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

	
	private boolean isContactsChanged() {
		return mContactsChanged;
	}

	private void setContactsChanged(boolean contactsChanged) {
		mContactsChanged = contactsChanged;
	}

	/**
	 * Provides an function to start load contacts
	 * 
	 * @return start load success return true, otherwise return false
	 */
	public boolean startLoadContacts() {
		if (true == isSearching()) {
			return false;
		}
		
		if(false==isContactsChanged()){
			return false;
		}
		
		initContactsHelper();

		mLoadTask = new AsyncTask<Object, Object, List<Contacts>>() {

			@Override
			protected List<Contacts> doInBackground(Object... params) {
				return loadContacts(mContext);
			}

			@Override
			protected void onPostExecute(List<Contacts> result) {
				parseContacts(result);
				super.onPostExecute(result);
				setContactsChanged(false);
				mLoadTask = null;
			}
		}.execute();

		return true;
	}

	
	/**
	 * @description search base data according to string parameter
	 * @param search (valid characters include:'0'~'9','*','#')
	 * @return void
	 *
	 * 
	 */
	public void parseT9InputSearchContacts(String search){
		if(null==search){//add all base data to search
			if(null!=mSearchContacts){
				mSearchContacts.clear();
			}else{
				mSearchContacts=new ArrayList<Contacts>();
			}
			
			for(Contacts contacts:mBaseContacts){
				contacts.setSearchByType(SearchByType.SearchByNull);
				contacts.clearMatchKeywords();
			}
			
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
		
		if(null!=mSearchContacts){
			mSearchContacts.clear();	
		}else{
			mSearchContacts=new ArrayList<Contacts>();
		}
		
		int contactsCount=mBaseContacts.size();
		
		/**
		 * search process:
		 * 1:Search by name
		 *  (1)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9','*','#')
		 *  (2)Search by org name		('0'~'9','*','#')
		 * 2:Search by phone number		('0'~'9','*','#')
		 */
		for(int i=0; i<contactsCount; i++){
			
			List<PinyinUnit> pinyinUnits=mBaseContacts.get(i).getNamePinyinUnits();
			StringBuffer chineseKeyWord=new StringBuffer();//In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
			String name=mBaseContacts.get(i).getName();
			if(true==matchPinyinUnits(pinyinUnits,name,search,chineseKeyWord)){//search by NamePinyinUnits;
				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).setMatchKeywords(chineseKeyWord.toString());
				chineseKeyWord.delete(0, chineseKeyWord.length());
				mSearchContacts.add(mBaseContacts.get(i));
				continue;
			}else{
					if(mBaseContacts.get(i).getPhoneNumber().contains(search)){	//search by phone number
						mBaseContacts.get(i).setSearchByType(SearchByType.SearchByPhoneNumber);
						mBaseContacts.get(i).setMatchKeywords(search);
						mSearchContacts.add(mBaseContacts.get(i));
						continue;
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
		mContext=T9SearchApplication.getContextObject();
		setContactsChanged(true);
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
	
	private void registerContentObserver(){
		if(null==mContentObserver){
			mContentObserver=new ContentObserver(mContactsHandler) {

				@Override
				public void onChange(boolean selfChange) {
					// TODO Auto-generated method stub
					setContactsChanged(true);
					if(null!=mOnContactsChanged){
						Log.i("ActivityTest","mOnContactsChanged mContactsChanged="+mContactsChanged);
						mOnContactsChanged.onContactsChanged();
					}
					super.onChange(selfChange);
				}
				
			};
		}
		
		if(null!=mContext){
			mContext.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true,
					mContentObserver);
		}
	}
	
	private void unregisterContentObserver(){
		if(null!=mContentObserver){
			if(null!=mContext){
				mContext.getContentResolver().unregisterContentObserver(mContentObserver);
			}
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
	
	/**
	 * @description match Pinyin Units
	 * @param pinyinUnits		
	 * @param baseData   		the original string which be parsed to PinyinUnit
	 * @param search			search key words
	 * @param chineseKeyWord	the sub string of base data
	 * @return true if match success,false otherwise. 
	 */
	public static boolean matchPinyinUnits(final List<PinyinUnit> pinyinUnits,final String baseData, String search,StringBuffer chineseKeyWord){
		if((null==pinyinUnits)||(null==search)||(null==chineseKeyWord)){
			return false;
		}
		
		StringBuffer matchSearch=new StringBuffer();
		matchSearch.delete(0, matchSearch.length());
		chineseKeyWord.delete(0, chineseKeyWord.length());
		PinyinUnit pyUnit=null;
		
		int pinyinUnitsLength=0;
		pinyinUnitsLength=pinyinUnits.size();
		StringBuffer searchBuffer=new StringBuffer();
		for(int i=0; i<pinyinUnitsLength; i++){
			pyUnit=pinyinUnits.get(i);
			for(int j=0; j<pyUnit.getT9PinyinUnitIndex().size(); j++){
				chineseKeyWord.delete(0, chineseKeyWord.length());
				searchBuffer.delete(0, searchBuffer.length());
				searchBuffer.append(search);
				boolean found=findPinyinUnits(pinyinUnits, i, j, baseData, searchBuffer, chineseKeyWord);
				if(true==found){
					return true;
				}
			}
		}
					
		return false;
	}
	
	/**
	 * @param pinyinUnits		pinyinUnits head node index
	 * @param pinyinUnitIndex   pinyinUint Index
	 * @param t9PinyinUnitIndex t9PinyinUnit Index 
	 * @param baseData			base data for search.
	 * @param searchBuffer		search keyword.
	 * @param chineseKeyWord	save the Chinese keyword.
	 * @return true if find,false otherwise.
	 */
	private static boolean findPinyinUnits(final List<PinyinUnit> pinyinUnits,int pinyinUnitIndex,int t9PinyinUnitIndex,final String baseData, StringBuffer searchBuffer,StringBuffer chineseKeyWord ){
		if((null==pinyinUnits)||(null==baseData)||(null==searchBuffer)||(null==chineseKeyWord)){
			return false;
		}
		
		String search=searchBuffer.toString();
		if(search.length()<=0){	//match success
			return true;
		}
		
		if(pinyinUnitIndex>=pinyinUnits.size()){
			return false;
		}
		PinyinUnit pyUnit=pinyinUnits.get(pinyinUnitIndex);
		
		if(t9PinyinUnitIndex>=pyUnit.getT9PinyinUnitIndex().size()){
			return false;
		}
		
		T9PinyinUnit t9PinyinUnit=pyUnit.getT9PinyinUnitIndex().get(t9PinyinUnitIndex);
		
		
		
		if(pyUnit.isPinyin()){
			
			if(search.startsWith(String.valueOf(t9PinyinUnit.getNumber().charAt(0)))){// match pinyin first character
				searchBuffer.delete(0,1);//delete the match character
				chineseKeyWord.append(baseData.charAt(pyUnit.getStartPosition()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWord);
				if(true==found){
					return true; 
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber().charAt(0));
					chineseKeyWord.deleteCharAt(chineseKeyWord.length()-1);
				}
				
			}
			
			if(t9PinyinUnit.getNumber().startsWith(search)){
				//The string of "search" is the string of t9PinyinUnit.getNumber() of a subset. means match success.
				chineseKeyWord.append(baseData.charAt(pyUnit.getStartPosition()));
				searchBuffer.delete(0, searchBuffer.length());	
				return true;
				
			}else if(search.startsWith(t9PinyinUnit.getNumber())){ //match quanpin  success
				//The string of t9PinyinUnit.getNumber() is the string of "search" of a subset.
				searchBuffer.delete(0, t9PinyinUnit.getNumber().length());
				chineseKeyWord.append(baseData.charAt(pyUnit.getStartPosition()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWord);
				if(true==found){
					return true;
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber());
					chineseKeyWord.deleteCharAt(chineseKeyWord.length()-1);
				}
			}else{ //mismatch
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex, t9PinyinUnitIndex+1, baseData, searchBuffer, chineseKeyWord);
				if(found==true){
					return true;
				}
			}
			
		}else{ //non-pure Pinyin
			
			if(t9PinyinUnit.getNumber().startsWith(search)){
				//The string of "search" is the string of t9PinyinUnit.getNumber() of a subset.
				int startIndex=0; 
				chineseKeyWord.append(baseData.substring(startIndex+pyUnit.getStartPosition(),startIndex+pyUnit.getStartPosition()+ search.length()));
				searchBuffer.delete(0, searchBuffer.length());
				return true;
			}else if(search.startsWith(t9PinyinUnit.getNumber())){ //match all non-pure pinyin 
				//The string of t9PinyinUnit.getNumber() is the string of "search" of a subset.
				int startIndex=0; 
				searchBuffer.delete(0, t9PinyinUnit.getNumber().length());
				chineseKeyWord.append(baseData.substring(startIndex+pyUnit.getStartPosition(),startIndex+pyUnit.getStartPosition()+ t9PinyinUnit.getNumber().length()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWord);
				if(true==found){
					return true;
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber());
					chineseKeyWord.delete(chineseKeyWord.length()-t9PinyinUnit.getNumber().length(), chineseKeyWord.length());
				}
			}else if((chineseKeyWord.length()<=0)){
				if(t9PinyinUnit.getNumber().contains(search)){
					int index=t9PinyinUnit.getNumber().indexOf(search);
					chineseKeyWord.append(baseData.substring(index+pyUnit.getStartPosition(),index+pyUnit.getStartPosition()+ search.length()));
					searchBuffer.delete(0, searchBuffer.length());
					return true;
				}else{
//					 match case:[Non-Chinese characters]+[Chinese characters]
//					 for example:baseData="Tony测试"; match this case:"onycs"<===>"66927" 
					//start [Non-Chinese characters]+[Chinese characters]
					int numLength=t9PinyinUnit.getNumber().length();
					for(int i=0; i<numLength; i++){
						String subStr=t9PinyinUnit.getNumber().substring(i);
						if(search.startsWith(subStr)){
							searchBuffer.delete(0, subStr.length());
							chineseKeyWord.append(baseData.substring(i+pyUnit.getStartPosition(), i+pyUnit.getStartPosition()+subStr.length()));
							boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWord);
							if(true==found){
								return true;
							}else{
								searchBuffer.insert(0, t9PinyinUnit.getNumber().substring(i));
								chineseKeyWord.delete(chineseKeyWord.length()-subStr.length(), chineseKeyWord.length());
							}
							
						}
					}
					//end [Non-Chinese characters]+[Chinese characters]
					
					//in fact,if pyUnit.isPinyin()==false, pyUnit.getT9PinyinUnitIndex().size()==1. The function of findPinyinUnits() will return false.
					boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex, t9PinyinUnitIndex+1, baseData, searchBuffer, chineseKeyWord);
					if(found==true){
						return true;
					}
				}
			}else { //mismatch
				//in fact,if pyUnit.isPinyin()==false, pyUnit.getT9PinyinUnitIndex().size()==1.  The function of findPinyinUnits() will return false.
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex, t9PinyinUnitIndex+1, baseData, searchBuffer, chineseKeyWord);
				if(found==true){
					return true;
				}
			}
		}
		return false;
	}
}
