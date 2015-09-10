package com.lqw.data;

public class Data {

	public String type;    //classification
	public double value;    //regression
	public double[] feature;    //continuous feature
	
	
	public Data(String type, double[] feature) {
		this.type = type;
		this.feature = feature;
	}
	
	public Data(double value, double[] feature) {
		this.value = value;
		this.feature = feature;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("type: " + type + "(value = " + value + "\n");
		for (int i = 0; i < feature.length; ++i)
			sb.append(feature[i] + " ");
		
		return sb.toString();
	}
}
