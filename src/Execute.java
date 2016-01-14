import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import cluster.AlphaCommunity;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import parse.CustomAnalyzer;
import parse.HtmlCrawler;
import parse.MovielensParser;
import config.ProjectSettings;

public class Execute {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int number_of_args = args.length; 
		if( number_of_args < 2) {
			System.out.println("You have to specify username and password of database.");
			return;
		}
		Connection conn_inst = null;
		Statement stmt = null;
		ResultSet rset = null;
		MongoClient mongo_client = null;
		DB project_db = null;
		int sz = 800;
		int[] user_arr = new int[sz];
		for(int i= 0; i<sz; i++)
			user_arr[i] = i+1;
		try {
		Class.forName(ProjectSettings.JDBC_DRIVER);
		conn_inst = DriverManager.getConnection(ProjectSettings.DB_URL, args[0], args[1]);
		/*stmt = conn_inst.createStatement();
		rset = stmt.executeQuery("Select imdb_url, id, title from ".concat(ProjectSettings.MOVIE_TABLE_NAME).concat(" limit 1,1"));
		mongo_client = new MongoClient(ProjectSettings.MONGO_IP, ProjectSettings.MONGO_PORT);
		project_db = mongo_client.getDB(ProjectSettings.MONGO_DB_NAME);
		if(rset.next()) {
			HtmlCrawler hc = new HtmlCrawler(rset.getString(1), project_db, rset.getInt(2), rset.getString(3).toLowerCase());
			hc.crawlMovieData(6);
		}*/
		/*
		 CustomAnalyzer test_analyzer = new CustomAnalyzer();
		 test_analyzer.tokenizeString(sb.toString());
		 test_analyzer.close();
		*/
		/*MovielensParser mlparser = new MovielensParser(conn_inst);
		mlparser.parseRatingInfo(ProjectSettings.MOVIELENS_100K_PATH.concat(ProjectSettings.MOVIELENS_RATING_INPUT_FILE), "\t");*/
		/* Cluster test */
		AlphaCommunity ac = new AlphaCommunity (0.1, 0.15, 1.4, 50, 100, 10, 3, 0.05, conn_inst);
		ac.clusterRatings(user_arr);
		
		} catch(Exception mainEx) {
			System.out.println("Error on main: ".concat(mainEx.toString()));
			mainEx.printStackTrace();
		} finally {			
			try { if(rset != null) rset.close(); } catch (Exception sqlEx) {
				System.out.println("Error on mysql resultset close: ".concat(sqlEx.toString()));
			}
			try { if(stmt != null) stmt.close(); } catch (Exception sqlEx) {
				System.out.println("Error on mysql statement close: ".concat(sqlEx.toString()));
			}
			try { if(conn_inst != null) conn_inst.close(); } catch (Exception sqlEx) {
				System.out.println("Error on mysql connection close: ".concat(sqlEx.toString()));
			}
			if(mongo_client != null)
				mongo_client.close();
		}
	}

}
