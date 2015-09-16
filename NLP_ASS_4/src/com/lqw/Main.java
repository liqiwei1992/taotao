package com.lqw;

import java.io.File;
import java.util.ArrayList;

public class Main {

	public static String ref1Path = "data/ref1.txt";
	public static String ref2Path = "data/ref2.txt";
	//public static String ref3Path = "data/ref3.txt";
	
	
	public static String refEnPath = "data/newstest2013-ref.en";
	
	
	public static ArrayList<ArrayList<Sentence>> allRef;
	
	
	public static void main(String[] args) {
		
		allRef = new ArrayList<ArrayList<Sentence>>();
		
		ArrayList<ArrayList<Sentence>> allNGramRef1 = WordSegmenter.segmenteFile(ref1Path);
		allRef.add(allNGramRef1.get(0));
		
		ArrayList<ArrayList<Sentence>> allNGramRef2 = WordSegmenter.segmenteFile(ref2Path);
		allRef.add(allNGramRef2.get(0));
		
		//ArrayList<ArrayList<Sentence>> allNGramRef3 = WordSegmenter.segmenteFile(ref3Path);
		//allRef.add(allNGramRef3.get(0));
		
		String output1 = "data/translation.1.output.txt";
		System.out.print("translation.1.output.txt\t\t\t");
		System.out.println(evaluate(output1));
		
		String output2 = "data/translation.2.output.txt";
		System.out.print("translation.2.output.txt\t\t\t");
		System.out.println(evaluate(output2));
		
		
		allRef = new ArrayList<ArrayList<Sentence>>();
		
		ArrayList<ArrayList<Sentence>> allNGramRefEn = WordSegmenter.segmenteFile(refEnPath);
		allRef.add(allNGramRefEn.get(0));
		
		File fr_enDir = new File("data/fr-en/");
		for (String fr_en : fr_enDir.list()) {
			System.out.print(fr_en + "\t\t\t");
			System.out.println(evaluate("data/fr-en/" + fr_en));
		}
		
		
		
		
		/*
		allRef = new ArrayList<ArrayList<Sentence>>();
		
		ArrayList<ArrayList<Sentence>> ref1 = WordSegmenter.segmenteFile("data_test/ref_1.txt");
		allRef.add(ref1.get(0));
		
		ArrayList<ArrayList<Sentence>> ref2 = WordSegmenter.segmenteFile("data_test/ref_2.txt");
		allRef.add(ref2.get(0));
		
		ArrayList<ArrayList<Sentence>> ref3 = WordSegmenter.segmenteFile("data_test/ref_3.txt");
		allRef.add(ref3.get(0));
		System.out.println();
		System.out.println(evaluate("data_test/output_1.txt"));
		System.out.println(evaluate("data_test/output_2.txt"));
		*/
	}
	
	public static double evaluate(String outputPath) {
		ArrayList<ArrayList<Sentence>> allNGramOutput = WordSegmenter.segmenteFile(outputPath);
		ArrayList<Sentence> unigramOutput = allNGramOutput.get(0);
		
		double unigramScore = MTEvaluation.evalute(unigramOutput, allRef);
		
		return unigramScore;
	}
}
