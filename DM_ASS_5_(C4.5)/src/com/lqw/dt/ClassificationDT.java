package com.lqw.dt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.lqw.data.CData;
import com.lqw.data.Data;
import com.lqw.dt.node.CNode;

public class ClassificationDT {

	public ArrayList<CData> trainCData;
	
	public String[] defaultValueDisc;
	public double[] defaultValueCont;
	
	public CNode root;
	
	public HashSet<String> allType = new HashSet<String>();
	
	
	
	public ClassificationDT(ArrayList<CData> trainCData) {
		
		defaultValueDisc = Data.preprocessDisc(trainCData);
		defaultValueCont = Data.preprocessCont(trainCData);
		
		this.trainCData = trainCData;
		
		for (CData cd : trainCData) {
			allType.add(cd.type);
		}
	}
	
	public void build() {
		boolean[] fDiscLabels = new boolean[trainCData.get(0).fDisc.length];
		for (int l = 0; l < fDiscLabels.length; ++l)
			fDiscLabels[l] = false;
		
		root = new CNode(trainCData, fDiscLabels);
		
		LinkedList<CNode> nodes = new LinkedList<CNode>();
		nodes.add(root);
		while (!nodes.isEmpty()) {
			CNode node = nodes.remove();
			node.split();
			for (String attrV : node.children.keySet()) {
				nodes.add(node.children.get(attrV));
			}
		}
		
	}
	
	public String predicate(CData cd) {
		CNode node = root;
		while (node.children.size() != 0) {
			//System.out.println(node + ", " + node.children.size());
			
			if (node.sai.DorC == true) {
				boolean flag = false;
				
				for (String attrV : node.children.keySet()) {
					//System.out.println(node.sai.idx + ", " + cd.fDisc[node.sai.idx] + ", " + attrV);
					if (cd.fDisc[node.sai.idx].equals(attrV)) {
						node = node.children.get(attrV);
						
						flag = true;
						break;
					}
				}
				
				if (flag == false) {
					String maxAttrV = null;
					int maxCnt = Integer.MIN_VALUE;
					
					for (String attrV : node.children.keySet()) {
						if (node.children.get(attrV).cdata.size() > maxCnt) {
							maxAttrV = attrV;
							maxCnt = node.children.get(attrV).cdata.size();
						}
					}
					node = node.children.get(maxAttrV);
				}
				
			} else {
				System.out.println("-_-#");
			}
		}
		
		
		return node.name;
	}
	
	public double predicate(ArrayList<CData> testCData) {
		//System.out.println(">_<");
		
		int rightCnt = 0;
		
		for (int i = 0; i < testCData.size(); ++i) {
			//System.out.println(i);
			if (testCData.get(i).type.equals(predicate(testCData.get(i))))
				rightCnt++;
			//System.out.println("rightCnt " + rightCnt);
		}
		
		System.out.println(rightCnt + " / " + testCData.size() + " = " + rightCnt / (double)testCData.size());
		
		return rightCnt / (double)testCData.size();
	}
}
