package org.sonatype.jsecurity.realms.tools;

public interface PasswordGenerator
{
    String ROLE = PasswordGenerator.class.getName();
    
    String generatePassword( int minChars, int maxChars );
    
    String hashPassword( String password );
}
