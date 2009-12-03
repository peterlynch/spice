package org.sonatype.buup.backup;

import java.io.File;

public class OperatedFile
{
    private final File originalFile;

    private final File operatedFile;

    private final Operation operation;

    public OperatedFile( Operation oper, File originalFile, File operatedFile )
    {
        this.operation = oper;

        this.originalFile = originalFile;

        this.operatedFile = operatedFile;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public File getOriginalFile()
    {
        return originalFile;
    }

    public File getOperatedFile()
    {
        return operatedFile;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( operation == null ) ? 0 : operation.hashCode() );
        result = prime * result + ( ( originalFile == null ) ? 0 : originalFile.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        OperatedFile other = (OperatedFile) obj;
        if ( operation == null )
        {
            if ( other.operation != null )
                return false;
        }
        else if ( !operation.equals( other.operation ) )
            return false;
        if ( originalFile == null )
        {
            if ( other.originalFile != null )
                return false;
        }
        else if ( !originalFile.equals( other.originalFile ) )
            return false;
        return true;
    }
}
