package org.sonatype.appbooter;

import java.io.File;
import java.util.List;

import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

/**
 * The default implementation of ForkedAppBooter, a.k.a. a Simple Plexus Component.
 * 
 * @see AbstractForkedAppBooter
 * 
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.appbooter.test.ForkedAppBooter"
 *                   role-hint="DefaultForkedAppBooter"
 *
 */
public class DefaultForkedAppBooter extends AbstractForkedAppBooter
{
    /**
     * @plexus.configuration default-value="some-jar"
     */
    private String platformFile;
    
    
    /** 
     * @plexus.configuration default-value="${basedir}/runtime/apps/nexus/conf, ${basedir}/runtime/apps/nexus/lib/*.jar" 
     */
    private List<String> classPathElements;
    

    public ClassworldsRealmConfiguration getClassworldsRealmConfig()
    {
        ClassworldsRealmConfiguration rootRealmConfig = new ClassworldsRealmConfiguration( "plexus" );
        
        rootRealmConfig.addLoadPatterns( classPathElements );
        return rootRealmConfig;
    }


    public File getPlatformFile()
        throws AppBooterServiceException
    {  
        return new File( platformFile );
    }


    

}
