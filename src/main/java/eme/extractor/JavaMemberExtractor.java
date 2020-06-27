package eme.extractor;

import static eme.extractor.JDTUtil.getModifier;
import static eme.extractor.JDTUtil.getName;
import static eme.extractor.JDTUtil.isAbstract;
import static eme.extractor.JDTUtil.isEnum;
import static eme.extractor.JDTUtil.isFinal;
import static eme.extractor.JDTUtil.isStatic;
import static eme.extractor.JDTUtil.isVoid;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;

import eme.model.ExtractedMethod;
import eme.model.ExtractedType;
import eme.model.MethodType;
import eme.model.datatypes.AccessLevelModifier;
import eme.model.datatypes.ExtractedField;
import eme.model.datatypes.ExtractedParameter;

/**
 * Extractor class for Java Members (Methods and fields). Uses the class
 * {@link DataTypeExtractor}.
 * 
 * @author Timur Saglam
 */
public class JavaMemberExtractor {
	private final DataTypeExtractor dataTypeExtractor;

	/**
	 * Basic constructor.
	 * 
	 * @param dataTypeExtractor sets the {@link DataTypeExtractor}.
	 */
	public JavaMemberExtractor(DataTypeExtractor dataTypeExtractor) {
		this.dataTypeExtractor = dataTypeExtractor;
	}

	/**
	 * Parses Fields from an {@link IType} and adds them to an
	 * {@link ExtractedType}.
	 * 
	 * @param type          is the {@link IType}.
	 * @param extractedType is the {@link ExtractedType}.
	 * @throws JavaModelException if there are problem with the JDT API.
	 */
	public void extractFields(IType type, ExtractedType extractedType) throws JavaModelException {
		ExtractedField extractedField;
		for (IField field : type.getFields()) {
			if (!isEnum(field)) { // if is no enumeral
				extractedField = dataTypeExtractor.extractField(field, type);
				extractedField.setFinal(isFinal(field));
				extractedField.setStatic(isStatic(field));
				extractedField.setModifier(getModifier(field));
				extractedType.addField(extractedField);
			}
		}
	}

	/**
	 * Parses the {@link IMethod}s from an {@link IType} and adds them to an
	 * ExtractedType.
	 * 
	 * @param type          is the {@link IType} whose methods get extracted.
	 * @param extractedType is the extracted type where the extracted methods should
	 *                      be added.
	 * @throws JavaModelException if there are problem with the JDT API.
	 */
	public void extractMethods(IType type, ExtractedType extractedType) throws JavaModelException {
		ExtractedMethod extractedMethod;
		System.out.println(extractedType.getName());
		String methodName; // name of the extracted method
		for (IMethod method : type.getMethods()) { // for every method
			ExtractedField extractedField = null;
			methodName = getName(type) + "." + method.getElementName(); // build name
			extractedMethod = new ExtractedMethod(methodName, dataTypeExtractor.extractReturnType(method));

			
			extractModifiers(method, extractedMethod);
			if (extractedMethod.isStatic() || extractedMethod.getMethodType() == MethodType.CONSTRUCTOR) {
				System.out.println(extractedMethod.getName());
				continue;
			}
				
			ITypeParameter[] typeParameters = method.getTypeParameters();
			extractedMethod.setTypeParameters(dataTypeExtractor.extractTypeParameters(typeParameters, type));
			for (ILocalVariable parameter : method.getParameters()) { // extract parameters:
				ExtractedParameter params = dataTypeExtractor.extractParameter(parameter, method);
				extractedMethod.addParameter(params);
				if (extractedField == null) {
					extractedField = new ExtractedField(extractedMethod.getName(), params.getFullTypeName(), 0);
					extractedField.setModifier(AccessLevelModifier.PUBLIC);
					extractedField.setFinal(false);
					extractedField.setStatic(false);
					//extractedField.setModifier(getModifier(field));

				}

			}
			for (String exception : method.getExceptionTypes()) { // extract throw declarations:
				extractedMethod.addThrowsDeclaration(dataTypeExtractor.extractDataType(exception, type));
			}
			
			//extractedType.addMethod(extractedMethod); 
			//System.out.println("Method: " + extractedMethod.getName() );
			if (extractedField != null) {
				System.out.println("Field: " + extractedField.getIdentifier() + " " + extractedField.getFullType());
				extractedType.addField(extractedField);
			}
			else
				System.out.println("Extractedfield was null: "+ extractedMethod.getName() );
			
		}
	}

	/**
	 * Parses the {@link MethodType} of an {@link IMethod}.
	 */
	private MethodType extractMethodType(IMethod method) throws JavaModelException {
		if (method.isConstructor()) {
			return MethodType.CONSTRUCTOR;
		} else if (isAccessor(method)) {
			return MethodType.ACCESSOR;
		} else if (isMutator(method)) {
			return MethodType.MUTATOR;
		} else if (method.isMainMethod()) {
			return MethodType.MAIN;
		}
		return MethodType.NORMAL;
	}

	/**
	 * Extracts modifiers from an {@link IMethod} and adds them to an
	 * {@link ExtractedMethod}.
	 */
	private void extractModifiers(IMethod method, ExtractedMethod extractedMethod) throws JavaModelException {
		extractedMethod.setAbstract(isAbstract(method));
		extractedMethod.setStatic(isStatic(method));
		extractedMethod.setMethodType(extractMethodType(method));
		extractedMethod.setModifier(getModifier(method));
	}

	/**
	 * Checks whether a {@link IMethod} is an access method (either an accessor or
	 * an mutator, depending on the prefix).
	 */
	private boolean isAccessMethod(String prefix, IMethod method) throws JavaModelException {
		IType type = method.getDeclaringType();
		for (IField field : type.getFields()) { // for ever field of IType:
			if (method.getElementName().equalsIgnoreCase(prefix + field.getElementName())) {
				return true; // is access method if name scheme fits for one field
			}
		}
		return false; // is not an access method if no field fits
	}

	/**
	 * Checks whether a {@link IMethod} is an accessor method.
	 */
	private boolean isAccessor(IMethod method) throws JavaModelException {
		if (isAccessMethod("get", method) || isAccessMethod("is", method)) { // if name fits
			return method.getNumberOfParameters() == 0 && !isVoid(method.getReturnType());
		}
		return false;
	}

	/**
	 * Checks whether a {@link IMethod} is a mutator method.
	 */
	private boolean isMutator(IMethod method) throws JavaModelException {
		if (isAccessMethod("set", method)) { // if name fits
			return method.getNumberOfParameters() == 1 && isVoid(method.getReturnType());
		}
		return false;
	}
}