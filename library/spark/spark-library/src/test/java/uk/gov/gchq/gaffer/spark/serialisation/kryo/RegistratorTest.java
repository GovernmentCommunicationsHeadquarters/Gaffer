/*
 * Copyright 2016 Crown Copyright
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
package uk.gov.gchq.gaffer.spark.serialisation.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Before;
import org.junit.Test;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Entity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class RegistratorTest {

    private final Kryo kryo = new Kryo();

    @Before
    public void setup() {
        new Registrator().registerClasses(kryo);
    }

    @Test
    public void testEntity() {
        // Given
        Entity entity = new Entity.Builder().group("group")
                                            .vertex("abc")
                                            .property("property1", 1)
                                            .build();

        // When
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeObject(output, entity);
        output.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Input input = new Input(bais);
        Entity read = kryo.readObject(input, Entity.class);
        input.close();

        // Then
        assertEquals(entity, read);
    }

    @Test
    public void testEdge() {
        // Given
        Edge edge = new Edge.Builder().group("group").source("abc")
                                      .destination("xyz")
                                      .directed(true)
                                      .property("property1", 1).build();

        // When
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeObject(output, edge);
        output.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Input input = new Input(bais);
        Edge read = kryo.readObject(input, Edge.class);
        input.close();

        // Then
        assertEquals(edge, read);
    }

}
