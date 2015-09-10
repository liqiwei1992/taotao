package com.lqw.feature;

import java.util.HashMap;

public class TF {

	public static HashMap<String, Double> calTf(HashMap<String, Integer> wordCount, int allWordCnt) {
	//public static HashMap<String, Double> calTf(HashMap<String, Integer> wordCount, int maxWordCnt, int allWordCnt) {
		HashMap<String, Double> tf = new HashMap<String, Double>();
		
		for (String key : wordCount.keySet())
			tf.put(key, wordCount.get(key) / (double)allWordCnt);
			//tf.put(key, 0.5 + 0.5 * wordCount.get(key) / (double)maxWordCnt);
		
		return tf;
	}
}
