package com.lqw.sdt;

import java.util.ArrayList;

import com.lqw.data.CData;
import com.lqw.dt.ClassificationDT;

public class CSDT {

	public static final int K = 9;
	public static final int N = 3;
	public static final int[] maxDepths = {80, 100, 150};
	public static final int[] minNodes = {5, 8, 10};
	public static final int metaDepth = Integer.MAX_VALUE;
	public static final int metaNode = 0;
	
	public ArrayList<ArrayList<CData>> KCData;
	
	public ClassificationDT metaDt;
	public ClassificationDT[] dts = new ClassificationDT[N];
	
	public CSDT(ArrayList<ArrayList<CData>> KCData) {
		this.KCData = KCData;
		
		assert K == KCData.size() : "wrong KRData size. it should be 10";
	}
	
	public void sdt() {
		ArrayList<CData> M = new ArrayList<CData>();
		for (int i = 0; i < K; ++i) {
			ArrayList<CData> S = new ArrayList<CData>();
			for (int d = 0; d < KCData.get(i).size(); ++d)
				S.add(new CData(KCData.get(i).get(d).type, new String[N], new double[0]));
			
			for (int j = 0; j < N; ++j) {
				String[] predTypes = crossValidateCData(i, maxDepths[j], minNodes[j]);
				
				for (int d = 0; d < KCData.get(i).size(); ++d) {
					S.get(d).fDisc[j] = predTypes[d];
				}
			}
			
			M.addAll(S);
		}
		
		metaDt = new ClassificationDT(M);
		metaDt.build(metaDepth, metaNode);
		
		ArrayList<CData> trainCData = new ArrayList<CData>();
		for (int i = 0; i < K; ++i) {
			trainCData.addAll(KCData.get(i));
		}
		for (int j = 0; j < N; ++j) {
			dts[j] = new ClassificationDT(trainCData);
			dts[j].build(maxDepths[j], minNodes[j]);
		}
	}
	
	public String[] crossValidateCData(int idx, int maxDepth, int minNode) {
		ArrayList<CData> trainCData = new ArrayList<CData>();
		
		for (int i = 0; i < K; ++i) {
			if (i == idx)
				continue;
			
			trainCData.addAll(KCData.get(i));
		}
		
		ArrayList<CData> testCData = KCData.get(idx);
		
		ClassificationDT cdt = new ClassificationDT(trainCData);
		cdt.build(maxDepth, minNode);
		
		String[] predTypes = new String[testCData.size()];
		for (int i = 0; i < testCData.size(); ++i)
			predTypes[i] = cdt.predicate(testCData.get(i));
		
		return predTypes;
	}
	
	public double predicate(ArrayList<CData> testCData) {
		ArrayList<CData> S = new ArrayList<CData>();
		for (int d = 0; d < testCData.size(); ++d) {
			S.add(new CData(testCData.get(d).type, new String[N], new double[0]));
			for (int j = 0; j < N; ++j) {
				S.get(d).fDisc[j] = dts[j].predicate(testCData.get(d));
			}
		}
		
		
		//for (int d = 0; d < S.size(); ++d)
		//	System.out.println(S.get(d));
		
		
		int rightCnt = 0;
		for (int i = 0; i < S.size(); ++i) {
			//String s = metaDt.predicate(S.get(i));
			//System.out.println(S.get(i).type + " vs " + s);
			if (S.get(i).type.equals(metaDt.predicate(S.get(i))))
					rightCnt++;
		}
		
		//System.out.println(rightCnt / (double)testCData.size());
		
		return rightCnt / (double)testCData.size();
	}
}
