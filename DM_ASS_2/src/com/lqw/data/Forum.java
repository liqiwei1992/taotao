package com.lqw.data;

import java.util.ArrayList;
import java.util.HashMap;


public class Forum {

	public String name;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public int allWordCnt = 0;
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	public HashMap<String, Double> wordFreq = new HashMap<String, Double>();
	
	public Forum(String name) {
		this.name = name;
	}
	
	public Forum(String name, ArrayList<Post> posts) {
		this.name = name;
		this.posts = posts;
	}
	
	public void addPost(Post post) {
		posts.add(post);
	}
	
	public void addPosts(ArrayList<Post> posts) {
		this.posts.addAll(posts);
	}
	
	public void calFreq() {
		for (Post post : posts) {
			allWordCnt += post.allWordCnt;
			for (String word : post.wordCount.keySet()) {
				if (wordCount .containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + post.wordCount.get(word));
				} else {
					wordCount.put(word, post.wordCount.get(word));
				}
			}
		}
		
		for (String word : wordCount.keySet()) {
			wordFreq.put(word, wordCount.get(word) / (double)allWordCnt);
		}
	}
}
