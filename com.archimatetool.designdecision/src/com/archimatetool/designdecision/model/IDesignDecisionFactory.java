/**
 */
package com.archimatetool.designdecision.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see com.archimatetool.designdecision.model.IDesignDecisionPackage
 * @generated
 */
public interface IDesignDecisionFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IDesignDecisionFactory eINSTANCE = com.archimatetool.designdecision.model.impl.DesignDecisionFactory.init();

	/**
	 * Returns a new object of class '<em>Design Decision</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Design Decision</em>'.
	 * @generated
	 */
	IDesignDecision createDesignDecision();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	IDesignDecisionPackage getDesignDecisionPackage();

} //IDesignDecisionFactory
