package com.lqw.dt;

import java.util.ArrayList;

import com.lqw.data.Data;
import com.lqw.data.RData;

public class RegressionDT {

	public ArrayList<RData> trainRData;
	
	public String[] defaultValueDisc;
	public double[] defaultValueCont;
	
	public RegressionDT(ArrayList<RData> trainRData) {
		
		defaultValueDisc = Data.preprocessDisc(trainRData);
		defaultValueCont = Data.preprocessCont(trainRData);
		
		this.trainRData = trainRData;
		
	}
	
	public void fit() {
		
		
	}
	
	public double predicate(ArrayList<RData> testRData) {
		
		
		
		return 0.0;
	}
}
