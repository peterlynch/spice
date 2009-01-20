package org.apache.maven.shared.model;

import java.util.List;

public interface ModelContainerRule {

    List<ModelProperty> execute(List<ModelProperty> modelProperties);   
}
