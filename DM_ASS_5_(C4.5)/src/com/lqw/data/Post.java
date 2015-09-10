package com.lqw.data;

import java.util.HashMap;

import com.lqw.feature.TF;
import com.lqw.feature.TF_IDF;

public class Post {

	public String forumName;
	
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	public HashMap<String, Double> wordTf = new HashMap<String, Double>();
	public HashMap<String, Double> wordTfIdf = new HashMap<String, Double>();
	
	public int allWordCnt = 0;
	
	public Post(HashMap<String, Integer> wordCount, String forumName) {
		this.wordCount = wordCount;
		this.forumName = forumName;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
		}
	}
	
	public void calTfIdf(HashMap<String, Double> wordIdf) {
		wordTf = TF.calTf(wordCount, allWordCnt);
		wordTfIdf = TF_IDF.calTfIdf(wordTf, wordIdf);
	}
}
