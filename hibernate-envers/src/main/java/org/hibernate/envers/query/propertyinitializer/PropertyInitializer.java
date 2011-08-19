package org.hibernate.envers.query.propertyinitializer;

public interface PropertyInitializer {
	<T> T getInitialValueForProperty(Class<T> propertyClass);
}
