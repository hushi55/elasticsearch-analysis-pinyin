package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

import com.ibm.icu.text.Transliterator;

public final class MixedSpellingICUTransformFilterFactory extends AbstractTokenFilterFactory {
	
	private Transliterator[] transforms = new Transliterator[1];

	@Inject
	public MixedSpellingICUTransformFilterFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);

		{
            String rules = ":: Han-Latin/Names;";
            rules += "[[:space:]][bpmfdtnlgkhjqxzcsryw] { [[:any:]-[:white_space:]] >;";
            rules += "::NFD;";
            rules += "[[:NonspacingMark:][:Space:]]>;";

            Transliterator transliterator = Transliterator.createFromRules("Han-Latin;", rules, Transliterator.FORWARD);

            transforms[0] = transliterator;
        }
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new MultipleICUTransformFilter(tokenStream, transforms);
	}
    
}