import java.util.ArrayList;
import java.util.List;


public class World {

	//lex control algorithm as defined in Distributed Multipath Network paper
	public static double lexControlAlgorithm(double large_number, List<Node> nodeList, List<Link> linkList){
		double fstar = boundControlAlgorithm(large_number, nodeList, linkList);
		int iterations = 0;
		double old_fstar = -1;
		while(fstar < large_number-0.000001 && old_fstar != fstar && fstar>=0.000001){
			nodeList.get(0).broadcast(linkList, fstar);
			//create G_*
			List<Link> graph_flow = new ArrayList<Link>();
			for(Link l : linkList){
				l.calculateProportionData(fstar);
				if((l.getCapacity() - l.getFlow()) > 0){
					Link temp = new Link();
					temp.setCapacity(l.getCapacity());
					temp.setEndNode(l.getEndNode());
					temp.setFlow(l.getFlow());
					temp.setFractionBound(l.getFractionBound());
					temp.setProportionData(l.getProportionData());
					temp.setSecurityConstant(l.getSecurityConstant());
					temp.setStartNode(l.getStartNode());
					graph_flow.add(temp);
				} 
				if(l.getFlow() > 0){
					Link temp = new Link();
					temp.setCapacity(l.getCapacity());
					temp.setEndNode(l.getStartNode());
					temp.setFlow(l.getFlow());
					temp.setFractionBound(l.getFractionBound());
					temp.setProportionData(l.getProportionData());
					temp.setSecurityConstant(l.getSecurityConstant());
					temp.setStartNode(l.getEndNode());
					graph_flow.add(temp);
				}
			}
			for(Link l : linkList){
				if(!pathExists(0, l.getStartNode(), l.getStartNode(), l.getEndNode(), nodeList, graph_flow) && !sameLinkExists(l, graph_flow)){
					for(Link change : graph_flow){
						change.uncheck();
					}
					l.setFakeSecurityConstant(1/large_number);
					l.setFakeFractionBound((l.getFlow()+l.getStartNode().getHiddenExcess())/ fstar);
					l.setSecurityConstant(1/large_number);				
					l.setFractionBound(l.getFlow() / fstar);
				}
			}
			//reset network
			for(Node n : nodeList){
				n.setHiddenExcess(0);
			}
			old_fstar = fstar;
			fstar = boundControlAlgorithm(large_number, nodeList, linkList);
			iterations++;
		}
		return fstar;
	}
	//finds the critical links in a graph
	public static List<Link> returnCriticalLinks(List<Link> links, Node find){
		List<Link> critical_links = new ArrayList<Link>();
		for(Link l : links){
			if(l.getEndNode().equals(find)){
				critical_links.add(l);
			}
		}
		return critical_links;
	}
	//is a node reachable given the currents links?
	public static boolean reachable(Node start, Node end, List<Link> links){
		boolean found_start = false;
		boolean found_end = false;
		for(Link l : links){
			if(l.getEndNode().equals(end))
				found_end = true;
			if(l.getStartNode().equals(start))
				found_start = true;
		}
		if(found_start && found_end)
			return true;
		return false;
	}
	//sees if link is critical (defunct).
	public static boolean linkIsCritical(Link l, Node source, Node sink, List<Node> nodes, List<Link> links){
		boolean all_connected = pathExistsWithout(l, source, sink, nodes, links);
		if(all_connected)
			return true;
		return false;
	}
	//does the link exist in the network (defunt)
	public static boolean sameLinkExists(Link l, List<Link> links){
		for(Link link : links){
			if(l.getStartNode().equals(link.getStartNode())
					&& l.getEndNode().equals(link.getEndNode()))
				return true;
		}
		return false;
	}
	//sees if the path exists in a network
	public static boolean pathExists(double level, Node start, Node current, Node sink, List<Node> nodes, List<Link> links){
		if(level < 10){
			for(Link link : links){
				if(link.getStartNode().getName() == current.getName() && !link.getEndNode().equals(start)){
					if(link.getEndNode().getName() == sink.getName()){
						return true;
					} else{
						return pathExists(level+1, start, link.getEndNode(), sink, nodes, links);
					}	
				}
			}
		}
		return false;
	}
	//does the path exist with the given link (defunct)
	public static boolean pathExistsWithout(Link l, Node current, Node sink, List<Node> nodes, List<Link> links){
		for(Link link : links){
			if(!link.equals(l) && link.getStartNode().equals(current)){
				if(link.getEndNode().equals(sink))
					return true;
				else
					return pathExistsWithout(l, link.getEndNode(), sink, nodes, links);
			}
		}
		return false;
	}
	//funs the bound control algorithm as defined in the paper
	public static double boundControlAlgorithm(double large_number, List<Node> nodeList, List<Link> linkList){
		//set fs to a large number
		double fs = large_number;
		Preflow flow = new Preflow();
		
		//broadcast fs=U (large number)
		//line 1
		nodeList.get(0).broadcast(linkList, fs);
		
		//set cap(l) 
		//lines 2 - 4
		for(Node n : nodeList){
			for(Link l : linkList){
				if(l.getStartNode().equals(n)){
					l.setCapacity(Math.min(1/l.getSecurityConstant(), fs * l.getFractionBound()));
					l.setFakeCapacity(Math.min(1/l.getFakeSecurityConstant(), fs * l.getFakeFractionBound()));
					flow.addLinkToList(l);
				}
			}
			flow.addNodeToList(n);
		}
		
		//preflow push
		double value = flow.run_r(flow, fs);
		int iterations = 0;
		while(fs != value && value >=0.0001){
			iterations++;
			fs = value;
			
			nodeList.get(0).broadcast(linkList, fs);
			
			for(Node n : flow.getNodeList()){
				for(Link l : flow.getLinkList()){
					if(l.getStartNode().equals(n)){
						l.setCapacity(Math.min(1/l.getSecurityConstant(), fs * l.getFractionBound()));
						l.setFakeCapacity(Math.min(1/l.getFakeSecurityConstant(), fs * l.getFakeFractionBound()));
						l.setFlow(0);
					}
				//	System.out.println(l.toString());
				}
	//			System.out.println("+--------------------------------+"+iterations);
			}
			value = flow.run_r(flow, fs);
		//	System.out.println(value);
		}
		return value;
	}

	//comapres the links between two link lists
	public static void compareLinkListsProportionData(List<Link> l1, List<Link> l2){
		int num = 0; //edited
		boolean different = false;
		for(Link link : l1){
			for(Link compare : l2){
				if(link.getStartNode().equals(compare.getStartNode())
						&& link.getEndNode().equals(compare.getEndNode())){
					System.out.print(num+") "); //edited
					num++; //edited
					if(link.getProportionData() == compare.getProportionData()){
						System.out.println(link.getStartNode().getName()+"->"+
								link.getEndNode().getName()+": same ("
								+link.getProportionData()+")");
					} else{
						System.out.println(link.getStartNode().getName()+"->"+
								link.getEndNode().getName()+": different ( false:"
								+link.getProportionData()+"|truth: "
								+compare.getProportionData()+")");
						different = true;
					}
				}
			}
		}
		if(different)
			System.out.println("Different");
		else
			System.out.println("Same");
	}
	//tests the example from the paper with an additional liar node
	public static void paperNetworkWithLiar(){
		List<Node> nodeList = new ArrayList<Node>();
		List<Link> linkList = new ArrayList<Link>();
		List<Link> linkListTrue = new ArrayList<Link>();
		
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
		Link l9 = new Link(n6, n7, 0.4, 1, 0.4);
		l9.setFakeFractionBound(1);
		l9.setFakeSecurityConstant(1);
		l9.setFakeCapacity(1);
		l9.setFake(true);
		
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

		Preflow flow = new Preflow();
		for(Node n : nodeList){
			flow.addNodeToList(n);
		}
		
		for(Link l : linkList){
			flow.addLinkToList(l);
			linkListTrue.add(l.copyLink());
		}
		
		Preflow flow_true = new Preflow();
		
		for(Node n : nodeList){
			flow_true.addNodeToList(n);
		}
		
		for(Link l : linkListTrue){
			flow_true.addLinkToList(l);
		}
		
		
		double ans = lexControlAlgorithm(10, nodeList, linkList);
		double attack_cost = 1/ans;
		System.out.println("FALSE\tf:"+ans+"\tattack cost:"+attack_cost);
		/*
		for(Link l : linkList){
			l.calculateProportionData(ans);
			System.out.print(l.paperValues());
		}
		
		System.out.println("\n----------------------------\n----------------");*/
		double ans_true = lexControlAlgorithm(10, nodeList, linkListTrue);
		double attack_cost_true = 1/ans_true;
		System.out.println("TRUE\tf:"+ans_true+"\tattack cost:"+attack_cost_true);
/*		
		for(Link l : linkListTrue){
			l.calculateProportionData(ans);
			System.out.print(l.paperValues());
		}*/
		
		compareLinkListsProportionData(linkList, linkListTrue);
	}
	//generates networks according the double inserted and runs with a 
	//random liar node comparing it against a truthful network
	public static void generateNetwork(int num_nodes){
		List<Node> nodeList = new ArrayList<Node>();
		List<Link> liarLinks = new ArrayList<Link>();
		List<Link> truthLinks = new ArrayList<Link>();
		for(int i=0; i<num_nodes; i++){
			nodeList.add(new Node(i, 0, 0));
		}
		double sc = 0;
		while(sc <= 0)
			sc = Math.random();
		double bandwidth = ((int)(Math.random()*10))/10.0;
		while(bandwidth <= 0 && bandwidth >=.9){
			bandwidth = ((int)(Math.random()*10))/10.0;
		}
		double fake_bandwidth = ((int)(Math.random()*10))/10.0;;
		while(fake_bandwidth <= bandwidth){
			fake_bandwidth = ((int)(Math.random()*10))/10.0;
		}
		
		for(int i=0; i<num_nodes; i++){
			int links = 2 + (int)(Math.random()*4);
			for(int j=1; j<=links; j++){
				if(i+j < num_nodes){
					Link l_liar = new Link(nodeList.get(i), nodeList.get(i+j), 1, sc, bandwidth);
					Link l_truth = new Link(nodeList.get(i), nodeList.get(i+j), 1, sc, bandwidth);
					liarLinks.add(l_liar);
					truthLinks.add(l_truth);
				}
			}
		}
		
		int liar_num = (int)(Math.random()*liarLinks.size());
		liarLinks.get(liar_num).setFakeFractionBound(fake_bandwidth);
		liarLinks.get(liar_num).setFake(true);
		
		Preflow truth = new Preflow();
		Preflow liar = new Preflow();
		
		for(Node n : nodeList){
			truth.addNodeToList(n);
			liar.addNodeToList(n);
		}
		for(Link l : liarLinks){
			liar.addLinkToList(l); 
		}
	
		for(Link l : truthLinks){
			truth.addLinkToList(l);
		}
		
		System.out.println("security: "+sc+"\nbandwidth:"+bandwidth);
		System.out.println(liar_num+"->"+fake_bandwidth);
		System.out.println("Percentage of true: "+(fake_bandwidth / bandwidth));
		
		double ans = lexControlAlgorithm(10, nodeList, liarLinks);
		double attack_cost = 1/ans;
		System.out.println("FALSE\tf:"+ans+"\tattack cost:"+attack_cost);
	
		double ans_true = lexControlAlgorithm(10, nodeList, truthLinks);
		double attack_cost_true = 1/ans_true;
		System.out.println("TRUE\tf:"+ans_true+"\tattack cost:"+attack_cost_true);
		
		compareLinkListsProportionData(liarLinks, truthLinks);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	paperNetworkWithLiar();
		//change the number in the function below in order to change
		//the number of nodes in the network
		//generateNetwork(<number of nodes in network>);
		generateNetwork(20);
	}
}
