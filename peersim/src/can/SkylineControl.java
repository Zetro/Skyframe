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

    private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
    private static int pid;
    private static int dim;

	public SkylineControl(String prefix) {
        System.out.println("Initializing skyline search: "+prefix);
        pid = Configuration.getPid(prefix + "."+ PAR_PROT);
        dim = Configuration.getPid(prefix + "."+ DIM_PROT);
        System.out.println("Initialized: "+pid+", "+dim);
	}

    @Override
    public boolean execute() {
        for(int i=0; i< Network.size(); i++){
            Node n = Network.get(i);
            n = Network.get(i);
            System.out.println("Node: "+n);
            CANProtocol cp = (CANProtocol) n.getProtocol(pid);
            CANNodeSpecs cd = (CANNodeSpecs) n.getProtocol(dim);
            List<Double[]> area = cd.getOwnershipArea();
            System.out.println("Area: ");
            for (Double[] p : area) {
                System.out.println(Arrays.toString(p));
            }
            System.out.println("Nodes: "+cp.nodes.size());
        }

        Node node = Network.get(0);
        EventMessage msg = new EventMessage("gss_init", new Query(
            Query.Component.Min,
            Query.Component.Min,
            Query.Component.Min,
            Query.Component.Min,
            Query.Component.Min,
            Query.Component.Min));
        EDSimulator.add(1, msg, node, pid);
        return false;
    }
}
