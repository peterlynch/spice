/**
 * Copyright 2008 Marvin Herman Froeder
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
package org.sonatype.plexus.component.bundlepublisher;

import java.io.File;
import java.io.InputStream;

import org.apache.maven.artifact.repository.ArtifactRepository;

public interface BundlePublisher
{

    String ROLE = BundlePublisher.class.getName();

    void install( File sourceFile, InputStream bundleDescriptor, ArtifactRepository localRepository )
        throws PublishingException;

    void deploy( File sourceFile, InputStream bundleDescriptor, ArtifactRepository deploymentRepository,
                 ArtifactRepository localRepository )
        throws PublishingException;

    void validate( File sourceFile, InputStream bundleDescriptor )
        throws PublishingException;

}
