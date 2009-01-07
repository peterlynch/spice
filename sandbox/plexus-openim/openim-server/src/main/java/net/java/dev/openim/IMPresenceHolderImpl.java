/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package net.java.dev.openim;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.tools.JIDParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMPresenceHolderImpl
    extends AbstractLogEnabled
    implements IMPresenceHolder
{

    Map<String, Map<String, IMPresence>> presenceMap = new HashMap<String, Map<String, IMPresence>>();

    public void setPresence( String jid, IMPresence presence )
    {
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map == null )
            {
                map = new HashMap<String, IMPresence>();
            }
            map.put( jid, presence );
            presenceMap.put( name, map );
        }
    }

    public Collection<IMPresence> getPresence( String jid )
    {
        Collection<IMPresence> col = null;
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map != null )
            {
                col = map.values();
            }
        }
        return col;
    }

    public IMPresence removePresence( String jid )
    {
        IMPresence presence = null;
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map != null )
            {
                presence = map.remove( jid );
                if ( map.isEmpty() )
                {
                    presenceMap.remove( map );
                }
            }
        }
        return presence;
    }

}
