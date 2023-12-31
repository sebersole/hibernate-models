/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.source.internal.standard;

import org.hibernate.models.internal.StringHelper;
import org.hibernate.models.source.ModelsException;
import org.hibernate.models.source.UnknownClassException;
import org.hibernate.models.source.spi.ClassDetails;
import org.hibernate.models.source.spi.ClassDetailsBuilder;
import org.hibernate.models.source.spi.SourceModelBuildingContext;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;

/**
 * HCANN based ClassDetailsBuilder
 *
 * @author Steve Ebersole
 */
public class ClassDetailsBuilderImpl implements ClassDetailsBuilder {
	public static final ClassDetailsBuilderImpl DEFAULT_BUILDER = new ClassDetailsBuilderImpl();

	public ClassDetailsBuilderImpl() {
	}

	@Override
	public ClassDetails buildClassDetails(String name, SourceModelBuildingContext processingContext) {
		return buildClassDetailsStatic( name, processingContext.getJandexIndex(), processingContext );
	}

	public static ClassDetails buildClassDetailsStatic(String name, SourceModelBuildingContext processingContext) {
		return buildClassDetailsStatic( name, processingContext.getJandexIndex(), processingContext );
	}

	public static ClassDetails buildClassDetailsStatic(
			String name,
			IndexView jandexIndex,
			SourceModelBuildingContext processingContext) {
		if ( "void".equals( name ) ) {
			name = Void.class.getName();
		}
		final ClassInfo classInfo = jandexIndex.getClassByName( name );
		if ( StringHelper.isNotEmpty( name ) && classInfo == null ) {
			// potentially handle primitives
			final Class<?> primitiveWrapperClass = resolveMatchingPrimitiveWrapper( name );
			if ( primitiveWrapperClass != null ) {
				final ClassInfo wrapperClassInfo = jandexIndex.getClassByName( primitiveWrapperClass.getName() );
				return new ClassDetailsImpl( wrapperClassInfo, processingContext );
			}

			throw new UnknownClassException( "Could not find class [" + name + "] in Jandex index" );
		}
		return new ClassDetailsImpl( classInfo, processingContext );
	}

	private static Class<?> resolveMatchingPrimitiveWrapper(String className) {
		if ( "boolean".equals( className ) ) {
			return Boolean.class;
		}

		if ( "byte".equals( className ) ) {
			return Byte.class;
		}

		if ( "short".equals( className ) ) {
			return Short.class;
		}

		if ( "int".equals( className ) ) {
			return Integer.class;
		}

		if ( "long".equals( className ) ) {
			return Long.class;
		}

		if ( "double".equals(  className ) ) {
			return Double.class;
		}

		if ( "float".equals( className ) ) {
			return Float.class;
		}

		return null;
	}
}
