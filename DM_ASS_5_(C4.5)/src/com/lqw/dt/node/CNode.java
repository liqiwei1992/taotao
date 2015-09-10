package com.lqw.dt.node;

import java.util.ArrayList;
import java.util.HashMap;

import com.lqw.data.CData;

public class CNode {

	public ArrayList<CData> cdata;
	public boolean[] fDiscLabels;    //label the discrete attribute that is already used
	
	public String name;
	
	public SplitAttrInfo sai;
	public HashMap<String, CNode> children = new HashMap<String, CNode>();
	
	public double INFO;
	
	
	public CNode(ArrayList<CData> cdata, boolean[] fDiscLabels) {
		this.cdata = cdata;
		this.fDiscLabels = fDiscLabels;
		
		HashMap<String, Integer> cntType = new HashMap<String, Integer>();
		for (CData cd : cdata)
			if (cntType.containsKey(cd.type)) {
				cntType.put(cd.type, cntType.get(cd.type) + 1);
			} else {
				cntType.put(cd.type, 1);
			}
		INFO = calInfo(cntType);
			
	}
	
	
	public void split() {
		
		if (cdata.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
		
		if (isLeaf()) {
			
			return;
		}
		
		//System.out.println("\n===========\n");
		
		
		//HashMap<String, Integer> cntType = new HashMap<String, Integer>();
		//for (CData cd : cdata)
		//	if (cntType.containsKey(cd.type)) {
		//		cntType.put(cd.type, cntType.get(cd.type) + 1);
		//	} else {
		//		cntType.put(cd.type, 1);
		//	}
		//for (String v : cntType.keySet())
		//	System.out.println(v + ", " + cntType.get(v));
		
		
		sai = getBestSplitAttrInfo();
		
		if (sai.idx == -1) { //means that there is no split attribute
			setName();
			sai = null;
			return;
		}
		
		
		if (sai.DorC == true) {
			//process the split at the discrete attribute
			HashMap<String, ArrayList<CData>> splitCData = new HashMap<String, ArrayList<CData>>();
			for (int i = 0; i < cdata.size(); ++i) {
				//System.out.println(cdata.get(i));
				//System.out.println(sai.idx);
				//System.out.println(cdata.get(i).fDisc[sai.idx]);
				
				if (!splitCData.containsKey(cdata.get(i).fDisc[sai.idx])) {
					splitCData.put(cdata.get(i).fDisc[sai.idx], new ArrayList<CData>());
				}
				splitCData.get(cdata.get(i).fDisc[sai.idx]).add(cdata.get(i));
			}
			
			for (String attrV : splitCData.keySet()) {
				boolean[] labels = new boolean[fDiscLabels.length];
				for (int l = 0; l < labels.length; ++l)
					labels[l] = fDiscLabels[l];
				labels[sai.idx] = true;
				
				children.put(attrV, new CNode(splitCData.get(attrV), labels));
			}
		} else {
			//process the split at the continuous attribute
			
		}
		
	}
	
	public SplitAttrInfo getBestSplitAttrInfo() {
		
		SplitAttrInfo saiDisc = getBestDiscSplitAttrInfo();
		SplitAttrInfo saiCond = getBestCondSplitAttrInfo();
		
		if (saiDisc == null && saiCond == null) {
			System.err.println("wrong, no available attribute");
			System.exit(-1);
		} else if (saiDisc == null) {
			return saiCond;
		} else if (saiCond == null) {
			return saiDisc;
		} else {
			if (saiDisc.gainRatio < saiCond.gainRatio)
				return saiCond;
			else
				return saiDisc;
		}
		return null;
	}
	
	public SplitAttrInfo getBestDiscSplitAttrInfo() {
		SplitAttrInfo bestSai = new SplitAttrInfo(true, -1, Double.NEGATIVE_INFINITY);
		for (int i = 0; i < cdata.get(0).fDisc.length; ++i) {
			//System.out.println(i);
			
			if (fDiscLabels[i] == true)
				continue;
			
			SplitAttrInfo sai = calBestDiscSplitInfo(i);
			
			//System.out.println(sai.gainRatio);
			
			if (sai.gainRatio > bestSai.gainRatio)
				bestSai = sai;
		}
		
		return bestSai;
	}
	
	public SplitAttrInfo calBestDiscSplitInfo(int idx) {
		HashMap<String, HashMap<String, Integer>> cntAllType = new HashMap<String, HashMap<String, Integer>>();
		for (int i = 0; i < cdata.size(); ++i) {
			if (!cntAllType.containsKey(cdata.get(i).fDisc[idx])) {
				cntAllType.put(cdata.get(i).fDisc[idx], new HashMap<String, Integer>());
			}
			//count each type in each split class
			if (cntAllType.get(cdata.get(i).fDisc[idx]).containsKey(cdata.get(i).type)) {
				cntAllType.get(cdata.get(i).fDisc[idx]).put(cdata.get(i).type, cntAllType.get(cdata.get(i).fDisc[idx]).get(cdata.get(i).type) + 1);
			} else {
				cntAllType.get(cdata.get(i).fDisc[idx]).put(cdata.get(i).type, 1);
			}
			//count all in each split class
			if (cntAllType.get(cdata.get(i).fDisc[idx]).containsKey("all")) {
				cntAllType.get(cdata.get(i).fDisc[idx]).put("all", cntAllType.get(cdata.get(i).fDisc[idx]).get("all") + 1);
			} else {
				cntAllType.get(cdata.get(i).fDisc[idx]).put("all", 1);
			}
		}
		
		double infoA = 0.0;
		double splitInfo = 0.0;
		
		//for (String vt : cntAllType.keySet())
		//	System.out.println(vt);
		//for (int i = 0; i < cdata.size(); ++i)
		//	System.out.println(cdata.get(i));
		
		
		for (String attrV : cntAllType.keySet()) {
			double f = cntAllType.get(attrV).get("all") / (double)cdata.size();
			splitInfo += (0.0 - f * Math.log(f) / Math.log(2));
			
			
			
			infoA += calInfo(cntAllType.get(attrV));
			
			//for (String vt : cntAllType.get(attrV).keySet())
			//	System.out.println(vt + ", " + cntAllType.get(attrV).get(vt));
		}
		
		//System.out.println(INFO + ", " + infoA + ", " + splitInfo);
		
		double gainRatio = (INFO - infoA) / splitInfo;
		
		return new SplitAttrInfo(true, idx, gainRatio);
	}
	
	public SplitAttrInfo getBestCondSplitAttrInfo() {
		
		
		return null;
	}
	
	public void setName() {
		HashMap<String, Integer> cntType = new HashMap<String, Integer>();
		
		for (CData cd : cdata)
			if (cntType.containsKey(cd.type)) {
				cntType.put(cd.type, cntType.get(cd.type) + 1);
			} else {
				cntType.put(cd.type, 1);
			}
		
		int maxCnt = Integer.MIN_VALUE;
		for (String type : cntType.keySet())
			if (cntType.get(type) > maxCnt) {
				name = type;
				maxCnt = cntType.get(type);
			}
	}
	
	
	public boolean isLeaf() {
		HashMap<String, Integer> cntType = new HashMap<String, Integer>();
		
		for (CData cd : cdata)
			if (cntType.containsKey(cd.type)) {
				cntType.put(cd.type, cntType.get(cd.type) + 1);
			} else {
				cntType.put(cd.type, 1);
			}
		
		String tmpName = null;
		int maxCnt = Integer.MIN_VALUE;
		for (String type : cntType.keySet())
			if (cntType.get(type) > maxCnt) {
				tmpName = type;
				maxCnt = cntType.get(type);
			}
		
		boolean flag = true;
		for (int l = 0; l < fDiscLabels.length; ++l)
			if (fDiscLabels[l] == false) {
				flag = false;
				break;
			}
		if (flag == true) {
			name = tmpName;
			return true;
		}
		
		if (cntType.size() > 1)
			return false;
		
		name = tmpName;
		return true;
	}
	
	public static double calInfo(HashMap<String, Integer> cntType) {
		double info = 0.0;
		
		int all = 0;
		for (String t : cntType.keySet()) {
			all += cntType.get(t);
		}
		for (String t : cntType.keySet()) {
			double f = cntType.get(t) / (double)all;
			info += (0.0 - f * Math.log(f) / Math.log(2)); 
		}
		return info;
	}
}
