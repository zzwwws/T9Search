package com.handsomezhou.t9search.util;

import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import com.handsomezhou.t9search.model.PinyinUnit;

public class PinyinUtil {
	// init Pinyin Output Format
	private static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

	/**
	 * Convert from Chinese string to a series of PinyinUnit
	 * 
	 * @param chineseString
	 * @param pinyinUnit
	 */
	public static void chineseStringToPinyinUnit(String chineseString,
			List<PinyinUnit> pinyinUnit) {
		if ((null == chineseString) || (null == pinyinUnit)) {
			return;
		}
		
		if(null==format){
			format = new HanyuPinyinOutputFormat();
		}
		
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		int chineseStringLength = chineseString.length();
		StringBuffer nonPinyinString = new StringBuffer();
		PinyinUnit pyUnit = null;
		String[] pinyinStr = null;
		boolean lastChineseCharacters = true;

		for (int i = 0; i < chineseStringLength; i++) {
			char ch = chineseString.charAt(i);
			try {
				pinyinStr = PinyinHelper.toHanyuPinyinStringArray(ch,format);
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				
				e.printStackTrace();
			}

			if (null == pinyinStr) {
				if (true == lastChineseCharacters) {
					pyUnit = new PinyinUnit();
					lastChineseCharacters = false;
					nonPinyinString.delete(0, nonPinyinString.length());
				}
				nonPinyinString.append(ch);
			} else {
				if (false == lastChineseCharacters) {
					// add continuous non-kanji characters to PinyinUnit
					String[] str = { nonPinyinString.toString() };
					addPinyinUnit(pinyinUnit, pyUnit, false, str);
					nonPinyinString.delete(0, nonPinyinString.length());
					lastChineseCharacters = true;
				}
				// add single Chinese characters Pinyin(include Multiple Pinyin)
				// to PinyinUnit
				pyUnit = new PinyinUnit();
				addPinyinUnit(pinyinUnit, pyUnit, true, pinyinStr);

			}
		}

		if (false == lastChineseCharacters) {
			// add continuous non-kanji characters to PinyinUnit
			String[] str = { nonPinyinString.toString() };
			addPinyinUnit(pinyinUnit, pyUnit, false, str);
			nonPinyinString.delete(0, nonPinyinString.length());
			lastChineseCharacters = true;
		}

	}

	private static void addPinyinUnit(List<PinyinUnit> pinyinUnit,
			PinyinUnit pyUnit, boolean pinyin, String[] string) {
		if ((null == pinyinUnit) || (null == pyUnit) || (null == string)) {
			return;
		}

		initPinyinUnit(pyUnit, pinyin, string);
		pinyinUnit.add(pyUnit);

		return;

	}

	private static void initPinyinUnit(PinyinUnit pinyinUnit, boolean pinyin,
			String[] string) {
		if ((null == pinyinUnit) || (null == string)) {
			return;
		}
		int i=0;
		int j=0; 
		int k=0;
		int strLength = string.length;
		pinyinUnit.setPinyin(pinyin);
		

		if(false==pinyin||strLength<=1){// no more than one pinyin
			for (i = 0; i < strLength; i++) {
				pinyinUnit.getStringIndex().add(new String(string[i]));
			}
		}else{ //more than one pinyin.//we must delete the same pinyin string,because pinyin without tone.
			pinyinUnit.getStringIndex().add(new String(string[0]));
			for( j=1; j<strLength; j++){
				int curStringIndexlength=pinyinUnit.getStringIndex().size();
				for( k=0; k<curStringIndexlength; k++){
					if(pinyinUnit.getStringIndex().get(k).equals(string[j])){
						break;
					}
				}
				
				if(k==curStringIndexlength){
					pinyinUnit.getStringIndex().add(new String(string[j]));
				}
			}
		}
	}
}
