package parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import config.ProjectSettings;

public class MovielensParser {
	private String[] line_info;  
	private Connection conn;
	private Statement stmt;
	private ResultSet resultset;
	private StringBuilder query_string;
	
	public MovielensParser(Connection connection_instance) {
		this.conn = connection_instance;
		this.query_string = new StringBuilder(100);
	}
	
	public void parseGenreInfo(String absolute_path, String column_separator) {
		this.query_string.append("INSERT INTO ")
						.append(ProjectSettings.GENRE_TABLE_NAME)
						.append("(id,name) VALUES");
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.trim().split(column_separator);
				this.query_string.append(" (")
						.append(Integer.toString(Integer.parseInt(this.line_info[1])+1))
						.append(",'")
						.append(this.line_info[0].replaceAll("\\'", ""))
						.append("'),");
			}
			this.stmt = this.conn.createStatement();
			this.stmt.executeUpdate(this.query_string.substring(0, this.query_string.length()-1));
		} catch(Exception ioExc) {
			System.out.println("Error on MovielensParser::parseGenreInfo genre file read: ".concat(ioExc.toString()));
		} finally {
			try {
				if(this.stmt != null)
					this.stmt.close();
			} catch(Exception stmtEx) {
				System.out.println("Error on MovielensParser::parseGenreInfo statement close: ".concat(stmtEx.toString()));
			}
			this.query_string.setLength(0);
		}
	}
	
	public void parseMovieInfo(String absolute_path, String column_separator) {
		int loop_ctr;
		this.query_string.append("INSERT INTO ")
					.append(ProjectSettings.MOVIE_TABLE_NAME)
					.append("(id,title,release_date,imdb_url,genres) VALUES");
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.trim().split(column_separator);
				this.query_string.append(" (")
								.append(this.line_info[0])
								.append(",'")
								.append(this.line_info[1].replaceAll("'", "''"))
								.append("','");
				try {
					this.query_string.append(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MMM-yyyy").parse(this.line_info[2])));
				} catch(Exception date_convert_exception) {}
				this.query_string.append("','")
								.append(this.line_info[4].replaceAll("'", "''"))
								.append("','");
				for(loop_ctr = 5; loop_ctr < this.line_info.length; loop_ctr++)
					this.query_string.append(this.line_info[loop_ctr]);
				this.query_string.append("'),");
			}
			this.stmt = this.conn.createStatement();
			this.stmt.executeUpdate(this.query_string.substring(0, this.query_string.length()-1));			
		} catch(Exception ioExc) {
			System.out.println("Error on MovielensParser::parseMovieInfo movie file read: ".concat(ioExc.toString()));
		} finally {
			try {
				if(this.stmt != null)
					this.stmt.close();
			} catch(Exception stmtEx) {
				System.out.println("Error on MovielensParser::parseMovieInfo statement close: ".concat(stmtEx.toString()));
			}
			this.query_string.setLength(0);
		}
	}
	
	public void parseRatingInfo(String absolute_path, String column_separator) {
		this.query_string.append("INSERT INTO ")
			.append(ProjectSettings.RATING_TABLE_NAME)
			.append("(user_id,movie_id,rating) VALUES");
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.trim().split(column_separator);
				if(this.line_info.length < 3)
					continue;
				this.query_string.append(" (")
								.append(this.line_info[0])
								.append(",")
								.append(this.line_info[1])
								.append(",")
								.append(this.line_info[2])
								.append("),");
			}
			this.stmt = this.conn.createStatement();
			this.stmt.executeUpdate(this.query_string.substring(0, this.query_string.length()-1));
		} catch(Exception ioExc) {
			System.out.println("Error on MovielensParser::parseRatingInfo rating file read: ".concat(ioExc.toString()));
		} finally {
			try {
				if(this.stmt != null)
					this.stmt.close();
			} catch(Exception stmtEx) {
				System.out.println("Error on MovielensParser::parseRatingInfo statement close: ".concat(stmtEx.toString()));
			}
			this.query_string.setLength(0);
		}
	}
	
	public void parseUserInfo(String absolute_path, String column_separator) {
		this.query_string.append("INSERT INTO ")
						.append(ProjectSettings.USER_TABLE_NAME)
						.append("(id,age,gender,occupation,zipcode) VALUES");
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.trim().split(column_separator);
				this.query_string.append(" (")
						.append(this.line_info[0])
						.append(",")
						.append(this.line_info[1])
						.append(",'")
						.append(this.line_info[2])
						.append("','")
						.append(this.line_info[3])
						.append("','")
						.append(this.line_info[4])
						.append("'),");
			}
			this.stmt = this.conn.createStatement();
			this.stmt.executeUpdate(this.query_string.substring(0, this.query_string.length()-1));
		} catch(Exception ioExc) {
			System.out.println("Error on MovielensParser::parseUserInfo user file read: ".concat(ioExc.toString()));
		} finally {
			try {
				if(this.stmt != null)
					this.stmt.close();
			} catch(Exception stmtEx) {
				System.out.println("Error on MovielensParser::parseUserInfo statement close: ".concat(stmtEx.toString()));
			}
			this.query_string.setLength(0);
		}
	}
}
