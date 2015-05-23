package can;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import algo.GSS;
import algo.Point;
import algo.Query;
import algo.Region;
import algo.SearchRegion;

import java.util.*;

public class CANProtocol implements EDProtocol{

	public HashMap<Long, Node> nodes;
	private Node root;

	private static final String PAR_PROT = "protocol";
//	private static final String DIM_PROT = "dimensions";
	private static final String SPEC_PROT = "nodespec";
	private static int dim;
	private static int spec;
	private ArrayList<Double[]> networkData;

	private static SearchRegion searchRegion;
	private static List<Region> processed;
	private static List<Point> points;

	private static boolean verbose = false;

	private HashSet<Long> nodesInvolved = new HashSet<>();;
	private int messageCount;

	public CANProtocol(String prefix) {
		if (verbose)
			System.out.println("Protocol loading...");
		spec = Configuration.getPid(prefix + "."+ SPEC_PROT);
//		dim = Configuration.getInt(prefix + "." + DIM_PROT);
		networkData = CANDataProvider.generateRandomDataset(1000);//loadData();
		nodes = new HashMap<>();
		if (verbose)
			System.out.println("Protocol loaded: "+dim);
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
		if (verbose)
			System.out.println("Event pid "+pid+", "+event);

		messageCount++;
		EventMessage msg = (EventMessage) event;
		if (verbose)
			System.out.println("Event msg: "+msg.type+", "+msg.o);
		if ("gss_init".equals(msg.type)) {
			searchRegion = null;
			processed = new ArrayList<>();
			points = new ArrayList<>();

			if (verbose)
				System.out.println("Query: "+msg.o);
			GSS gss = new GSS(node, pid, spec);
			gss.greedySkylineSearch(node, (Query) msg.o, null, 1);
			nodesInvolved.add(node.getID());
		} else if ("gss_region".equals(msg.type)) {
			SearchRegion sr = (SearchRegion) msg.o;
			searchRegion = sr;
		} else if ("gss_result".equals(msg.type)) {
			Object[] params = (Object[]) msg.o;
			List<Point> localSkylinePoints = (List<Point>) params[0];
			Region region = (Region) params[1];
			processed.add(region);
			if (verbose)
				System.out.println("Recieved results: "+localSkylinePoints.size() +" "+ points.size());
			/*for (Point sp : localSkylinePoints) {
				System.out.println(sp);
			}*/
			points.addAll(localSkylinePoints);

			if (searchRegion != null) {
				SearchRegion sr = searchRegion;
				for (Region r : processed) {
					sr = sr.subtract(r);
				}
				if (sr.regions.length == 0) {
					if (verbose) {
						System.out.println("Query done!");
						System.out.println("Skyline: "+points.size());
					}
					System.out.println(nodesInvolved.size());
					System.out.println(messageCount);
					/*for (Point p : points) {
						System.out.println("  "+p);
					}*/
				}
			}
		} else if ("gss_next".equals(msg.type)) {
			Object[] params = (Object[]) msg.o;
			GSS gss = new GSS((Node) params[0], pid, spec);
			gss.greedySkylineSearch(node, (Query) params[1], (SearchRegion) params[2], (int) params[3]);
			nodesInvolved.add(node.getID());
		} else {
			System.err.println("Unknown event: " + msg.type);
		}
		if (verbose)
			System.out.println();
	}


	public void addNode(Node n) {
		nodes.put(n.getID(), n);
		CANNodeSpecs newSpecs = (CANNodeSpecs) n.getProtocol(spec);
		if (root == null){
			ArrayList<Double[]> nOwnershipArea = new ArrayList<Double[]>();
			Double[] t_0 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			Double[] t_1 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
			nOwnershipArea.add(t_0);
			nOwnershipArea.add(t_1);
			newSpecs.setOwnershipArea(nOwnershipArea);
			newSpecs.setOwnedData(networkData);
			root = n;
			if (verbose)
				System.out.println("Adding root: "+root);
			return;
		}
		CANNodeSpecs ownerNode = (CANNodeSpecs) root.getProtocol(spec);
		HashSet<CANNodeSpecs> visitedNodes = new HashSet<CANNodeSpecs>();
		visitedNodes.add(ownerNode);
		while(!ownerNode.isOwnerOf(newSpecs)){
			//System.out.println(ownerNode.hashCode());
			ownerNode = ownerNode.findClosestNeighborTo(newSpecs,visitedNodes);
		}
		if (verbose) {
            System.out.println("Area: ");
            for (Double[] p : ownerNode.getOwnershipArea()) {
                System.out.println(Arrays.toString(p));
            }
            System.out.println("Spec: ");
            for (Double[] p : newSpecs.getOwnershipArea()) {
                System.out.println(Arrays.toString(p));
            }
        }
		newSpecs.getHalfZoneOf(ownerNode);
		newSpecs.setNodeId(n.getID());
		if (verbose) {
            System.out.println("New A: ");
            for (Double[] p : ownerNode.getOwnershipArea()) {
                System.out.println(Arrays.toString(p));
            }
            System.out.println("New B: ");
            for (Double[] p : newSpecs.getOwnershipArea()) {
                System.out.println(Arrays.toString(p));
            }
            System.out.println(ownerNode);
            System.out.println(newSpecs);
        }
	}

}
