package can;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;

public class CANNodeProvider implements Control{

	private static CANNodeProvider reference = null;
	
	private static final String NODE_SOURCE_FILE = "nodeSourceFile";
	private static final String IS_RANDOM = "isRandom";
	private boolean isRandom;
	private String nodeSourceFile;

	private ArrayList<Double[]> nodes;

	
	public static CANNodeProvider getReference(){
		if(reference == null){
			reference = new CANNodeProvider();
		}
		return reference;
	}
	
	private CANNodeProvider() {
		isRandom = Configuration.getBoolean(IS_RANDOM);
		if(!isRandom){
			nodeSourceFile = Configuration.getString(NODE_SOURCE_FILE);
		}
	}
	
	@Override
	public boolean execute() {
		if(!isRandom){
			try {
				nodes = new ArrayList<Double[]>();
				BufferedReader br = new BufferedReader(new FileReader(nodeSourceFile));
				String line = br.readLine();
				while(line != null){
					String[] temp = line.split(",");
					Double nodeDimensions[] = new Double[temp.length];
					for (int i = 0; i < temp.length; i++) {
						nodeDimensions[i] = Double.parseDouble(temp[i]);
					}
					nodes.add(nodeDimensions);
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	
	public Double[] nextNodeInfo(){
		if(!isRandom){
			if(!nodes.isEmpty()){
				return nodes.remove(0);
			}
			return null;
		}
		Double[] randomCoord = {CommonState.r.nextDouble(),CommonState.r.nextDouble(),CommonState.r.nextDouble(),CommonState.r.nextDouble(),CommonState.r.nextDouble(),CommonState.r.nextDouble()};
		return  randomCoord;
	}
}
