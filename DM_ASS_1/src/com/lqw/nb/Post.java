package com.lqw.nb;

import java.util.HashMap;

import com.lqw.feature.TF;
import com.lqw.feature.TF_IDF;

public class Post {

	public String name;
	
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	public HashMap<String, Double> tf = new HashMap<String, Double>();
	public HashMap<String, Double> tf_idf = new HashMap<String, Double>();
	
	
	public int allWordCnt = 0;
	//public int maxTF = 0;
	
	public Post(String name, HashMap<String, Integer> wordCount) {
		this.name = name;
		this.wordCount = wordCount;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
			//if (wordCount.get(key) > maxTF)
			//	maxTF = wordCount.get(key);
		}
	}
	
	public void calFeature(HashMap<String, Double> wordIdf) {
		//tf = TF.calTf(wordCount, maxTF, allWordCnt);
		tf = TF.calTf(wordCount, allWordCnt);
		tf_idf = TF_IDF.calTfIdf(tf, wordIdf);
	}
	
	public void calTf() {
		//tf = TF.calTf(wordCount, maxTF, allWordCnt);
		tf = TF.calTf(wordCount, allWordCnt);
	}
	
	public void calTfIdf(HashMap<String, Double> wordIdf) {
		tf_idf = TF_IDF.calTfIdf(tf, wordIdf);
	}
}
