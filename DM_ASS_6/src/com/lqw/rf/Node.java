package com.lqw.rf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import com.lqw.Main;
import com.lqw.data.Data;

public class Node {

	public Random rand;
	
	public ArrayList<Data> data;
	
	public int m;
	public HashSet<Integer> mFeature;
	
	public Pivot pivot;
	public Node left;
	public Node right;
	
	public String name;    //the type of this node
	
	public Node(ArrayList<Data> data, int m) {
		this.data = data;
		this.m = m;
		
		rand = Main.rand;
		mFeature = new HashSet<Integer>();
		while (mFeature.size() < m) {
			mFeature.add(rand.nextInt(data.get(0).feature.length));
		}
		
		if (data.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
	}
	
	public void split() {
		if (isLeaf()) {
			data = null;
			return;
		}
		
		pivot = findBestPivot();
		
		if (pivot == null || pivot.idx == -1) {
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
		
		//System.out.println(pivot.value + ": " + leftData.size() + ", " + rightData.size());
		if (leftData.size() == 0 || rightData.size() == 0) {
			System.err.println("zero size of data");
			System.err.println(pivot.value + ": " + leftData.size() + ", " + rightData.size());
		}
		
		left = new Node(leftData, m);
		right = new Node(rightData, m);
		
		data = null;
	}
	
	public Pivot findBestPivot() {
		Pivot best = new Pivot(-1, 0.0, Double.MAX_VALUE);
		
		for (int idx : mFeature) {
			Pivot p = findBestPivot(idx);
			if (p != null && p.giniGain < best.giniGain)
				best = p;
		}
		
		return best;
	}
	
	public Pivot findBestPivot(int idx) {
		
		/*
		int domainCnt = 0;
		double[] rawDomain = new double[data.size()];
		for (int i = 0; i < data.size(); ++i) {
			if (data.get(i).feature[idx] != 0.0) {
				rawDomain[domainCnt++] = data.get(i).feature[idx];
			}
		}
		double[] domain = new double[domainCnt + 1];
		domain[0] = 0.0;
		for (int i = 0; i < domainCnt; ++i)
			domain[i + 1] = rawDomain[i];
		Arrays.sort(domain);
		//for (int i = 0; i <= domain.length; ++i)
		//	System.out.print(domain[i] + ", ");
		//System.out.println();
		*/
		
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
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
		}
		
		/*
		double[] domain = new double[data.size()];
		for (int i = 0; i < data.size(); ++i) {
			domain[i] = data.get(i).feature[idx];
		}
		Arrays.sort(domain);
		//for (int i = 0; i <= domain.length; ++i)
		//	System.out.print(domain[i] + ", ");
		//System.out.println();
		
		double minGiniGain = Double.MAX_VALUE;
		double minSv = 0.0;
		for (int d = 0; d < domain.length - 1; ++d) {
			if (domain[d] == 0.0 && domain[d + 1] == 0.0) continue;
			//if (domain.get(d) == 0.0) System.err.println("wrong");
			
			//double sv = ((Double)domain[d] + (Double)domain[d + 1]) / 2;
			double sv = (domain[d] + domain[d + 1]) / 2;
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
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
		}
		*/
		
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
		
		if (cntType.size() > 1)
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
