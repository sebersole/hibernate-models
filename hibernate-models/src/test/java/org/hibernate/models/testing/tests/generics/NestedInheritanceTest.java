/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.testing.tests.generics;

import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassTypeDetails;
import org.hibernate.models.spi.FieldDetails;
import org.hibernate.models.spi.SourceModelBuildingContext;
import org.hibernate.models.spi.TypeDetails;
import org.hibernate.models.spi.TypeVariableDetails;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.testing.TestHelper.createModelContext;

/**
 * @author Marco Belladelli
 */
public class NestedInheritanceTest {
	@Test
	void testNestedGenericHierarchy() {
		final SourceModelBuildingContext buildingContext = createModelContext(
				BaseClass.class,
				IntermediateOne.class,
				IntermediateTwo.class,
				IntermediateThree.class,
				LeafClass.class
		);

		final ClassDetails baseClassDetails = buildingContext.getClassDetailsRegistry().getClassDetails( BaseClass.class.getName() );
		final ClassDetails intermediateClassDetails = buildingContext.getClassDetailsRegistry().getClassDetails( IntermediateOne.class.getName() );
		final ClassDetails leafClassDetails = buildingContext.getClassDetailsRegistry().getClassDetails( LeafClass.class.getName() );

		final FieldDetails base = baseClassDetails.findFieldByName( "base" );
		final TypeDetails baseFieldType = base.getType();
		assertThat( baseFieldType.getTypeKind() ).isEqualTo( TypeDetails.Kind.TYPE_VARIABLE );
		assertThat( baseFieldType.isResolved() ).isFalse();

		{
			final TypeDetails resolvedRelativeType = base.resolveRelativeType( baseClassDetails );
			assertThat( resolvedRelativeType.isResolved() ).isFalse();
			assertThat( resolvedRelativeType ).isInstanceOf( TypeVariableDetails.class );
		}

		final FieldDetails one = intermediateClassDetails.findFieldByName( "one" );
		final TypeDetails oneFieldType = one.getType();
		assertThat( oneFieldType.getTypeKind() ).isEqualTo( TypeDetails.Kind.TYPE_VARIABLE );
		assertThat( oneFieldType.isResolved() ).isFalse();

		{
			final TypeDetails concreteType = base.resolveRelativeType( intermediateClassDetails );
			assertThat( concreteType ).isInstanceOf( ClassTypeDetails.class );
			final ClassDetails concreteClassDetails = ( (ClassTypeDetails) concreteType ).getClassDetails();
			assertThat( concreteClassDetails.toJavaClass() ).isEqualTo( Integer.class );

			final TypeDetails intermediateConcreteType = one.resolveRelativeType( intermediateClassDetails );
			assertThat( intermediateConcreteType.isResolved() ).isFalse();
			assertThat( intermediateConcreteType ).isInstanceOf( TypeVariableDetails.class );
		}

		{
			final TypeDetails baseConcreteType = base.resolveRelativeType( leafClassDetails );
			assertThat( baseConcreteType ).isInstanceOf( ClassTypeDetails.class );
			final ClassDetails concreteClassDetails = ( (ClassTypeDetails) baseConcreteType ).getClassDetails();
			assertThat( concreteClassDetails.toJavaClass() ).isEqualTo( Integer.class );

			final TypeDetails intermediateConcreteType = one.resolveRelativeType( leafClassDetails );
			assertThat( intermediateConcreteType ).isInstanceOf( ClassTypeDetails.class );
			final ClassDetails intermediateConcreteClassDetails = ( (ClassTypeDetails) intermediateConcreteType ).getClassDetails();
			assertThat( intermediateConcreteClassDetails.toJavaClass() ).isEqualTo( String.class );
		}
	}

	static class BaseClass<T> {
		T base;
	}

	static class IntermediateOne<T> extends BaseClass<Integer> {
		T one;
	}

	static class IntermediateTwo<T, J> extends IntermediateOne<String> {
	}

	static class IntermediateThree extends IntermediateTwo<Long, Short> {
	}

	static class LeafClass extends IntermediateThree {
	}
}
