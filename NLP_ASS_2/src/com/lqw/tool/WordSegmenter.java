package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import ICTCLAS.I3S.AC.ICTCLAS50;

import com.lqw.data.Post;




public class WordSegmenter {

	private ICTCLAS50 testICTCLAS50;
	private HashSet<String> stopWords = new HashSet<String>();
	
	public WordSegmenter() {
		try {
			testICTCLAS50 = new ICTCLAS50();
			String arg = ".";
			
			if (testICTCLAS50.ICTCLAS_Init(arg.getBytes("GB2312")) == false) {
				System.err.println("Init Fail!");
				System.exit(-1);
			}
			
			String usrdir = "userdict.txt";
			byte[] usrdirb = usrdir.getBytes();
			testICTCLAS50.ICTCLAS_ImportUserDictFile(usrdirb, 0);
			
			String stopdir = "cn_stopword.dic";
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stopdir)));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				String word = line.trim();
				stopWords.add(word);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public ArrayList<HashMap<String, Integer>> segmente(String content) {
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		HashMap<String, Integer> word2Count = new HashMap<String, Integer>();
		
		ArrayList<HashMap<String, Integer>> allCount = new ArrayList<HashMap<String, Integer>>();
		allCount.add(wordCount);
		allCount.add(word2Count);
		
		try {
			byte contentBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(content.getBytes("GB2312"), 2, 1);
			String contentStr = new String(contentBytes, 0, contentBytes.length, "GB2312");
			String[] words = contentStr.split("\\s+");
			for (String word2 : words) {
				if (word2.matches("-?\\d+.*") || word2.matches("\\w+.*") || word2.length() == 0)
					continue;
				
				int idxOfSlash = word2.indexOf("/");
				if (idxOfSlash == -1) continue;
				
				String word = word2.substring(0, idxOfSlash);
				if (word.length() == 0 || stopWords.contains(word))
					continue;
				
				if (wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + 1);
				} else {
					wordCount.put(word, 1);
				}
				
				if (word2Count.containsKey(word2)) {
					word2Count.put(word2, word2Count.get(word2) + 1);
				} else {
					word2Count.put(word2, 1);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allCount;
	}
	
	public ArrayList<LinkedList<Post>> segmenteFile(String path, String docName) {
		LinkedList<Post> wordPosts = new LinkedList<Post>();
		LinkedList<Post> word2Posts = new LinkedList<Post>();
		
		ArrayList<LinkedList<Post>> allPosts = new ArrayList<LinkedList<Post>>();
		allPosts.add(wordPosts);
		allPosts.add(word2Posts);
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + docName)));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				ArrayList<HashMap<String, Integer>> allCount = segmente(line);
				HashMap<String, Integer> wordCount = allCount.get(0);
				HashMap<String, Integer> word2Count = allCount.get(1);
				
				Post wordPost = new Post(docName);
				wordPost.addWordCount(wordCount);
				wordPosts.add(wordPost);
				
				Post word2Post = new Post(docName);
				word2Post.addWordCount(word2Count);
				word2Posts.add(word2Post);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allPosts;
	}
	
	public void exit() {
		testICTCLAS50.ICTCLAS_SaveTheUsrDic();
		testICTCLAS50.ICTCLAS_Exit();
	}
}
