package com.lqw;

import java.util.ArrayList;

public class MTEvaluation {

	public static int MAX_RPEAT = 3;
	
	public static double evalute(ArrayList<Sentence> ngramOutput, ArrayList<ArrayList<Sentence>> allRef) {
		double score = 0.0;
		int zeroNum = 0;
		
		for (int i = 0; i < ngramOutput.size(); ++i) {
			Sentence outputSent = ngramOutput.get(i);
			int C = 0;
			int N = outputSent.ngramNum;
			if (N == 0) {
				zeroNum += 1;
				continue;
			}
			for (String ngram : outputSent.ngramCount.keySet()) {
				for (ArrayList<Sentence> ref : allRef) {
					Sentence refSent = ref.get(i);
					if (refSent.ngramCount.containsKey(ngram)) {
						C += Math.min(MAX_RPEAT, outputSent.ngramCount.get(ngram));
						break;
					}
				}
			}
			score += (C / (double)N);
			//System.out.println(C / (double)N);
			//if (ngramOutput.size() == 1) {
			//	System.out.println(C + ", " + N);
			//}
		}
		
		return score / (ngramOutput.size() - zeroNum);
	}
}
