package org.sonatype.buup.cfgfiles;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

/**
 * Default implementation for support editing/changing of "properties-like" configuration files, with support for
 * "multi values" in JSW fashion (by adding key suffixes like key.1, key.2, etc).
 * 
 * @author cstamas
 */
public class DefaultPropertiesFile
    implements PropertiesFile
{
    /**
     * The file from where we load the config.
     */
    private final File wrapperConfFile;

    /**
     * Lines of the files beeing edited
     */
    private LinkedList<String> lines;

    /**
     * The pattern used to detect commented out lines. The default is "starts with '#'" (but watch for blanklines).
     */
    private Pattern commentPattern = Pattern.compile( "^\\s*#\\s*.*" );

    public Pattern getCommentPattern()
    {
        return commentPattern;
    }

    public void setCommentPattern( Pattern commentPattern )
    {
        this.commentPattern = commentPattern;
    }

    public DefaultPropertiesFile( File file )
        throws IOException
    {
        this.wrapperConfFile = file;

        reset();
    }

    public void reset()
        throws IOException
    {
        // clear the buffer that holds the changes
        lines = null;

        // reload
        load();
    }

    public void save()
        throws IOException
    {
        save( wrapperConfFile );
    }

    public void save( File target )
        throws IOException
    {
        BufferedOutputStream bout = new BufferedOutputStream( new FileOutputStream( target ) );

        try
        {
            PrintWriter writer = new PrintWriter( bout );

            java.util.Iterator<String> i = getLines().iterator();

            while ( i.hasNext() )
            {
                writer.println( i.next() );
            }

            writer.flush();
        }
        finally
        {
            bout.close();
        }
    }

    public String getProperty( String key, String defaultValue )
    {
        int lineIndex = getLineIndexWithKey( key );

        if ( lineIndex > -1 )
        {
            return getValueFromLine( getLines().get( lineIndex ), defaultValue );
        }
        else
        {
            return defaultValue;
        }
    }

    public String getProperty( String key )
    {
        return getProperty( key, null );
    }

    public int getIntegerProperty( String key, int defaultValue )
    {
        try
        {
            return Integer.valueOf( getProperty( key, String.valueOf( defaultValue ) ) );
        }
        catch ( NumberFormatException e )
        {
            return defaultValue;
        }
    }

    public void setProperty( String key, String value )
    {
        setProperty( getLineIndexWithKey( key ), key, value );
    }

    protected void setProperty( int lineToChange, String key, String value )
    {
        String newLine = key + "=" + value;

        if ( lineToChange > -1 )
        {
            String existingLine = getLines().get( lineToChange );

            String existingLineKey = getKeyFromLine( existingLine );

            if ( existingLineKey != null && existingLineKey.startsWith( key ) )
            {
                getLines().remove( lineToChange );
            }

            getLines().add( lineToChange, newLine );
        }
        else
        {
            getLines().add( newLine );
        }
    }

    public void setIntegerProperty( String key, int value )
    {
        setProperty( key, String.valueOf( value ) );
    }

    public boolean removeProperty( String key )
    {
        int lineIndex = getLineIndexWithKey( key );

        if ( lineIndex > -1 )
        {
            getLines().remove( lineIndex );

            return true;
        }
        else
        {
            return false;
        }
    }

    public List<String> getPropertyList( String key )
    {
        // set idx to add or set it as last line
        int idxOfFirstOne = getLineIndexWithKey( key, false );

        if ( idxOfFirstOne != -1 )
        {
            ArrayList<String> result = new ArrayList<String>();

            // remove them and remember position of 1st
            String line = getLines().get( idxOfFirstOne++ );

            String keyFromLine = getKeyFromLine( line );

            while ( keyFromLine != null && keyFromLine.startsWith( key ) )
            {
                String value = getValueFromLine( line, null );

                if ( value != null )
                {
                    result.add( value );
                }

                line = getLines().get( idxOfFirstOne++ );

                keyFromLine = getKeyFromLine( line );
            }

            return result;
        }
        else
        {
            return null;
        }
    }

    public void setPropertyList( String key, List<String> values )
    {
        // set idx to add or set it as last line
        int idxOfFirstOne = getLineIndexWithKey( key, false );

        if ( idxOfFirstOne != -1 )
        {
            // remove them and remember position of 1st
            String line = getLines().get( idxOfFirstOne );

            String keyFromLine = getKeyFromLine( line );

            while ( keyFromLine != null && keyFromLine.startsWith( key ) )
            {
                getLines().remove( idxOfFirstOne );

                line = getLines().get( idxOfFirstOne );

                keyFromLine = getKeyFromLine( line );
            }
        }

        if ( values != null )
        {
            // add them
            int runIdx = 1;

            for ( String value : values )
            {
                setProperty( idxOfFirstOne, key + "." + runIdx, value );

                idxOfFirstOne++;

                runIdx++;
            }
        }
    }

    public Map<String, String> getAllKeyValuePairs()
    {
        // TODO: this is wrong implementation, since if there are multiple lines of same key, those will not be
        // represented in result Map. Either use Multimap or something else should be done here.

        HashMap<String, String> result = new HashMap<String, String>();

        LinkedList<String> lines = getLines();

        for ( String line : lines )
        {
            String key = getKeyFromLine( line );

            if ( key != null )
            {
                String value = getValueFromLine( line, null );

                result.put( key, value );
            }
        }

        return result;
    }

    // ==

    protected LinkedList<String> getLines()
    {
        if ( lines == null )
        {
            lines = new LinkedList<String>();
        }

        return lines;
    }

    protected void load()
        throws IOException
    {
        InputStream in = new FileInputStream( wrapperConfFile );

        LinkedList<String> lines = getLines();

        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

            String line;

            while ( ( line = reader.readLine() ) != null )
            {
                lines.add( line );
            }
        }
        finally
        {
            in.close();
        }
    }

    protected int getLineIndexWithKey( String key )
    {
        return getLineIndexWithKey( key, true );
    }

    protected int getLineIndexWithKey( String key, boolean exact )
    {
        LinkedList<String> lines = getLines();

        for ( int idx = 0; idx < lines.size(); idx++ )
        {
            String line = lines.get( idx );

            if ( !exact )
            {
                String keyFromLine = getKeyFromLine( line );

                if ( keyFromLine != null && keyFromLine.startsWith( key ) )
                {
                    return idx;
                }
            }
            else
            {
                if ( StringUtils.equals( key, getKeyFromLine( line ) ) )
                {
                    return idx;
                }
            }
        }

        return -1;
    }

    protected String getKeyFromLine( String line )
    {
        String[] elems = explodeLine( line );

        if ( elems != null && elems.length >= 2 )
        {
            return elems[0];
        }

        return null;
    }

    protected String getValueFromLine( String line, String defaultValue )
    {
        String[] elems = explodeLine( line );

        if ( elems != null && elems.length == 2 )
        {
            return elems[1];
        }
        else if ( elems != null && elems.length > 2 )
        {
            StringBuilder sb = new StringBuilder();
            
            for ( int idx = 1; idx < elems.length; idx++ )
            {
                sb.append( elems[idx] );
                
                sb.append( "=" );
            }

            return sb.substring( 0, sb.length()-1 ); 
        }

        return defaultValue;
    }

    protected String[] explodeLine( String line )
    {
        if ( isLineCommentedOut( line ) )
        {
            return null;
        }

        return line.split( "\\s=\\s|\\s=|=\\s|=" );
    }

    protected boolean isLineCommentedOut( String line )
    {
        Pattern pattern = getCommentPattern();

        if ( pattern != null )
        {
            return getCommentPattern().matcher( line ).matches();
        }
        else
        {
            // no comment pattern, no comments
            return false;
        }
    }
}
