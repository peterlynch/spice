package org.sonatype.guice.plexus.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

final class AnnotatedComponentScanner
    implements ClassVisitor, AnnotationVisitor
{
    private static final int CLASS_READER_FLAGS =
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    private static final String EXTENSION_POINT_DESC = Type.getDescriptor( ExtensionPoint.class );

    private static final String MANAGED_DESC = Type.getDescriptor( Managed.class );

    private static final String NAMED_DESC = Type.getDescriptor( Named.class );

    private static final String SINGLETON_DESC = Type.getDescriptor( Singleton.class );

    private final ClassSpace space;

    private final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();

    private String implementation;

    private String instantiationStrategy;

    private String hint;

    private boolean isExtension;

    private boolean isManaged;

    AnnotatedComponentScanner( final ClassSpace space )
    {
        this.space = space;
    }

    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) != 0 )
        {
            return;
        }

        for ( final String i : interfaces )
        {
            try
            {
                implementation = name;
                instantiationStrategy = "per-lookup";
                hint = Hints.DEFAULT_HINT;

                isExtension = false;
                isManaged = false;

                scan( space.getResources( i + ".class" ).nextElement() );

                if ( isExtension || isManaged )
                {
                    implementation = implementation.replace( '/', '.' );
                    if ( isExtension && Hints.isDefaultHint( hint ) )
                    {
                        hint = implementation;
                    }
                    final Class<?> role = space.loadClass( i.replace( '/', '.' ) );
                    final Component component = new ComponentImpl( role, hint, instantiationStrategy, "" );
                    components.put( component, space.deferLoadClass( implementation ) );
                }
            }
            catch ( final Exception e )
            {
                // ignore?
            }
            finally
            {
                implementation = null;
            }
        }
    }

    public void visitSource( final String source, final String debug )
    {
    }

    public void visitOuterClass( final String owner, final String name, final String desc )
    {
    }

    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( null != implementation )
        {
            if ( EXTENSION_POINT_DESC.equals( desc ) )
            {
                isExtension = true;
            }
            else if ( MANAGED_DESC.equals( desc ) )
            {
                isManaged = true;
            }
            else if ( NAMED_DESC.equals( desc ) )
            {
                return this;
            }
            else if ( SINGLETON_DESC.equals( desc ) )
            {
                instantiationStrategy = "singleton";
            }
        }
        return null;
    }

    public void visitAttribute( final Attribute attr )
    {
    }

    public void visitInnerClass( final String name, final String outerName, final String innerName, final int access )
    {
    }

    public FieldVisitor visitField( final int access, final String name, final String desc, final String signature,
                                    final Object value )
    {
        return null;
    }

    public MethodVisitor visitMethod( final int access, final String name, final String desc, final String signature,
                                      final String[] exceptions )
    {
        return null;
    }

    public void visit( final String name, final Object value )
    {
        if ( "value".equals( name ) && value instanceof String )
        {
            hint = (String) value;
        }
    }

    public void visitEnum( final String name, final String desc, final String value )
    {
    }

    public AnnotationVisitor visitAnnotation( final String name, final String desc )
    {
        return null;
    }

    public AnnotationVisitor visitArray( final String name )
    {
        return null;
    }

    public void visitEnd()
    {
    }

    void scan( final URL url )
        throws IOException
    {
        final InputStream in = url.openStream();
        try
        {
            new ClassReader( in ).accept( this, CLASS_READER_FLAGS );
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    Map<Component, DeferredClass<?>> getComponents()
    {
        return components;
    }
}