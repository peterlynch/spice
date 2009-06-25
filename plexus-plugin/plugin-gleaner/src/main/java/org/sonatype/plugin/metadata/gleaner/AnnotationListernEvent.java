package org.sonatype.plugin.metadata.gleaner;

public class AnnotationListernEvent
{
    private Object annotation;

    private String className;

    public AnnotationListernEvent( Object annotation, String className )
    {
        this.annotation = annotation;

        this.className = className;
    }

    public Object getAnnotation()
    {
        return annotation;
    }

    public String getClassName()
    {
        return className;
    }
}
