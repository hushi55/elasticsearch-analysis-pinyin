/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.analysis;

/**
 */
public class PinyinAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("pinyin", PinyinAnalyzerProvider.class);
    }

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("ik", IKTokenizerFactory.class);
        tokenizersBindings.processTokenizer("pinyin", PinyinTokenizerFactory.class);
        tokenizersBindings.processTokenizer("pinyin_first_letter", PinyinAbbreviationsTokenizerFactory.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings binding) {
        binding.processTokenFilter("pinyin", PinyinTokenFilterFactory.class);

        binding.processTokenFilter("script", 					ScriptTokenFilterFactory.class);
        binding.processTokenFilter("full_spelling_icu", 		FullSpellingICUTransformFilterFactory.class);
        binding.processTokenFilter("full_spelling_token", 		FullSpellingTokenFilterFactory.class);
        binding.processTokenFilter("mixed_spelling_icu", 		MixedSpellingICUTransformFilterFactory.class);
        binding.processTokenFilter("mutiple_icu", 				MultipleICUTransformFilterFactory.class);
        binding.processTokenFilter("mutiple_spelling_icu", 		MultipleSpellingICUTransformFilterFactory.class);
        binding.processTokenFilter("mutiple_spelling_token",	MultipleSpellingTokenFilterFactory.class);
        binding.processTokenFilter("mobile_keyboard_number",	MobileKeyboardNumberSpellingTokenFilterFactory.class);


    }
}
