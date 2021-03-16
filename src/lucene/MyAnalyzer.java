package lucene;

import java.io.IOException;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.id.IndonesianStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

public class MyAnalyzer extends StopwordAnalyzerBase{
	public final static String DEFAULT_STOPWORD_FILE = "/lucene/stopwords.txt";
	protected final CharArraySet stemExclusionSet;
	private boolean stemDerivational = true;

	public static CharArraySet getDefaultStopSet(){
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}
	  
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;

		static {
			try {
				DEFAULT_STOP_SET = loadStopwordSet(false, 
						MyAnalyzer.class, 
	        		DEFAULT_STOPWORD_FILE, 
	        		"#");
			} catch (IOException ex) {
				// default set should always be present as it is part of the
				// distribution (JAR)
				throw new RuntimeException("Unable to load default stopword set");
			}
	    }
	}
	  
	public MyAnalyzer() {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}
	
	public MyAnalyzer(CharArraySet stopwords){
		this(stopwords, CharArraySet.EMPTY_SET);
	}
	
	public MyAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet){
	    super(stopwords);
	    this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
	}
	
	@Override
	protected TokenStreamComponents createComponents(String paramString) {
		final Tokenizer source = new StandardTokenizer();
		TokenStream result = new LowerCaseFilter(source);
		result = new StopFilter(result, stopwords);
		result = new IndonesianStemFilter(result, stemDerivational);
		result = new SynonymGraphFilter(result, getSynonyms(), true);
		result = new FlattenGraphFilter(result);
		
		return new TokenStreamComponents(source, result);
	}
	
	private static SynonymMap getSynonyms() {
	    // de-duplicate rules when loading:
	    boolean dedup = Boolean.TRUE;
	    // include original word in index:
	    boolean includeOrig = Boolean.TRUE;

	    SynonymMap.Builder builder = new SynonymMap.Builder(dedup);

	    // examples of single synonyms:
	    builder.add(new CharsRef("ugm"), new CharsRef("universitas gajah mada"), includeOrig);

	    // example with multiple synonyms:
	    CharsRefBuilder multiWordCharsRef = new CharsRefBuilder();
	    SynonymMap.Builder.join(new String[]{"it", "ti", "ilkom"}, multiWordCharsRef);
	    builder.add(new CharsRef("informatika"), multiWordCharsRef.get(), includeOrig);
	    
	    SynonymMap.Builder.join(new String[]{"universitas", "gajah", "mada"}, multiWordCharsRef);
	    builder.add(new CharsRef("ugm"), multiWordCharsRef.get(), includeOrig);

	    SynonymMap synonymMap = null;
	    try {
	        synonymMap = builder.build();
	    } catch (IOException ex) {
	        System.err.print(ex);
	    }
	    return synonymMap;
	}

}
