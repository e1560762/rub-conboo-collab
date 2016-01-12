package elements;

import java.util.HashMap;

public class WeightContainer {
	public int user_id;
	public double[] values;
	public HashMap<Integer, Integer> adjacency_matrix;
	
	public WeightContainer(int uid, int size_of_array) {
		this.user_id = uid;
		this.values = new double[size_of_array];
		this.adjacency_matrix = new HashMap<Integer, Integer>();
	}
}
