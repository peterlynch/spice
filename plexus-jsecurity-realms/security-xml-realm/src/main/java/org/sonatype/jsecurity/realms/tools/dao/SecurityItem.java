package org.sonatype.jsecurity.realms.tools.dao;

public interface SecurityItem
{
    boolean isReadOnly();
    void setReadOnly( boolean readOnly );
}
