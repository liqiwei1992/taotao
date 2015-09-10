package com.lqw.gbdt_v2;

import java.util.ArrayList;
import java.util.TreeSet;

import com.lqw.data.Data;
import com.lqw.rf.Pivot;
import com.lqw.tool.MyMath;

public class Node {

	public static final int maxDepth = Integer.MAX_VALUE;
	public static final double leafMinVar = 0.01;
	
	public ArrayList<Data> data;
	
	public Pivot pivot;
	public Node left;
	public Node right;
	
	public int depth;
	
	public double value;
	//public double gamma;
	
	public Node(ArrayList<Data> data, int depth) {
		this.data = data;
		this.depth = depth;
		
		if (data.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
	}
	
	public void split() {
		//it is a leaf
		if (isLeaf()) {
			data = null;
			return;
		}
		
		pivot = findBestPivot();
		
		//it is a leaf
		if (pivot.idx == -1) {
			data = null;
			return;
		}
		
		ArrayList<Data> leftData = new ArrayList<Data>();
		ArrayList<Data> rightData = new ArrayList<Data>();
		
		for (int i = 0; i < data.size(); ++i) {
			if (data.get(i).feature[pivot.idx] < pivot.value) {
				leftData.add(data.get(i));
			} else {
				rightData.add(data.get(i));
			}
		}
		
		if (leftData.size() == 0 || rightData.size() == 0) {
			System.err.println("zero size of data");
			System.err.println(pivot.value + ": " + leftData.size() + ", " + rightData.size());
		}
		
		//System.out.println(data.size() + ": " + leftData.size() + ", " + rightData.size());
		
		left = new Node(leftData, depth + 1);
		right = new Node(rightData, depth + 1);
		
		data = null;
	}
	
	public Pivot findBestPivot() {
		Pivot best = new Pivot(-1, 0.0, Double.MAX_VALUE);
		
		//the original algorithm of adaboost
		
		for (int idx = 0; idx < data.get(0).feature.length; ++idx) {
			Pivot p = findBestPivot(idx);
			if (p != null && p.giniGain < best.giniGain)
				best = p;
		}
		/*
		//the method that random forest uses to select the feature
		Random rand = Main.rand;
		HashSet<Integer> mFeature = new HashSet<Integer>();
		int m = (int)Math.sqrt(rand.nextInt(data.get(0).feature.length));
		while (mFeature.size() < m) {
			mFeature.add(rand.nextInt(data.get(0).feature.length));
		}
		for (int idx : mFeature) {
			Pivot p = findBestPivot(idx);
			if (p != null && p.giniGain < best.giniGain)
				best = p;
		}
		*/
		return best;
	}
	
	public Pivot findBestPivot(int idx) {
		TreeSet<Double> domainSet = new TreeSet<Double>();
		domainSet.add(0.0);
		for (int i = 0; i < data.size(); ++i) {
			if (data.get(i).feature[idx] != 0.0) {
				domainSet.add(data.get(i).feature[idx]);
			}
		}
		Object[] domain = domainSet.toArray();
		
		double minVar = Double.MAX_VALUE;
		double minSv = 0.0;
		for (int d = 0; d < domain.length - 1; ++d) {
			double sv = ((Double)domain[d] + (Double)domain[d + 1]) / 2;
			
			ArrayList<Double> valueLeft = new ArrayList<Double>();
			ArrayList<Double> valueRight = new ArrayList<Double>();
			for (int i = 0; i < data.size(); ++i) {
				if (data.get(i).feature[idx] < sv)
					valueLeft.add(data.get(i).value);
				else
					valueRight.add(data.get(i).value);
			}
			
			if (valueLeft.size() == 0 || valueRight.size() == 0) continue;
			//if (cntLeft.size() < data.size() * split_percentage || cntRight.size() < data.size() * split_percentage) continue;
			
			double varLeft = MyMath.variance(valueLeft);
			double varRight = MyMath.variance(valueRight);
			
			double var = (varLeft * valueLeft.size() + varRight * valueRight.size()) / (double)(valueLeft.size() + valueRight.size());
			//double var = varLeft * valueLeft.size() + varRight * valueRight.size();
			if (var < minVar) {
				minVar = var;
				minSv = sv;
			}
		}
		
		return new Pivot(idx, minSv, minVar);
	}
	
	public boolean isLeaf() {
		double[] values = new double[data.size()];
		//double sum1 = 0.0;
		//double sum2 = 0.0;
		for (int i = 0; i < data.size(); ++i) {
			values[i] = data.get(i).value;
			
			//sum1 += data.get(i).value;
			//sum2 += data.get(i).value * (1.0 - data.get(i).value);
		}
		value = MyMath.average(values);
		
		//if (Double.isNaN(sum1 / sum2))
		//	System.out.println(sum1 + ", " + sum2);
		
		//gamma = MyMath.average(values);// = sum1 / sum2;
		
		double var = MyMath.variance(values);
		
		if (depth < maxDepth && var > leafMinVar)
			return false;
		return true;
	}
}
