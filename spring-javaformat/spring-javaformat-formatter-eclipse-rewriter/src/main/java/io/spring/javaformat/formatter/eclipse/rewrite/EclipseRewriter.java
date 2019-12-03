/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.formatter.eclipse.rewrite;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Internal build utility used to rewrite eclipse runtime classes.
 *
 * @author Phillip Webb
 */
public final class EclipseRewriter {

	private static final Set<String> UPDATED_METHODS;
	static {
		Set<String> updatedMethods = new LinkedHashSet<String>();
		updatedMethods.add("prepareWraps");
		updatedMethods.add("tokenizeSource");
		UPDATED_METHODS = Collections.unmodifiableSet(updatedMethods);
	}

	private static final Set<String> UPDATED_FIELDS;
	static {
		Set<String> updatedFields = new LinkedHashSet<String>();
		updatedFields.add("sourceLevel");
		updatedFields.add("tokens");
		UPDATED_FIELDS = Collections.unmodifiableSet(updatedFields);
	}

	private EclipseRewriter() {
	}

	public void rewrite(String file) throws IOException {
		System.out.println("Rewriting classes in " + file);
		URI uri = URI.create("jar:file:" + Paths.get(file).toUri().getPath());
		try (FileSystem zip = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"))) {
			rewrite(zip);
		}
	}

	private void rewrite(FileSystem zip) throws IOException {
		Path path = zip.getPath("org/eclipse/jdt/internal/formatter/DefaultCodeFormatter.class");
		ClassWriter writer = new ClassWriter(0);
		try (InputStream in = Files.newInputStream(path)) {
			DefaultCodeFormatterManipulator manipulator = new DefaultCodeFormatterManipulator(writer);
			ClassReader reader = new ClassReader(in);
			reader.accept(manipulator, 0);
		}
		Files.copy(new ByteArrayInputStream(writer.toByteArray()), path, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void main(String[] args) throws Exception {
		new EclipseRewriter().rewrite(args[0]);
	}

	private static class DefaultCodeFormatterManipulator extends ClassVisitor {

		DefaultCodeFormatterManipulator(ClassVisitor visitor) {
			super(Opcodes.ASM5, visitor);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if (access == Opcodes.ACC_PRIVATE && UPDATED_FIELDS.contains(name)) {
				access = Opcodes.ACC_PROTECTED;
			}
			return super.visitField(access, name, desc, signature, value);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (access == Opcodes.ACC_PRIVATE && UPDATED_METHODS.contains(name)) {
				access = Opcodes.ACC_PROTECTED;
			}
			return new DefaultCodeFormatterMethodManipulator(
					super.visitMethod(access, name, desc, signature, exceptions));
		}

	}

	private static class DefaultCodeFormatterMethodManipulator extends MethodVisitor {

		DefaultCodeFormatterMethodManipulator(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (opcode == Opcodes.INVOKESPECIAL && UPDATED_METHODS.contains(name)) {
				opcode = Opcodes.INVOKEVIRTUAL;
			}
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}

	}

}
