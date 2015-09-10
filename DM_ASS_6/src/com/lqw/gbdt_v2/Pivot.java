package com.lqw.gbdt_v2;

public class Pivot {

	public int idx;
	public double value;
	public double var;
	
	public Pivot(int idx, double value, double var) {
		this.idx = idx;
		this.value = value;
		this.var = var;
	}
}
