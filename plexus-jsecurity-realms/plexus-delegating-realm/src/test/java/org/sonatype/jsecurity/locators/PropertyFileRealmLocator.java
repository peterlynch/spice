package org.sonatype.jsecurity.locators;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.realm.Realm;

@Component( role = RealmLocator.class )
public class PropertyFileRealmLocator
    extends AbstractLogEnabled
    implements RealmLocator, Serviceable
{
    private ArrayList<Realm> locatedRealms = new ArrayList<Realm>();

    public static final String PLEXUS_LOADER = "plexus-loader";

    public static final String REALM_IMPL_KEY = "realm-implementations";

    @Configuration( value = "${realm-locator-property-file}" )
    private File propertyFile;

    public void service( ServiceLocator locator )
    {
        Properties properties = PropertyUtils.loadProperties( propertyFile );

        if ( properties == null )
        {
            getLogger().error(
                "Unable to load properties file " + propertyFile.getAbsolutePath() + " no realms located" );
            return;
        }

        String realmlist = (String) properties.get( REALM_IMPL_KEY );

        if ( StringUtils.isEmpty( realmlist ) )
        {
            getLogger().error( "No realms located in " + REALM_IMPL_KEY + " property" );
            return;
        }

        String[] realms = realmlist.split( "," );

        boolean plexusLoader = Boolean.parseBoolean( (String) properties.get( PLEXUS_LOADER ) );

        for ( String realm : realms )
        {
            if ( plexusLoader )
            {
                try
                {
                    locatedRealms.add( (Realm) locator.lookup( Realm.class.getName(), realm.trim() ) );
                }
                catch ( ComponentLookupException e )
                {
                    getLogger().error( "Unable to load component using plexus", e );
                }
            }
            else
            {
                try
                {
                    locatedRealms.add( (Realm) Class.forName( realm.trim() ).newInstance() );
                }
                catch ( ClassNotFoundException e )
                {
                    getLogger().error( "Unable to load component using plexus", e );
                }
                catch ( InstantiationException e )
                {
                    getLogger().error( "Unable to load component using plexus", e );
                }
                catch ( IllegalAccessException e )
                {
                    getLogger().error( "Unable to load component using plexus", e );
                }
            }
        }
    }

    public List<Realm> getRealms()
    {
        return Collections.unmodifiableList( locatedRealms );
    }
}
