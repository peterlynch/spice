package org.sonatype.webdav;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.webcontainer.ServletContainer;

public class WebdavTest extends PlexusTestCase 
{
	public void testWebdav() throws Exception 
	{
		lookup( ServletContainer.class );
	}

    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
    	try 
    	{
    		URL url = new File( getBasedir(), "src/main/plexus/plexus.xml" ).toURI().toURL();
			containerConfiguration.setContainerConfigurationURL( url );
		} 
    	catch (MalformedURLException e) 
    	{
			// do nothing
		}
    }
}
