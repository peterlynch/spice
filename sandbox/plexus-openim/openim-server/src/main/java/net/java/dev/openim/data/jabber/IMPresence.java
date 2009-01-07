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
package net.java.dev.openim.data.jabber;

import net.java.dev.openim.data.Transitable;

/**
 * @author AlAg
 */
public interface IMPresence extends Transitable 
{
    public static final String TYPE_AVAILABLE   = "available";
    public static final String TYPE_UNAVAILABLE = "unavailable";
    public static final String TYPE_SUBSCRIBE   = "subscribe";
    public static final String TYPE_SUBSCRIBED  = "subscribed";
    public static final String TYPE_UNSUBSCRIBE = "unsubscribe";
    public static final String TYPE_UNSUBSCRIBED= "unsubscribed";
    public static final String TYPE_PROBE       = "probe";

    public void setStatus( String status );
    public String getStatus();
    
    public String getPriority();
    public void setPriority( String priority );

    public void setShow( String show );
    public String getShow();
    
    public Object clone();
    
}

