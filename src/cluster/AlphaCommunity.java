package cluster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.sun.org.apache.bcel.internal.generic.NEW;

import config.ProjectSettings;
import elements.WeightContainer;

public class AlphaCommunity {
	private double k1;
	private double k2;
	private double distance_denominator;
	private int num_of_turns;
	private int size_of_grid;
	private int size_of_agents;
	private int size_of_neighborhood;
	private double pick_drop_threshold;
	private HashMap<String,WeightContainer> community_locations;
	private HashMap<Integer, String> agent_positions;
	/*private ArrayList<String> available_x_coordinates = new ArrayList<String>();
	private ArrayList<String> available_y_coordinates = new ArrayList<String>();*/
	private Connection conn;
	private Statement stmt;
	private ResultSet rset;
	private Random random_inst;
	private HashMap<Integer, WeightContainer> agent_load;
	private int ac_count_x;
	private int ac_count_y;
	private PearsonsCorrelation pearson_obj= new PearsonsCorrelation();
	private EuclideanDistance euclidean_obj = new EuclideanDistance();
	double[] arr_main;
	double[] arr_neighbor;
	
	public AlphaCommunity(double pk1, double pk2, double pdistance_denominator, int pnum_of_turns, int psize_of_grid, 
							int psize_of_agents, int psize_of_neighborhood, double ppick_drop_threshold, Connection conn_inst) {
		this.k1 = pk1;
		this.k2 = pk2;
		this.distance_denominator = pdistance_denominator;
		this.num_of_turns = pnum_of_turns;
		this.size_of_grid = psize_of_grid;
		this.size_of_agents = psize_of_agents;
		this.size_of_neighborhood = psize_of_neighborhood;
		this.pick_drop_threshold = ppick_drop_threshold;
		this.conn = conn_inst;
		this.community_locations = new HashMap<String,WeightContainer>();
		this.agent_positions = new HashMap<Integer, String>();
		this.random_inst = new Random();
		//this.initializeAvailableCoordinates();
		this.agent_load = new HashMap<Integer, WeightContainer>();
	}
	
	public void setDistanceDenominator(double arg_nominator) {this.distance_denominator = arg_nominator;}
	public void clusterByAntBasedAlgorithm(boolean isRatings) {
		int counter;
		int round_counter;
		int x_position;
		int y_position;
		String composite_position;
		WeightContainer wc;
		StringBuilder pos_builder = new StringBuilder();
		try {
		for(counter = 0; counter < this.size_of_agents; counter++) {
			while(true) {
				pos_builder.append(this.random_inst.nextInt(this.size_of_grid))
						.append("_")
						.append(this.random_inst.nextInt(this.size_of_grid));
				if(this.agent_positions.containsValue(pos_builder.toString()) == false) {
					this.agent_positions.put(counter, pos_builder.toString());
					pos_builder.setLength(0);
					break;
				}
				pos_builder.setLength(0);
			}
		}
		this.initializeSparseRatingArrays(ProjectSettings.MOVIELENS_100K_NUM_OF_MOVIES);
		
		if(isRatings == true) { /*Use rated_movies field*/
			for(round_counter = 0; round_counter < this.num_of_turns; round_counter++) {
				for(counter = 0; counter < this.size_of_agents; counter++) {
					composite_position = this.agent_positions.remove(counter);
					x_position = Integer.parseInt(composite_position.split("_")[0]);
					y_position = Integer.parseInt(composite_position.split("_")[1]);
					if(this.agent_load.containsKey(counter)) { /*agent has load*/
						if(this.community_locations.containsKey(composite_position) == false &&
							this.dropProbability(this.computeDissimilarityByPearson(x_position, y_position, 
								this.agent_load.get(counter).rated_movies)) > this.pick_drop_threshold) {
							this.community_locations.put(composite_position, this.agent_load.remove(counter));
						}
					}
					else { /* agent doesnot have load*/
						wc = this.community_locations.get(composite_position);
						if(wc != null) {
							/*Compute this dissimilarity to neighbours*/
							if(this.pickProbability(this.computeDissimilarityByPearson(x_position, y_position, wc.rated_movies)) > this.pick_drop_threshold) {
								this.agent_load.put(counter, this.community_locations.remove(composite_position));
							}
						}
					}
					this.determineNewPosition(x_position, y_position, 3);
					this.agent_positions.put(counter, Integer.toString(x_position).concat("_").concat(Integer.toString(y_position)));
				}
			}
		}
		else { /*use values field*/
			for(round_counter = 0; round_counter < this.num_of_turns; round_counter++) {
				for(counter = 0; counter < this.size_of_agents; counter++) {
					composite_position = this.agent_positions.remove(counter);
					x_position = Integer.parseInt(composite_position.split("_")[0]);
					y_position = Integer.parseInt(composite_position.split("_")[1]);
					if(this.agent_load.containsKey(counter)) { /*agent has load*/
						if(this.dropProbability(this.computeDissimilarityByEuclidean(x_position, y_position, 
								this.agent_load.get(counter).values)) > this.pick_drop_threshold) {
							this.community_locations.put(composite_position, this.agent_load.remove(counter));
						}
					}
					else { /* agent doesnot have load*/
						wc = this.community_locations.get(composite_position);
						if(wc != null) {
							/*Compute this dissimilarity to neighbours*/
							if(this.pickProbability(this.computeDissimilarityByEuclidean(x_position, y_position, wc.values)) > this.pick_drop_threshold) {
								this.agent_load.put(counter, this.community_locations.remove(composite_position));
							}
						}
					}
					this.determineNewPosition(x_position, y_position, 3);
					this.agent_positions.put(counter, Integer.toString(x_position).concat("_").concat(Integer.toString(y_position)));
				}
			}	
		}
		} catch(Exception excp) {
			System.out.println("Error in clusterByAntBasedAlgorithm:");
			excp.printStackTrace();
		}
	}
	
	public void clusterGenres(int[] user_list) {
		try {
			StringBuilder pos_builder = new StringBuilder(); 
			StringBuilder users_string = new StringBuilder();
			int loop_counter;
			int inner_loop_counter;
			char[] char_arr;
			int posx,posy;
			int number_of_rates_of_user=0;
			int[] number_of_rates_of_user_for_genres = new int[19];
			String[] rates_for_each_genre = new String[19];
			double mean = 0d;
			double variance = 0d;
			double total_weight = 0d;
			double[] rates;
			String[] splitted_rates_as_string;
			Arrays.fill(number_of_rates_of_user_for_genres, 0);
			Arrays.fill(rates_for_each_genre, "");

			for(int user : user_list)
				users_string.append(Integer.toString(user)).append(",");
			users_string.deleteCharAt(users_string.length()-1);
			this.stmt = this.conn.createStatement();
			this.rset = this.stmt.executeQuery("SELECT m.genres, r.user_id, r.rating from movies m inner join ratings r on m.id = r.movie_id where r.user_id in (".concat(users_string.toString()).concat(")  order by r.user_id"));
			users_string.setLength(0);
			
			for(int user_id : user_list) {
				while(this.rset.next()) {
					if(this.rset.getInt(2) != user_id || this.rset.isLast()) {
						WeightContainer wc = new WeightContainer(user_id, 19);
						for(loop_counter = 0; loop_counter < 19; loop_counter++) {
							if(rates_for_each_genre[loop_counter].startsWith("$"))
								rates_for_each_genre[loop_counter] = rates_for_each_genre[loop_counter].substring(1);
							rates = new double[number_of_rates_of_user_for_genres[loop_counter]];
							Arrays.fill(rates, 0d);
							splitted_rates_as_string = rates_for_each_genre[loop_counter].split("\\$");
							for(inner_loop_counter = 0; inner_loop_counter<number_of_rates_of_user_for_genres[loop_counter]; inner_loop_counter++) {
								rates[inner_loop_counter] = Double.parseDouble(splitted_rates_as_string[inner_loop_counter]);								
							}
							mean = StatUtils.mean(rates);
							if(Double.isNaN(mean))
								mean = 0d;
							variance = StatUtils.variance(rates, mean);
							if(Double.isNaN(variance))
								variance = 0d;
							wc.values[loop_counter] = (0.5 * number_of_rates_of_user_for_genres[loop_counter] / number_of_rates_of_user)
										+ (0.2 * mean / 5d) + (0.3 * variance / 5d);
							total_weight += wc.values[loop_counter];
						}
						
						/* Normalize weight vector */
						for(loop_counter = 0; loop_counter < 19; loop_counter++)
							wc.values[loop_counter] /= total_weight;
						while(true) {
							pos_builder.append(this.random_inst.nextInt(this.size_of_grid)).append("_")
										.append(this.random_inst.nextInt(this.size_of_grid));
							if(this.community_locations.containsKey(pos_builder.toString()) == false) {
								this.community_locations.put(pos_builder.toString(), wc);
								pos_builder.setLength(0);
								break;
							}
							pos_builder.setLength(0);
						}
						users_string.setLength(0);
						
						char_arr = this.rset.getString(1).toCharArray();
						for(loop_counter = 0; loop_counter < 19; loop_counter++) {
							if(char_arr[loop_counter] == '1') {
								number_of_rates_of_user_for_genres[loop_counter] = 1;
								rates_for_each_genre[loop_counter] = Integer.toString(this.rset.getInt(3));
							}
							else {
								number_of_rates_of_user_for_genres[loop_counter] = 0;
								rates_for_each_genre[loop_counter] = "";
							}
						}
						number_of_rates_of_user = 1;
						total_weight = 0d;
						break;
					}
					char_arr = this.rset.getString(1).toCharArray();
					for(loop_counter = 0; loop_counter < 19; loop_counter++) {
						if(char_arr[loop_counter] == '1') {
							number_of_rates_of_user_for_genres[loop_counter]++;
							rates_for_each_genre[loop_counter] = rates_for_each_genre[loop_counter].concat("$")
										.concat(Integer.toString(this.rset.getInt(3)));
						}
					}
					number_of_rates_of_user++;
				}
			}
			this.clusterByAntBasedAlgorithm(false);
			String[] str_iter = this.community_locations. keySet().toArray(new String[this.community_locations.keySet().size()]);
			ArrayList<WeightContainer> kmeans_clustered = new ArrayList<WeightContainer>();
			for(String cur_key : str_iter) {
				WeightContainer tmpwc = this.community_locations.remove(cur_key);
				tmpwc.points = new double[2];
				tmpwc.points[0] = Double.parseDouble(cur_key.split("_")[0]);
				tmpwc.points[1] = Double.parseDouble(cur_key.split("_")[1]);
				kmeans_clustered.add(tmpwc);				
			}
			this.agent_load.clear();
			KMeansPlusPlusClusterer<WeightContainer> clusterer = 
					new KMeansPlusPlusClusterer<WeightContainer>(19, 1000);
			List<CentroidCluster<WeightContainer>> clusterResults = clusterer.cluster(kmeans_clustered);
			for (int i=0; i<clusterResults.size(); i++) {
			    System.out.println("Cluster " + i);
			    for(WeightContainer wc2 : clusterResults.get(i).getPoints())
			    	System.out.print(wc2.user_id + ",");
			    System.out.println();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(this.rset != null) try{this.rset.close();} catch(Exception ex){System.out.println(ex.toString());}
			if(this.stmt != null) try{this.stmt.close();} catch(Exception ex){System.out.println(ex.toString());}
		}
		
	}
	
	public void clusterRatings(int[] user_list) {
		try {
			StringBuilder pos_builder = new StringBuilder();
			StringBuilder users_string = new StringBuilder();
			WeightContainer wc = new WeightContainer(1, 0);
			wc.rated_movies = new HashMap<Integer, Double>();
			for(int user : user_list)
				users_string.append(Integer.toString(user)).append(",");
			users_string.deleteCharAt(users_string.length()-1);
			this.stmt = this.conn.createStatement();			
			this.rset = this.stmt.executeQuery("SELECT r.rating, r.user_id, r.movie_id from ratings r where r.user_id in (".concat(users_string.toString()).concat(")  order by r.user_id"));
			for(int user : user_list) {
				while(this.rset.next()) {
					if(user != this.rset.getInt(2) || this.rset.isLast()) {
						while(true) {
							pos_builder.append(this.random_inst.nextInt(this.size_of_grid))
									.append("_")
									.append(this.random_inst.nextInt(this.size_of_grid));
							if(this.community_locations.containsKey(pos_builder.toString()) == false) {
								this.community_locations.put(pos_builder.toString(), wc);
								pos_builder.setLength(0);
								break;
							}
							pos_builder.setLength(0);
						}
						
						wc = new WeightContainer(this.rset.getInt(2), 0);
						wc.rated_movies = new HashMap<Integer, Double>();						
						wc.rated_movies.put(this.rset.getInt(3), (double)this.rset.getInt(1));
						break;
					}
					else {
						wc.rated_movies.put(this.rset.getInt(3), (double)this.rset.getInt(1));
					}
				}
			}
			this.clusterByAntBasedAlgorithm(true);
			String[] str_iter = this.community_locations.keySet().toArray(new String[this.community_locations.keySet().size()]);
			ArrayList<WeightContainer> kmeans_clustered = new ArrayList<WeightContainer>();
			for(String cur_key : str_iter) {
				WeightContainer tmpwc = this.community_locations.remove(cur_key);
				tmpwc.points = new double[2];
				tmpwc.points[0] = Double.parseDouble(cur_key.split("_")[0]);
				tmpwc.points[1] = Double.parseDouble(cur_key.split("_")[1]);
				kmeans_clustered.add(tmpwc);				
			}
			this.agent_load.clear();
			KMeansPlusPlusClusterer<WeightContainer> clusterer = 
					new KMeansPlusPlusClusterer<WeightContainer>(5, 1000);
			List<CentroidCluster<WeightContainer>> clusterResults = clusterer.cluster(kmeans_clustered);
			for (int i=0; i<clusterResults.size(); i++) {
			    System.out.println("Cluster " + i);
			    for(WeightContainer wc2 : clusterResults.get(i).getPoints())
			    	System.out.print(wc2.user_id + ",");
			    System.out.println();
			}
		} catch(Exception e) {
			System.out.println("Error on cluster Ratings: ".concat(e.toString()));
			e.printStackTrace();
		} finally {
			if(this.rset != null) try{this.rset.close();} catch(Exception ex){System.out.println(ex.toString());}
			if(this.stmt != null) try{this.stmt.close();} catch(Exception ex){System.out.println(ex.toString());}
		}
	}
	
	public void printCommunityLocations() {
		System.out.println("COMMUNITY LOCATIONS");
		for(Map.Entry<String,WeightContainer> entry : this.community_locations.entrySet()) {
			System.out.print(entry.getKey());
			System.out.println(" Location Values\n");
			entry.getValue().printContents();
		}
	}
	
	public void printAgentLoads() {
		System.out.println("AGENT LOAD");
		for(Map.Entry<Integer,WeightContainer> entry : this.agent_load.entrySet()) {
			System.out.print(entry.getKey());
			System.out.println(" Location Values\n");
			entry.getValue().printContents();
		}
	} 
	/*
	private void initializeAvailableCoordinates() {
		this.available_x_coordinates.clear(); 
		this.available_y_coordinates.clear();
		
		for(int i=0; i < this.size_of_grid - 1; i++) {
			this.available_x_coordinates.add(Integer.toString(i));
			this.available_y_coordinates.add(Integer.toString(i));
		}
	}*/
	private void initializeSparseRatingArrays(int allocation_size) {
		this.arr_main = new double[allocation_size];
		this.arr_neighbor = new double[allocation_size];
	}
	private void determineNewPosition(int argX, int argY, int velocity) {
		if(this.random_inst.nextBoolean()) {
			argX = myMin(this.size_of_grid, argX + this.random_inst.nextInt(velocity));
		}
		else {
			argX = myMax(0,argX - this.random_inst.nextInt(velocity));
		}
		
		if(this.random_inst.nextBoolean()) {
			argY = myMin(this.size_of_grid, argY + this.random_inst.nextInt(velocity));
		}
		else {
			argY = myMax(0,argY - this.random_inst.nextInt(velocity));
		}
	} 
	
	private int myMin(int a, int b) {return a<b ? a : b;}
	private int myMax(int a, int b) {return a>b ? a : b;}

	private double computeDissimilarityByPearson(int x_pos, int y_pos, HashMap<Integer, Double> curr_weight) {
		double result = 0d;
		HashMap<Integer, Double> neighbour;
		int i=0;
		for(i = 0; i < ProjectSettings.MOVIELENS_100K_NUM_OF_MOVIES; i++) {
			if(curr_weight.containsKey(i+1))
				this.arr_main[i] = curr_weight.get(i+1);
			else 
				this.arr_main[i] = 0d;
		}
		
		for(this.ac_count_x = myMax(x_pos-this.size_of_neighborhood, 0); this.ac_count_x < myMax(x_pos, x_pos + this.size_of_neighborhood); this.ac_count_x++) {
			for(this.ac_count_y = myMax(y_pos-this.size_of_neighborhood, 0); this.ac_count_y < myMax(y_pos, y_pos + this.size_of_neighborhood); this.ac_count_y++) {
				if(this.ac_count_x == x_pos && this.ac_count_y == y_pos) continue;
				if(this.community_locations.containsKey(Integer.toString(this.ac_count_x).concat("_").concat(Integer.toString(this.ac_count_y)))) {
					neighbour = this.community_locations.get(Integer.toString(this.ac_count_x).concat("_").concat(Integer.toString(this.ac_count_y))).rated_movies;
					for(i = 0; i < ProjectSettings.MOVIELENS_100K_NUM_OF_MOVIES; i++) {
						if(neighbour.containsKey(i+1))
							this.arr_neighbor[i] = neighbour.get(i+1);
						else
							this.arr_neighbor[i] = 0d;
					}
					result += 1-(this.pearson_obj.correlation(this.arr_main, this.arr_neighbor)/this.distance_denominator);
				}
			}
		}
		return Math.max(0d, result/(this.size_of_neighborhood * this.size_of_neighborhood));
	}
	
	private double computeDissimilarityByEuclidean(int x_pos, int y_pos, double[] genreVals){
		double result = 0d;
		for(this.ac_count_x = myMax(x_pos-this.size_of_neighborhood, 0); this.ac_count_x < myMax(x_pos, x_pos + this.size_of_neighborhood); this.ac_count_x++) {
			for(this.ac_count_y = myMax(y_pos-this.size_of_neighborhood, 0); this.ac_count_y < myMax(y_pos, y_pos + this.size_of_neighborhood); this.ac_count_y++) {
				if(this.ac_count_x == x_pos && this.ac_count_y == y_pos) continue;
				if(this.community_locations.containsKey(Integer.toString(this.ac_count_x).concat("_").concat(Integer.toString(this.ac_count_y)))) {
					result += 1-(this.euclidean_obj.compute(genreVals, 
							this.community_locations.get(Integer.toString(this.ac_count_x)
									.concat("_").concat(Integer.toString(this.ac_count_y))
									).values)/this.distance_denominator);
				}
			}
		}
		return Math.max(0d, result/(this.size_of_neighborhood * this.size_of_neighborhood));
	}

	private double pickProbability(double dissimilarity) {
		return Math.pow(this.k1 / (this.k1 + dissimilarity), 2); 
	}
	private double dropProbability(double dissimilarity) {
		return (dissimilarity < this.k2) ? (2*dissimilarity): 1;
	}
}
