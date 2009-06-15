package org.sonatype.plugin.metadata;

import org.sonatype.plugin.metadata.gleaner.GleanerException;

public interface PluginMetadataGenerator
{

    void generatePluginDescriptor( PluginMetadataGenerationRequest request ) throws GleanerException;
    
}
