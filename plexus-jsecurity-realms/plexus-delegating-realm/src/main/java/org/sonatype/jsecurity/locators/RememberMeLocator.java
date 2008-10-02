package org.sonatype.jsecurity.locators;

import org.jsecurity.subject.RememberMeManager;

public interface RememberMeLocator
{
    RememberMeManager getRememberMeManager();
}
