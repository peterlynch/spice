/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
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
