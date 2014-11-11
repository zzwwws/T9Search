package com.handsomezhou.t9search.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @description PinyinUnit as a base data structure to save the string that Chinese characters  converted to Pinyin characters.
 * for example:
 * Reference: http://www.cnblogs.com/bomo/archive/2012/12/25/2833081.html
 * Chinese characters:"你说了什么???"
 * Pinyin:
 * 		你->ni3					===>mPinyin=true, 	mStringIndex.size=1,	mStringIndex[0]="ni";
 *     	说->shuo1,shui4,yue4 	===>mPinyin=true,	mStringIndex.size=3,	mStringIndex[0]="shuo",mStringIndex[1]="shui",mStringIndex[2]="yue",
 *      了->le5,liao3,liao4  	===>mPinyin=true, 	mStringIndex.size=2,	mStringIndex[0]="le",mStringIndex[1]="liao";
 * 		什->shen2,shi2,she2		===>mPinyin=true, 	mStringIndex.size=3,	mStringIndex[0]="shen",mStringIndex[1]="shi",mStringIndex[2]="she",
 * 		么->me5,ma5,yao1			===>mPinyin=true,	mStringIndex.size=4,	mStringIndex[0]="me",mStringIndex[1]="ma",mStringIndex[2]="yao",
 * 		???->???				===>mPinyin=false, 	mStringIndex.size=1,	mStringIndex[0]="???";
 * @author handsomezhou
 * @date 2014-11-11
 */

public class PinyinUnit {
	//Whether Pinyin
	private boolean mPinyin;
	
	/*
	 * save the string which single Chinese characters Pinyin(include Multiple Pinyin),or continuous non-kanji characters.
	 * if mStringIndex.size not more than 1, it means the is not Polyphonic characters.
	 */
	private List<String> mStringIndex;

	public PinyinUnit() {
		//super();
		mStringIndex=new ArrayList<String>();
	}

	public boolean isPinyin() {
		return mPinyin;
	}

	public void setPinyin(boolean pinyin) {
		mPinyin = pinyin;
	}

	public List<String> getStringIndex() {
		return mStringIndex;
	}

	public void setStringIndex(List<String> stringIndex) {
		mStringIndex = stringIndex;
	}
}
