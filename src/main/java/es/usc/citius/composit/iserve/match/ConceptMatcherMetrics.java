package es.usc.citius.composit.iserve.match;


import com.google.common.collect.Table;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public class ConceptMatcherMetrics implements ConceptMatcher {
    private ConceptMatcher matcher;

    public ConceptMatcherMetrics(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String getMatcherDescription() {
        return matcher.getMatcherDescription();
    }

    @Override
    public String getMatcherVersion() {
        return matcher.getMatcherVersion();
    }

    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return matcher.getMatchTypesSupported();
    }

    @Override
    public MatchResult match(URI uri, URI uri2) {
        Metrics.get().increment("ConceptMatcherMetrics.match(uri,uri)");
        return matcher.match(uri, uri2);
    }

    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> uris, Set<URI> uris2) {
        Metrics.get().increment("ConceptMatcherMetrics.match(set,set)");
        return matcher.match(uris, uris2);
    }

    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI uri, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesOfType");
        return matcher.listMatchesOfType(uri, matchType);
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI uri, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesAtLeastOfType");
        return matcher.listMatchesAtLeastOfType(uri, matchType);
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI uri, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesAtMostOfType");
        return matcher.listMatchesAtMostOfType(uri, matchType);
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI uri, MatchType matchType, MatchType matchType2) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesWithinRange");
        return matcher.listMatchesWithinRange(uri, matchType, matchType2);
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> uris, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesAtLeastOfType");
        return matcher.listMatchesAtLeastOfType(uris, matchType);
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> uris, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesAtMostOfType");
        return matcher.listMatchesAtMostOfType(uris, matchType);
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> uris, MatchType matchType, MatchType matchType2) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesWithinRange");
        return matcher.listMatchesWithinRange(uris, matchType, matchType2);
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> uris, MatchType matchType) {
        Metrics.get().increment("ConceptMatcherMetrics.listMatchesOfType");
        return matcher.listMatchesOfType(uris, matchType);
    }
}
