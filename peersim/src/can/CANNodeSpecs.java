package can;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

import peersim.core.Protocol;

public class CANNodeSpecs implements Protocol{

	private long nodeId;
	private Double[] location;
	private int depth;
	private ArrayList<Double[]> ownershipArea;
	private ArrayList<Double[]> ownedData;
	private ArrayList<CANNodeSpecs> neighbors;

	public CANNodeSpecs(String prefix) {
		setLocation(new Double[6]);
		for (int i = 0; i < location.length; i++) {
			location[i] = -1.0;
		}
		depth = 0;
		ownershipArea = new ArrayList<Double[]>();
		ownedData = new ArrayList<Double[]>();
		setNeighbors(new ArrayList<CANNodeSpecs>());
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

	public boolean isOwnerOf(CANNodeSpecs n) {
		Double[] nLocation = n.getLocation();
		Double[] x_0 = ownershipArea.get(0);
		Double[] x_1 = ownershipArea.get(1);
		for (int i = 0; i < nLocation.length; i++) {
			Double coord_i = nLocation[i];
			if(coord_i < x_0[i] || coord_i > x_1[i]) return false;
		}
		return true;
	}

	public void getHalfZoneOf(CANNodeSpecs ownerSpecs) {
		Double[] ownerLocation = ownerSpecs.getLocation();
		ArrayList<Double[]> ownerArea = ownerSpecs.getOwnershipArea();
		/*int bestDimension = -1;
		double dimDivision = Double.MAX_VALUE;
		for (int i = 0; i < ownerLocation.length; i++) {
			if(ownerLocation[i] == location[i]) continue;
			double t = (ownerLocation[i] + location[i])/2;
			double x_0 = (ownerArea.get(0))[i];
			double x_1 = (ownerArea.get(1))[i];
			double center = (x_0 + x_1)/2;
			double distanceToCenter = Math.abs(center - t);
			if(distanceToCenter < Math.abs(center - dimDivision)){
				bestDimension = i;
				dimDivision = t;
			}
		}*/
		int bestDimension = (ownerSpecs.depth + 1) % ownerLocation.length;
		double dimDivision = (ownerLocation[bestDimension] + location[bestDimension]) / 2;
		//In case two node locations repeat. We can deal with this better later on.
		if (bestDimension == -1) throw new RuntimeException("Two nodes at same position");
		getOwnershipArea();

		ArrayList<Double[]> myArea = (ArrayList<Double[]>) ownerArea.clone();
		myArea = new ArrayList<>();
		for (Double[] p : ownerArea) {
			Double[] newp = new Double[p.length];
			for (int i=0; i<p.length; i++) {
				newp[i] = p[i];
			}
			myArea.add(newp);
		}
		if (location[bestDimension] < dimDivision) {
			myArea.get(1)[bestDimension] = dimDivision;
			ownerArea.get(0)[bestDimension] = dimDivision;
		} else {
			myArea.get(0)[bestDimension] = dimDivision;
			ownerArea.get(1)[bestDimension] = dimDivision;
		}
		ownershipArea = myArea;
		ownerSpecs.giveDataTo(this);
		ownerSpecs.getNeighbors().add(this);
		ownerSpecs.giveNeighborsTo(this);
		neighbors.add(ownerSpecs);
		ownerSpecs.depth += 1;
		depth = ownerSpecs.depth;
	}

	private void giveNeighborsTo(CANNodeSpecs newOwner) {
		ArrayList<CANNodeSpecs> newOwnerNeighbors = new ArrayList<CANNodeSpecs>();
		ListIterator<CANNodeSpecs> iterator = neighbors.listIterator();
		while(iterator.hasNext()){
			CANNodeSpecs next = iterator.next();
			if(areNeighbors(newOwner, next)){
				newOwnerNeighbors.add(next);
				next.getNeighbors().add(newOwner);
			}
			if(!areNeighbors(next, this)){
				next.getNeighbors().remove(this);
				iterator.remove();
			}
		}
		newOwner.setNeighbors(newOwnerNeighbors);
	}

	private static boolean areNeighbors(CANNodeSpecs a, CANNodeSpecs b) {
		ArrayList<Double[]> ownershipAreaOne = a.getOwnershipArea();
		ArrayList<Double[]> ownershipAreaTwo = b.getOwnershipArea();
		Double[] a_min = ownershipAreaOne.get(0);
		Double[] a_max = ownershipAreaOne.get(1);
		Double[] b_min = ownershipAreaTwo.get(0);
		Double[] b_max = ownershipAreaTwo.get(1);
		boolean foundBorder = false;
		for (int i = 0; i < b_max.length; i++) {
			if(Math.min(a_max[i], b_max[i]) == Math.max(a_min[i], b_min[i])){
				if (foundBorder) {
					return false;
				}
				foundBorder = true;
			}
			if(Math.min(a_max[i], b_max[i]) < Math.max(a_min[i], b_min[i])){
				  return false;
			}
		}
		return foundBorder;
	}

	private void giveDataTo(CANNodeSpecs newOwner) {
		ArrayList<Double[]> giveAwayData = new ArrayList<Double[]>();
		ArrayList<Double[]> newOwnerArea = newOwner.getOwnershipArea();
		Double[] lowerBound = newOwnerArea.get(0);
		Double[] upperBound = newOwnerArea.get(1);
		ListIterator<Double[]> iterator = ownedData.listIterator();
		while(iterator.hasNext()) {
			Double[] data = iterator.next();
			boolean belongsToNewOwner = true;
			for (int i = 0; i < data.length; i++) {
				if(data[i] < lowerBound[i] || data[i] > upperBound[i]){
					belongsToNewOwner = false;
					break;
				}
			}
			if(belongsToNewOwner){
				giveAwayData.add(data);
				iterator.remove();
			}
		}
		newOwner.setOwnedData(giveAwayData);
	}

	public void setNodeId(long id) {
		this.nodeId = id;
	}

	public long getNodeId() {
		return this.nodeId;
	}

	public Double[] getLocation() {
		return location;
	}
	
	public void setLocation(Double[] doubles) {
		this.location = doubles;
	}

	public ArrayList<Double[]> getOwnershipArea() {
		return ownershipArea;
	}

	public void setOwnershipArea(ArrayList<Double[]> ownershipArea) {
		this.ownershipArea = ownershipArea;
	}

	public ArrayList<Double[]> getOwnedData() {
		return ownedData;
	}

	public void setOwnedData(ArrayList<Double[]> ownedData) {
		this.ownedData = ownedData;
	}

	public ArrayList<CANNodeSpecs> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<CANNodeSpecs> neighbors) {
		this.neighbors = neighbors;
	}

	public CANNodeSpecs findClosestNeighborTo(CANNodeSpecs newSpecs) {
		double minDist = Double.POSITIVE_INFINITY;
		CANNodeSpecs winningNeighbor = null;
		for (CANNodeSpecs neighbor : neighbors) {
			if(neighbor.isOwnerOf(newSpecs)) return neighbor;
			double distance = neighbor.calcOptDistanceTo(newSpecs);
			if(distance < minDist) {
				minDist = distance;
				winningNeighbor = neighbor;
			}
		}
		return winningNeighbor;
	}
	
	private double calcOptDistanceTo(CANNodeSpecs newSpecs) {
		Double[] min = ownershipArea.get(0);
		Double[] max = ownershipArea.get(1);
		Double[] point = new Double[max.length];
		Double[] target = newSpecs.getLocation();
		for (int i = 0; i < max.length; i++) {
			point[i] = Math.max(Math.min(max[i], target[i]),min[i]);
		}
		return euclideanDistance(target, point);
	}
	
	private double calcMinDistanceTo(CANNodeSpecs newSpecs) {
		Double[] max = ownershipArea.get(0);
		Double[] min = ownershipArea.get(1);
		Double[] mid = new Double[max.length];
		for (int i = 0; i < mid.length; i++) {
			mid[i] = (max[i]+min[i])/2;
		}
		Double[] calc = new Double[max.length];
		return recCalc(max, min, mid, calc, newSpecs.getLocation(), 0);
	}

	private double recCalc(Double[] max, Double[] min, Double[] mid, Double[] calc, Double[] location, int i) {
		if(i == calc.length){
			return euclideanDistance(calc, location);
		}
		calc[i] = min[i];
		Double[] clone_min = calc.clone();
		double min_r = recCalc(max, min, mid, clone_min, location, i+1);
		calc[i] = max[i];
		Double[] clone_max = calc.clone();
		double max_r = recCalc(max, min, mid, clone_max, location, i+1);
		calc[i] = mid[i];
		Double[] clone_mid = calc.clone();
		double mid_r = recCalc(max, min, mid, clone_mid, location, i+1);
		return Math.min(max_r, Math.min(min_r, mid_r));
	}

	private double euclideanDistance(Double[] calc, Double[] location) {
		double result = 0.0;
		for (int i = 0; i < location.length; i++) {
			result += Math.pow(calc[i] - location[i],2);
		}
		return result;
	}

}
