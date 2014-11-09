package com.handsomezhou.t9search.model;

public class Contacts {
	private String mName;
	private String mPhoneNumber;
	
	public Contacts(String name, String phoneNumber) {
		//super();
		mName = name;
		mPhoneNumber = phoneNumber;
	}

	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getPhoneNumber() {
		return mPhoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}
}
