/**
 * 
 */
package io.spring.javaformat.checkstyle.check;

import com.puppycrawl.tools.checkstyle.checks.imports.IllegalImportCheck;

/**
 * Checks for illegal import packages and classes
 * 
 * @author Sushant Kumar Singh
 *
 */
public class SpringIllegalImportCheck extends IllegalImportCheck{
	
	/**
	 * Illegal packages
	 */
	private static final String[] ILLEGAL_PKGS = {"sun","lombok"};
	
	/**
	 * Illegal classes
	 */
	private static final String[] ILLEGAL_CLASSES = {};
	

	public SpringIllegalImportCheck() {
		setIllegalPkgs(ILLEGAL_PKGS);
		setIllegalClasses(ILLEGAL_CLASSES);
	}
	
	

}
