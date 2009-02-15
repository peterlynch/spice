package org.sonatype.plexus.webcontainer;

import java.io.File;

public class Webapp 
	extends Servlet
{
    private File warPath;
    private File webappDir;

    public File getWarPath()
    {
        return warPath;
    }

    public File getWebappDir()
    {
        return webappDir;
    }    
}
