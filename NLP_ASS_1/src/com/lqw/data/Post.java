package com.lqw.data;

import java.util.HashMap;

public class Post {

	public String forumName;
	
	//public HashMap<Bigram, Integer> bigramCount = new HashMap<Bigram, Integer>();
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	public HashMap<String, Integer> bigramCount = new HashMap<String, Integer>();
	
	public int allWordCnt = 0;
	public int allBigramCnt = 0;
	
	
	public Post(String forumName) {
		this.forumName = forumName;
	}
	
	public void addBigramCount(HashMap<String, Integer> bigramCount) {
		this.bigramCount = bigramCount;
		
		for (String key : bigramCount.keySet()) {
			allBigramCnt += bigramCount.get(key);
		}
	}
	
	public void addWordCount(HashMap<String, Integer> wordCount) {
		this.wordCount = wordCount;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
		}
	}
	
}
