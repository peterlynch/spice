package org.sonatype.jsecurity.realms.tools.dao;

import org.sonatype.jsecurity.model.CProperty;

public class SecurityProperty
    extends CProperty
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityProperty()
    {
    }
    
    public SecurityProperty( CProperty property )
    {
        setKey( property.getKey() );
        setValue( property.getValue() );
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }
}
