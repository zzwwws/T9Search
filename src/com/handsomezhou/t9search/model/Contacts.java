package com.handsomezhou.t9search.model;

import java.util.ArrayList;
import java.util.List;

public class Contacts {
	private String mName;
	private List<PinyinUnit> mNamePinyinUnits;	//save the mName converted to Pinyin characters.
	private String mPhoneNumber;
	
	
	public Contacts(String name, String phoneNumber) {
		//super();
		mName = name;
		mPhoneNumber = phoneNumber;
		setNamePinyinUnits(new ArrayList<PinyinUnit>());
	}

	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public List<PinyinUnit> getNamePinyinUnits() {
		return mNamePinyinUnits;
	}

	public void setNamePinyinUnits(List<PinyinUnit> namePinyinUnits) {
		mNamePinyinUnits = namePinyinUnits;
	}
	
	public String getPhoneNumber() {
		return mPhoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}
}
