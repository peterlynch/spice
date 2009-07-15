/* Copyright (C) 2003 Vladimir Roubtsov. All rights reserved.
 * 
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * $Id$
 */
package com.vladium.emma.instr;

import java.io.IOException;

import com.vladium.util.ClassLoaderResolver;
import com.vladium.util.args.IOptsParser;
import com.vladium.util.asserts.$assert;
import com.vladium.emma.Command;
import com.vladium.emma.IAppConstants;
import com.vladium.emma.IAppErrorCodes;
import com.vladium.emma.EMMARuntimeException;

// ----------------------------------------------------------------------------
/**
 * @author Vlad Roubtsov, (C) 2003
 */
public
final class instrCommand extends Command
{
    // public: ................................................................

    public instrCommand (final String usageToolName, final String [] args)
    {
        super (usageToolName, args);
        
        m_outMode = InstrProcessor.OutMode.OUT_MODE_COPY; // default
    }

    public synchronized void run ()
    {
        ClassLoader loader;
        try
        {
            loader = ClassLoaderResolver.getClassLoader ();
        }
        catch (Throwable t)
        {
            loader = getClass ().getClassLoader ();
        }
                
        try
        {
            // process 'args':
            {
                final IOptsParser parser = getOptParser (loader);
                final IOptsParser.IOpts parsedopts = parser.parse (m_args);
                
                final int usageRequestLevel = parsedopts.usageRequestLevel ();
                
                // check if usage is requested before checking args parse errors etc:
                
                if (usageRequestLevel > 0)
                {
                    usageexit (null, parser, usageRequestLevel);
                    return;
                }
                
                final IOptsParser.IOpt [] opts = parsedopts.getOpts ();
                
                if (opts == null) // this means there were args parsing errors
                {
                    parsedopts.error (m_out, STDOUT_WIDTH);
                    usageexit (null, parser, IOptsParser.SHORT_USAGE);
                    return;
                }

                // [assertion: args parsed Ok]
                
                // version flag is handled as a special case:
                
                if (parsedopts.hasArg ("v"))
                {
                    usageexit (null, null, usageRequestLevel);
                    return;
                }
                
                // process parsed args:              
                try
                {
                    for (int o = 0; o < opts.length; ++ o)
                    {
                        final IOptsParser.IOpt opt = opts [o];
                        final String on = opt.getCanonicalName ();
                        
                        if (! processOpt (opt))
                        {
                            if ("ip".equals (on))
                            {
                                m_instrpath = getListOptValue (opt, PATH_DELIMITERS, true);
                            }
                            else if ("d".equals (on))
                            {
                                m_outDirName = opt.getFirstValue ();
                            }
                            else if ("out".equals (on))
                            {
                                m_outFileName = opt.getFirstValue ();
                            }
                            else if ("merge".equals (on))
                            {
                                m_outDataMerge = getOptionalBooleanOptValue (opt) ? Boolean.TRUE : Boolean.FALSE; 
                            }
                            else if ("ix".equals (on))
                            {
                                // note: this allows path delimiter in the pattern list as well
                                m_ixpath = getListOptValue (opt, COMMA_DELIMITERS, true);
                            }
                            else if ("m".equals (on))
                            {
                                final String ov = opt.getFirstValue ();
                                
                                final InstrProcessor.OutMode outMode = InstrProcessor.OutMode.nameToMode (ov);
                                if (outMode == null)
                                {
                                    usageexit ("invalid '" + opts [o].getName () + "' option value: " + ov, parser,
                                        IOptsParser.SHORT_USAGE);
                                    return;
                                }
                                m_outMode = outMode;
                            }
                        }
                    }

                    // process prefixed opts:
                    
                    processCmdPropertyOverrides (parsedopts);

                    // user '-props' file property overrides:
                    
                    if (! processFilePropertyOverrides ()) return;
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (IAppErrorCodes.ARGS_IO_FAILURE, ioe);
                }
                
                // handle cmd line-level defaults:
                {
                    if ($assert.ENABLED) $assert.ASSERT (m_outMode != null, "m_outMode not set");
                    
                    if ((m_outMode != InstrProcessor.OutMode.OUT_MODE_OVERWRITE) && (m_outDirName == null))
                    {
                        usageexit ("output directory must be specified for '" + m_outMode + "' output mode", parser,
                            IOptsParser.SHORT_USAGE);
                        return;
                    }
                }
            }
            
            // run the instrumentor:
            {
                final InstrProcessor processor = InstrProcessor.create ();
                processor.setAppName (IAppConstants.APP_NAME); // for log prefixing
                
                processor.setInstrPath (m_instrpath, true); // TODO: an option to set 'canonical'?
                processor.setInclExclFilter (m_ixpath);
                $assert.ASSERT (m_outMode != null, "m_outMode not set");
                processor.setOutMode (m_outMode);
                processor.setInstrOutDir (m_outDirName);
                processor.setMetaOutFile (m_outFileName);
                processor.setMetaOutMerge (m_outDataMerge);
                processor.setPropertyOverrides (m_propertyOverrides);
                
                processor.run ();
            }
        }
        catch (EMMARuntimeException yre)
        {
            // TODO: see below
            
            exit (true, yre.getMessage (), yre, RC_UNEXPECTED); // does not return
            return;
        }
        catch (Throwable t)
        {
            // TODO: embed: OS/JVM fingerprint, build #, etc
            // TODO: save stack trace in a file and prompt user to send it to ...
            
            exit (true, "unexpected failure: ", t, RC_UNEXPECTED); // does not return
            return;
        }

        exit (false, null, null, RC_OK);
    }    
    
    // protected: .............................................................


    protected String usageArgsMsg ()
    {
        return "[options]";
    }

    // package: ...............................................................
    
    // private: ...............................................................
    
        
    private String [] m_instrpath;
    private String [] m_ixpath;
    private String m_outDirName;
    private String m_outFileName;
    private Boolean m_outDataMerge;
    private InstrProcessor.OutMode m_outMode;
    
} // end of class
// ----------------------------------------------------------------------------