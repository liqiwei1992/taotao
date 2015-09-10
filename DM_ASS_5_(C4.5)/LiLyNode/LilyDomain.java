package com.lqw.dt.node;

import java.util.ArrayList;

import com.lqw.data.Post;


//abandoned
public class LilyDomain implements Comparable<LilyDomain> {

	public double value;
	public Post post;
	
	boolean zero;   //true if value == 0.0 or false if value != 0.0
	public ArrayList<Post> zeroPosts;
	
	public LilyDomain(double value, Post post) {
		this.value = value;
		this.post = post;
		
		zero = false;
		zeroPosts = null;
	}
	
	public LilyDomain(ArrayList<Post> zeroPosts) {
		this.zeroPosts = zeroPosts;
		
		value = 0.0;
		post = null;
		zero = true;
	}
	
	@Override
	public int compareTo(LilyDomain o) {
		if (this.value < o.value)
			return -1;
		else if (this.value == o.value)
			return 0;
		else
			return 1;
	}
}
