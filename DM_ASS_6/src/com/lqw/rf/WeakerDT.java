package com.lqw.rf;

import java.util.ArrayList;
import java.util.LinkedList;

import com.lqw.data.Data;

public class WeakerDT {

	public ArrayList<Data> trainData;
	
	public Node root;
	
	public WeakerDT(ArrayList<Data> trainData) {
		this.trainData = trainData;
	}
	
	public void build() {
		int m = (int)Math.sqrt(trainData.get(0).feature.length);
		root = new Node(trainData, m);
		
		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(root);
		
		while (!nodes.isEmpty()) {
			Node node = nodes.remove();
			node.split();
			
			if (node.left != null && node.right != null) {
				nodes.add(node.left);
				nodes.add(node.right);
			}
		}
	}
	
	public String predicate(Data da) {
		Node node = root;
		while (node.left != null) {
			assert node.right != null : "wrong when build the tree";
			
			if (da.feature[node.pivot.idx] < node.pivot.value) {
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return node.name;
	}
}
