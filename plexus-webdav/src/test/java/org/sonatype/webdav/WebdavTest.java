package org.sonatype.webdav;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.webcontainer.ServletContainer;

public class WebdavTest
	extends PlexusTestCase
{
    public void testWebdav()
        throws Exception
	{
		lookup( ServletContainer.class );
	}
}
