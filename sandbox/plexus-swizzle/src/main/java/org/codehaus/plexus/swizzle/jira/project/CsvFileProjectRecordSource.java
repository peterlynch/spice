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
package org.codehaus.plexus.swizzle.jira.project;

import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Jason van Zyl
 * @version $Id$
 */
public class CsvFileProjectRecordSource
    implements ProjectRecordSource
{
    private File source;

    private Map variables;

    public CsvFileProjectRecordSource( File source )
    {
        this.source = source;

        variables = new HashMap();
    }

    public Iterator getRecords()
        throws ProjectRecordRetrievalException
    {
        try
        {
            return new RecordIterator( source );
        }
        catch ( FileNotFoundException e )
        {
            throw new ProjectRecordRetrievalException( "Cannot find source file: " + source );
        }
    }

    class RecordIterator
        implements Iterator
    {
        private BufferedReader reader;

        private String line;

        public RecordIterator( File source )
            throws FileNotFoundException
        {
            reader = new BufferedReader( new FileReader( source ) );
        }

        public boolean hasNext()
        {
            try
            {
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( line.indexOf( "=" ) > 0 )
                    {
                        String[] s = StringUtils.split( line, "=" );

                        String key = s[0].trim();

                        String value = s[1].trim();

                        variables.put( key, value );

                        continue;
                    }
                    else if ( line.startsWith( "#" ) || line.trim().length() == 0 )
                    {
                        continue;
                    }

                    break;
                }
            }
            catch ( IOException e )
            {
                // do nothing
            }

            return line != null;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Object next()
        {
            String[] s = StringUtils.split( StringUtils.interpolate( line, variables ), "," );

            return new ProjectRecord( s[0], s[1], s[2], s[3], s[4], s[5] );
        }
    }
}

