package com.lqw.rf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.Main;
import com.lqw.data.Data;
import com.lqw.data.Forum;
import com.lqw.data.Post;

public class RandomForest {

	public static final double threshold_tfidf = 0.06;
	public static final int DT_NUM = 30;
	public static final double sample_percentage = 0.66;
	
	public Random rand;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	public ArrayList<String> fw;
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public ArrayList<WeakerDT> rf = new ArrayList<WeakerDT>();
	
	
	public RandomForest(HashMap<String, Forum> rawForums) {
		HashMap<String, Double> rawWordIdf = new HashMap<String, Double>();
		HashMap<String, Integer> rawWordInPost = new HashMap<String, Integer>();
		int rawPostCnt = 0;
		
		for (String forumName : rawForums.keySet()) {
			for (Post post : rawForums.get(forumName).posts) {
				for (String word : post.wordCount.keySet()) {
					if (rawWordInPost.containsKey(word)) {
						rawWordInPost.put(word, rawWordInPost.get(word) + 1);
					} else {
						rawWordInPost.put(word, 1);
					}
				}
			}
			rawPostCnt += rawForums.get(forumName).posts.size();
		}
		for (String word : rawWordInPost.keySet()) {
			rawWordIdf.put(word, Math.log10(rawPostCnt / ((double)rawWordInPost.get(word) + 1.0)));
		}
		for (String forumName : rawForums.keySet()) {
			for (Post post : rawForums.get(forumName).posts) {
				post.calTfIdf(rawWordIdf);
				for (String word : post.wordTfIdf.keySet()) {
					if (post.wordTfIdf.get(word) > threshold_tfidf) {
						featureWords.add(word);
					}
				}
			}
		}
		//System.out.println(featureWords.size());
		fw = new ArrayList<String>(featureWords);
		
		for (String forumName : rawForums.keySet()) {
			for (Post post : rawForums.get(forumName).posts) {
				HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
				for (String word : post.wordCount.keySet()) {
					if (featureWords.contains(word)) {
						postWordCount.put(word, post.wordCount.get(word));
					}
				}
				posts.add(new Post(postWordCount, post.forumName));
			}
		}
		for (String word : featureWords) {
			int wordInPostCnt = 0;
			for (Post post : posts) {
				if (post.wordCount.containsKey(word)) {
					wordInPostCnt++;
				}
			}
			wordIdf.put(word, Math.log10(posts.size() / ((double)wordInPostCnt + 1.0)));
		}
		
		for (Post post : posts) {
			post.calTfIdf(wordIdf);
			//for (String word : post.wordTfIdf.keySet())
			//	System.out.println(post.wordTfIdf.get(word));
		}
		
		rand = Main.rand;
	}
	
	public void build() {
		ArrayList<Data> trainData = new ArrayList<Data>();
		for (int p = 0; p < posts.size(); ++p) {
			double[] feature = new double[fw.size()];
			for (int f = 0; f < fw.size(); ++f) {
				if (posts.get(p).wordTfIdf.containsKey(fw.get(f))) {
					feature[f] = posts.get(p).wordTfIdf.get(fw.get(f));
				} else {
					feature[f] = 0.0;
				}
			}
			trainData.add(new Data(posts.get(p).forumName, feature));
		}
		
		int size = trainData.size();
		for (int t = 0; t < DT_NUM; ++t) {
			ArrayList<Data> data = new ArrayList<Data>();
			
			for (int i = 0; i < size * sample_percentage; ++i) {
				data.add(trainData.get(rand.nextInt(size)));
			}
			
			WeakerDT dt = new WeakerDT(data);
			dt.build();
			
			rf.add(dt);
		}
	}
	
	public double predicate(ArrayList<Post> rawTestPosts) {
		ArrayList<Post> testPosts = new ArrayList<Post>();
		
		for (Post post : rawTestPosts) {
			HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
			for (String word : post.wordCount.keySet()) {
				if (featureWords.contains(word)) {
					postWordCount.put(word, post.wordCount.get(word));
				}
			}
			testPosts.add(new Post(postWordCount, post.forumName));
		}
		for (Post post : testPosts) {
			post.calTfIdf(wordIdf);
		}
		
		ArrayList<Data> testData = new ArrayList<Data>();
		ArrayList<String> fw = new ArrayList<String>(featureWords);
		for (int p = 0; p < testPosts.size(); ++p) {
			double[] feature = new double[fw.size()];
			for (int f = 0; f < fw.size(); ++f) {
				if (testPosts.get(p).wordTfIdf.containsKey(fw.get(f))) {
					feature[f] = testPosts.get(p).wordTfIdf.get(fw.get(f));
				} else {
					feature[f] = 0.0;
				}
			}
			testData.add(new Data(testPosts.get(p).forumName, feature));
		}
		
		int rightCnt = 0;
		for (int p = 0; p < testData.size(); ++p) {
			if (testData.get(p).type.equals(predicate(testData.get(p))))
				rightCnt++;
		}
		
		return rightCnt / (double)rawTestPosts.size();
	}
	
	public String predicate(Data da) {
		HashMap<String, Integer> predCnt = new HashMap<String, Integer>();
		for (int t = 0; t < rf.size(); ++t) {
			String type = rf.get(t).predicate(da);
			if (predCnt.containsKey(type))
				predCnt.put(type, predCnt.get(type) + 1);
			else
				predCnt.put(type, 1);
		}
		
		String maxType = null;
		int maxCnt = Integer.MIN_VALUE;
		for (String type : predCnt.keySet()) {
			if (predCnt.get(type) > maxCnt) {
				maxType = type;
				maxCnt = predCnt.get(type);
			}
		}
		
		return maxType;
	}
}
