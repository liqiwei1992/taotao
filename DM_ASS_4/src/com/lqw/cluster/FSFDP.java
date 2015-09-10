package com.lqw.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.Post;

public class FSFDP {

	public static final int K = 10;
	public static final double threshold_tfidf = 0.07;
	//0.842, 0.860 with stop words
	public static final double dc = 0.85;
	public static final double threshold_rhor = 0.85;
	
	public int[] rho;
	public double[] delta;
	//public Feature[] feature;
	public double[][] distance;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	public HashSet<String> featureWords = new HashSet<String>();
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	//the cluster center
	public int[] clusters;
	//the labels of posts
	public ArrayList<ArrayList<Integer>> clusterLabels = new ArrayList<ArrayList<Integer>>();
	
	//the true labels of posts
	public HashMap<String, HashSet<Integer>> trueLabels = new HashMap<String, HashSet<Integer>>();
	
	public FSFDP(ArrayList<Post> rawPosts) {
		
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
		
		rho = new int[posts.size()];
		delta = new double[posts.size()];
		distance = new double[posts.size()][posts.size()];
		
		//calculate the distance between post and post
		for (int p = 0; p < distance.length; ++p)
			for (int q = p + 1; q < distance[p].length; ++q) {
				distance[p][q] = calDistance(posts.get(p), posts.get(q));
				distance[q][p] = distance[p][q];
			}
	}
	
	
	public void classify() {
		//rho = new int[posts.size()];
		//delta = new double[posts.size()];
		//clusterLabels = new ArrayList<ArrayList<Integer>>();
		
		for (int p = 0; p < distance.length; ++p) {
			int chi = 0;
			for (int q = 0; q < distance[p].length; ++q) {
				if (q != p) {
					if (distance[p][q] - dc < 0.0)
						chi++;
				}
			}
			rho[p] = chi;
		}
		
		ArrayList<Integer> alrho = new ArrayList<Integer>();
		for (int p = 0; p < rho.length; ++p) {
			alrho.add(rho[p]);
		}
		Collections.sort(alrho);
		int threshold_rho = alrho.get((int)(alrho.size() * threshold_rhor));
		//System.out.println(threshold_rho);
		
		for (int p = 0; p < rho.length; ++p) {
			int index = -1;
			double minDeltaDistance = Double.MAX_VALUE;
			double maxDistance = Double.NEGATIVE_INFINITY;
			for (int q = 0; q < distance[p].length; ++q) {
				if (q != p) {
					if (rho[q] > rho[p] && distance[p][q] < minDeltaDistance) {
						minDeltaDistance = distance[p][q];
						index = q;
					}
					if (distance[p][q] > maxDistance) {
						maxDistance = distance[p][q];
					}
				}
			}
			if (index == -1) {
				minDeltaDistance = maxDistance;
			}
			delta[p] = minDeltaDistance;
		}
		
		/*
		int cn = 0;
		for (int p = 0; p < rho.length; ++p) {
			cn += rho[p];
			System.out.println(rho[p] + ", " + delta[p]);
		}
		System.out.println(cn / (double)(posts.size() * (posts.size() - 1)));
		*/
		
		//select the cluster center
		boolean[] color = new boolean[rho.length];
		for (int c = 0; c < color.length; ++c) color[c] = false;
		clusters = new int[K];
		for (int k = 0; k < clusters.length; ++k) {
			int index = -1;
			double maxDistance = Double.NEGATIVE_INFINITY;
			for (int p = 0; p < rho.length; ++p) {
				if (color[p] == true)
					continue;
				boolean flag = false;
				for (int kk = 0; kk < k; ++kk) {
					if (rho[p] == rho[clusters[kk]] && delta[p] == delta[clusters[kk]] && distance[p][clusters[kk]] < 0.0001)
						flag = true;
				}
				if (flag == true)
					continue;
				
				if (delta[p] > maxDistance && rho[p] >= threshold_rho) {
					maxDistance = delta[p];
					index = p;
				}
			}
			clusters[k] = index;
			color[index] = true;
		}
		
		//assign a cluster center for each post
		for (int k = 0; k < clusters.length; ++k) {
			clusterLabels.add(new ArrayList<Integer>());
		}
		for (int p = 0; p < rho.length; ++p) {
			if (color[p] == true) {
				for (int k = 0; k < clusters.length; ++k) {
					if (p == clusters[k])
						clusterLabels.get(k).add(p);
				}
			} else {
				int index = -1;
				double minDistance = Double.MAX_VALUE;
				for (int k = 0; k < clusters.length; ++k) {
					if (rho[clusters[k]] >= rho[p] && distance[p][clusters[k]] < minDistance) {
						index = k;
						minDistance = distance[p][clusters[k]];
					}
				}
				clusterLabels.get(index).add(p);
			}
		}
		
		//int cnt = 0;
		//for (int k = 0; k < clusters.length; ++k) {
		//	cnt += clusterLabels.get(k).size();
		//	System.out.println(clusterLabels.get(k).size());
		//}
		//System.out.println(cnt + "\n");
	}
	
	
	public double evaluate() {
		
		int allPostCnt = posts.size();
		double hc = 0.0;
		for (String forumName : trueLabels.keySet()) {
			double in = trueLabels.get(forumName).size() / (double)allPostCnt;
			hc += (0.0 - in * Math.log10(in));
		}
		double hw = 0.0;
		for (int k = 0; k < clusterLabels.size(); ++k) {
			double in = clusterLabels.get(k).size() / (double)allPostCnt;
			hw += (0.0 - in * Math.log10(in));
		}
		
		//what if cnt == 0?
		double i = 0.0;
		for (int k = 0; k < clusterLabels.size(); ++k) {
			for (String forumName : trueLabels.keySet()) {
				int cnt = 0;
				for (int p = 0; p < clusterLabels.get(k).size(); ++p) {
					if (trueLabels.get(forumName).contains(clusterLabels.get(k).get(p))) {
						cnt++;
					}
				}
				if (cnt != 0)
					i += (cnt / (double)allPostCnt) * Math.log10(allPostCnt * cnt / ((double)(clusterLabels.get(k).size() * trueLabels.get(forumName).size())));
			}
		}
		
		//System.out.println(i + ", " + hc + ", " + hw);
		
		return i / ((hc + hw) / 2.0);
	}
	
	public static double calDistance(Post p, Post q) {
		if (Math.abs(p.module) < 0.0000001 || Math.abs(q.module) < 0.0000001)
			//return Math.acos(0.0);
			return 1.0;
		
		double dotProduct = 0.0;
		for (String word : p.wordTfIdf.keySet())
			if (q.wordTfIdf.containsKey(word))
				dotProduct += p.wordTfIdf.get(word) * q.wordTfIdf.get(word);
		
		double cos = dotProduct / (p.module * q.module);
		if (cos > 1.0) cos = 1.0;
		//return Math.acos(cos);
		return 1.0 - cos;
	}
	
}
