package com.lqw.dt.node;

import com.lqw.data.RData;

public class RDomain implements Comparable<RDomain> {

	public double value;
	public RData rd;
	
	public RDomain(double value, RData rd) {
		this.value = value;
		this.rd = rd;
	}

	@Override
	public int compareTo(RDomain o) {
		if (this.value < o.value)
			return -1;
		else if (this.value == o.value)
			return 0;
		else
			return 1;
	}
}
