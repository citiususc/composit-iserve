package es.usc.citius.composit.iserve.match;


import es.usc.citius.composit.core.matcher.MatchFunction;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.net.URI;

public class iServeMatchFunction implements MatchFunction<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;

    public iServeMatchFunction(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public LogicConceptMatchType match(URI source, URI target) {
        Metrics.get().increment("iServeMatchFunction.match");
        return (LogicConceptMatchType) matcher.match(source, target).getMatchType();
    }
}
