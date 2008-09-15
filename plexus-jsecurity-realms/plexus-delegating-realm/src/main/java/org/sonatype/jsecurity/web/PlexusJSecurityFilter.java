package org.sonatype.jsecurity.web;

import org.jsecurity.web.servlet.JSecurityFilter;

/**
 * Extension of JSecurityFilter that uses Plexus lookup to get the J
 * @author cstamas
 *
 */
public class PlexusJSecurityFilter
    extends JSecurityFilter
{
    public PlexusJSecurityFilter()
    {
        this.configClassName = PlexusConfiguration.class.getName();
    }
}
