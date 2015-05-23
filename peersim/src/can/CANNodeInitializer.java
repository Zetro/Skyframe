package can;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class CANNodeInitializer implements NodeInitializer, Control{

    private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
    private static int pid;
    private static int dim_protocol;
    private CANProtocol cp;
	private CANNodeSpecs cd;
	private int dim;

	public CANNodeInitializer(String prefix) {
        System.out.println("Initializing node: "+prefix);
    	pid = Configuration.getPid(prefix + "."+ PAR_PROT);
    	dim_protocol = Configuration.getPid(prefix + "."+ DIM_PROT);
    	dim = Configuration.getInt(DIM_PROT);
        System.out.println("Initialized: "+pid+", "+dim_protocol);
	}

	@Override
	public void initialize(Node n) {
        System.out.println("Initializing node?: "+n);
    	cp = (CANProtocol) n.getProtocol(pid);
    	cd = (CANNodeSpecs) n.getProtocol(dim_protocol);
    	cd.setLocation(CANDataProvider.nextNodeInfo(dim));
    	cp.addNode(n);
	}

    @Override
    public boolean execute() {
        cp = (CANProtocol) Network.get(0).getProtocol(pid);
        for(int i=0; i< Network.size(); i++){
            Node n = Network.get(i);
            System.out.println("Initializing node: "+n);
            cd = (CANNodeSpecs) n.getProtocol(dim_protocol);
            cd.setLocation(CANDataProvider.nextNodeInfo(dim));
            cp.addNode(n);
        }
        System.out.println("All nodes initialized");
        return false;
    }
}
