package com.lqw.dt.node;

public class SplitAttrInfo {

	public boolean DorC;    //C means continuous (false) and D means discrete (true)
	public int idx;    //the index of the split attribute in the fDond or fDisc
	public double value;    //the value of the split attribute if the DorC is false
	
	public double gainRatio;    //the entropy of the data at this attribute (and at this value if the DorC is false)
	
	/*
	public int cntLE;    //the count of the data those are less than or equal the value if the DorC is false
	public int cntGT;    //the count of the data those are greater than the value if the DorC is false
	public HashMap<String, Integer> cntType;    //the count of each class if the DorC is true
	*/
	
	public SplitAttrInfo(boolean DorC, int idx, double gainRatio) {
		this.DorC = DorC;
		this.idx = idx;
		this.gainRatio = gainRatio;
	}
}
