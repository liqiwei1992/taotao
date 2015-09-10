package com.lqw.ab;

public class Pivot {

	public int idx;
	public double value;
	public double giniGain;
	
	public Pivot(int idx, double value, double giniGain) {
		this.idx = idx;
		this.value = value;
		this.giniGain = giniGain;
	}
}
