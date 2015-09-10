package com.lqw.feature;

import java.util.HashMap;

public class TF_IDF {

	
	public static HashMap<String, Double> calTfIdf(HashMap<String, Double> tf, HashMap<String, Double> wordIdf) {
		HashMap<String, Double> tf_idf = new HashMap<String, Double>();
		
		for (String word : tf.keySet()) {
			//System.out.println(postCnt + ", " + wordInPostCnt.get(word));
			tf_idf.put(word, tf.get(word) * wordIdf.get(word));
		}
		
		return tf_idf;
	}
}
