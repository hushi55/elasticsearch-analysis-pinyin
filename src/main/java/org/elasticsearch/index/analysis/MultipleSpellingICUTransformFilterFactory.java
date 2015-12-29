package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

import com.ibm.icu.text.Transliterator;

public final class MultipleSpellingICUTransformFilterFactory extends AbstractTokenFilterFactory {
	
	private Transliterator[] transforms = new Transliterator[1];

	@Inject
	public MultipleSpellingICUTransformFilterFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);

		{
            String rules = ":: Han-Latin;";
            rules += "[[:any:]-[[:space:][\uFFFF]]] { [[:any:]-[:white_space:]] >;";
            rules += ":: Null;";
            rules += "[[:Nonspacing Mark:][:Space:]]>;";

            Transliterator transliterator = Transliterator.createFromRules("Han-Latin;", rules, Transliterator.FORWARD);

            transforms[0] = transliterator;
        }
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new MultipleICUTransformFilter(tokenStream, transforms);
//		return new ICUTransformFilter(tokenStream, transforms);
	}
    
}