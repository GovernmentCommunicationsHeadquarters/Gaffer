/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.gaffer.script.operation.builder;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.gaffer.script.operation.DockerFileUtils;
import uk.gov.gchq.gaffer.script.operation.ScriptTestConstants;
import uk.gov.gchq.gaffer.script.operation.image.Image;
import uk.gov.gchq.gaffer.script.operation.provider.GitScriptProvider;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DockerImageBuilderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerImageBuilderTest.class);

    @Test
    public void bothPathsGivenShouldBuildImage() {

        // Given
        final String currentWorkingDirectory = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
        final String directoryPath = currentWorkingDirectory.concat(ScriptTestConstants.CURRENT_WORKING_DIRECTORY);
        Path pathAbsoluteScriptRepo = DockerFileUtils.getPathAbsoluteScriptRepo(directoryPath, ScriptTestConstants.REPO_NAME);
        DockerImageBuilder dockerImageBuilder = new DockerImageBuilder();

        final GitScriptProvider gitScriptProvider = new GitScriptProvider();

        try {
            gitScriptProvider.retrieveScripts(pathAbsoluteScriptRepo.toString(), ScriptTestConstants.REPO_URI);
        } catch (final GitAPIException | IOException e) {
            Assert.fail("Failed to get the scripts");
        }
        try {
            dockerImageBuilder.getFiles(directoryPath, "/.ScriptBin/default/Dockerfile");
        } catch (final IOException e) {
            Assert.fail("Failed to get the build files");
        }


        // When
        Image returnedImage = null;
        try {
            returnedImage = dockerImageBuilder.buildImage(ScriptTestConstants.SCRIPT_NAME, 0, null,
                    directoryPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String returnedImageId = returnedImage.getImageId();

        // Then
        Assert.assertNotNull(returnedImageId);
    }

    @Test
    public void blankDirectoryPathShouldBuildImage() {

        // Given
        final String currentWorkingDirectory = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
        final String directoryPath = currentWorkingDirectory.concat(ScriptTestConstants.CURRENT_WORKING_DIRECTORY);
        Path pathAbsoluteScriptRepo = DockerFileUtils.getPathAbsoluteScriptRepo(directoryPath, ScriptTestConstants.REPO_NAME);
        DockerImageBuilder dockerImageBuilder = new DockerImageBuilder();

        final GitScriptProvider gitScriptProvider = new GitScriptProvider();
        try {
            gitScriptProvider.retrieveScripts(pathAbsoluteScriptRepo.toString(), ScriptTestConstants.REPO_URI);
        } catch (GitAPIException | IOException e) {
            Assert.fail("Failed to retrieve the scripts");
        }
        // When
        try {
            dockerImageBuilder.getFiles(directoryPath, "");
        } catch (final IOException e) {
            Assert.fail("Failed to get the build files");
        }

        Image returnedImage = null;
        try {
            returnedImage = dockerImageBuilder.buildImage(ScriptTestConstants.SCRIPT_NAME, 0, null,
                    directoryPath);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        String returnedImageId = returnedImage.getImageId();

        // Then
        Assert.assertNotNull(returnedImageId);
    }

    @Test
    public void shouldThrowErrorIfPathsAreBlank() {
        // Given
        final String directoryPath = "";
        final String dockerfilePath = "";
        DockerImageBuilder dockerImageBuilder = new DockerImageBuilder();

        // Then
        assertThrows(NullPointerException.class, () -> dockerImageBuilder.getFiles(directoryPath, dockerfilePath));
    }
}