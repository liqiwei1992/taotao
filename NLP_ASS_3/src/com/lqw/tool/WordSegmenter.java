package com.lqw.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.lqw.data.Post;


public class WordSegmenter {

	public static HashSet<String> featureWords = new HashSet<String>();
	
	public static void readFeatureWords(String path) {
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			
			String word = null;
			while ((word = br.readLine()) != null) 
				featureWords.add(word.trim());
			
			br.close();
			
			//System.out.println(featureWords);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static HashMap<String, Integer> segmente(String content) {
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		
		try {
			StringReader reader = new StringReader(content);
			IKSegmenter ik = new IKSegmenter(reader, true);
			
			Lexeme lexeme = ik.next();
			if (lexeme == null) return wordCount;
			
			while ((lexeme = ik.next()) != null){
				String word = lexeme.getLexemeText();
				
				if (!featureWords.contains(word)) continue;
				
				if (wordCount.containsKey(word))
					wordCount.put(word, wordCount.get(word) + 1);
				else
					wordCount.put(word, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordCount;
	}
	
	public static ArrayList<Post> segmenteFile(String pathList, String path) {
		ArrayList<Post> posts = new ArrayList<Post>();
		
		try {
			BufferedReader brList = new BufferedReader(new InputStreamReader(new FileInputStream(pathList), "GBK"));
			
			String entryLine = null;
			while ((entryLine = brList.readLine()) != null) {
				String[] entry = entryLine.split(" ");
				
				String postPath = path + entry[0];
				boolean positive = false;
				if (entry[1].equals("+1"))
					positive = true;
				
				//System.out.println(entry[0] + ", " + entry[1]);
				
				File postFile = new File(postPath);
				BufferedReader brPost = new BufferedReader(new InputStreamReader(new FileInputStream(postFile), "GBK"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = brPost.readLine()) != null) {
					if (line.length() == 0)
						continue;
					sb.append(line);
				}
				
				//System.out.println(sb.toString());
				
				HashMap<String, Integer> wordCount = segmente(sb.toString());
				Post post = new Post(postFile.getName(), positive);
				post.addWordCount(wordCount);
				posts.add(post);
				
				//System.out.println(wordCount);
				
				brPost.close();
			}
			
			brList.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return posts;
	}
}
