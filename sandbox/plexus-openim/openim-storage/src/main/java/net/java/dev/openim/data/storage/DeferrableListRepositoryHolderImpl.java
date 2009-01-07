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
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import net.java.dev.openim.tools.XStreamStore;

/**
 * @version 1.5
 * @author AlAg
 */
public class DeferrableListRepositoryHolderImpl
    extends AbstractLogEnabled
    implements DeferrableListRepositoryHolder, Initializable
{

    // Locals
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
        repository.substitute( "net.java.dev.openim.data.jabber.IMMessage", "message" );
        repository.load();

    }

    
    //--------------------------------------------------------------------------     
    public List getDeferrableList( String username )
    {
        List list = null;
        try
        {
            list = (List) repository.get( username );
        }
        catch ( Exception e )
        {
            getLogger().warn( "User " + username + " message list not found" );
        }
        return list;
    }

    //--------------------------------------------------------------------------    
    public void setDeferrableList( String username, List deferrableList )
    {
        if ( username != null && deferrableList != null )
        {
            repository.put( username, deferrableList );
        }
    }

}
