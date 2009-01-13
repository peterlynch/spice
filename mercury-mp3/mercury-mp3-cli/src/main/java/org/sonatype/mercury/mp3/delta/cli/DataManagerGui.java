/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.sonatype.mercury.mp3.delta.cli;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.TileObserver;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
@SuppressWarnings("serial")
public class DataManagerGui
extends JFrame
{
    private static final Language LANG = new DefaultLanguage( DataManagerGui.class );
    
    DeltaManagerCli _cli;
    
    DataManagerGui _gui = this;
    
    
    JTabbedPane _tabsPane = new JTabbedPane();
    JPanel      _updateTab = new JPanel();
    JPanel      _dirTab = new JPanel();
    
    JPanel [] _tabs = new JPanel [] { _updateTab, _dirTab };
    
    JTextField _mavenHomeField = new JTextField();
    JLabel _mavenHomeLabel = new JLabel();
    
    JComboBox _versionList = new JComboBox();
    
    JButton _updateButton = new JButton( LANG.getMessage( "gui.update.button.title" ));
    JButton _cancelButton = new JButton( LANG.getMessage( "gui.cancel.button.title" ));
    
    public DataManagerGui()
    {
        super( LANG.getMessage( "gui.title" ) );
        
        int count = 0;
        
        Color bg = new Color( 240, 240, 240 );
        
        _tabsPane.setBackground( bg );
        
        for( JPanel p : _tabs )
        {
            String tabTitle = LANG.getMessage( "gui.tab.title."+(count++) );
            
            p.setBackground( bg );
            p.setBorder( new TitledBorder( tabTitle ) );
            
            _tabsPane.addTab( tabTitle, null, p );
        }
        
        setupUpdateTab();
        
        setupDirsTab();
        
        add( "Center", _tabsPane );
        
        
        
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        
        setPreferredSize( new Dimension(450, 300) );
//        setSize( 450, 300 );
        setBackground( bg );
        
    }

    private void setupUpdateTab()
    {
        GridBagLayout gridBagLM = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        _updateTab.setLayout( gridBagLM );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.EAST;
        _updateTab.add( new JLabel( LANG.getMessage( "gui.label.maven.home" ) ), c );
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _updateTab.add( _mavenHomeLabel, c );

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        _updateTab.add( new JLabel( LANG.getMessage( "gui.label.versions" ) ), c );
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _updateTab.add( _versionList, c );
        
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        _updateTab.add( _updateButton, c );

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        _updateTab.add( _cancelButton, c );
        
        _updateButton.addActionListener( 
                         new ActionListener()
                         {
                            public void actionPerformed( ActionEvent e )
                            {
                                _cli.update( _gui );
                            }
                          }
                         );
        
        _cancelButton.addActionListener( 
                         new ActionListener()
                         {
                            public void actionPerformed( ActionEvent e )
                            {
                                _gui.dispose();
                                System.exit( 0 );
                            }
                          }
                         );
    }

    private void setupDirsTab()
    {
        _dirTab.add( _mavenHomeField );
    }

}
