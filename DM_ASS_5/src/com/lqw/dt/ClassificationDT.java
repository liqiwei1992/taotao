package com.lqw.dt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import com.lqw.data.CData;
import com.lqw.data.Data;
import com.lqw.dt.node.CNode;
import com.lqw.dt.node.CDomain;

public class ClassificationDT {

	public ArrayList<CData> trainCData;
	
	public String[] defaultValueDisc;
	public double[] defaultValueCont;
	
	public CNode root;
	
	//public HashSet<String> allType = new HashSet<String>();
	
	
	
	public ClassificationDT(ArrayList<CData> trainCData) {
		
		defaultValueDisc = Data.preprocessDisc(trainCData);
		defaultValueCont = Data.preprocessCont(trainCData);
		
		this.trainCData = trainCData;
		
		//for (CData cd : trainCData) {
		//	allType.add(cd.type);
		//}
	}
	
	public void build(int maxDepth, int minNode) {
		CNode.maxDepth = maxDepth;
		CNode.minNode = minNode;
		
		build();
	}
	
	public void build() {
		
		ArrayList<ArrayList<CDomain>> contDomain = new ArrayList<ArrayList<CDomain>>();
		for (int d = 0; d < trainCData.get(0).fCont.length; ++d) {
			contDomain.add(new ArrayList<CDomain>());
			for (int i = 0; i < trainCData.size(); ++i) {
				contDomain.get(d).add(new CDomain(trainCData.get(i).fCont[d], trainCData.get(i)));
			}
			Collections.sort(contDomain.get(d));
			
			//for (int i = 0; i < contDomain.get(d).size(); ++i)
			//	System.out.print(contDomain.get(d).get(i).value + ", ");
			//System.out.println();
		}
		
		root = new CNode(trainCData, contDomain, 1);
		
		LinkedList<CNode> nodes = new LinkedList<CNode>();
		nodes.add(root);
		while (!nodes.isEmpty()) {
			CNode node = nodes.remove();
			node.split();
			
			if (node.leftNode != null && node.rightNode != null) {
				nodes.add(node.leftNode);
				nodes.add(node.rightNode);
			}
		}
		
	}
	
	public String predicate(CData cd) {
		CNode node = root;
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
		
		return node.name;
	}
	
	public void processTestRData(ArrayList<CData> testCData) {
		for (int i = 0; i < testCData.get(0).fDisc.length; ++i) {
			for (int j = 0; j < testCData.size(); ++j) {
				if (testCData.get(j).fDisc[i].equals("NaN"))
					testCData.get(j).fDisc[i] = defaultValueDisc[i];
			}
		}
		
		for (int i = 0; i < testCData.get(0).fCont.length; ++i) {
			for (int j = 0; j < testCData.size(); ++j) {
				if (Double.isNaN(testCData.get(j).fCont[i])) {
					testCData.get(j).fCont[i] = defaultValueCont[i];
				}
			}
		}
	}
	
	public double predicate(ArrayList<CData> testCData) {
		processTestRData(testCData);
		
		int rightCnt = 0;
		
		for (int i = 0; i < testCData.size(); ++i) {
			if (testCData.get(i).type.equals(predicate(testCData.get(i))))
				rightCnt++;
		}
		
		//System.out.println(rightCnt + " / " + testCData.size() + " = " + rightCnt / (double)testCData.size());
		
		return rightCnt / (double)testCData.size();
	}
}
