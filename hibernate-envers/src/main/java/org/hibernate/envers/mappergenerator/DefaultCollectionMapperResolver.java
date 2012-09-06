package org.hibernate.envers.mappergenerator;

import org.hibernate.envers.entities.mapper.PropertyMapper;
import org.hibernate.envers.entities.mapper.relation.*;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.*;
import org.hibernate.mapping.Collection;
import org.hibernate.type.*;

import java.util.*;

public class DefaultCollectionMapperResolver implements CollectionMapperResolver {

    public PropertyMapper resolveCollectionMapper(Collection collection, CommonCollectionMapperData commonCollectionMapperData, MiddleComponentData elementComponentData, MiddleComponentData indexComponentData) {
        Type type = collection.getType();
        if (type instanceof SortedSetType) {
            return new SortedSetCollectionMapper(commonCollectionMapperData, TreeSet.class, SortedSetProxy.class, elementComponentData, collection.getComparator());
        } else if (type instanceof SetType) {
            return new BasicCollectionMapper<Set>(commonCollectionMapperData, HashSet.class, SetProxy.class, elementComponentData);
        } else if (type instanceof SortedMapType) {
            // Indexed collection, so <code>indexComponentData</code> is not null.
            return new SortedMapCollectionMapper(commonCollectionMapperData, TreeMap.class, SortedMapProxy.class, elementComponentData, indexComponentData, collection.getComparator());
        } else if (type instanceof MapType) {
            // Indexed collection, so <code>indexComponentData</code> is not null.
            return new MapCollectionMapper<Map>(commonCollectionMapperData, HashMap.class, MapProxy.class, elementComponentData, indexComponentData);
        } else if (type instanceof BagType) {
            return new BasicCollectionMapper<List>(commonCollectionMapperData, ArrayList.class, ListProxy.class, elementComponentData);
        } else if (type instanceof ListType) {
            // Indexed collection, so <code>indexComponentData</code> is not null.
            return new ListCollectionMapper(commonCollectionMapperData, elementComponentData, indexComponentData);
        }
        return null;
    }
}
