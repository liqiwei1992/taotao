package com.lqw.dt.node;

import java.util.ArrayList;
import java.util.HashMap;

import com.lqw.data.Post;


//abandoned
public class LilyNode {

	public ArrayList<Post> posts;
	public ArrayList<String> featureWords;
	public ArrayList<ArrayList<LilyDomain>> domains;
	
	public HashMap<String, Integer> cntTypeAll;
	
	public SplitAttrInfo sai;    //the sai just have continuous attribute and the DorC is always false
	public LilyNode leftNode;    //the data whose attribute at sai.idx is less than sai.value (DorC is false) 
	public LilyNode rightNode;    //the data whose attribute at sai.idx is not less than sai.value (DorC is false)
	
	public String name;    //the attribute's name if the node is not leaf, and the predicated forum if the node is leaf
	
	public LilyNode(ArrayList<Post> posts, ArrayList<String> featureWords, ArrayList<ArrayList<LilyDomain>> domains) {
		this.posts = posts;
		this.featureWords = featureWords;
		this.domains = domains;
		
		cntTypeAll = new HashMap<String, Integer>();
		for (int i = 0; i < posts.size(); ++i) {
			if (cntTypeAll.containsKey(posts.get(i).forumName)) {
				cntTypeAll.put(posts.get(i).forumName, cntTypeAll.get(posts.get(i).forumName) + 1);
			} else {
				cntTypeAll.put(posts.get(i).forumName, 1);
			}
		}
		cntTypeAll.put("all", posts.size());
	}
	
	public void split() {
		if (posts.size() == 0) {
			System.err.println("the size of cdata is zero");
			System.exit(-1);
		}
		
		if (isLeaf()) {
			posts = null;
			featureWords = null;
			domains = null;
			return;
		}
		sai = getBestSai();
		
		if (sai.idx == -1) {
			setName();
			posts = null;
			featureWords = null;
			domains = null;
			return;
		}
		
		ArrayList<Post> leftPosts = new ArrayList<Post>();
		ArrayList<Post> rightPosts = new ArrayList<Post>();
		ArrayList<ArrayList<LilyDomain>> leftDomains = new ArrayList<ArrayList<LilyDomain>>();
		ArrayList<ArrayList<LilyDomain>> rightDomains = new ArrayList<ArrayList<LilyDomain>>();
		
		assert sai.DorC == false : "DorC should be false in LilyNode";
		
		//split the posts and the domains
		String sw = featureWords.get(sai.idx);
		for (int p = 0; p < posts.size(); ++p) {
			if (!posts.get(p).wordTfIdf.containsKey(sw)) {
				leftPosts.add(posts.get(p));
			} else if (posts.get(p).wordTfIdf.get(sw) < sai.contV) {
				leftPosts.add(posts.get(p));
			} else {
				rightPosts.add(posts.get(p));
			}
		}
		for (int d = 0; d < domains.size(); ++d) {
			leftDomains.add(new ArrayList<LilyDomain>());
			rightDomains.add(new ArrayList<LilyDomain>());
			for (int i = 0; i < domains.get(d).size(); ++i) {
				if (domains.get(d).get(i).zero == true) {
					leftDomains.get(d).add(domains.get(d).get(i));
				} else {
					if (domains.get(d).get(i).post.wordTfIdf.get(sw) < sai.contV)
						leftDomains.get(d).add(domains.get(d).get(i));
					else
						rightDomains.get(d).add(domains.get(d).get(i));
				}
			}
		}
		
		if (leftPosts.size() == 0 || rightPosts.size() == 0) {
			System.out.println(leftPosts.size() + ", " + rightPosts.size());
			System.err.println("zero size");
			System.exit(-1);
		}
		
		leftNode = new LilyNode(leftPosts, featureWords, leftDomains);
		rightNode = new LilyNode(rightPosts, featureWords, rightDomains);
		
		posts = null;
		featureWords = null;
		domains = null;
	}
	
	public SplitAttrInfo getBestSai() {
		SplitAttrInfo bestSai = new SplitAttrInfo(false, -1, 0.0, Double.MAX_VALUE);
		for (int i = 0; i < featureWords.size(); ++i) {
			SplitAttrInfo sai = getBestSai(i);
			
			if (sai != null && sai.giniGain < bestSai.giniGain)
				bestSai = sai;
		}
		
		//System.out.println("best : " + bestSai.idx + ", " + bestSai.contV);
		
		return bestSai;
	}
	
	public SplitAttrInfo getBestSai(int idx) {
		double minGiniGain = Double.MAX_VALUE;
		double minSv = 0.0;
		
		HashMap<String, Integer> cntTypeLeft = new HashMap<String, Integer>();
		HashMap<String, Integer> cntTypeRight = new HashMap<String, Integer>();
		
		int cntAll = 0;
		for (int i = 0; i < posts.size(); ++i) {
			if (cntTypeRight.containsKey(posts.get(i).forumName)) {
				cntTypeRight.put(posts.get(i).forumName, cntTypeRight.get(posts.get(i).forumName) + 1);
			} else {
				cntTypeRight.put(posts.get(i).forumName, 1);
			}
			cntAll++;
		}
		cntTypeLeft.put("all", 0);
		cntTypeRight.put("all", cntAll);
		
		//cannot process this condition that the word has the same values in two different posts
		for (int f = 0; f < domains.get(idx).size() - 1; ++f) {
			double sv = (domains.get(idx).get(f).value + domains.get(idx).get(f + 1).value) / 2;
			
			if (domains.get(idx).get(f).zero == true) {
				for (Post post : domains.get(idx).get(f).zeroPosts) {
					if (cntTypeLeft.containsKey(post.forumName))
						cntTypeLeft.put(post.forumName, cntTypeLeft.get(post.forumName) + 1);
					else
						cntTypeLeft.put(post.forumName, 1);
					cntTypeRight.put(post.forumName, cntTypeRight.get(post.forumName) - 1);
					if (cntTypeRight.get(post.forumName) == 0)
						cntTypeRight.remove(post.forumName);
				}
				cntTypeLeft.put("all", cntTypeLeft.get("all") + domains.get(idx).get(f).zeroPosts.size());
				cntTypeRight.put("all", cntTypeRight.get("all") - domains.get(idx).get(f).zeroPosts.size());
			} else {
				if (cntTypeLeft.containsKey(domains.get(idx).get(f).post.forumName))
					cntTypeLeft.put(domains.get(idx).get(f).post.forumName, cntTypeLeft.get(domains.get(idx).get(f).post.forumName) + 1);
				else
					cntTypeLeft.put(domains.get(idx).get(f).post.forumName, 1);
				cntTypeRight.put(domains.get(idx).get(f).post.forumName, cntTypeRight.get(domains.get(idx).get(f).post.forumName) - 1);
				if (cntTypeRight.get(domains.get(idx).get(f).post.forumName) == 0)
					cntTypeRight.remove(domains.get(idx).get(f).post.forumName);
				
				cntTypeLeft.put("all", cntTypeLeft.get("all") + 1);
				cntTypeRight.put("all", cntTypeRight.get("all") - 1);
			}
			
			int cntLeft = cntTypeLeft.get("all");
			int cntRight = cntTypeRight.get("all");
			
			double giniLeft = calGini(cntTypeLeft);
			double giniRight = calGini(cntTypeRight);
			
			double giniGain = (giniLeft * cntLeft + giniRight * cntRight) / (double)(cntLeft + cntRight);
			if (giniGain < minGiniGain) {
				minGiniGain = giniGain;
				minSv = sv;
			}
		}
		
		return new SplitAttrInfo(false, idx, minSv, minGiniGain);
	}
	
	
	public void setName() {
		int maxCnt = Integer.MIN_VALUE;
		for (String type : cntTypeAll.keySet()) {
			if (type.equals("all"))
				continue;
			
			if (cntTypeAll.get(type) > maxCnt) {
				name = type;
				maxCnt = cntTypeAll.get(type);
			}
		}
	}
	
	public boolean isLeaf() {
		//for pruning
		double gini = calGini(cntTypeAll);
		
		if (cntTypeAll.size() > 1 && gini > 0.1)
			return false;
		
		setName();
		
		return true;
	}
	
	public static double calGini(HashMap<String, Integer> cntType) {
		double gini = 1.0;
		
		int all = 0;
		all += cntType.get("all");
		for (String vt : cntType.keySet()) {
			if (vt.equals("all"))
				continue;
			
			double f = cntType.get(vt) / (double)all;
			gini -= f * f;
		}
		
		return gini;
	}
}
