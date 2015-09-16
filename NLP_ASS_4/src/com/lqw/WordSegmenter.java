package com.lqw;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public class WordSegmenter {

	public static ArrayList<HashMap<String, Integer>> segmente(String content) {
		HashMap<String, Integer> unigramCount = new HashMap<String, Integer>();
		HashMap<String, Integer> bigramCount = new HashMap<String, Integer>();
		
		
		ArrayList<HashMap<String, Integer>> allNGramCount = new ArrayList<HashMap<String, Integer>>();
		allNGramCount.add(unigramCount);
		allNGramCount.add(bigramCount);
		
		try {
			StringReader reader = new StringReader(content);
			IKSegmenter ik = new IKSegmenter(reader, true);
			
			Lexeme lexeme = ik.next();
			if (lexeme == null) return allNGramCount;
			
			String w1 = lexeme.getLexemeText().toLowerCase();
			unigramCount.put(w1, 1);
			while ((lexeme = ik.next()) != null){
				String w2 = lexeme.getLexemeText().toLowerCase();
				String bigram = w1 + ":" + w2;
				if (bigramCount.containsKey(bigram))
					bigramCount.put(bigram, bigramCount.get(bigram) + 1);
				else
					bigramCount.put(bigram, 1);
				
				if (unigramCount.containsKey(w2))
					unigramCount.put(w2, unigramCount.get(w2) + 1);
				else
					unigramCount.put(w2, 1);
				
				w1 = w2;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allNGramCount;
	}
	
	public static ArrayList<ArrayList<Sentence>> segmenteFile(String path) {
		ArrayList<Sentence> unigramSentences = new ArrayList<Sentence>();
		
		ArrayList<ArrayList<Sentence>> allSentences = new ArrayList<ArrayList<Sentence>>();
		allSentences.add(unigramSentences);
		
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				
				ArrayList<HashMap<String, Integer>> allNGramCount = segmente(line);
				HashMap<String, Integer> unigramCount = allNGramCount.get(0);
				
				Sentence unigramSentence = new Sentence();
				unigramSentence.addNGramCount(unigramCount);
				unigramSentences.add(unigramSentence);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allSentences;
	}
}
