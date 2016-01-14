package elements;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Clusterable;

public class WeightContainer implements Clusterable{
	public int user_id;
	public double[] values;
	public double[] points;
	public HashMap<Integer, Double> rated_movies;
	public StringBuilder str = new StringBuilder();
	
	public WeightContainer() {this.user_id = 0;}
	public WeightContainer(int uid, int size_of_array) {
		this.user_id = uid;
		this.values = new double[size_of_array];
	}
	
	public void printContents() {
		this.str.append(this.user_id).append("\nFavourite Genres:\n");
		for(int i=0; i<this.values.length; i++)
			this.str.append(Double.toString(this.values[i])).append(" ");
		if(this.rated_movies != null) {
			this.str.append("\nMovie Ratings:\n");
			for(Map.Entry<Integer, Double> entry : this.rated_movies.entrySet()) {
				this.str.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
			}
			this.str.append("\n");
		}
		System.out.println(this.str.toString());
		this.str.setLength(0);
	}
	/*public void copyFrom(WeightContainer wc) {
		this.user_id = wc.user_id;
		this.values = 
	}*/
	@Override
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return this.points;
	}
}
