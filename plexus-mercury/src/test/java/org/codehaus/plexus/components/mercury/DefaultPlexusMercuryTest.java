package org.codehaus.plexus.components.mercury;

/*
 * The MIT License
 *
 * Copyright (c) 2005, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

/**
 * 
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultPlexusMercuryTest
extends TestCase
{
  PlexusMercury pm;

  RemoteRepositoryM2 remoteRepo;
  LocalRepositoryM2  localRepo;
  
  List<Repository>   repos;
  
  Artifact a;
  
  protected static final String keyId   = "0EDB5D91141BC4F2";

  protected static final String secretKeyFile = "/pgp/secring.gpg";
  protected static final String publicKeyFile = "/pgp/pubring.gpg";
  protected static final String secretKeyPass = "testKey82";
  
//  public static final String SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_URL = "plexus.mercury.test.url";
//  private String remoteServerUrl = System.getProperty( SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_URL, null );
  static String remoteServerUrl = "http://people.apache.org/~ogusakov/repos/test";
  String artifactCoord = "org.apache.maven.mercury:mercury-repo-virtual:1.0.0-alpha-2-SNAPSHOT";

  private File localRepoDir;
  
  public static final String SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_USER = "plexus.mercury.test.user";
  static String remoteServerUser = System.getProperty( SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_USER, "admin" );

  public static final String SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_PASS = "plexus.mercury.test.pass";
  static String remoteServerPass = System.getProperty( SYSTEM_PARAMETER_PLEXUS_MERCURY_TEST_PASS, "admin123" );
  
  PgpStreamVerifierFactory pgpRF;
  PgpStreamVerifierFactory pgpWF;
  
  SHA1VerifierFactory      sha1F;
  HashSet<StreamVerifierFactory> vFacSha1;
  
  //-------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();

    // prep. Artifact
    File artifactBinary = File.createTempFile( "test-repo-writer", "bin" );
    FileUtil.writeRawData( artifactBinary, getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
    
    a = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven.mercury:mercury-core:2.0.9") );
    
    a.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    a.setFile( artifactBinary );
    
    // prep Repository
    pm = new DefaultPlexusMercury();
    
    pgpRF = pm.createPgpReaderFactory( true, true, getClass().getResourceAsStream( publicKeyFile ) );
    pgpWF = pm.createPgpWriterFactory( true, true, getClass().getResourceAsStream( secretKeyFile ), keyId, secretKeyPass );
    
    sha1F = new SHA1VerifierFactory( true, false );
    
    remoteRepo = pm.constructRemoteRepositoryM2( "testRepo"
                        , new URL(remoteServerUrl), remoteServerUser, remoteServerPass
                        , null, null, null
                        , null, FileUtil.vSet( pgpRF, sha1F )
                        , null, FileUtil.vSet( pgpWF, sha1F )
                                        );
    
    localRepoDir = File.createTempFile( "local-", "-repo" );
    localRepoDir.delete();
    localRepoDir.mkdir();
    
    localRepo = new LocalRepositoryM2( "testLocalRepo", localRepoDir );
    
    repos = new ArrayList<Repository>();
    repos.add( localRepo );
    repos.add( remoteRepo );
    
  }
  //-------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    if( remoteServerUrl == null )
      return;
    
    super.tearDown();
  }
  //-------------------------------------------------------------------------------------
  public void notestWrite()
  throws RepositoryException
  {
    pm.write( remoteRepo, a );
  }
  //-------------------------------------------------------------------------------------
  public void testRead()
  throws RepositoryException
  {
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata(artifactCoord);
    
    Collection<Artifact> res = pm.read( repos, bmd );
    
    assertNotNull( res );
    
    assertFalse( res.isEmpty() );
    
    Artifact a = res.toArray( new Artifact[1] )[0];
    
    assertNotNull( a );
    
    File fBin = a.getFile();
    
    assertNotNull( fBin );

    assertTrue( fBin.exists() );
    
    byte [] pomBytes = a.getPomBlob();
    
    assertNotNull( pomBytes );
    
    assertTrue( pomBytes.length > 10 );
  }
  //-------------------------------------------------------------------------------------
  public void testResolve()
  throws Exception
  {
//    Server central = new Server( "central", new URL("http://repo1.maven.org/maven2") );
    Server central = new Server( "central", new URL("http://repository.sonatype.org/content/groups/public") );
    
    repos.add( new RemoteRepositoryM2(central) );

    String artifactId = "asm:asm-xml:3.0";

    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata( artifactId );
    
    List<ArtifactBasicMetadata> res = (List<ArtifactBasicMetadata>)pm.resolve( repos, new MavenDependencyProcessor(), ArtifactScopeEnum.compile, bmd );
    
    System.out.println("Resolved as "+res);

    assertEquals( 4, res.size() );
    
    assertTrue( assertHasArtifact( res, "asm:asm-xml:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-util:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm-tree:3.0" ) );
    assertTrue( assertHasArtifact( res, "asm:asm:3.0" ) );
  }
  //----------------------------------------------------------------------------------------------
  private static boolean assertHasArtifact( List<ArtifactBasicMetadata> res, String gav )
  {
    ArtifactMetadata gavMd = new ArtifactMetadata(gav);
    
    for( ArtifactBasicMetadata md : res )
      if( md.sameGAV( gavMd ) )
        return true;
    
    return false;
  }
  //-------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------
}
