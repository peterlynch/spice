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
package org.sonatype.timeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

import com.thoughtworks.xstream.XStream;

/**
 * @author juven
 */
@Component( role = TimelinePersistor.class )
public class FSTimelinePersistor
    implements TimelinePersistor
{
    private static XStream xStream;

    private File persistDirectory;

    {
        xStream = new XStream();

        xStream.processAnnotations( TimelineRecord.class );
    }

    public void configure( File persistDirectory )
    {
        this.persistDirectory = persistDirectory;

        if ( !this.persistDirectory.exists() )
        {
            this.persistDirectory.mkdirs();
        }
    }

    public void persist( TimelineRecord record )
        throws TimelineException
    {
        String xml = xStream.toXML( record );

        BufferedWriter writer = null;

        synchronized ( this )
        {
            try
            {
                writer = new BufferedWriter( new FileWriter( getDataFile(), true ) );

                writer.write( xml );

                writer.write( '\n' );
            }
            catch ( IOException e )
            {
                throw new TimelineException( "Failed to persist timeline record to data file!", e );
            }
            finally
            {
                if ( writer != null )
                {
                    try
                    {
                        writer.close();
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        }
    }

    public List<TimelineRecord> readAll()
        throws TimelineException
    {
        List<TimelineRecord> result = new ArrayList<TimelineRecord>();

        synchronized ( this )
        {
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader( new FileReader( getDataFile() ) );

                String line = null;

                while ( reader.ready() )
                {
                    line = reader.readLine();

                    if ( line.equals( "<record>" ) )
                    {
                        StringBuffer xmlRecord = new StringBuffer( line );

                        while ( true )
                        {
                            line = reader.readLine();
                            
                            xmlRecord.append( line );

                            if ( line.equals( "</record>" ) )
                            {
                                break;
                            }
                        }

                        result.add( (TimelineRecord) xStream.fromXML( xmlRecord.toString() ) );
                    }
                }

            }
            catch ( Exception e )
            {
                throw new TimelineException( "Failed to read timeline record from data file!", e );
            }
            finally
            {
                if ( reader != null )
                {
                    try
                    {
                        reader.close();
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        }

        return result;
    }

    private File getDataFile()
        throws IOException
    {
        File dataFile = new File( persistDirectory, "timeline.data" );

        if ( !dataFile.exists() )
        {
            dataFile.createNewFile();
        }

        return dataFile;
    }

}
