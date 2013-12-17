package es.usc.citius.composit.iserve.match;


import es.usc.citius.composit.core.matcher.MatchFunction;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;

public class iServeMatchFunction implements MatchFunction<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;

    public iServeMatchFunction(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public LogicConceptMatchType match(URI source, URI target) {
        return (LogicConceptMatchType) matcher.match(source, target).getMatchType();
    }
}
