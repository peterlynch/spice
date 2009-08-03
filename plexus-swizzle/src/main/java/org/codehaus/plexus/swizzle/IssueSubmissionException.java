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
package org.codehaus.plexus.swizzle;

public class IssueSubmissionException
    extends Exception
{
    public IssueSubmissionException( String arg0, Throwable arg1 )
    {
        super( arg0, arg1 );
    }

    public IssueSubmissionException( String arg0 )
    {
        super( arg0 );
    }

    public IssueSubmissionException( Throwable arg0 )
    {
        super( arg0 );
    }
}