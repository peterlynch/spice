package org.apache.maven.shared.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class DomainModelWrapper implements DomainModel {

    private JXPathContext context;	
    
    private DomainModel domainModel;
	
	public DomainModelWrapper(InputStream inputStream) {
        context = JXPathContext.newContext( inputStream );
	}
	
	public DomainModelWrapper(DomainModel domainModel) throws IOException, XmlPullParserException {
		this.domainModel = domainModel;
		/*
		byte[] inputBytes = 
			ModelMarshaller.unmarshalModelPropertiesToXml(domainModel.getModelProperties(), "http://test").getBytes() ;
        byte[] copy = new byte[inputBytes.length];
        System.arraycopy( inputBytes, 0, copy, 0, inputBytes.length );
        new ByteArrayInputStream( copy ) 
        */
        context = JXPathContext.newContext(  
        		Xpp3DomBuilder.build( new StringReader( ModelMarshaller.unmarshalModelPropertiesToXml(domainModel.getModelProperties(), "http://test") ) ) );
	}
	
	public DomainModel getDomainModel()
	{
		return domainModel;
	}
	
    public Object getValue( String expression )
    {
        try
        {
            return context.getValue( expression );
        }
        catch ( JXPathNotFoundException e )
        {
        	e.printStackTrace();
            return null;
        }
    }

	public String getEventHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ModelProperty> getModelProperties() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isMostSpecialized() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEventHistory(String history) {
		// TODO Auto-generated method stub
		
	}

	public void setMostSpecialized(boolean isMostSpecialized) {
		// TODO Auto-generated method stub
		
	}	

}
