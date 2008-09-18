package org.sonatype.jsecurity.realms.tools;

import java.io.StringWriter;

import org.sonatype.jsecurity.realms.validator.ValidationMessage;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

public class InvalidConfigurationException
    extends Exception
{
    /**
     * The validation response.
     */
    private ValidationResponse validationResponse;

    public InvalidConfigurationException()
    {
        this( "Configuration is invalid!" );
    }

    public InvalidConfigurationException( String msg )
    {
        super( msg );
    }

    public InvalidConfigurationException( String msg, Throwable t )
    {
        super( msg, t );
    }

    public InvalidConfigurationException( ValidationResponse validationResponse )
    {
        this();

        this.validationResponse = validationResponse;
    }

    public ValidationResponse getValidationResponse()
    {
        return validationResponse;
    }

    public String getMessage()
    {
        StringWriter sw = new StringWriter();

        sw.append( super.getMessage() );

        if ( getValidationResponse() != null )
        {
            if ( getValidationResponse().getValidationErrors().size() > 0 )
            {
                sw.append( "\nValidation errors follows:\n" );

                for ( ValidationMessage error : getValidationResponse().getValidationErrors() )
                {
                    sw.append( error.toString() );
                }
                sw.append( "\n" );
            }

            if ( getValidationResponse().getValidationWarnings().size() > 0 )
            {
                sw.append( "\nValidation warnings follows:\n" );

                for ( ValidationMessage warning : getValidationResponse().getValidationWarnings() )
                {
                    sw.append( warning.toString() );
                }
                sw.append( "\n" );
            }
        }

        return sw.toString();
    }

}
