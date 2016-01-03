package parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import config.ProjectSettings;

public class MovielensParser {
	private String[] line_info;  
	private Connection conn;
	private Statement stmt;
	private ResultSet resultset;
	private String query_string;
	
	public MovielensParser(Connection connection_instance) {this.conn = connection_instance;}
	
	public void parseGenreInfo(String absolute_path, String column_separator) {
		this.query_string = "INSERT INTO ".concat(ProjectSettings.GENRE_TABLE_NAME)
							.concat("(id,name) VALUES");
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.split(column_separator);
				this.query_string = this.query_string.concat(" (")
						.concat(Integer.toString(Integer.parseInt(this.line_info[1])+1))
						.concat(",'")
						.concat(this.line_info[0].replaceAll("\\'", ""))
						.concat("'),");
			}
			this.query_string = this.query_string.substring(0, this.query_string.length()-1);
			this.stmt = this.conn.createStatement();
			this.stmt.executeUpdate(this.query_string);
		} catch(Exception ioExc) {
			System.out.println("Error on MovielensParser::parseGenreInfo genre file read: ".concat(ioExc.toString()));
		} finally {
			try {
				if(this.stmt != null)
					this.stmt.close();
			} catch(Exception stmtEx) {
				System.out.println("Error on MovielensParser::parseGenreInfo statement close: ".concat(stmtEx.toString()));
			}
		}
	}
	
	public void parseMovieInfo(String absolute_path, String column_separator) {
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine().trim()) != null;) {
				this.line_info = line.split(column_separator);
			}
		} catch(Exception ioExc) {
			System.out.println("Error on movie file read: ".concat(ioExc.toString()));
		}
	}
	
	public void parseRatingInfo(String absolute_path, String column_separator) {
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine().trim()) != null;) {
				this.line_info = line.split(column_separator);
			}
		} catch(Exception ioExc) {
			System.out.println("Error on rating file read: ".concat(ioExc.toString()));
		}
	}
	
	public void parseUserInfo(String absolute_path, String column_separator) {
		try(BufferedReader br = new BufferedReader(new FileReader(absolute_path))) {
			for(String line; (line = br.readLine()) != null;) {
				this.line_info = line.trim().split(column_separator);
			}
		} catch(Exception ioExc) {
			System.out.println("Error on user file read: ".concat(ioExc.toString()));
		}
	}
}
