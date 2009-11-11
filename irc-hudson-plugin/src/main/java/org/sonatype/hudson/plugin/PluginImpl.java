package org.sonatype.hudson.plugin;

import hudson.Plugin;

/**
 * @plugin
 */
public class PluginImpl
    extends Plugin
{

    @Override
    public void stop()
        throws Exception
    {
        IrcNotifier.DESCRIPTOR.stop();
    }

}
