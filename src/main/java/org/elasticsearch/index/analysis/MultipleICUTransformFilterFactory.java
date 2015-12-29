package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

import com.ibm.icu.text.Transliterator;

public final class MultipleICUTransformFilterFactory extends AbstractTokenFilterFactory {
	
	private Transliterator[] transforms = new Transliterator[3];

	@Inject
	public MultipleICUTransformFilterFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);

		{
            String identifier = "Han-Latin;";
            identifier += "NFD;";
            identifier += "[[:Nonspacing Mark:][:Space:]] Remove";

            Transliterator transliterator = Transliterator.getInstance(identifier);

            transforms[0] = transliterator;
        }
        {
            String rules = ":: Han-Latin;";
            rules += "[[:any:]-[[:space:][\uFFFF]]] { [[:any:]-[:white_space:]] >;";
            rules += ":: Null;";
            rules += "[[:Nonspacing Mark:][:Space:]]>;";

            Transliterator transliterator = Transliterator.createFromRules("Han-Latin;", rules, Transliterator.FORWARD);

            transforms[1] = transliterator;
        }
        {
            String rules = ":: Han-Latin/Names;";
            rules += "[[:space:]][bpmfdtnlgkhjqxzcsryw] { [[:any:]-[:white_space:]] >;";
            rules += "::NFD;";
            rules += "[[:NonspacingMark:][:Space:]]>;";

            Transliterator transliterator = Transliterator.createFromRules("Han-Latin;", rules, Transliterator.FORWARD);

            transforms[2] = transliterator;
        }
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new MultipleICUTransformFilter(tokenStream, transforms);
	}
    
}