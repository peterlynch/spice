package org.sonatype.appbooter.bundle.service;

import org.sonatype.appbooter.bundle.ApplicationAppBooter;

public interface BundleService
{
    boolean handles( ApplicationAppBooter application );

    void startManage( ApplicationAppBooter application );

    void stopManage( ApplicationAppBooter application );
}
