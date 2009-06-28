package org.sonatype.plugin.test;

import javax.inject.Named;

@Named( "another" )
public class NamedUserCustomComponent
    implements UserCustomComponent
{

    public String getMessage()
    {
        return "another";
    }

}
