package com.lqw.nb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import com.lqw.tool.MyMath;
import com.lqw.tool.WordSegmenter;

public class Main {
	
	public static enum ClassifierType {NBD, NBCD, NBCG};
	
	public static String path = "E:\\Data Mining\\data\\lily\\";
	//public static String path = "D:\\java_workspace-4.4\\DM_ASS_1\\test\\";
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
	
	public static double crossValidate(int idxValidation, ClassifierType ct) {
		
		HashMap<String, Forum> forums = new HashMap<String, Forum>();
		
		
		forums.put(basketball, new Forum(basketball));
		forums.put(D_Computer, new Forum(D_Computer));
		forums.put(FleaMarket, new Forum(FleaMarket));
		forums.put(Girls, new Forum(Girls));
		forums.put(JobExpress, new Forum(JobExpress));
		forums.put(Mobile, new Forum(Mobile));
		forums.put(Stock, new Forum(Stock));
		forums.put(V_Suggestions, new Forum(V_Suggestions));
		forums.put(WarAndPeace, new Forum(WarAndPeace));
		forums.put(WorldFootball, new Forum(WorldFootball));
		
		for (int i = 0; i < K; ++i) {
			if (i == idxValidation)
				continue;
			
			for (Entry<String, Forum> forumEntry : allData.get(i).entrySet()) {
				forums.get(forumEntry.getKey()).addPosts(forumEntry.getValue().posts);
			}
		}
		
		//for extract the feature words
		for (Entry<String, Forum> forumEntry : forums.entrySet()) {
			forumEntry.getValue().calTf();
		}
		
		ArrayList<Post> validationPosts = new ArrayList<Post>();
		ArrayList<String> lables = new ArrayList<String>();
		for (Entry<String, Forum> forumEntry : allData.get(idxValidation).entrySet()) {
			for (Post post : forumEntry.getValue().posts) {
				validationPosts.add(post);
				lables.add(forumEntry.getKey());
			}
		}
		
		return dm(ct, forums, validationPosts, lables);
	}

	public static double dm(ClassifierType ct, HashMap<String, Forum> trainForums, ArrayList<Post> validationPosts, ArrayList<String> lables) {
		
		if (ct == ClassifierType.NBD) {
			NBD nbd = new NBD(trainForums);
			nbd.fit();
			return nbd.predicate(validationPosts, lables);
		} else if (ct == ClassifierType.NBCD) {
			NBCD nbcd = new NBCD(trainForums);
			nbcd.fit();
			return nbcd.predicate(validationPosts, lables);
		} else { //if (ct == ClassifierType.NBCG) {
			NBCG nbcg = new NBCG(trainForums);
			nbcg.fit();
			return nbcg.predicate(validationPosts, lables);
		}
	}
	
	public static void main(String[] args) {
		
		long begin = System.currentTimeMillis();
		
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
		
		long end = System.currentTimeMillis();
		System.out.println("word segmentation and partition the posts: " + (end - begin) / 1000.0 + " s\n");
		
		
		{
			System.out.println(K + "-cross validation of NBD: ");
			begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidate(i, ClassifierType.NBD);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
		{
			System.out.println(K + "-cross validation of NBCD: ");
			begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidate(i, ClassifierType.NBCD);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
		{
			System.out.println(K + "-cross validation of NBCG: ");
			begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidate(i, ClassifierType.NBCG);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
	}
}
