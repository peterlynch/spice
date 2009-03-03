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
package org.apache.maven.shared.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.*;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ModelMarshallerTest
{

    @Test
    public void unmarshalWithEmptyCollectionTags()
        throws IOException
    {
        List<ModelProperty> modelProperties = Arrays.asList(
            new ModelProperty( "http://apache.org/maven/project", null ),
            new ModelProperty( "http://apache.org/maven/project/dependencies#collection", null ) );
        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void unmarshalWithSingleProperty()
        throws IOException
    {
        List<ModelProperty> modelProperties = Arrays.asList(
            new ModelProperty( "http://apache.org/maven/project", null ),
            new ModelProperty( "http://apache.org/maven/project/modelVersion", "4.0.0" ) );
        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void unmarshalWithEmptyTags111()
        throws IOException
    {
        List<ModelProperty> modelProperties = ModelMarshaller.marshallXmlToModelProperties( new ByteArrayInputStream(
            "<project><S></S><version>1.2</version><developers><developer><organization></organization></developer></developers><modelVersion>4</modelVersion></project>".getBytes( "UTF-8" ) ),
                                                                                            "http://apache.org/maven",
                                                                                            null );

        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void unmarshalWithNullPropertiesOnSameLevel()
        throws IOException
    {
        List<ModelProperty> modelProperties =
            Arrays.asList( new ModelProperty( "http://apache.org/maven/project", null ),
                           new ModelProperty( "http://apache.org/maven/project/name", null ),
                           new ModelProperty( "http://apache.org/maven/project/description", null ) );
        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void unmarshalWithCollection()
        throws IOException
    {
        String src = "<project><goals>" + "<goal>b</goal><goal>a</goal><goal>c</goal>" + "</goals></project>";
        List<ModelProperty> modelProperties =
            ModelMarshaller.marshallXmlToModelProperties(
                                                          toStream( src ),
                                                          "http://apache.org/maven",
                                                          Collections.singleton( "http://apache.org/maven/goals#collection" ) );

        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void unmarshalWithAttributes()
        throws IOException
    {
        String src = "<project groupId='gid' artifactId='aid'><item attrib='value'/></project>";
        List<ModelProperty> modelProperties =
            ModelMarshaller.marshallXmlToModelProperties( toStream( src ), "http://apache.org/maven", null );

        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
        assertTrue( xml.matches( "(?s).*<item\\s+attrib=\\s*\"value\".*" ) );
        assertTrue( xml.matches( "(?s).*<project\\s+groupId=\\s*\"gid\"\\s+artifactId=\\s*\"aid\".*" ) );
    }

    @Test
    public void unmarshalWithContentContainingMarkupCharacters()
        throws IOException
    {
        List<ModelProperty> modelProperties =
            Arrays.asList( new ModelProperty( "http://apache.org/maven/project", null ),
                           new ModelProperty( "http://apache.org/maven/project/name", "<&" ) );
        String xml = ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
        assertWellFormedXml( xml );
    }

    @Test
    public void marshal()
        throws IOException
    {
        List<ModelProperty> modelProperties = ModelMarshaller.marshallXmlToModelProperties(
            new ByteArrayInputStream( "<project><version>1.1</version></project>".getBytes( "UTF-8" ) ),
            "http://apache.org/maven", null );

        assertEquals( 2, modelProperties.size() );
        assertEquals( "http://apache.org/maven/project", modelProperties.get( 0 ).getUri() );
        assertEquals( "http://apache.org/maven/project/version", modelProperties.get( 1 ).getUri() );
        assertEquals( "1.1", modelProperties.get( 1 ).getResolvedValue() );
    }

    /*
    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithBadBaseUri() throws IOException, XmlPullParserException {
        List<ModelProperty> modelProperties = Arrays.asList(
                new ModelProperty("http://apache.org/maven/project", null),
                new ModelProperty("http://apache.org/maven/project/version", "1.1")
        );

        ModelMarshaller.unmarshalModelPropertiesToXml(modelProperties, "http://apache.org");
    }
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithNullBaseUri()
        throws IOException
    {
        List<ModelProperty> modelProperties =
            Arrays.asList( new ModelProperty( "http://apache.org/maven/project", null ) );

        ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, null );
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithEmptyBaseUri()
        throws IOException
    {
        List<ModelProperty> modelProperties =
            Arrays.asList( new ModelProperty( "http://apache.org/maven/project", null ) );

        ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithEmptyModelProperties()
        throws IOException
    {
        ModelMarshaller.unmarshalModelPropertiesToXml( new ArrayList<ModelProperty>(),
                                                       "http://apache.org/maven/project" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithNullModelProperties()
        throws IOException
    {
        ModelMarshaller.unmarshalModelPropertiesToXml( null, "http://apache.org/maven/project" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmarshalWithIncorrectModelPropertyUri()
        throws IOException
    {
        List<ModelProperty> modelProperties = Arrays.asList(
            new ModelProperty( "http://apache.org/maven/project", null ),
            new ModelProperty( "http://bogus.org/maven", "1.1" ) );

        ModelMarshaller.unmarshalModelPropertiesToXml( modelProperties, "http://apache.org/maven" );
    }

    private void assertWellFormedXml( String xml )
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.parse( new InputSource( new StringReader( xml ) ) );
        }
        catch ( SAXException e )
        {
            fail( e + "\n" + xml );
        }
        catch ( IOException e )
        {
            fail( e.toString() );
        }
        catch ( ParserConfigurationException e )
        {
            fail( e.toString() );
        }
    }

    private InputStream toStream( String xml )
    {
        try
        {
            return new ByteArrayInputStream( xml.getBytes( "UTF-8" ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new Error( "Broken JVM", e );
        }
    }

}
