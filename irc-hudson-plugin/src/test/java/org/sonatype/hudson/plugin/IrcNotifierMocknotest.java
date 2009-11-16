package org.sonatype.hudson.plugin;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class IrcNotifierMocknotest
    extends HudsonTestCase
{

    @Test
    public void test()
        throws Exception
    {
        // HtmlPage configPage = new WebClient().goTo( "configure" );
        // HtmlForm form = configPage.getFormByName( "config" );
        // WebAssert.assertElementPresent( configPage, "irc_publisher.enabled" );
        // form.submit( (HtmlButton) last( form.getHtmlElementsByTagName( "button" ) ) );
        //
        // FreeStyleProject project = createFreeStyleProject();
        // FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
        // System.out.println( build.getDisplayName() + " completed" );
        //
        // // TODO: change this to use HtmlUnit
        // String s = FileUtils.readFileToString( build.getLogFile() );
        // assertTrue( s.contains( "+ echo hello" ) );
        //
        // StaplerRequest req = mock( StaplerRequest.class );
        // when( req.getParameter( "irc_publisher.enabled" ) ).thenReturn( "true" );
        // when( req.getParameter( "irc_publisher.hostname" ) ).thenReturn( "irc.sonatype.com" );
        // when( req.getParameter( "irc_publisher.port" ) ).thenReturn( "6667" );
        // when( req.getParameter( "irc_publisher.nick" ) ).thenReturn( "test-bot" );
        // when( req.getParameter( "irc_publisher.channels" ) ).thenReturn( "#ci" );
        // IrcNotifier.DESCRIPTOR.configure( req, null );

        // UserCause userCause = mock( UserCause.class );
        // when( userCause.getUserName() ).thenReturn( "tester" );
        // Cause cause = userCause;
        //
        // AbstractProject project = mock( AbstractProject.class );
        // when( project.getName() ).thenReturn( "test-project" );
        //
        // IrcNotifier not = new IrcNotifier();
        // AbstractBuild build = mock( AbstractBuild.class );
        // when( build.getResult() ).thenReturn( Result.UNSTABLE );
        // when( build.getCauses() ).thenReturn( Arrays.asList( cause ) );
        // when( build.getProject() ).thenReturn( project );
        //
        // not.perform( build, null, null );
    }
}
