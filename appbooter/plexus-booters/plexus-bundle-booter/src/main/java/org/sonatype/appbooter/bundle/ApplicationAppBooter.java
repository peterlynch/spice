package org.sonatype.appbooter.bundle;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appbooter.TerminalContextPublisher;
import org.sonatype.appbooter.bundle.service.BundleService;

public class ApplicationAppBooter
    extends PlexusAppBooter
{
    private final ClassRealm realm;

    private List<BundleService> bundleServices;

    public ApplicationAppBooter( String name, ClassRealm realm )
    {
        this.realm = realm;

        setName( name );

        setWorld( realm.getWorld() );

        // do not publish to SystemProperties, but to terminal only
        getContextPublishers().clear();
        getContextPublishers().add( new TerminalContextPublisher() );
    }

    public ClassRealm getRealm()
    {
        return realm;
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        super.customizeContainerConfiguration( containerConfiguration );

        containerConfiguration.setRealm( getRealm() );
    }

    public List<BundleService> getBundleServices()
    {
        if ( bundleServices == null )
        {
            bundleServices = new ArrayList<BundleService>();
        }

        return bundleServices;
    }

    @Override
    public void startContainer()
        throws PlexusContainerException
    {
        super.startContainer();

        for ( BundleService service : getBundleServices() )
        {
            service.startManage( this );
        }
    }

    @Override
    public void stopContainer()
    {
        for ( BundleService service : getBundleServices() )
        {
            service.stopManage( this );
        }

        super.stopContainer();
    }
}
