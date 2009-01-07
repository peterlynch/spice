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
package org.sonatype.nexus.applet;


import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;

public class DigestApplet extends Applet {
	
  private static final int BUFFER_SIZE = 0x1000;
  private static final String SHA1 = "SHA1";
  private static final Color BACKGROUND_COLOR = new Color( 242, 242, 242 ); 
  
//  private long totalBytes = 0L;
//  private long currentBytes = 0L;

  public void init() {
    setLayout( new FlowLayout() );
  }
  
  public void paint( Graphics g ) {
    Dimension d = getSize();
//    int w = 0;
//    if ( totalBytes > 0L ) {
//      w = ( int ) ( currentBytes * d.width / totalBytes );
//      g.setColor( Color.BLACK );
//      g.fillRect( 0, 0, w, d.height );
//    }
    g.setColor( BACKGROUND_COLOR );
    g.fillRect( 0, 0, d.width, d.height );
  }

//  public void resetProgress() {
//    totalBytes = 0L;
//    repaint();
//  }

  public String selectFile() {
    return String.valueOf( AccessController.doPrivileged( new PrivilegedAction() {
      public Object run() {
        Frame frame = new Frame();
        FileDialog fd = new FileDialog( frame );
        fd.setVisible( true );
        
        String filename = fd.getFile();
        if ( filename == null ) {
          return "";
        }
        else {
          return fd.getDirectory() + filename;
        }
      }
    } ) );
  }
  
  public String digest( final String filename ) {
    return String.valueOf( AccessController.doPrivileged( new PrivilegedAction() {
      public Object run() {
        FileInputStream in = null;
        try {
//          currentBytes = 0L;
//          totalBytes = new File( filename ).length();
          return readAndDigest( in = new FileInputStream( filename ) );
        }
        catch ( FileNotFoundException fileNotFoundException ) {
          return fileNotFoundException.getMessage();
        }
        catch ( IOException ioException ) {
          ioException.printStackTrace();
          return ioException.getMessage();
        }
        finally {
          if ( in != null ) try {
            in.close();
          }
          catch ( IOException ioException ) {
            ioException.printStackTrace();
            return ioException.getMessage();
          }
        }
      }
    } ) );
  }


  private String readAndDigest( InputStream in ) throws IOException {

  	byte[] bytes = new byte[BUFFER_SIZE];
  	
  	try {
      MessageDigest digest = MessageDigest.getInstance( SHA1 );
      for ( int n; ( n = in.read( bytes ) ) >= 0; ) {
        if ( n > 0 ) {
          digest.update( bytes, 0, n );
//          currentBytes += n;
//          repaint();
        }
      }
      
      bytes = digest.digest();
      StringBuffer sb = new StringBuffer( bytes.length * 2 );
      for ( int i = 0; i < bytes.length; i++ ) {
        int n = bytes[i] & 0xFF;
        if ( n < 0x10 ) {
          sb.append( '0' );
        }
        sb.append( Integer.toHexString( n ) );
      }
      
      return sb.toString();
    }
  	catch ( NoSuchAlgorithmException noSuchAlgorithmException ) {
  	  noSuchAlgorithmException.printStackTrace();
  	  return noSuchAlgorithmException.getMessage();
  	}
  }
}
