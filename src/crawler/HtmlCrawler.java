package crawler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import elements.Movie;

public class HtmlCrawler {
	private List<Movie> movies;
	private Document doc;
	//private Cleaner doc_cleaner;
	
	public HtmlCrawler() {
		this.movies = new ArrayList<Movie>();
		//this.doc_cleaner = new Cleaner(Whitelist.basic());
	}
	public void crawlMovie(String url) {
		try {
			 this.doc = Jsoup.connect(url).get();
			 for(Element elem : this.doc.select(".cast_list .odd")) {
				 System.out.println(elem.getElementsByAttributeValue("itemprop", "name").html());
			 }
			 for(Element elem : this.doc.select(".cast_list .even")) {
				 System.out.println(elem.getElementsByAttributeValue("itemprop", "name").html());
			 }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
