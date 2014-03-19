package es.usc.citius.composit.iserve.discovery;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.knowledge.Concept;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.core.model.impl.ResourceOperation;
import es.usc.citius.composit.core.model.impl.SignatureIO;
import es.usc.citius.composit.wsc08.data.knowledge.WSCXMLKnowledgeBase;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WSCInputDiscoveryUriAdapter implements InputDiscoverer<URI> {
    private InputDiscoverer<Concept> discoverer;
    private WSCXMLKnowledgeBase kb;
    private final String baseUri;

    public WSCInputDiscoveryUriAdapter(InputDiscoverer<Concept> discoverer, WSCXMLKnowledgeBase kb, String baseUri) {
        this.discoverer = discoverer;
        this.kb = kb;
        this.baseUri = baseUri;
    }

    @Override
    public Set<Operation<URI>> findOperationsConsuming(URI input) {
        // Get concept name
        String name = input.getFragment();
        Set<Operation<Concept>> result = discoverer.findOperationsConsuming(kb.getConcept(name));
        // Translate to URI
        return Sets.newHashSet(Collections2.transform(result, new Function<Operation<Concept>, Operation<URI>>() {
            @Override
            public Operation<URI> apply(Operation<Concept> op) {
                Set<URI> inputs = new HashSet<URI>();
                for(Concept input : op.getSignature().getInputs()){
                    inputs.add(URI.create(baseUri + "#" + input.getID()));
                }
                Set<URI> outputs = new HashSet<URI>();
                for(Concept output : op.getSignature().getOutputs()){
                    outputs.add(URI.create(baseUri + "#" + output.getID()));
                }
                return new ResourceOperation<URI>(op.getID(),  new SignatureIO<URI>(inputs, outputs));
            }
        }));
    }

    @Override
    public Set<Operation<URI>> findOperationsConsumingSome(Collection<URI> inputs) {
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI input : inputs){
            ops.addAll(findOperationsConsuming(input));
        }
        return ops;
    }
}
