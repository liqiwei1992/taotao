package com.lqw.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.data.Post;

public class KMeans {

	public static final double threshold_tfidf = 0.1;
	
	public Random rand;
	
	public HashMap<String, HashSet<Integer>> trueLabels = new HashMap<String, HashSet<Integer>>();
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	public HashSet<String> featureWords = new HashSet<String>();
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public int K;
	public int N;
	public ArrayList<HashMap<String, Double>> clusters = new ArrayList<HashMap<String, Double>>();
	public ArrayList<ArrayList<Integer>> clusterLabels = new ArrayList<ArrayList<Integer>>();
	
	public KMeans(ArrayList<Post> rawPosts, int K, int N) {
		this.K = K;
		this.N = N;
		
		HashMap<String, Double> rawWordIdf = new HashMap<String, Double>();
		HashMap<String, Integer> rawWordInPost = new HashMap<String, Integer>();
		
		for (Post post : rawPosts) {
			for (String word : post.wordCount.keySet()) {
				if (rawWordInPost.containsKey(word)) {
					rawWordInPost.put(word, rawWordInPost.get(word) + 1);
				} else {
					rawWordInPost.put(word, 1);
				}
			}
		}
		
		for (String word : rawWordInPost.keySet()) {
			rawWordIdf.put(word, Math.log10(rawPosts.size() / ((double)rawWordInPost.get(word) + 1.0)));
		}
		for (Post post : rawPosts) {
			post.calTfIdf(rawWordIdf);
			for (String word : post.wordTfIdf.keySet()) {
				if (post.wordTfIdf.get(word) > threshold_tfidf) {
					featureWords.add(word);
				}
			}
		}
		
		//System.out.println(featureWords.size());
		
		for (Post post : rawPosts) {
			HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
			for (String word : post.wordCount.keySet()) {
				if (featureWords.contains(word)) {
					postWordCount.put(word, post.wordCount.get(word));
				}
			}
			posts.add(new Post(postWordCount, post.forumName));
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
		
		
		for (int p = 0; p < posts.size(); ++p) {
			Post post = posts.get(p);
			if (trueLabels.containsKey(post.forumName)) {
				trueLabels.get(post.forumName).add(p);
			} else {
				trueLabels.put(post.forumName, new HashSet<Integer>());
				trueLabels.get(post.forumName).add(p);
			}
		}
		
		//1414220605757
		
		//long time = System.currentTimeMillis();
		//System.out.println(time);
		long time = 1414220605757L;
		rand = new Random(time);
		for (int k = 0; k < K; ++k) {
			clusters.add(new HashMap<String, Double>());
			for (String word : featureWords) {
				//clusters.get(k).put(word, rand.nextDouble() / 10.0);
				clusters.get(k).put(word, 0.0);
			}
			int p = rand.nextInt(posts.size());
			for (String word : posts.get(p).wordTfIdf.keySet()) {
				clusters.get(k).put(word, posts.get(p).wordTfIdf.get(word));
			}
		}
	}
	
	public void classify() {
		ArrayList<Double> clusterModules;
		int[] postLabels = new int[posts.size()];
		boolean finished = false;
		
		for (int n = 0; n < N && !finished; ++n) {
		//for (int n = 0; n < N; ++n) {
			finished = true;
			
			//calculate the module of clusters
			clusterModules = new ArrayList<Double>();
			for (int k = 0; k < K; ++k) {
				double sum = 0.0;
				for (String word : clusters.get(k).keySet()) {
					sum += clusters.get(k).get(word) * clusters.get(k).get(word); 
				}
				clusterModules.add(Math.sqrt(sum));
			}
			
			//update the label for each post
			clusterLabels = new ArrayList<ArrayList<Integer>>();
			for (int k = 0; k < K; ++k) {
				clusterLabels.add(new ArrayList<Integer>());
			}
			for (int p = 0; p < posts.size(); ++p) {
				int label = -1;
				double maxDistance = Double.NEGATIVE_INFINITY;
				for (int k = 0; k < K; ++k) {
					double distance = calDistance(clusters.get(k), clusterModules.get(k), posts.get(p));
					
					if (distance > maxDistance) {
						maxDistance = distance;
						label = k;
					}
				}
				if (label != postLabels[p])
					finished = false;
				postLabels[p] = label;
				clusterLabels.get(label).add(p);
			}
			
			//update the clusters
			for (int k = 0; k < K; ++k) {
				for (String word : clusters.get(k).keySet()) {
					double value = 0.0;
					for (int i = 0; i < clusterLabels.get(k).size(); ++i) {
						if (posts.get(clusterLabels.get(k).get(i)).wordTfIdf.containsKey(word)) {
							value += posts.get(clusterLabels.get(k).get(i)).wordTfIdf.get(word);
						}
					}
					clusters.get(k).put(word, value / (double)clusterLabels.get(k).size());
				}
			}
		}
		

		//for (int k = 0; k < K; ++k) {
		//	System.out.println(clusterLabels.get(k).size());
		//}
		//System.out.println();
		
		//for (int k = 0; k < K; ++k) {
		//	System.out.println(clusterLabels.get(k).size());
		//}
	}
	
	public double evaluate() {
		
		int allPostCnt = posts.size();
		double hc = 0.0;
		for (String forumName : trueLabels.keySet()) {
			double in = trueLabels.get(forumName).size() / (double)allPostCnt;
			hc += (0.0 - in * Math.log(in));
			
			//hc += (0.0 - trueLabels.get(forumName).size() * (Math.log10(trueLabels.get(forumName).size()) - Math.log10(allPostCnt)) / (double)allPostCnt);
		}
		double hw = 0.0;
		for (int k = 0; k < K; ++k) {
			double in = clusterLabels.get(k).size() / (double)allPostCnt;
			hw += (0.0 - in * Math.log(in));
			
			//hw += (0.0 - clusterLabels.get(k).size() * (Math.log10(clusterLabels.get(k).size()) - Math.log10(allPostCnt)) / (double)allPostCnt);
		}
		
		//what if cnt == 0?
		double i = 0.0;
		for (int k = 0; k < K; ++k) {
			for (String forumName : trueLabels.keySet()) {
				int cnt = 0;
				for (int p = 0; p < clusterLabels.get(k).size(); ++p) {
					if (trueLabels.get(forumName).contains(clusterLabels.get(k).get(p))) {
						cnt++;
						//System.out.println(">_<");
					}
				}
				if (cnt != 0)
					//i += (cnt / (double)allPostCnt) * (Math.log10(allPostCnt * cnt) - 
					//		Math.log10(((double)(clusterLabels.get(k).size() * trueLabels.get(forumName).size()))));
					i += (cnt / (double)allPostCnt) * Math.log(allPostCnt * cnt / ((double)(clusterLabels.get(k).size() * trueLabels.get(forumName).size())));
				//System.out.println(i);
			}
		}
		
		//System.out.println(i + ", " + hc + ", " + hw);
		
		return 2.0 * i / (hc + hw);
	}
	
	public static double calDistance(HashMap<String, Double> cluster, double clusterModule, Post p) {
		if (Math.abs(clusterModule) == 0.0 || Math.abs(p.module) == 0.0)
			return 0.0;
		
		double dotProduct = 0.0;
		for (String word : p.wordTfIdf.keySet())
			dotProduct += p.wordTfIdf.get(word) * cluster.get(word);
		
		return dotProduct / (clusterModule * p.module);
	}
}
