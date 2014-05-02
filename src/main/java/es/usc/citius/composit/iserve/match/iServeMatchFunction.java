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


import es.usc.citius.composit.core.matcher.MatchFunction;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import javax.inject.Inject;
import java.net.URI;

public class iServeMatchFunction implements MatchFunction<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;

    @Inject
    public iServeMatchFunction(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public LogicConceptMatchType match(URI source, URI target) {
        Metrics.get().increment("iServeMatchFunction.match");
        return (LogicConceptMatchType) matcher.match(source, target).getMatchType();
    }
}
