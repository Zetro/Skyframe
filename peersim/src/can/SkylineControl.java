package can;

import java.util.*;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDSimulator;

import algo.Query;

public class SkylineControl implements Control {

    private static boolean verbose = false;

    private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
    private static int pid;
    private static int dim;

	public SkylineControl(String prefix) {
        if (verbose)
            System.out.println("Initializing skyline search: "+prefix);
        pid = Configuration.getPid(prefix + "."+ PAR_PROT);
        dim = Configuration.getPid(prefix + "."+ DIM_PROT);
        if (verbose)
            System.out.println("Initialized: "+pid+", "+dim);
	}

    @Override
    public boolean execute() {
        if (!true) return false;
        for(int i=0; i< Network.size(); i++){
            Node n = Network.get(i);
            if (verbose)
                System.out.println("Node: "+n);
            CANProtocol cp = (CANProtocol) n.getProtocol(pid);
            CANNodeSpecs cd = (CANNodeSpecs) n.getProtocol(dim);
            List<Double[]> area = cd.getOwnershipArea();
            if (verbose) {
                System.out.println("Node: "+cd);
                System.out.println("Area: ");
                for (Double[] p : area) {
                    System.out.println(Arrays.toString(p));
                }
                System.out.println("Neighbors: ");
                for (CANNodeSpecs m : cd.getNeighbors()) {
                    System.out.println(m);
                }
                System.out.println();
            }
        }

        Node node = Network.get(0);
        Query.Component[] query = new Query.Component[Configuration.getInt(DIM_PROT)];
        for (int i=0; i< query.length; i++) {
        	query[i] = Query.Component.Min;
        }
        EventMessage msg = new EventMessage("gss_init", new Query(query));
        EDSimulator.add(1, msg, node, pid);
        return false;
    }
}
