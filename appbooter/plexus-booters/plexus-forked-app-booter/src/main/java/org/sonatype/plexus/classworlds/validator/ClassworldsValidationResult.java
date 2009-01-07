/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.classworlds.validator;

import java.util.ArrayList;
import java.util.List;

public class ClassworldsValidationResult
{

    private List<String> errors = new ArrayList<String>();

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setErrors( List<String> errors )
    {
        this.errors = errors;
    }

    public void addError( String error )
    {
        errors.add( error );
    }

    public String render()
    {
        StringBuilder builder = new StringBuilder();

        if ( !errors.isEmpty() )
        {
            builder.append( "Configuration contains the following errors:\n\n" );

            for ( String error : errors )
            {
                builder.append( "\n" ).append( error );
            }
        }
        else
        {
            builder.append( "Configuration is valid." );
        }

        return builder.toString();
    }

}
