package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jsecurity.authc.UsernamePasswordToken;

public class UsernamePasswordRealmToken
    extends UsernamePasswordToken
{
    private Set<String> realmNames = new HashSet<String>();
    
    public UsernamePasswordRealmToken( String username, String password, Set<String> realmNames)
    {
        super( username, password );
        
        this.realmNames = realmNames;
    }
    
    public UsernamePasswordRealmToken( String username, String password, String realmName )
    {
        this( username, password, Collections.singleton( realmName ) );
    }
    
    public Set<String> getRealmNames()
    {
        return realmNames;
    }
    
    public void setRealmNames( Set<String> realmNames )
    {
        this.realmNames = realmNames;
    }
}
