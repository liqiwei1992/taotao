package com.lqw.data;

public class CData extends Data {

	public String type;
	
	public CData(String type, String[] fDisc, double[] fCont) {
		super(fDisc, fCont);
		
		this.type = type;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Class: " + type + ":\nDisc: ");
		
		sb.append(super.toString());
		
		return sb.toString();
	}
}
