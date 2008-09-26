package org.codehaus.plexus.components.mercury;

/*
 * Copyright 2001-2007 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @plexus.component
 * 
 * @author Oleg Gusakov
 * 
 */
public class DefaultPlexusMercury
extends AbstractLogEnabled
implements PlexusMercury, Initializable
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( DefaultPlexusMercury.class ); 
  private static final Language _lang = new DefaultLanguage( DefaultPlexusMercury.class );

  //---------------------------------------------------------------
  public void initialize()
  throws InitializationException
  {
  }

  //---------------------------------------------------------------
  public RemoteRepositoryM2 constructRemoteRepositoryM2(
                        String id
                      , URL serverUrl, String serverUser, String serverPass 
                      , URL proxyUrl,  String proxyUser,  String proxyPass
                      , Set<StreamObserverFactory> readerStreamObservers
                      , Set<StreamVerifierFactory> readerStreamVerifiers
                      , Set<StreamObserverFactory> writerStreamObservers
                      , Set<StreamVerifierFactory> writerStreamVerifiers
                                                       )
  throws RepositoryException
  {
    Server server = new Server( id, serverUrl );
    
    server.setReaderStreamObserverFactories( readerStreamObservers );
    server.setReaderStreamVerifierFactories( readerStreamVerifiers );
    server.setWriterStreamObserverFactories( writerStreamObservers );
    server.setWriterStreamVerifierFactories( writerStreamVerifiers );
    
    if( serverUser != null )
    {
      Credentials cred = new Credentials( serverUser, serverPass );
      server.setServerCredentials( cred );
    }
    
    if( proxyUrl != null )
    {
      server.setProxy( proxyUrl );
      
      if( proxyUser != null )
      {
        Credentials cred = new Credentials( proxyUser, proxyPass );
        server.setProxyCredentials( cred );
      }
    }

    RemoteRepositoryM2 repo = new RemoteRepositoryM2( id, server );

    return repo;
  }

  //---------------------------------------------------------------
  public LocalRepositoryM2 constructLocalRepositoryM2(
      String id,
      File rootDir,
      Set<StreamObserverFactory> readerStreamObservers,
      Set<StreamVerifierFactory> readerStreamVerifiers,
      Set<StreamObserverFactory> writerStreamObservers,
      Set<StreamVerifierFactory> writerStreamVerifiers )
      throws RepositoryException
  {
    Server server;
    try
    {
      server = new Server( id, rootDir.toURL() );
    }
    catch( MalformedURLException e )
    {
      throw new RepositoryException(e);
    }
    
    server.setReaderStreamObserverFactories( readerStreamObservers );
    server.setReaderStreamVerifierFactories( readerStreamVerifiers );
    server.setWriterStreamObserverFactories( writerStreamObservers );
    server.setWriterStreamVerifierFactories( writerStreamVerifiers );

    LocalRepositoryM2 repo = new LocalRepositoryM2( server );

    return repo;
  }

  //---------------------------------------------------------------
  public void write( Repository repo, Artifact... artifacts )
  throws RepositoryException
  {
    if( repo == null )
      throw new RepositoryException( _lang.getMessage( "null.repo" ) );
    
    RepositoryWriter wr = repo.getWriter();
    
    wr.writeArtifact( Arrays.asList( artifacts ) );
    
  }
  //---------------------------------------------------------------
  public Collection<Artifact> read( Repository repo, ArtifactBasicMetadata... artifacts )
  throws RepositoryException
  {
    if( repo == null )
      throw new RepositoryException( _lang.getMessage( "null.repo" ) );
    
    RepositoryReader rr = repo.getReader( new DependencyProcessor() {

      public List<ArtifactBasicMetadata> getDependencies(
          ArtifactBasicMetadata bmd,
          MetadataReader mdReader,
          Hashtable env )
          throws MetadataReaderException
      {
        return null;
      }} );
    
    ArtifactResults ar = rr.readArtifacts( Arrays.asList( artifacts ) );
    if( ar.hasExceptions() )
      throw new RepositoryException( ar.getExceptions().toString() );
    
    if( !ar.hasResults() )
      return null;
    
    Map<ArtifactBasicMetadata, List<Artifact>> am = ar.getResults();
    
    List<Artifact> al = new ArrayList<Artifact>();
    for( Map.Entry<ArtifactBasicMetadata, List<Artifact>> e : am.entrySet() )
      al.addAll( e.getValue() );

    return al;
    
  }
  //---------------------------------------------------------------
  public PgpStreamVerifierFactory createPgpReaderFactory(
      boolean lenient,
      boolean sufficient,
      InputStream pubRing )
  throws StreamVerifierException
  {
    return new PgpStreamVerifierFactory(
        new StreamVerifierAttributes(PgpStreamVerifierFactory.DEFAULT_EXTENSION,lenient,sufficient )
        , pubRing
                                      );
  }

  //---------------------------------------------------------------
  public PgpStreamVerifierFactory createPgpWriterFactory(
      boolean lenient,
      boolean sufficient,
      InputStream secRing,
      String keyId,
      String keyPass )
      throws StreamVerifierException
  {
    return new PgpStreamVerifierFactory(
        new StreamVerifierAttributes(PgpStreamVerifierFactory.DEFAULT_EXTENSION,lenient,sufficient )
        , secRing , keyId, keyPass
                                      );
  }
  //---------------------------------------------------------------
  //---------------------------------------------------------------
}
