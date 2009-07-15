/* Copyright (C) 2003 Vladimir Roubtsov. All rights reserved.
 * 
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * $Id$
 */
package com.vladium.emma;

import com.vladium.app.IAppVersion;
import com.vladium.jcd.lib.Types;

// ----------------------------------------------------------------------------
/**
 * @author Vlad Roubtsov, (C) 2003
 */
public
interface IAppConstants extends IAppVersion
{
    // public: ................................................................
    
    String APP_NAME         = "EMMA";
    String APP_NAME_LC      = "emma";
    String APP_COPYRIGHT    = "(C) Vladimir Roubtsov";
    String APP_THROWABLE_BUILD_ID   = "[" + APP_NAME + " v" + APP_VERSION_WITH_BUILD_ID_AND_TAG + "]";
    String APP_USAGE_BUILD_ID   = "[" + APP_NAME + " v" + APP_VERSION + ", build " + APP_BUILD_ID_AND_TAG + "]";
    String APP_VERBOSE_BUILD_ID = "[" + APP_NAME + " v" + APP_VERSION + ", build " + APP_BUILD_ID_AND_TAG + " (" + APP_BUILD_DATE + ")]";
    
    String APP_USAGE_PREFIX = APP_NAME + " usage: ";
    
    String APP_PACKAGE = Types.getClassPackageName (IAppConstants.class);
    
    /*
     * This field needs to be incremented every time data serialization format
     * gets a backwards-incompatible change (which does not necessarily happen
     * during each major/minor app version change).
     */ 
    long DATA_FORMAT_VERSION    = 0x21L;

} // end of interface
// ----------------------------------------------------------------------------