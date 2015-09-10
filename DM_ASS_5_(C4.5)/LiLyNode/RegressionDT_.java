package com.lqw.dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.lqw.data.Data;
import com.lqw.data.RData;
import com.lqw.dt.node.RNode_;

public class RegressionDT_ {

public ArrayList<RData> trainRData;
	
	public String[] defaultValueDisc;
	public double[] defaultValueCont;
	
	public RNode_ root;
	
	public RegressionDT_(ArrayList<RData> trainRData) {
		
		defaultValueDisc = Data.preprocessDisc(trainRData);
		defaultValueCont = Data.preprocessCont(trainRData);
		
		this.trainRData = trainRData;
		
	}
	
	public void build() {
		ArrayList<Double[]> domains = new ArrayList<Double[]>();
		for (int f = 0; f < trainRData.get(0).fCont.length; ++f) {
			Double[] domain = new Double[trainRData.size()];
			for (int i = 0; i < trainRData.size(); ++i) {
				domain[i] = trainRData.get(i).fCont[f];
			}
			Arrays.sort(domain);
			domains.add(domain);
		}
		
		root = new RNode_(trainRData, domains);
		LinkedList<RNode_> nodes = new LinkedList<RNode_>();
		nodes.add(root);
		while (!nodes.isEmpty()) {
			RNode_ node = nodes.remove();
			node.split();
			
			if (node.leftNode != null && node.rightNode != null) {
				nodes.add(node.leftNode);
				nodes.add(node.rightNode);
			}
		}
	}
	
	public double predicate(RData cd) {
		RNode_ node = root;
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
}
