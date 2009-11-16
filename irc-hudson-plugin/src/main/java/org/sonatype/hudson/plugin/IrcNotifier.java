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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventAdapter;
import org.schwering.irc.lib.IRCUser;

public class IrcNotifier
    extends Notifier
{

    @Extension
    public static final IrcBuildStepDescriptor DESCRIPTOR = new IrcBuildStepDescriptor();

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener )
        throws InterruptedException, IOException
    {
        DESCRIPTOR.notify( build );
        return super.perform( build, launcher, listener );
    }

    @Override
    public IrcBuildStepDescriptor getDescriptor()
    {
        return DESCRIPTOR;
    }

    public static class IrcBuildStepDescriptor
        extends BuildStepDescriptor<Publisher>
    {
        private static final Logger LOG = Logger.getLogger( IrcBuildStepDescriptor.class.getName() );

        private transient Object connectionNotifier = new Object();

        private transient IRCConnection conn;

        private boolean enabled;

        private String hostname;

        private String password;

        private String nick;

        private Integer port;

        private String commandPrefix;

        private List<String> channels;

        private transient String connectionError;

        private transient boolean isRegistered;

        private transient Map<String, List<String>> pendingMessages = new LinkedHashMap<String, List<String>>();

        protected IrcBuildStepDescriptor()
        {
            load();
        }

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
                conn.addIRCEventListener( new IRCEventAdapter()
                {
                    @Override
                    public void onError( String msg )
                    {
                        onError( -1, msg );
                    }

                    @Override
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

                    @Override
                    public void onJoin( String chan, IRCUser user )
                    {
                        checkPendingMessage( user.getNick() );
                    }

                    private void checkPendingMessage( String user )
                    {
                        List<String> msgs = pendingMessages.get( user );
                        if ( msgs != null )
                        {
                            for ( String msg : msgs )
                            {
                                conn.doPrivmsg( user, msg );
                            }

                            msgs.clear();
                        }
                    }

                    @Override
                    public void onNick( IRCUser user, String newNick )
                    {
                        checkPendingMessage( user.getNick() );
                        checkPendingMessage( newNick );
                    }

                    @Override
                    public void onPrivmsg( String target, IRCUser user, String msg )
                    {
                        checkPendingMessage( user.getNick() );

                        if ( msg.startsWith( commandPrefix ) )
                        {
                            conn.doPrivmsg( target, "PONG" );
                        }
                    }

                    @Override
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

                } );
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

            final IRCConnection conn = getIrcConnection();
            for ( String channel : channels )
            {
                final String buildMsg =
                    build.getProject().getName() + " build is unstable  ( " + Hudson.getInstance().getRootUrl()
                        + build.getUrl() + " )";
                if ( relatedUsers == null || relatedUsers.isEmpty() )
                {
                    conn.doPrivmsg( channel, "Unknown user: " + buildMsg );
                }
                else
                {
                    for ( final String user : relatedUsers )
                    {
                        conn.doPrivmsg( channel, user + ": " + buildMsg );
                        IRCEventAdapter userListener = new IRCEventAdapter()
                        {
                            @Override
                            public void onError( int num, String msg )
                            {
                                if ( num == 401 )
                                {
                                    addPendingMessage( user, buildMsg );
                                }
                                conn.removeIRCEventListener( this );
                            }

                        };
                        conn.addIRCEventListener( userListener );
                        conn.doPrivmsg( user, buildMsg );
                    }
                }

            }

        }

        private void addPendingMessage( String user, String buildMsg )
        {
            if ( !pendingMessages.containsKey( user ) || pendingMessages.get( user ) == null )
            {
                pendingMessages.put( user, new ArrayList<String>() );
            }
            List<String> msgs = pendingMessages.get( user );
            msgs.add( buildMsg );
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
