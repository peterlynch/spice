package org.sonatype.micromailer;

import java.util.Iterator;

/**
 * Source interface for supplying batch mail requests.
 * 
 * @author cstamas
 */
public interface MailRequestSource
{
    boolean hasWaitingRequests();

    Iterator<MailRequest> getRequestIterator();

    void setMailRequestStatus( MailRequest request, MailRequestStatus status );
}
