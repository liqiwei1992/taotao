package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.lqw.data.Post;


public class WordSegmenter {

	public static ArrayList<HashMap<String, Integer>> segmente(String content) {
		HashMap<String, Integer> bigramCount = new HashMap<String, Integer>();
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		
		ArrayList<HashMap<String, Integer>> allCount = new ArrayList<HashMap<String, Integer>>();
		allCount.add(wordCount);
		allCount.add(bigramCount);
		
		try {
			StringReader reader = new StringReader(content);
			IKSegmenter ik = new IKSegmenter(reader, true);
			
			Lexeme lexeme = ik.next();
			if (lexeme == null) return allCount;
			
			String w1 = lexeme.getLexemeText();
			wordCount.put(w1, 1);
			//bigramCount.put(w1, 1);
			while ((lexeme = ik.next()) != null){
				String w2 = lexeme.getLexemeText();
				//Bigram bigram = new Bigram(w1, w2);
				String bigram = w1 + ":" + w2;
				if (bigramCount.containsKey(bigram))
					bigramCount.put(bigram, bigramCount.get(bigram) + 1);
				else
					bigramCount.put(bigram, 1);
				
				/*
				if (bigramCount.containsKey(w2))
					bigramCount.put(w2, bigramCount.get(w2) + 1);
				else
					bigramCount.put(w2, 1);
				*/
				if (wordCount.containsKey(w2))
					wordCount.put(w2, wordCount.get(w2) + 1);
				else
					wordCount.put(w2, 1);
				
				w1 = w2;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allCount;
	}
	
	public static ArrayList<LinkedList<Post>> segmenteFile(String path, String docName) {
		LinkedList<Post> wordPosts = new LinkedList<Post>();
		LinkedList<Post> bigramPosts = new LinkedList<Post>();
		LinkedList<Post> bothPosts = new LinkedList<Post>();
		
		ArrayList<LinkedList<Post>> allPosts = new ArrayList<LinkedList<Post>>();
		allPosts.add(wordPosts);
		allPosts.add(bigramPosts);
		allPosts.add(bothPosts);
		
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + docName)));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				ArrayList<HashMap<String, Integer>> allCount = segmente(line);
				HashMap<String, Integer> wordCount = allCount.get(0);
				HashMap<String, Integer> bigramCount = allCount.get(1);
				
				Post wordPost = new Post(docName);
				wordPost.addWordCount(wordCount);
				wordPosts.add(wordPost);
				
				Post bigramPost = new Post(docName);
				bigramPost.addBigramCount(bigramCount);
				bigramPosts.add(bigramPost);
				
				Post bothPost = new Post(docName);
				bothPost.addWordCount(wordCount);
				bothPost.addBigramCount(bigramCount);
				bothPosts.add(bothPost);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allPosts;
	}
}
