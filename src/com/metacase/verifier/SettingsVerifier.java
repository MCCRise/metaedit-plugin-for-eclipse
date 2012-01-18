/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.verifier;

import javax.swing.JComponent;

/**
 * Simple settings verifier interface. Is implemented by the
 * inner classes in SettinsDialog class.
 *
 */
public interface SettingsVerifier {
	/**
	 * Verifies the given input. Used for settings dialog textfields.
	 * @param input - component that contains the input.
	 * @return integer showing the verifying result
	 */
	public int verify(JComponent input);
}
