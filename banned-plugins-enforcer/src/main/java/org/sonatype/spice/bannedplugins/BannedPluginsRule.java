package org.sonatype.spice.bannedplugins;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugins.enforcer.BannedDependencies;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class BannedPluginsRule
    extends BannedDependencies
{

    @SuppressWarnings( "unchecked" )
    @Override
    public void execute( EnforcerRuleHelper helper )
        throws EnforcerRuleException
    {
        MavenProject project = null;
        try
        {
            project = (MavenProject) helper.evaluate( "${project}" );
        }
        catch ( ExpressionEvaluationException eee )
        {
            throw new EnforcerRuleException( "Unable to retrieve the MavenProject: ", eee );
        }

        Set<Artifact> dependencies = project.getPluginArtifacts();
        Set<Artifact> foundExcludes = checkDependencies( dependencies, helper.getLog() );

        if ( foundExcludes != null && !foundExcludes.isEmpty() )
        {
            StringBuffer buf = new StringBuffer();
            if ( message != null )
            {
                buf.append( message + "\n" );
            }
            for ( Artifact artifact : foundExcludes )
            {
                buf.append( "Found Banned Plugin: " + artifact.getId() + "\n" );
            }

            message = buf.toString();
            throw new EnforcerRuleException( message );
        }
    }

}
