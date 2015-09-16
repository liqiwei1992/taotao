package com.lqw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;

import com.lqw.data.Forum;
import com.lqw.data.Post;
import com.lqw.nb.NBD;
import com.lqw.tool.MyMath;
import com.lqw.tool.WordSegmenter;

public class Main {

	public static String pathLily = "./Lily/";
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
	//record the all forums of lily for K Crossing Validation
	public static ArrayList<HashMap<String, Forum>> KWordForums;
	public static ArrayList<HashMap<String, Forum>> KWord2Forums;
	
	public static Random rand;
	
	public static void main(String[] args) {
		
		long time = System.currentTimeMillis();
		rand = new Random(time);
		
		File file = new File(pathLily);
		if (!file.isDirectory()) {
			System.err.println(pathLily + " should be a directory");
			System.exit(-1);
		}
		
		KWordForums = new ArrayList<HashMap<String, Forum>>(K);
		KWord2Forums = new ArrayList<HashMap<String, Forum>>(K);
		for (int i = 0; i < K; ++i) {
			KWordForums.add(new HashMap<String, Forum>());
			KWord2Forums.add(new HashMap<String, Forum>());
		}
		
		WordSegmenter ws = new WordSegmenter();
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				
				ArrayList<LinkedList<Post>> allPosts = ws.segmenteFile(pathLily, docName);
				
				LinkedList<Post> wordPosts = allPosts.get(0);
				LinkedList<Post> word2Posts = allPosts.get(1);
				
				//System.out.println(wordPosts.get(0).wordCount.size() + ": " + wordPosts.get(0));
				//System.out.println(word2Posts.get(0).wordCount.size() + ": " + word2Posts.get(0));
				//System.out.println();
				
				partitionPosts(KWordForums, docName, wordPosts);
				partitionPosts(KWord2Forums, docName, word2Posts);
			}
		}
		ws.exit();
		
		//System.out.println(crossValidatePost(0));
		{
			System.out.println(K + "-cross validation of Word NBD for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(KWordForums, i);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
		//System.out.println(crossValidatePost(0));
		{
			System.out.println(K + "-cross validation of PoS Word NBD for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(KWord2Forums, i);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
	}
	
	public static void partitionPosts(ArrayList<HashMap<String, Forum>> KForums, String forumName, LinkedList<Post> posts) {
		for (int i = 0; i < K; ++i)
			KForums.get(i).put(forumName, new Forum(forumName));
		
		int cnt = posts.size();
		int index = 0;
		while (cnt != 0) {
			int r = rand.nextInt(cnt);
			Post post = posts.remove(r);
			KForums.get(index).get(forumName).addPost(post);
			
			cnt--;
			index = (index + 1) % K;
		}
	}
	
	public static double crossValidatePost(ArrayList<HashMap<String, Forum>> KForums, int idxValidation) {
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
			
			for (Entry<String, Forum> forumEntry : KForums.get(i).entrySet()) {
				trainForums.get(forumEntry.getKey()).addPosts(forumEntry.getValue().posts);
			}
		}
		
		ArrayList<Post> validationPosts = new ArrayList<Post>();
		for (Entry<String, Forum> forumEntry : KForums.get(idxValidation).entrySet()) {
			for (Post post : forumEntry.getValue().posts) {
				validationPosts.add(post);
			}
		}
		
		NBD nbd = new NBD(trainForums);
		nbd.train();
		
		return nbd.predicate(validationPosts);
	}
}
