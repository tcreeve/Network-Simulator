import java.util.List;


public class Node {
	private int name;
	private double height;
	private double excess;
	
	private double hidden_excess;
	
	private double fs_val;
	private double fstar_val;
	
	public Node(){
		name = 0;
		height = 0;
		excess = 0;
		hidden_excess = 0;
	}
	
	public Node(int n, double h, double e){
		name = n;
		height = h;
		excess = e;
		hidden_excess = 0;
	}
	
	public int getName(){
		return name;
	}
	
	public void setName(int n){
		name = n;
	}
	
	public double getHeight(){
		return height;
	}
	
	public void setHeight(double h){
		height = h;
	}
	
	public double getExcess(){
		return excess;
	}
	
	public void setExcess(double e){
		excess = e;
	}
	
	public double getHiddenExcess(){
		return hidden_excess;
	}
	
	public void setHiddenExcess(double he){
		hidden_excess = he;
	}
	
	//broadcasts maximum flow value to neighboring links
	public void broadcast(List<Link> links, double fs){
		for(Link l : links){
			if(l.getStartNode().getName() == name){
				fs_val = fs;
				l.getEndNode().broadcast(links, fs);
			}
		}
	}
	
	public String toString(){
		return "name: "+name+"\theight: "+height+"\texcess: "+excess;
	}
}
