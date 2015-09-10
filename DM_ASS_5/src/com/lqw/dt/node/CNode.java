package com.lqw.dt.node;

import java.util.ArrayList;
import java.util.HashMap;

import com.lqw.data.CData;

public class CNode {

	public static int maxDepth = Integer.MAX_VALUE;
	public static int minNode = 0;
	
	public ArrayList<CData> cdata;
	public ArrayList<ArrayList<CDomain>> contDomain;    //the domain (binding the CData) of each continuous feature
	
	public HashMap<String, Integer> cntTypeAll;
	
	public SplitAttrInfo sai;
	public CNode leftNode;    //the data whose attribute at sai.idx equals sai.value (if DorC is true) or is less than sai.value (if DorC is false) 
	public CNode rightNode;    //the data whose attribute at sai.idx does not equal sai.value (if DorC is true) or is not less than sai.value (if DorC is false)
	
	public String name;
	
	public int depth;
	
	public CNode(ArrayList<CData> cdata, ArrayList<ArrayList<CDomain>> contDomain, int depth) {
		this.cdata = cdata;
		this.contDomain = contDomain;
		this.depth = depth;
		
		cntTypeAll = new HashMap<String, Integer>();
		for (int i = 0; i < cdata.size(); ++i) {
			if (cntTypeAll.containsKey(cdata.get(i).type)) {
				cntTypeAll.put(cdata.get(i).type, cntTypeAll.get(cdata.get(i).type) + 1);
			} else {
				cntTypeAll.put(cdata.get(i).type, 1);
			}
		}
		cntTypeAll.put("all", cdata.size());
		
		//System.out.println(cdata.size() + ", " + contDomain.get(0).size());
	}
	
	public void split() {
		
		if (cdata.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
		
		if (isLeaf()) {
			cdata = null;
			contDomain = null;
			cntTypeAll = null;
			return;
		}
		
		//System.out.println("\n===============\nbegin\n");
		
		//System.out.println(cdata.size());
		//for (int i = 0; i < cdata.size(); ++i)
		//	System.out.println(cdata.get(i));
		
		
		sai = getBestSai();
		
		//System.out.println("best : " + sai.idx + ", " + sai.contV + ", " + sai.giniGain);
		
		if (sai.idx == -1) {
			setName();
			cdata = null;
			contDomain = null;
			cntTypeAll = null;
			return;
		}
		
		ArrayList<CData> leftData = new ArrayList<CData>();
		ArrayList<CData> rightData = new ArrayList<CData>();
		ArrayList<ArrayList<CDomain>> leftDomain = new ArrayList<ArrayList<CDomain>>();
		ArrayList<ArrayList<CDomain>> rightDomain = new ArrayList<ArrayList<CDomain>>();
		
		if (sai.DorC == true) {
			for (int i = 0; i < cdata.size(); ++i) {
				if (cdata.get(i).fDisc[sai.idx].equals(sai.discV)) {
					leftData.add(cdata.get(i));
				} else {
					rightData.add(cdata.get(i));
				}
			}
			
			//need split the domain
		} else {
			for (int i = 0; i < cdata.size(); ++i) {
				if (cdata.get(i).fCont[sai.idx] < sai.contV) {
					leftData.add(cdata.get(i));
				} else {
					rightData.add(cdata.get(i));
				}
			}
			
			for (int d = 0; d < contDomain.size(); ++d) {
				leftDomain.add(new ArrayList<CDomain>());
				rightDomain.add(new ArrayList<CDomain>());
				for (int i = 0; i < contDomain.get(d).size(); ++i) {
					if (contDomain.get(d).get(i).cd.fCont[sai.idx] < sai.contV)
						leftDomain.get(d).add(contDomain.get(d).get(i));
					else
						rightDomain.get(d).add(contDomain.get(d).get(i));
				}
			}
		}
		if (leftData.size() == 0 || rightData.size() == 0) {
			System.out.println(leftData.size() + ", " + rightData.size());
			System.err.println("zero size");
			System.exit(-1);
		}
		
		leftNode = new CNode(leftData, leftDomain, depth + 1);
		rightNode = new CNode(rightData, rightDomain, depth + 1);
		
		cdata = null;
		contDomain = null;
		cntTypeAll = null;
	}
	
	public SplitAttrInfo getBestSai() {
		
		SplitAttrInfo saiDisc = getBestDiscSai();
		SplitAttrInfo saiCont = getBestContSai();
		
		if (saiDisc == null) {
			return saiCont;
		} else if (saiCont == null) {
			return saiDisc;
		} else {
			if (saiDisc.giniGain > saiCont.giniGain)
				return saiCont;
			else
				return saiDisc;
		}
	}
	
	public SplitAttrInfo getBestDiscSai() {
		SplitAttrInfo bestSai = new SplitAttrInfo(true, -1, "", Double.MAX_VALUE);
		for (int i = 0; i < cdata.get(0).fDisc.length; ++i) {
			SplitAttrInfo sai = getBestDiscSai(i);
			
			if (sai != null && sai.giniGain < bestSai.giniGain)
				bestSai = sai;
		}
		//System.out.println("pivot: " + bestSai.idx + ", " + bestSai.discV + ", " + bestSai.giniGain);
		return bestSai;
	}
	
	public SplitAttrInfo getBestDiscSai(int idx) {
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
		
		if (cntAllType.size() == 1)
			return null;
		
		double minGiniGain = Double.MAX_VALUE;
		String minAttrV = null;
		for (String attrV : cntAllType.keySet()) {
			double giniLeft = calGini(cntAllType.get(attrV));
			double giniRight = calGini(cntTypeAll, cntAllType.get(attrV));
			
			double giniGain =  (giniLeft * cntAllType.get(attrV).get("all") + 
					giniRight * (cntTypeAll.get("all") - cntAllType.get(attrV).get("all"))) / (double)cntTypeAll.get("all");
			
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minAttrV = attrV;
			}
		}
		
		return new SplitAttrInfo(true, idx, minAttrV, minGiniGain);
	}
	
	public SplitAttrInfo getBestContSai() {
		SplitAttrInfo bestSai = new SplitAttrInfo(false, -1, 0.0, Double.MAX_VALUE);
		for (int i = 0; i < cdata.get(0).fCont.length; ++i) {
			//System.out.println("idx: " + i);
			
			SplitAttrInfo sai = getBestContSai(i);
			
			if (sai != null && sai.giniGain < bestSai.giniGain)
				bestSai = sai;
		}
		
		//System.out.println("best : " + bestSai.idx + ", " + bestSai.contV);
		
		return bestSai;
	}
	
	public SplitAttrInfo getBestContSai(int idx) {
		/*
		double[] domain = new double[cdata.size()];
		for (int i = 0; i < cdata.size(); ++i) {
			domain[i] = cdata.get(i).fCont[idx];
		}
		Arrays.sort(domain);
		
		double minGiniGain = Double.MAX_VALUE;
		double minSv = 0.0;
		for (int d = 0; d < domain.length - 1; ++d) {
			double sv = (domain[d] + domain[d + 1]) / 2;
			int cntLeft = 0;
			int cntRight = 0;
			HashMap<String, Integer> cntTypeLeft = new HashMap<String, Integer>();
			HashMap<String, Integer> cntTypeRight = new HashMap<String, Integer>();
			for (int i = 0; i < cdata.size(); ++i) {
				if (cdata.get(i).fCont[idx] < sv) {
					cntLeft++;
					if (cntTypeLeft.containsKey(cdata.get(i).type)) {
						cntTypeLeft.put(cdata.get(i).type, cntTypeLeft.get(cdata.get(i).type) + 1);
					} else {
						cntTypeLeft.put(cdata.get(i).type, 1);
					}
				} else {
					cntRight++;
					if (cntTypeRight.containsKey(cdata.get(i).type)) {
						cntTypeRight.put(cdata.get(i).type, cntTypeRight.get(cdata.get(i).type) + 1);
					} else {
						cntTypeRight.put(cdata.get(i).type, 1);
					}
				}
			}
			cntTypeLeft.put("all", cntLeft);
			cntTypeRight.put("all", cntRight);
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
			
		}
		*/
		
		double minGiniGain = Double.MAX_VALUE;
		double minSv = 0.0;
		
		HashMap<String, Integer> cntTypeLeft = new HashMap<String, Integer>();
		HashMap<String, Integer> cntTypeRight = new HashMap<String, Integer>();
		
		int cntAll = 0;
		for (int i = 0; i < cdata.size(); ++i) {
			if (cntTypeRight.containsKey(cdata.get(i).type)) {
				cntTypeRight.put(cdata.get(i).type, cntTypeRight.get(cdata.get(i).type) + 1);
			} else {
				cntTypeRight.put(cdata.get(i).type, 1);
			}
			cntAll++;
		}
		cntTypeLeft.put("all", 0);
		cntTypeRight.put("all", cntAll);
		
		int preD = -1;
		for (int d = 0; d < contDomain.get(idx).size() - 1; ++d) {
			while (d + 1 < contDomain.get(idx).size() && contDomain.get(idx).get(preD + 1).value == contDomain.get(idx).get(d + 1).value) d++;
			
			if (d + 1 == contDomain.get(idx).size()) break;
			
			double sv = (contDomain.get(idx).get(d).value + contDomain.get(idx).get(d + 1).value) / 2;
			
			cntTypeLeft.put("all", cntTypeLeft.get("all") + (d - preD) );
			cntTypeRight.put("all", cntTypeRight.get("all") - (d - preD));
			
			for (int j = preD + 1; j <= d; ++j) {
				String type = contDomain.get(idx).get(j).cd.type;
				if (cntTypeLeft.containsKey(type))
					cntTypeLeft.put(type, cntTypeLeft.get(type) + 1);
				else
					cntTypeLeft.put(type, 1);
				cntTypeRight.put(type, cntTypeRight.get(type) - 1);
				if (cntTypeRight.get(type) == 0)
					cntTypeRight.remove(type);
			}
			preD = d;
			
			int cntLeft = cntTypeLeft.get("all");
			int cntRight = cntTypeRight.get("all");
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
		}
		
		return new SplitAttrInfo(false, idx, minSv, minGiniGain);
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
		for (CData cd : cdata) {
			if (cntType.containsKey(cd.type)) {
				cntType.put(cd.type, cntType.get(cd.type) + 1);
			} else {
				cntType.put(cd.type, 1);
			}
		}
		
		if (depth > maxDepth || cdata.size() < minNode) {
			String tmpName = null;
			int maxCnt = Integer.MIN_VALUE;
			for (String type : cntType.keySet())
				if (cntType.get(type) > maxCnt) {
					tmpName = type;
					maxCnt = cntType.get(type);
				}
			
			name = tmpName;
			return true;
		}
		
		//for pruning
		double gini = calGini(cntTypeAll);
		
		if (cntType.size() > 1 && gini > 0.1)
			return false;
		
		String tmpName = null;
		int maxCnt = Integer.MIN_VALUE;
		for (String type : cntType.keySet())
			if (cntType.get(type) > maxCnt) {
				tmpName = type;
				maxCnt = cntType.get(type);
			}
		
		name = tmpName;
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
	
	public static double calGini(HashMap<String, Integer> cntTypeAll, HashMap<String, Integer> cntType) {
		double gini = 1.0;
		
		int all = 0;
		all += cntTypeAll.get("all");
		all -= cntType.get("all");
		
		for (String vt : cntTypeAll.keySet()) {
			if (vt.equals("all"))
				continue;
			
			if (cntType.containsKey(vt)) {
				if (cntTypeAll.get(vt) - cntType.get(vt) > 0) {
					double f = (cntTypeAll.get(vt) - cntType.get(vt)) / (double)all;
					gini -= f * f;
				}
			} else {
				double f = cntTypeAll.get(vt) / (double)all; 
				gini -= f * f;
			}
		}
		
		return gini;
	}
}
