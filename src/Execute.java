import java.sql.Connection;
import java.sql.DriverManager;

import parse.HtmlCrawler;
import parse.MovielensParser;
import config.ProjectSettings;

public class Execute {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int number_of_args = args.length; 
		if( number_of_args < 3) {
			System.out.println("You have to specify at url, database username and password");
			return;
		}
		Connection conn_inst = null;
		try {
		Class.forName(ProjectSettings.JDBC_DRIVER);
		conn_inst = DriverManager.getConnection(ProjectSettings.DB_URL, args[1], args[2]);
		/*HtmlCrawler hc = new HtmlCrawler();
		hc.crawlMovie(args[0]);*/
		MovielensParser mlparser = new MovielensParser(conn_inst);
		mlparser.parseGenreInfo(ProjectSettings.MOVIELENS_100K_PATH.concat(ProjectSettings.MOVIELENS_GENRE_INPUT_FILE), ProjectSettings.MOVIELENS_COLUMN_SEPARATOR);
		} catch(Exception mainEx) {
			System.out.println("Error on main: ".concat(mainEx.toString()));
		} finally {			
			try {
				if(conn_inst != null)
					conn_inst.close();
			} catch (Exception sqlEx) {
				System.out.println("Error on mysql connection close: ".concat(sqlEx.toString()));
			}
		}
	}

}
