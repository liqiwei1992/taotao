package com.lqw.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.lqw.feature.Gauss;
import com.lqw.tool.MyMath;

public class NBCG {

	public static final double tf_threshold = 0.0002;
	
	public int allWordCnt = 0;
	public Set<String> featureWord = new HashSet<String>();
	
	public HashMap<String, Forum> train = new HashMap<String, Forum>();
	
	public int allPostCnt = 0;
	//record the # of posts that include the words
	//public HashMap<String, Integer> wordInPostCount = new HashMap<String, Integer>();
	//record the idf of words
	public HashMap<String, Double> wordIdf = new HashMap<String, Double>();
	
	
	public NBCG(HashMap<String, Forum> rawTrain) {
		for (Entry<String, Forum> forumEntry : rawTrain.entrySet())
			for (Entry<String, Double> wordTfEntry : forumEntry.getValue().wordTf.entrySet())
				if (wordTfEntry.getValue() > tf_threshold)
					featureWord.add(wordTfEntry.getKey());
		
		for (Entry<String, Forum> forumEntry : rawTrain.entrySet()) {
			allPostCnt += forumEntry.getValue().posts.size();
			
			ArrayList<Post> trainPosts = new ArrayList<Post>();
			for (Post post : forumEntry.getValue().posts) {
				HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
				for (Entry<String, Integer> wordEntry : post.wordCount.entrySet()) {
					if (featureWord.contains(wordEntry.getKey())) {
						wordCount.put(wordEntry.getKey(), post.wordCount.get(wordEntry.getKey()));
						
						allWordCnt += post.wordCount.get(wordEntry.getKey());
					}
				}
				trainPosts.add(new Post(post.name, wordCount));
			}
			
			Forum forum = new Forum(forumEntry.getKey(), trainPosts);
			train.put(forumEntry.getKey(), forum);
		}
		
		for (String word : featureWord) {
			//wordInPostCount.put(word, 0);
			int cnt = 0;
			for (Entry<String, Forum> forumEntry : train.entrySet()) {
				for (Post post : forumEntry.getValue().posts) {
					if (post.wordCount.containsKey(word)) {
						//wordInPostCount.put(word, wordInPostCount.get(word) + 1);
						cnt++;
					}
				}
			}
			wordIdf.put(word, Math.log(allPostCnt / ((double)cnt + 1.0)));
		}
		
		//if the forum does not contains a feature word, put it in the word count with 0
		for (String word : featureWord) {
			//System.out.println(word);
			for (Entry<String, Forum> forumEntry : train.entrySet()) {
				if (!forumEntry.getValue().wordCount.containsKey(word))
					forumEntry.getValue().wordCount.put(word, 0);
			}
		}
		
		for (Entry<String, Forum> forumEntry : train.entrySet()) {
			forumEntry.getValue().calFeature(wordIdf);
		}
		
		//System.out.println("# of feature words = " + featureWord.size());
	}
	
	public void fit() {
		for (Entry<String, Forum> forumEntry : train.entrySet()) {
			Forum forum = forumEntry.getValue();
			forum.priorProb = forum.allWordCnt / (double)this.allWordCnt;
			
			for (String word : forum.wordCount.keySet()) {
				double[] allTf_idf = new double[forum.posts.size()];
				int cnt = 0;
				for (Post post : forum.posts) {
					if (post.tf_idf.containsKey(word))
						//allTf_idf[cnt++] = 0.5 + 0.5 * post.tf_idf.get(word);
						allTf_idf[cnt++] = post.tf_idf.get(word);
					else
						allTf_idf[cnt++] = 0.0;
				}
				
				double mu = MyMath.average(allTf_idf);
				double sigma = MyMath.variance(allTf_idf) + 0.00001;
				
				forum.wordGaus.put(word, new Gauss(mu, sigma, featureWord.size(), forum.allWordCnt));
			}
		}
	}
	
	//Posterior probability
	public String predicate(Post post) {
		
		HashMap<String, Integer> postFeatureWordCount = new HashMap<String, Integer>();
		for (Entry<String, Integer> wordEntry : post.wordCount.entrySet())
			if (featureWord.contains(wordEntry.getKey()))
				postFeatureWordCount.put(wordEntry.getKey(), wordEntry.getValue());
		
		post = new Post(post.name, postFeatureWordCount);
		post.calTf();
		post.calTfIdf(wordIdf);
		
		double maxPosteriorProb = Double.NEGATIVE_INFINITY;
		String postForumName = "";
		
		for (Entry<String, Forum> forumEntry : train.entrySet()) {
			Forum forum = forumEntry.getValue();
			
			double posteriorProb = Math.log(forum.priorProb);
			for (Entry<String, Integer> wordEntry : post.wordCount.entrySet()) {
				if (featureWord.contains(wordEntry.getKey())) {
					posteriorProb += forum.wordGaus.get(wordEntry.getKey()).logFunction(post.tf_idf.get(wordEntry.getKey())) * wordEntry.getValue();
				}
			}
			//System.out.println(posteriorProb);
			if (posteriorProb > maxPosteriorProb) {
				postForumName = forum.name;
				maxPosteriorProb = posteriorProb;
			}
		}
		//System.out.println(maxPosteriorProb);
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
}
