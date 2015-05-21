package can;

import java.util.ArrayList;

import org.lsmp.djep.groupJep.GOperatorSet;

import peersim.core.Node;
import peersim.core.Protocol;

public class CANNodeSpecs implements Protocol{

	private Double[] dimensions;
	private ArrayList<Double[]> ownershipArea;
	private ArrayList<Double[]> ownedData;
	private ArrayList<CANNodeSpecs> neighbors;

	public CANNodeSpecs() {
		setDimensions(new Double[6]);
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = -1.0;
		}
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
		Double[] nDimensions = n.getDimensions();
		Double[] x_0 = ownershipArea.get(0);
		Double[] x_1 = ownershipArea.get(1);
		for (int i = 0; i < nDimensions.length; i++) {
			Double coord_i = nDimensions[i];
			if(coord_i < x_0[i] || coord_i > x_1[i]) return false;
		}
		return true;
	}
	
	public void getHalfZoneOf(CANNodeSpecs ownerSpecs) {
		Double[] ownerDimensions = ownerSpecs.getDimensions();
		int bestDimension = -1;
		double dimDivision = Double.MAX_VALUE;
		ArrayList<Double[]> ownerArea = ownerSpecs.getOwnershipArea();
		for (int i = 0; i < ownerDimensions.length; i++) {
			if(ownerDimensions[i] == dimensions[i]) continue;
			double t = (ownerDimensions[i] + dimensions[i])/2;
			double x_0 = (ownerArea.get(0))[i];
			double x_1 = (ownerArea.get(1))[i];
			double center = (x_0 + x_1)/2;
			double distanceToCenter = Math.abs(center - t);
			if(distanceToCenter < Math.abs(center - dimDivision)){
				bestDimension = i;
				dimDivision = t;
			}
		}
		//In case two node locations repeat. We can deal with this better later on.
		if (bestDimension == -1) throw new RuntimeException("Two nodes at same position");
		getOwnershipArea();
		if (dimensions[bestDimension] < dimDivision) {
			ArrayList<Double[]> myArea = (ArrayList<Double[]>) ownerArea.clone();
			myArea.get(1)[bestDimension] = dimDivision;
			ownershipArea = myArea;
			ownerArea.get(0)[bestDimension] = dimDivision;
		} else {
			ArrayList<Double[]> myArea = (ArrayList<Double[]>) ownerArea.clone();
			myArea.get(0)[bestDimension] = dimDivision;
			ownerArea.get(1)[bestDimension] = dimDivision;
		}
		ownerSpecs.giveDataTo(this);
		ownerSpecs.getNeighbors().add(this);
		neighbors.add(ownerSpecs);
		ownerSpecs.giveNeighborsTo(this);
	}

	private void giveNeighborsTo(CANNodeSpecs newOwner) {
		for (CANNodeSpecs neighbor : neighbors) {
			areNeighbors(neighbor, this);
		}
	}
	
	private static boolean areNeighbors(CANNodeSpecs one, CANNodeSpecs two) {
		//TODO
		ArrayList<Double[]> ownershipAreaOne = one.getOwnershipArea();
		ArrayList<Double[]> ownershipAreaTwo = two.getOwnershipArea();
		Double[] one_lowerBound = ownershipAreaOne.get(0);
		Double[] one_upperBound = ownershipAreaOne.get(1);
		Double[] two_lowerBound = ownershipAreaTwo.get(0);
		Double[] two_upperBound = ownershipAreaTwo.get(1);
		for (int i = 0; i < two_upperBound.length; i++) {
			if(two_upperBound[i] == one_lowerBound[i]) return true;
			if(one_upperBound[i] == two_lowerBound[i]) return true;
		}
		return false;
	}

	private void giveDataTo(CANNodeSpecs newOwner) {
		ArrayList<Double[]> giveAwayData = new ArrayList<Double[]>();
		ArrayList<Double[]> newOwnerArea = newOwner.getOwnershipArea();
		Double[] lowerBound = newOwnerArea.get(0);
		Double[] upperBound = newOwnerArea.get(1);
		for (Double[] data : ownedData) {
			boolean belongsToNewOwner = true;
			for (int i = 0; i < data.length; i++) {
				if(data[i] < lowerBound[i] || data[i] > upperBound[i]){
					belongsToNewOwner = false;
					break;
				}
			}
			if(belongsToNewOwner){
				giveAwayData.add(data);
				ownedData.remove(data);
			}
		}
		newOwner.setOwnedData(giveAwayData);
	}

	public Double[] getDimensions() {
		return dimensions;
	}
	
	public void setDimensions(Double[] doubles) {
		this.dimensions = doubles;
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
			double distance = neighbor.calcMinDistanceTo(newSpecs);
			if(distance < minDist) {
				minDist = distance;
				winningNeighbor = neighbor;
			}
		}
		return winningNeighbor;
	}

	private double calcMinDistanceTo(CANNodeSpecs newSpecs) {
		//TODO
		return 0.0;
	}

}
