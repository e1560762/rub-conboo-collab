package parse;

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

public class CustomAnalyzer extends Analyzer {
	private TokenStream ts;
	private HashSet<String> stopwords;
	private CharArraySet cas;
	
	public CustomAnalyzer() {
		this.stopwords = new HashSet<String>();
		this.stopwords.add("and");
		this.stopwords.add("or");
		this.stopwords.add("the");
		this.stopwords.add("a");
		this.stopwords.add("in");
		this.cas = new CharArraySet(stopwords, true);
	}
	
	public void tokenizeString(String source) {
		this.ts = this.tokenStream("afield", source);
		this.ts = new LowerCaseFilter(this.ts);
		this.ts = new StopFilter(this.ts, this.cas);
		this.ts = new PorterStemFilter(this.ts);
		try {
			this.ts.reset();
			while (ts.incrementToken()){
				System.out.print(ts.getAttribute(CharTermAttribute.class).toString().concat(" "));
			}
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
