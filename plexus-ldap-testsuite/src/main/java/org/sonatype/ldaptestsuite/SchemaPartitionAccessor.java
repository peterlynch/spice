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
