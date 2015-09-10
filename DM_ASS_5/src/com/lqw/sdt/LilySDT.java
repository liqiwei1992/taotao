package com.lqw.sdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.CData;
import com.lqw.data.Forum;
import com.lqw.data.Post;

public class LilySDT {

	public static final double threshold_tfidf = 0.16;
	public static final int K = 9;
	
	public ArrayList<Post> posts = new ArrayList<Post>(K);
	
	public HashSet<String> featureWords = new HashSet<String>();
	public ArrayList<String> fw;
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public CSDT csdt;
	
	public LilySDT(HashMap<String, Forum> rawForums) {
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
	}
	
	public void sdt() {
		ArrayList<ArrayList<CData>> trainCData = new ArrayList<ArrayList<CData>>();
		for (int k = 0; k < K; ++k) {
			trainCData.add(new ArrayList<CData>());
		}
		
		for (int p = 0; p < posts.size(); ++p) {
			String[] disc = new String[0];
			double[] cont = new double[fw.size()];
			for (int f = 0; f < fw.size(); ++f) {
				if (posts.get(p).wordTfIdf.containsKey(fw.get(f))) {
					cont[f] = posts.get(p).wordTfIdf.get(fw.get(f));
				} else {
					cont[f] = 0.0;
				}
			}
			
			trainCData.get(p % K).add(new CData(posts.get(p).forumName, disc, cont));
		}
		
		csdt = new CSDT(trainCData);
		csdt.sdt();
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
		
		ArrayList<CData> testCData = new ArrayList<CData>();
		ArrayList<String> fw = new ArrayList<String>(featureWords);
		for (int p = 0; p < testPosts.size(); ++p) {
			String[] disc = new String[0];
			double[] cont = new double[fw.size()];
			for (int f = 0; f < fw.size(); ++f) {
				if (testPosts.get(p).wordTfIdf.containsKey(fw.get(f))) {
					cont[f] = testPosts.get(p).wordTfIdf.get(fw.get(f));
				} else {
					cont[f] = 0.0;
				}
			}
			testCData.add(new CData(testPosts.get(p).forumName, disc, cont));
		}
		
		return csdt.predicate(testCData);
	}
}
