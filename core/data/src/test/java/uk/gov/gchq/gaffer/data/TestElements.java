/*
 * Copyright 2017 Crown Copyright
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
package uk.gov.gchq.gaffer.data;

import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Entity;

public class TestElements {

    // Edges
    public static Edge getEdge() {
        return new Edge.Builder().group(TestGroups.EDGE)
                                 .build();
    }

    // Entities
    public static Entity getEntity() {
        return new Entity.Builder().group(TestGroups.ENTITY)
                                   .build();
    }

    public static Entity getEntity_2() {
        return new Entity.Builder().group(TestGroups.ENTITY_2)
                                   .build();
    }
}
