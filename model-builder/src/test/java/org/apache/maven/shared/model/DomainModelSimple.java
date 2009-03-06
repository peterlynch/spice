package org.apache.maven.shared.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DomainModelSimple implements InputStreamDomainModel {

	private List<ModelProperty> modelProperties;
	
	public DomainModelSimple(List<ModelProperty> modelProperties)
	{
		this.modelProperties = modelProperties;
	}
	
	public String getEventHistory() {
		return "";
	}

	public List<ModelProperty> getModelProperties() throws IOException {

		return modelProperties;
	}

	public boolean isMostSpecialized() {
		return false;
	}

	public void setEventHistory(String history) {

		
	}

	public void setMostSpecialized(boolean isMostSpecialized) {
		
	}

	public InputStream getInputStream() {
		return null;
	}

}
