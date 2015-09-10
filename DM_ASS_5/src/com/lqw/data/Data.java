package com.lqw.data;

import java.util.ArrayList;
import java.util.HashMap;

public class Data {

	public String[] fDisc;
	public double[] fCont;
	
	public Data(String[] fDisc, double[] fCont) {
		this.fDisc = fDisc;
		this.fCont = fCont;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fDisc.length; ++i)
			sb.append(fDisc[i] + " ");
		sb.append("\nCont: ");
		for (int i = 0; i < fCont.length; ++i)
			sb.append(fCont[i] + " ");
		
		return sb.toString();
	}
	
	public static String[] preprocessDisc(ArrayList<? extends Data> data) {
		String[] defaultValue = new String[data.get(0).fDisc.length];
		
		//preprocess the discrete value
		for (int i = 0; i < data.get(0).fDisc.length; ++i) {
			HashMap<String, Integer> fCount = new HashMap<String, Integer>();
			for (int j = 0; j < data.size(); ++j) {
				String f = data.get(j).fDisc[i];
				if (f.equals("NaN"))
					continue;
				
				if (fCount.containsKey(f)) {
					fCount.put(f, fCount.get(f) + 1);
				} else {
					fCount.put(f, 0);
				}
			}
			
			String maxF = null;
			int maxCount = Integer.MIN_VALUE;
			for (String f : fCount.keySet()) {
				if (fCount.get(f) > maxCount) {
					maxF = f;
					maxCount = fCount.get(f);
				}
			}
			
			if (maxF == null)
				continue;
			
			for (int j = 0; j < data.size(); ++j) {
				if (data.get(j).fDisc[i].equals("NaN"))
					data.get(j).fDisc[i] = maxF;
			}
			
			defaultValue[i] = maxF;
		}
		
		return defaultValue;
	}
	
	public static double[] preprocessCont(ArrayList<? extends Data> data) {
		double[] defaultValue = new double[data.get(0).fCont.length];
		
		//preprocess the continuous value
		for (int i = 0; i < data.get(0).fCont.length; ++i) {
			double sum = 0.0;
			int cnt = 0;
			for (int j = 0; j < data.size(); ++j) {
				double v = data.get(j).fCont[i];
				if (!Double.isNaN(v)) {
					sum += v;
					cnt++;
				}
			}
			if (cnt == 0)
				continue;
			
			double avg = sum / (double)cnt;
			for (int j = 0; j < data.size(); ++j) {
				if (Double.isNaN(data.get(j).fCont[i])) {
					data.get(j).fCont[i] = avg;
				}
			}
			
			defaultValue[i] = avg;
		}
		
		return defaultValue;
	}
}
