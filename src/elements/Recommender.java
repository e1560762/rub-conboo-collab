package elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Recommender {
	public Connection conn;
	public int agreed_rating;
	public double agreed_percentage;
	int total_voters;
	
	public Recommender(Connection conn_inst) {
		this.conn = conn_inst;
		this.agreed_rating = 4;
		this.agreed_percentage = 0.25d;
		this.total_voters = 0;
	}
	
	public Recommender(Connection conn_inst, int rate_threshold, double per_threshold) {
		this.conn = conn_inst;
		this.agreed_rating = rate_threshold;
		this.agreed_percentage = per_threshold;
		this.total_voters = 0;
	}
	
	public void recommendByAge(int user_id) {
		Statement stmt = null;
		ResultSet rset = null;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_count = new StringBuilder();
		int age;
		try {
			stmt = this.conn.createStatement();
			rset = stmt.executeQuery(sb.append("Select age from users where id=")
					.append(Integer.toString(user_id)).toString());
			System.out.println(sb.toString());
			sb.setLength(0);
			sb_count.append("select count(u.id) from users u where ");
			if(rset.next()) {
				age = rset.getInt(1);
				sb.append("select r.movie_id, r.rating from ratings r inner join users u on u.id = r.user_id and ");
				if (age < 16) {
					sb.append("u.age < 16");
					sb_count.append("u.age < 16");
				}
				else if(age >= 16 && age <= 25) {
					sb.append("u.age >= 26 and u.age <= 45");
					sb_count.append("u.age >= 26 and u.age <= 45");
				}
				else if(age >= 26 && age <= 45) {
					sb.append("u.age >= 26 and u.age <= 45");
					sb_count.append("u.age >= 26 and u.age <= 45");
				}
				else if(age >= 46 && age <= 60) {
					sb.append("u.age >= 46 and u.age <= 60");
					sb_count.append("u.age >= 46 and u.age <= 60");
				}
				else {
					sb.append("u.age > 60");
					sb_count.append("u.age > 60");
				}
				sb.append(" order by r.movie_id");				
			}
			else {
				System.out.println("USER NOT FOUND OR AGE IS NOT DEFINED");
				rset.close();
				stmt.close();
				return;
			}
			rset = stmt.executeQuery(sb_count.toString());
			rset.next();
			this.total_voters = rset.getInt(1);
			rset = stmt.executeQuery(sb.toString());
			HashMap<Integer, LevelOfAgreement> loa_list = new HashMap<Integer, LevelOfAgreement>();
			LevelOfAgreement loa_obj;
			while(rset.next()) {
				if(loa_list.containsKey(rset.getInt(1))) {
					loa_obj = loa_list.get(rset.getInt(1));
					loa_obj.number_of_votes += 1;
					loa_obj.total_rating += rset.getInt(2);
					loa_list.put(rset.getInt(1), loa_obj);
				}
				else {
					loa_obj = new LevelOfAgreement();
					loa_obj.number_of_votes += 1;
					loa_obj.total_rating = rset.getInt(2);
					loa_list.put(rset.getInt(1), loa_obj);
				}
			}
			sb.setLength(0);
			System.out.println("Hede " + this.total_voters);
			this.eliminateByLevelOfAgreement(loa_list);
			for(Map.Entry<Integer, LevelOfAgreement> entry : loa_list.entrySet()) {
				System.out.println(entry.getKey() + " ");
			}
		} catch(Exception ex) {} 
		  finally {
			if(rset != null) try{rset.close();} catch(Exception exc){}
			if(stmt != null) try{stmt.close();} catch(Exception exc){}
		}	
	}
	
	public void recommendByOccupation(int user_id) {
		Statement stmt = null;
		ResultSet rset = null;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_count = new StringBuilder();
		String job;
		try {
			stmt = this.conn.createStatement();
			rset = stmt.executeQuery("select occupation from users where id=" + user_id);
			if(rset.next()) {
				job = rset.getString(1).toLowerCase();
			}
			else {
				rset.close();
				stmt.close();
				System.out.println("User not found or occupation is not defined");
				return;
			}
			sb_count.append("select count(u.id) from users u where ");
			sb.append("select r.movie_id, r.rating from ratings r inner join users u on u.id = r.user_id and ");
			if("educator".equals(job) || "student".equals(job) || "scientist".equals(job)) {
				sb.append("occupation in (\'teacher\',\'student\',\'scientist\')");
				sb_count.append("occupation in (\'teacher\',\'student\',\'scientist\')");
			}
			else if("tradesman".equals(job) || "marketing".equals(job)) {
				sb.append("occupation in (\'tradesman\', \'marketing\')");
				sb_count.append("occupation in (\'tradesman\', \'marketing\')");
			}
			else if("engineer".equals(job) || "technician".equals(job)) {
				sb.append("occupation in (\'engineer\', \'technician\')");
				sb_count.append("occupation in (\'engineer\', \'technician\')");
			}
			else if("artist".equals(job) || "leisure".equals(job)) {
				sb.append("occupation in (\'leisure\', \'artist\')");
				sb_count.append("occupation in (\'leisure\', \'artist\')");
			}
			else if("healthcare".equals(job) || "doctor".equals(job)) {
				sb.append("occupation in (\'healthcare\', \'doctor\')");
				sb_count.append("occupation in (\'healthcare\', \'doctor\')");
			}
			else if("retired".equals(job) || "homemaker".equals(job)) {
				sb.append("occupation in (\'retired\', \'homemaker\')");
				sb_count.append("occupation in (\'retired\', \'homemaker\')");
			}
			else {
				sb.append("occupation not in (\'teacher\',\'student\',\'scientist\',\'tradesman\',\'marketing\',\'engineer\',\'technician\',\'leisure\', \'artist\',\'healthcare\', \'doctor\',\'retired\', \'homemaker\')");
				sb_count.append("occupation not in (\'teacher\',\'student\',\'scientist\',\'tradesman\',\'marketing\',\'engineer\',\'technician\',\'leisure\', \'artist\',\'healthcare\', \'doctor\',\'retired\', \'homemaker\')");
			}
			sb.append("order by r.movie_id");
			rset = stmt.executeQuery(sb_count.toString());
			rset.next();
			this.total_voters = rset.getInt(1);
			rset = stmt.executeQuery(sb.toString());
			while(rset.next()) {
				HashMap<Integer, LevelOfAgreement> loa_list = new HashMap<Integer, LevelOfAgreement>();
				LevelOfAgreement loa_obj;
				while(rset.next()) {
					if(loa_list.containsKey(rset.getInt(1))) {
						loa_obj = loa_list.get(rset.getInt(1));
						loa_obj.number_of_votes += 1;
						loa_obj.total_rating += rset.getInt(2);
						loa_list.put(rset.getInt(1), loa_obj);
					}
					else {
						loa_obj = new LevelOfAgreement();
						loa_obj.number_of_votes += 1;
						loa_obj.total_rating = rset.getInt(2);
						loa_list.put(rset.getInt(1), loa_obj);
					}
				}
				sb.setLength(0);
				/*loa_list = */this.eliminateByLevelOfAgreement(loa_list);
				for(Map.Entry<Integer, LevelOfAgreement> entry : loa_list.entrySet()) {
					System.out.println(entry.getKey() + " ");
				}
			}
		} catch(Exception exc){ exc.printStackTrace();}
		  finally{
			if(rset != null) try{rset.close();} catch(Exception exc){}
			if(stmt != null) try{stmt.close();} catch(Exception exc){}
		  }
	}
	
	public void recommendByLocation(int user_id) {
		
	}
	
	public void recommendByContent(int user_id) {
		
	}

	public void recommendByGenre(int user_id) {
		/*String line;
		StringBuilder sb = new StringBuilder();*/
		try {
		Runtime r = Runtime.getRuntime();
	    Process p2 = r.exec("/home/yigit/Doktora/KnowledgeEngineering/20151/project/c5_0/c5.0 -f /home/yigit/Doktora/KnowledgeEngineering/20151/project/test/see5/genre");
		/*BufferedReader br = new BufferedReader(new InputStreamReader(p2.getInputStream()));
		while((line = br.readLine()) != null )
			sb.append(line);*/
		p2.waitFor();
		/*System.out.println(sb.toString());
		br.close();*/
	    p2.destroy();
		} catch(Exception ex) {
			System.out.println("Error on recommendation by genre");
			ex.printStackTrace();
		}
	}
	
	public void recommendByRating(int user_id) {
		
	}
	
	private void eliminateByLevelOfAgreement(HashMap<Integer, LevelOfAgreement> loa_list) {
		ArrayList<Integer> deleted_members = new ArrayList<Integer>();
		double avg = 0d;
		for(Map.Entry<Integer, LevelOfAgreement> entry : loa_list.entrySet()) {
			avg = entry.getValue().total_rating / (double)entry.getValue().number_of_votes;
			if(avg < this.agreed_rating)
				deleted_members.add(entry.getKey());
			else
				entry.getValue().average_rating = avg;
		}
		for(int i : deleted_members) {
			loa_list.remove(i);
		}
		
		for(Map.Entry<Integer, LevelOfAgreement> entry : loa_list.entrySet()) {
			if((entry.getValue().number_of_votes / (double)this.total_voters) < this.agreed_percentage)
				deleted_members.add(entry.getKey());
		}
		for(int i : deleted_members) {
			loa_list.remove(i);
		}
		//return loa_list;
	}
	
}
