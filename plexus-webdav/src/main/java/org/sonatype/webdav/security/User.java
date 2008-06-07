package org.sonatype.webdav.security;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface User
{
    final String ANONYMOUS = "anonymous";

    String getUsername();

    String getEmail();

    void setEmail( String email );

    boolean isAnonymous();
    
}
