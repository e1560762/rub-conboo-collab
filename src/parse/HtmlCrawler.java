package parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import config.ProjectSettings;
import elements.Movie;

public class HtmlCrawler {
	private int movie_id;
	private String movie_title;
	//private List<Movie> movies;
	private Document doc;
	private String base_url;
	private String current_url;
	private StringBuilder sb;
	private CustomAnalyzer test_analyzer;
	private DB mongo_db;
	private String current_string;
	//private Cleaner doc_cleaner;
	
	public HtmlCrawler(String url, DB mng_db, int movie_id, String title) {
		//this.movies = new ArrayList<Movie>();
		try {
			this.base_url = Jsoup.connect(url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.execute().url().toString();
		} catch(Exception connectExc) {
			this.base_url = url;
		}
		try {
			Document d = Jsoup.connect(this.base_url)
					.userAgent(ProjectSettings.USER_AGENT)
					.followRedirects(true)
					.get();
			if(d.select("[itemprop=director] [itemprop=name]").isEmpty()) {
					System.out.println("NOT FOUND " + movie_id);
					String s = d.select(".result_text a[href]").attr("href");
					s = s.substring(0, s.indexOf('?')-1);
					this.base_url = "http://us.imdb.com" + s;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sb = new StringBuilder(100);
		this.test_analyzer = new CustomAnalyzer();
		this.mongo_db = mng_db;
		this.movie_id = movie_id;
		this.movie_title = title.replaceAll(" ", "_");
		//this.doc_cleaner = new Cleaner(Whitelist.basic());
	}
	
	public void crawlMovieData(int mode) {
		switch (mode) {
		case 0:
			this.crawlMovieDirectorAndRelatedMovies();
			break;
		case 1:
			this.crawlMovieCast("fullcredits");
			break;
		case 2:
			this.crawlMovieSynopsis("synopsis");
			break;
		case 3:
			this.crawlMoviePlotSummary("plotsummary");
			break;
		case 4:
			this.crawlMovieComments("reviews?start=", 0);
			break;
		case 5:
			this.crawlMovieDirectorAndRelatedMovies();
			this.crawlMovieCast("fullcredits");
			break;
		case 6:
			this.crawlMovieSynopsis("synopsis");
			this.crawlMoviePlotSummary("plotsummary");
			this.crawlMovieComments("reviews?start=", 0);
			break;
		default:
			this.crawlMovieDirectorAndRelatedMovies();
			this.crawlMovieCast("fullcredits");
			this.crawlMovieSynopsis("synopsis");
			this.crawlMoviePlotSummary("plotsummary");
			this.crawlMovieComments("reviews?start=", 0);
			break;
		}
		
	}
	public void crawlMovieCast(String suffix) {
		this.current_url = this.base_url.charAt(this.base_url.length()-1) == '/' ? this.base_url.concat(suffix) 
								: this.base_url.concat("/").concat(suffix);
		try {
			this.doc = Jsoup.connect(current_url)
				 		.userAgent(ProjectSettings.USER_AGENT)
				 		.followRedirects(true)
				 		.get();
			BasicDBObject updated_values = new BasicDBObject();
			for(Element elem : this.doc.select(".cast_list .odd")) {
				this.current_string = elem.getElementsByAttributeValue("itemprop", "name").html().toLowerCase().replaceAll("\\.\\s|\\s", "_"); 
				this.sb.append(this.current_string).append(" ");
				this.current_string = ProjectSettings.MONGO_MOVIES_TABLE_PLAYER_KEY.concat(".").concat(this.current_string);
				updated_values.put(this.current_string, 1);
			}
			for(Element elem : this.doc.select(".cast_list .even")) {
				this.current_string = elem.getElementsByAttributeValue("itemprop", "name").html().toLowerCase().replaceAll("\\.\\s|\\s", "_");
				this.sb.append(this.current_string).append(" ");
				this.current_string = ProjectSettings.MONGO_MOVIES_TABLE_PLAYER_KEY.concat(".").concat(this.current_string);
				updated_values.put(this.current_string, 1);
			}
			
			/* convert sb to string and save to mongodb as player slot*/
			this.updateWordCountInMongo(this.mongo_db.getCollection(ProjectSettings.MONGO_MOVIE_COLLECTION_NAME),
					new BasicDBObject().append(ProjectSettings.MONGO_MOVIES_TABLE_MOVIE_ID_KEY, this.movie_id),
					new BasicDBObject().append("$inc", updated_values) 
							);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sb.setLength(0);
	}
	
	public void crawlMovieDirectorAndRelatedMovies() {
		try {
			this.doc = Jsoup.connect(this.base_url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.get();
			this.sb.append(this.movie_title).append(" ");
			BasicDBObject updated_values = new BasicDBObject();
			updated_values.put(ProjectSettings.MONGO_MOVIES_TABLE_TITLE_KEY.concat(".").concat(this.movie_title), 1);
			updated_values.put(ProjectSettings.MONGO_MOVIES_TABLE_RELATED_TITLE_KEY.concat(".").concat(this.movie_title), 1);
			
			for(Element elem : this.doc.select("[itemprop=director] [itemprop=name]")) {
				this.current_string = elem.html().toLowerCase().replaceAll("\\.\\s|\\s", "_");
				this.sb.append(this.current_string).append(" ");
				updated_values.put(ProjectSettings.MONGO_MOVIES_TABLE_DIRECTOR_KEY.concat(".").concat(this.current_string), 1);
				updated_values.put(ProjectSettings.MONGO_MOVIES_TABLE_RELATED_DIRECTOR_KEY.concat(".").concat(this.current_string), 1);
			}
			/* convert sb to string and save to mongodb as director slot*/
			this.sb.setLength(0);
			
			for(Element elem : this.doc.select("#title_recs .rec_overview")) {
				try {
				this.current_string = elem.select(".rec-title a").first().text()
										.toLowerCase()
										.replaceAll("\\.\\s|\\s", "_")
										.concat("_")
										.concat(elem.getElementsByClass("nobr").first().html());
				this.sb.append(this.current_string).append(" ");
				} catch(Exception xx){}
				try{
				this.current_string = ProjectSettings.MONGO_MOVIES_TABLE_RELATED_TITLE_KEY.concat(".").concat(this.current_string);
				updated_values.put(this.current_string, 1);
				} catch(Exception xx){}
				try {
				this.current_string = elem.select(".rec-jaw-lower .rec-director").first().text()
										.substring(10)
										.toLowerCase()
										.replaceAll("\\.\\s|\\s", "_");
				} catch(Exception xx){}
				this.sb.append(this.current_string).append(" ");
				
				this.current_string = ProjectSettings.MONGO_MOVIES_TABLE_RELATED_DIRECTOR_KEY.concat(".").concat(this.current_string);
				if(updated_values.containsKey(this.current_string))
					updated_values.put(this.current_string, updated_values.getInt(this.current_string) + 1);
				else
					updated_values.put(this.current_string, 1);
			}
			/* convert sb to string and save to mongodb as related_movies slot*/
			this.updateWordCountInMongo(this.mongo_db.getCollection(ProjectSettings.MONGO_MOVIE_COLLECTION_NAME),
					new BasicDBObject().append(ProjectSettings.MONGO_MOVIES_TABLE_MOVIE_ID_KEY, this.movie_id),
					new BasicDBObject().append("$inc", updated_values) 
							);
			this.sb.setLength(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void crawlMovieSynopsis(String suffix) {
		this.current_url = this.base_url.charAt(this.base_url.length()-1) == '/' ? this.base_url.concat(suffix) 
				: this.base_url.concat("/").concat(suffix);
		try {
			this.doc = Jsoup.connect(current_url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.get();
			
			this.test_analyzer.tokenizeString(this.doc.getElementById("swiki.2.1").text().replaceAll("[.,()!{}*\\[\\];:'\"]", "")
					, this.mongo_db.getCollection(ProjectSettings.MONGO_MOVIE_COLLECTION_NAME), this.movie_id, ProjectSettings.MONGO_MOVIES_TABLE_CONTENT_KEY);			
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
	public void crawlMoviePlotSummary(String suffix) {
		this.current_url = this.base_url.charAt(this.base_url.length()-1) == '/' ? this.base_url.concat(suffix) 
				: this.base_url.concat("/").concat(suffix);
		try {
			this.doc = Jsoup.connect(current_url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.get();
			for(Element elem : this.doc.select(".zebraList .plotSummary")) {
				this.sb.append(elem.text().replaceAll("[.,()!{}*\\[\\];:'\"]", "")).append(" ");
			}
			this.test_analyzer.tokenizeString(this.sb.toString()
					, this.mongo_db.getCollection(ProjectSettings.MONGO_MOVIE_COLLECTION_NAME), this.movie_id, ProjectSettings.MONGO_MOVIES_TABLE_CONTENT_KEY);						
			this.sb.setLength(0);
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
	public void crawlMovieComments(String suffix, int offset) {
		this.current_url = this.base_url.charAt(this.base_url.length()-1) == '/' ? this.base_url.concat(suffix).concat(Integer.toString(offset))
				: this.base_url.concat("/").concat(suffix).concat(Integer.toString(offset));
		try {
			int length_of_string = 0;
			this.doc = Jsoup.connect(current_url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.get();
			for(Element elem : this.doc.select("#tn15content p")) {
				this.sb.append(elem.text().replaceAll("[.,()!{}*\\[\\];:'\"]", "")).append("\n");
			}
			length_of_string = this.sb.length();
			/* remove add another review line*/
			this.sb = this.sb.delete(length_of_string-20, length_of_string);

			this.test_analyzer.tokenizeString(this.sb.toString()
					, this.mongo_db.getCollection(ProjectSettings.MONGO_MOVIE_COLLECTION_NAME), this.movie_id, ProjectSettings.MONGO_MOVIES_TABLE_CONTENT_KEY);
			
			this.sb.setLength(0);
			if(Integer.parseInt(this.doc.select("#tn15content tbody [align=right]").first().text().split(" ")[0]) > offset + 10) {
				this.crawlMovieComments(suffix, offset+10);
			}
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
	public void setUrlIdTitle(String newUrl, int newId, String newTitle) {
		try {
			this.base_url = Jsoup.connect(newUrl)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.execute().url().toString();
		} catch(Exception connectExc) {
			this.base_url = newUrl;
		}
		this.movie_id = newId;
		this.movie_title = newTitle;
	}
	
	/* Private methods */
	private boolean updateWordCountInMongo(DBCollection mongo_collection, BasicDBObject searched_obj, BasicDBObject updated_obj) {
		try {
			mongo_collection.update(searched_obj, updated_obj, true, false);
			return true;
		} catch(Exception e) {
			System.out.println("Error on mongo db update in updateWordCountInMongo: ".concat(e.toString()));
			return false;
		}
	}
}
