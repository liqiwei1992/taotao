package com.lqw.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class NBD {

	public static double tf_threshold = 0.0002;
	//public static double tf_threshold = 0.00085;
	
	//record the # of words (each appearance count 1)
	public int allWordCnt = 0;
	//record the all feature words
	public Set<String> featureWord = new HashSet<String>();
	
	public HashMap<String, Forum> train = new HashMap<String, Forum>();
	
	public NBD(HashMap<String, Forum> rawTrain) {
		
		//extract the feature words
		for (Entry<String, Forum> forumEntry : rawTrain.entrySet())
			for (Entry<String, Double> wordTfEntry : forumEntry.getValue().wordTf.entrySet())
				if (wordTfEntry.getValue() > tf_threshold)
					featureWord.add(wordTfEntry.getKey());
		
		//build the train forums with feature word
		for (Entry<String, Forum> forumEntry : rawTrain.entrySet()) {
			ArrayList<Post> trainPosts = new ArrayList<Post>();
			for (Post post : forumEntry.getValue().posts) {
				HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
				for (Entry<String, Integer> wordEntry : post.wordCount.entrySet()) {
					if (featureWord.contains(wordEntry.getKey())) {
						//System.out.println(wordEntry.getKey() + ", " + postEntry.getValue().wordCount.get(wordEntry.getKey()));
						wordCount.put(wordEntry.getKey(), post.wordCount.get(wordEntry.getKey()));
						
						allWordCnt += post.wordCount.get(wordEntry.getKey());
					}
				}
				trainPosts.add(new Post(post.name, wordCount));
			}
			
			//countWord() will be called in the calFeature()
			Forum forum = new Forum(forumEntry.getKey(), trainPosts);
			forum.countWord();
			train.put(forumEntry.getKey(), forum);
		}
		
		//if the forum does not contains a feature word, put it in the word count with 0
		for (String word : featureWord) {
			for (Entry<String, Forum> forumEntry : train.entrySet()) {
				if (!forumEntry.getValue().wordCount.containsKey(word))
					forumEntry.getValue().wordCount.put(word, 0);
			}
		}
		
		//System.out.println("# of feature words = " + featureWord.size());
		//System.out.println(allWordCnt);
		
	}
	
	
	public void fit() {
		
		for (Entry<String, Forum> forumEntry : train.entrySet()) {
			Forum forum = forumEntry.getValue();
			
			forum.priorProb = forum.allWordCnt / (double)this.allWordCnt;
			for (Entry<String, Integer> wordEntry : forum.wordCount.entrySet()) {
				forum.wordCondProb.put(wordEntry.getKey(), (wordEntry.getValue() + 1.0) / ((double)forum.allWordCnt + featureWord.size()));
			}
		}
	}
	
	//Posterior probability
	public String predicate(Post post) {
		
		double maxPosteriorProb = Double.NEGATIVE_INFINITY;
		String postForumName = "";
		
		for (Entry<String, Forum> forumEntry : train.entrySet()) {
			Forum forum = forumEntry.getValue();
			
			double posteriorProb = Math.log(forum.priorProb);
			for (Entry<String, Integer> wordEntry : post.wordCount.entrySet()) {
				if (featureWord.contains(wordEntry.getKey())) {
					posteriorProb += Math.log(forum.wordCondProb.get(wordEntry.getKey())) * wordEntry.getValue();
				}
			}
			
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
