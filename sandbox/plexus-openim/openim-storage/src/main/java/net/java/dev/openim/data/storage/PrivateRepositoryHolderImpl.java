/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package net.java.dev.openim.data.storage;

import java.io.File;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import net.java.dev.openim.tools.XStreamStore;

/**
 * @version 1.5
 * @author AlAg
 */
public class PrivateRepositoryHolderImpl
    extends AbstractLogEnabled
    implements PrivateRepositoryHolder, Initializable
{

    private XStreamStore repository;

    // Configuration
    private String filename;
    private String encoding;

    public void initialize()
        throws InitializationException
    {

        File storeFile = new File( filename );
        if( !storeFile.exists() )
        {
            storeFile.getParentFile().mkdirs();
        }

        repository = new XStreamStore( storeFile, getLogger(), encoding );
        repository.load();
    }

    //--------------------------------------------------------------------------
    public String getData( String username, String key )
    {
        String data = null;
        try
        {
            String repKey = username + "::" + key;
            data = (String) repository.get( repKey );
            getLogger().debug( "Get key: " + repKey + " => " + data );
        }
        catch ( Exception e )
        {
            getLogger().debug( "Username " + username + " dont have private for element " + key );
        }
        return data;
    }

    public void setData( String username, String key, String data )
    {
        String repKey = username + "::" + key;
        getLogger().debug( "Put key: " + repKey + " => " + data );
        repository.put( repKey, data );
    }

}
