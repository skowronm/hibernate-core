package org.hibernate.envers.query.propertyinitializer;

import org.hibernate.envers.entities.PropertyData;

import java.util.HashMap;
import java.util.Map;

public class CustomPropertyInitializers {
	private final Map<String, PropertyInitializer> initializers = new HashMap<String, PropertyInitializer>();

	public static CustomPropertyInitializers empty() {
		return new CustomPropertyInitializers();
	}

	public void addInitializer(String propertyName, PropertyInitializer initializer) {
		assert !initializers.containsKey(propertyName);
		initializers.put(propertyName, initializer);
	}

	public boolean canInitialize(PropertyData propertyData) {
		PropertyInitializer initializer = initializers.get(propertyData.getName());
		assert initializer == null || propertyData.isUsingCustomInitialization();
		return initializer != null && propertyData.isUsingCustomInitialization();
	}

	public <T> T initialize(PropertyData propertyData, Class<T> propertyClass) {
		return initializers.get(propertyData.getName()).getInitialValueForProperty(propertyClass);
	}

	public boolean hasInitializers() {
		return !initializers.isEmpty();
	}
}
