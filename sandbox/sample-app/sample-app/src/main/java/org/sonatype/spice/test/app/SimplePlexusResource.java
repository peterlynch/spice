package org.sonatype.spice.test.app;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role=PlexusResource.class, hint="sample-resource" )
public class SimplePlexusResource
    extends AbstractPlexusResource
{

    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( this.getResourceUri(), "authcBasic,perms[sample:priv-name]" );
    }

    public String getResourceUri()
    {
        return "/test";
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return "Hello";
    }

}
