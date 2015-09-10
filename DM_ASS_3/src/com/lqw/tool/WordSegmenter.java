package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.lqw.data.Post;
import com.lqw.lsh.Main;

public class WordSegmenter {

	
	
	public static HashMap<String, Integer> segmente(String content) {
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		
		try {
			StringReader reader = new StringReader(content);
			IKSegmenter ik = new IKSegmenter(reader, true);
			Lexeme lexeme = null;
			while ((lexeme = ik.next()) != null){
				String word = lexeme.getLexemeText();
				//if (word.length() == 1)
				//	continue;
				if (wordCount.containsKey(word))
					wordCount.put(word, wordCount.get(word) + 1);
				else
					wordCount.put(word, 1);
				
				//System.out.println(word);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordCount;
	}
	
	public static ArrayList<ArrayList<Post>> segmenteFile(String path, String docName) {
		ArrayList<Post> QPosts = new ArrayList<Post>();
		ArrayList<Post> X_QPosts = new ArrayList<Post>();
		
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + docName)));
			
			String line = null;
			int cnt = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				Post post = new Post(segmente(line), docName);
				if (cnt < Main.LINE) {
					QPosts.add(post);
				} else {
					X_QPosts.add(post);
				}
				
				cnt++;
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<ArrayList<Post>> posts = new ArrayList<ArrayList<Post>>();
		posts.add(QPosts);
		posts.add(X_QPosts);
		
		return posts;
	}
	
	/*
	private static class ValueComparator implements Comparator<Map.Entry<String,Integer>> {  
		public int compare(Map.Entry<String,Integer> m, Map.Entry<String,Integer> n) {  
			return n.getValue() - m.getValue();
		}
	}
	
	
	public static void main(String[] args) {
		HashMap<String, Integer> hm = segmente(path + basketball);
		
		ArrayList<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>();
		list.addAll(hm.entrySet());
		WordSegmentation.ValueComparator vc = new ValueComparator();
		Collections.sort(list, vc);
		for (Entry<String, Integer> entry : list)
			System.out.println(entry.getKey() + " = " + entry.getValue());
	}
	*/
	
}
