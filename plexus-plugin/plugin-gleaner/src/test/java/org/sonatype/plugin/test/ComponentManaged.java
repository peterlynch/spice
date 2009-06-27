package org.sonatype.plugin.test;

import javax.inject.Singleton;

import org.sonatype.plugin.Managed;

@Managed
@Singleton
public class ComponentManaged
{

    public String defines  = "an extention point";
    
}
