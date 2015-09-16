package com.lqw.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lqw.data.Forum;
import com.lqw.data.Post;

public class NBD {

	public static final int threshold_word_num = 0;
	
	public int allWordCnt = 0;
	
	public HashMap<String, Forum> trainForums = new HashMap<String, Forum>();
	
	public HashSet<String> featureWord = new HashSet<String>();
	public HashMap<String, Integer> wordCnt = new HashMap<String, Integer>();
	
	
	public NBD(HashMap<String, Forum> rawTrainForums) {
		//build the feature word set
		for (String forumName : rawTrainForums.keySet()) {
			Forum forum = rawTrainForums.get(forumName);
			for (String word : forum.wordCount.keySet()) {
				if (forum.wordCount.get(word) > threshold_word_num) {
					featureWord.add(word);
				}
			}
		}
		//System.out.println("feature word size: " + featureWord.size());
		
		//build the train forums with feature word
		for (String forumName : rawTrainForums.keySet()) {
			Forum forum = rawTrainForums.get(forumName);
			ArrayList<Post> trainPosts = new ArrayList<Post>();
			
			for (Post post : forum.posts) {
				HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
				for (String word : post.wordCount.keySet()) {
					if (featureWord.contains(word)) {
						wordCount.put(word, post.wordCount.get(word));
						
						allWordCnt += post.wordCount.get(word);
					}
				}
				
				Post newPost = new Post(post.forumName);
				newPost.addWordCount(wordCount);
				trainPosts.add(newPost);
			}
			
			trainForums.put(forumName, new Forum(forumName, trainPosts));
		}
		
		//if the forum does not contains a feature word, put it in the word count with 0
		for (String word : featureWord) {
			for (String forumName : trainForums.keySet()) {
				if (!trainForums.get(forumName).wordCount.containsKey(word))
					trainForums.get(forumName).wordCount.put(word, 0);
			}
		}
	}
	
	public void train() {
		for (String forumName : trainForums.keySet()) {
			Forum forum = trainForums.get(forumName);
			
			forum.priorProb = forum.allWordCnt / (double)this.allWordCnt;
			
			for (String word : forum.wordCount.keySet()) {
				forum.wordCondProb.put(word, (forum.wordCount.get(word) + 1.0) / ((double)forum.allWordCnt + featureWord.size()));
				
				//System.out.println(forum.bigramCondProb.get(word));
			}
		}
	}
	
	public String predicate(Post post) {
		double maxPosteriorProb = Double.NEGATIVE_INFINITY;
		String predForumName = "";
		
		for (String forumName : trainForums.keySet()) {
			Forum forum = trainForums.get(forumName);
			double posteriorProb = Math.log(forum.priorProb);
			for (String word : post.wordCount.keySet()) {
				if (featureWord.contains(word)) {
					posteriorProb += Math.log(forum.wordCondProb.get(word)) * post.wordCount.get(word);
				}
			}
			
			//System.out.println(posteriorProb);
			
			if (posteriorProb > maxPosteriorProb) {
				predForumName = forum.name;
				maxPosteriorProb = posteriorProb;
			}
		}
		
		return predForumName;
	}
	
	public double predicate(ArrayList<Post> posts) {
		int rightCnt = 0;
		
		for (int i = 0; i < posts.size(); ++i) {
			Post post = posts.get(i);
			String predName = predicate(post);
			
			//System.out.println(post.forumName + " vs " + predName);
			
			if (post.forumName.equals(predName))
				rightCnt++;
		}
		
		return rightCnt / (double)posts.size();
	}
}
