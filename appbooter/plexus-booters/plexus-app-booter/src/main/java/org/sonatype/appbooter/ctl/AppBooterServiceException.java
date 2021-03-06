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
package org.sonatype.appbooter.ctl;

/**
 * Thrown if there is a problem starting/stopping a Service.
 */
public class AppBooterServiceException
    extends Exception
{
    private static final long serialVersionUID = -2544659876529142511L;

    public AppBooterServiceException( String message )
    {
        super( message );
    }

    public AppBooterServiceException( Throwable cause )
    {
        super( cause );
    }

    public AppBooterServiceException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
