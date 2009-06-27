package org.sonatype.plugin.test;

import javax.inject.Singleton;

import org.sonatype.plugin.ExtensionPoint;

@ExtensionPoint
@Singleton
public class ComponentExtentionPoint
{

    public String defines  = "an extention point";
    
}
