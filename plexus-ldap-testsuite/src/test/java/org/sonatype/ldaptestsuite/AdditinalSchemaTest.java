package org.sonatype.ldaptestsuite;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.directory.server.schema.bootstrap.Schema;

public class AdditinalSchemaTest  extends AbstractLdapTestEnvironment
{

    
    public void testSchema()
    {
        
        Map<String,Schema> schemas = this.getLdapServer().directoryService.getRegistries().getLoadedSchemas();

        Assert.assertNotNull( schemas.get( "nis" ) );
        Assert.assertFalse( schemas.get( "nis" ).isDisabled() );
        
    }
    
}
