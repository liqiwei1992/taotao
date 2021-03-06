package com.lqw.gbdt_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.Main;
import com.lqw.data.Data;
import com.lqw.data.Forum;
import com.lqw.data.Post;

public class GBDT {

	public static final double threshold_tfidf = 0.16;
	public static final int M = 6;
	public static final double sample_percentage = 0.66;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	public ArrayList<String> fw;
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public HashSet<String> forumNames = new HashSet<String>();
	public ArrayList<String> fn;
	public int K;
	
	public BaseDT[][] dts;
	
	public Random rand;
	
	public GBDT(HashMap<String, Forum> rawForums) {
		HashMap<String, Double> rawWordIdf = new HashMap<String, Double>();
		HashMap<String, Integer> rawWordInPost = new HashMap<String, Integer>();
		int rawPostCnt = 0;
		
		for (String forumName : rawForums.keySet()) {
			forumNames.add(forumName);
			
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
		fn = new ArrayList<String>(forumNames);
		
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
		
		
		K = fn.size();
		dts = new BaseDT[K][M];
		//build the tree iteratively
		double[][] Y = new double[trainData.size()][K];
		for (int i = 0; i < trainData.size(); ++i) {
			for (int k = 0; k < K; ++k) {
				if (trainData.get(i).type.equals(fn.get(k))) {
					Y[i][k] = 1.0;
				} else {
					Y[i][k] = 0.0;
				}
			}
		}
		double[][] F = new double[trainData.size()][K];
		for (int i = 0; i < trainData.size(); ++i) {
			for (int k = 0; k < K; ++k) {
				F[i][k] = 0.0;
			}
		}
		
		double[][] P = new double[trainData.size()][K];
		for (int m = 0; m < M; ++m) {
			
			//System.out.println("m: " + m);
			
			for (int i = 0; i < trainData.size(); ++i) {
				double sumExpF = 0.0;
				for (int k = 0; k < K; ++k) {
					sumExpF += Math.exp(F[i][k]);
				}
				
				//System.out.printf("%.2f ", sumExpF);
				
				
				for (int k = 0; k < K; ++k) {
					P[i][k] = Math.exp(F[i][k]) / sumExpF;
				}
			}
			
			//System.out.println();
			
			
			for (int k = 0; k < K; ++k) {
				
				//System.out.println("k: " + k);
				
				for (int i = 0; i < trainData.size(); ++i) {
					trainData.get(i).value = Y[i][k] - P[i][k];
					//System.out.printf("%.2f - %.2f = %.2f, ", Y[i][k], P[i][k], trainData.get(i).value);
				}
				//System.out.println();
				
				
				ArrayList<Data> data = new ArrayList<Data>();
				for (int i = 0; i < trainData.size() * sample_percentage; ++i) {
					data.add(trainData.get(rand.nextInt(trainData.size())));
				}
				BaseDT dt = new BaseDT(data);
				dt.build();
				
				for (int i = 0; i < trainData.size(); ++i) {
					F[i][k] = F[i][k] + dt.predicate(trainData.get(i)) * (K - 1) / (double)K;
				}
				
				dts[k][m] = dt;
			}
			
			//for (int k = 0; k < K; ++k) {
			//	for (int i = 0; i < trainData.size(); ++i) {
			//		System.out.printf("%.2f ", F[i][k]);
			//	}
			//	System.out.println();
			//}
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
		
		
		double maxGama = Double.NEGATIVE_INFINITY;
		int maxK = -1;
		for (int k = 0; k < K; ++k) {
			
			//System.out.println("\n==========");
			
			double gama = 0.0;
			for (int m = 0; m < M; ++m) {
				double p = dts[k][m].predicate(da);
				gama += p;
				//System.out.println(p);
			}
			
			if (gama > maxGama) {
				maxGama = gama;
				maxK = k;
			}
			//System.out.println("p: " + gama + ", k: " + maxK);
		}
		
		//System.out.println(fn.get(maxK));
		
		return fn.get(maxK);
	}
}
