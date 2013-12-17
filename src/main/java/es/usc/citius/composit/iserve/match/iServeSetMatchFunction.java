package es.usc.citius.composit.iserve.match;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import es.usc.citius.composit.core.matcher.MatchTable;
import es.usc.citius.composit.core.matcher.SetMatchFunction;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;
import java.util.Set;

public class iServeSetMatchFunction implements SetMatchFunction<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;

    public iServeSetMatchFunction(ConceptMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public MatchTable<URI, LogicConceptMatchType> partialMatch(Set<URI> source, Set<URI> target) {
        return fullMatch(source, target);
    }

    @Override
    public MatchTable<URI, LogicConceptMatchType> fullMatch(Set<URI> source, Set<URI> target) {
        Table<URI, URI, MatchResult> result = this.matcher.match(source, target);
        Table<URI, URI, LogicConceptMatchType> transformed = HashBasedTable.create();
        for(Table.Cell<URI, URI, MatchResult> cell : result.cellSet()){
            transformed.put(cell.getRowKey(), cell.getColumnKey(), (LogicConceptMatchType) cell.getValue().getMatchType());
        }
        return new MatchTable<URI, LogicConceptMatchType>(transformed);
    }

    @Override
    public LogicConceptMatchType match(URI source, URI target) {
        return (LogicConceptMatchType) matcher.match(source, target).getMatchType();
    }
}
