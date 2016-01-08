package parse;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import config.ProjectSettings;
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
		System.out.println("Given URL: ".concat(url));
		StringBuilder sb = new StringBuilder(100);
		try {
			StandardAnalyzer test_analyzer;
			TokenStream ts;
			HashSet<String> stopwords = new HashSet<String>();
			CharArraySet cas;
			this.doc = Jsoup.connect(url)
				 		.userAgent(ProjectSettings.USER_AGENT)
				 		.followRedirects(true)
				 		.get();
			for(Element elem : this.doc.select(".cast_list .odd")) {
				sb.append(elem.getElementsByAttributeValue("itemprop", "name").html())
					.append(" ");
			}
			for(Element elem : this.doc.select(".cast_list .even")) {
				sb.append(elem.getElementsByAttributeValue("itemprop", "name").html())
					.append(" ");
			}
			stopwords.add("and");
			stopwords.add("or");
			stopwords.add("the");
			stopwords.add("a");
			stopwords.add("in");
			test_analyzer = new StandardAnalyzer();
			ts = test_analyzer.tokenStream("aField", sb.toString());
			ts = new LowerCaseFilter(ts);
			cas = new CharArraySet(stopwords, true);
			ts = new StopFilter(ts, cas);
			ts = new PorterStemFilter(ts);
			ts.reset();
			while (ts.incrementToken()){
				CharTermAttribute ca = ts.getAttribute(CharTermAttribute.class);
				System.out.println(ca.toString());
			}
			ts.close();
			test_analyzer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
