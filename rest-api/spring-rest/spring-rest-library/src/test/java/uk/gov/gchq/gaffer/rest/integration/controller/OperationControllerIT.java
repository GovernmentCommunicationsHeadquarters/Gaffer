/*
 * Copyright 2020 Crown Copyright
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

package uk.gov.gchq.gaffer.rest.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import uk.gov.gchq.gaffer.cache.impl.HashMapCacheService;
import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.commonutil.iterable.StreamIterator;
import uk.gov.gchq.gaffer.commonutil.iterable.WrappedCloseableIterable;
import uk.gov.gchq.gaffer.core.exception.Error;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.mapstore.MapStoreProperties;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.data.CustomVertex;
import uk.gov.gchq.gaffer.operation.impl.Map;
import uk.gov.gchq.gaffer.operation.impl.OperationImpl;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.operation.impl.job.GetAllJobDetails;
import uk.gov.gchq.gaffer.operation.impl.output.ToList;
import uk.gov.gchq.gaffer.rest.factory.GraphFactory;
import uk.gov.gchq.gaffer.rest.factory.MockGraphFactory;
import uk.gov.gchq.gaffer.rest.integration.AbstractRestApiIT;
import uk.gov.gchq.gaffer.store.StoreProperties;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.koryphe.impl.function.CallMethod;
import uk.gov.gchq.koryphe.impl.function.CreateObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.gchq.gaffer.cache.util.CacheProperties.CACHE_SERVICE_CLASS;
import static uk.gov.gchq.gaffer.core.exception.Status.SERVICE_UNAVAILABLE;

public class OperationControllerIT extends AbstractRestApiIT {

    @Autowired
    private GraphFactory graphFactory; // This will be a Mock (see application-test.properties)

    private MockGraphFactory getGraphFactory() {
        return (MockGraphFactory) graphFactory;
    }


    @Test
    public void shouldReturnHelpfulErrorMessageIfJsonIsIncorrect() {

    }

    @Test
    public void shouldReturnHelpfulErrorMessageIfOperationIsUnsupported() {
        // Given
        Graph graph = new Graph.Builder()
                .config(StreamUtil.graphConfig(this.getClass()))
                .storeProperties(new MapStoreProperties())
                .addSchema(new Schema())
                .build();

        when(getGraphFactory().getGraph()).thenReturn(graph);

        final OperationImpl operation = new OperationImpl();
        operation.setRequiredField1("requiredField1");
        operation.setRequiredField2(new CustomVertex());

        // When
        final ResponseEntity<Error> response = post("/graph/operations/execute",
                operation,
                Error.class);

        // Then

        assertNotNull(response.getBody().getSimpleMessage());
        assertTrue(response.getBody().getSimpleMessage().contains("OperationImpl is not supported by the MapStore"));
    }

    @Test
    public void shouldNotAllowUserToReadFilesOffTheFileSystemUsingKorypheFunctions() throws IOException {
        Graph graph = new Graph.Builder()
                .config(StreamUtil.graphConfig(this.getClass()))
                .storeProperties(new MapStoreProperties())
                .addSchema(new Schema())
                .build();

        String cacheProperties = getClass().getResource("/cache-store.properties").getPath();

        when(getGraphFactory().getGraph()).thenReturn(graph);

        ResponseEntity<Object> lines = post("/graph/operations/execute", new OperationChain.Builder()
                .first(new Map.Builder<>()
                        .input(cacheProperties)
                        .first(new CreateObject(FileInputStream.class))
                        .then(new CreateObject(InputStreamReader.class))
                        .then(new CreateObject(BufferedReader.class))
                        .then(new CallMethod("lines"))
                        .then(new CreateObject(StreamIterator.class))
                        .then(new CreateObject(WrappedCloseableIterable.class))
                        .build())
                .then(new ToList())
                .build(), Object.class);

        assertNotEquals(200, lines.getStatusCodeValue());
    }

    @Test
    public void shouldReturn403WhenUnauthorised() throws IOException {
        // Given
        Graph graph = new Graph.Builder()
                .config(StreamUtil.graphConfig(this.getClass()))
                .storeProperties(new MapStoreProperties())
                .addSchema(new Schema())
                .build();

        when(getGraphFactory().getGraph()).thenReturn(graph);

        // When
        final ResponseEntity<Error> response = post("/graph/operations/execute",
                new GetAllElements(),
                Error.class);

        // Then
        assertEquals(403, response.getStatusCode().value());
        assertEquals(403, response.getBody().getStatusCode());
    }

    @Test
    public void shouldPropagateStatusInformationContainedInOperationExceptionsThrownByOperationHandlers() throws IOException {
        // Given
        final StoreProperties storeProperties = new MapStoreProperties();
        storeProperties.set(StoreProperties.JOB_TRACKER_ENABLED, Boolean.FALSE.toString());

        final Graph graph = new Graph.Builder()
                .config(StreamUtil.graphConfig(this.getClass()))
                .storeProperties(storeProperties)
                .addSchema(new Schema())
                .build();

        when(getGraphFactory().getGraph()).thenReturn(graph);

        // When
        final ResponseEntity<Error> response = post("/graph/operations/execute",
                new GetAllJobDetails(),
                Error.class);
        // Then
        assertEquals(SERVICE_UNAVAILABLE.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void shouldReturnSameJobIdInHeaderAsGetAllJobDetailsOperation() throws IOException {
        // Given
        StoreProperties properties = new MapStoreProperties();
        properties.setJobTrackerEnabled(true);
        properties.set(CACHE_SERVICE_CLASS, HashMapCacheService.class.getName());

        Graph graph = new Graph.Builder()
                .config(StreamUtil.graphConfig(this.getClass()))
                .storeProperties(properties)
                .addSchema(new Schema())
                .build();

        when(getGraphFactory().getGraph()).thenReturn(graph);

        // When
        final ResponseEntity<Set> response = post("/graph/operations/execute",
                new GetAllJobDetails(),
                Set.class);

        // Then
        try {
            assertTrue(response.getBody().toString().contains(response.getHeaders().get("job-id").get(0)));
        } catch (final AssertionError e) {
            System.out.println("Job ID was not found in the Header");
            System.out.println("Header was: " + response.getHeaders().get("job-id"));
            System.out.println("Body was: " + response.getBody());
            throw e;
        }
    }

}
