package cluster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.math3.stat.StatUtils;

import com.sun.org.apache.bcel.internal.generic.NEW;

import elements.WeightContainer;

public class AlphaCommunity {
	private float k1;
	private float k2;
	private float distance_denominator;
	private int num_of_turns;
	private int size_of_grid;
	private int size_of_agents;
	private int size_of_neighborhood;
	private float pick_drop_threshold;
	private HashMap<String,WeightContainer> community_locations;
	private HashMap<String, Integer> agent_positions;
	private Connection conn;
	private Statement stmt;
	private ResultSet rset;
	private Random random_inst;
	
	public AlphaCommunity(float pk1, float pk2, float pdistance_denominator, int pnum_of_turns, int psize_of_grid, 
							int psize_of_agents, int psize_of_neighborhood, float ppick_drop_threshold, Connection conn_inst) {
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
		this.agent_positions = new HashMap<String, Integer>();
		this.random_inst = new Random();
	}
	
	public void setDistanceDenominator(float arg_nominator) {this.distance_denominator = arg_nominator;}
	public void clusterByAntBasedAlgorithm() {}
	
	public void clusterGenres(int[] user_list) {
		try {
			StringBuilder users_string = new StringBuilder();
			int loop_counter;
			char[] char_arr;
			int number_of_rates_of_user=0;
			double[] number_of_rates_of_user_for_genres = new double[19];
			double mean = 0d;
			double variance = 0d;
			double total_weight = 0d;
			for(int user : user_list)
				users_string.append(Integer.toString(user)).append(",");
			users_string.deleteCharAt(users_string.length());
			this.stmt = this.conn.createStatement();
			this.rset = this.stmt.executeQuery("SELECT m.genres, r.user_id from movies m inner join ratings r on m.id = r.movie_id where r.user_id in (".concat(users_string.toString()).concat(")  order by r.user_id"));
			users_string.setLength(0);
			
			for(int user_id : user_list) {
				while(this.rset.next()) {
					if(this.rset.getInt(2) != user_id) {
						mean = StatUtils.mean(number_of_rates_of_user_for_genres);
						variance = StatUtils.variance(number_of_rates_of_user_for_genres, mean);
						WeightContainer wc = new WeightContainer(user_id, 19);
						for(loop_counter = 0; loop_counter < 19; loop_counter++) {
							wc.values[loop_counter] = (0.5 * number_of_rates_of_user_for_genres[loop_counter] / number_of_rates_of_user)
										+ (0.2 * mean / 5d) + (0.3 * variance / 5d);
							total_weight += wc.values[loop_counter];
						}
						
						/* Normalize weight vector */
						for(loop_counter = 0; loop_counter < 19; loop_counter++)
							wc.values[loop_counter] /= total_weight; 
						
						this.community_locations.put(users_string.append(this.random_inst.nextInt(this.size_of_grid))
										.append("$").append(this.random_inst.nextInt(this.size_of_grid)).toString(), wc);
						users_string.setLength(0);
						
						char_arr = this.rset.getString(1).toCharArray();
						for(loop_counter = 0; loop_counter < 19; loop_counter++) {
							if(char_arr[loop_counter] == 1)
								number_of_rates_of_user_for_genres[loop_counter] = 1;
							else number_of_rates_of_user_for_genres[loop_counter] = 0;
						}
						number_of_rates_of_user = 1;
						total_weight = 0d;
						break;
					}
					char_arr = this.rset.getString(1).toCharArray();
					for(loop_counter = 0; loop_counter < 19; loop_counter++) {
						if(char_arr[loop_counter] == 1)
							number_of_rates_of_user_for_genres[loop_counter]++;
					}
					number_of_rates_of_user++;
				}
			}
			
			this.clusterByAntBasedAlgorithm();
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
			StringBuilder users_string = new StringBuilder();
			int loop_counter;
			
			for(int user : user_list)
				users_string.append(Integer.toString(user)).append(",");
			users_string.deleteCharAt(users_string.length());
			this.stmt = this.conn.createStatement();
			this.rset = this.stmt.executeQuery("SELECT r.rating, r.user_id, r.movie_id from ratings r where r.user_id in (".concat(users_string.toString()).concat(")  order by r.user_id"));
			for(int user : user_list) {
				while(this.rset.next()) {
					if(user != this.rset.getInt(3)) {
						this.community_locations.put(users_string.append(this.random_inst.nextInt(this.size_of_grid))
								.append("$").append(this.random_inst.nextInt(this.size_of_grid)).toString(), wc);
						users_string.setLength(0);

					}
				}
			}
			this.clusterByAntBasedAlgorithm();
		} catch(Exception e) {
			System.out.println("Error on cluster Ratings: ".concat(e.toString()));
		} finally {
			
		}
	}
}
