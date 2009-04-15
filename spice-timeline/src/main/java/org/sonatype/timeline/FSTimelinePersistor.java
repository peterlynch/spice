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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

/**
 * @author juven
 */
@Component( role = TimelinePersistor.class )
public class FSTimelinePersistor
    implements TimelinePersistor
{
    private File persistDirectory;

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
        ObjectOutputStream out = null;

        synchronized ( this )
        {
            try
            {
                out = new ObjectOutputStream( new FileOutputStream( getDataFile() ) );

                out.writeObject( record );
            }
            catch ( Exception e )
            {
                throw new TimelineException( "Failed to persist timeline record to data file!", e );
            }
            finally
            {
                if ( out != null )
                {
                    try
                    {
                        out.close();
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
            ObjectInputStream in = null;

            try
            {
                in = new ObjectInputStream( new FileInputStream( getDataFile() ) );

                while ( true )
                {
                    result.add( (TimelineRecord) in.readObject() );
                }

            }
            catch ( EOFException e )
            {
                // end of the file
            }
            catch ( Exception e )
            {
                throw new TimelineException( "Failed to read timeline record from data file!", e );
            }
            finally
            {
                if ( in != null )
                {
                    try
                    {
                        in.close();
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
