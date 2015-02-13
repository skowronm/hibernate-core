package org.hibernate.envers.mappergenerator;

import org.hibernate.envers.entities.mapper.PropertyMapper;
import org.hibernate.envers.entities.mapper.relation.CommonCollectionMapperData;
import org.hibernate.envers.entities.mapper.relation.MiddleComponentData;
import org.hibernate.mapping.Collection;

/**
 * Interface for classes responsible for generating appropriate collection mappers for hibernate collection data types
 *
 * @author Michal Skowronek
 */
public interface CollectionMapperResolver {
    PropertyMapper resolveCollectionMapper(Collection collection, CommonCollectionMapperData commonCollectionMapperData, MiddleComponentData elementComponentData, MiddleComponentData indexComponentData);
}
