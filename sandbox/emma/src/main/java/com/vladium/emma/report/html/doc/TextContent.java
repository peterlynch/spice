/* Copyright (C) 2003 Vladimir Roubtsov. All rights reserved.
 * 
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * $Id$
 */
package com.vladium.emma.report.html.doc;

// ----------------------------------------------------------------------------
/**
 * @author Vlad Roubtsov, (C) 2003
 */
public
final class TextContent implements IContent
{
    // public: ................................................................

    public TextContent (final String text)
    {
        m_text = text;
    }   

    public void emit (final HTMLWriter out)
    {
        if (m_text != null)
        {
            out.write (m_text);
        }
    }
    
    // protected: .............................................................

    // package: ...............................................................
    
    // private: ...............................................................
    
    
    private final String m_text;

} // end of class
// ----------------------------------------------------------------------------