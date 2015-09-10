package com.lqw.data;

import java.util.HashMap;

import com.lqw.feature.TF;
import com.lqw.feature.TF_IDF;

public class Post {

	public static HashMap<String, Integer> wordInPostCount = new HashMap<String, Integer>();
	
	public String name;
	
	
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	public HashMap<String, Double> wordTf = new HashMap<String, Double>();
	public HashMap<String, Double> wordTfIdf = new HashMap<String, Double>();
	
	public int allWordCnt = 0;
	//public int maxWordCnt = 0;
	
	public Post(String name, HashMap<String, Integer> wordCount) {
		this.name = name;
		this.wordCount = wordCount;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
		}
	}
	
	public void calTfIdf(HashMap<String, Double> wordIdf) {
		wordTf = TF.calTf(wordCount, allWordCnt);
		wordTfIdf = TF_IDF.calTfIdf(wordTf, wordIdf);
	}
}
