package com.lqw;

import java.util.HashMap;

public class Sentence {

	public HashMap<String, Integer> ngramCount = new HashMap<String, Integer>();
	public int ngramNum = 0;
	
	public void addNGramCount(HashMap<String, Integer> ngramCount) {
		for (String ngram : ngramCount.keySet()) {
			this.ngramNum += ngramCount.get(ngram);
			if (this.ngramCount.containsKey(ngram))
				this.ngramCount.put(ngram, this.ngramCount.get(ngram) + ngramCount.get(ngram));
			else
				this.ngramCount.put(ngram, ngramCount.get(ngram));
		}
	}
}
