package org.sonatype.spice.jscoverage;

import java.io.IOException;

public interface JsonReportHandler
{

    public void appendResults( String json );

    public void persist( )
        throws IOException;

}
