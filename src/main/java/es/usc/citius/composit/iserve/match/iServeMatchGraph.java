package es.usc.citius.composit.iserve.match;


import es.usc.citius.composit.core.matcher.graph.AbstractMatchGraph;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class iServeMatchGraph extends AbstractMatchGraph<URI, LogicConceptMatchType> {

    private ConceptMatcher matcher;
    private KnowledgeBaseManager kb;

    public iServeMatchGraph(ConceptMatcher matcher, KnowledgeBaseManager kb) {
        this.matcher = matcher;
        this.kb = kb;
    }

    @Override
    public Set<URI> getElements() {
        return kb.listConcepts(null);
    }

    @Override
    public Map<URI, LogicConceptMatchType> getTargetElementsMatchedBy(URI source) {
        Map<URI, LogicConceptMatchType> match = new HashMap<URI, LogicConceptMatchType>();
        Map<URI, MatchResult> result = matcher.listMatchesAtLeastOfType(source, LogicConceptMatchType.Plugin);
        for(Map.Entry<URI, MatchResult> entry : result.entrySet()){
            // TODO: ConceptMatcher does not define a generic type for the match type.
            match.put(entry.getKey(), (LogicConceptMatchType)entry.getValue().getMatchType());
        }
        return match;
    }

    @Override
    public Map<URI, LogicConceptMatchType> getSourceElementsThatMatch(URI target) {
        throw new UnsupportedOperationException("not implemented");
    }
}
