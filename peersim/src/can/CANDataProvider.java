package can;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class CANDataProvider{

	private static final String DATA_SOURCE_FILE = "dataSourceFile";
	private static String dataSourceFile = Configuration.getString(DATA_SOURCE_FILE);

//	Loads the dataSet referred by DATA_SOURCE_FILE and return as an Arraylist of double's
	public static ArrayList<Double[]> loadData(long dataSetSize) {
		double maxValueOfData = Double.NEGATIVE_INFINITY;
		ArrayList<Double[]> data = new ArrayList<Double[]>();;
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataSourceFile));
			String line;
			long c = 0;
			while((line = br.readLine()) != null && c < dataSetSize){
				String[] temp = line.split(",");
				Double dataDimensions[] = new Double[temp.length];
				for (int i = 0; i < temp.length; i++) {
					dataDimensions[i] = Double.parseDouble(temp[i]);
					if(dataDimensions[i] > maxValueOfData) maxValueOfData = dataDimensions[i];
				}
				data.add(dataDimensions);
				c++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Double[] doubles : data) {
			data.set(data.indexOf(doubles), normalizeBy(doubles, maxValueOfData));
		}
		return data;
	}

	private static Double[] normalizeBy(Double[] doubles, double maxValueOfData) {
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = doubles[i] / maxValueOfData;
		}
		return doubles;
	}

	public static ArrayList<Double[]> generateRandomDataset(long dataSize, int dim) {
		ArrayList<Double[]> data = new ArrayList<Double[]>();
		for (long i = 0; i < dataSize; i++) {
			Double[] randomCoord = new Double[dim];
			for (int j = 0; j < randomCoord.length; j++) {
				randomCoord[j] = CommonState.r.nextDouble();
			}
			data.add(randomCoord);
		}
		return data;
	}

//	Used to generate new random node position
	public static Double[] nextNodeInfo(int dim){
		Double[] randomCoord = new Double[dim];
		for (int i = 0; i < randomCoord.length; i++){
			randomCoord[i] = CommonState.r.nextDouble();
		}
		return  randomCoord;
	}

}
