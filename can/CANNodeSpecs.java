package can;

import java.util.ArrayList;

import peersim.core.Protocol;

public class CANNodeSpecs implements Protocol{

	private Double[] dimensions;
	private ArrayList<Double[]> ownershipArea;
	//dataset
	
	public CANNodeSpecs() {
		setDimensions(new Double[6]);
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = -1.0;
		}
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
		for (int i = 0; i < nDimensions.length; i++) {
			Double[] area_i = ownershipArea.get(i);
			Double coord_i = nDimensions[i];
			if(coord_i < area_i[0] || coord_i > area_i[1]) return false;
		}
		return true;
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

	public void getHalfZoneOf(CANNodeSpecs rootSpecs) {
		
	}
	
}
