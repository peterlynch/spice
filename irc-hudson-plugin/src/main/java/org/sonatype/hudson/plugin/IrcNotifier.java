package org.sonatype.hudson.plugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Cause.UserCause;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

public class IrcNotifier
    extends Notifier
{

    @Extension
    public static final IrcBuildStepDescriptor DESCRIPTOR = new IrcBuildStepDescriptor();

    public BuildStepMonitor getRequiredMonitorService()
    {
        System.out.println( "====================================================================" );
        System.out.println( "=                                                                  =" );
        System.out.println( "= required IrcNotifier                                             =" );
        System.out.println( "=                                                                  =" );
        System.out.println( "====================================================================" );

        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener )
        throws InterruptedException, IOException
    {
        System.out.println( "====================================================================" );
        System.out.println( "=                                                                  =" );
        System.out.println( "= perform IrcNotifier                                              =" );
        System.out.println( "=                                                                  =" );
        System.out.println( "====================================================================" );

        DESCRIPTOR.notify( build );
        return super.perform( build, launcher, listener );
    }

    @Override
    public IrcBuildStepDescriptor getDescriptor()
    {
        System.out.println( "====================================================================" );
        System.out.println( "=                                                                  =" );
        System.out.println( "= descriptor IrcNotifier                                           =" );
        System.out.println( "=                                                                  =" );
        System.out.println( "====================================================================" );

        return DESCRIPTOR;
    }

    public static class IrcBuildStepDescriptor
        extends BuildStepDescriptor<Publisher>
        implements IRCEventListener
    {
        private static final Logger LOG = Logger.getLogger( IrcBuildStepDescriptor.class.getName() );

        private transient volatile Object connectionNotifier = new Object();

        private transient volatile IRCConnection conn;

        private boolean enabled;

        private String hostname;

        private String password;

        private String nick;

        private Integer port;

        private String commandPrefix;

        private List<String> channels;

        private transient String connectionError;

        private transient boolean isRegistered;

        @SuppressWarnings( "unchecked" )
        @Override
        public boolean isApplicable( Class<? extends AbstractProject> jobType )
        {
            return true;
        }

        /**
         * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
         */
        @Override
        public Publisher newInstance( StaplerRequest req, JSONObject formData )
            throws FormException
        {
            return new IrcNotifier();
        }

        @Override
        public boolean configure( StaplerRequest req, JSONObject json )
            throws hudson.model.Descriptor.FormException
        {
            System.out.println( "====================================================================" );
            System.out.println( "=                                                                  =" );
            System.out.println( "= configure IrcNotifier                                            =" );
            System.out.println( "=                                                                  =" );
            System.out.println( "====================================================================" );

            enabled =
                "on".equals( req.getParameter( "irc_publisher.enabled" ) )
                    || "true".equals( req.getParameter( "irc_publisher.enabled" ) );
            if ( enabled )
            {
                hostname = req.getParameter( "irc_publisher.hostname" );
                password = req.getParameter( "irc_publisher.password" );
                nick = req.getParameter( "irc_publisher.nick" );
                try
                {
                    port = Integer.valueOf( req.getParameter( "irc_publisher.port" ) );
                    if ( port == null )
                    {
                        port = 6667;
                    }
                }
                catch ( NumberFormatException e )
                {
                    throw new FormException( "port field must be an Integer", "irc_publisher.port" );
                }
                commandPrefix = req.getParameter( "irc_publisher.commandPrefix" );
                if ( commandPrefix == null || "".equals( commandPrefix.trim() ) )
                {
                    commandPrefix = "~hudson";
                }
                else
                {
                    commandPrefix = commandPrefix.trim() + " ";
                }
                channels = Arrays.asList( req.getParameter( "irc_publisher.channels" ).split( " " ) );
            }

            save();

            if ( conn != null )
            {
                conn.close();
                conn = null;
            }

            try
            {
                getIrcConnection();
            }
            catch ( IOException e )
            {
                throw new FormException( e, null );
            }

            return super.configure( req, json );
        }

        private IRCConnection getIrcConnection()
            throws IOException
        {
            if ( enabled && conn == null )
            {
                conn = new IRCConnection( hostname, new int[] { port }, password, nick, nick, nick );
                conn.addIRCEventListener( this );
                conn.connect();

                synchronized ( connectionNotifier )
                {
                    try
                    {
                        connectionNotifier.wait();
                    }
                    catch ( InterruptedException e )
                    {
                        // ignorable
                    }
                    if ( connectionError != null )
                    {
                        throw new IOException( connectionError );
                    }
                }
            }

            return conn;
        }

        @Override
        public String getDisplayName()
        {
            return "IRC notification";
        }

        public void onDisconnected()
        {
            // TODO Auto-generated method stub

        }

        public void onError( String msg )
        {
            onError( -1, msg );
        }

        public void onError( int num, String msg )
        {
            if ( !isRegistered )
            {
                connectionError = msg;
                synchronized ( connectionNotifier )
                {
                    connectionNotifier.notifyAll();
                }
            }
            else
            {
                LOG.log( Level.WARNING, msg );
            }
        }

        public void onInvite( String chan, IRCUser user, String passiveNick )
        {
            // TODO Auto-generated method stub

        }

        public void onJoin( String chan, IRCUser user )
        {
            // TODO Auto-generated method stub

        }

        public void onKick( String chan, IRCUser user, String passiveNick, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onMode( String chan, IRCUser user, IRCModeParser modeParser )
        {
            // TODO Auto-generated method stub

        }

        public void onMode( IRCUser user, String passiveNick, String mode )
        {
            // TODO Auto-generated method stub

        }

        public void onNick( IRCUser user, String newNick )
        {
            // TODO Auto-generated method stub

        }

        public void onNotice( String target, IRCUser user, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onPart( String chan, IRCUser user, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onPing( String ping )
        {
            // TODO Auto-generated method stub

        }

        public void onPrivmsg( String target, IRCUser user, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onQuit( IRCUser user, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onRegistered()
        {
            LOG.fine( "Connected to " + hostname );

            isRegistered = true;

            for ( String channel : channels )
            {
                conn.doJoin( channel );
            }

            synchronized ( connectionNotifier )
            {
                connectionNotifier.notifyAll();
            }
        }

        public void onReply( int num, String value, String msg )
        {
            // TODO Auto-generated method stub

        }

        public void onTopic( String chan, IRCUser user, String topic )
        {
            // TODO Auto-generated method stub

        }

        public void unknown( String prefix, String command, String middle, String trailing )
        {
            // TODO Auto-generated method stub

        }

        public void notify( AbstractBuild<?, ?> build )
            throws InterruptedException, IOException
        {
            if ( !build.getResult().isWorseThan( Result.SUCCESS ) )
            {
                return;
            }

            List<String> relatedUsers = new ArrayList<String>();
            List<Cause> causes = build.getCauses();
            for ( Cause cause : causes )
            {
                if ( cause instanceof UserCause )
                {
                    UserCause uCause = (UserCause) cause;
                    relatedUsers.add( uCause.getUserName() );
                }
            }

            ChangeLogSet<? extends Entry> changes = build.getChangeSet();
            for ( Entry change : changes )
            {
                relatedUsers.add( change.getAuthor().getDisplayName() );
            }

            for ( String user : relatedUsers )
            {
                String msg =
                    build.getProject().getName() + " build is unstable  ( " + Hudson.getInstance().getRootUrl()
                        + build.getUrl() + " )";
                getIrcConnection().doPrivmsg( user, msg );
                for ( String channel : channels )
                {
                    getIrcConnection().doPrivmsg( channel, user + ": " + msg );
                }
            }

        }

        public void stop()
        {
            conn.close();
        }
    }

    @Override
    public boolean needsToRunAfterFinalized()
    {
        return true;
    }
}
