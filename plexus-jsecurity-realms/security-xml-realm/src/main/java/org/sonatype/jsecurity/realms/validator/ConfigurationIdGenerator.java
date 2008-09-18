package org.sonatype.jsecurity.realms.validator;

public interface ConfigurationIdGenerator
{
    String ROLE = ConfigurationIdGenerator.class.getName();
    
    String generateId();
}
