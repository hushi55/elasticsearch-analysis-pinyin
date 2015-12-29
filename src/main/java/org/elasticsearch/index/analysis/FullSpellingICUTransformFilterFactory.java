package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

import com.ibm.icu.text.Transliterator;

public final class FullSpellingICUTransformFilterFactory extends AbstractTokenFilterFactory {
	
	private Transliterator[] transforms = new Transliterator[1];

	@Inject
	public FullSpellingICUTransformFilterFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);

		{
            String identifier = "Han-Latin;";
            identifier += "NFD;";
            identifier += "[[:Nonspacing Mark:][:Space:]] Remove";

            Transliterator transliterator = Transliterator.getInstance(identifier);

            transforms[0] = transliterator;
        }
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new MultipleICUTransformFilter(tokenStream, transforms);
	}
    
}