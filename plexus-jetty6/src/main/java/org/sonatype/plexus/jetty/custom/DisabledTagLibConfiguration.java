package org.sonatype.plexus.jetty.custom;

import org.mortbay.jetty.webapp.TagLibConfiguration;

public class DisabledTagLibConfiguration
    extends TagLibConfiguration
{

    private static final long serialVersionUID = 1L;

    @Override
    public void configureWebApp()
        throws Exception
    {
        // Disable this from the superclass.
    }

}
