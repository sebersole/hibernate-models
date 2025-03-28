/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.bytebuddy.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hibernate.models.bytebuddy.spi.TypeSwitch;
import org.hibernate.models.bytebuddy.spi.TypeSwitcher;
import org.hibernate.models.internal.ClassTypeDetailsImpl;
import org.hibernate.models.internal.ParameterizedTypeDetailsImpl;
import org.hibernate.models.internal.PrimitiveTypeDetailsImpl;
import org.hibernate.models.internal.TypeVariableDetailsImpl;
import org.hibernate.models.internal.TypeVariableReferenceDetailsImpl;
import org.hibernate.models.internal.VoidTypeDetailsImpl;
import org.hibernate.models.internal.WildcardTypeDetailsImpl;
import org.hibernate.models.internal.util.CollectionHelper;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.SourceModelBuildingContext;
import org.hibernate.models.spi.TypeDetails;
import org.hibernate.models.spi.TypeDetailsHelper;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;

import static org.hibernate.models.internal.util.CollectionHelper.arrayList;

/**
 * @author Steve Ebersole
 */
public class TypeSwitchStandard implements TypeSwitch<TypeDetails> {

	public static TypeDetails switchType(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		return switchType( typeDescription, null, buildingContext );
	}

	public static TypeDetails switchType(TypeDefinition typeDescription, ClassDetails declaringType, SourceModelBuildingContext buildingContext) {
		final TypeSwitchStandard switchImpl = new TypeSwitchStandard( declaringType );
		return TypeSwitcher.switchType( typeDescription, switchImpl, buildingContext );
	}


	private final ClassDetails declaringType;

	public TypeSwitchStandard(ClassDetails declaringType) {
		this.declaringType = declaringType;
	}

	@Override
	public TypeDetails caseClass(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( typeDescription.getTypeName() );
		return new ClassTypeDetailsImpl( classDetails, TypeDetails.Kind.CLASS );
	}

	@Override
	public TypeDetails casePrimitive(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( typeDescription.getTypeName() );
		return new PrimitiveTypeDetailsImpl( classDetails );
	}

	@Override
	public TypeDetails caseVoid(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				// allows for void or Void
				.resolveClassDetails( typeDescription.getTypeName() );
		return new VoidTypeDetailsImpl( classDetails );
	}

	@Override
	public TypeDetails caseParameterizedType(
			TypeDefinition typeDescription,
			SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( typeDescription.asErasure().getName() );
		return new ParameterizedTypeDetailsImpl(
				classDetails,
				resolveTypes( typeDescription.asGenericType().getTypeArguments(), this, buildingContext ),
				null
		);
	}

	@Override
	public TypeDetails caseWildcardType(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final TypeList.Generic upperBounds = typeDescription.asGenericType().getUpperBounds();
		final TypeList.Generic lowerBounds = typeDescription.asGenericType().getLowerBounds();

		final TypeList.Generic bound;
		final boolean isExtends;

		if ( isExtends( upperBounds, lowerBounds ) ) {
			bound = upperBounds;
			isExtends = true;
		}
		else {
			bound = lowerBounds;
			isExtends = false;
		}

		return new WildcardTypeDetailsImpl( TypeSwitcher.switchType( bound.get( 0 ), this, buildingContext ), isExtends );
	}

	private boolean isExtends(TypeList.Generic upperBounds, TypeList.Generic lowerBounds) {
		if ( lowerBounds.isEmpty() ) {
			return true;
		}

		return upperBounds.isEmpty();
	}

	private HashSet<String> typeVariableIdentifiers;

	@Override
	public TypeDetails caseTypeVariable(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final boolean isTypeVariableRef;
		if ( typeVariableIdentifiers == null ) {
			typeVariableIdentifiers = new HashSet<>();
			typeVariableIdentifiers.add( typeDescription.getActualName() );
			isTypeVariableRef = false;
		}
		else {
			final boolean newlyAdded = typeVariableIdentifiers.add( typeDescription.getActualName() );
			isTypeVariableRef = !newlyAdded;
		}

		if ( isTypeVariableRef ) {
			return new TypeVariableReferenceDetailsImpl( typeDescription.getActualName() );
		}

		return new TypeVariableDetailsImpl(
				typeDescription.getActualName(),
				declaringType,
				resolveTypes( typeDescription.asGenericType().getUpperBounds(), this, buildingContext )
		);
	}

	@Override
	public TypeDetails caseTypeVariableReference(
			TypeDefinition typeDescription,
			SourceModelBuildingContext buildingContext) {
		// todo : This is not actually correct I think.  From the Byte Buddy javadocs:
		//		> Represents a type variable that is merely symbolic and is not
		//		> attached to a net.bytebuddy.description.TypeVariableSource and does
		//		> not defined bounds.
		//   - but I am unsure of an actual scenario this is attempting to describe
		//		to be able to test it out
		return new TypeVariableReferenceDetailsImpl( typeDescription.getActualName() );
	}

	@Override
	public TypeDetails caseArrayType(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		final TypeDetails constituentType = TypeSwitcher.switchType( typeDescription.getComponentType(), this, buildingContext );
		return TypeDetailsHelper.arrayOf( constituentType, buildingContext );
	}

	@Override
	public TypeDetails defaultCase(TypeDefinition typeDescription, SourceModelBuildingContext buildingContext) {
		throw new UnsupportedOperationException( "Unexpected Type kind - " + typeDescription );
	}

	public static List<TypeDetails> resolveTypes(
			TypeList.Generic generics,
			TypeSwitchStandard typeSwitch,
			SourceModelBuildingContext buildingContext) {
		if ( CollectionHelper.isEmpty( generics ) ) {
			return Collections.emptyList();
		}

		final ArrayList<TypeDetails> result = arrayList( generics.size() );
		for ( TypeDescription.Generic bound : generics ) {
			final TypeDetails switchedType = TypeSwitcher.switchType( bound, typeSwitch, buildingContext );
			result.add( switchedType );
		}

		return result;
	}
}
