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


package org.sonatype.mercury.mp3.delta.cli;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
    JPanel      _repoTab = new JPanel();
    
    JPanel [] _tabs = new JPanel [] { _updateTab }; //, _repoTab, _dirTab };
    
    JTextField _mavenHomeField = new JTextField();
    JLabel _mavenHomeLabel = new JLabel();
    
    JLabel _currentVersion      = new JLabel();
    
    JTextField _localRepoField = new JTextField();
    
    JComboBox _remoteRepoList = new JComboBox();
    
    JComboBox _cdList = new JComboBox();
    DefaultComboBoxModel _cdModel;
    
    JButton _updateButton = new JButton( LANG.getMessage( "gui.update.button.title" ));
    JButton _cancelButton = new JButton( LANG.getMessage( "gui.cancel.button.title" ));
    
    String _lock;
    
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
        
        setupRepoTab();
        
        add( "Center", _tabsPane );

        setDefaultCloseOperation( EXIT_ON_CLOSE );
        
        setPreferredSize( new Dimension(600, 250) );
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
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        _updateTab.add( _mavenHomeLabel, c );

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.EAST;
        _updateTab.add( new JLabel( LANG.getMessage( "gui.label.current.version" ) ), c );
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        _updateTab.add( _currentVersion, c );

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        _updateTab.add( new JLabel( LANG.getMessage( "gui.label.versions" ) ), c );
        
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _updateTab.add( _cdList, c );
        
        _cdModel = new DefaultComboBoxModel();
        _cdList.setModel( _cdModel );
        _cdList.addActionListener(
                                  new ActionListener()
                                  {
                                    public void actionPerformed( ActionEvent e )
                                    {
                                        String sel = (String) _cdList.getSelectedItem();
                                        
                                        if( sel != null && sel.charAt( 0 ) == '-')
                                        {
                                            int max = _cdModel.getSize();
                                            
                                            int curr = _cdList.getSelectedIndex();
                                            
                                            if( curr == max - 1 )
                                                --curr;
                                            else
                                                ++curr;

                                            if( curr >= 0 )
                                                _cdList.setSelectedIndex(curr);
                                        }
                                    }
                                  } 
                                );

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        _updateTab.add( _cancelButton, c );

        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        _updateTab.add( _updateButton, c );
        
        _updateButton.addActionListener( 
                         new ActionListener()
                         {
                            public void actionPerformed( ActionEvent e )
                            {
                                _cli.resumeYourself();
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
        GridBagLayout gridBagLM = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        _dirTab.setLayout( gridBagLM );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.EAST;
        _dirTab.add( new JLabel( LANG.getMessage( "gui.label.maven.home" ) ), c );
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _dirTab.add( _mavenHomeField, c );
    }

    private void setupRepoTab()
    {
        GridBagLayout gridBagLM = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        _repoTab.setLayout( gridBagLM );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.EAST;
        _repoTab.add( new JLabel( LANG.getMessage( "gui.label.repo.local" ) ), c );
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _repoTab.add( _localRepoField, c );

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        _repoTab.add( new JLabel( LANG.getMessage( "gui.label.repo.remote" ) ), c );
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        _repoTab.add( _remoteRepoList, c );
    }

}
