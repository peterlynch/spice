package org.sonatype.jettytestsuite.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.DefaultServlet;

public class SleepingServlet
    extends DefaultServlet
{

    public static final String SLEEP_PARAM_KEY = "numberOfMillisecondsToSleep";
    
    private static final long serialVersionUID = -6822203057278773072L;

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        if( this.getInitParameter( SLEEP_PARAM_KEY ) != null )
        {
            int numberOfMillisecondsToSleep = new Integer( this.getInitParameter( SLEEP_PARAM_KEY ) );
          try
          {
              Thread.sleep( numberOfMillisecondsToSleep );
          }
          catch ( InterruptedException e )
          {
              // we just care about the sleep part
          }
        }   
        
        super.doGet( request, response );
    }
}
