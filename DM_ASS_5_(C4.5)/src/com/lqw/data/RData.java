package com.lqw.data;

public class RData extends Data {

	public double value;
	
	public RData(double value, String[] fDisc, double[] fCont) {
		super(fDisc, fCont);
		
		this.value = value;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Value: " + value + ":\nDisc: ");
		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	public void preprocess() {
		for (int i = 0; i < fDisc.length; ++i) {
			
		}
	}
}
