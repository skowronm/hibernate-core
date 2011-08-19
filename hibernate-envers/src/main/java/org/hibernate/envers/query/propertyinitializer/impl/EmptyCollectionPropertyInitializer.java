package org.hibernate.envers.query.propertyinitializer.impl;

import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.propertyinitializer.PropertyInitializer;

import java.util.Collection;

public class EmptyCollectionPropertyInitializer implements PropertyInitializer {
	@Override
	public <T> T getInitialValueForProperty(Class<T> propertyClass) {
		T value;
		try {
			value = propertyClass.newInstance();
			assert value instanceof Collection;
			assert ((Collection) value).isEmpty();
		} catch (InstantiationException e) {
			throw new AuditException("", e);
		} catch (IllegalAccessException e) {
			throw new AuditException("", e);
		}
		return value;
	}
}
