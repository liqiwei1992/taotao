package com.lqw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.lqw.data.Post;
import com.lqw.nb.NBD;
import com.lqw.tool.WordSegmenter;

public class Main {

	public static String TRAIN_LIST = ".\\data\\train2.rlabelclass";
	public static String TRAIN = ".\\data\\train2\\";
	
	public static String TEST_LIST = ".\\data\\test2.rlabelclass";
	public static String TEST = ".\\data\\test2\\";
	
	public static String POSITIVE = ".\\src\\ntusd-positive.txt";
	public static String NEGATIVE = ".\\src\\ntusd-negative.txt";
	
	public static String RESULT = ".\\result\\predict_test_label.txt";
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		
		WordSegmenter.readFeatureWords(NEGATIVE);
		WordSegmenter.readFeatureWords(POSITIVE);
		
		ArrayList<Post> trainPosts = WordSegmenter.segmenteFile(TRAIN_LIST, TRAIN);
		ArrayList<Post> testPosts = WordSegmenter.segmenteFile(TEST_LIST, TEST);
		
		predict(trainPosts, testPosts);
		
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end - start) / 1000.0 + " s");
	}
	
	public static void predict(ArrayList<Post> trainPosts, ArrayList<Post> testPosts) throws Exception {
		NBD nbd = new NBD(trainPosts);
		nbd.train();
		//double p = nbd.predicate(testPosts);
		//System.out.println(p);
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File(RESULT)));
		
		int rightCnt = 0;
		for (Post post : testPosts) {
			boolean predPositive = nbd.predicate(post);
			if (predPositive)
				pw.println(post.path + " " + "+1");
			else
				pw.println(post.path + " " + "-1");
			
			if (predPositive == post.positive)
				rightCnt++;
		}
		
		double p = rightCnt / (double)testPosts.size();
		//System.out.println(p);
		
		pw.close();
	}
	
}
