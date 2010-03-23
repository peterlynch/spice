package org.sonatype.hudson.plugin;

import org.kohsuke.args4j.Option;

public class Args
{
    @Option( name = "-help", aliases = { "-h" }, usage = "Show help" )
    private boolean help;

    @Option( name = "-list", aliases = { "-l" }, usage = "List the available projects." )
    private boolean list;

    @Option( name = "-project", aliases = { "-p" }, usage = "Show the status for the required project." )
    private String project;

    @Option( name = "-status", aliases = { "-s" }, usage = "Return the irc plugin status" )
    private boolean status;

    public String getProject()
    {
        return project;
    }

    public boolean isHelp()
    {
        return help;
    }

    public boolean isList()
    {
        return list;
    }

    public boolean isStatus()
    {
        return status;
    }

}
