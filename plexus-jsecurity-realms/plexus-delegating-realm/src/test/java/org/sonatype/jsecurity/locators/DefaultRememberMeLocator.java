package org.sonatype.jsecurity.locators;

import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;

/**
 * @plexus.component
 */
public class DefaultRememberMeLocator
    implements
    RememberMeLocator
{
    public RememberMeManager getRememberMeManager()
    {
        // For simplicity's sake, just return this one
        return new WebRememberMeManager();
    }
}
