/*
 * Copyright (c) 2014.
 * Centro de Investigación en Tecnoloxías da Información (CITIUS), University of Santiago de Compostela (USC)
 * Knowledge Media Institute (KMi) - The Open University (OU)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.usc.citius.composit.iserve.match;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import es.usc.citius.composit.core.matcher.graph.AbstractMatchGraph;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class iServeMatchGraph extends AbstractMatchGraph<URI, LogicConceptMatchType> implements MatchGraph<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;
    private KnowledgeBaseManager kb;
    private LoadingCache<URI, Map<URI, LogicConceptMatchType>> cacheSource;
    private LoadingCache<URI, Map<URI, LogicConceptMatchType>> cacheTarget;

    @Inject
    public iServeMatchGraph(ConceptMatcher matcher, KnowledgeBaseManager kb) {
        this(matcher, kb, 0);
    }

    public iServeMatchGraph(ConceptMatcher matcher, KnowledgeBaseManager kb, final int cacheSize) {
        this.matcher = matcher;
        this.kb = kb;

        // Configure caches
        this.cacheTarget = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(new CacheLoader<URI, Map<URI, LogicConceptMatchType>>() {
                    @Override
                    public Map<URI, LogicConceptMatchType> load(URI key) throws Exception {
                        return computeTargetElementsMatchedBy(key);
                    }
                });

        this.cacheSource = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(new CacheLoader<URI, Map<URI, LogicConceptMatchType>>() {
                    @Override
                    public Map<URI, LogicConceptMatchType> load(URI key) throws Exception {
                        return computeSourceElementsThatMatch(key);
                    }
                });
    }

    @Override
    public Set<URI> getElements() {
        return kb.listConcepts(null);
    }

    private  Map<URI, LogicConceptMatchType> computeTargetElementsMatchedBy(URI source) {
        Metrics.get().increment("iServeMatchGraph.getTargetElementsMatchedBy");
        Map<URI, LogicConceptMatchType> match = new HashMap<URI, LogicConceptMatchType>();
        Map<URI, MatchResult> result = matcher.listMatchesAtLeastOfType(source, LogicConceptMatchType.Plugin);
        for(Map.Entry<URI, MatchResult> entry : result.entrySet()){
            // TODO: ConceptMatcher does not define a generic type for the match type.
            match.put(entry.getKey(), (LogicConceptMatchType)entry.getValue().getMatchType());
        }
        return match;
    }

    @Override
    public Map<URI, LogicConceptMatchType> getTargetElementsMatchedBy(URI source) {
        return this.cacheTarget.getUnchecked(source);
    }

    private Map<URI, LogicConceptMatchType> computeSourceElementsThatMatch(URI target) {
        Metrics.get().increment("iServeMatchGraph.getTargetElementsThatMatch");
        // Get elements that can match the specific target
        Map<URI, LogicConceptMatchType> match = new HashMap<URI, LogicConceptMatchType>();
        Map<URI, MatchResult> resultSubsume = matcher.listMatchesOfType(target, LogicConceptMatchType.Subsume);
        Map<URI, MatchResult> resultExact = matcher.listMatchesOfType(target, LogicConceptMatchType.Exact);
        // TODO: ConceptMatcher does not define a generic type for the match type.
        for(Map.Entry<URI, MatchResult> entry : resultSubsume.entrySet()){
            match.put(entry.getKey(), (LogicConceptMatchType)entry.getValue().getMatchType());
        }
        for(Map.Entry<URI, MatchResult> entry : resultExact.entrySet()){
            match.put(entry.getKey(), (LogicConceptMatchType)entry.getValue().getMatchType());
        }
        return match;
    }

    @Override
    public Map<URI, LogicConceptMatchType> getSourceElementsThatMatch(URI target) {
        return this.cacheSource.getUnchecked(target);
    }
}
