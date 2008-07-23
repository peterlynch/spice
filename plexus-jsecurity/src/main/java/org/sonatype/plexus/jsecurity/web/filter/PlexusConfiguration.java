package org.sonatype.plexus.jsecurity.web.filter;

import java.util.List;

import javax.servlet.ServletContext;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.JSecurityException;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.mgt.SecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.util.LifecycleUtils;
import org.jsecurity.web.config.IniWebConfiguration;

public class PlexusConfiguration
    extends IniWebConfiguration
{
    public static final String SECURITY_MANAGER_ROLE = "securityManagerRole";

    public static final String DEFAULT_SECURITY_MANAGER_ROLE = SecurityManager.class.getName();

    public static final String SECURITY_MANAGER_ROLE_HINT = "securityManagerRoleHint";

    public static final String DEFAULT_SECURITY_MANAGER_ROLE_HINT = PlexusConstants.PLEXUS_DEFAULT_HINT + "-web";

    protected String securityManagerRole;

    protected String securityManagerRoleHint;

    public String getSecurityManagerRole()
    {
        if ( securityManagerRole != null )
        {
            return securityManagerRole;
        }
        else
        {
            return DEFAULT_SECURITY_MANAGER_ROLE;
        }
    }

    public void setSecurityManagerRole( String securityManagerRole )
    {
        this.securityManagerRole = securityManagerRole;
    }

    public String getSecurityManagerRoleHint()
    {
        if ( securityManagerRoleHint != null )
        {
            return securityManagerRoleHint;
        }
        else
        {
            return DEFAULT_SECURITY_MANAGER_ROLE_HINT;
        }
    }

    public void setSecurityManagerRoleHint( String securityManagerRoleHint )
    {
        this.securityManagerRoleHint = securityManagerRoleHint;
    }

    @Override
    public void init()
        throws JSecurityException
    {
        String role = getFilterConfig().getInitParameter( SECURITY_MANAGER_ROLE );

        if ( role != null )
        {
            setSecurityManagerRole( role );
        }

        String roleHint = getFilterConfig().getInitParameter( SECURITY_MANAGER_ROLE_HINT );

        if ( roleHint != null )
        {
            setSecurityManagerRoleHint( roleHint );
        }

        super.init();
    }

    @Override
    protected SecurityManager createSecurityManager()
    {
        ServletContext servletContext = getFilterConfig().getServletContext();

        PlexusContainer container = (PlexusContainer) servletContext.getAttribute( PlexusConstants.PLEXUS_KEY );

        return getOrCreateSecurityManager( container );
    }

    protected SecurityManager getOrCreateSecurityManager( PlexusContainer container )
    {
        SecurityManager securityManager = null;

        try
        {
            securityManager = (SecurityManager) container.lookup(
                getSecurityManagerRole(),
                getSecurityManagerRoleHint() );
        }
        catch ( ComponentLookupException e )
        {
            log.info( "Could not lookup SecurityManager with role='" + getSecurityManagerRole() + "' and roleHint='"
                + getSecurityManagerRoleHint() + "'. Will look for Realms..." );

            securityManager = null;
        }

        if ( securityManager == null )
        {
            securityManager = createDefaultSecurityManagerFromRealms( container );
        }

        if ( securityManager == null )
        {
            String msg = "There is no component with role "
                + SecurityManager.class.getName()
                + " available in the "
                + "Plexus Context. If your security manager uses different role and roleHint, you can specify those with this filter's '"
                + SECURITY_MANAGER_ROLE + "' and '" + SECURITY_MANAGER_ROLE_HINT + "' init-params.";

            throw new JSecurityException( msg );
        }
        return securityManager;
    }

    @SuppressWarnings( "unchecked" )
    protected SecurityManager createDefaultSecurityManagerFromRealms( PlexusContainer container )
    {
        SecurityManager securityManager = null;

        // Create security manager according to superclass
        securityManager = super.createSecurityManager();

        try
        {
            List<Realm> realms = container.lookupList( Realm.class );

            if ( !realms.isEmpty() )
            {
                if ( securityManager instanceof RealmSecurityManager )
                {
                    RealmSecurityManager realmSM = (RealmSecurityManager) securityManager;

                    realmSM.setRealms( realms );
                }
                else
                {
                    log.warn( "Attempted to set realms declared in Plexus Context on SecurityManager, but was not of "
                        + "type RealmSecurityManager - instead was of type: " + securityManager.getClass().getName() );
                }
            }
        }
        catch ( ComponentLookupException e )
        {
            log.warn( "Attempted to lookup realms declared in Plexus Context but found none", e );
        }

        LifecycleUtils.init( securityManager );

        return securityManager;
    }
}
