/**
 */
package com.archimatetool.designdecision.model;

import com.archimatetool.model.IMotivationElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Design Decision</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.archimatetool.designdecision.model.IDesignDecision#getDecisionType <em>Decision Type</em>}</li>
 * </ul>
 *
 * @see com.archimatetool.designdecision.model.IDesignDecisionPackage#getDesignDecision()
 * @model
 * @generated
 */
public interface IDesignDecision extends IMotivationElement {

	/**
	 * Returns the value of the '<em><b>Decision Type</b></em>' attribute.
	 * The default value is <code>"Structural"</code>.
	 * The literals are from the enumeration {@link com.archimatetool.designdecision.model.DesignDecisionType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Decision Type</em>' attribute.
	 * @see com.archimatetool.designdecision.model.DesignDecisionType
	 * @see #setDecisionType(DesignDecisionType)
	 * @see com.archimatetool.designdecision.model.IDesignDecisionPackage#getDesignDecision_DecisionType()
	 * @model default="Structural"
	 * @generated
	 */
	DesignDecisionType getDecisionType();

	/**
	 * Sets the value of the '{@link com.archimatetool.designdecision.model.IDesignDecision#getDecisionType <em>Decision Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Decision Type</em>' attribute.
	 * @see com.archimatetool.designdecision.model.DesignDecisionType
	 * @see #getDecisionType()
	 * @generated
	 */
	void setDecisionType(DesignDecisionType value);
} // IDesignDecision
