package com.lqw.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.Post;
import com.lqw.tool.MyMath;

public class BruteForce {

	public static final double threshold_tfidf = 0.0001;
	
	public ArrayList<Post> X_QPosts = new ArrayList<Post>();
	public ArrayList<Post> QPosts = new ArrayList<Post>();
	
	public double[][] similarity;
	
	public HashSet<String> featureWords = new HashSet<String>();
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public BruteForce(ArrayList<Post> rawX_QPosts, ArrayList<Post> rawQPosts) {
		
		HashMap<String, Double> rawX_QWordIdf = new HashMap<String, Double>();
		HashMap<String, Integer> rawX_QWordInPost = new HashMap<String, Integer>();
		
		for (Post post : rawX_QPosts) {
			for (String word : post.wordCount.keySet()) {
				if (rawX_QWordInPost.containsKey(word)) {
					rawX_QWordInPost.put(word, rawX_QWordInPost.get(word) + 1);
				} else {
					rawX_QWordInPost.put(word, 1);
				}
			}
		}
		
		for (String word : rawX_QWordInPost.keySet()) {
			rawX_QWordIdf.put(word, Math.log10(rawX_QPosts.size() / ((double)rawX_QWordInPost.get(word) + 1.0)));
		}
		for (Post post : rawX_QPosts) {
			post.calTfIdf(rawX_QWordIdf);
			for (String word : post.wordTfIdf.keySet()) {
				if (post.wordTfIdf.get(word) > threshold_tfidf) {
					featureWords.add(word);
				}
			}
		}
		
		//build the X - Q
		for (Post post : rawX_QPosts) {
			HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
			for (String word : post.wordCount.keySet()) {
				if (featureWords.contains(word)) {
					postWordCount.put(word, post.wordCount.get(word));
				}
			}
			X_QPosts.add(new Post(postWordCount, post.forumName));
		}
		for (String word : featureWords) {
			int wordInPostCnt = 0;
			for (Post post : X_QPosts) {
				if (post.wordCount.containsKey(word)) {
					wordInPostCnt++;
				}
			}
			wordIdf.put(word, Math.log10(X_QPosts.size() / ((double)wordInPostCnt + 1.0)));
		}
		for (Post post : X_QPosts) {
			post.calTfIdf(wordIdf);
		}
		
		time = new double[rawQPosts.size()];
		for (int i = 0; i < time.length; ++i)
			time[i] = 0.0;
		//build the Q
		for (int i = 0; i < rawQPosts.size(); ++i) {
			long begin = System.currentTimeMillis();
			
			Post post = rawQPosts.get(i);
			HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
			for (String word : post.wordCount.keySet()) {
				if (featureWords.contains(word)) {
					postWordCount.put(word, post.wordCount.get(word));
				}
			}
			QPosts.add(new Post(postWordCount, post.forumName));
			
			long end = System.currentTimeMillis();
			time[i] += (end - begin) / 1000.0;
		}
		for (int i = 0; i < QPosts.size(); ++i) {
			long begin = System.currentTimeMillis();
			
			Post post = QPosts.get(i);
			post.calTfIdf(wordIdf);
			
			long end = System.currentTimeMillis();
			time[i] += (end - begin) / 1000.0;
		}
		
		
		similarity = new double[QPosts.size()][X_QPosts.size()];
		//calSimilarity();
		
		
	}
	
	public double[] time;
	
	public void calSimilarity() {
		for (int i = 0; i < similarity.length; ++i) {
			long begin = System.currentTimeMillis();
			
			for (int j = 0; j < similarity[i].length; ++j) {
					similarity[i][j] = calSimilarityB(QPosts.get(i), X_QPosts.get(j));
			}
			
			long end = System.currentTimeMillis();
			time[i] += (end - begin) / 1000.0;
		}
	}
	
	public void KNearest(int K) {
		calSimilarity();
		
		double[] precision = new double[QPosts.size()];
		//int predRightCnt = 0;
		
		for (int i = 0; i < QPosts.size(); ++i) {
			long begin = System.currentTimeMillis();
			
			int rightCnt = 0;
			
			ArrayList<Post> kNearestPost = new ArrayList<Post>();
			
			boolean[] color = new boolean[X_QPosts.size()];
			for (int l = 0; l < color.length; ++l) color[l] = false;
			
			for (int k = 0; k < K; ++k) {
				double maxSimlarity = Double.NEGATIVE_INFINITY;
				int index = -1;
				
				for (int j = 0; j < similarity[i].length; ++j) {
					if (!color[j] && maxSimlarity < similarity[i][j]) {
						maxSimlarity = similarity[i][j];
						index = j;
					}
				}
				
				color[index] = true;
				kNearestPost.add(X_QPosts.get(index));
			}
			
			/*
			{
				HashMap<String, Integer> pred = new HashMap<String, Integer>();
				for (Post knpost : kNearestPost) {
					if (pred.containsKey(knpost.forumName)) {
						pred.put(knpost.forumName, pred.get(knpost.forumName) + 1);
					} else {
						pred.put(knpost.forumName, 1);
					}
				}
				String predName = "";
				int maxCnt = Integer.MIN_VALUE;
				for (String forumName : pred.keySet()) {
					if (pred.get(forumName) > maxCnt) {
						maxCnt = pred.get(forumName);
						predName = forumName;
					}
				}
				if (predName.equals(QPosts.get(i).forumName))
					predRightCnt++;
			}
			*/
			
			for (Post knpost : kNearestPost) {
				if (knpost.forumName.equals(QPosts.get(i).forumName))
					rightCnt++;
			}
			
			precision[i] = rightCnt / (double)K;
			
			long end = System.currentTimeMillis();
			time[i] += (end - begin) / 1000.0;
		}
		
		//System.out.println(rightCnt + " / " + " (" + K + " * " + QPosts.size() + ")) = ");
		
		//System.out.println(predRightCnt / (double)QPosts.size());
		
		System.out.println(K + " precision: avg = " + MyMath.average(precision) + ", var = " + MyMath.standardVariance(precision) + ", time: avg = " + 
				MyMath.average(time) + ", var = " + MyMath.standardVariance(time) + ", all = " + MyMath.sum(time));
		
	}
	
	public static double calSimilarityA(Post p, Post q) {
		int intersection = 0;
		for (String word : p.wordCount.keySet())
			if (q.wordCount.containsKey(word))
				intersection += Math.min(p.wordCount.get(word), q.wordCount.get(word));
		
		return intersection / (double)(p.allWordCnt + q.allWordCnt - intersection);
	}
	
	public static double calSimilarityB(Post p, Post q) {
		if (Math.abs(p.module) < 0.0000001 || Math.abs(q.module) < 0.0000001)
			return 0.0;
		
		double dotProduct = 0.0;
		for (String word : p.wordTfIdf.keySet())
			if (q.wordTfIdf.containsKey(word))
				dotProduct += p.wordTfIdf.get(word) * q.wordTfIdf.get(word);
		
		return dotProduct / (p.module * q.module);
	}
}
