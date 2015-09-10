package com.lqw.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.lqw.feature.Gauss;

public class Forum {

	public String name;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	//record the # of feature words in the forum
	public HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	//record the term-frequency of feature words in the forum, and it does not equal the TF of word in post
	public HashMap<String, Double> wordTf = new HashMap<String, Double>();
	
	//record the condition probability of a word in a forum, include all feature word
	public HashMap<String, Double> wordCondProb = new HashMap<String, Double>();
	
	public double allWordTfIdf = 0.0;
	
	//record the mu and sigma for gaus function
	public HashMap<String, Gauss> wordGaus = new HashMap<String, Gauss>();
	
	public int allWordCnt = 0;
	
	public double priorProb = 0.0;
	
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
	
	private boolean isCountWord = false;
	public void countWord() {
		if (isCountWord)
			return;
		
		isCountWord = true;
		
		for (Post post : posts) {
			allWordCnt += post.allWordCnt;
			for (Entry<String, Integer> wordEntry : post.wordCount.entrySet()) {
				String word = wordEntry.getKey();
				if (wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + wordEntry.getValue());
				} else {
					wordCount.put(word, wordEntry.getValue());
				}
			}
		}
		
		//System.out.println(allWordCnt);
	}
	
	//for selecting the feature words, and it does not equal the TF of word in post
	public void calTf() {
		countWord();
		
		for (Entry<String, Integer> wordEntry : wordCount.entrySet()) {
			wordTf.put(wordEntry.getKey(), wordEntry.getValue() / (double)allWordCnt);
		}
	}
	
	public void calFeature(HashMap<String, Double> wordIdf) {
		countWord();
		
		for (Post post : posts) {
			post.calFeature(wordIdf);
			for (Entry<String, Integer> wordEntry : post.wordCount.entrySet())
				allWordTfIdf += post.tf_idf.get(wordEntry.getKey()) * NBCD.DISC_COEFF;// * wordEntry.getValue();
		}
		
	}
}
