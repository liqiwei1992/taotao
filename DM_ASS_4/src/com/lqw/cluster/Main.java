package com.lqw.cluster;

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
		
		ArrayList<Post> rawPosts = new ArrayList<Post>();
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				ArrayList<Post> posts = WordSegmenter.segmenteFile(path, docName);
				
				rawPosts.addAll(posts);
			}
		}
		
		
		dmKMeans(rawPosts);
			
		System.out.println("\n");
		
		dmFSFDP(rawPosts);
	}
	
	public static void dmKMeans(ArrayList<Post> rawPosts) {
		
		System.out.println("KMeans");
		{
			long begin = System.currentTimeMillis();
			KMeans km = new KMeans(rawPosts, 10, 15);
			km.classify();
			System.out.println("NMI: " + km.evaluate());
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + "s");
		}
	}
	
	public static void dmFSFDP(ArrayList<Post> rawPosts) {
		
		System.out.println("FSFDP");
		{
			long begin = System.currentTimeMillis();
			FSFDP fsfdp = new FSFDP(rawPosts);
			fsfdp.classify();
			System.out.println("NMI: " + fsfdp.evaluate());
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + "s");
		}
	}
}
