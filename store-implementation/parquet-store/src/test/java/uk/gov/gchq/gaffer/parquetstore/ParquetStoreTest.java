/*
 * Copyright 2017. Crown Copyright
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

package uk.gov.gchq.gaffer.parquetstore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.gov.gchq.gaffer.commonutil.CommonTestConstants;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.commonutil.TestPropertyNames;
import uk.gov.gchq.gaffer.commonutil.TestTypes;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.graph.GraphConfig;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.store.StoreException;
import uk.gov.gchq.gaffer.store.StoreTrait;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaEdgeDefinition;
import uk.gov.gchq.gaffer.store.schema.SchemaEntityDefinition;
import uk.gov.gchq.gaffer.store.schema.TypeDefinition;
import uk.gov.gchq.gaffer.user.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static uk.gov.gchq.gaffer.parquetstore.testutils.TestUtils.getParquetStoreProperties;

public class ParquetStoreTest {

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder(CommonTestConstants.TMP_DIRECTORY);
    private final Schema schema = new Schema.Builder()
            .type(TestTypes.ID_STRING, new TypeDefinition.Builder()
                    .clazz(String.class)
                    .build())
            .type(TestTypes.PROP_INTEGER, Integer.class)
            .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                    .source(TestTypes.ID_STRING)
                    .destination(TestTypes.ID_STRING)
                    .property(TestPropertyNames.PROP_1, TestTypes.PROP_INTEGER)
                    .aggregate(false)
                    .build())
            .entity(TestGroups.ENTITY, new SchemaEntityDefinition.Builder()
                    .property(TestPropertyNames.PROP_1, TestTypes.PROP_INTEGER)
                    .vertex(TestTypes.ID_STRING)
                    .aggregate(false)
                    .build())
            .build();
    private final Edge testEdge = new Edge.Builder()
            .group(TestGroups.EDGE_2)
            .source("X")
            .dest("Y")
            .directed(false)
            .property("long", 2L)
            .build();
    private final Entity testEntity = new Entity.Builder()
            .group(TestGroups.ENTITY_2)
            .property("long", 3L)
            .build();

    @Test
    public void testTraits() throws StoreException {
        final ParquetStore store = new ParquetStore();
        final Set<StoreTrait> expectedTraits = new HashSet<>();
        expectedTraits.add(StoreTrait.INGEST_AGGREGATION);
        expectedTraits.add(StoreTrait.PRE_AGGREGATION_FILTERING);
        expectedTraits.add(StoreTrait.ORDERED);
        expectedTraits.add(StoreTrait.STORE_VALIDATION);
        expectedTraits.add(StoreTrait.VISIBILITY);
        assertEquals(expectedTraits, store.getTraits());
    }

    @Test
    public void shouldNotThrowExceptionWhenAddingASingleEdgeWithGroupNotInSchema() throws Exception {
        getGraph().execute(new AddElements.Builder()
                        .input(testEdge)
                        .build(),
                new User());
    }

    @Test
    public void shouldNotThrowExceptionWhenAddingASingleEntityWithGroupNotInSchema() throws Exception {
        getGraph().execute(new AddElements.Builder()
                        .input(testEntity)
                        .build(),
                new User());
    }

    @Test
    public void shouldNotThrowExceptionWhenAddingAMultipleElementsWithGroupsNotInSchema() throws Exception {
        getGraph().execute(new AddElements.Builder()
                        .input(testEntity, testEdge)
                        .build(),
                new User());
    }

    private Graph getGraph() throws IOException {
        return new Graph.Builder()
                .addSchema(schema)
                .storeProperties(getParquetStoreProperties(testFolder))
                .config(new GraphConfig.Builder().graphId("testGraphId").build())
                .build();
    }
}
