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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.gchq.gaffer.core.exception.Error;
import uk.gov.gchq.gaffer.rest.integration.AbstractRestApiIT;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_BANNER_COLOUR;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_BANNER_DESCRIPTION;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_DESCRIPTION;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_DESCRIPTION_DEFAULT;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_DOCUMENTATION_URL;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_DOCUMENTATION_URL_DEFAULT;
import static uk.gov.gchq.gaffer.rest.SystemProperty.APP_TITLE;
import static uk.gov.gchq.gaffer.rest.SystemProperty.EXPOSED_PROPERTIES;
import static uk.gov.gchq.gaffer.rest.SystemProperty.FAVICON_LARGE_URL;
import static uk.gov.gchq.gaffer.rest.SystemProperty.FAVICON_SMALL_URL;
import static uk.gov.gchq.gaffer.rest.SystemProperty.GAFFER_VERSION;
import static uk.gov.gchq.gaffer.rest.SystemProperty.GAFFER_VERSION_DEFAULT;
import static uk.gov.gchq.gaffer.rest.SystemProperty.KORYPHE_VERSION;
import static uk.gov.gchq.gaffer.rest.SystemProperty.KORYPHE_VERSION_DEFAULT;
import static uk.gov.gchq.gaffer.rest.SystemProperty.LOGO_IMAGE_URL;
import static uk.gov.gchq.gaffer.rest.SystemProperty.LOGO_IMAGE_URL_DEFAULT;
import static uk.gov.gchq.gaffer.rest.SystemProperty.LOGO_LINK;
import static uk.gov.gchq.gaffer.rest.SystemProperty.LOGO_LINK_DEFAULT;

@ActiveProfiles("propertiesIT")
public class PropertiesControllerIT extends AbstractRestApiIT {

    private static final Map<String, String> DEFAULT_PROPERTIES;

    static {
        final java.util.Map<String, String> map = new LinkedHashMap<>();
        map.put(APP_TITLE, "PropertiesIntegrationTest");
        map.put(APP_DESCRIPTION, APP_DESCRIPTION_DEFAULT);
        map.put(APP_BANNER_DESCRIPTION, "");
        map.put(APP_BANNER_COLOUR, "");
        map.put(APP_DOCUMENTATION_URL, APP_DOCUMENTATION_URL_DEFAULT);
        map.put(LOGO_LINK, LOGO_LINK_DEFAULT);
        map.put(LOGO_IMAGE_URL, "/rest/images/logo.png");
        map.put(FAVICON_SMALL_URL, LOGO_IMAGE_URL_DEFAULT);
        map.put(FAVICON_LARGE_URL, LOGO_IMAGE_URL_DEFAULT);
        map.put(GAFFER_VERSION, GAFFER_VERSION_DEFAULT);
        map.put(KORYPHE_VERSION, KORYPHE_VERSION_DEFAULT);
        DEFAULT_PROPERTIES = Collections.unmodifiableMap(map);
    }

    @BeforeEach
    public void clearGafferSystemProperties() {
        System.clearProperty(APP_DESCRIPTION);
        System.clearProperty(EXPOSED_PROPERTIES);
        System.clearProperty("gaffer.random.property");
    }

    @Test
    public void shouldReturnAMapOfGafferSystemProperties() {
        // When
        ResponseEntity<Map> response = get("/properties", Map.class);

        // Then
        checkResponse(response, 200);
        assertEquals(DEFAULT_PROPERTIES, response.getBody());
    }

    @Test
    public void shouldIncludePropertiesSetBySystemProperty() {
        // Given
        System.setProperty(APP_DESCRIPTION, "My super graph");

        // When
        ResponseEntity<Map> response = get("/properties", Map.class);

        // Then
        checkResponse(response, 200);
        assertEquals(11, response.getBody().size());
        assertEquals("My super graph", response.getBody().get(APP_DESCRIPTION));
    }

    @Test
    public void shouldNotIncludeGafferPropertiesWhichAreNotPartOfTheCoreProperties() {
        // Given
        System.setProperty("gaffer.random.property", "test");

        // When
        ResponseEntity<Map> response = get("/properties", Map.class);

        // Then
        checkResponse(response, 200);
        assertEquals(11, response.getBody().size());
        assertFalse(response.getBody().containsKey("gaffer.random.property"), "Expected the property not to be returned");
    }

    @Test
    public void shouldIncludeNonCorePropertiesIfSetByCorePropertiesProperty() {
        // Given
        System.setProperty("gaffer.properties", "gaffer.random.property");
        System.setProperty("gaffer.random.property", "test");

        // When
        ResponseEntity<Map> response = get("/properties", Map.class);

        // Then
        checkResponse(response, 200);
        assertEquals(12, response.getBody().size());
        assertTrue(response.getBody().containsKey("gaffer.random.property"), "Expected the property to be returned");

    }

    @Test
    public void shouldNotReturnNonCoreProperties() {
        // Given
        System.setProperty("gaffer.random.property", "test");

        // When
        ResponseEntity<Error> response = get("/properties/gaffer.random.property", Error.class);

        // Then
        checkResponse(response, 404);
        assertEquals("Property: gaffer.random.property could not be found.", response.getBody().getSimpleMessage());
    }

    @Test
    public void shouldReturnErrorIfPropertyIsNotFound() {
        // When
        ResponseEntity<Error> response = get("/properties/gaffer.non-existent.property", Error.class);

        // Then
        checkResponse(response, 404);
        assertEquals("Property: gaffer.non-existent.property could not be found.", response.getBody().getSimpleMessage());
    }

    @Test
    public void shouldReturnPropertyIfFound() {
        // When
        ResponseEntity<String> response = get("/properties/" + APP_TITLE, String.class);

        // Then
        checkResponse(response, 200);
        assertEquals("PropertiesIntegrationTest", response.getBody());
    }

    @Test
    public void shouldReturnPropertyIfAddedToCoreProperties() {
        // Given
        System.setProperty("gaffer.random.property", "test");
        System.setProperty("gaffer.properties", "gaffer.random.property");

        // When
        ResponseEntity<String> response = get("/properties/gaffer.random.property", String.class);

        // Then
        checkResponse(response, 200);
        assertEquals("test", response.getBody());
    }
}
