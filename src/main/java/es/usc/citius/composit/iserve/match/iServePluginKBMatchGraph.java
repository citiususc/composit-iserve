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


import es.usc.citius.composit.core.matcher.graph.AbstractMatchGraph;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MatchGraph implementation using a Knowledge base manager to resolve exact/plugin matches.
 */
public class iServePluginKBMatchGraph extends AbstractMatchGraph<URI, LogicConceptMatchType> {
    private KnowledgeBaseManager kb;
    private URI ontologyGraph;

    @Inject
    public iServePluginKBMatchGraph(URI ontologyGraph, KnowledgeBaseManager kb) {
        this.ontologyGraph = ontologyGraph;
        this.kb = kb;
    }

    @Override
    public Set<URI> getElements() {
        return kb.listConcepts(ontologyGraph);
    }

    @Override
    public Map<URI, LogicConceptMatchType> getTargetElementsMatchedBy(URI source) {
        Metrics.get().increment("iServePluginKBMatchGraph.getTargetElementsMatchedBy");
        Map<URI, LogicConceptMatchType> matchMapping = new HashMap<URI, LogicConceptMatchType>();
        matchMapping.put(source, LogicConceptMatchType.Exact);
        for(URI superclass : kb.listSuperClasses(source, false)){
            matchMapping.put(superclass, LogicConceptMatchType.Plugin);
        }
        return matchMapping;
    }

    @Override
    public Map<URI, LogicConceptMatchType> getSourceElementsThatMatch(URI target) {
        Metrics.get().increment("iServePluginKBMatchGraph.getSourceElementsThatMatch");
        Map<URI, LogicConceptMatchType> matchMapping = new HashMap<URI, LogicConceptMatchType>();
        matchMapping.put(target, LogicConceptMatchType.Exact);
        for(URI subclass : kb.listSubClasses(target, false)){
            matchMapping.put(subclass, LogicConceptMatchType.Plugin);
        }
        return matchMapping;
    }
}
