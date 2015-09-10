package com.lqw.lsh;

import java.io.File;
import java.util.ArrayList;

import com.lqw.data.Post;
import com.lqw.tool.WordSegmenter;

public class Main {

	public static String path = "E:\\Data Mining\\data\\lily\\";
	
	public static String basketball = "Basketball.txt";
	public static String D_Computer = "D_Computer.txt";
	public static String FleaMarket = "FleaMarket.txt";
	public static String Girls = "Girls.txt";
	public static String JobExpress = "JobExpress.txt";
	public static String Mobile = "Mobile.txt";
	public static String Stock = "Stock.txt";
	public static String V_Suggestions = "V_Suggestions.txt";
	public static String WarAndPeace = "WarAndPeace.txt";
	public static String WorldFootball = "WorldFootball.txt";
	
	public static void main(String[] args) {
		File file = new File(path);
		if (!file.isDirectory()) {
			System.err.println(path + " should be a directory");
			System.exit(-1);
		}
		
		ArrayList<Post> rawQPosts = new ArrayList<Post>();
		ArrayList<Post> rawX_QPosts = new ArrayList<Post>();
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				ArrayList<ArrayList<Post>> posts = WordSegmenter.segmenteFile(path, docName);
				
				rawQPosts.addAll(posts.get(0));
				rawX_QPosts.addAll(posts.get(1));
			}
		}
		
		dmBF(rawX_QPosts, rawQPosts);
		
		System.out.println("\n");
		
		dmLSH(rawX_QPosts, rawQPosts);
	}
	
	public static final int LINE = 20;
	public static int[] K = {10, 20, 30, 40, 50};
	public static void dmBF(ArrayList<Post> rawX_QPosts, ArrayList<Post> rawQPosts) {
		
		{
			for (int i = 0; i < K.length; ++i) {
				long begin = System.currentTimeMillis();
				
				BruteForce bf = new BruteForce(rawX_QPosts, rawQPosts);
				bf.KNearest(K[i]);
				
				long end = System.currentTimeMillis();
				System.out.printf("time = %.3f(s)\n", (end - begin) / 1000.0);
			}
		}
	}
	
	public static void dmLSH(ArrayList<Post> rawX_QPosts, ArrayList<Post> rawQPosts) {
		{
			for (int i = 0; i < K.length; ++i) {
				long begin = System.currentTimeMillis();
				
				LSH lsh = new LSH(rawX_QPosts, rawQPosts);
				lsh.KNearest(K[i]);
				
				long end = System.currentTimeMillis();
				System.out.printf("time = %.3f(s)\n", (end - begin) / 1000.0);
			}
		}
	}
}
