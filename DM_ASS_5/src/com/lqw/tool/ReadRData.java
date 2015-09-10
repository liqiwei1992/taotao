package com.lqw.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.lqw.data.RData;

public class ReadRData {

	public static LinkedList<RData> readRData(String path) {
		LinkedList<RData> rdata = new LinkedList<RData>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			
			String line = null;
			boolean[] labels;
			int cntCont = -1;
			int cntDisc = 0;
			
			{
				line = br.readLine();
				StringTokenizer st = new StringTokenizer(line, ",");
				
				int cnt = st.countTokens();
				labels = new boolean[cnt];
				
				for (int i = 0; i < cnt; ++i) {
					String l = st.nextToken();
					if (l.equals("1")) {
						labels[i] = true;
						cntDisc++;
					} else {
						labels[i] = false;
						cntCont++;
					}
				}
				
				assert labels[cnt - 1] == false : "labels of regression must be continuous";
			}
			
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				
				int cnt = st.countTokens();
				int cntC = 0;
				double[] fCont = new double[cntCont];
				int cntD = 0;
				String[] fDisc = new String[cntDisc];
				
				for (int i = 0; i < cnt - 1; ++i) {
					String v = st.nextToken();
					
					if (labels[i])
						fDisc[cntD++] = String.valueOf(v);
					else
						fCont[cntC++] = Double.valueOf(v);
				}
				
				double value = Double.valueOf(st.nextToken());
				
				rdata.add(new RData(value, fDisc, fCont));
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rdata;
	}
}
