package com.lqw.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.data.Post;
import com.lqw.tool.MyMath;

public class LSH {

	public static final double threshold_tfidf = 0.0001;
	
	public ArrayList<Post> X_QPosts = new ArrayList<Post>();
	public ArrayList<Post> QPosts = new ArrayList<Post>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	
	public int t = 1;
	public int b = 2;
	public Random rand = new Random();
	
	//the hash function
	public ArrayList<ArrayList<HashMap<String, Double>>> H = new ArrayList<ArrayList<HashMap<String, Double>>>();
	//group the X - Q
	public ArrayList<HashMap<String, ArrayList<Post>>> hashGroupPosts = new ArrayList<HashMap<String, ArrayList<Post>>>(t);
	//public ArrayList<HashMap<String, Double[][]>> similarity = new ArrayList<HashMap<String, Double[][]>>();
	
	//the similarity between Q and X - Q
	public ArrayList<ArrayList<Double[]>> similarity = new ArrayList<ArrayList<Double[]>>();
	//record the hash value of Q
	public ArrayList<ArrayList<String>> hashVofQ = new ArrayList<ArrayList<String>>();

	public double[] time;
	
	public LSH(ArrayList<Post> rawX_QPosts, ArrayList<Post> rawQPosts) {
		
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
		for (int p = 0; p < time.length; ++p)
			time[p] = 0.0;
		//build the Q
		for (int p = 0; p < rawQPosts.size(); ++p) {
			long begin = System.currentTimeMillis();
			
			Post post = rawQPosts.get(p);
			HashMap<String, Integer> postWordCount = new HashMap<String, Integer>();
			for (String word : post.wordCount.keySet()) {
				if (featureWords.contains(word)) {
					postWordCount.put(word, post.wordCount.get(word));
				}
			}
			QPosts.add(new Post(postWordCount, post.forumName));

			long end = System.currentTimeMillis();
			time[p] += (end - begin) / 1000.0;
		}
		for (int p = 0; p < QPosts.size(); ++p) {
			long begin = System.currentTimeMillis();
			
			Post post = QPosts.get(p);
			post.calTfIdf(wordIdf);

			long end = System.currentTimeMillis();
			time[p] += (end - begin) / 1000.0;
		}
		
		
		for (int i = 0; i < t; ++i) {
			H.add(new ArrayList<HashMap<String, Double>>());
			hashGroupPosts.add(new HashMap<String, ArrayList<Post>>());
			//similarity.add(new HashMap<String, Double[][]>());
			for (int j = 0; j < b; ++j) {
				HashMap<String, Double> h = new HashMap<String, Double>();
				for (String word : featureWords) {
					h.put(word, rand.nextDouble() * 2 - 1.0);
				}
				H.get(i).add(h);
			}
		}
		
		//calculate the hash values of posts in X - Q
		for (int i = 0; i < t; ++i) {
			for (int p = 0; p < X_QPosts.size(); ++p) {
				Post post = X_QPosts.get(p);
				
				String hash = calHash(H.get(i), post);
				if (hashGroupPosts.get(i).containsKey(hash)) {
					hashGroupPosts.get(i).get(hash).add(post);
				} else {
					hashGroupPosts.get(i).put(hash, new ArrayList<Post>());
					hashGroupPosts.get(i).get(hash).add(post);
				}
			}
		}
		
		/*
		for (int i = 0; i < t; ++i) {
			for (String hash : hashGroupPosts.get(i).keySet()) {
				similarity.get(i).put(hash, new Double[QPosts.size()][hashGroupPosts.get(i).get(hash).size()]);
			}
		}
		*/
		
		//calculate the hash values of posts in Q
		for (int i = 0; i < t; ++i) {
			hashVofQ.add(new ArrayList<String>());
			similarity.add(new ArrayList<Double[]>());
			
			for (int p = 0; p < QPosts.size(); ++p) {
				long begin = System.currentTimeMillis();
				
				Post post = QPosts.get(p);
				String hash = calHash(H.get(i), post);
				hashVofQ.get(i).add(hash);
				similarity.get(i).add(new Double[hashGroupPosts.get(i).get(hash).size()]);
				
				long end = System.currentTimeMillis();
				time[p] += (end - begin) / 1000.0;
			}
		}
	}
	
	public void calSimilarity() {
		for (int i = 0; i < t; ++i) {
			for (int p = 0; p < QPosts.size(); ++p) {
				long begin = System.currentTimeMillis();
				
				String hash = hashVofQ.get(i).get(p);
				Double[] sim = similarity.get(i).get(p);
				for (int jj = 0; jj < hashGroupPosts.get(i).get(hash).size(); ++jj) {
					sim[jj] = calSimilarityB(QPosts.get(p), hashGroupPosts.get(i).get(hash).get(jj));
				}
				
				long end = System.currentTimeMillis();
				time[p] += (end - begin) / 1000.0;
			}
		}
		
	}
	
	public void KNearest(int K) {
		
		calSimilarity();
		
		double[] precision = new double[QPosts.size()];
		
		for (int p = 0; p < QPosts.size(); ++p) {
			long begin = System.currentTimeMillis();
			
			int rightCnt = 0;
			
			HashSet<Post> kNearestPost = new HashSet<Post>();
			
			for (int k = 0; k < K; ++k) {
				double maxSimlarity = Double.NEGATIVE_INFINITY;
				Post knpost = null;
				
				for (int i = 0; i < t; ++i) {
					String hash = hashVofQ.get(i).get(p);
					Double[] sim = similarity.get(i).get(p);
					
					for (int jj = 0; jj < hashGroupPosts.get(i).get(hash).size(); ++jj) {
						Post tmp = hashGroupPosts.get(i).get(hash).get(jj);
						if (!kNearestPost.contains(tmp) && maxSimlarity < sim[jj]) {
							maxSimlarity = sim[jj];
							knpost = tmp;
						}
					}
				}
				
				if (knpost == null)
					System.err.println(">_<");
				
				kNearestPost.add(knpost);
			}
			
			for (Post knpost : kNearestPost) {
				if (knpost.forumName.equals(QPosts.get(p).forumName))
					rightCnt++;
			}
			
			precision[p] = rightCnt / (double)K;
			
			long end = System.currentTimeMillis();
			time[p] += (end - begin) / 1000.0;
		}
		
		//System.out.println(rightCnt + " / " + " (" + K + " * " + QPosts.size() + ") = ");
		System.out.println(K + " precision: avg = " + MyMath.average(precision) + ", var = " + MyMath.standardVariance(precision) + ", time: avg = " + 
				MyMath.average(time) + ", var = " + MyMath.standardVariance(time) + ", all = " + MyMath.sum(time));
	}
	
	public String calHash(ArrayList<HashMap<String, Double>> hashH, Post post) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < hashH.size(); ++i) {
			HashMap<String, Double> h = hashH.get(i);
			double product = 0.0;
			for (String word : post.wordTfIdf.keySet()) {
				product += post.wordTfIdf.get(word) * h.get(word);
			}
			if (product > 0.0)
				sb.append("1");
			else
				sb.append("0");
		}
		
		return sb.toString();
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
