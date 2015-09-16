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
import com.lqw.nb.NBD.Type;
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
	public static ArrayList<HashMap<String, Forum>> KBigramForums;
	public static ArrayList<HashMap<String, Forum>> KBothForums;
	
	public static Random rand;
	
	public static void main(String[] args) {
		
		//1415336481133
		long time = System.currentTimeMillis();
		//System.out.println(time);
		rand = new Random(time);
		
		File file = new File(pathLily);
		if (!file.isDirectory()) {
			System.err.println(pathLily + " should be a directory");
			System.exit(-1);
		}
		
		KWordForums = new ArrayList<HashMap<String, Forum>>(K);
		KBigramForums = new ArrayList<HashMap<String, Forum>>(K);
		KBothForums = new ArrayList<HashMap<String, Forum>>(K);
		for (int i = 0; i < K; ++i) {
			KWordForums.add(new HashMap<String, Forum>());
			KBigramForums.add(new HashMap<String, Forum>());
			KBothForums.add(new HashMap<String, Forum>());
		}
		
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				ArrayList<LinkedList<Post>> allPosts = WordSegmenter.segmenteFile(pathLily, docName);
				
				LinkedList<Post> wordPosts = allPosts.get(0);
				LinkedList<Post> bigramPosts = allPosts.get(1);
				LinkedList<Post> bothPosts = allPosts.get(2);
				
				partitionPosts(KWordForums, docName, wordPosts);
				partitionPosts(KBigramForums, docName, bigramPosts);
				partitionPosts(KBothForums, docName, bothPosts);
			}
		}
		
		
		//System.out.println(crossValidatePost(0));
		{
			System.out.println(K + "-cross validation of Word NBD for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(Type.WORD, KWordForums, i);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
		//System.out.println(crossValidatePost(0));
		{
			System.out.println(K + "-cross validation of Bigram NBD for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(Type.BIGRAM, KBigramForums, i);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
		//System.out.println(crossValidatePost(0));
		{
			System.out.println(K + "-cross validation of Word and Bigram NBD for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(Type.BOTH, KBothForums, i);
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
	
	public static double crossValidatePost(Type type, ArrayList<HashMap<String, Forum>> KForums, int idxValidation) {
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
		
		NBD nbd = new NBD(type,trainForums);
		nbd.train();
		
		return nbd.predicate(validationPosts);
	}
}
