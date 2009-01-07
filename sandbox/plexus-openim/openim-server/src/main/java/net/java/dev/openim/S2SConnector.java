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

import java.io.IOException;
import net.java.dev.openim.session.SessionsManager;
import net.java.dev.openim.session.IMServerSession;

/**
 * @version 1.5
 * @author AlAg
 */
public interface S2SConnector extends Runnable {
    
    public void setToHostname( String toHostname );    
    public void setRouter( IMRouter router );    
    public void setIMConnectionHandler( IMConnectionHandler connectionHandler );
    public void setSessionsManager( SessionsManager sessionManager );
    
    public IMServerSession getSession() throws Exception ;
    public void sendResult() throws IOException;
    public void sendVerify( String dialbackValue, String id ) throws IOException;
    
    public void run() ;
    public boolean isAlive() ;
    
} // class
