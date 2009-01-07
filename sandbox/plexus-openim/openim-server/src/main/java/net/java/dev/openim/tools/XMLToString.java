/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package net.java.dev.openim.tools;

import org.apache.commons.lang.StringUtils;

/**
 * @author AlAg
 */
public class XMLToString {
    
    private String m_elementName;
    private StringBuffer m_buffer;
    private StringBuffer m_innerBuffer;
    
    public XMLToString( String elementName ){
        m_buffer = new StringBuffer();
        m_buffer.append( '<' ).append( elementName );
        m_elementName = elementName;
    }
    
    public void addAttribut( String name, String value ){
        if( name != null && name.length() > 0 && value != null ){
            m_buffer.append( ' ' ).append( name ).append( "='" ).append( value ).append( "'" );
        }
    }
    public void addFilledAttribut( String name, String value ){
        if( name != null && name.length() > 0 && value != null && value.length() > 0 ){
            m_buffer.append( ' ' ).append( name ).append( "='" ).append( value ).append( "'" );
        }
    }
    
    public void addTextNode( String text ){
        if( text != null && text.length() > 0 ){
            if( m_innerBuffer == null ){
                m_innerBuffer = new StringBuffer();
            }
            m_innerBuffer.append( convert( text ) );
        }
    }

    public void addStringElement( String stringElement ){
        if( stringElement != null ){
            if( m_innerBuffer == null ){
                m_innerBuffer = new StringBuffer();
            }
            m_innerBuffer.append( stringElement );
        }
    }
    
    public void addElement( XMLToString xmlToString ){
        if( xmlToString != null  ){
            if( m_innerBuffer == null ){
                m_innerBuffer = new StringBuffer();
            }
            m_innerBuffer.append( xmlToString.toStringBuffer() );
        }
    }
    
    public String toString(){
        return toStringBuffer().toString();
    }

    public StringBuffer toStringBuffer(){
        StringBuffer buffer = new StringBuffer();
        if( m_innerBuffer != null ){
            buffer.append( m_buffer ).append( '>' ).append( m_innerBuffer ).append( "</" ).append( m_elementName ).append( '>' );
        }
        else{
            buffer.append( m_buffer ).append( "/>" );
        }
        return buffer;
    }
    
    
    // -----------------------------------------------------------------------
    // should be optimized...
    private static final String convert( String s ){
        s = StringUtils.replace( s, "&", "&amp;" );
        s = StringUtils.replace( s, "<", "&lt;" );
        s = StringUtils.replace( s, ">", "&gt;" );
        return s;
    }
    
    
    
}

