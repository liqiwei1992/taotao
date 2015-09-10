package com.lqw.sgdlr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.lqw.data.Forum;
import com.lqw.data.Post;

public class SGDLR {

	public static final double threshold_freq = 0.0002;
	public static final int ALLITCnt = 2;
	public static final int ITCnt = 10;
	public static final double alpha = 1;
	public static final String BIAS = "weight_bias";
	
	public ArrayList<Post> trainPosts = new ArrayList<Post>();
	public ArrayList<String> trainLabels = new ArrayList<String>();
	
	public HashSet<String> featureWords = new HashSet<String>();
	//record the idf of each feature word
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	//record the weight of each feature word for each forum
	public HashMap<String, HashMap<String, Double>> allWordWeights = new HashMap<String, HashMap<String, Double>>();
	
	public SGDLR(HashMap<String, Forum> trainForums) {
		for (String forumName : trainForums.keySet()) {
			Forum forum = trainForums.get(forumName);
			
			forum.calFreq();
			for (String word : forum.wordFreq.keySet())
				if (forum.wordFreq.get(word) > threshold_freq)
					featureWords.add(word);
		}
		//System.out.println(featureWords.size());
		
		for (String forumName : trainForums.keySet()) {
			Forum forum = trainForums.get(forumName);
			
			allWordWeights.put(forum.name, new HashMap<String, Double>());
			
			for (Post post : forum.posts) {
				HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
				for (String word : post.wordCount.keySet()) {
					if (featureWords.contains(word)) {
						wordCount.put(word, post.wordCount.get(word));
					}
				}
				trainPosts.add(new Post(post.name, wordCount));
				trainLabels.add(forum.name);
			}
		}
		
		for (String word : featureWords) {
			int wordInPostCnt = 0;
			for (Post post : trainPosts) {
				if (post.wordCount.containsKey(word)) {
					wordInPostCnt++;
				}
			}
			wordIdf.put(word, Math.log10(trainPosts.size() / ((double)wordInPostCnt + 1.0)));
		}
		
		for (Post post : trainPosts) {
			post.calTfIdf(wordIdf);
		}
		
		for (String forum : allWordWeights.keySet()) {
			allWordWeights.get(forum).put(BIAS, 1.0);
		}
		for (String word : featureWords) {
			for (String forum : allWordWeights.keySet()) {
				allWordWeights.get(forum).put(word, 1.0);
			}
		}
	}
	
	public Random rand = new Random();
	public void fit() {
		
		for (String forum : allWordWeights.keySet()) {
			
			double[] labels = new double[trainLabels.size()];
			for (int i = 0; i < trainLabels.size(); ++i) {
				if (trainLabels.get(i).equals(forum))
					labels[i] = 1.0;
				else
					labels[i] = 0.0;
			}
			
			for (int l = 0; l < ALLITCnt; ++l) {
				for (int i = 0; i < trainPosts.size(); ++i) {
					Post post = trainPosts.get(i);
					
					double lire = innerProduct(allWordWeights.get(forum), post.wordTfIdf);
					double output = sigmoid(lire);
					double error = labels[i] - output;
					
					for (String word : post.wordTfIdf.keySet()) {
						allWordWeights.get(forum).put(word, allWordWeights.get(forum).get(word) + alpha * error * post.wordTfIdf.get(word));
					}
					allWordWeights.get(forum).put(BIAS, allWordWeights.get(forum).get(BIAS) + alpha * error);
					//System.out.println(labels[i] + ", " + error + ", " + allWordWeights.get(forum).get(BIAS));
				}
			}
			for (int l = 0; l < ITCnt; ++l) {
				for (int i = 0; i < trainPosts.size(); ++i) {
					int index = rand.nextInt(trainPosts.size());
					
					Post post = trainPosts.get(index);
					
					double lire = innerProduct(allWordWeights.get(forum), post.wordTfIdf);
					double output = sigmoid(lire);
					double error = labels[index] - output;
					
					for (String word : post.wordTfIdf.keySet()) {
						allWordWeights.get(forum).put(word, allWordWeights.get(forum).get(word) + alpha * error * post.wordTfIdf.get(word));
					}
					allWordWeights.get(forum).put(BIAS, allWordWeights.get(forum).get(BIAS) + alpha * error);
					//System.out.println(labels[i] + ", " + error + ", " + allWordWeights.get(forum).get(BIAS));
				}
			}
			//System.exit(1);
		}
		
		//for (String forum : allWordWeights.keySet())
		//	System.out.println(allWordWeights.get(forum).get(BIAS));
	}
	
	public String predicate(Post post) {
		HashMap<String, Integer> postFeatureWordCount = new HashMap<String, Integer>();
		for (String word : post.wordCount.keySet())
			if (featureWords.contains(word))
				postFeatureWordCount.put(word, post.wordCount.get(word));
		
		post = new Post(post.name, postFeatureWordCount);
		post.calTfIdf(wordIdf);
		
		double maxProb = Double.NEGATIVE_INFINITY;
		String postForumName = "";
		
		for (String forum : allWordWeights.keySet()) {
			double lire = innerProduct(allWordWeights.get(forum), post.wordTfIdf);
			double prob = sigmoid(lire);
			
			if (prob > maxProb) {
				maxProb = prob;
				postForumName = forum;
			}
			//System.out.println(prob);
		}
		
		return postForumName;
	}
	
	public double predicate(ArrayList<Post> posts, ArrayList<String> lables) {
		int rightCnt = 0;
		
		for (int i = 0; i < posts.size(); ++i) {
			Post post = posts.get(i);
			if (predicate(post).equals(lables.get(i)))
				rightCnt++;
		}
		
		return rightCnt / (double)posts.size();
	}
	
	public double innerProduct(HashMap<String, Double> weights, HashMap<String, Double> x) {
		double sum = weights.get(BIAS) * 0.001;
		for (String word : x.keySet()) {
			sum += weights.get(word) * x.get(word);
		}
		
		//System.out.println(weights.get(BIAS) + ", " + (sum - weights.get(BIAS)));
		
		return sum;
	}
	
	public double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(0.0 - x));
	}
}
