
public class Link {
	private Node start;
	private Node end;
	private double capacity;
	private double flow;
	
	private double security_constant;
	private double fraction_bound;
	
	private double proportion_data;
	
	private boolean checked;
	
	private double fake_fraction_bound;
	private double fake_security_constant;
	private double fake_capacity;
	private boolean fake;
	
	public Link(){
		start = new Node();
		end = new Node();
		capacity = 0;
		flow = 0;
		security_constant = 1;
		fraction_bound = 1;
		proportion_data = 0;
		fake_fraction_bound = 1;
		fake_security_constant = 1;
		fake_capacity = 0;
		fake = false;
		checked = false;
	}
	
	public Link(Node s, Node e, double c, double sc, double fb){
		start = s;
		end = e;
		capacity = c;
		flow = 0;
		security_constant = sc;
		fraction_bound = fb;
		fake_fraction_bound = fb;
		fake_security_constant = sc;
		fake_capacity = c;
		fake = false;
		checked = false;
	}
	
	public Node getStartNode(){
		return start;
	}
	
	public void setStartNode(Node s){
		start = s;
	}
	
	public Node getEndNode(){
		return end;
	}
	
	public void setEndNode(Node e){
		end = e;
	}
	
	public double getCapacity(){
		if(fake && fake_capacity < capacity)
			return fake_capacity;
		return capacity;
	}
	
	public void setCapacity(double c){
		capacity  = c;
	}
	
	public double getFlow(){
		return flow;
	}
	
	public void setFlow(double f){
		flow = f;
	}
	
	public double getSecurityConstant(){
		return security_constant;
	}
	
	public void setSecurityConstant(double sc){
		security_constant = sc;
	}
	
	public double getFractionBound(){
		return fraction_bound;
	}
	
	public void setFractionBound(double fb){
		fraction_bound = fb;
	}
	
	public double getProportionData(){
		return proportion_data;
	}
	
	public void setProportionData(double pd){
		proportion_data = pd;
	}
	
	public void calculateProportionData(double max_flow){
		proportion_data = flow / max_flow;
	}
	
	public double getFakeFractionBound(){
		return fake_fraction_bound;
	}
	
	public void setFakeFractionBound(double ffb){
		fake_fraction_bound = ffb;
	}
	
	public double getFakeSecurityConstant(){
		return fake_security_constant;
	}
	
	public void setFakeSecurityConstant(double fsc){
		fake_security_constant = fsc;
	}
	
	public double getFakeCapacity(){
		return fake_capacity;
	}
	
	public void setFakeCapacity(double fc){
		fake_capacity = fc;
	}
	
	public boolean useFakeValues(){
		return fake;
	}
	
	public void setFake(boolean use){
		fake = use;
	}
	
	public String paperValues(){
		return "Node "+start.getName()+" to Node "+end.getName()+": ("
				+proportion_data+", "+flow+", "+fraction_bound+")";
	}
	
	public Link copyLink(){
		return new Link(start, end, capacity, security_constant, fraction_bound);
	}
	
	public boolean returnChecked(){
		return checked;
	}
	
	public void check(){
		checked = true;
	}
	
	public void uncheck(){
		checked = false;
	}
	//compares the real and fake capacity of the link
	public String compareRealAndFake(){
		String s = "";
		if(capacity == fake_capacity){
			s+="capacity: same ("+capacity+")\n";
		} else {
			s+="capacity: "+fake_capacity+"/"+capacity+"\n";
		}
		
		if(fake_fraction_bound == fraction_bound){
			s+="fraction bound: same ("+fraction_bound+")\n";
		} else {
			s+="fraction bound: "+fake_fraction_bound+"/"+fraction_bound+"\n";
		}
		
		if(fake_security_constant == security_constant){
			s+="security constant: same ("+security_constant+")\n";
		} else {
			s+="security constant: "+fake_security_constant+"/"+security_constant+"\n";
		}
		return s;
	}
	
	public String toString(){
		return "Node "+start.getName()+" to Node "+end.getName()+"\tcapacity: "+capacity
				+"\tflow: "+flow+"\tsecurity constant:"+security_constant
				+"\tfraction bound:"+fraction_bound+"\tproportion data:"
				+proportion_data;
	}
}
