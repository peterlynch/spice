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
package net.java.dev.openim;

import java.util.List;

import net.java.dev.openim.data.Transitable;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.0
 * @author AlAg
 */
public interface IMRouter 
{
    public void setS2SConnectorManager( S2SConnectorManager s2sConnectorManager );
    public S2SConnectorManager  getS2SConnectorManager();
    
    // client session related
    public void registerSession( IMClientSession session );
    public void unregisterSession( IMClientSession session );
    public List<IMSession> getAllRegisteredSession( String username );
    public void releaseSessions();
    
    public void route( IMSession session, Transitable message ) throws java.io.IOException;

    

}
