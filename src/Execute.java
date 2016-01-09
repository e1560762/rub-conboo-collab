import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

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
		try {
		Class.forName(ProjectSettings.JDBC_DRIVER);
		conn_inst = DriverManager.getConnection(ProjectSettings.DB_URL, args[0], args[1]);
		stmt = conn_inst.createStatement();
		rset = stmt.executeQuery("Select imdb_url from ".concat(ProjectSettings.MOVIE_TABLE_NAME).concat(" limit 1,1"));
		if(rset.next()) {
			HtmlCrawler hc = new HtmlCrawler(rset.getString(1));
			hc.crawlMovieComments("reviews?start=", 0);
		}
		/*
		 CustomAnalyzer test_analyzer = new CustomAnalyzer();
		 test_analyzer.tokenizeString(sb.toString());
		 test_analyzer.close();
		*/
		/*MovielensParser mlparser = new MovielensParser(conn_inst);
		mlparser.parseRatingInfo(ProjectSettings.MOVIELENS_100K_PATH.concat(ProjectSettings.MOVIELENS_RATING_INPUT_FILE), "\t");*/
		} catch(Exception mainEx) {
			System.out.println("Error on main: ".concat(mainEx.toString()));
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
		}
	}

}
