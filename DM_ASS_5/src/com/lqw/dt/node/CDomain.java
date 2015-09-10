package com.lqw.dt.node;

import com.lqw.data.CData;

public class CDomain implements Comparable<CDomain> {

	public double value;
	public CData cd;
	
	public CDomain(double value, CData cd) {
		this.value = value;
		this.cd = cd;
	}

	@Override
	public int compareTo(CDomain o) {
		if (this.value < o.value)
			return -1;
		else if (this.value == o.value)
			return 0;
		else
			return 1;
	}
}
