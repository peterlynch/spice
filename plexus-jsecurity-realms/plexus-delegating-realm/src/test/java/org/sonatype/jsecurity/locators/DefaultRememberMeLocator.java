package org.sonatype.jsecurity.locators;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;

@Component( role = RememberMeLocator.class )
public class DefaultRememberMeLocator
    implements RememberMeLocator
{
    public RememberMeManager getRememberMeManager()
    {
        // For simplicity's sake, just return this one
        return new WebRememberMeManager();
    }
}
