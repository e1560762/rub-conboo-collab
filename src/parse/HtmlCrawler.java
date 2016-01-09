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

import config.ProjectSettings;
import elements.Movie;

public class HtmlCrawler {
	private List<Movie> movies;
	private Document doc;
	private String base_url;
	private String current_url;
	private StringBuilder sb;
	//private Cleaner doc_cleaner;
	
	public HtmlCrawler(String url) {
		this.movies = new ArrayList<Movie>();
		try {
			this.base_url = Jsoup.connect(url)
			 		.userAgent(ProjectSettings.USER_AGENT)
			 		.followRedirects(true)
			 		.execute().url().toString();
		} catch(Exception connectExc) {
			this.base_url = url;
		}
		this.sb = new StringBuilder(100);
		//this.doc_cleaner = new Cleaner(Whitelist.basic());
	}
	public void crawlMovieCast(String suffix) {
		this.current_url = this.base_url.charAt(this.base_url.length()-1) == '/' ? this.base_url.concat(suffix) 
								: this.base_url.concat("/").concat(suffix);
		try {
			this.doc = Jsoup.connect(current_url)
				 		.userAgent(ProjectSettings.USER_AGENT)
				 		.followRedirects(true)
				 		.get();

			for(Element elem : this.doc.select(".cast_list .odd")) {
				this.sb.append(elem.getElementsByAttributeValue("itemprop", "name").html().toLowerCase().replaceAll(" ", "_"))
					.append(" ");
			}
			for(Element elem : this.doc.select(".cast_list .even")) {
				this.sb.append(elem.getElementsByAttributeValue("itemprop", "name").html().toLowerCase().replaceAll(" ", "_"))
					.append(" ");
			}
			
			/* convert sb to string and save to mongodb as player slot*/
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
			
			for(Element elem : this.doc.select("[itemprop=director] [itemprop=name]")) {
				this.sb.append(elem.html().toLowerCase().replaceAll(" ", "_"))
					.append(" ");
			}
			/* convert sb to string and save to mongodb as director slot*/
			this.sb.setLength(0);
			
			for(Element elem : this.doc.select("#title_recs .rec_overview .rec-title")) {
				this.sb.append(elem.getElementsByTag("a").first().text()
						.toLowerCase()
						.replaceAll(" ", "_")
						.concat("_")
						.concat(elem.getElementsByClass("nobr").first().html()))
						.append(" ");
			}
			/* convert sb to string and save to mongodb as related_movies slot*/
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
			CustomAnalyzer test_analyzer = new CustomAnalyzer();
			test_analyzer.tokenizeString(this.doc.getElementById("swiki.2.1").text().replaceAll("[.,()]", ""));
			test_analyzer.close();
			
			//System.out.println(this.doc.getElementById("swiki.2.1").text());
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
				this.sb.append(elem.text().replaceAll("[.,()]", "")).append(" ");
			}
			CustomAnalyzer test_analyzer = new CustomAnalyzer();
			test_analyzer.tokenizeString(this.sb.toString());
			test_analyzer.close();
			
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
				this.sb.append(elem.text().replaceAll("[.,()]", "")).append("\n");
			}
			length_of_string = this.sb.length();
			/* remove add another review line*/
			this.sb = this.sb.delete(length_of_string-20, length_of_string);
			
			CustomAnalyzer test_analyzer = new CustomAnalyzer();
			test_analyzer.tokenizeString(this.sb.toString());
			test_analyzer.close();
			
			this.sb.setLength(0);
			if(Integer.parseInt(this.doc.select("#tn15content tbody [align=right]").first().text().split(" ")[0]) > offset + 10) {
				this.crawlMovieComments(suffix, offset+10);
			}
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
}
