package com.lqw.dt.node;

import java.util.ArrayList;
import java.util.HashMap;

import com.lqw.data.RData;
import com.lqw.tool.MyMath;

public class RNode_ {

	public static final double threshold_var = 0.00001;
	
	public ArrayList<RData> rdata;
	public ArrayList<Double[]> domains;
	
	//public ArrayList<Double[]> intervals;
	
	public SplitAttrInfo sai;
	public RNode_ leftNode;    //the data whose attribute at sai.idx equals sai.value (if DorC is true) or is less than sai.value (if DorC is false) 
	public RNode_ rightNode;    //the data whose attribute at sai.idx does not equal sai.value (if DorC is true) or is not less than sai.value (if DorC is false)
	
	public double value = Double.NaN;    //the average value of rdata if the node is a leaf or the NaN 
	
	public RNode_(ArrayList<RData> rdata, ArrayList<Double[]> domains) {
		this.rdata = rdata;
		this.domains = domains;
		
		/*
		intervals = new ArrayList<Double[]>();
		for (int f = 0; f < rdata.get(0).fCont.length; ++f) {
			double min = Double.MAX_VALUE;
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < rdata.size(); ++i) {
				if (rdata.get(i).fCont[f] < min)
					min = rdata.get(i).fCont[f];
				if (rdata.get(i).fCont[f] > max)
					max = rdata.get(i).fCont[f];
			}
			intervals.add(new Double[]{min, max});
		}
		*/
	}
	
	public void split() {
		if (rdata.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
		
		if (isLeaf()) {
			rdata = null;
			return;
		}
		
		sai = getBestSai();
		
		if (sai.idx == -1) {
			setValue();
			rdata = null;
			return;
		}
		
		ArrayList<RData> leftData = new ArrayList<RData>();
		ArrayList<RData> rightData = new ArrayList<RData>();
		if (sai.DorC == true) {
			for (int i = 0; i < rdata.size(); ++i) {
				if (rdata.get(i).fDisc[sai.idx].equals(sai.discV)) {
					leftData.add(rdata.get(i));
				} else {
					rightData.add(rdata.get(i));
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
		}
		if (leftData.size() == 0 || rightData.size() == 0) {
			System.out.println(leftData.size() + ", " + rightData.size());
			System.err.println("zero size");
			System.exit(-1);
		}
		
		leftNode = new RNode_(leftData, domains);
		rightNode = new RNode_(rightData, domains);
		
		rdata = null;
		
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
		/*
		double[] domain = new double[rdata.size()];
		for (int i = 0; i < rdata.size(); ++i) {
			domain[i] = rdata.get(i).fCont[idx];
		}
		Arrays.sort(domain);
		*/
		Double[] domain = domains.get(idx);
		
		double minVar = Double.MAX_VALUE;
		double minSv = 0.0;
		for (int d = 0; d < domain.length - 1; ++d) {
			double sv = (domain[d] + domain[d + 1]) / 2;
			
			if (sv <= rdata.get(0).fCont[idx] || sv >= rdata.get(rdata.size() - 1).fCont[idx])
				continue;
			//if (sv <= intervals.get(idx)[0] || sv >= intervals.get(idx)[1])
			//	continue;
			
			double var = calVar(rdata, idx, sv);
			
			if (var < minVar) {
				minVar = var;
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
		double var = calVar(rdata);
		
		if (var > threshold_var) {
			return false;
		} else {
			setValue();
			return true;
		}
	}
	
	public static double calVar(ArrayList<RData> rd, int idx, double sv) {
		int leftCnt = 0;
		int rightCnt = 0;
		
		for (int i = 0; i < rd.size(); ++i) {
			if (rd.get(i).fCont[idx] < sv)
				leftCnt++;
			else
				rightCnt++;
		}
		
		assert leftCnt != 0 && rightCnt != 0 : "wrong split value";
		
		//System.out.println(rd.size() + ", " + leftCnt + ", " + rightCnt);
		
		double[] leftValues = new double[leftCnt];
		double[] rightValues = new double[rightCnt];
		
		leftCnt = 0;
		rightCnt = 0;
		for (int i = 0; i < rd.size(); ++i) {
			if (rd.get(i).fCont[idx] < sv)
				leftValues[leftCnt++] = rd.get(i).value;
			else
				rightValues[rightCnt++] = rd.get(i).value;
		}
		
		return MyMath.variance(leftValues) + MyMath.variance(rightValues);
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
