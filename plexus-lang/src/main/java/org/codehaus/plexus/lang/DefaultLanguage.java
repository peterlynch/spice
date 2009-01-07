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
package org.codehaus.plexus.lang;

import java.util.Locale;
import java.util.ResourceBundle;

import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;


/**
 *
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @plexus.component
 *   role="org.codehaus.plexus.lang.Language"
 */
public class DefaultLanguage
implements Language
{	
	private String bundleName;
	private Locale locale;
//	private ResourceBundle rb;
	private DefaultI18N i18n = new DefaultI18N();
	private String error;
	//-------------------------------------------------------------------------------------
	public DefaultLanguage()
	{
	}
	//-------------------------------------------------------------------------------------
	public DefaultLanguage( Class clazz )
	{
    this.bundleName = clazz.getPackage().getName()+"."+DEFAULT_NAME;
    try
    {
      i18n.initialize();
    }
    catch( InitializationException e )
    {
      error = e.getMessage();
    }
	}
	//-------------------------------------------------------------------------------------
	public DefaultLanguage( Class clazz, Locale locale )
	{
		this( clazz );
		this.locale = locale;
//		rb = ResourceBundle.getBundle( clazz.getPackage().getName()+"."+DEFAULT_NAME, locale, clazz.getClassLoader() );
	}
	//-------------------------------------------------------------------------------------
  public String getMessage( String key, String... args )
  {
    
    if( error != null )
      return error;
    
    if( args == null || args.length == 0)
      return i18n.getString( bundleName, locale, key );

    return i18n.format( bundleName, locale, key, args );
  }
	//-------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
}
