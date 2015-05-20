package can;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDProtocol;

public class CANProtocol implements EDProtocol{

	private HashMap<Integer, Integer[]> nodes;
	private Node root;
	
	private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
	private static int dim;
	
	public CANProtocol() {
		dim = Configuration.getInt(DIM_PROT);
	}
	
	@Override
	public Object clone(){
		Object clone = null;
		try {
			clone = super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return clone;
	}
	
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		
	}


	public void addNode(Node n) {
		CANNodeSpecs newSpecs = (CANNodeSpecs) n.getProtocol(dim);
		if (root == null){
			ArrayList<Double[]> nOwnershipArea = new ArrayList<Double[]>();
			for (int i = 0; i < newSpecs.getDimensions().length; i++) {
				Double[] t = {0.0,1.0};
				nOwnershipArea.add(t);
			}
			return;
		}
		CANNodeSpecs rootSpecs = (CANNodeSpecs) root.getProtocol(dim);
		if(rootSpecs.isOwnerOf(newSpecs)){
			newSpecs.getHalfZoneOf(rootSpecs);
		}
	}

}
