/*
 * Copyright 2021 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore.integration;

import org.junit.Before;

import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.federatedstore.FederatedStoreProperties;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.integration.AbstractStoreIT;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.user.User;

public abstract class AbstractStandaloneFederatedStoreIT {

    protected Graph graph;
    protected User user = new User();

    @Before
    public void setup() throws Exception {
        createGraph();
        _setup();
    }

    protected void _setup() throws Exception {
        // Override if required;
    }

    private void createGraph() {
        graph = new Graph.Builder()
                .config(AbstractStoreIT.createDefaultGraphConfig())
                .storeProperties(getStoreProperties())
                .addSchema(createSchema())
                .build();
    }

    private FederatedStoreProperties getStoreProperties() {
        return FederatedStoreProperties.loadStoreProperties(
                StreamUtil.openStream(FederatedStoreITs.class, "publicAccessPredefinedFederatedStore.properties"));

    }

    protected Schema createSchema() {
        return AbstractStoreIT.createDefaultSchema();
    }
}