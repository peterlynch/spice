package org.sonatype.jsecurity.realms;

import org.jsecurity.authc.UsernamePasswordToken;

public class UsernamePasswordRealmToken
    extends UsernamePasswordToken
{
    private String realmName;
    
    public UsernamePasswordRealmToken( String username, String password, String realmName )
    {
        super( username, password );
        
        this.realmName = realmName;
    }
    
    public String getRealmName()
    {
        return realmName;
    }
    
    public void setRealmName( String realmName )
    {
        this.realmName = realmName;
    }
}
