package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

public class MultipleSpellingTokenFilterFactory extends AbstractTokenFilterFactory  {

	@Inject
	public MultipleSpellingTokenFilterFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new MultipleSpellingTokenFilter(tokenStream);
	}

   
}
