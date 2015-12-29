package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IKTokenizerFactory extends AbstractTokenizerFactory {

	private boolean useSmart = true;
	
	@Inject
    public IKTokenizerFactory(Index index, IndexSettingsService indexSettings, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings.getSettings(), name, settings);

		String useSmartStr = settings.get("useSmart");
        useSmartStr = (useSmartStr == null) ? "true" : useSmartStr.trim();
        useSmartStr = (useSmartStr == "") ? "true" : useSmartStr;
        this.useSmart = Boolean.valueOf(useSmartStr);
	}




	@Override
	public Tokenizer create() {
		return new IKTokenizer(this.useSmart);
	}
}
