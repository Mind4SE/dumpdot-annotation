/**
 * Copyright (C) 2012 Schneider Electric
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Julien TOUS
 * Contributors:
 */

package org.ow2.mind.adl.annotations;

import java.io.*;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.adl.annotations.DotWriter;

/**
 * @author Julien TOUS
 */
public class DumpDotAnnotationProcessor extends
AbstractADLLoaderAnnotationProcessor {

	private String buildDir;

	private void showComposite(final Definition definition, String instanceName, DotWriter currentDot) {
		final Component[] subComponents = ((ComponentContainer) definition)
				.getComponents();
		for (int i = 0; i < subComponents.length; i++) {
			currentDot.addSubComponent(subComponents[i]);
		}
		final Binding[] bindings = ((BindingContainer) definition)
				.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			currentDot.addBinding(binding);
		}

		for (int i = 0; i < subComponents.length; i++) {
			final Component subComponent = subComponents[i];
			showComponents(subComponent, instanceName);
		}

	}

	private void showPrimitive(final Definition definition, String instanceName, DotWriter currentDot) {
		final Source[] sources = ((ImplementationContainer) definition).getSources();
		for (int i = 0; i < sources.length; i++) {
			currentDot.addSource(sources[i]);
		}
		
	}	

	private void showComponents(final Component component, String instanceName) {

		try {
			final Definition definition = ASTHelper.getResolvedDefinition(component
					.getDefinitionReference(), null, null);
			instanceName = instanceName + "." + component.getName();

			DotWriter currentDot = new DotWriter(buildDir, instanceName);

			final Interface[] interfaces = ((InterfaceContainer) definition).getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				final MindInterface itf = (MindInterface) interfaces[i];
				if (itf.getRole()==TypeInterface.SERVER_ROLE) {
					currentDot.addServer(itf.getName());
				}
			}
			for (int i = 0; i < interfaces.length; i++) {
				final MindInterface itf = (MindInterface) interfaces[i];	
				if (itf.getRole()==TypeInterface.CLIENT_ROLE) {
					currentDot.addClient(itf.getName());
				}
			}

			if (ASTHelper.isComposite(definition)) {
				showComposite(definition, instanceName, currentDot);
			} else if (ASTHelper.isPrimitive(definition)) {
				showPrimitive(definition, instanceName, currentDot);
			}
			currentDot.close();
		} catch (final ADLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor#processAnnotation
	 * (org.ow2.mind.annotation.Annotation, org.objectweb.fractal.adl.Node,
	 * org.objectweb.fractal.adl.Definition,
	 * org.ow2.mind.adl.annotation.ADLLoaderPhase, java.util.Map)
	 */
	public Definition processAnnotation(final Annotation annotation,
			final Node node, final Definition definition,
			final ADLLoaderPhase phase, final Map<Object, Object> context)
					throws ADLException {
		assert annotation instanceof DumpDot;

		String topLevelName = "TopLevel"; //FIXME get the executable name.

		buildDir = ((File) context.get(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY)).getPath() +  File.separator;
		DotWriter topDot = new DotWriter(buildDir, topLevelName);
		if (ASTHelper.isComposite(definition)) {
			showComposite(definition, topLevelName, topDot);
		} else if (ASTHelper.isPrimitive(definition)) {
			showPrimitive(definition, topLevelName, topDot);
		}
		topDot.close();
		return null;
	}

}
