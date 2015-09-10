package com.lqw.gbdt;

import java.util.ArrayList;
import java.util.Random;

import com.lqw.Main;
import com.lqw.data.Data;

public class GBDT {

	public static final int DT_NUM = 5;
	public static final double sample_percentage = 0.66;
	
	public ArrayList<Data> trainData;
	
	public ArrayList<BaseDT> dts = new ArrayList<BaseDT>();
	
	public Random rand;
	
	
	public GBDT(ArrayList<Data> trainData) {
		this.trainData = trainData;
		
		rand = Main.rand;
	}
	
	public void build() {
		for (int t = 0; t < DT_NUM; ++t) {
			//System.out.println("t = " + t);
			
			
			//ArrayList<Data> data = new ArrayList<Data>();
			//for (int i = 0; i < trainData.size() * sample_percentage; ++i) {
			//	data.add(trainData.get(rand.nextInt(trainData.size())));
			//}
			
			//BaseDT dt = new BaseDT(data);
			BaseDT dt = new BaseDT(trainData);
			dt.build();
			
			dts.add(dt);
			
			for (int i = 0; i < trainData.size(); ++i) {
				
				double predValue = dt.predicate(trainData.get(i));
				
				//System.out.print(i + ": " + trainData.get(i).value + " - " + predValue + " = ");
				
				trainData.get(i).value = trainData.get(i).value - predValue;
				
				//System.out.println(trainData.get(i).value);
			}
			
			//System.out.println();
		}
		
		//for (int i = 0; i < trainData.size(); ++i) {
			//System.out.print(i + ": ");
			
			//double p = predicate(trainData.get(i));
			//System.out.println( " " + p + " ");
		//}
	}
	
	public double predicate(Data da) {
		double predValue = 0.0;
		for (int i = 0; i < dts.size(); ++i) {
			double p = dts.get(i).predicate(da);
			//System.out.print( " " + p + " ");
			predValue += p;
		}
		return predValue;
	}
}
