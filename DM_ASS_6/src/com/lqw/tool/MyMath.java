package com.lqw.tool;

import java.util.ArrayList;

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
	
	public static double average(ArrayList<Double> data) {
		double sum = 0.0;
		for (int i = 0; i < data.size(); ++i)
			sum += data.get(i);
		return sum / data.size();
	}
	
	public static double variance(ArrayList<Double> data) {
		double avg = average(data);
		double squarSum = 0.0;
		for (int i = 0; i < data.size(); ++i)
			squarSum += data.get(i) * data.get(i);
		return (squarSum - avg * avg * data.size()) / data.size();
	}
}
