/**
 */
package com.archimatetool.designdecision.model.util;

import com.archimatetool.designdecision.model.*;

import com.archimatetool.model.IAdapter;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateModelObject;
import com.archimatetool.model.ICloneable;
import com.archimatetool.model.IDocumentable;
import com.archimatetool.model.IFeatures;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.IMotivationElement;
import com.archimatetool.model.INameable;
import com.archimatetool.model.IProfiles;
import com.archimatetool.model.IProperties;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see com.archimatetool.designdecision.model.IDesignDecisionPackage
 * @generated
 */
public class DesignDecisionAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static IDesignDecisionPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DesignDecisionAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = IDesignDecisionPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DesignDecisionSwitch<Adapter> modelSwitch =
		new DesignDecisionSwitch<Adapter>() {
			@Override
			public Adapter caseDesignDecision(IDesignDecision object) {
				return createDesignDecisionAdapter();
			}
			@Override
			public Adapter caseAdapter(IAdapter object) {
				return createAdapterAdapter();
			}
			@Override
			public Adapter caseNameable(INameable object) {
				return createNameableAdapter();
			}
			@Override
			public Adapter caseIdentifier(IIdentifier object) {
				return createIdentifierAdapter();
			}
			@Override
			public Adapter caseFeatures(IFeatures object) {
				return createFeaturesAdapter();
			}
			@Override
			public Adapter caseArchimateModelObject(IArchimateModelObject object) {
				return createArchimateModelObjectAdapter();
			}
			@Override
			public Adapter caseCloneable(ICloneable object) {
				return createCloneableAdapter();
			}
			@Override
			public Adapter caseDocumentable(IDocumentable object) {
				return createDocumentableAdapter();
			}
			@Override
			public Adapter caseProperties(IProperties object) {
				return createPropertiesAdapter();
			}
			@Override
			public Adapter caseProfiles(IProfiles object) {
				return createProfilesAdapter();
			}
			@Override
			public Adapter caseArchimateConcept(IArchimateConcept object) {
				return createArchimateConceptAdapter();
			}
			@Override
			public Adapter caseArchimateElement(IArchimateElement object) {
				return createArchimateElementAdapter();
			}
			@Override
			public Adapter caseMotivationElement(IMotivationElement object) {
				return createMotivationElementAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.designdecision.model.IDesignDecision <em>Design Decision</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.designdecision.model.IDesignDecision
	 * @generated
	 */
	public Adapter createDesignDecisionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IAdapter <em>Adapter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IAdapter
	 * @generated
	 */
	public Adapter createAdapterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.INameable <em>Nameable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.INameable
	 * @generated
	 */
	public Adapter createNameableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IIdentifier <em>Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IIdentifier
	 * @generated
	 */
	public Adapter createIdentifierAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IFeatures <em>Features</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IFeatures
	 * @generated
	 */
	public Adapter createFeaturesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IArchimateModelObject <em>Model Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IArchimateModelObject
	 * @generated
	 */
	public Adapter createArchimateModelObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.ICloneable <em>Cloneable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.ICloneable
	 * @generated
	 */
	public Adapter createCloneableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IDocumentable <em>Documentable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IDocumentable
	 * @generated
	 */
	public Adapter createDocumentableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IProperties
	 * @generated
	 */
	public Adapter createPropertiesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IProfiles <em>Profiles</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IProfiles
	 * @generated
	 */
	public Adapter createProfilesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IArchimateConcept <em>Concept</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IArchimateConcept
	 * @generated
	 */
	public Adapter createArchimateConceptAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IArchimateElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IArchimateElement
	 * @generated
	 */
	public Adapter createArchimateElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.archimatetool.model.IMotivationElement <em>Motivation Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.archimatetool.model.IMotivationElement
	 * @generated
	 */
	public Adapter createMotivationElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //DesignDecisionAdapterFactory
