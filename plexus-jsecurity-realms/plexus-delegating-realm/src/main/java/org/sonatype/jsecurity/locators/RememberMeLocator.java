package org.sonatype.jsecurity.locators;

import org.jsecurity.subject.RememberMeManager;

public interface RememberMeLocator
{
    String ROLE = RememberMeLocator.class.getName();
    
    RememberMeManager getRememberMeManager();
}
