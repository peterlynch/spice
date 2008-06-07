package org.sonatype.webdav.security;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Authorization
{
    boolean authorize( User user, Permission permission );

    boolean authorize( User user, Permission permission, String path );
}
