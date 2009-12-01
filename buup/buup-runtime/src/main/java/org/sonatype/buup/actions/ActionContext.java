package org.sonatype.buup.actions;

import java.io.File;
import java.util.HashMap;

import org.sonatype.buup.Buup;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

public class ActionContext
    extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1783950650409400826L;
    
    public Buup getBuup()
    {
        return  null;
    }

    public WrapperConfEditor getWrapperConfEditor()
    {
        return null;
    }

    public File getBasedir()
    {
        return null;
    }

    public File getUpgradeBundleBasedir()
    {
        return null;
    }
}
