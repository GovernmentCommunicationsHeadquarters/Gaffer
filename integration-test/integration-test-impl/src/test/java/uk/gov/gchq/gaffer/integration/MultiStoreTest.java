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

package uk.gov.gchq.gaffer.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.gchq.gaffer.integration.extensions.GafferTestContextProvider;
import uk.gov.gchq.gaffer.integration.extensions.LoaderTestContextProvider;
import uk.gov.gchq.gaffer.integration.extensions.ProxyGraphResetter;
import uk.gov.gchq.gaffer.integration.extensions.SpringBootLoader;
import uk.gov.gchq.gaffer.rest.GafferWebApplication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({GafferTestContextProvider.class, LoaderTestContextProvider.class, ProxyGraphResetter.class, SpringBootLoader.class})
@SpringBootTest(classes = GafferWebApplication.class, webEnvironment = DEFINED_PORT)
@ActiveProfiles("proxy")
public @interface MultiStoreTest {
}