package com.lqw.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.Post;

public class NBD {

	public int allWordCnt = 0;
	
	public HashSet<String> featureWord = new HashSet<String>();
	public HashMap<String, Integer> wordCnt = new HashMap<String, Integer>();
	
	public ArrayList<Post> trainPosts;
	
	//public int allPositiveCnt = 0;
	//public int allNegativeCnt = 0;
	public int allPositiveWordCnt = 0;
	public int allNegativeWordCnt = 0;
	public double positivePriorProb = 0;
	public double negativePriorProb = 0;
	public HashMap<String, Double> positiveWordCondProb = new HashMap<String, Double>();
	public HashMap<String, Double> negativeWordCondProb = new HashMap<String, Double>();
	
	
	public NBD(ArrayList<Post> posts) {
		for (Post post : posts)
			for (String word : post.wordCount.keySet())
				featureWord.add(word);
		
		this.trainPosts = posts;
	}
	
	public void train() {
		HashMap<String, Integer> positiveWordCnt = new HashMap<String, Integer>();
		HashMap<String, Integer> negativeWordCnt = new HashMap<String, Integer>();
		
		for (Post post : trainPosts) {
			if (post.positive) {
				allPositiveWordCnt += post.allWordCnt;
				//allPositiveCnt++;
			} else {
				allNegativeWordCnt += post.allWordCnt;
				//allNegativeCnt++;
			}
			for (String word : post.wordCount.keySet()) {
				if (post.positive) {
					if (positiveWordCnt.containsKey(word))
						positiveWordCnt.put(word, positiveWordCnt.get(word) + 1);
					else
						positiveWordCnt.put(word, 1);
				} else {
					if (negativeWordCnt.containsKey(word))
						negativeWordCnt.put(word, negativeWordCnt.get(word) + 1);
					else
						negativeWordCnt.put(word, 1);
				}
			}
		}
		
		for (String word : featureWord) {
			if (!positiveWordCnt.containsKey(word))
				positiveWordCnt.put(word, 0);
			if (!negativeWordCnt.containsKey(word))
				negativeWordCnt.put(word, 0);
		}
		
		positivePriorProb = allPositiveWordCnt / (double)(allPositiveWordCnt + allNegativeWordCnt);
		negativePriorProb = allNegativeWordCnt / (double)(allPositiveWordCnt + allNegativeWordCnt);
		//positivePriorProb = allPositiveCnt / (double)(allPositiveCnt + allNegativeCnt);
		//negativePriorProb = allNegativeCnt / (double)(allPositiveCnt + allNegativeCnt);
		
		for (String word : positiveWordCnt.keySet())
			positiveWordCondProb.put(word, (positiveWordCnt.get(word) + 1.0) / (double)(allPositiveWordCnt + featureWord.size()));
		for (String word : negativeWordCnt.keySet())
			negativeWordCondProb.put(word, (negativeWordCnt.get(word) + 1.0) / (double)(allNegativeWordCnt + featureWord.size()));
	}
	
	public boolean predicate(Post post) {
		
		double positiveVal = Math.log(positivePriorProb);
		double negativeVal = Math.log(negativePriorProb);
		
		if (post.wordCount.size() == 0) return true;
		
		for (String word : post.wordCount.keySet()) {
			if (featureWord.contains(word)) {
				positiveVal += Math.log(positiveWordCondProb.get(word)) * post.wordCount.get(word);
				negativeVal += Math.log(negativeWordCondProb.get(word)) * post.wordCount.get(word);
			}
		}
		
		return positiveVal > negativeVal;
	}
	
	public double predicate(ArrayList<Post> posts) {
		int rightCnt = 0;
		
		for (int i = 0; i < posts.size(); ++i) {
			Post post = posts.get(i);
			boolean predictPositive = predicate(post);
			
			//System.out.println(post.forumName + " vs " + predName);
			
			if (post.positive == predictPositive)
				rightCnt++;
		}
		
		return rightCnt / (double)posts.size();
	}
}
