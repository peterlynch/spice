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
package org.codehaus.plexus.logging;

public interface LoggerManager
{
    String ROLE = LoggerManager.class.getName();

    Logger getLoggerForComponent( String role );

    Logger getLoggerForComponent( String role, String hint );

    void returnComponentLogger( String role );

    void returnComponentLogger( String role, String hint );

    int getThreshold();

    void setThreshold( int threshold );

    void setThresholds( int threshold );

    int getActiveLoggerCount();
}