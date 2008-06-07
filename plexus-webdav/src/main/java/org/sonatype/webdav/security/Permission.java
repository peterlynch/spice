package org.sonatype.webdav.security;

/**
 * A simple security role class.
 * User defined roles are allowed, but the default ones are listed as static members.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Permission
{
    public static final Permission PERMISSION_REPOSITORY_READ = new Permission( "repository.read" );

    public static final Permission PERMISSION_REPOSITORY_WRITE = new Permission( "repository.write" );

    public static final Permission PERMISSION_SITE_REPOSITORY_READ = new Permission( "repository.read.read" );

    public static final Permission PERMISSION_SITE_REPOSITORY_WRITE = new Permission( "repository.site.write" );

    private String role;

    public Permission( String id )
    {
        this.role = id;
    }

    public String getId()
    {
        return role;
    }

    public String toString()
    {
        return "WebdavRole: " + role;
    }

    public boolean equals( Object compare )
    {
        if ( !( compare instanceof Permission ) )
        {
            return false;
        }

        return role.equals( ( (Permission) compare ).getId() );
    }
}