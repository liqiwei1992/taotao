package com.lqw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;

import com.lqw.data.CData;
import com.lqw.data.Post;
import com.lqw.data.Forum;
import com.lqw.data.RData;
import com.lqw.dt.ClassificationDT;
import com.lqw.dt.LilyDT;
import com.lqw.dt.RegressionDT;
import com.lqw.tool.MyMath;
import com.lqw.tool.ReadCData;
import com.lqw.tool.ReadRData;
import com.lqw.tool.WordSegmenter;

public class Main {

	public static String pathLily = "E:\\Data Mining\\data\\lily\\";
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
	
	public static String pathBreastCancer = "E:\\Data Mining\\data\\classification\\breast-cancer.data";
	public static String pathSegment = "E:\\Data Mining\\data\\classification\\segment.data";
	public static String pathHousing = "E:\\Data Mining\\data\\regression\\housing.data";
	public static String pathMeta = "E:\\Data Mining\\data\\regression\\meta.data";
	
	public static final int K = 10;
	public static final Random rand = new Random(System.currentTimeMillis());
	//public static final Random rand = new Random(100);
	
	//record the all forums of lily for K Crossing Validation
	public static ArrayList<HashMap<String, Forum>> KForums = new ArrayList<HashMap<String, Forum>>(K);
	
	//record the all data of breast-cancer, segment, housing and meta for KCV
	public static ArrayList<ArrayList<CData>> KCData;
	public static ArrayList<ArrayList<RData>> KRData;
	
	
	//classification
	//for lily
	public static void processLily() throws Exception {
		
		for (int i = 0; i < K; ++i)
			KForums.add(new HashMap<String, Forum>());
		
		File file = new File(pathLily);
		if (!file.isDirectory()) {
			System.err.println(pathLily + " should be a directory");
			System.exit(-1);
		}
		
		for (String docName : file.list()) {
			if (docName.matches("[a-zA-Z_]+\\.txt")) {
				LinkedList<Post> posts = WordSegmenter.segmenteFile(pathLily, docName);
				partitionPosts(docName, posts);
			}
		}
		
		crossValidatePost(0);
		/*
		{
			System.out.println(K + "-cross validation of DT for Lily: ");
			long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidatePost(i);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			long end = System.currentTimeMillis();
			System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		*/
	}
	
	public static void partitionPosts(String forumName, LinkedList<Post> posts) {
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
	
	public static double crossValidatePost(int idxValidation) {
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
		
		LilyDT ldt = new LilyDT(trainForums);
		ldt.build();
		
		return ldt.predicate(validationPosts);
	}
	
	
	//for breast-cancer and segment
	public static void processBreastCancer() {
		KCData = new ArrayList<ArrayList<CData>>(K);
		
		LinkedList<CData> cdata = ReadCData.readCData(pathBreastCancer);
		partitionCData(cdata);
		
		
		{
			System.out.println(K + "-cross validation of DT for Classification of breast-cancer: ");
			//long begin = System.currentTimeMillis();
			
			double[] precision = new double[K];
			for (int i = 0; i < K; ++i)
				precision[i] = crossValidateCData(i);
			
			for (double p : precision)
				System.out.println(p);
			System.out.println("average: " + MyMath.average(precision));
			System.out.println("variance: " + MyMath.standardVariance(precision));
			
			//long end = System.currentTimeMillis();
			//System.out.println("time: " + (end - begin) / 1000.0 + " s\n");
		}
		
	}
	
	public static void processSegment() {
		KCData = new ArrayList<ArrayList<CData>>(K);
		
		LinkedList<CData> cdata = ReadCData.readCData(pathSegment);
		partitionCData(cdata);
		
		
		crossValidateCData(0);
	}
	
	public static void partitionCData(LinkedList<CData> cdata) {
		
		//System.out.println(cdata.size());
		
		for (int i = 0; i < K; ++i)
			KCData.add(new ArrayList<CData>());
		
		int cnt = cdata.size();
		int index = 0;
		while (cnt != 0) {
			int r = rand.nextInt(cnt);
			CData d = cdata.remove(r);
			
			KCData.get(index).add(d);
			
			cnt--;
			index = (index + 1) % K;
		}
		
		/*
		cnt = 0;
		for (int i = 0; i < K; ++i) {
			for (int j = 0; j < KCData.get(i).size(); ++j) {
				System.out.println(KCData.get(i).get(j));
				
				cnt++;
			}
		}
		System.out.println(cnt);
		*/
	}
	
	public static double crossValidateCData(int idxValidation) {
		ArrayList<CData> trainCData = new ArrayList<CData>();
		
		for (int i = 0; i < K; ++i) {
			if (i == idxValidation)
				continue;
			
			trainCData.addAll(KCData.get(i));
		}
		
		ArrayList<CData> testCData = KCData.get(idxValidation);
		
		/*
		for (int i = 0; i < trainCData.size(); ++i) {
			CData cd = trainCData.get(i);
			System.out.println(i + ", " + cd.fDisc[3]);
		}
		
		System.out.println("==================");
		
		for (int i = 0; i < testCData.size(); ++i) {
			CData cd = testCData.get(i);
			System.out.println(i + ", " + cd.fDisc[3]);
		}
		*/
		
		ClassificationDT cdt = new ClassificationDT(trainCData);
		cdt.build();
		
		return cdt.predicate(testCData);
	}
	
	
	
	//regression
	//for housing and meta
	public static void processHousing() {
		KRData = new ArrayList<ArrayList<RData>>(K);
		
		LinkedList<RData> rdata = ReadRData.readRData(pathHousing);
		partitionRData(rdata);
		
		
		crossValidateRData(0);
	}
	
	public static void processMeta() {
		KRData = new ArrayList<ArrayList<RData>>(K);
		
		LinkedList<RData> rdata = ReadRData.readRData(pathMeta);
		partitionRData(rdata);
		
		
		crossValidateRData(0);
	}
	
	public static void partitionRData(LinkedList<RData> rdata) {
		
		System.out.println(rdata.size());
		
		for (int i = 0; i < K; ++i)
			KRData.add(new ArrayList<RData>());
		
		int cnt = rdata.size();
		int index = 0;
		while (cnt != 0) {
			int r = rand.nextInt(cnt);
			RData d = rdata.remove(r);
			
			KRData.get(index).add(d);
			
			cnt--;
			index = (index + 1) % K;
		}
		
		
		cnt = 0;
		for (int i = 0; i < K; ++i) {
			for (int j = 0; j < KRData.get(i).size(); ++j) {
				System.out.println(KRData.get(i).get(j));
				
				cnt++;
			}
		}
		System.out.println(cnt);
		
	}
	
	public static double crossValidateRData(int idxValidation) {
		ArrayList<RData> trainRData = new ArrayList<RData>();
		
		for (int i = 0; i < K; ++i) {
			if (i == idxValidation)
				continue;
			
			trainRData.addAll(KRData.get(i));
		}
		
		ArrayList<RData> testRData = KRData.get(idxValidation);
		
		RegressionDT rdt = new RegressionDT(trainRData);
		rdt.fit();
		
		return rdt.predicate(testRData);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		//processLily();
		
		processBreastCancer();
		
		//processSegment();
		
		//processHousing();
		
		//processMeta();
		
	}
}
