package org.sonatype.webdav;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.webcontainer.ServletContainer;

public class WebdavTest extends PlexusTestCase 
{

    File _testFile;
    
    String _testText;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        _testFile = new File( "./target/webdav/test.txt" );
        _testFile.deleteOnExit();
        
        _testFile.getParentFile().mkdirs();
        
        _testText = System.currentTimeMillis() +" ms";
        
        FileOutputStream out = new FileOutputStream( _testFile );
        out.write( _testText.getBytes() );
        out.flush();
        out.close();
    }
    
	public void testWebdav() throws Exception 
	{
		lookup( ServletContainer.class );
		
		URL url = new URL("http://localhost:9000/webdav");
		
		InputStream in = url.openStream();
		
		byte [] buf = new byte [ 32 ];
		
		int n = in.read( buf );
		
		in.close();
		
		assertEquals( _testText.getBytes().length << 1 , n );
		
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
