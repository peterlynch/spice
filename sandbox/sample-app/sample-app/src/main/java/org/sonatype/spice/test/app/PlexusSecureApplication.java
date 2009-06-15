package org.sonatype.spice.test.app;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.PlexusMutableWebConfiguration;
import org.sonatype.security.web.PlexusWebConfiguration;
import org.sonatype.security.web.SecurityConfigurationException;

@Component( role = Application.class, hint = "secureApplication" )
public class PlexusSecureApplication
    extends PlexusRestletApplicationBridge
{

    @Requirement
    private PlexusWebConfiguration plexusWebConfiguration;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        super.doCreateRoot( root, isStarted );

        // protecting service resources with "wall" permission
        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                // We are adding a flat wall to be hit if a mapping is missed
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource(
                    "/**",
                    "authcBasic,perms[sample:permToCatchAllUnprotecteds]" );
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to add WALL to the end of the chain", e );
            }

            // signal we finished adding resources
            ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).protectedResourcesAdded();
        }
    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource( descriptor
                    .getPathPattern(), descriptor.getFilterExpression() );
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to protect resource mounted to "
                    + resource.getResourceUri() + " of class " + resource.getClass().getName(), e );
            }
        }
    }

    protected PlexusWebConfiguration getPlexusWebConfiguration()
    {
        return this.plexusWebConfiguration;
    }

}
