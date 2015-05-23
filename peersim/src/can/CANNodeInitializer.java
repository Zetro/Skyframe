package can;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class CANNodeInitializer implements NodeInitializer{

    private static final String PAR_PROT = "protocol";
	private static final String DIM_PROT = "dimensions";
    private static int pid;
    private static int dim;
    private CANProtocol cp;
	private CANNodeSpecs cd;

	public CANNodeInitializer(String prefix) {
    	pid = Configuration.getPid(prefix + "."+ PAR_PROT);
    	dim = Configuration.getPid(prefix + "."+ DIM_PROT);
	}

	@Override
	public void initialize(Node n) {
    	cp = (CANProtocol) n.getProtocol(pid);
    	cd = (CANNodeSpecs) n.getProtocol(dim);
    	cd.setLocation(CANDataProvider.nextNodeInfo());
    	cp.addNode(n);
	}

}
