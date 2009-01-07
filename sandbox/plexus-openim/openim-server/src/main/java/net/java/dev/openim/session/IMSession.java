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
package net.java.dev.openim.session;

import java.net.Socket;


import java.io.IOException;


import net.java.dev.openim.IMRouter;
import net.java.dev.openim.jabber.Streams;
import org.xmlpull.v1.XmlPullParser;


/**
 * @version 1.0
 * @author AlAg
 */
public interface IMSession 
{
    public static final int    UNKNOWN_CONNECTION = 0; 
    public static final int    C2S_CONNECTION = 1; 
    public static final int    S2S_L2R_CONNECTION = 2;
    public static final int    S2S_R2L_CONNECTION = 3;   

    public void setup( Socket socket ) throws Exception;
	public boolean isClosed();
    public void close();

    
    public long getId();

    public XmlPullParser getXmlPullParser();
    
    public int getConnectionType();

    public void writeOutputStream( String s ) throws IOException;
    public String getEncoding();
    
    public void setRouter( IMRouter router );
    public IMRouter getRouter();
    public void setStreams(Streams streams);
    public Streams getStreams();
}



