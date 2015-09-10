package com.lqw.dt.node;

public class SplitAttrInfo {

	public boolean DorC;    //C means continuous (false) and D means discrete (true)
	public int idx;    //the index of the split attribute in the fDond or fDisc
	
	public double contV;    //the value of the split attribute if the DorC is false
	public String discV;    //the value of the split attribute if the DorC is true
	
	public double giniGain;    //the gini gain of the split or the variance
	
	public SplitAttrInfo(boolean DorC, int idx, double contV, double giniGain) {
		assert DorC == false : "";
		
		this.DorC = DorC;
		this.idx = idx;
		this.contV = contV;
		this.giniGain = giniGain;
	}
	
	public SplitAttrInfo(boolean DorC, int idx, String discV, double giniGain) {
		assert DorC == true : "";
		
		this.DorC = DorC;
		this.idx = idx;
		this.discV = discV;
		this.giniGain = giniGain;
	}
}
