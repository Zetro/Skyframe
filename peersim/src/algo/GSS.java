package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import can.CANNodeSpecs;
import can.CANProtocol;
import can.EventMessage;
import java.util.Set;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

// todo: s/Object/CANNodeSpecs/
public class GSS {

	private static boolean verbose = false;

	public static int pid, spec;
	public Node qi; // query initiator

	public GSS(Node qi, int pid, int spec) {
		this.qi = qi;
		this.pid = pid;
		this.spec = spec;
	}

	public Node getNode(CANNodeSpecs n) {
		CANProtocol cp = (CANProtocol) Network.get(0).getProtocol(pid);
		return cp.nodes.get(n.getNodeId());
	}

	public void sendRSS(Object n, Query q, int dim, SearchRegion sr, int p, Set<Long> sp) {
		Node node = getNode((CANNodeSpecs) n);
		EventMessage msg = new EventMessage("rss_next", new Object[]{qi,q,dim,sr,p,sp});
		EDSimulator.add(1, msg, node, pid);
	}
	public void sendGSS(Object n, Query q, SearchRegion sr, int p) {
		Node node = getNode((CANNodeSpecs) n);
		EventMessage msg = new EventMessage("gss_next", new Object[]{qi,q,sr,p});
		EDSimulator.add(1, msg, node, pid);
	}
	public void sendResult(Node node, List<Point> localSkylinePoints, Region region) {
		EventMessage msg = new EventMessage("gss_result", new Object[]{localSkylinePoints,region});
		EDSimulator.add(1, msg, node, pid);
	}
	public void sendRSSResult(Node node, List<Point> localSkylinePoints, CANNodeSpecs source) {
		EventMessage msg = new EventMessage("rss_result", new Object[]{localSkylinePoints,source});
		EDSimulator.add(1, msg, node, pid);
	}

	public void greedySkylineSearch(Node node, Query q, SearchRegion sr, int p) {
		CANNodeSpecs n = (CANNodeSpecs) node.getProtocol(spec);
		greedySkylineSearch(n, q, sr, p);
	}
	public void relaxedSkylineSearch(Node node, Query q, int d, SearchRegion sr, int p, Set<Long> sp) {
		CANNodeSpecs n = (CANNodeSpecs) node.getProtocol(spec);
		relaxedSkylineSearch(n, q, d, sr, p, sp);
	}

	public void greedySkylineSearch(CANNodeSpecs n, Query q, SearchRegion sr, int p) {
		if (verbose)
			System.out.println("Running GSS (phase "+p+") "+n);
		if (n == null) {
			System.err.println("Node is null!");
			return;
		}

		if (p == 1) {
			if (isSqStarter(n, q)) {
				List<Point> localSkylinePoints = computeSkylinePoints(n, q);
				Point p_md = comptePmd(localSkylinePoints, q);
				if (verbose)
					System.out.println("pmd: "+p_md);
				SearchRegion SR = computeSearchRegion(p_md, q);
				//System.out.println(SR);
				EventMessage msg = new EventMessage("gss_region", SR);
				EDSimulator.add(0, msg, qi, pid);
				// Partition SR into a disjoint set of subSRs for neighbornodes in RT(n)
				Map<Object, SearchRegion> partition = partition(SR, n);
				for (Object m : routing_table(n)) {
					SearchRegion subSR = partition.get(m);
					if (isInChargeOf(m, subSR)) {
						sendGSS(m, q, subSR, 2);
					}
				}
				// return local skyline points
				sendResult(qi, localSkylinePoints, getRegion(n));
			} else {
				Object x = findNearerNode(n, q);
				sendGSS(x, q, sr, 1);
			}
		} else if (p == 2) {
			List<Point> localSkylinePoints = computeSkylinePoints(n, q);
			// return local skyline points
			sendResult(qi, localSkylinePoints, getRegion(n));
			if (!sr.isCoveredBy(getRegion(n))) {
				SearchRegion SR = sr.subtract(getRegion(n));
				Map<Object, SearchRegion> partition = partition(SR, n);
				for (Object m : routing_table(n)) {
					SearchRegion subSR = partition.get(m);
					if (isInChargeOf(m, subSR)) {
						sendGSS(m, q, subSR, 2);
					}
				}
			}
		}
	}

	public void relaxedSkylineSearch(CANNodeSpecs n, Query q, int d, SearchRegion sr, int p, Set<Long> sp) {
		if (verbose)
			System.out.println("Running RSS (phase "+p+") "+n);
		if (n == null) {
			System.err.println("Node is null!");
			return;
		}

		if (p == 1) {
			if (d == -1) {
				for (int dim=0; dim<q.dims.length; dim++) {
					CANNodeSpecs x = findNearerToBorder(n, q, dim);
					Set<Long> set = new HashSet<>();
					set.add(x.getNodeId());
					sendRSS(x, q, dim, null, 1, set);
				}
			} else if (isBorderNode(n, q, d)) {
				List<Point> localSkylinePoints = computeSkylinePoints(n, q);
				// return local skyline points
				sendRSSResult(qi, localSkylinePoints, n);
				// for all neighbor Border Node nn at dimension d
				for (CANNodeSpecs nn : routing_table(n)) {
					if (isBorderNode(nn, q, d)) {
						// if nn does not exist in the search path from the QUERY INITIATOR to n
						if (!sp.contains(nn.getNodeId())) {
							sp.add(nn.getNodeId());
							sendRSS(nn, q, d, null, 1, sp);
						}
					}
				}
			} else {
				CANNodeSpecs x = findNearerToBorder(n, q, d);
				Set<Long> set = new HashSet<>();
				set.add(x.getNodeId());
				sendRSS(x, q, d, null, 1, set);
			}
		} else if (p == 2) {
			List<Point> localSkylinePoints = computeSkylinePoints(n, q);
			boolean any = false;
			for (int dim=0; dim<q.dims.length; dim++) {
				if (isBorderNode(n, q, dim)) {
					any = true;
					break;
				}
			}
			if (!any) {
				// return local skyline points
				sendRSSResult(qi, localSkylinePoints, n);
			}
			if (!sr.isCoveredBy(getRegion(n))) {
				SearchRegion SR = sr.subtract(getRegion(n));
				Map<Object, SearchRegion> partition = partition(SR, n);
				for (Object m : routing_table(n)) {
					SearchRegion subSR = partition.get(m);
					if (isInChargeOf(m, subSR)) {
						sendRSS(m, q, d, subSR, 2, sp);
					}
				}
			}
		}
	}
	
	public static Region getRegion(Object node) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		List<Double[]> ownershipArea = n.getOwnershipArea();
		Range[] ranges = new Range[ownershipArea.get(0).length];
		for (int i=0; i<ranges.length; i++) {
			ranges[i] = new Range(ownershipArea.get(0)[i], ownershipArea.get(1)[i]);
		}
		return new Region(ranges);
	}

	public static boolean isSqStarter(Object node, Query q) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		Region region = getRegion(n);
		for (int i=0; i<q.dims.length; i++) {
			if (q.dims[i] == Query.Component.Min && region.dims[i].low > 0)
				return false;
			else if (q.dims[i] == Query.Component.Max && region.dims[i].high < 1)
				return false;
		}
		return true;
	}
	
	public static boolean isBorderNode(Object node, Query q, int d) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		Region region = getRegion(n);
		if (q.dims[d] == Query.Component.Min && region.dims[d].low > 0)
			return false;
		else if (q.dims[d] == Query.Component.Max && region.dims[d].high < 1)
			return false;
		return true;
	}

	public static boolean isInChargeOf(Object node, SearchRegion sr) {
		 // todo? same as in python version but makes no sense
		Region nodeRegion = getRegion(node);
		for (Region r: sr.regions) {
			if (r.intersect(nodeRegion) != null) {
				return true;
			}
		}
		return false;
	}

	public static CANNodeSpecs findNearerToBorder(CANNodeSpecs node, Query q, int d) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		Region region = getRegion(n);
		for (CANNodeSpecs m : n.getNeighbors()) {
			if (q.dims[d] == Query.Component.Min && getRegion(m).dims[d].low >= region.dims[d].low)
				continue;
			else if (q.dims[d] == Query.Component.Max && getRegion(m).dims[d].high <= region.dims[d].high)
				continue;
			return m;
		}
		return node;
	}
	
	public static Object findNearerNode(Object node, Query q) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		Region region = getRegion(n);
		neighbor:
		for (CANNodeSpecs m : n.getNeighbors()) {
			for (int i=0; i<q.dims.length; i++) {
				if (q.dims[i] == Query.Component.Min && getRegion(m).dims[i].low > region.dims[i].low)
					continue neighbor;
				else if (q.dims[i] == Query.Component.Max && getRegion(m).dims[i].high < region.dims[i].high)
					continue neighbor;
			}
			return m;
		}
		System.out.println("No nearer node!");
		for (CANNodeSpecs m : n.getNeighbors()) {
			System.out.println(m);
		}
		return null;
	}

	public static List<CANNodeSpecs> routing_table(Object node) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		List<CANNodeSpecs> neighbors = new ArrayList<>();
		for (CANNodeSpecs neighbor : n.getNeighbors()) {
			neighbors.add(neighbor);
		}
		return neighbors;
	}

	public static List<Point> todoGetTuplesFromNode(Object node) {
		CANNodeSpecs n = (CANNodeSpecs) node;
		List<Point> points = new ArrayList<>();
		for (Double[] data : n.getOwnedData()) {
			double[] data2 = new double[data.length];
			for (int i=0; i<data.length; i++) {
				data2[i] = data[i];
			}
			points.add(new Point(data2));
		}
		return points;
	}

	public static Map<Object, SearchRegion> partition(SearchRegion sr, Object n) {
		Map<Object, SearchRegion> partition = new HashMap<>();
		// give a sub search region to the current node
		partition.put(n, sr.intersect(getRegion(n)));
		sr = sr.subtract(getRegion(n));
		// give a sub search region to each of its neighbors
		for (Object m : routing_table(n)) {
			partition.put(m, sr.intersect(getRegion(m)));
			sr = sr.subtract(getRegion(m));
		}
		// merge the unallocated of the search region with suitable neighbors
		for (Object m : routing_table(n)) {
			if (partition.get(m).regions.length != 0) {
				SearchRegion[] res = computeConnectedRegion(partition.get(m), sr);
				sr = res[1];
				partition.put(m, partition.get(m).union(res[0]));
			}
		}
		return partition;
	}

	public static SearchRegion[] computeConnectedRegion(SearchRegion sr, SearchRegion unallocated) {
		SearchRegion connected = new SearchRegion();
		for (Region a : sr.regions) {
			for (Region b : unallocated.regions) {
				if (a.borders(b)) {
					connected = connected.union(b);
					unallocated = unallocated.subtract(b);
				}
			}
		}
		if (unallocated.regions.length == 0 || connected.regions.length == 0) {
			return new SearchRegion[] {connected, unallocated};
		}
		SearchRegion[] res = computeConnectedRegion(connected, unallocated);
		connected = connected.union(res[0]);
		return new SearchRegion[] {connected,res[1]};
	}

	public static List<Point> computeSkylinePoints(Object n, Query q) {
		List<Point> skyline = new ArrayList<>();
		List<Point> points = todoGetTuplesFromNode(n);
		for (Point p : points) {
			if (q.isSkyLinePoint(p, points))
				skyline.add(p);
		}
		return skyline;
	}

	public static Point comptePmd(List<Point> points, Query q) {
		if (points.isEmpty()) {
			System.err.println("Warning: no p_md!");
			return null;
		}

		double max_score = -1;
		Point max_point = null;
		for (Point p : points) {
			Region dominating = computeDominatingRegion(p, q);
			double score = 0;
			for (int i=0; i<q.dims.length; i++) {
				score += dominating.dims[i].high-dominating.dims[i].low;
			}
			if (score > max_score) {
				max_score = score;
				max_point = p;
			}
		}

		return max_point;
	}

	public static Region computeDominatingRegion(Point p, Query q) {
		Range[] dimensions = new Range[q.dims.length];
		for (int i=0; i<q.dims.length; i++) {
			if (q.dims[i] == Query.Component.Min)
				dimensions[i] = new Range(p.get(i), 1);
			else if (q.dims[i] == Query.Component.Max)
				dimensions[i] = new Range(0, p.get(i));
		}
		return new Region(dimensions);
	}

	public static SearchRegion computeSearchRegion(Point pmd, Query q) {
		Range[] full_ranges = new Range[q.dims.length];
		for (int i=0; i<q.dims.length; i++) {
			full_ranges[i] = new Range(0, 1);
		}
		SearchRegion full = new SearchRegion(new Region(full_ranges));
		if (pmd == null)
			return full;
		Region dominating = computeDominatingRegion(pmd, q);
		SearchRegion sr = full.subtract(dominating);
		return sr;
	}
}
