/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.configuration;

public interface PlexusConfiguration
{
    String getName();

    String getValue();

    String getValue( String defaultValue );

    void setValue( String value );

    String[] getAttributeNames();

    String getAttribute( String attributeName );

    String getAttribute( String attributeName, String defaultValue );

    void setAttribute( String name, String value );

    PlexusConfiguration getChild( String childName );

    PlexusConfiguration getChild( String childName, boolean create );

    PlexusConfiguration[] getChildren();

    PlexusConfiguration[] getChildren( String childName );

    int getChildCount();

    PlexusConfiguration getChild( int index );

    void addChild( PlexusConfiguration child );

    PlexusConfiguration addChild( String name, String value );
}
