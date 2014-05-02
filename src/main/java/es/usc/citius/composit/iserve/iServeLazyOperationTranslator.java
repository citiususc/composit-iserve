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

package es.usc.citius.composit.iserve;


import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.core.model.Signature;
import es.usc.citius.composit.core.model.impl.ResourceOperation;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class iServeLazyOperationTranslator implements OperationTranslator {

    private ServiceManager serviceMgr;

    @Inject
    public iServeLazyOperationTranslator(ServiceManager serviceMgr) {
        this.serviceMgr = serviceMgr;
    }

    @Override
    public Set<Operation<URI>> getOperations() {
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI serviceUri : serviceMgr.listServices()){
            ops.addAll(getOperationsOfService(serviceUri));
        }
        return ops;
    }

    @Override
    public Operation<URI> getOperation(final URI operation) {
        return new ResourceOperation<URI>(operation.toString(), new Signature<URI>() {
            @Override
            public Set<URI> getInputs() {
                Set<URI> models = new HashSet<URI>();
                for(URI opUri : serviceMgr.listInputs(operation)){
                    for(URI mandatoryPart : serviceMgr.listMandatoryParts(opUri)){
                        models.addAll(serviceMgr.listModelReferences(mandatoryPart));
                    }
                }
                return models;
            }

            @Override
            public Set<URI> getOutputs() {
                Set<URI> models = new HashSet<URI>();
                for(URI opUri : serviceMgr.listOutputs(operation)){
                    for(URI mandatoryPart : serviceMgr.listMandatoryParts(opUri)){
                        models.addAll(serviceMgr.listModelReferences(mandatoryPart));
                    }
                }
                return models;
            }
        });
    }

    @Override
    public Set<Operation<URI>> getOperationsOfService(URI service) {
        Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
        for(URI op : serviceMgr.listOperations(service)){
            ops.add(getOperation(op));
        }
        return ops;
    }
}
