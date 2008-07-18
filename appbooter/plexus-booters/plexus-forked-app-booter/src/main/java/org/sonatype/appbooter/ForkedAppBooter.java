package org.sonatype.appbooter;

import org.sonatype.appbooter.ctl.Service;



/**
 * The ForkedAppBooter makes the Service interface a Plexus component geared to starting a container as a separate process.
 *
 */
public interface ForkedAppBooter extends Service
{

    /** The Plexus role identifier. */
    public static String ROLE = ForkedAppBooter.class.getName();

}
