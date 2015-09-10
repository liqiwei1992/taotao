package com.lqw.dt.node;

import java.util.ArrayList;
import java.util.HashMap;

import com.lqw.data.RData;
import com.lqw.tool.MyMath;

public class RNode {

	public static double threshold_var = 0.00001;
	public static int threshold_depth = Integer.MAX_VALUE;
	public static double split_percentage = 0.001;
	
	public ArrayList<RData> rdata;
	public ArrayList<ArrayList<RDomain>> contDomain;    //the domain (binding the CData) of each continuous feature
	public int depth;
	
	public SplitAttrInfo sai;
	public RNode leftNode;    //the data whose attribute at sai.idx equals sai.value (if DorC is true) or is less than sai.value (if DorC is false) 
	public RNode rightNode;    //the data whose attribute at sai.idx does not equal sai.value (if DorC is true) or is not less than sai.value (if DorC is false)
	
	public double value = Double.NaN;    //the average value of rdata if the node is a leaf or the NaN 
	
	public RNode(ArrayList<RData> rdata, ArrayList<ArrayList<RDomain>> contDomain, int depth) {
		this.rdata = rdata;
		this.contDomain = contDomain;
		this.depth = depth;
		
		//System.out.println(rdata.size() + ", " + depth);
	}
	
	public void split() {
		if (rdata.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
		
		if (isLeaf()) {
			rdata = null;
			contDomain = null;
			return;
		}
		
		sai = getBestSai();
		
		if (sai.idx == -1) {
			setValue();
			rdata = null;
			contDomain = null;
			return;
		}
		
		ArrayList<RData> leftData = new ArrayList<RData>();
		ArrayList<RData> rightData = new ArrayList<RData>();
		ArrayList<ArrayList<RDomain>> leftDomain = new ArrayList<ArrayList<RDomain>>();
		ArrayList<ArrayList<RDomain>> rightDomain = new ArrayList<ArrayList<RDomain>>();
		
		if (sai.DorC == true) {
			for (int i = 0; i < rdata.size(); ++i) {
				if (rdata.get(i).fDisc[sai.idx].equals(sai.discV)) {
					leftData.add(rdata.get(i));
				} else {
					rightData.add(rdata.get(i));
				}
			}
			
			for (int d = 0; d < contDomain.size(); ++d) {
				leftDomain.add(new ArrayList<RDomain>());
				rightDomain.add(new ArrayList<RDomain>());
				for (int i = 0; i < contDomain.get(d).size(); ++i) {
					if (contDomain.get(d).get(i).rd.fDisc[sai.idx].equals(sai.discV))
						leftDomain.get(d).add(contDomain.get(d).get(i));
					else
						rightDomain.get(d).add(contDomain.get(d).get(i));
				}
			}
		} else {
			for (int i = 0; i < rdata.size(); ++i) {
				if (rdata.get(i).fCont[sai.idx] < sai.contV) {
					leftData.add(rdata.get(i));
				} else {
					rightData.add(rdata.get(i));
				}
			}
			
			for (int d = 0; d < contDomain.size(); ++d) {
				leftDomain.add(new ArrayList<RDomain>());
				rightDomain.add(new ArrayList<RDomain>());
				for (int i = 0; i < contDomain.get(d).size(); ++i) {
					if (contDomain.get(d).get(i).rd.fCont[sai.idx] < sai.contV)
						leftDomain.get(d).add(contDomain.get(d).get(i));
					else
						rightDomain.get(d).add(contDomain.get(d).get(i));
				}
			}
		}
		if (leftData.size() == 0 || rightData.size() == 0) {
			//System.err.println(leftData.size() + ", " + rightData.size());
			//System.err.println("zero size");
			//System.exit(-1);
			setValue();
			rdata = null;
			contDomain = null;
			return;
		}
		
		//System.out.println(rdata.size() + ": " + leftData.size() + ", " + rightData.size());
		
		leftNode = new RNode(leftData, leftDomain, depth + 1);
		rightNode = new RNode(rightData, rightDomain, depth + 1);
		
		rdata = null;
		contDomain = null;
		
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
		for (int i = 0; i < rdata.get(0).fDisc.length; ++i) {
			SplitAttrInfo sai = getBestDiscSai(i);
			
			if (sai != null && sai.giniGain < bestSai.giniGain)
				bestSai = sai;
		}
		
		return bestSai;
	}
	
	public SplitAttrInfo getBestDiscSai(int idx) {
		HashMap<String, ArrayList<RData>> typeRData = new HashMap<String, ArrayList<RData>>();
		for (int i = 0; i < rdata.size(); ++i) {
			String attrV = rdata.get(i).fDisc[idx];
			if (!typeRData.containsKey(attrV)) {
				typeRData.put(attrV, new ArrayList<RData>());
			}
			typeRData.get(attrV).add(rdata.get(i));
		}
		
		if (typeRData.size() == 1)
			return null;
		
		double minVar = Double.MAX_VALUE;
		String minAttrV = null;
		for (String attrV : typeRData.keySet()) {
			if (typeRData.get(attrV).size() < rdata.size() * split_percentage) continue;
			if (typeRData.get(attrV).size() > rdata.size() * (1.0 - split_percentage)) continue;
			
			double varLeft = calVar(typeRData.get(attrV));
			double varRight = calVar(typeRData, attrV);
			
			double varAll =  varLeft + varRight;
			
			if (varAll < minVar) {
				minVar = varAll;
				minAttrV = attrV;
			}
		}
		
		return new SplitAttrInfo(true, idx, minAttrV, minVar);
	}
	
	public SplitAttrInfo getBestContSai() {
		SplitAttrInfo bestSai = new SplitAttrInfo(false, -1, 0.0, Double.MAX_VALUE);
		for (int i = 0; i < rdata.get(0).fCont.length; ++i) {
			//System.out.println("idx: " + i);
			
			SplitAttrInfo sai = getBestContSai(i);
			
			if (sai != null && sai.giniGain < bestSai.giniGain)
				bestSai = sai;
		}
		
		//System.out.println("best : " + bestSai.idx + ", " + bestSai.contV);
		
		return bestSai;
	}
	
	public SplitAttrInfo getBestContSai(int idx) {
		
		double minVar = Double.MAX_VALUE;
		double minSv = 0.0;
		
		int preD = (int)(contDomain.get(idx).size() * split_percentage);
		for (int d = preD; d < contDomain.get(idx).size(); ++d) {
			while (d + 1 < contDomain.get(idx).size() && contDomain.get(idx).get(preD + 1).value == contDomain.get(idx).get(d + 1).value) d++;
			
			//if (d + 1 < contDomain.get(idx).size() * split_percentage) continue;
			if (d + 1 > contDomain.get(idx).size() * (1.0 - split_percentage)) break;
			//if (d + 1 == contDomain.get(idx).size()) break;
			
			double sv = (contDomain.get(idx).get(d).value + contDomain.get(idx).get(d + 1).value) / 2;
			
			ArrayList<RData> leftTypeRData = new ArrayList<RData>();
			ArrayList<RData> rightTypeRData = new ArrayList<RData>();
			
			for (int i = 0; i <= d; ++i)
				leftTypeRData.add(contDomain.get(idx).get(i).rd);
			for (int i = d + 1; i < contDomain.get(idx).size(); ++i)
				rightTypeRData.add(contDomain.get(idx).get(i).rd);
			
			double varLeft = calVar(leftTypeRData);
			double varRight = calVar(rightTypeRData);
			
			double varAll =  varLeft + varRight;
			
			if (varAll < minVar) {
				minVar = varAll;
				minSv = sv;
			}
		}
		
		return new SplitAttrInfo(false, idx, minSv, minVar);
	}
	
	public void setValue() {
		double sum = 0.0;
		for (int i = 0; i < rdata.size(); ++i)
			sum += rdata.get(i).value;
		value = sum / (double)rdata.size();
	}
	
	public boolean isLeaf() {
		
		if (depth > threshold_depth) {
			setValue();
			return true;
		}
		
		double var = calVar(rdata);
		
		if (var > threshold_var) {
			return false;
		} else {
			setValue();
			return true;
		}
	}
	
	public static double calVar(ArrayList<RData> rd) {
		double[] values = new double[rd.size()];
		for (int i = 0; i < rd.size(); ++i)
			values[i] = rd.get(i).value;
		return MyMath.variance(values);
	}
	public static double calVar(HashMap<String, ArrayList<RData>> rd, String exAttrV) {
		int cnt = 0;
		for (String attrV : rd.keySet()) {
			if (!attrV.equals(exAttrV))
				cnt += rd.get(attrV).size();
		}
		double[] values = new double[cnt];
		cnt = 0;
		for (String attrV : rd.keySet()) {
			if (!attrV.equals(exAttrV)) {
				for (int i = 0; i < rd.get(attrV).size(); ++i)
					values[cnt++] = rd.get(attrV).get(i).value;
			}
		}
		return MyMath.variance(values);
	}
}
