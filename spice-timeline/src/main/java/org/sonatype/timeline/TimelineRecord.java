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

public class TimelineRecord
{
    private long timestamp;

    private String type;

    private String subType;

    private Map<String, String> data = new HashMap<String, String>();

    public TimelineRecord()
    {

    }

    public TimelineRecord( long timestamp, String type, String subType, Map<String, String> data )
    {
        this.timestamp = timestamp;

        this.type = type;

        this.subType = subType;

        this.data = data;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( long timestamp )
    {
        this.timestamp = timestamp;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getSubType()
    {
        return subType;
    }

    public void setSubType( String subType )
    {
        this.subType = subType;
    }

    public Map<String, String> getData()
    {
        return data;
    }

    public void setData( Map<String, String> data )
    {
        this.data = data;
    }

}
