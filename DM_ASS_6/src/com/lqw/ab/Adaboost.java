package com.lqw.ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.Main;
import com.lqw.data.Data;
import com.lqw.data.Forum;
import com.lqw.data.Post;

public class Adaboost {

	public static final int K = 10;
	public static final double threshold_tfidf = 0.06;
	public static final int IT_NUM = 20;
	public static final double sample_percentage = 0.66;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	public ArrayList<String> fw;
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	public ArrayList<BaseDT> dts = new ArrayList<BaseDT>();
	public ArrayList<Double> alphas = new ArrayList<Double>();
	
	public Random rand;
	
	
	public Adaboost(HashMap<String, Forum> rawForums) {
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
		
		//initialize the weight of each data
		double[] weight = new double[trainData.size()];
		for (int i = 0; i < trainData.size(); ++i)
			weight[i] = 1.0 / trainData.size();
		
		for (int it = 0; it < IT_NUM; ++it) {
			//sample the train data
			ArrayList<Data> data = new ArrayList<Data>();
			double sampleNum = sample_percentage * trainData.size() / 3;
			for (int s = 0; s < 3; ++s) {
				for (int i = 0; i < trainData.size(); ++i) {
					double prob = rand.nextDouble();
					if (prob < weight[i] * sampleNum) {
						data.add(trainData.get(i));
					}
				}
			}
			//System.out.println(trainData.size() + ", " + data.size());
			
			//build the base classifier, i.e. dt
			BaseDT dt = new BaseDT(data);
			dt.build();
			
			boolean[] right = new boolean[trainData.size()];
			for (int i = 0; i < trainData.size(); ++i) {
				String predType = dt.predicate(trainData.get(i));
				//System.out.println(trainData.get(i).type + " vs " + predType);
				if (trainData.get(i).type.equals(predType))
					right[i] = true;
				else
					right[i] = false;
			}
			
			//calculate the alpha
			double epsilon = 0.0;
			for (int i = 0; i < trainData.size(); ++i) {
				if (right[i] == false) {
					epsilon += weight[i];
				}
			}
			double alpha = Math.log((1.0 - epsilon) / epsilon) / 2.0;
			//System.out.println(epsilon + ", " + alpha);
			
			//update the weight
			double z = 0.0;
			for (int i = 0; i < weight.length; ++i) {
				if (right[i] == true)
					z += weight[i] * Math.exp(0.0 - alpha);
				else
					z += weight[i] * Math.exp(0.0 + alpha);
			}
			for (int i = 0; i < weight.length; ++i) {
				if (right[i] == true)
					weight[i] = weight[i] * Math.exp(0.0 - alpha) / z;
				else
					weight[i] = weight[i] * Math.exp(0.0 + alpha) / z;
			}
			
			//add the dt into dts
			dts.add(dt);
			alphas.add(alpha);
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
			String predType = predicate(testData.get(p));
			if (testData.get(p).type.equals(predType))
				rightCnt++;
		}
		
		//System.out.println(rightCnt);
		
		return rightCnt / (double)rawTestPosts.size();
	}
	
	public String predicate(Data da) {
		HashMap<String, Double> predH = new HashMap<String, Double>();
		for (int it = 0; it < IT_NUM; ++it) {
			BaseDT dt = dts.get(it);
			double alpha = alphas.get(it);
			
			String predType = dt.predicate(da);
			
			//System.out.println(da.type + " vs " + predType);
			
			if (predH.containsKey(predType))
				predH.put(predType, predH.get(predType) + alpha);
			else
				predH.put(predType, alpha);
		}
		
		//for (String predType : predH.keySet()) {
		//	System.out.println(predType + ": " + predH.get(predType));
		//}
		
		String maxPredType = null;
		double maxH = Double.NEGATIVE_INFINITY;
		for (String predType : predH.keySet()) {
			if (predH.get(predType) > maxH) {
				maxPredType = predType;
				maxH = predH.get(predType);
			}
		}
		
		return maxPredType;
	}
}
