import java.util.ArrayList;
import java.util.List;


public class Preflow {
	List<Node> nodeList = new ArrayList<Node>();
	List<Link> linkList = new ArrayList<Link>();
	
	//creates a new preflow
	public Preflow(){
		nodeList = new ArrayList<Node>();
		linkList = new ArrayList<Link>();
	}
	
	//creates a preflow based on a nodelist and link list
	public Preflow(List<Node> nl, List<Link> ll){
		nodeList = nl;
		linkList = ll;
	}
	
	//returns the node list of the network
	public List<Node> getNodeList(){
		return nodeList;
	}
	
	//redefines the node list of the preflow
	public void setNodeList(List<Node> n){
		nodeList = n;
	}
	
	public void addNodeToList(Node n){
		nodeList.add(n);
	}
	
	public List<Link> getLinkList(){
		return linkList;
	}
	
	public void setLinkList(List<Link> l){
		linkList = l;
	}
	
	public void addLinkToList(Link l){
		linkList.add(l);
	}
	
	//initializes the preflow
	public void initialize(Node sink, double start){
		for(Link l : linkList){
			l.setFlow(0);
		}
		for(Node n : nodeList){
			if(n.equals(sink)){
				n.setExcess(start);
				n.setHeight(nodeList.size());
			} else {
				n.setExcess(0);
				n.setHeight(0);
			}
			n.setHiddenExcess(0);
		}
	}
	
	//push-flow using a recursive function that hands front and back flow
	public double recursivePush(Preflow flow, Node current, Node sink, List<Link> links){
		for(Link l : links){
			if(l.getStartNode().equals(current)){
				if(l.getEndNode().equals(sink)){
					double delta = Math.min(current.getExcess(), l.getCapacity() - l.getFlow());
					delta = Math.max(delta, 0);
					l.setFlow(l.getFlow() + delta);
					l.getEndNode().setExcess(l.getEndNode().getExcess() + delta);
					l.getStartNode().setExcess(l.getStartNode().getExcess() - delta);
					if(l.useFakeValues() && l.getFakeCapacity() > l.getCapacity()){
						double difference = Math.max(l.getFakeCapacity() - l.getFlow(), 0);
						l.getStartNode().setHiddenExcess(l.getStartNode().getHiddenExcess() + difference);
						l.getStartNode().setExcess(l.getStartNode().getExcess() - difference);
					}
				} else{
					double delta = Math.min(current.getExcess(), l.getCapacity() - l.getFlow());
					delta = Math.max(delta, 0);
					l.setFlow(l.getFlow() + delta);
					l.getEndNode().setExcess(l.getEndNode().getExcess() + delta);
					l.getStartNode().setExcess(l.getStartNode().getExcess() - delta);
					if(l.useFakeValues() && l.getFakeCapacity() > l.getCapacity()){
						double difference = Math.max(l.getFakeCapacity() - l.getFlow(), 0);
						l.getStartNode().setHiddenExcess(l.getStartNode().getHiddenExcess() + difference);
						l.getStartNode().setExcess(l.getStartNode().getExcess() - difference);
					}
					double returned = flow.recursivePush(flow, l.getEndNode(), sink, links);
					l.setFlow(l.getFlow() - returned);
					l.getEndNode().setExcess(l.getEndNode().getExcess() - returned);
					l.getStartNode().setExcess(l.getStartNode().getExcess() + returned);
				}
			}
		}
		return current.getExcess();
	}
	
	//push portion of the push-preflow algorithm
	public void push(Node i, Node j, Link l){
		if(i.getExcess() > 0 && i.getHeight() > j.getHeight()){
			double delta = Math.min(i.getExcess(), l.getCapacity() - l.getFlow());
			l.setFlow(l.getFlow() + delta);
			l.getEndNode().setExcess(l.getEndNode().getExcess() + delta);
		}
	}
	
	//relabel portion of the push-preflow algorithm
	public void relabel(Preflow flow, Node i){
		if(i.getExcess() > 0){
			boolean all_higher = true;
			for(Link l : flow.getLinkList()){
				if(l.getStartNode().equals(i) && l.getEndNode().getHeight() < l.getStartNode().getHeight()){
					all_higher = false;
				}
			}
			if(all_higher){
				i.setHeight(i.getHeight() + 1);
			}
		}
	}
	//finds node with excess < defunct>
	public Node excess(Preflow flow){
		List<Node> nodes = flow.getNodeList();
		int size = nodes.size();
		for(Node n : nodes){
			if(!n.equals(size - 1)){
				if(n.getExcess() > 0){
					for(Link l : flow.getLinkList()){
						if(l.getStartNode().equals(n)){
							if(l.getCapacity() - l.getFlow() != 0){
								return n;
							}
						}
					}
				}
			}
		}
		return null;
	}
	//finds a link to push more flow into <defunct>
	public Link findPushableLink(Preflow flow, Node n){
		List<Link> links = flow.getLinkList();
		for(Link l : links){
			if(l.getStartNode().equals(n)){
				if(l.getEndNode().getHeight() < n.getHeight() && l.getFlow() < l.getCapacity())
					return l;
			}
		}
		return null;
	}
	//returns a forward link if it isn't full <defunct>
	public Link emptyForwardLinkAvailable(Preflow flow, Node n){
		for(Link l : flow.getLinkList()){
			if(n.equals(l.getStartNode())){
				if(l.getFlow() < l.getCapacity()){
					return l;
				}
			}
		}
		return null;
	}
	
	//runs the recursive push-preflow push algorithm
	public double run_r(Preflow flow, double start){
		Node source = flow.getNodeList().get(0);
		flow.initialize(source, start);
		flow.recursivePush(flow, source, flow.getNodeList().get(flow.getNodeList().size() - 1), flow.getLinkList());
		return flow.getNodeList().get(flow.getNodeList().size() - 1).getExcess();
	}
	
	//runs preflow-push <defunct>
	public double run(Preflow flow, double start){
		Node source = flow.getNodeList().get(0);
		flow.initialize(source, start);
//		flow.recursivePush(flow, source, flow.getNodeList().get(flow.getNodeList().size() - 1), flow.getLinkList());
		
		Node n = excess(flow);
		int iteration = 0;
		for(Link l : flow.getLinkList()){
			boolean equals = l.getStartNode().equals(source);
			if(equals){
				flow.push(l.getStartNode(), l.getEndNode(), l);
			}
		}
		flow.getNodeList().get(0).setExcess(0);
		while(n != null && iteration < flow.getNodeList().size()*100){
			Link pushable = findPushableLink(flow, n);
			if(pushable != null){
	//			System.out.println(pushable.getStartNode().getName()+"->"+pushable.getEndNode().getName());
				flow.push(pushable.getStartNode(), pushable.getEndNode(), pushable);
			} else{
				flow.relabel(flow, n);
			}
			n = excess(flow);
			iteration++;
		}
		List<Node> fix = flow.getNodeList();
		for(Node node : fix){
			for(Link l : flow.getLinkList()){
				if(l.getStartNode().equals(node)){
//					System.out.println(n.getName()+": "+l.getFlow()+", "+n.getExcess());
					
				}
			}
		}
//		getRidOfExcess(flow, flow.getNodeList().get(flow.getNodeList().size() - 1));*/
		return flow.getNodeList().get(flow.getNodeList().size() - 1).getExcess();
	}
	//gets rid of the excess flow in a network
	//gets rid of the excess in nodes <defunct>
	public static void getRidOfExcess(Preflow flow, Node sink){
		List<Node> nodes = flow.getNodeList();
		List<Link> links = flow.getLinkList();
		Node current = sink;
		
		
		boolean finished = false;
		
		while(!finished){
			finished = true;
			for(Node n : nodes){
				if(n.equals(current)){
					for(Link l : links){
						if(l.getEndNode().equals(current)){
							if(l.getStartNode().getExcess() > 0){
								for(Link l2 : links){
									if(l2.getEndNode().equals(l.getStartNode())){
//										System.out.println(l.getStartNode().getExcess()+" | "+l2.toString()+" | "+l.toString());
										double delta = Math.min(l2.getFlow(), l.getStartNode().getExcess());
										l2.setFlow(l2.getFlow() - delta);
										l.getStartNode().setExcess(l.getStartNode().getExcess() - delta);
										finished = false;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	//pre-set up network for testing purposes
	public static void setUp(Preflow flow){
		List<Node> nodeList = new ArrayList<Node>();
		List<Link> linkList = new ArrayList<Link>();
		
		Node n0 = new Node();
		Node n1 = new Node(1, 0, 0);
		Node n2 = new Node(2, 0, 0);
		Node n3 = new Node(3, 0, 0);
		Node n4 = new Node(4, 0, 0);
		Node n5 = new Node(5, 0, 0);
		Node n6 = new Node(6, 0, 0);
		Node n7 = new Node(7, 0, 0);
		
		nodeList.add(n0);
		nodeList.add(n1);
		nodeList.add(n2);
		nodeList.add(n3);
		nodeList.add(n4);
		nodeList.add(n5);
		nodeList.add(n6);
		nodeList.add(n7);
		
		Link l0 = new Link(n0, n1, 1, 1, 1);
		Link l1 = new Link(n0, n2, 1, 1, 1);
		Link l2 = new Link(n0, n3, 1, 1, 1);
		Link l3 = new Link(n0, n4, 1, 1, 1);
		Link l4 = new Link(n1, n5, 1, 1, 1);
		Link l5 = new Link(n2, n5, 1, 1, 1);
		Link l6 = new Link(n3, n6, 1, 1, 1);
		Link l7 = new Link(n4, n6, 1, 1, 1);
		Link l8 = new Link(n5, n7, 1, 1, 1);
		Link l9 = new Link(n6, n7, 0.6, 1, 1);
		l9.setFakeFractionBound(1);
		l9.setFakeSecurityConstant(1);
		l9.setFakeCapacity(.5);
		l9.setFake(false);
		
		linkList.add(l0);
		linkList.add(l1);
		linkList.add(l2);
		linkList.add(l3);
		linkList.add(l4);
		linkList.add(l5);
		linkList.add(l6);
		linkList.add(l7);
		linkList.add(l8);
		linkList.add(l9);

		//Preflow flow = new Preflow();
		for(Node n : nodeList){
			flow.addNodeToList(n);
		}
		
		for(Link l : linkList){
			flow.addLinkToList(l);
		}
	}
	//pre set up network for testing purposes
	//pre-set up network for testing purposes
	public static void setUpTwo(Preflow flow){
		List<Node> nodeList = new ArrayList<Node>();
		List<Link> linkList = new ArrayList<Link>();
		
		Node n0 = new Node();
		Node n1 = new Node(1, 0, 0);
		
		nodeList.add(n0);
		nodeList.add(n1);
		
		Link l0 = new Link(n0, n1, 2, 1, 0.5);
		l0.setFakeFractionBound(1);
		l0.setFakeSecurityConstant(1);
		l0.setFakeCapacity(3);
		l0.setFake(true);
		
		linkList.add(l0);
		
		for(Node n : nodeList){
			flow.addNodeToList(n);
		}
		
		for(Link l : linkList){
			flow.addLinkToList(l);
		}
	}
	
	public static void main(String[] args) {
		Preflow flow = new Preflow();
		/*
		Node source = new Node(0, 0, 0);
		Node n1 = new Node(1, 0, 0);
		Node n2 = new Node(2, 0, 0);
		Link l1 = new Link(source, n1, 1, 1, 1);
		Link l2 = new Link(source, n2, 2, 1, 1);
		Link l4 = new Link(n1, n2, 1, 1, 1);
		Node sink = new Node(3, 0, 0);
		Link l3 = new Link(n1, sink, 1, 1, 1);
		Link l5 = new Link(n2, sink, 1, 1, 1);
		
		flow.addNodeToList(source);
		flow.addNodeToList(n1);
		flow.addNodeToList(n2);
		flow.addNodeToList(sink);
		
		flow.addLinkToList(l1);
		flow.addLinkToList(l2);
		flow.addLinkToList(l3);
		flow.addLinkToList(l4);
		flow.addLinkToList(l5);
		*/
		setUp(flow);
		
//		System.out.println(flow.run_r(flow, 1.67));
		System.out.println(flow.run_r(flow, 10));
		//getRidOfExcess(flow, flow.getNodeList().get(flow.getNodeList().size() - 1));
		
		System.out.println("----------------");
/*		for(Node n : flow.getNodeList()){
			System.out.println(n.toString());
		}*/
		for(Link l : flow.getLinkList()){
			l.calculateProportionData(flow.run_r(flow, 10));
			System.out.println(l.paperValues()+" "+l.getStartNode().getHiddenExcess());
		}
	}
}
