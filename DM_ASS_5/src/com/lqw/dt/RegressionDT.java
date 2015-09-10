package com.lqw.dt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import com.lqw.data.Data;
import com.lqw.data.RData;
import com.lqw.dt.node.RDomain;
import com.lqw.dt.node.RNode;

public class RegressionDT {

	public ArrayList<RData> trainRData;
	
	public String[] defaultValueDisc;
	public double[] defaultValueCont;
	
	public RNode root;
	
	public RegressionDT(ArrayList<RData> trainRData) {
		
		defaultValueDisc = Data.preprocessDisc(trainRData);
		defaultValueCont = Data.preprocessCont(trainRData);
		
		this.trainRData = trainRData;
		
	}
	
	
	public void build(int type) {
		
		RNode.threshold_depth = Integer.MAX_VALUE;
		RNode.threshold_var = 0.00001;
		
		if (type == 0)
			RNode.split_percentage = 0.1;
		else if (type == 1)
			RNode.split_percentage = 0.001;
		else
			System.err.println("wrong type");
		
		build2();
	}
	
	public void build(int maxDepth, double maxVar, double split_percentage) {
		RNode.threshold_depth = maxDepth;
		RNode.threshold_var = maxVar;
		RNode.split_percentage = split_percentage;
		
		build2();
	}
	
	public void build2() {
		ArrayList<ArrayList<RDomain>> contDomain = new ArrayList<ArrayList<RDomain>>();
		for (int d = 0; d < trainRData.get(0).fCont.length; ++d) {
			contDomain.add(new ArrayList<RDomain>());
			for (int i = 0; i < trainRData.size(); ++i) {
				contDomain.get(d).add(new RDomain(trainRData.get(i).fCont[d], trainRData.get(i)));
			}
			Collections.sort(contDomain.get(d));
			
			//for (int i = 0; i < contDomain.get(d).size(); ++i)
			//	System.out.print(contDomain.get(d).get(i).value + ", ");
			//System.out.println();
		}
		
		root = new RNode(trainRData, contDomain, 1);
		
		LinkedList<RNode> nodes = new LinkedList<RNode>();
		nodes.add(root);
		while (!nodes.isEmpty()) {
			RNode node = nodes.remove();
			node.split();
			
			if (node.leftNode != null && node.rightNode != null) {
				nodes.add(node.leftNode);
				nodes.add(node.rightNode);
			}
		}
	}
	
	public double predicate(RData cd) {
		RNode node = root;
		while (node.leftNode != null) {
			assert node.rightNode != null : "wrong when build the tree";
			
			if (node.sai.DorC == true) {
				if (cd.fDisc[node.sai.idx].equals(node.sai.discV)) {
					node = node.leftNode;
				} else {
					node = node.rightNode;
				}
				
			} else {
				if (cd.fCont[node.sai.idx] < node.sai.contV) {
					node = node.leftNode;
				} else {
					node = node.rightNode;
				}
			}
		}
		//System.out.println(node.name);
		
		return node.value;
	}
	
	public void processTestRData(ArrayList<RData> testRData) {
		for (int i = 0; i < testRData.get(0).fDisc.length; ++i) {
			for (int j = 0; j < testRData.size(); ++j) {
				if (testRData.get(j).fDisc[i].equals("NaN"))
					testRData.get(j).fDisc[i] = defaultValueDisc[i];
			}
		}
		
		for (int i = 0; i < testRData.get(0).fCont.length; ++i) {
			for (int j = 0; j < testRData.size(); ++j) {
				if (Double.isNaN(testRData.get(j).fCont[i])) {
					testRData.get(j).fCont[i] = defaultValueCont[i];
				}
			}
		}
	}
	
	public double predicate(ArrayList<RData> testRData) {
		processTestRData(testRData);
		
		double[] values = new double[testRData.size()];
		double[] diff = new double[testRData.size()];
		
		for (int i = 0; i < testRData.size(); ++i) {
			values[i] = predicate(testRData.get(i));
		}
		
		//System.out.println(rightCnt + " / " + testCData.size() + " = " + rightCnt / (double)testCData.size());
		double rms = 0.0;
		for (int i = 0; i < testRData.size(); ++i) {
			diff[i] = testRData.get(i).value - values[i];
			rms += diff[i] * diff[i];
			
			//System.out.println(diff[i]);
		}
		
		//System.out.println(Math.sqrt(rms / testRData.size()));
		
		return Math.sqrt(rms / testRData.size());
	}
	
	public void print(RNode node, int depth) {
		if (node.leftNode != null) {
			print(node.leftNode, depth + 1);
			print(node.rightNode, depth + 1);
		} else {
			System.out.println(depth);
		}
		
	}
}
