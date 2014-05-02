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


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import es.usc.citius.composit.core.matcher.MatchTable;
import es.usc.citius.composit.core.matcher.SetMatchFunction;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;
import java.util.Set;

public class iServeSetMatchFunction implements SetMatchFunction<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;

    @Inject
    public iServeSetMatchFunction(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public MatchTable<URI, LogicConceptMatchType> partialMatch(Set<URI> source, Set<URI> target) {
        Metrics.get().increment("iServeSetMatchFunction.partialMatch");
        return fullMatch(source, target);
    }

    @Override
    public MatchTable<URI, LogicConceptMatchType> fullMatch(Set<URI> source, Set<URI> target) {
        Metrics.get().increment("iServeSetMatchFunction.fullMatch");
        Table<URI, URI, MatchResult> result = this.matcher.match(source, target);
        Table<URI, URI, LogicConceptMatchType> transformed = HashBasedTable.create();
        for(Table.Cell<URI, URI, MatchResult> cell : result.cellSet()){
            transformed.put(cell.getRowKey(), cell.getColumnKey(), (LogicConceptMatchType) cell.getValue().getMatchType());
        }
        return new MatchTable<URI, LogicConceptMatchType>(transformed);
    }

    @Override
    public LogicConceptMatchType match(URI source, URI target) {
        Metrics.get().increment("iServeSetMatchFunction.match");
        return (LogicConceptMatchType) matcher.match(source, target).getMatchType();
    }
}
