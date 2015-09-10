package com.lqw.sgdlr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;

import com.lqw.data.Forum;
import com.lqw.data.Post;
import com.lqw.tool.MyMath;
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
	
	public static final int K = 10;
	public static Random rand = new Random(System.currentTimeMillis());
	public static ArrayList<HashMap<String, Forum>> allData = new ArrayList<HashMap<String, Forum>>(K); 
	
	static {
		for (int i = 0; i < K; ++i)
			allData.add(new HashMap<String, Forum>());
	}
	
	public static long time = 0;
	
	public static void partitionPosts(String forumName, LinkedList<Post> posts) {
		
		for (int i = 0; i < K; ++i)
			allData.get(i).put(forumName, new Forum(forumName));
		
		int cnt = posts.size();
		int index = 0;
		while (cnt != 0) {
			int r = rand.nextInt(cnt);
			Post post = posts.remove(r);
			allData.get(index).get(forumName).addPost(post);
			
			cnt--;
			index = (index + 1) % K;
		}
	}
	
	public static double crossValidate(int idxValidation) {
		
		HashMap<String, Forum> trainForums = new HashMap<String, Forum>();
		
		trainForums.put(basketball, new Forum(basketball));
		trainForums.put(D_Computer, new Forum(D_Computer));
		trainForums.put(FleaMarket, new Forum(FleaMarket));
		trainForums.put(Girls, new Forum(Girls));
		trainForums.put(JobExpress, new Forum(JobExpress));
		trainForums.put(Mobile, new Forum(Mobile));
		trainForums.put(Stock, new Forum(Stock));
		trainForums.put(V_Suggestions, new Forum(V_Suggestions));
		trainForums.put(WarAndPeace, new Forum(WarAndPeace));
		trainForums.put(WorldFootball, new Forum(WorldFootball));
		
		for (int i = 0; i < K; ++i) {
			if (i == idxValidation)
				continue;
			
			for (Entry<String, Forum> forumEntry : allData.get(i).entrySet()) {
				trainForums.get(forumEntry.getKey()).addPosts(forumEntry.getValue().posts);
			}
		}
		
		ArrayList<Post> validationPosts = new ArrayList<Post>();
		ArrayList<String> lables = new ArrayList<String>();
		for (Entry<String, Forum> forumEntry : allData.get(idxValidation).entrySet()) {
			for (Post post : forumEntry.getValue().posts) {
				validationPosts.add(post);
				lables.add(forumEntry.getKey());
			}
		}
		
		long begin = System.currentTimeMillis();
		
		double prec = dm(trainForums, validationPosts, lables);
		
		long end = System.currentTimeMillis();
		time += end - begin;
		
		return prec;
	}

	public static double dm(HashMap<String, Forum> trainForums, ArrayList<Post> validationPosts, ArrayList<String> lables) {
		
		SGDLR sgdlr = new SGDLR(trainForums);
		sgdlr.fit();
		return sgdlr.predicate(validationPosts, lables);
	}
	
	public static void main(String[] args) {
		
		File file = new File(path);
		if (!file.isDirectory()) {
			System.err.println(path + " should be a directory");
			System.exit(-1);
		}
		
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				LinkedList<Post> posts = WordSegmenter.segmenteFile(path + docName);
				partitionPosts(docName, posts);
			}
		}
		
		//System.out.println(crossValidate(0));
		
		{
			time = 0;
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidate(i);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			System.out.println("time: " + time / 1000.0 + "s");
		}
		
	}
}
