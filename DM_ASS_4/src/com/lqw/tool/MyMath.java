package com.lqw.tool;

public class MyMath {

	public static double average(double[] data) {
		double sum = 0.0;
		for (int i = 0; i < data.length; ++i)
			sum += data[i];
		
		return sum / data.length;
	}
	
	public static double variance(double[] data) {
		double avg = average(data);
		double squarSum = 0.0;
		for (int i = 0; i < data.length; ++i)
			squarSum += data[i] * data[i];
		return (squarSum - avg * avg * data.length) / data.length;
	}
	
	public static double standardVariance(double[] data) {
		return Math.sqrt(variance(data));
	}
}
