package com.lqw.gbdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.Data;
import com.lqw.data.Forum;
import com.lqw.data.Post;

public class ClassificationGBDT {

	public static final double threshold_tfidf = 0.16;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	public ArrayList<String> fw;
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public HashSet<String> forums = new HashSet<String>();
	public HashMap<String, GBDT> gbdts = new HashMap<String, GBDT>();
	
	
	public ClassificationGBDT(HashMap<String, Forum> rawForums) {
		HashMap<String, Double> rawWordIdf = new HashMap<String, Double>();
		HashMap<String, Integer> rawWordInPost = new HashMap<String, Integer>();
		int rawPostCnt = 0;
		
		for (String forumName : rawForums.keySet()) {
			forums.add(forumName);
			
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
		
		for (String forumName : forums) {
			
			System.out.println(forumName);
			
			for (int i = 0; i < trainData.size(); ++i) {
				if (trainData.get(i).type.equals(forumName))
					trainData.get(i).value = 1.0;
				else
					trainData.get(i).value = -1.0;
			}
			
			GBDT gbdt = new GBDT(trainData);
			gbdt.build();
			
			gbdts.put(forumName, gbdt);
		}
	}
	
	public double predicat(ArrayList<Post> rawTestPosts) {
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
		
		//System.out.println("\n" + da.type);
		
		String maxPredType = null;
		double maxPredValue = Double.NEGATIVE_INFINITY;
		for (String forumName : gbdts.keySet()) {
			double predValue = gbdts.get(forumName).predicate(da);
			if (predValue > maxPredValue) {
				maxPredValue = predValue;
				maxPredType = forumName;
			}
			
			//System.out.println(forumName + ": " + predValue);
		}
		return maxPredType;
	}
}
