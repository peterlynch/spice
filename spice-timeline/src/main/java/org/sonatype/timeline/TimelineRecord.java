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
package org.sonatype.timeline;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used internally as base "record" that carries user data.
 * 
 * @author cstamas
 */
public class TimelineRecord
{
    private final long timestamp;

    private final String type;

    private final String subType;

    private final Map<String, String> data;

    public TimelineRecord( final long timestamp, final String type, final String subType, final Map<String, String> data )
    {
        this.timestamp = timestamp;

        this.type = type == null ? "" : type;

        this.subType = subType == null ? "" : subType;

        this.data = data == null ? new HashMap<String, String>() : data;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getType()
    {
        return type;
    }

    public String getSubType()
    {
        return subType;
    }

    public Map<String, String> getData()
    {
        return data;
    }
}
