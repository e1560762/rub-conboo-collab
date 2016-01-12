package parse;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import config.ProjectSettings;

public class CustomAnalyzer extends Analyzer {
	private TokenStream ts;
	private HashSet<String> stopwords;
	private CharArraySet cas;
	private HashMap<String, Integer> count_keeper;
	private String current_token;
	public CustomAnalyzer() {
		this.stopwords = new HashSet<String>();
		this.stopwords.add("and");
		this.stopwords.add("or");
		this.stopwords.add("the");
		this.stopwords.add("a");
		this.stopwords.add("in");
		this.cas = new CharArraySet(stopwords, true);
		this.count_keeper = new HashMap<String, Integer>();
	}
	
	public void tokenizeString(String source, DBCollection mongo_collection, int movie_id, String slot_name) {
		this.ts = this.tokenStream("afield", source);
		this.ts = new LowerCaseFilter(this.ts);
		this.ts = new StopFilter(this.ts, this.cas);
		this.ts = new PorterStemFilter(this.ts);
		BasicDBObject updated_values = new BasicDBObject();
		try {
			this.ts.reset();
			while (ts.incrementToken()){
				this.current_token = slot_name.concat(".")
										.concat(ts.getAttribute(CharTermAttribute.class).toString());
				if(updated_values.containsKey(this.current_token) == false) 
					updated_values.put(this.current_token, 1);
				else
					updated_values.put(this.current_token, updated_values.getInt(this.current_token)+1);
				
				//System.out.print(ts.getAttribute(CharTermAttribute.class).toString().concat(" "));
			}
			mongo_collection.update(new BasicDBObject().append(ProjectSettings.MONGO_MOVIES_TABLE_MOVIE_ID_KEY, movie_id), new BasicDBObject().append("$inc", updated_values), true, false);
		} catch(Exception tsIterationEx) {
			System.out.println("Error on tokenstream iteration: ".concat(tsIterationEx.toString()));
		} finally {
			if(this.ts != null) try{this.ts.close();} catch(Exception tsExc) {System.out.println("Error on tokenstream close: ".concat(tsExc.toString()));} 
		}
	}
	
	@Override
	public void close() {
		if(this.ts != null) try{this.ts.close();} catch(Exception tsExc) {System.out.println("Error on tokenstream close: ".concat(tsExc.toString()));} 
		this.cas.clear();
		this.cas.clear();
		super.close();
	}
	@Override
	protected TokenStreamComponents createComponents(String fieldname) {
		// TODO Auto-generated method stub
		return new TokenStreamComponents(new WhitespaceTokenizer());
	}

}
