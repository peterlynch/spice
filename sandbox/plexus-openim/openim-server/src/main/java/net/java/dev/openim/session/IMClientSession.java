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
package net.java.dev.openim.session;


import net.java.dev.openim.data.jabber.User;
import net.java.dev.openim.data.jabber.IMPresence;


/**
 * @version 1.5
 * @author AlAg
 */
public interface IMClientSession extends IMSession
{
    // C2S session  (will split)
    public User getUser();
    public void setUser( User user );
    
    public IMPresence getPresence();
    public void setPresence( IMPresence presence );
}


