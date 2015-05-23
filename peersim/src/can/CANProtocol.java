package can;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class CANProtocol implements EDProtocol{

	private HashMap<Long, Node> nodes;
	private Node root;
	
	private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
	private static int dim;
	private ArrayList<Double[]> networkData;
	
	public CANProtocol() {
		dim = Configuration.getInt(DIM_PROT);
		networkData = CANDataProvider.loadData();
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
			Double[] t_0 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			Double[] t_1 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
			nOwnershipArea.add(t_0);
			nOwnershipArea.add(t_1);
			newSpecs.setOwnershipArea(nOwnershipArea);
			newSpecs.setOwnedData(networkData);
			root = n;
			nodes.put(n.getID(), n);
			return;
		}
		CANNodeSpecs ownerNode = (CANNodeSpecs) root.getProtocol(dim);
		while(!ownerNode.isOwnerOf(newSpecs)){
			ownerNode = ownerNode.findClosestNeighborTo(newSpecs);
		}
		newSpecs.getHalfZoneOf(ownerNode);
	}

}
