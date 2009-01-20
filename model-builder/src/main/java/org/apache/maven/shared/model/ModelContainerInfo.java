package org.apache.maven.shared.model;

import java.util.Collection;
import java.util.ArrayList;

public interface ModelContainerInfo {

    ModelContainerFactory getModelContainerFactory();

    ModelContainerRule getModelContainerRule();

    Collection<ModelContainerInfo> getChildren();

    Collection<ModelContainerFactory> getAllModelContainerFactories();

    public static class Factory
    {
        public static ModelContainerInfo createModelContainerInfo(final ModelContainerFactory factory,final ModelContainerRule rule,
                                                                  final Collection<ModelContainerInfo> children )
        {
            return new ModelContainerInfo() {
                private Collection<ModelContainerFactory> factories;

                public ModelContainerFactory getModelContainerFactory()
                {
                    return factory;
                }

                public ModelContainerRule getModelContainerRule() {
                    return rule;
                }

                public Collection<ModelContainerInfo> getChildren()
                {
                    return children;
                }

                public Collection<ModelContainerFactory> getAllModelContainerFactories()
                {
                    if(factories == null)
                    {
                        factories = new ArrayList<ModelContainerFactory>();
                        factories.add(factory);
                        if(children != null)
                        {
                            for(ModelContainerInfo info : children)
                            {
                                factories.addAll(info.getAllModelContainerFactories());
                            }
                        }
                    }

                    return factories;
                }
            };
        }
    }

}
