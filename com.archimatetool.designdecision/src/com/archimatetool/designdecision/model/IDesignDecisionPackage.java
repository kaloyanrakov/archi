/**
 */
package com.archimatetool.designdecision.model;

import com.archimatetool.model.IArchimatePackage;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see com.archimatetool.designdecision.model.IDesignDecisionFactory
 * @model kind="package"
 * @generated
 */
public interface IDesignDecisionPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.archimatetool.com/archimate/designdecision";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "designdecision";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IDesignDecisionPackage eINSTANCE = com.archimatetool.designdecision.model.impl.DesignDecisionPackage.init();

	/**
	 * The meta object id for the '{@link com.archimatetool.designdecision.model.impl.DesignDecision <em>Design Decision</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.archimatetool.designdecision.model.impl.DesignDecision
	 * @see com.archimatetool.designdecision.model.impl.DesignDecisionPackage#getDesignDecision()
	 * @generated
	 */
	int DESIGN_DECISION = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__NAME = IArchimatePackage.MOTIVATION_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__ID = IArchimatePackage.MOTIVATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Features</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__FEATURES = IArchimatePackage.MOTIVATION_ELEMENT__FEATURES;

	/**
	 * The feature id for the '<em><b>Documentation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__DOCUMENTATION = IArchimatePackage.MOTIVATION_ELEMENT__DOCUMENTATION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__PROPERTIES = IArchimatePackage.MOTIVATION_ELEMENT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Profiles</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION__PROFILES = IArchimatePackage.MOTIVATION_ELEMENT__PROFILES;

	/**
	 * The number of structural features of the '<em>Design Decision</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESIGN_DECISION_FEATURE_COUNT = IArchimatePackage.MOTIVATION_ELEMENT_FEATURE_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link com.archimatetool.designdecision.model.IDesignDecision <em>Design Decision</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Design Decision</em>'.
	 * @see com.archimatetool.designdecision.model.IDesignDecision
	 * @generated
	 */
	EClass getDesignDecision();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	IDesignDecisionFactory getDesignDecisionFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link com.archimatetool.designdecision.model.impl.DesignDecision <em>Design Decision</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.archimatetool.designdecision.model.impl.DesignDecision
		 * @see com.archimatetool.designdecision.model.impl.DesignDecisionPackage#getDesignDecision()
		 * @generated
		 */
		EClass DESIGN_DECISION = eINSTANCE.getDesignDecision();

	}

} //IDesignDecisionPackage
