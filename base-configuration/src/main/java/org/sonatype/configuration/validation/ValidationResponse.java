/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.configuration.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * A validation response, returned after configuration validation from validator.
 * 
 * @author cstamas
 */
public class ValidationResponse
{
    /**
     * A simple counter to enumerate messages.
     */
    private int key = 1;

    /**
     * A flag to mark is the config valid (usable) or not.
     */
    private boolean valid = true;

    /**
     * A flag to mark is the config modified during validation or not.
     */
    private boolean modified = false;

    /**
     * List of validation errors.
     */
    private List<ValidationMessage> validationErrors;

    /**
     * List of valiation warnings.
     */
    private List<ValidationMessage> validationWarnings;

    /**
     * Context for validators to communicate.
     */
    private ValidationContext context;

    public boolean isValid()
    {
        return valid;
    }

    public void setValid( boolean valid )
    {
        this.valid = valid;
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified( boolean modified )
    {
        this.modified = modified;
    }

    public List<ValidationMessage> getValidationErrors()
    {
        if ( validationErrors == null )
        {
            validationErrors = new ArrayList<ValidationMessage>();
        }
        return validationErrors;
    }

    public ValidationMessage getValidationError( String key )
    {
        if ( validationErrors != null )
        {
            for ( ValidationMessage vm : validationErrors )
            {
                if ( vm.getKey().equals( key ) )
                {
                    return vm;
                }
            }
        }

        return null;
    }

    public void setValidationErrors( List<ValidationMessage> validationErrors )
    {
        this.validationErrors = validationErrors;

        valid = validationErrors == null || validationErrors.size() == 0;
    }

    public void addValidationError( ValidationMessage message )
    {
        getValidationErrors().add( message );

        this.valid = false;
    }

    public void addValidationError( String message )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message );

        addValidationError( e );
    }

    public void addValidationError( String message, Throwable t )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message, t );

        addValidationError( e );
    }

    public ValidationMessage getValidationWarning( String key )
    {
        if ( validationWarnings != null )
        {
            for ( ValidationMessage vm : validationWarnings )
            {
                if ( vm.getKey().equals( key ) )
                {
                    return vm;
                }
            }
        }

        return null;
    }

    public List<ValidationMessage> getValidationWarnings()
    {
        if ( validationWarnings == null )
        {
            validationWarnings = new ArrayList<ValidationMessage>();
        }
        return validationWarnings;
    }

    public void setValidationWarnings( List<ValidationMessage> validationWarnings )
    {
        this.validationWarnings = validationWarnings;
    }

    public void addValidationWarning( ValidationMessage message )
    {
        getValidationWarnings().add( message );
    }

    public void addValidationWarning( String message )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message );

        addValidationWarning( e );
    }

    /**
     * A method to append a validation response to this validation response. The errors list and warnings list are
     * simply appended, and the isValid is logically AND-ed and isModified is logically OR-ed.
     * 
     * @param validationResponse
     */
    public void append( ValidationResponse validationResponse )
    {
        for ( ValidationMessage msg : validationResponse.getValidationErrors() )
        {
            if ( getValidationError( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationError( msg );
        }

        for ( ValidationMessage msg : validationResponse.getValidationWarnings() )
        {
            if ( getValidationWarning( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationWarning( msg );
        }

        setValid( isValid() && validationResponse.isValid() );

        setModified( isModified() || validationResponse.isModified() );
    }

    public void setContext( ValidationContext context )
    {
        this.context = context;
    }

    public ValidationContext getContext()
    {
        return context;
    }
}
