package can;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class CANNodeInitializer implements NodeInitializer, Control{

    private static boolean verbose = false;

    private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
    private static int pid;
    private static int dim;
    private CANProtocol cp;
	private CANNodeSpecs cd;

	public CANNodeInitializer(String prefix) {
        if (verbose)
            System.out.println("Initializing node: "+prefix);
    	pid = Configuration.getPid(prefix + "."+ PAR_PROT);
    	dim = Configuration.getPid(prefix + "."+ DIM_PROT);
        if (verbose)
            System.out.println("Initialized: "+pid+", "+dim);
	}

	@Override
	public void initialize(Node n) {
    	cp = (CANProtocol) n.getProtocol(pid);
    	cd = (CANNodeSpecs) n.getProtocol(dim);
    	cd.setLocation(CANDataProvider.nextNodeInfo());
    	cp.addNode(n);
	}

    @Override
    public boolean execute() {
        cp = (CANProtocol) Network.get(0).getProtocol(pid);
        for(int i=0; i< Network.size(); i++){
            Node n = Network.get(i);
            if (verbose)
                System.out.println("Initializing node: "+n);
            cd = (CANNodeSpecs) n.getProtocol(dim);
            cd.setLocation(CANDataProvider.nextNodeInfo());
            cp.addNode(n);
        }
        if (verbose)
            System.out.println("All nodes initialized");
        return false;
    }
}
