package org.sonatype.buup.actions;

import java.io.File;
import java.util.Map;

import org.sonatype.buup.Buup;

public interface ActionContext
    extends Map<Object, Object>
{
    /**
     * Returns the BUUP instance in which is this AtionContext run.
     * 
     * @return
     */
    Buup getBuup();

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
    
    /**
     * Returns the upgrade-bundle content basedir.
     * 
     * @return
     */
    File getUpgradeBundleContentBasedir();

    /**
     * Returns the unmodifiable map of BUUP invocation parameters.
     * 
     * @return
     */
    Map<String, String> getParameters();
}
