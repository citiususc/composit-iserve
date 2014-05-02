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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import es.usc.citius.composit.core.composition.InputDiscoverer;
import es.usc.citius.composit.core.composition.optimization.BackwardMinimizationOptimizer;
import es.usc.citius.composit.core.composition.optimization.FunctionalDominanceOptimizer;
import es.usc.citius.composit.core.composition.optimization.NetworkOptimizer;
import es.usc.citius.composit.core.matcher.SetMatchFunction;
import es.usc.citius.composit.core.matcher.graph.MatchGraph;
import es.usc.citius.composit.iserve.discovery.iServeOperationDiscovererAdapter;
import es.usc.citius.composit.iserve.match.iServeMatchGraph;
import es.usc.citius.composit.iserve.match.iServeSetMatchFunction;
import uk.ac.open.kmi.iserve.api.iServeEngineModule;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import java.net.URI;

/**
 * CompositIserveModule
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/04/2014
 */
public class CompositIserveModule extends AbstractModule {

    @Override
    protected void configure() {
        // Configure iServeEngine
        install(new iServeEngineModule());

        // bind the Operation Translator
//            bind(OperationTranslator.class).to(iServeIndexedOperationTranslator.class);
        bind(OperationTranslator.class).to(iServeLazyOperationTranslator.class);

        // bind the Input Discoverer
        bind(new TypeLiteral<InputDiscoverer<URI>>(){}).to(iServeOperationDiscovererAdapter.class);

        // bind setMatchFunction
        bind(new TypeLiteral<SetMatchFunction<URI, LogicConceptMatchType>>(){}).to(iServeSetMatchFunction.class);

        // bind MatchGraph
        bind(new TypeLiteral<MatchGraph<URI, LogicConceptMatchType>>(){}).to(iServeMatchGraph.class);

        // Bind optimisers
        MapBinder<String, NetworkOptimizer<URI, LogicConceptMatchType>> mapbinder =
                MapBinder.newMapBinder(
                        binder(),
                        new TypeLiteral<String>(){},
                        new TypeLiteral<NetworkOptimizer<URI, LogicConceptMatchType>>() {});

        // For now hard-code the optimisations bindings. Should have a plugin method later.
        mapbinder.addBinding(BackwardMinimizationOptimizer.class.getName()).
                to(new TypeLiteral<BackwardMinimizationOptimizer<URI, LogicConceptMatchType>>() {
                });

        mapbinder.addBinding(FunctionalDominanceOptimizer.class.getName()).
                to(new TypeLiteral<FunctionalDominanceOptimizer<URI, LogicConceptMatchType>>() {});

        // bind the implementation of the engine
        install(new FactoryModuleBuilder()
                .implement(CompositIserveEngine.class, CompositIserveEngineImpl.class)
                .build(CompositIserveEngineFactory.class));

    }
}
