package com.lqw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;

import com.lqw.ab.Adaboost;
import com.lqw.data.Forum;
import com.lqw.data.Post;
import com.lqw.gbdt_v2.GBDT;
import com.lqw.rf.RandomForest;
import com.lqw.tool.MyMath;
import com.lqw.tool.WordSegmenter;

public class Main {

	public static String pathLily = ".\\lily\\";
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
	public static Random rand;
	
	//record the all forums of lily for K Crossing Validation
	public static ArrayList<HashMap<String, Forum>> KForums;
	
	public static enum AL_TYPE {RandomForest, Adaboost, GBDT};
	
	public static void main(String[] args) {
		
		
		File file = new File(pathLily);
		if (!file.isDirectory()) {
			System.err.println(pathLily + " should be a directory");
			System.exit(-1);
		}
		
		HashMap<String, ArrayList<Post>> allPosts = new HashMap<String, ArrayList<Post>>();
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				ArrayList<Post> posts = WordSegmenter.segmenteFile(pathLily, docName);
				//partitionPosts(docName, posts);
				allPosts.put(docName, posts);
			}
		}
		
		/*
		KForums = new ArrayList<HashMap<String, Forum>>(K);
		for (int i = 0; i < K; ++i)
			KForums.add(new HashMap<String, Forum>());
		
		long time = System.currentTimeMillis();
		rand = new Random(time);
		for (String docName : allPosts.keySet())
			partitionPosts(docName, allPosts.get(docName));
		*/
		
		//random forest
		
		{
			KForums = new ArrayList<HashMap<String, Forum>>(K);
			for (int i = 0; i < K; ++i)
				KForums.add(new HashMap<String, Forum>());
			
			long time = System.currentTimeMillis();
			//long time = 1415150834695L;
			//System.out.println(time);
			rand = new Random(time);
			for (String docName : allPosts.keySet())
				partitionPosts(docName, allPosts.get(docName));
			
			randomForest();
		}
		
		
		
		//adaboost
		
		{
			KForums = new ArrayList<HashMap<String, Forum>>(K);
			for (int i = 0; i < K; ++i)
				KForums.add(new HashMap<String, Forum>());
			
			long time = System.currentTimeMillis();
			//long time = 1414721574085L;
			//System.out.println(time);
			rand = new Random(time);
			for (String docName : allPosts.keySet())
				partitionPosts(docName, allPosts.get(docName));
			
			adaboost();
		}
		
		
		
		//gbdt
		
		{
			KForums = new ArrayList<HashMap<String, Forum>>(K);
			for (int i = 0; i < K; ++i)
				KForums.add(new HashMap<String, Forum>());
			
			long time = System.currentTimeMillis();
			//long time = 1415079570288L;
			//System.out.println(time);
			rand = new Random(time);
			for (String docName : allPosts.keySet())
				partitionPosts(docName, allPosts.get(docName));
			
			gbdt();
		}
		
	}
	
	public static void randomForest() {
		
		
		//System.out.println(crossValidatePost(0, AL_TYPE.RandomForest));
		
		{
			System.out.println(K + "-cross validation of Random Forest for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(i, AL_TYPE.RandomForest);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
	}
	
	public static void adaboost() {
		/*
		{
			long begin = System.currentTimeMillis();
			System.out.println(crossValidatePost(0, AL_TYPE.Adaboost));
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		*/
		
		{
			System.out.println(K + "-cross validation of Adaboost for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(i, AL_TYPE.Adaboost);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
	}
	
	public static void gbdt() {
		/*
		{
			long begin = System.currentTimeMillis();
			System.out.println(crossValidatePost(0, AL_TYPE.GBDT));
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		*/
		
		{
			System.out.println(K + "-cross validation of GBDT for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i) {
				precision[i] = crossValidatePost(i, AL_TYPE.GBDT);
				System.out.println(precision[i]);
			}
			
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
	}
	
	public static void partitionPosts(String forumName, ArrayList<Post> rawPosts) {
		LinkedList<Post> posts = new LinkedList<Post>();
		for (int p = 0; p < rawPosts.size(); ++p)
			posts.add(rawPosts.get(p));
		
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
	
	public static double crossValidatePost(int idxValidation, AL_TYPE at) {
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
		
		if (at == AL_TYPE.RandomForest) {
			RandomForest rf = new RandomForest(trainForums);
			rf.build();
			return rf.predicate(validationPosts);
		} else if (at == AL_TYPE.Adaboost) {
			Adaboost ab = new Adaboost(trainForums);
			ab.build();
			return ab.predicate(validationPosts);
		} else if (at == AL_TYPE.GBDT) {
			//ClassificationGBDT cgbdt = new ClassificationGBDT(trainForums);
			//cgbdt.build();
			//return cgbdt.predicat(validationPosts);
			
			GBDT gbdt = new GBDT(trainForums);
			gbdt.build();
			return gbdt.predicat(validationPosts);
		}
		
		return 0.0;
	}
}
