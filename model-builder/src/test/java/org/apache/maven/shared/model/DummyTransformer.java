package org.apache.maven.shared.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DummyTransformer implements ModelTransformer {

	private String baseUri;
	
	public DummyTransformer(String baseUri)
	{
		this.baseUri = baseUri;
	}
	
	public DummyTransformer()
	{
		this.baseUri = "http://test";
	}
	public String getBaseUri() {
		return baseUri;
	}

	public void interpolateModelProperties(List<ModelProperty> modelProperties,
			List<InterpolatorProperty> interpolatorProperties,
			DomainModel domainModel) throws IOException {		
	}

	public List<ModelProperty> preprocessModelProperties(
			List<ModelProperty> modelProperties) {
		return modelProperties;
	}

	public DomainModel transformToDomainModel(List<ModelProperty> properties,
			List<? extends ModelEventListener> eventListeners)
			throws IOException {
		return new DomainModelSimple(properties);
	}

	public List<ModelProperty> transformToModelProperties(
			List<? extends DomainModel> domainModels) throws IOException {
	
		List<ModelProperty> properties = new ArrayList<ModelProperty>();	
		for(DomainModel model : domainModels)
		{
			byte[] inputBytes = 
				ModelMarshaller.unmarshalModelPropertiesToXml(model.getModelProperties(), "http://test").getBytes() ;
	        byte[] copy = new byte[inputBytes.length];
	        System.arraycopy( inputBytes, 0, copy, 0, inputBytes.length );
	                
			properties.addAll(ModelMarshaller.marshallXmlToModelProperties(new ByteArrayInputStream( copy ) , baseUri, URIS));	
		}
		
		return properties;
	}
	
	public static final Set<String> URIS = Collections.unmodifiableSet(new HashSet<String>( Arrays.asList(  "http://test/project/containers#collection")));

}
