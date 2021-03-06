/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.examples.guice.swing.impl;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.sonatype.inject.Mediator;

@Named
@Singleton
final class WindowMediator
    implements Mediator<String, JPanel, Window>
{
    public void add( final String name, final Provider<JPanel> bean, final Window window )
        throws Exception
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                window.add( name, bean.get() );
            }
        } );
    }

    public void remove( final String name, final Provider<JPanel> bean, final Window window )
        throws Exception
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                window.remove( name );
            }
        } );
    }
}
