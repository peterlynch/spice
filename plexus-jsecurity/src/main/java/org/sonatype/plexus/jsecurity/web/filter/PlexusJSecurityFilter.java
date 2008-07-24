package org.sonatype.plexus.jsecurity.web.filter;

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
