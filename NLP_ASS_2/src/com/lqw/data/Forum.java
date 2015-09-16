package com.lqw.data;

import java.util.ArrayList;
import java.util.HashMap;

public class Forum {

	public String name;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public int allWordCnt = 0;
	
	//record the # of each word in a forum, include all feature word
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	//record the condition probability of each word in a forum, include all feature word
	public HashMap<String, Double> wordCondProb = new HashMap<String, Double>();
	
	public double priorProb = 0.0;
	
	public Forum(String name) {
		this.name = name;
	}
	
	public Forum(String name, ArrayList<Post> posts) {
		this.name = name;
		this.posts = posts;
		
		for (Post post : posts) {
			for (String word : post.wordCount.keySet()) {
				if (wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + post.wordCount.get(word));
				} else {
					wordCount.put(word, post.wordCount.get(word));
				}
				allWordCnt += post.wordCount.get(word);
			}
		}
	}
	
	public void addPost(Post post) {
		posts.add(post);
		
		for (String word : post.wordCount.keySet()) {
			if (wordCount.containsKey(word)) {
				wordCount.put(word, wordCount.get(word) + post.wordCount.get(word));
			} else {
				wordCount.put(word, post.wordCount.get(word));
			}
			allWordCnt += post.wordCount.get(word);
		}
	}
	
	public void addPosts(ArrayList<Post> posts) {
		this.posts.addAll(posts);
		
		for (Post post : posts) {
			for (String word : post.wordCount.keySet()) {
				if (wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + post.wordCount.get(word));
				} else {
					wordCount.put(word, post.wordCount.get(word));
				}
				allWordCnt += post.wordCount.get(word);
			}
		}
	}
	
	
}
