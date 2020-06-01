/*
 * Copyright 2016-2020 Crown Copyright
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

package uk.gov.gchq.gaffer.accumulostore;

import com.google.common.collect.Iterables;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.accumulostore.operation.handler.GetElementsBetweenSetsHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.handler.GetElementsInRangesHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.handler.GetElementsWithinSetHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.hdfs.handler.AddElementsFromHdfsHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.hdfs.handler.ImportAccumuloKeyValueFilesHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.hdfs.handler.SampleDataForSplitPointsHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.hdfs.handler.SplitStoreHandler;
import uk.gov.gchq.gaffer.accumulostore.operation.hdfs.operation.ImportAccumuloKeyValueFiles;
import uk.gov.gchq.gaffer.accumulostore.operation.impl.GetElementsBetweenSets;
import uk.gov.gchq.gaffer.accumulostore.operation.impl.GetElementsInRanges;
import uk.gov.gchq.gaffer.accumulostore.operation.impl.GetElementsWithinSet;
import uk.gov.gchq.gaffer.accumulostore.operation.impl.SummariseGroupOverRanges;
import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.commonutil.TestPropertyNames;
import uk.gov.gchq.gaffer.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.data.element.function.ElementFilter;
import uk.gov.gchq.gaffer.data.element.id.EntityId;
import uk.gov.gchq.gaffer.data.elementdefinition.exception.SchemaException;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewElementDefinition;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.hdfs.operation.AddElementsFromHdfs;
import uk.gov.gchq.gaffer.hdfs.operation.SampleDataForSplitPoints;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.data.EntitySeed;
import uk.gov.gchq.gaffer.operation.impl.SplitStore;
import uk.gov.gchq.gaffer.operation.impl.Validate;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.operation.impl.generate.GenerateElements;
import uk.gov.gchq.gaffer.operation.impl.generate.GenerateObjects;
import uk.gov.gchq.gaffer.operation.impl.get.GetElements;
import uk.gov.gchq.gaffer.serialisation.Serialiser;
import uk.gov.gchq.gaffer.serialisation.implementation.JavaSerialiser;
import uk.gov.gchq.gaffer.serialisation.implementation.StringSerialiser;
import uk.gov.gchq.gaffer.serialisation.implementation.raw.CompactRawLongSerialiser;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.StoreException;
import uk.gov.gchq.gaffer.store.StoreTrait;
import uk.gov.gchq.gaffer.store.TestTypes;
import uk.gov.gchq.gaffer.store.operation.handler.OperationHandler;
import uk.gov.gchq.gaffer.store.operation.handler.generate.GenerateElementsHandler;
import uk.gov.gchq.gaffer.store.operation.handler.generate.GenerateObjectsHandler;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaEdgeDefinition;
import uk.gov.gchq.gaffer.store.schema.SchemaEntityDefinition;
import uk.gov.gchq.gaffer.store.schema.TypeDefinition;
import uk.gov.gchq.gaffer.user.User;
import uk.gov.gchq.koryphe.impl.binaryoperator.Max;
import uk.gov.gchq.koryphe.impl.binaryoperator.Min;
import uk.gov.gchq.koryphe.impl.binaryoperator.StringConcat;
import uk.gov.gchq.koryphe.impl.binaryoperator.Sum;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.gchq.gaffer.store.StoreTrait.INGEST_AGGREGATION;
import static uk.gov.gchq.gaffer.store.StoreTrait.ORDERED;
import static uk.gov.gchq.gaffer.store.StoreTrait.POST_AGGREGATION_FILTERING;
import static uk.gov.gchq.gaffer.store.StoreTrait.POST_TRANSFORMATION_FILTERING;
import static uk.gov.gchq.gaffer.store.StoreTrait.PRE_AGGREGATION_FILTERING;
import static uk.gov.gchq.gaffer.store.StoreTrait.QUERY_AGGREGATION;
import static uk.gov.gchq.gaffer.store.StoreTrait.STORE_VALIDATION;
import static uk.gov.gchq.gaffer.store.StoreTrait.TRANSFORMATION;
import static uk.gov.gchq.gaffer.store.StoreTrait.VISIBILITY;

public class AccumuloStoreTest {
    private static final String BYTE_ENTITY_GRAPH = "byteEntityGraph";
    private static final String GAFFER_1_GRAPH = "gaffer1Graph";
    private static SingleUseMiniAccumuloStore byteEntityStore;
    private static SingleUseMiniAccumuloStore gaffer1KeyStore;
    private static AccumuloProperties byteEntityStoreProperties;
    private static AccumuloProperties gaffer1KeyStoreProperties;
    private static final Schema SCHEMA = Schema.fromJson(StreamUtil.schemas(AccumuloStoreTest.class));
    private static final AccumuloProperties PROPERTIES = AccumuloProperties.loadStoreProperties(StreamUtil.storeProps(AccumuloStoreTest.class));
    private static final AccumuloProperties CLASSIC_PROPERTIES = AccumuloProperties.loadStoreProperties(StreamUtil.openStream(AccumuloStoreTest.class, "/accumuloStoreClassicKeys.properties"));

    @BeforeAll
    public static void setup() throws StoreException {
        byteEntityStore = new SingleUseMiniAccumuloStore();
        byteEntityStoreProperties = (AccumuloProperties) byteEntityStore.setUpTestDB(PROPERTIES);
        gaffer1KeyStore = new SingleUseMiniAccumuloStore();
        gaffer1KeyStoreProperties = (AccumuloProperties) gaffer1KeyStore.setUpTestDB(CLASSIC_PROPERTIES);
    }

    @BeforeEach
    public void beforeMethod() throws StoreException {
        byteEntityStore.initialise(BYTE_ENTITY_GRAPH, SCHEMA, byteEntityStoreProperties);
        gaffer1KeyStore.initialise(GAFFER_1_GRAPH, SCHEMA, gaffer1KeyStoreProperties);
    }

    @AfterAll
    public static void tearDown() {
        byteEntityStore.tearDownTestDB();
        gaffer1KeyStore.tearDownTestDB();
    }

    @Test
    public void shouldNotCreateTableWhenInitialisedWithGeneralInitialiseMethod() throws StoreException, AccumuloSecurityException, AccumuloException, TableNotFoundException {
        Connector connector = byteEntityStore.getConnection();

        connector.tableOperations().delete(byteEntityStore.getTableName());
        assertFalse(connector.tableOperations().exists(byteEntityStore.getTableName()));

        byteEntityStore.preInitialise(BYTE_ENTITY_GRAPH, SCHEMA, byteEntityStoreProperties);
        connector = byteEntityStore.getConnection();
        assertFalse(connector.tableOperations().exists(byteEntityStore.getTableName()));

        byteEntityStore.initialise(GAFFER_1_GRAPH, SCHEMA, byteEntityStoreProperties);
        connector = byteEntityStore.getConnection();
        assertTrue(connector.tableOperations().exists(byteEntityStore.getTableName()));
    }

    @Test
    public void shouldCreateAStoreUsingTableName() throws Exception {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();
        properties.setTable("tableName");

        // When
        byteEntityStore.initialise(null, SCHEMA, properties);

        // Then
        assertEquals("tableName", byteEntityStore.getTableName());
        assertEquals("tableName", byteEntityStore.getGraphId());
    }

    @Test
    public void shouldCreateAStoreUsingTableNameWithNamespace() throws Exception {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();
        properties.setNamespace("namespaceName");

        // When
        byteEntityStore.initialise("graphId", SCHEMA, properties);

        // Then
        assertEquals("namespaceName.graphId", byteEntityStore.getTableName());
        assertEquals("graphId", byteEntityStore.getGraphId());
    }

    @Test
    public void shouldBuildGraphAndGetGraphIdFromTableName() {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();
        properties.setTable("tableName");

        // When
        final Graph graph = new Graph.Builder()
                .addSchemas(StreamUtil.schemas(getClass()))
                .storeProperties(properties)
                .build();

        // Then
        assertEquals("tableName", graph.getGraphId());
    }

    @Test
    public void shouldCreateAStoreUsingGraphIdIfItIsEqualToTableName() throws Exception {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();
        properties.setTable("tableName");

        // When
        byteEntityStore.initialise("tableName", SCHEMA, properties);

        // Then
        assertEquals("tableName", byteEntityStore.getTableName());
    }

    @Test()
    public void shouldThrowExceptionIfGraphIdAndTableNameAreProvidedAndDifferent() throws StoreException {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();
        properties.setTable("tableName");

        // When
        try {
            byteEntityStore.initialise("graphId", SCHEMA, properties);
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            final String expected = "The table in store.properties should no longer be used. Please use a graphId instead " +
                    "or for now just set the graphId to be the same value as the store.properties table.";
            assertTrue(e.getMessage().equals(expected));
        }
    }

    @Test
    public void shouldCreateAStoreUsingGraphId() throws Exception {
        // Given
        final AccumuloProperties properties = byteEntityStoreProperties.clone();

        // When
        byteEntityStore.initialise("graphId", SCHEMA, properties);

        // Then
        assertEquals("graphId", byteEntityStore.getTableName());
    }


    @Test
    public void shouldBeAnOrderedStore() {
        assertTrue(byteEntityStore.hasTrait(StoreTrait.ORDERED));
        assertTrue(gaffer1KeyStore.hasTrait(StoreTrait.ORDERED));
    }

    @Test
    public void shouldAllowRangeScanOperationsWhenVertexSerialiserDoesPreserveObjectOrdering() throws StoreException {
        // Given
        final Serialiser serialiser = new StringSerialiser();
        byteEntityStore.initialise(
                BYTE_ENTITY_GRAPH,
                new Schema.Builder()
                        .vertexSerialiser(serialiser)
                        .build(),
                byteEntityStoreProperties);

        // When
        final boolean isGetElementsInRangesSupported = byteEntityStore.isSupported(GetElementsInRanges.class);
        final boolean isSummariseGroupOverRangesSupported = byteEntityStore.isSupported(SummariseGroupOverRanges.class);

        // Then
        assertTrue(isGetElementsInRangesSupported);
        assertTrue(isSummariseGroupOverRangesSupported);
    }

    @Test
    public void shouldNotAllowRangeScanOperationsWhenVertexSerialiserDoesNotPreserveObjectOrdering() throws StoreException {
        // Given
        final Serialiser serialiser = new CompactRawLongSerialiser();
        byteEntityStore.initialise(
                BYTE_ENTITY_GRAPH,
                new Schema.Builder()
                        .vertexSerialiser(serialiser)
                        .build(),
                byteEntityStoreProperties);

        // When
        final boolean isGetElementsInRangesSupported = byteEntityStore.isSupported(GetElementsInRanges.class);
        final boolean isSummariseGroupOverRangesSupported = byteEntityStore.isSupported(SummariseGroupOverRanges.class);

        // Then
        assertFalse(isGetElementsInRangesSupported);
        assertFalse(isSummariseGroupOverRangesSupported);
    }

    @Test
    public void testAbleToInsertAndRetrieveEntityQueryingEqualAndRelatedGaffer1() throws OperationException {
        testAbleToInsertAndRetrieveEntityQueryingEqualAndRelated(gaffer1KeyStore);
    }

    @Test
    public void testAbleToInsertAndRetrieveEntityQueryingEqualAndRelatedByteEntity() throws OperationException {
        testAbleToInsertAndRetrieveEntityQueryingEqualAndRelated(byteEntityStore);
    }

    private void testAbleToInsertAndRetrieveEntityQueryingEqualAndRelated(final AccumuloStore store) throws OperationException {
        final Entity e = new Entity(TestGroups.ENTITY, "1");
        e.putProperty(TestPropertyNames.PROP_1, 1);
        e.putProperty(TestPropertyNames.PROP_2, 2);
        e.putProperty(TestPropertyNames.PROP_3, 3);
        e.putProperty(TestPropertyNames.PROP_4, 4);
        e.putProperty(TestPropertyNames.COUNT, 1);

        final User user = new User();
        final AddElements add = new AddElements.Builder()
                .input(e)
                .build();
        store.execute(add, new Context(user));

        final EntityId entityId1 = new EntitySeed("1");
        final GetElements getBySeed = new GetElements.Builder()
                .view(new View.Builder()
                        .entity(TestGroups.ENTITY)
                        .build())
                .input(entityId1)
                .build();
        final CloseableIterable<? extends Element> results = store.execute(getBySeed, new Context(user));

        assertEquals(1, Iterables.size(results));
        assertTrue(Iterables.contains(results, e));

        final GetElements getRelated = new GetElements.Builder()
                .view(new View.Builder()
                        .entity(TestGroups.ENTITY)
                        .build())
                .input(entityId1)
                .build();
        CloseableIterable<? extends Element> relatedResults = store.execute(getRelated, store.createContext(user));
        assertEquals(1, Iterables.size(relatedResults));
        assertTrue(Iterables.contains(relatedResults, e));

        final GetElements getRelatedWithPostAggregationFilter = new GetElements.Builder()
                .view(new View.Builder()
                        .entity(TestGroups.ENTITY, new ViewElementDefinition.Builder()
                                .preAggregationFilter(new ElementFilter.Builder()
                                        .select(TestPropertyNames.PROP_1)
                                        .execute(new IsMoreThan(0))
                                        .build())
                                .postAggregationFilter(new ElementFilter.Builder()
                                        .select(TestPropertyNames.COUNT)
                                        .execute(new IsMoreThan(6))
                                        .build())
                                .build())
                        .build())
                .input(entityId1)
                .build();
        relatedResults = store.execute(getRelatedWithPostAggregationFilter, store.createContext(user));

        assertEquals(0, Iterables.size(relatedResults));
    }

    @Test
    public void testStoreReturnsHandlersForRegisteredOperationsGaffer1() {
        testStoreReturnsHandlersForRegisteredOperations(gaffer1KeyStore);
    }

    @Test
    public void testStoreReturnsHandlersForRegisteredOperationsByteEntity() {
        testStoreReturnsHandlersForRegisteredOperations(byteEntityStore);
    }

    public void testStoreReturnsHandlersForRegisteredOperations(final SingleUseMiniAccumuloStore store) {
        // Then
        assertNotNull(store.getOperationHandlerExposed(Validate.class));
        assertTrue(store.getOperationHandlerExposed(AddElementsFromHdfs.class) instanceof AddElementsFromHdfsHandler);
        assertTrue(store.getOperationHandlerExposed(GetElementsBetweenSets.class) instanceof GetElementsBetweenSetsHandler);
        assertTrue(store.getOperationHandlerExposed(GetElementsInRanges.class) instanceof GetElementsInRangesHandler);
        assertTrue(store.getOperationHandlerExposed(GetElementsWithinSet.class) instanceof GetElementsWithinSetHandler);
        assertTrue(store.getOperationHandlerExposed(SplitStore.class) instanceof SplitStoreHandler);
        assertTrue(store.getOperationHandlerExposed(SampleDataForSplitPoints.class) instanceof SampleDataForSplitPointsHandler);
        assertTrue(store.getOperationHandlerExposed(ImportAccumuloKeyValueFiles.class) instanceof ImportAccumuloKeyValueFilesHandler);
        assertTrue(store.getOperationHandlerExposed(GenerateElements.class) instanceof GenerateElementsHandler);
        assertTrue(store.getOperationHandlerExposed(GenerateObjects.class) instanceof GenerateObjectsHandler);
    }

    @Test
    public void testRequestForNullHandlerManagedGaffer1() {
        testRequestForNullHandlerManaged(gaffer1KeyStore);
    }

    @Test
    public void testRequestForNullHandlerManagedByteEntity() {
        testRequestForNullHandlerManaged(byteEntityStore);
    }

    public void testRequestForNullHandlerManaged(final SingleUseMiniAccumuloStore store) {
        final OperationHandler returnedHandler = store.getOperationHandlerExposed(null);
        assertNull(returnedHandler);
    }

    @Test
    public void testStoreTraitsGaffer1() {
        testStoreTraits(gaffer1KeyStore);
    }

    @Test
    public void testStoreTraitsByteEntity() {
        testStoreTraits(byteEntityStore);
    }

    public void testStoreTraits(final AccumuloStore store) {
        final Collection<StoreTrait> traits = store.getTraits();
        assertNotNull(traits);
        assertEquals(10, traits.size(), "Collection size should be 10");
        assertTrue(traits.contains(INGEST_AGGREGATION), "Collection should contain INGEST_AGGREGATION trait");
        assertTrue(traits.contains(QUERY_AGGREGATION), "Collection should contain QUERY_AGGREGATION trait");
        assertTrue(traits.contains(PRE_AGGREGATION_FILTERING), "Collection should contain PRE_AGGREGATION_FILTERING trait");
        assertTrue(traits.contains(POST_AGGREGATION_FILTERING), "Collection should contain POST_AGGREGATION_FILTERING trait");
        assertTrue(traits.contains(TRANSFORMATION), "Collection should contain TRANSFORMATION trait");
        assertTrue(traits.contains(POST_TRANSFORMATION_FILTERING), "Collection should contain POST_TRANSFORMATION_FILTERING trait");
        assertTrue(traits.contains(STORE_VALIDATION), "Collection should contain STORE_VALIDATION trait");
        assertTrue(traits.contains(ORDERED), "Collection should contain ORDERED trait");
        assertTrue(traits.contains(VISIBILITY), "Collection should contain VISIBILITY trait");
    }

    @Test
    public void shouldFindInconsistentVertexSerialiser() throws StoreException {
        final Schema inconsistentSchema = new Schema.Builder()
                .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                        .source("string")
                        .destination("string")
                        .directed("false")
                        .property(TestPropertyNames.INT, "int")
                        .groupBy(TestPropertyNames.INT)
                        .build())
                .type("string", new TypeDefinition.Builder()
                        .clazz(String.class)
                        .serialiser(new JavaSerialiser())
                        .aggregateFunction(new StringConcat())
                        .build())
                .type("int", new TypeDefinition.Builder()
                        .clazz(Integer.class)
                        .serialiser(new JavaSerialiser())
                        .aggregateFunction(new Sum())
                        .build())
                .type("false", Boolean.class)
                .vertexSerialiser(new JavaSerialiser())
                .build();

        Exception e1 = assertThrows(SchemaException.class, () -> byteEntityStore.preInitialise("graphId", inconsistentSchema, byteEntityStoreProperties));
        assertTrue(e1.getMessage().equals("Vertex serialiser is inconsistent. This store requires vertices to be serialised in a consistent way."));

        Exception e2 = assertThrows(SchemaException.class, () -> byteEntityStore.validateSchemas());
        assertTrue(e2.getMessage().equals("Vertex serialiser is inconsistent. This store requires vertices to be serialised in a consistent way."));
    }

    @Test
    public void shouldValidateTimestampPropertyHasMaxAggregator() throws Exception {
        // Given
        final Schema schema = new Schema.Builder()
                .entity(TestGroups.ENTITY, new SchemaEntityDefinition.Builder()
                        .vertex(TestTypes.ID_STRING)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP)
                        .build())
                .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                        .source(TestTypes.ID_STRING)
                        .destination(TestTypes.ID_STRING)
                        .directed(TestTypes.DIRECTED_EITHER)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP_2)
                        .build())
                .type(TestTypes.ID_STRING, String.class)
                .type(TestTypes.DIRECTED_EITHER, new TypeDefinition.Builder()
                        .clazz(Boolean.class)
                        .build())
                .type(TestTypes.TIMESTAMP, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .aggregateFunction(new Max())
                        .build())
                .type(TestTypes.TIMESTAMP_2, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .aggregateFunction(new Max())
                        .build())
                .timestampProperty(TestPropertyNames.TIMESTAMP)
                .build();

        // When
        byteEntityStore.initialise("graphId", schema, byteEntityStoreProperties);

        // Then - no validation exceptions
    }

    @Test
    public void shouldPassSchemaValidationWhenTimestampPropertyDoesNotHaveAnAggregator() throws Exception {
        // Given
        final Schema schema = new Schema.Builder()
                .entity(TestGroups.ENTITY, new SchemaEntityDefinition.Builder()
                        .vertex(TestTypes.ID_STRING)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP)
                        .aggregate(false)
                        .build())
                .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                        .source(TestTypes.ID_STRING)
                        .destination(TestTypes.ID_STRING)
                        .directed(TestTypes.DIRECTED_EITHER)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP_2)
                        .aggregate(false)
                        .build())
                .type(TestTypes.ID_STRING, String.class)
                .type(TestTypes.DIRECTED_EITHER, new TypeDefinition.Builder()
                        .clazz(Boolean.class)
                        .build())
                .type(TestTypes.TIMESTAMP, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .build())
                .type(TestTypes.TIMESTAMP_2, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .build())
                .timestampProperty(TestPropertyNames.TIMESTAMP)
                .build();

        // When
        byteEntityStore.initialise("graphId", schema, byteEntityStoreProperties);

        // Then - no validation exceptions
    }

    @Test()
    public void shouldFailSchemaValidationWhenTimestampPropertyDoesNotHaveMaxAggregator() throws StoreException {
        // Given
        final Schema schema = new Schema.Builder()
                .entity(TestGroups.ENTITY, new SchemaEntityDefinition.Builder()
                        .vertex(TestTypes.ID_STRING)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP)
                        .build())
                .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                        .source(TestTypes.ID_STRING)
                        .destination(TestTypes.ID_STRING)
                        .property(TestPropertyNames.TIMESTAMP, TestTypes.TIMESTAMP_2)
                        .build())
                .type(TestTypes.ID_STRING, String.class)
                .type(TestTypes.TIMESTAMP, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .aggregateFunction(new Max())
                        .build())
                .type(TestTypes.TIMESTAMP_2, new TypeDefinition.Builder()
                        .clazz(Long.class)
                        .aggregateFunction(new Min())
                        .build())
                .timestampProperty(TestPropertyNames.TIMESTAMP)
                .build();

        // When / Then
        Exception e1 = assertThrows(SchemaException.class, () -> byteEntityStore.initialise("graphId", schema, byteEntityStoreProperties));
        final String expected = "Schema is not valid. Validation errors: \n" +
                "The aggregator for the timestamp property must be set to: uk.gov.gchq.koryphe.impl.binaryoperator.Max " +
                "this cannot be overridden for this Accumulo Store, as you have told Accumulo to store this property " +
                "in the timestamp column.";
        assertTrue(e1.getMessage().equals(expected));
    }

}
