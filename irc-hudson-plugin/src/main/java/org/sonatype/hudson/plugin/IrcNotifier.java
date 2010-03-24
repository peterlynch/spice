package org.sonatype.hudson.plugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserCause;
import hudson.plugins.im.bot.DefaultJobProvider;
import hudson.plugins.im.tools.ExceptionHelper;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.stapler.StaplerRequest;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventAdapter;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCUser;

public class IrcNotifier
    extends Notifier
{

    private static final DefaultJobProvider JOB_PROVIDER = new DefaultJobProvider();

    public static class IrcBuildStepDescriptor
        extends BuildStepDescriptor<Publisher>
    {
        private static final Logger LOG = Logger.getLogger( IrcBuildStepDescriptor.class.getName() );

        private List<String> channels;

        private String commandPrefix;

        private transient IRCConnection conn;

        private transient String connectionError;

        private transient Object connectionNotifier = new Object();

        private boolean enabled;

        private String hostname;

        private transient boolean isRegistered;

        private String mediator;

        private String nick;

        private String password;

        private transient Map<String, List<String>> pendingMessages = new LinkedHashMap<String, List<String>>();

        private Integer port;

        private IRCEventListener hudsonListener;

        protected IrcBuildStepDescriptor()
        {
            super( IrcNotifier.class );
            load();

            try
            {
                getIrcConnection();
            }
            catch ( final Exception e )
            {
                // Server temporarily unavailable or misconfigured?
                LOG.warning( ExceptionHelper.dump( e ) );
            }
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
                    commandPrefix = commandPrefix.trim();
                }

                channels = Arrays.asList( req.getParameter( "irc_publisher.channels" ).split( " " ) );
                if ( channels == null || channels.isEmpty() )
                {
                    throw new FormException( "At least one channel must be informed", "irc_publisher.channels" );
                }
                mediator = req.getParameter( "irc_publisher.mediator" );
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

        public String getChannels()
        {
            StringBuilder sb = new StringBuilder();
            for ( String c : channels )
            {
                if ( sb.length() != 0 )
                {
                    sb.append( ' ' );
                }

                sb.append( c );
            }
            return sb.toString();
        }

        public String getCommandPrefix()
        {
            return commandPrefix;
        }

        @Override
        public String getDisplayName()
        {
            return "IRC notification";
        }

        public String getHostname()
        {
            return hostname;
        }

        private void checkPendingMessage( String user )
        {
            synchronized ( pendingMessages )
            {
                List<String> msgs = pendingMessages.get( user );
                if ( msgs != null )
                {
                    for ( String msg : msgs )
                    {
                        LOG.finest( "Notifying user about a pending message " + user + ": " + msg );
                        conn.doPrivmsg( user, msg );
                    }

                    msgs.clear();
                }
            }
        }

        private void addPendingMessage( String user, String buildMsg )
        {
            synchronized ( pendingMessages )
            {
                if ( !pendingMessages.containsKey( user ) || pendingMessages.get( user ) == null )
                {
                    pendingMessages.put( user, new ArrayList<String>() );
                }
                List<String> msgs = pendingMessages.get( user );
                msgs.add( buildMsg );
            }
        }

        private IRCConnection getIrcConnection()
            throws IOException
        {
            if ( enabled && conn == null )
            {
                conn = new IRCConnection( hostname, new int[] { port }, password, nick, nick, nick );
                hudsonListener = new IRCEventAdapter()
                {

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
                            LOG.warning( "Num: " + num + " - Msg: " + msg );
                        }
                    }

                    @Override
                    public void onError( String msg )
                    {
                        onError( -1, msg );
                    }

                    @Override
                    public void onJoin( String chan, IRCUser user )
                    {
                        checkPendingMessage( user.getNick() );
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

                        if ( !msg.startsWith( commandPrefix ) )
                        {
                            return;
                        }

                        String[] args = msg.split( " " );
                        // remove prefix
                        args = Arrays.copyOfRange( args, 1, args.length );

                        String[] answer = getAnswer( args );
                        for ( String a : answer )
                        {
                            conn.doPrivmsg( target, user.getNick() + ": " + a );
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
                        conn.doJoin( "#irc-plugin-debug" );

                        synchronized ( connectionNotifier )
                        {
                            connectionNotifier.notifyAll();
                        }
                    }

                    @Override
                    public void onDisconnected()
                    {
                        try
                        {
                            conn.connect();
                        }
                        catch ( Exception e )
                        {
                            LOG.severe( ExceptionHelper.dump( e ) );
                        }
                    }
                };
                conn.addIRCEventListener( hudsonListener );
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

        public String getMediator()
        {
            return mediator;
        }

        public String getNick()
        {
            return nick;
        }

        public String getPassword()
        {
            return password;
        }

        public Integer getPort()
        {
            return port;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public boolean isApplicable( Class<? extends AbstractProject> jobType )
        {
            return true;
        }

        public boolean isEnabled()
        {
            return enabled;
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

        public void notify( AbstractBuild<?, ?> build )
            throws InterruptedException, IOException
        {
            final IRCConnection conn = getIrcConnection();

            AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();

            boolean isBroken = build.getResult().isWorseThan( Result.SUCCESS );
            boolean isBackFromBroken = build.getResult().isBetterThan( previousBuild.getResult() );

            if ( !isBroken && !isBackFromBroken )
            {
                return;
            }

            Set<String> relatedUsers = getRelatedUsers( build, conn );

            if ( isBroken )
            {
                notifyBroken( build, conn, relatedUsers );
            }

            if ( isBackFromBroken )
            {
                for ( String channel : channels )
                {
                    final String buildMsg =
                        build.getProject().getName() + " build is stable again  ( " + Hudson.getInstance().getRootUrl()
                            + build.getUrl() + " )";

                    conn.doPrivmsg( channel, buildMsg );
                }
            }

        }

        private void notifyBroken( AbstractBuild<?, ?> build, final IRCConnection conn, Set<String> relatedUsers )
        {
            for ( String channel : channels )
            {
                final String buildMsg =
                    build.getProject().getName() + " build is unstable  ( " + Hudson.getInstance().getRootUrl()
                        + build.getUrl() + " )";

                for ( final String user : relatedUsers )
                {
                    notifyUser( conn, channel, buildMsg, user );
                }

                if ( mediator != null )
                {
                    if ( relatedUsers.size() > 1 )
                    {
                        // mediator may need to intervention
                        String mediatorMsg =
                            "Hello " + mediator + ". The following users: " + relatedUsers
                                + " are involded on this problem: " + buildMsg;
                        notifyUser( conn, channel, mediatorMsg, mediator );
                    }

                    if ( relatedUsers.isEmpty() )
                    {
                        String mediatorMsg =
                            "Hello " + mediator + ". Unable to figure out which users are involded on this problem: "
                                + buildMsg;
                        notifyUser( conn, channel, mediatorMsg, mediator );
                    }
                }

            }
        }

        private void notifyUser( final IRCConnection conn, final String channel, final String msg, final String user )
        {
            LOG.finest( "Notifying user " + user + ": " + msg );

            conn.doPrivmsg( channel, user + ": " + msg );
            IRCEventAdapter userListener = new IRCEventAdapter()
            {
                @Override
                public void onError( int num, String errorMsg )
                {
                    if ( num == 401 )
                    {
                        addPendingMessage( user, msg );
                    }
                    conn.removeIRCEventListener( this );

                    if ( mediator != null && !mediator.equals( user ) )
                    {
                        String mediatorMsg =
                            "Hello " + mediator + ". The following user: " + user + " is involded on this problem: "
                                + msg + ". But I was not able to find this user.";
                        notifyUser( conn, channel, mediatorMsg, mediator );
                    }
                }

            };
            conn.addIRCEventListener( userListener );
            conn.doPrivmsg( user, msg );
        }

        public void stop()
        {
            conn.removeIRCEventListener( hudsonListener );
            conn.close();

            hudsonListener = null;
            conn = null;
        }

    }

    @SuppressWarnings( "unchecked" )
    private static String[] getAnswer( String[] args )
    {
        Args parsed = new Args();
        CmdLineParser parser = new CmdLineParser( parsed );
        try
        {
            parser.parseArgument( args );
        }
        catch ( Exception e )
        {
            return new String[] { "Error parsing options: " + Arrays.toString( args ) + ": " + e.getMessage() };
        }

        if ( parsed.isHelp() )
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            parser.printUsage( bos );
            return new String( bos.toByteArray() ).split( "\n" );
        }

        if ( parsed.isStatus() )
        {
            return new String[] { "RUNNING" };
        }

        if ( parsed.isList() )
        {
            Set<String> projectNames = new LinkedHashSet<String>();
            projectNames.add( "Availible projects:" );

            List<AbstractProject> projects = JOB_PROVIDER.getAllJobs();
            for ( AbstractProject project : projects )
            {
                if ( project.getParent() instanceof Hudson )
                {
                    projectNames.add( project.getName() );
                }
            }
            return projectNames.toArray( new String[0] );
        }

        if ( parsed.getProject() != null )
        {
            String name = parsed.getProject();
            List<AbstractProject> projects = JOB_PROVIDER.getAllJobs();
            for ( AbstractProject project : projects )
            {
                if ( name.equals( project.getName() ) )
                {
                    List<String> result = new ArrayList<String>();

                    AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) project.getLastCompletedBuild();
                    result.add( "Project '" + name + "' status: " + build.getBuildStatusSummary().message );
                    result.add( "Build URL: " + Hudson.getInstance().getRootUrl() + build.getUrl() );

                    Set<String> users = getRelatedUsers( build, null );
                    if ( !users.isEmpty() )
                    {
                        result.add( "Related user(s): " );
                        for ( String user : users )
                        {
                            result.add( user );
                        }
                    }

                    ChangeLogSet<? extends Entry> changes = build.getChangeSet();
                    if ( !changes.isEmptySet() )
                    {
                        result.add( "Changes: " );
                        for ( Entry change : changes )
                        {
                            result.add( change.getMsg() );
                        }
                    }

                    return result.toArray( new String[0] );
                }
            }
            return new String[] { "Project '" + name + "' not found!" };
        }

        return new String[] { "Processed options: " + Arrays.toString( args ) };
    }

    private static Set<String> getRelatedUsers( AbstractBuild<?, ?> build, final IRCConnection conn )
    {
        Set<String> relatedUsers = new LinkedHashSet<String>();
        List<Cause> causes = build.getCauses();
        for ( Cause cause : causes )
        {
            relatedUsers.addAll( getRelatedUsers( cause, conn ) );
        }

        ChangeLogSet<? extends Entry> changes = build.getChangeSet();
        for ( Entry change : changes )
        {
            relatedUsers.add( change.getAuthor().getId() );
        }

        relatedUsers.remove( "" );

        return relatedUsers;
    }

    @SuppressWarnings( "unchecked" )
    private static Set<String> getRelatedUsers( Cause cause, IRCConnection conn )
    {
        if ( cause instanceof UserCause )
        {
            UserCause uCause = (UserCause) cause;
            return Collections.singleton( uCause.getUserName() );
        }
        else if ( cause instanceof UpstreamCause )
        {
            List<Cause> upstreamCauses;
            try
            {
                Field f = UpstreamCause.class.getDeclaredField( "upstreamCauses" );
                f.setAccessible( true );
                upstreamCauses = (List<Cause>) f.get( cause );
            }
            catch ( Exception e )
            {
                return Collections.emptySet();
            }

            Set<String> users = new HashSet<String>();
            for ( Cause c : upstreamCauses )
            {
                users.addAll( getRelatedUsers( c, conn ) );
            }

            return users;
        }
        else
        {
            if ( conn != null )
            {
                conn.doPrivmsg( "#irc-plugin-debug", "Unexpected cause type! '" + cause.getClass() + "'" );
            }
        }

        return Collections.emptySet();
    }

    @Extension
    public static final IrcBuildStepDescriptor DESCRIPTOR = new IrcBuildStepDescriptor();

    @Override
    public IrcBuildStepDescriptor getDescriptor()
    {
        return DESCRIPTOR;
    }

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean needsToRunAfterFinalized()
    {
        return true;
    }

    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener )
        throws InterruptedException, IOException
    {
        DESCRIPTOR.notify( build );
        return super.perform( build, launcher, listener );
    }

}
