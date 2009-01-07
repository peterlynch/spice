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
package org.sonatype.ldaptestsuite;

import java.lang.reflect.Field;

import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.schema.PartitionSchemaLoader;
import org.apache.directory.server.core.schema.SchemaService;
import org.apache.directory.server.schema.registries.DefaultRegistries;
import org.apache.directory.server.schema.registries.Registries;
import org.apache.directory.server.schema.registries.SchemaLoader;

public class SchemaPartitionAccessor
{

    public static Partition getSchemaPartition(SchemaService schemaService) throws Exception
    {
        Field field = SchemaService.class.getDeclaredField( "schemaPartition" );
        field.setAccessible( true );
        Partition partition =  (Partition) field.get( schemaService );
        
        return partition;        
    }
    
    public static PartitionSchemaLoader getSchemaLoader(DefaultRegistries registries )  throws Exception
    {
        Field field = DefaultRegistries.class.getDeclaredField( "schemaLoader" );
        field.setAccessible( true );
        PartitionSchemaLoader schemaLoader =  (PartitionSchemaLoader) field.get( registries );
        
        return schemaLoader; 
    }
    
}
