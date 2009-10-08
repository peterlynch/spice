/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.JSecurityException;
import org.jsecurity.io.IniResource;
import org.jsecurity.io.ResourceUtils;
import org.jsecurity.mgt.DefaultSecurityManager;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.mgt.SecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.realm.RealmFactory;
import org.jsecurity.util.LifecycleUtils;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * @author Les Hazlewood
 * @author Jeremy Haile
 * @since 0.9
 */
public class IniConfiguration extends TextConfiguration {

    private static final Log log = LogFactory.getLog(IniConfiguration.class);    

    public static final String DEFAULT_INI_RESOURCE_PATH = "classpath:jsecurity.ini";

    public static final String MAIN = "main";

    public static final String SESSION_MODE_PROPERTY_NAME = "sessionMode";

    protected String configUrl;
    protected IniResource iniResource;
    protected boolean ignoreResourceNotFound = false;

    public IniConfiguration() {
    }

    public IniConfiguration(String configBodyOrResourcePath) {
        load(configBodyOrResourcePath);
    }

    public IniConfiguration(String configBodyOrResourcePath, String charsetName) {
        try {
            this.iniResource = new IniResource(configBodyOrResourcePath, charsetName);
            process(this.iniResource);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    protected String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public void init() throws JSecurityException {

        if (configUrl != null) {
            if (ResourceUtils.resourceExists(configUrl)) {
                load(configUrl);
            } else {
                if (ignoreResourceNotFound) {
                    if (log.isDebugEnabled()) {
                        log.debug("JSecurity resource [" + configUrl + "] not found.  Ignoring since " +
                                "'ignoreResourceNotFound' is set to true.");
                    }
                } else {
                    throw new ConfigurationException("JSecurity resource [" + configUrl + "] specified as a 'configUrl' " +
                            "cannot be found.  If you want to fall back on default configuration specified " +
                            "via the 'config' parameter, then set 'ignoreResourceNotFound' to true.");
                }
            }

        } else {
            if (ResourceUtils.resourceExists(DEFAULT_INI_RESOURCE_PATH)) {
                load(DEFAULT_INI_RESOURCE_PATH);
            }
        }

        // Only call super.init() after we try loading from the configUrl first.
        super.init();

        SecurityManager sm = getSecurityManager();
        if (sm == null) {
            //no config specified, use the defaults:
            sm = createDefaultSecurityManager();
            setSecurityManager(sm);
        }
    }

    protected void load(Reader r) throws ConfigurationException {
        try {
            this.iniResource = new IniResource(r);
            process(this.iniResource);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    protected void load(Scanner s) throws ConfigurationException {
        try {
            this.iniResource = new IniResource(s);
            process(this.iniResource);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public void load(String path) throws ConfigurationException {
        try {
            this.iniResource = new IniResource(path);
            process(this.iniResource);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public void load(InputStream is) throws ConfigurationException {
        try {
            this.iniResource = new IniResource(is);
            process(this.iniResource);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    protected void process(IniResource ini) {
        processIni(ini.getSections());
    }

    protected void processIni(Map<String, Map<String, String>> sections) {
        SecurityManager securityManager = createSecurityManager(sections);
        if (securityManager == null) {
            String msg = "A " + SecurityManager.class + " instance must be created at startup.";
            throw new ConfigurationException(msg);
        }
        setSecurityManager(securityManager);

        afterSecurityManagerSet(sections);
    }

    protected SecurityManager createDefaultSecurityManager() {
        return newSecurityManagerInstance();
    }

    protected SecurityManager createSecurityManager(Map<String, Map<String, String>> sections) {
        Map<String, String> mainSection = sections.get(MAIN);
        return createSecurityManagerForSection(mainSection);
    }

    protected RealmSecurityManager newSecurityManagerInstance() {
        return new DefaultSecurityManager();
    }

    @SuppressWarnings({"unchecked"})
    protected SecurityManager createSecurityManagerForSection(Map<String, String> mainSection) {

        Map<String, Object> defaults = new LinkedHashMap<String, Object>();

        RealmSecurityManager securityManager = newSecurityManagerInstance();
        defaults.put("securityManager", securityManager);
        //convenient alias:
        defaults.put("sm", securityManager);
        ReflectionBuilder builder = new ReflectionBuilder(defaults);
        Map<String, Object> objects = builder.buildObjects(mainSection);

        //realms and realm factory might have been created - pull them out first so we can
        //initialize the securityManager:

        List<Realm> realms = new ArrayList<Realm>();

        //iterate over the map entries to pull out the realm factory(s):

        for (Map.Entry<String, Object> entry : objects.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof RealmSecurityManager) {
                securityManager = (RealmSecurityManager) value;
            } else if (value instanceof RealmFactory) {
                RealmFactory factory = (RealmFactory) value;
                LifecycleUtils.init(factory);
                Collection<Realm> factoryRealms = factory.getRealms();
                if (factoryRealms != null && !factoryRealms.isEmpty()) {
                    realms.addAll(factoryRealms);
                }
            } else if (value instanceof Realm) {
                Realm realm = (Realm) value;
                //set the name if null:
                String existingName = realm.getName();
                if (existingName == null || existingName.startsWith(realm.getClass().getName())) {
                    try {
                        builder.applyProperty(realm, "name", name);
                    } catch (Exception ignored) {
                    }
                }
                realms.add(realm);
            }
        }

        //set them on the SecurityManager
        if (!realms.isEmpty()) {
            securityManager.setRealms(realms);
            LifecycleUtils.init(realms);
        }

        return securityManager;
    }

    protected void afterSecurityManagerSet(Map<String, Map<String, String>> sections) {
    }
}
