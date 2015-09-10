package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.lqw.nb.Post;


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
	
	public static LinkedList<Post> segmenteFile(String fileName) {
		LinkedList<Post> posts = new LinkedList<Post>();
		
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				int index = line.indexOf("\t");
				if (index == -1)
					continue;
				String title = line.substring(0, index);
				
				//StringTokenizer st = new StringTokenizer(line);
				//if (st.countTokens() != 2)
				//	System.out.println("wrong tokenizer");
				//String title = st.nextToken();
				//String content = st.nextToken();
				
				Post post = new Post(title, segmente(line));
				posts.add(post);
				
				//System.out.println(title + ": ");
				//for (Entry<String, Integer> entry : post.wordCount.entrySet()) {
				//	System.out.println(entry.getKey() + " = " + entry.getValue());
				//}
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
