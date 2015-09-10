package com.lqw.ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import com.lqw.Main;
import com.lqw.data.Data;
import com.lqw.rf.Pivot;

public class Node {

	public static final int maxDepth = Integer.MAX_VALUE;
	//public static final double split_percentage = 0.02;
	
	public ArrayList<Data> data;
	
	public Pivot pivot;
	public Node left;
	public Node right;
	
	public int depth;
	
	public String name;    //the type of this node
	
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
		
		//System.out.println(depth + ": " + leftData.size() + ", " + rightData.size());
		
		left = new Node(leftData, depth + 1);
		right = new Node(rightData, depth + 1);
		
		data = null;
	}
	
	public Pivot findBestPivot() {
		Pivot best = new Pivot(-1, 0.0, Double.MAX_VALUE);
		
		//the original algorithm of adaboost
		/*
		for (int idx = 0; idx < data.get(0).feature.length; ++idx) {
			Pivot p = findBestPivot(idx);
			if (p != null && p.giniGain < best.giniGain)
				best = p;
		}
		*/
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
		
		double minGiniGain = Double.MAX_VALUE;
		double minSv = 0.0;
		for (int d = 0; d < domain.length - 1; ++d) {
			double sv = ((Double)domain[d] + (Double)domain[d + 1]) / 2;
			
			int cntLeft = 0;
			int cntRight = 0;
			HashMap<String, Integer> cntTypeLeft = new HashMap<String, Integer>();
			HashMap<String, Integer> cntTypeRight = new HashMap<String, Integer>();
			
			for (int i = 0; i < data.size(); ++i) {
				String type = data.get(i).type;
				if (data.get(i).feature[idx] < sv) {
					cntLeft++;
					if (cntTypeLeft.containsKey(type)) {
						cntTypeLeft.put(type, cntTypeLeft.get(type) + 1);
					} else {
						cntTypeLeft.put(type, 1);
					}
				} else {
					cntRight++;
					if (cntTypeRight.containsKey(type)) {
						cntTypeRight.put(type, cntTypeRight.get(type) + 1);
					} else {
						cntTypeRight.put(type, 1);
					}
				}
			}
			cntTypeLeft.put("all", cntLeft);
			cntTypeRight.put("all", cntRight);
			
			if (cntLeft == 0 || cntRight == 0) continue;
			//if (cntLeft < data.size() * split_percentage || cntRight < data.size() * split_percentage) continue;
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
		}
		
		return new Pivot(idx, minSv, minGiniGain);
	}
	
	public boolean isLeaf() {
		HashMap<String, Integer> cntType = new HashMap<String, Integer>();
		for (Data da : data) {
			if (cntType.containsKey(da.type)) {
				cntType.put(da.type, cntType.get(da.type) + 1);
			} else {
				cntType.put(da.type, 1);
			}
		}
		
		int maxCnt = Integer.MIN_VALUE;
		for (String type : cntType.keySet())
			if (cntType.get(type) > maxCnt) {
				name = type;
				maxCnt = cntType.get(type);
			}
		
		if (depth < maxDepth && cntType.size() > 1)
			return false;
		return true;
	}
	
	public static double calGini(HashMap<String, Integer> cntType) {
		double gini = 1.0;
		
		int all = 0;
		all += cntType.get("all");
		for (String vt : cntType.keySet()) {
			if (vt.equals("all"))
				continue;
			
			double f = cntType.get(vt) / (double)all; 
			gini -= f * f;
		}
		
		return gini;
	}
}
