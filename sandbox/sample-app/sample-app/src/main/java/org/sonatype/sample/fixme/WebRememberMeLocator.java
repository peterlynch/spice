package org.sonatype.sample.fixme;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;
import org.sonatype.jsecurity.locators.RememberMeLocator;

@Component( role = RememberMeLocator.class )
public class WebRememberMeLocator
    implements RememberMeLocator
{

    public RememberMeManager getRememberMeManager()
    {
//        return new WebRememberMeManager();
        return null;
    }

}
