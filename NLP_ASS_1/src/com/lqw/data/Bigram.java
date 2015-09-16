package com.lqw.data;

public class Bigram {

	public String w1;
	public String w2;
	
	public Bigram(String w1, String w2) {
		this.w1 = w1;
		this.w2 = w2;
	}
	
	@Override
	public String toString() {
		return w1 + ":" + w2;
	}
	
	@Override
	public boolean equals(Object b) {
		return w1.equals(((Bigram)b).w1) && w2.equals(((Bigram)b).w2);
	}
	
	@Override
	public int hashCode() {
		return (w1 + ":" + w2).hashCode();
	}
}
