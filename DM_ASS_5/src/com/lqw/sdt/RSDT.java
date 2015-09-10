package com.lqw.sdt;

import java.util.ArrayList;

import com.lqw.data.RData;
import com.lqw.dt.RegressionDT;

public class RSDT {

	public static final int K = 9;
	public static final int N = 3;
	public static final int[] maxDepths = {30, 50, 70};
	public static final double[] maxVars = {0.01, 0.001, 0.0001};
	public static final double[] split_percentage = {0.1, 0.01, 0.001};
	public static final int metaDepth = Integer.MAX_VALUE;
	public static final double metaVar = 0.00001;
	public static final double metaSplitPercentage = 0.01;
	
	public ArrayList<ArrayList<RData>> KRData;
	
	public RegressionDT metaDt;
	public RegressionDT[] dts = new RegressionDT[N];
	
	public RSDT(ArrayList<ArrayList<RData>> KRData) {
		this.KRData = KRData;
		
		assert K == KRData.size() : "wrong KRData size. it should be 10";
	}
	
	public void sdt() {
		ArrayList<RData> M = new ArrayList<RData>();
		for (int i = 0; i < K; ++i) {
			ArrayList<RData> S = new ArrayList<RData>();
			for (int d = 0; d < KRData.get(i).size(); ++d)
				S.add(new RData(KRData.get(i).get(d).value, new String[0], new double[N]));
			
			for (int j = 0; j < N; ++j) {
				double[] predValues = crossValidateRData(i, maxDepths[j], maxVars[j], split_percentage[j]);
				
				for (int d = 0; d < KRData.get(i).size(); ++d) {
					S.get(d).fCont[j] = predValues[d];
				}
			}
			
			M.addAll(S);
		}
		
		metaDt = new RegressionDT(M);
		metaDt.build(metaDepth, metaVar, metaSplitPercentage);
		
		ArrayList<RData> trainRData = new ArrayList<RData>();
		for (int i = 0; i < K; ++i) {
			trainRData.addAll(KRData.get(i));
		}
		for (int j = 0; j < N; ++j) {
			dts[j] = new RegressionDT(trainRData);
			dts[j].build(maxDepths[j], maxVars[j], split_percentage[j]);
		}
	}
	
	public double[] crossValidateRData(int idx, int maxDepth, double maxVar, double splitPercentage) {
		ArrayList<RData> trainRData = new ArrayList<RData>();
		
		for (int i = 0; i < K; ++i) {
			if (i == idx)
				continue;
			
			trainRData.addAll(KRData.get(i));
		}
		
		ArrayList<RData> testRData = KRData.get(idx);
		
		RegressionDT rdt = new RegressionDT(trainRData);
		rdt.build(maxDepth, maxVar, splitPercentage);
		
		double[] predValues = new double[testRData.size()];
		for (int i = 0; i < testRData.size(); ++i)
			predValues[i] = rdt.predicate(testRData.get(i));
		
		return predValues;
	}
	
	public double predicate(ArrayList<RData> testRData) {
		ArrayList<RData> S = new ArrayList<RData>();
		for (int d = 0; d < testRData.size(); ++d) {
			S.add(new RData(testRData.get(d).value, new String[0], new double[N]));
			for (int j = 0; j < N; ++j) {
				S.get(d).fCont[j] = dts[j].predicate(testRData.get(d));
			}
		}
		
		double[] values = new double[testRData.size()];
		
		for (int i = 0; i < S.size(); ++i) {
			values[i] = metaDt.predicate(S.get(i));
		}
		
		double rms = 0.0;
		for (int i = 0; i < S.size(); ++i) {
			double diff = S.get(i).value - values[i];
			rms += diff * diff;
		}
		
		//System.out.println(Math.sqrt(rms / testRData.size()));
		
		return Math.sqrt(rms / testRData.size());
	}
}
