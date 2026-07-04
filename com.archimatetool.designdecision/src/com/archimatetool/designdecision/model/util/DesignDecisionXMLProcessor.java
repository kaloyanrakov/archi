/**
 */
package com.archimatetool.designdecision.model.util;

import com.archimatetool.designdecision.model.IDesignDecisionPackage;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class DesignDecisionXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DesignDecisionXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		IDesignDecisionPackage.eINSTANCE.eClass();
	}
	
	/**
	 * Register for "*" and "xml" file extensions the DesignDecisionResourceFactory factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new DesignDecisionResourceFactory());
			registrations.put(STAR_EXTENSION, new DesignDecisionResourceFactory());
		}
		return registrations;
	}

} //DesignDecisionXMLProcessor
