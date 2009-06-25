package org.sonatype.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as component contract, and implementations will be managed components/beans (by some container).
 * 
 * @author cstamas
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Managed
{
}
