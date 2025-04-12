package d4m.acc.access;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.hl7.fhir.emf.SDSSwitch;
import org.hl7.fhir.emf.SkipSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is not used.  The implementation needs work.
// Usage is in the future.

public class FHIRD4M {

	private static final Logger LOG = LoggerFactory.getLogger(FHIRD4M.class);

	SDSSwitch sdsSwitch = new SDSSwitch();
	SkipSwitch skipSwitch = new SkipSwitch();

	public String[] serialize(final EObject eObject) {
		EClass eClass = eObject.eClass();
		if (skipSwitch.doSwitch(eObject)) {
			return null;
		}
		EAnnotation eAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
		if (eClass == eAnnotation.eClass()) {
			return null;
		}

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			LOG.trace("eAttribute=" + eClass.getName() + "." + eAttribute.getName());
			return serialize(eObject, eAttribute);
		}

		for (EReference eReference : eClass.getEAllReferences()) {
			LOG.trace("eReference=" + eClass.getName() + "." + eReference.getName());
			return serialize(eObject, eReference);
		}
		return null;
	}

	public String[] serialize(final EObject eObject, final EAttribute eAttribute) {

		if (isOr(eObject, eAttribute)) {
			return null;
		}
		if (skipSwitch.doSwitch(eObject)) {
			return null;
		}

		if (eAttribute.isMany()) {
			LOG.info("isMany=" + eAttribute.getName());
			for (EObject eObject1 : eAttribute.eContents()) {
				return serializeOne(eObject1, eAttribute);
			}
		} else {
			return serializeOne(eObject, eAttribute);
		}
		return null;
	}

	public String[] serialize(final EObject eObject, final EReference eReference) {

		if (isOr(eObject, eReference)) {
			return null;
		}
		if (skipSwitch.doSwitch(eObject)) {
			return null;
		}

		if (eReference.isMany()) {
			LOG.info("isMany=" + eReference.getName());
			for (EObject eObject1 : eReference.eContents()) {
				return serializeOne(eObject1, eReference);
			}
		} else {
			return serializeOne(eObject, eReference);
		}
		return null;
	}

	public String[] serializeOne(final EObject eObject, final EAttribute eAttribute) {
		return new String[3];
	}

	public String[] serializeOne(final EObject eObject, final EReference eReference) {
		return new String[3];
	}

	public Boolean isOr(final EObject eObject, final EAttribute eAttribute) {
		return false;
	}
	
	public Boolean isOr(final EObject eObject, final EReference eReference) {
		return false;
	}
}
