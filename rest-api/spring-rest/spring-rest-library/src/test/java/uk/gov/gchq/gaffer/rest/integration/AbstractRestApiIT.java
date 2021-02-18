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

package uk.gov.gchq.gaffer.rest.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.gchq.gaffer.rest.GafferWebApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.gchq.gaffer.rest.ServiceConstants.GAFFER_MEDIA_TYPE_HEADER;

/**
 * Base class for Integration Tests.
 *
 * The AbstractRestApiIT starts the application with spring boot and provides {@code get()} and {@code post()} methods
 * for easy access, as well as a {@code checkResponse()} method which asserts that the correct status code is returned
 * and that the Gaffer Media type header was added.
 */
@SpringBootTest(classes = GafferWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class AbstractRestApiIT {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @AfterAll
    public static void clearSystemProperties() {
        Properties properties = System.getProperties();
        List<String> propertiesToBeRemoved = new ArrayList<>();
        properties.forEach((key, value) -> {
            if (key instanceof String && ((String) key).startsWith("gaffer")) {
                propertiesToBeRemoved.add((String) key);
            }
        });

        propertiesToBeRemoved.forEach(System::clearProperty);
    }

    protected int getPort() {
        return port;
    }

    protected String getContextPath() {
        return contextPath;
    }

    protected String getBaseURl() {
        return "http://localhost:" + port + contextPath;
    }

    protected <T> ResponseEntity<T> get(final String path, final Class<T> responseBodyClass) {
        try {
            return restTemplate.getForEntity(new URI(getBaseURl() + path), responseBodyClass);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to constuct URI from " + getBaseURl() + path, e);
        }
    }

    protected <T> ResponseEntity<T> request(final String path, final HttpMethod method, final HttpEntity entity, final Class<T> responseBodyClass) {
        try {
            return restTemplate.exchange(new URI(getBaseURl() + path), method, entity, responseBodyClass);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to constuct URI from " + getBaseURl() + path, e);
        }
    }

    protected <T> ResponseEntity<T> post(final String path, final Object body, final Class<T> responseBodyClass) {
        try {
            return restTemplate.postForEntity(new URI(getBaseURl() + path), body, responseBodyClass);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to constuct URI from " + getBaseURl() + path, e);
        }
    }

    protected void checkResponse(final ResponseEntity<?> response, final int expectedCode) {
        assertEquals(expectedCode, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey(GAFFER_MEDIA_TYPE_HEADER), "Gaffer header was not present");
    }
}
