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
import com.handsomezhou.t9search.model.Contacts.SearchByType;
import com.handsomezhou.t9search.model.PinyinUnit;
import com.handsomezhou.t9search.model.T9PinyinUnit;

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
		
/*		for(int i=0; i<contactsCount; i++){
			if(mBaseContacts.get(i).getPhoneNumber().startsWith(search)){	//contains(search)search by phone number
				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByPhoneNumber);
				mBaseContacts.get(i).setMatchKeywords(search);
				mSearchContacts.add(mBaseContacts.get(i));
				continue;
			}else{
				if(mBaseContacts.get(i).getName().contains(search)){//search by org name;
					mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
					mBaseContacts.get(i).setMatchKeywords(search);
					mSearchContacts.add(mBaseContacts.get(i));
					continue;
				}else{
				
					List<PinyinUnit> pinyinUnits=mBaseContacts.get(i).getNamePinyinUnits();
					StringBuffer chineseKeyWords=new StringBuffer();//In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
					String name=mBaseContacts.get(i).getName();
					if(true==matchPinyinUnits(pinyinUnits,name,search,chineseKeyWords)){//search by NamePinyinUnits;
						mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
						mBaseContacts.get(i).setMatchKeywords(chineseKeyWords.toString());
						chineseKeyWords.delete(0, chineseKeyWords.length());
						mSearchContacts.add(mBaseContacts.get(i));
						continue;
					}
				}
			}
		}*/
		for(int i=0; i<contactsCount; i++){
			
			List<PinyinUnit> pinyinUnits=mBaseContacts.get(i).getNamePinyinUnits();
			StringBuffer chineseKeyWords=new StringBuffer();//In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
			String name=mBaseContacts.get(i).getName();
			if(true==matchPinyinUnits(pinyinUnits,name,search,chineseKeyWords)){//search by NamePinyinUnits;
				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).setMatchKeywords(chineseKeyWords.toString());
				chineseKeyWords.delete(0, chineseKeyWords.length());
				mSearchContacts.add(mBaseContacts.get(i));
				continue;
			}else{
				if(mBaseContacts.get(i).getName().contains(search)){//search by org name;
					mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
					mBaseContacts.get(i).setMatchKeywords(search);
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
	
	/**
	 * @description
	 * @param pinyinUnits		
	 * @param baseData   		the original string which be parsed to PinyinUnit
	 * @param search			search key words('0'~'9')
	 * @param chineseKeyWords	the sub string of base data
	 * @return
	 */
	private boolean matchPinyinUnits(final List<PinyinUnit> pinyinUnits,final String baseData, String search,StringBuffer chineseKeyWords){
		if((null==pinyinUnits)||(null==search)||(null==chineseKeyWords)){
			return false;
		}
		StringBuffer matchSearch=new StringBuffer();
		matchSearch.delete(0, matchSearch.length());
		chineseKeyWords.delete(0, chineseKeyWords.length());
		PinyinUnit pyUnit=null;
		
		int pinyinUnitsLength=pinyinUnits.size();
		
		//The string of "search" is the string of pyUnit.getT9PinyinUnitIndex().get(x).getNumber() of a subset.
		for(int i=0; i<pinyinUnitsLength; i++){
			pyUnit=pinyinUnits.get(i);
			for(int j=0; j<pyUnit.getT9PinyinUnitIndex().size(); j++){
				T9PinyinUnit t9PinyinUnit=pyUnit.getT9PinyinUnitIndex().get(j);
				if(pyUnit.isPinyin()){
					if(t9PinyinUnit.getNumber().startsWith(search)){
						chineseKeyWords.delete(0, chineseKeyWords.length());
						chineseKeyWords.append(baseData.charAt(pinyinUnits.get(i).getStartPosition()));
						return true;
					}
				}else{
					/**
					 * match from any position
					 */
					int index=t9PinyinUnit.getNumber().indexOf(search);
					if(index>=0){
						Log.i(TAG,"t9PinyinUnit.getNumber()=["+t9PinyinUnit.getNumber()+"]  search=["+search+"]"+"index=["+index+"]t9PinyinUnit.getPinyin()=["+t9PinyinUnit.getPinyin()+"]" );
						chineseKeyWords.delete(0, chineseKeyWords.length());
						String keyWords=t9PinyinUnit.getPinyin().substring(index, index+search.length());
						
						Log.i(TAG,"t9PinyinUnit.getNumber()=["+t9PinyinUnit.getNumber()+"]  search=["+search+"]"+"index=["+index+"]t9PinyinUnit.getPinyin()=["+t9PinyinUnit.getPinyin()+"]"+"keyWords=["+keyWords+"]" );
						chineseKeyWords.append(keyWords);
						return true;
					}
					/**
					 * match from start position
					if(t9PinyinUnit.getNumber().startsWith(search)){
						String keyWords=baseData.substring(0, search.length());
						chineseKeyWords.append(keyWords);
					}
					*/
				}
			}
		}
		//The string of pyUnit.getT9PinyinUnitIndex().get(x).getNumber() is the string of "search" of a subset.
		//And the string of "search" is not equal the string of pyUnit.getT9PinyinUnitIndex().get(x).getNumber().
		pinyinUnitsLength=pinyinUnits.size();
		StringBuffer searchBuffer=new StringBuffer();
		for(int i=0; i<pinyinUnitsLength; i++){
			pyUnit=pinyinUnits.get(i);
			for(int j=0; j<pyUnit.getT9PinyinUnitIndex().size(); j++){
				chineseKeyWords.delete(0, chineseKeyWords.length());
				searchBuffer.delete(0, searchBuffer.length());
				searchBuffer.append(search);
				boolean found=findPinyinUnits(pinyinUnits, i, j, baseData, searchBuffer, chineseKeyWords);
				if(true==found){
					return true;
				}
			}
		}
		
		
		
		return false;
	}
	
	private boolean findPinyinUnits(final List<PinyinUnit> pinyinUnits,int pinyinUnitIndex,int t9PinyinUnitIndex,final String baseData, StringBuffer searchBuffer,StringBuffer chineseKeyWords ){
		if((null==pinyinUnits)||(null==baseData)||(null==searchBuffer)||(null==chineseKeyWords)){
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
				chineseKeyWords.append(baseData.charAt(pyUnit.getStartPosition()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWords);
				if(true==found){
					return true; 
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber().charAt(0));
					chineseKeyWords.deleteCharAt(chineseKeyWords.length()-1);
				}
				
			}
			
			if(t9PinyinUnit.getNumber().startsWith(search)){
				//The string of "search" is the string of t9PinyinUnit.getNumber() of a subset. means match success.
				chineseKeyWords.append(baseData.charAt(pyUnit.getStartPosition()));
				searchBuffer.delete(0, searchBuffer.length());	
				return true;
				
			}else if(search.startsWith(t9PinyinUnit.getNumber())){ //match quanpin  success
				//The string of t9PinyinUnit.getNumber() is the string of "search" of a subset.
				searchBuffer.delete(0, t9PinyinUnit.getNumber().length());
				chineseKeyWords.append(baseData.charAt(pyUnit.getStartPosition()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWords);
				if(true==found){
					return true;
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber());
					chineseKeyWords.deleteCharAt(chineseKeyWords.length()-1);
				}
			}else{ //mismatch
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex, t9PinyinUnitIndex+1, baseData, searchBuffer, chineseKeyWords);
				if(found==true){
					return true;
				}
			}
			
		}else{ //non-pure Pinyin
			
			if(t9PinyinUnit.getNumber().startsWith(search)){
				//The string of "search" is the string of t9PinyinUnit.getNumber() of a subset.
				chineseKeyWords.append(baseData.substring(pyUnit.getStartPosition(),pyUnit.getStartPosition()+ search.length()));
				searchBuffer.delete(0, searchBuffer.length());
				return true;
			}else if(search.startsWith(t9PinyinUnit.getNumber())){ //match all non-pure pinyin 
				//The string of t9PinyinUnit.getNumber() is the string of "search" of a subset.
				searchBuffer.delete(0, t9PinyinUnit.getNumber().length());
				chineseKeyWords.append(baseData.substring(pyUnit.getStartPosition(),pyUnit.getStartPosition()+ t9PinyinUnit.getNumber().length()));
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex+1, 0, baseData, searchBuffer, chineseKeyWords);
				if(true==found){
					return true;
				}else{
					searchBuffer.insert(0, t9PinyinUnit.getNumber());
					chineseKeyWords.delete(chineseKeyWords.length()-t9PinyinUnit.getNumber().length(), chineseKeyWords.length());
				}
			}//else if((chineseKeyWords.length()<=0)){}
			else { //mismatch
				boolean found=findPinyinUnits(pinyinUnits, pinyinUnitIndex, t9PinyinUnitIndex+1, baseData, searchBuffer, chineseKeyWords);
				if(found==true){
					return true;
				}
			}
		}
		return false;
	}
}
