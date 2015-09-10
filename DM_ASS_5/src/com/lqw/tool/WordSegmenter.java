package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.lqw.data.Post;

public class WordSegmenter {

	public static HashMap<String, Integer> segmente(String content) {
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		
		try {
			StringReader reader = new StringReader(content);
			IKSegmenter ik = new IKSegmenter(reader, true);
			Lexeme lexeme = null;
			while ((lexeme = ik.next()) != null) {
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
	
	public static LinkedList<Post> segmenteFile(String path, String docName) {
		LinkedList<Post> posts = new LinkedList<Post>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + docName)));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				Post post = new Post(segmente(line), docName);
				posts.add(post);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
