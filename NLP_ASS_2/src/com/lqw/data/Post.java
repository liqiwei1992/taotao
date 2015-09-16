package com.lqw.data;

import java.util.HashMap;

public class Post {

	public String forumName;
	
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	
	public int allWordCnt = 0;
	
	
	public Post(String forumName) {
		this.forumName = forumName;
	}
	
	public void addWordCount(HashMap<String, Integer> wordCount) {
		this.wordCount = wordCount;
		
		for (String key : wordCount.keySet()) {
			allWordCnt += wordCount.get(key);
		}
	}
	

	@Override
	public String toString() {
		return wordCount.toString(); 
	}
}
