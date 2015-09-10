package com.lqw.gbdt;

import java.util.ArrayList;
import java.util.LinkedList;

import com.lqw.data.Data;

public class BaseDT {

	public ArrayList<Data> trainData;
	
	public Node root;
	
	
	public BaseDT(ArrayList<Data> trainData) {
		this.trainData = trainData;
	}
	
	public void build() {
		root = new Node(trainData, 0);
		
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
	
	public double predicate(Data da) {
		Node node = root;
		while (node.left != null) {
			if (da.feature[node.pivot.idx] < node.pivot.value) {
				node = node.left;
			} else {
				node = node.right;
			}
		}
		
		//System.out.println(node.value);
		
		return node.value;
	}
}
