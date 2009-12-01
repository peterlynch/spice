package org.sonatype.buup.actions;

import java.io.File;
import java.util.Map;

import org.sonatype.buup.Buup;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

public interface ActionContext
    extends Map<String, Object>
{
    /**
     * Returns the Buup instance that is running the action.
     * 
     * @return
     */
    Buup getBuup();

    /**
     * Gets the wrapper.conf editor to apply all the changes and at the end will be swapped in as the one used by
     * bundle.
     * 
     * @return
     */
    WrapperConfEditor getWrapperConfEditor();

    /**
     * Returns the bundle (beeing upgraded) basedir.
     * 
     * @return
     */
    File getBasedir();

    /**
     * Returns the upgrade-bundle basedir.
     * 
     * @return
     */
    File getUpgradeBundleBasedir();
}
