package can;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class WireCAN extends WireGraph{

    private static final String PAR_ALPHA = "alpha";
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
	
    
//    private final double alpha;
    private final int coordPid;

	protected WireCAN(String prefix) {
		super(prefix);
//		alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA, 0.5);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
	}

	@Override
	public void wire(Graph g) {
        int size = Network.size();
		int[] hops = new int[size];
        for (int i = 0; i < size; i++) {
            Node n = (Node) g.getNode(i);
            int candidate_index = 0;
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < i; j++) {
                Node parent = (Node) g.getNode(j);
                double jHopDistance = hops[j];
                double value = jHopDistance + (/**alpha **/ distance(n, parent, coordPid));
                if (value < min) {
                    min = value;
                    candidate_index = j;
                }
            }
            hops[i] = hops[candidate_index] + 1;
            g.setEdge(i, candidate_index);
        }
	}

	private double distance(Node n_1, Node n_2, int coordPid2) {
		Double[] dimensions_1 = ((CANNodeSpecs) n_1.getProtocol(coordPid)).getDimensions();
		Double[] dimensions_2 = ((CANNodeSpecs) n_2.getProtocol(coordPid)).getDimensions();
//        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
//            throw new RuntimeException("Found un-initialized coordinate. Use e.g.,InetInitializer class in the config file.");
		double dist = 1;
		for (int i = 0; i < dimensions_2.length; i++) {
			dist*= Math.pow((dimensions_1[i] - dimensions_2[i]),2); 
		}
        return Math.sqrt(dist);
	}

}
