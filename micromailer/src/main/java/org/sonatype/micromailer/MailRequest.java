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
package org.sonatype.micromailer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;

public class MailRequest
{
    private String requestId;

    private String mailTypeId;

    private boolean storeable;

    private String encoding = "UTF-8";

    private Map<String, String> customHeaders;

    private Map<String, Object> bodyContext;

    private Address sender;

    private Address from;

    private Address replyTo;

    private Date sentDate;

    private List<Address> toAddresses;

    private List<Address> bccAddresses;

    private List<Address> ccAddresses;

    private Map<String, DataSource> attachmentMap;

    private String expandedSubject;

    private String expandedBody;

    private MimeMessage mimeMessage;

    public MailRequest( String id, String mailTypeId )
    {
        super();

        this.requestId = id;

        this.mailTypeId = mailTypeId;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId( String requestId )
    {
        this.requestId = requestId;
    }

    public String getMailTypeId()
    {
        return mailTypeId;
    }

    public void setMailTypeId( String mailTypeId )
    {
        this.mailTypeId = mailTypeId;
    }

    public boolean isStoreable()
    {
        return storeable;
    }

    public void setStoreable( boolean storeable )
    {
        this.storeable = storeable;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public Map<String, String> getCustomHeaders()
    {
        if ( customHeaders == null )
        {
            customHeaders = new HashMap<String, String>();
        }

        return customHeaders;
    }

    public void setCustomHeaders( Map<String, String> customHeaders )
    {
        this.customHeaders = customHeaders;
    }

    public Map<String, Object> getBodyContext()
    {
        if ( bodyContext == null )
        {
            bodyContext = new HashMap<String, Object>();
        }

        return bodyContext;
    }

    public void setBodyContext( Map<String, Object> bodyContext )
    {
        this.bodyContext = bodyContext;
    }

    public Address getSender()
    {
        return sender;
    }

    public void setSender( Address sender )
    {
        this.sender = sender;
    }

    public Address getFrom()
    {
        return from;
    }

    public void setFrom( Address from )
    {
        this.from = from;
    }

    public Address getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo( Address replyTo )
    {
        this.replyTo = replyTo;
    }

    public Date getSentDate()
    {
        return sentDate;
    }

    public void setSentDate( Date sentDate )
    {
        this.sentDate = sentDate;
    }

    public Map<String, DataSource> getAttachmentMap()
    {
        if ( attachmentMap == null )
        {
            attachmentMap = new HashMap<String, DataSource>();
        }

        return attachmentMap;
    }

    public void setAttachmentMap( Map<String, DataSource> attachmentMap )
    {
        this.attachmentMap = attachmentMap;
    }

    public List<Address> getToAddresses()
    {
        if ( toAddresses == null )
        {
            toAddresses = new ArrayList<Address>();
        }

        return toAddresses;
    }

    public void setToAddresses( List<Address> toAddresses )
    {
        this.toAddresses = toAddresses;
    }

    public List<Address> getBccAddresses()
    {
        if ( bccAddresses == null )
        {
            bccAddresses = new ArrayList<Address>();
        }

        return bccAddresses;
    }

    public void setBccAddresses( List<Address> bccAddresses )
    {
        this.bccAddresses = bccAddresses;
    }

    public List<Address> getCcAddresses()
    {
        if ( ccAddresses == null )
        {
            ccAddresses = new ArrayList<Address>();
        }
        
        return ccAddresses;
    }

    public void setCcAddresses( List<Address> ccAddresses )
    {
        this.ccAddresses = ccAddresses;
    }

    public String getExpandedSubject()
    {
        return expandedSubject;
    }

    public void setExpandedSubject( String expandedSubject )
    {
        this.expandedSubject = expandedSubject;
    }

    public String getExpandedBody()
    {
        return expandedBody;
    }

    public void setExpandedBody( String expandedBody )
    {
        this.expandedBody = expandedBody;
    }

    public MimeMessage getMimeMessage()
    {
        return mimeMessage;
    }

    public void setMimeMessage( MimeMessage mimeMessage )
    {
        this.mimeMessage = mimeMessage;
    }

}
