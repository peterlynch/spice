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
package net.java.dev.openim.data;

/**
 * @version 1.0
 *
 * @author AlAg
 */
public interface Account {
    
    public static final int AUTH_TYPE_PLAIN = 1;
    public static final int AUTH_TYPE_DIGEST = 2;
    
    public void setName( String name );
    public String getName();

    
    public void setPassword( String password );
    public String getPassword();
    

    public boolean isAuthenticationTypeSupported( int type );
    public void authenticate( int type, String value, String sessionId ) throws Exception;
        
    
}

