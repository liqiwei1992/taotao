package com.lqw.feature;

public class Gauss {

	public int featureWordCnt;
	public int forumWordCnt;
	
	public double mu = 0.0;
	public double sigma = 0.0;
	
	public Gauss(double mu, double sigma, int featureWordCnt, int forumWordCnt) {
		this.mu = mu;
		this.sigma = sigma;
		
		this.featureWordCnt = featureWordCnt;
		this.forumWordCnt = forumWordCnt;
	}
	
	public double logFunction(double x) {
		/*
		if (Math.abs(sigma) < 0.000000001) {
			if (Math.abs(mu) < 0.000000001) {
				return 0.0 - Math.log(1.0 / (featureWordCnt + forumWordCnt));
			} else {
				return x;
			}
		}
		*/
		return 0.0 - Math.log(2 * Math.PI * sigma) / 2 - (x - mu) * (x - mu) / (2 * sigma);
	}
}
