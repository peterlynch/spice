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
package net.java.dev.openim.data.jabber;


import java.util.List;

/*
 * @version 1.5
 *
 * @author AlAg
 */
public interface User 
{
    
    public void setName( String username );
    public String getName();
    
    public void setHostname( String hostname );
    public String getHostname();
    
    public void setPassword( String password );
    public String getPassword();
    
    public void setDigest( String digest );
    public String getDigest();
    
    public void setResource( String resource );
    public String getResource();
    
    public boolean isAuthenticationTypeSupported( int type );
    public void authenticate( String sessionId ) throws Exception;
    
    public String getJID();
    public String getNameAndRessource();
    public String getJIDAndRessource();

    public void setRosterItemList( List rosterlist );
    public List<IMRosterItem> getRosterItemList();


}


