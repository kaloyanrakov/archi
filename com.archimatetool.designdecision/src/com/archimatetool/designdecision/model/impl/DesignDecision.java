/**
 */
package com.archimatetool.designdecision.model.impl;

import com.archimatetool.designdecision.model.DesignDecisionType;
import com.archimatetool.designdecision.model.IDesignDecision;
import com.archimatetool.designdecision.model.IDesignDecisionPackage;

import com.archimatetool.model.impl.ArchimateElement;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Design Decision</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.archimatetool.designdecision.model.impl.DesignDecision#getDecisionType <em>Decision Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DesignDecision extends ArchimateElement implements IDesignDecision {
	/**
	 * The default value of the '{@link #getDecisionType() <em>Decision Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDecisionType()
	 * @generated
	 * @ordered
	 */
	protected static final DesignDecisionType DECISION_TYPE_EDEFAULT = DesignDecisionType.STRUCTURAL;
	/**
	 * The cached value of the '{@link #getDecisionType() <em>Decision Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDecisionType()
	 * @generated
	 * @ordered
	 */
	protected DesignDecisionType decisionType = DECISION_TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DesignDecision() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IDesignDecisionPackage.Literals.DESIGN_DECISION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DesignDecisionType getDecisionType() {
		return decisionType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDecisionType(DesignDecisionType newDecisionType) {
		DesignDecisionType oldDecisionType = decisionType;
		decisionType = newDecisionType == null ? DECISION_TYPE_EDEFAULT : newDecisionType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IDesignDecisionPackage.DESIGN_DECISION__DECISION_TYPE, oldDecisionType, decisionType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case IDesignDecisionPackage.DESIGN_DECISION__DECISION_TYPE:
				return getDecisionType();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case IDesignDecisionPackage.DESIGN_DECISION__DECISION_TYPE:
				setDecisionType((DesignDecisionType)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case IDesignDecisionPackage.DESIGN_DECISION__DECISION_TYPE:
				setDecisionType(DECISION_TYPE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case IDesignDecisionPackage.DESIGN_DECISION__DECISION_TYPE:
				return decisionType != DECISION_TYPE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (decisionType: ");
		result.append(decisionType);
		result.append(')');
		return result.toString();
	}

} //DesignDecision
