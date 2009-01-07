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
package org.sonatype.appbooter.ctl;

public final class ControllerVocabulary
{

    public static final byte SHUTDOWN_SERVICE = 0x1;
    public static final byte STOP_SERVICE = 0x2;
    public static final byte START_SERVICE = 0x3;
    public static final byte SHUTDOWN_ON_CLOSE = 0x4;
    public static final byte DETACH_ON_CLOSE = 0x5;

    private ControllerVocabulary(){}

}
