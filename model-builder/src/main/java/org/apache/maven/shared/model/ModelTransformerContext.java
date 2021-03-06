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

import org.apache.maven.shared.model.impl.DefaultModelDataSource;

import java.io.IOException;
import java.util.*;


/**
 * Primary context for this package. Provides methods for doing transforms.
 */
public final class ModelTransformerContext
{
    /**
     * Factories to use for construction of model containers
     */
    private Collection<? extends ModelContainerInfo> infos;

    /**
     * List of system and environmental properties to use during interpolation
     */
    private final static List<InterpolatorProperty> systemInterpolatorProperties =
        new ArrayList<InterpolatorProperty>();

    private final static List<InterpolatorProperty> environmentInterpolatorProperties =
        new ArrayList<InterpolatorProperty>();

    static
    {
        for ( Map.Entry<Object, Object> e : System.getProperties().entrySet() )
        {
            systemInterpolatorProperties.add(
                new InterpolatorProperty( "${" + e.getKey() + "}", (String) e.getValue() ) );
        }

        for ( Map.Entry<String, String> e : System.getenv().entrySet() )
        {
            environmentInterpolatorProperties.add( new InterpolatorProperty( "${env." + e.getKey() + "}", e.getValue() ) );
        }
    }


    protected ModelTransformerContext()
    {
         this(null);
    }

    /**
     * Default constructor
     *
     */
    public ModelTransformerContext( Collection<? extends ModelContainerInfo> infos )
    {
        if ( infos == null )
        {
            this.infos = Collections.emptyList();
        }
        else
        {
            this.infos = infos;
        }
    }

    public static List<InterpolatorProperty> createInterpolatorProperties(List<ModelProperty> modelProperties,
                                                                      String baseUriForModel,
                                                                      Map<String, String> aliases,
                                                                      String interpolatorTag,
                                                                      boolean includeSystemProperties,
                                                                      boolean includeEnvironmentProperties)
    {
        if(modelProperties == null)
        {
            throw new IllegalArgumentException("modelProperties: null");
        }

        if(baseUriForModel == null)
        {
            throw new IllegalArgumentException( "baseUriForModel: null");
        }

        List<InterpolatorProperty> interpolatorProperties
                = new ArrayList<InterpolatorProperty>( );

        if( includeSystemProperties )
        {
            interpolatorProperties.addAll( systemInterpolatorProperties );
        }

        if( includeEnvironmentProperties )
        {
            interpolatorProperties.addAll( environmentInterpolatorProperties );
        }

        for ( ModelProperty mp : modelProperties )
        {
            InterpolatorProperty ip = mp.asInterpolatorProperty( baseUriForModel );
            if ( ip != null )
            {   ip.setTag( interpolatorTag );
                interpolatorProperties.add( ip );
                for ( Map.Entry<String, String> a : aliases.entrySet() )
                {
                    interpolatorProperties.add( new InterpolatorProperty(
                            ip.getKey().replaceAll( a.getKey(), a.getValue()),
                            ip.getValue().replaceAll( a.getKey(), a.getValue()),
                            interpolatorTag) );
                }
            }
        }
        
        List<InterpolatorProperty> ips = new ArrayList<InterpolatorProperty>();
        for(InterpolatorProperty ip : interpolatorProperties) {
            if(!ips.contains(ip)) {
                ips.add(ip);
            }
        }
        return ips;
    }

    public static void interpolateModelProperties(List<ModelProperty> modelProperties, 
                                                  List<InterpolatorProperty> interpolatorProperties )
    {
        if( modelProperties == null )
        {
            throw new IllegalArgumentException("modelProperties: null");
        }

        if( interpolatorProperties == null )
        {
            throw new IllegalArgumentException("interpolatorProperties: null");
        }

        List<ModelProperty> unresolvedProperties = new ArrayList<ModelProperty>();
        for ( ModelProperty mp : modelProperties )
        {
            if ( !mp.isResolved() )
            {
                unresolvedProperties.add( mp );
            }
        }

        LinkedHashSet<InterpolatorProperty> ips = new LinkedHashSet<InterpolatorProperty>();
        ips.addAll(interpolatorProperties);
        boolean continueInterpolation = true;
        while(continueInterpolation)
        {
            continueInterpolation = false;
            for ( InterpolatorProperty ip : ips)
            {
                for ( ModelProperty mp : unresolvedProperties )
                {
                    if(mp.resolveWith(ip) && !continueInterpolation )
                    {
                        continueInterpolation = true;
                    }
                }
            }
        }
    }


    /**
     * Transforms the specified model properties using the specified transformers.
     *
     * @param modelProperties
     * @param modelPropertyTransformers
     * @return transformed model properties
     */
    public static List<ModelProperty> transformModelProperties(List<ModelProperty> modelProperties,
                                                        List<? extends ModelPropertyTransformer> modelPropertyTransformers)
    {
        if(modelProperties == null) {
            throw new IllegalArgumentException("modelProperties: null");
        }
        if(modelPropertyTransformers == null) {
            throw new IllegalArgumentException("modelPropertyTransformers: null");
        }
        
        List<ModelProperty> properties = new ArrayList<ModelProperty>(modelProperties);
        List<ModelPropertyTransformer> transformers = new ArrayList<ModelPropertyTransformer>(modelPropertyTransformers);
        for(ModelPropertyTransformer mpt : transformers) {
            properties = sort(mpt.transform(sort(properties, mpt.getBaseUri())), mpt.getBaseUri());
            if(transformers.indexOf(mpt) == transformers.size() - 1) {
                properties = sort(sort(properties, mpt.getBaseUri()), mpt.getBaseUri());
            }
        }

        return properties;
    }

    /**
     * Transforms and interpolates specified hierarchical list of domain models (inheritence) to target domain model.
     * Unlike ModelTransformerContext#transform(java.util.List, ModelTransformer, ModelTransformer), this method requires
     * the user to add interpolator properties. It's intended to be used by IDEs.
     *
     * @param domainModels           the domain model list to transform
     * @param fromModelTransformer   transformer that transforms from specified domain models to canonical data model
     * @param toModelTransformer     transformer that transforms from canonical data model to returned domain model
     * @param importModels
     * @param interpolatorProperties properties to use during interpolation. @return processed domain model
     * @throws IOException if there was a problem with the transform
     */
    public DomainModel transform(List<? extends DomainModel> domainModels,
                                 ModelTransformer fromModelTransformer,
                                 ModelTransformer toModelTransformer,
                                 Collection<? extends ImportModel> importModels,
                                 List<InterpolatorProperty> interpolatorProperties,
                                 List<? extends ModelEventListener> eventListeners)
        throws IOException
    {

        if( eventListeners == null )
        {
            eventListeners = new ArrayList<ModelEventListener>();
        }
        else
        {
            eventListeners = new ArrayList<ModelEventListener>(eventListeners);
        }

        if(interpolatorProperties == null) 
        {
            interpolatorProperties = new ArrayList<InterpolatorProperty>();
        }


        List<ModelProperty> transformedProperties =
                importModelProperties(importModels, fromModelTransformer.transformToModelProperties( domainModels));
        transformedProperties = fromModelTransformer.preprocessModelProperties(transformedProperties);

        String baseUriForModel = fromModelTransformer.getBaseUri();
        List<ModelProperty> modelProperties =
            sort( transformedProperties, baseUriForModel );

        modelProperties = determineLeafNodes(modelProperties);

        Collection<ModelContainerFactory> factories = new ArrayList<ModelContainerFactory>();
        for(ModelContainerInfo info : infos)
        {
            factories.addAll(info.getAllModelContainerFactories());
        }

        ModelDataSource modelDataSource = new DefaultModelDataSource( modelProperties, factories );

        for(ModelContainerInfo info : infos)
        {
            processContainers( modelDataSource, null, info );
        }

        List<ModelProperty> mps = modelDataSource.getModelProperties();

        mps = sort( mps, baseUriForModel );
        DomainModel specialized = null;
        for( DomainModel domainModel : domainModels)
        {
        	if(domainModel.isMostSpecialized())
        	{
        		specialized = domainModel;
        		break;
        	}
        }
        fromModelTransformer.interpolateModelProperties( mps, interpolatorProperties, specialized );

        try
        {
            DomainModel domainModel = toModelTransformer.transformToDomainModel( mps, eventListeners );
            domainModel.setEventHistory(modelDataSource.getEventHistory());
            return domainModel;
        }
        catch ( IOException e )
        {
            System.out.println( modelDataSource.getEventHistory() );
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    private void processChildContainers( ModelDataSource modelDataSource, ModelDataSource childModelDataSource, ModelContainerInfo info )
        throws IOException {
            ModelContainerFactory factory = info.getModelContainerFactory();
            for ( String uri : factory.getUris() )
            {
                List<ModelContainer> modelContainers;
                try
                {
                    modelContainers = childModelDataSource.queryFor( uri );
                }
                catch ( IllegalArgumentException e )
                {
                    System.out.println( modelDataSource.getEventHistory() );
                    throw new IllegalArgumentException( e );
                }
                List<ModelContainer> removedModelContainers = new ArrayList<ModelContainer>();
                Collections.reverse( modelContainers );
                for ( int i = 0; i < modelContainers.size(); i++ )
                {
                    ModelContainer mcA = modelContainers.get( i );
                    if ( removedModelContainers.contains( mcA ) )
                    {
                        continue;
                    }
                    for ( ModelContainer mcB : modelContainers.subList( i + 1, modelContainers.size() ) )
                    {
                        ModelContainerAction action = mcA.containerAction( mcB );

                        if ( ModelContainerAction.DELETE.equals( action ) )
                        {
                            modelDataSource.delete( mcB );
                            removedModelContainers.add( mcB );
                        }
                        else if ( ModelContainerAction.JOIN.equals( action ) )
                        {
                            try
                            {   
                                /*
                                if(childModelDataSource != null)
                                {
                                    mcA = modelDataSource.replace(mcA, mcA.createNewInstance(childModelDataSource.getModelProperties()));
                                }
                                else {
                                    ;
                                }
                                */
                                mcA = modelDataSource.join( mcA, mcB );
                                removedModelContainers.add( mcB );

                                if(info.getChildren() != null)
                                {
                                    for(ModelContainerInfo child : info.getChildren())
                                    {
                                        ModelDataSource childDataSource =
                                                new DefaultModelDataSource(mcA.getProperties(), child.getAllModelContainerFactories());
                                        processChildContainers( modelDataSource, childDataSource, child );
                                        if(child.getModelContainerRule() != null)
                                        {
                                            mcA = modelDataSource.replace(mcA,
                                                    mcA.createNewInstance(child.getModelContainerRule()
                                                            .execute(childDataSource.getModelProperties())));
                                        }
                                    }
                                }
                            }
                            catch ( DataSourceException e )
                            {
                                System.out.println( modelDataSource.getEventHistory() );
                                e.printStackTrace();
                                throw new IOException( "Failed to join model containers: URI = " + uri +
                                    ", Factory = " + factory.getClass().getName() );
                            }
                        }
                    }
                }
            }
    }

    private void processContainers( ModelDataSource modelDataSource, ModelDataSource childModelDataSource, ModelContainerInfo info )
        throws IOException {
            ModelContainerFactory factory = info.getModelContainerFactory();
            for ( String uri : factory.getUris() )
            {
                List<ModelContainer> modelContainers;
                try
                {
                    modelContainers = modelDataSource.queryFor( uri );
                }
                catch ( IllegalArgumentException e )
                {
                    System.out.println( modelDataSource.getEventHistory() );
                    throw new IllegalArgumentException( e );
                }
                List<ModelContainer> removedModelContainers = new ArrayList<ModelContainer>();
                Collections.reverse( modelContainers );
                for ( int i = 0; i < modelContainers.size(); i++ )
                {
                    ModelContainer mcA = modelContainers.get( i );
                    if ( removedModelContainers.contains( mcA ) )
                    {
                        continue;
                    }
                    for ( ModelContainer mcB : modelContainers.subList( i + 1, modelContainers.size() ) )
                    {
                        ModelContainerAction action = mcA.containerAction( mcB );

                        if ( ModelContainerAction.DELETE.equals( action ) )
                        {
                            modelDataSource.delete( mcB );
                            removedModelContainers.add( mcB );
                        }
                        else if ( ModelContainerAction.JOIN.equals( action ) )
                        {
                            try
                            {   /*
                                if(childModelDataSource != null)
                                {
                                    mcA = modelDataSource.replace(mcA, mcA.createNewInstance(childModelDataSource.getModelProperties()));
                                }
                                else {
                                    ;
                                }
                                */
                                mcA = modelDataSource.join( mcA, mcB );
                                removedModelContainers.add( mcB );

                                if(info.getChildren() != null)
                                {
                                    for(ModelContainerInfo child : info.getChildren())
                                    {
                                        ModelDataSource childDataSource =
                                                new DefaultModelDataSource(mcA.getProperties(), child.getAllModelContainerFactories());
                                        processChildContainers( modelDataSource, childDataSource, child );
                                        //mcA = modelDataSource.replace(mcA, mcA.createNewInstance(childDataSource.getModelProperties()));
                                    }
                                }
                            }
                            catch ( DataSourceException e )
                            {
                                System.out.println( modelDataSource.getEventHistory() );
                                e.printStackTrace();
                                throw new IOException( "Failed to join model containers: URI = " + uri +
                                    ", Factory = " + factory.getClass().getName() );
                            }
                        }
                    }
                }
            }
    }

    /**
     * Transforms and interpolates specified hierarchical list of domain models (inheritence) to target domain model.
     * Uses standard environmental and system properties for intepolation.
     *
     * @param domainModels         the domain model list to transform
     * @param fromModelTransformer transformer that transforms from specified domain models to canonical data model
     * @param toModelTransformer   transformer that transforms from canonical data model to returned domain model
     * @return processed domain model
     * @throws IOException if there was a problem with the transform
     */
    public DomainModel transform( List<? extends DomainModel> domainModels, ModelTransformer fromModelTransformer,
                                  ModelTransformer toModelTransformer )
        throws IOException
    {
        return this.transform( domainModels, fromModelTransformer, toModelTransformer, null, systemInterpolatorProperties, null );
    }

    private static List<ModelProperty> importModelProperties(Collection<? extends ImportModel> importModels,
                                                             List<ModelProperty> modelProperties) {
        List<ModelProperty> properties = new ArrayList<ModelProperty>();
        for(ModelProperty mp: modelProperties) {
            if(mp.getUri().endsWith("importModel")) {
                for(ImportModel im : importModels) {
                    if(im.getId().equals(mp.getResolvedValue())) {
                        properties.addAll(im.getModelProperties());
                    }
                }
            } else {
                properties.add(mp);
            }
        }
        return properties;
    }

    /**
     * Sorts specified list of model properties. Typically the list contain property information from the entire
     * hierarchy of models, with most specialized model first in the list.
     * <p/>
     * Define Sorting Rules: Sorting also removes duplicate values (same URI) unless the value contains a parent with
     * a #collection (http://apache.org/model/project/dependencyManagement/dependencies#collection/dependency)
     *
     * @param properties unsorted list of model properties. List may not be null.
     * @param baseUri    the base URI of every model property
     * @return sorted list of model properties
     */
    public static List<ModelProperty> sort( List<ModelProperty> properties, String baseUri )
    {

        if ( properties == null )
        {
            throw new IllegalArgumentException( "properties" );
        }
        LinkedList<ModelProperty> processedProperties = new LinkedList<ModelProperty>();
        List<String> position = new ArrayList<String>();
        boolean projectIsContained = false;

        for ( ModelProperty p : properties )
        {
            String uri = p.getUri();
            String parentUri = uri.substring( 0, uri.lastIndexOf( "/" ) );

            if ( !projectIsContained && uri.equals( baseUri ) )
            {
                projectIsContained = true;
                processedProperties.add( p );
                position.add( 0, uri );
            }
            else if ( !position.contains( uri ) || parentUri.contains( "#collection" ) || parentUri.contains( "#set" )  )
            {
                int pst = (parentUri.endsWith("#property"))
                        ? (position.indexOf( parentUri.replaceAll("#property", "") ) + 1) : (position.indexOf( parentUri ) + 1);

                if(pst == 0 && !uri.equals(properties.get(0).getUri()) )
                {
                    for(ModelProperty mp : properties)
                    {
                        System.out.println(mp);
                    }
                    throw new IllegalArgumentException("Could not locate parent: Parent URI = " + parentUri
                            + ": Child - " + p.toString());
                }

                processedProperties.add( pst, p );
                position.add( pst, uri );
            }
        }
        return processedProperties;
    }

    private static List<ModelProperty> determineLeafNodes(List<ModelProperty> modelProperties)
    {
        List<ModelProperty> mps = new ArrayList<ModelProperty>();
        for(ModelProperty mp : modelProperties)
        {
            if(mp.getResolvedValue() != null && mp.getResolvedValue().trim().equals("") && isLeafNode( mp, modelProperties) )
            {
                mps.add( new ModelProperty(mp.getUri(), null) );
            }
            else
            {
                mps.add(mp);
            }
        }
        return mps;
    }

    private static boolean isLeafNode(ModelProperty modelProperty, List<ModelProperty> modelProperties)
    {
        for(int i = modelProperties.indexOf(modelProperty); i < modelProperties.size() - 1 ; i++)
        {
            ModelProperty peekProperty = modelProperties.get( i + 1 );
            if(modelProperty.isParentOf( peekProperty ) && !peekProperty.getUri().contains( "#property") )
            {
                return true;
            }
            else if(!modelProperty.isParentOf( peekProperty ) )
            {
                return modelProperty.getDepth() < peekProperty.getDepth();
            }
        }
        return true;
    }
}
