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
 *
 * @generated
 */
public class DesignDecision extends ArchimateElement implements IDesignDecision {
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

} //DesignDecision
