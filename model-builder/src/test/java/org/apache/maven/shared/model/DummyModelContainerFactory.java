package org.apache.maven.shared.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DummyModelContainerFactory implements ModelContainerFactory {

	public ModelContainer create(List<ModelProperty> modelProperties) {
		
		return new DummyModelContainer( modelProperties);
	}
	 public static final Set<String> URIS = Collections.unmodifiableSet(new HashSet<String>( Arrays.asList(  "http://test/project/containers#collection")));
	    
	public Collection<String> getUris() {
		return URIS;
	}
	
	private class DummyModelContainer implements ModelContainer
	{

		private List<ModelProperty> properties;
		public DummyModelContainer(List<ModelProperty> properties)
		{
			this.properties = properties;
		}
		
		public ModelContainerAction containerAction(
				ModelContainer modelContainer) {
			return ModelContainerAction.NOP;
		}

		public ModelContainer createNewInstance(
				List<ModelProperty> modelProperties) {
			return new DummyModelContainer(modelProperties);
		}

		public List<ModelProperty> getProperties() {
			return properties;
		}
		
	}

}
