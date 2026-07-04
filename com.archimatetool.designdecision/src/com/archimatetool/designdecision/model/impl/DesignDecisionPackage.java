/**
 */
package com.archimatetool.designdecision.model.impl;

import com.archimatetool.designdecision.model.IDesignDecision;
import com.archimatetool.designdecision.model.IDesignDecisionFactory;
import com.archimatetool.designdecision.model.IDesignDecisionPackage;

import com.archimatetool.model.IArchimatePackage;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DesignDecisionPackage extends EPackageImpl implements IDesignDecisionPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass designDecisionEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see com.archimatetool.designdecision.model.IDesignDecisionPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DesignDecisionPackage() {
		super(eNS_URI, IDesignDecisionFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link IDesignDecisionPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static IDesignDecisionPackage init() {
		if (isInited) return (IDesignDecisionPackage)EPackage.Registry.INSTANCE.getEPackage(IDesignDecisionPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDesignDecisionPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DesignDecisionPackage theDesignDecisionPackage = registeredDesignDecisionPackage instanceof DesignDecisionPackage ? (DesignDecisionPackage)registeredDesignDecisionPackage : new DesignDecisionPackage();

		isInited = true;

		// Initialize simple dependencies
		IArchimatePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theDesignDecisionPackage.createPackageContents();

		// Initialize created meta-data
		theDesignDecisionPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDesignDecisionPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(IDesignDecisionPackage.eNS_URI, theDesignDecisionPackage);
		return theDesignDecisionPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDesignDecision() {
		return designDecisionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IDesignDecisionFactory getDesignDecisionFactory() {
		return (IDesignDecisionFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		designDecisionEClass = createEClass(DESIGN_DECISION);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		IArchimatePackage theArchimatePackage = (IArchimatePackage)EPackage.Registry.INSTANCE.getEPackage(IArchimatePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		designDecisionEClass.getESuperTypes().add(theArchimatePackage.getMotivationElement());

		// Initialize classes and features; add operations and parameters
		initEClass(designDecisionEClass, IDesignDecision.class, "DesignDecision", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //DesignDecisionPackage
