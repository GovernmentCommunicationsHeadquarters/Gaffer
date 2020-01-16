/*
 * Copyright 2016-2019 Crown Copyright
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

package uk.gov.gchq.gaffer.script.operation.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class", defaultImpl = GitScriptProvider.class)
@JsonSubTypes({@JsonSubTypes.Type(value = GitScriptProvider.class)})
@JsonDeserialize

public interface ScriptProvider {

    /**
     * Gets the scripts from the given repo URI and places
     * them at the given path
     *
     * @param absoluteRepoPath       the path to clone the repo to
     * @param repoURI                the URI of the repo with the scripts
     * @throws Exception             exception if it fails to get the latest scripts
     */
    void retrieveScripts(String absoluteRepoPath,
                         String repoURI) throws Exception;
}