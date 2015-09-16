package com.lqw.data;

import java.util.HashMap;

public class Post {

	public String path;
	public boolean positive;
	
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	
	public int allWordCnt = 0;
	
	public Post(String path, boolean positive) {
		this.path = path;
		this.positive = positive;
	}
	
	public void addWordCount(HashMap<String, Integer> wordCount) {
		this.wordCount = wordCount;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
		}
	}
}
