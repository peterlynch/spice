package org.sonatype.idiom.confluence;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.idiom.IdiomException;
import org.sonatype.idiom.IdiomPlugin;

@Component(role = IdiomPlugin.class, hint = "confluence")
public class IdiomConfluencePlugin
    implements IdiomPlugin
{
    public void execute()
        throws IdiomException
    {
        System.out.println( "hello!" );
    }
}
