package edu.bu;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;

public class Stemmer extends Analyzer {
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new PorterStemFilter(new LowerCaseTokenizer(reader));
	}
}
