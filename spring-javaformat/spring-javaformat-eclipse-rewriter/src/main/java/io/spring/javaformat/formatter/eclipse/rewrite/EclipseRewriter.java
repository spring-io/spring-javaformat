/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Internal build utility used to rewrite eclipse runtime classes.
 *
 * @author Phillip Webb
 */
public final class EclipseRewriter {

	private EclipseRewriter() {
	}

	public void rewrite(String file) throws IOException {
		System.out.println("Rewriting classes in " + file);
		URI uri = URI.create("jar:file:" + file);
		try (FileSystem zip = FileSystems.newFileSystem(uri,
				Collections.singletonMap("create", "true"))) {
			rewrite(zip);
		}
	}

	private void rewrite(FileSystem zip) throws IOException {
		Path path = zip
				.getPath("org/eclipse/jdt/internal/formatter/DefaultCodeFormatter.class");
		ClassWriter writer = new ClassWriter(0);
		try (InputStream in = Files.newInputStream(path)) {
			DefaultCodeFormatterManipulator manipulator = new DefaultCodeFormatterManipulator(
					writer);
			ClassReader reader = new ClassReader(in);
			reader.accept(manipulator, 0);
		}
		Files.copy(new ByteArrayInputStream(writer.toByteArray()), path,
				StandardCopyOption.REPLACE_EXISTING);
	}

	public static void main(String[] args) throws Exception {
		new EclipseRewriter().rewrite(args[0]);
	}

	private static class DefaultCodeFormatterManipulator extends ClassVisitor {

		DefaultCodeFormatterManipulator(ClassVisitor visitor) {
			super(Opcodes.ASM5, visitor);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			if ("prepareWraps".equals(name) && Opcodes.ACC_PRIVATE == access) {
				return super.visitMethod(Opcodes.ACC_PROTECTED, name, desc, signature,
						exceptions);
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
		public void visitMethodInsn(int opcode, String owner, String name, String desc,
				boolean itf) {
			if ("prepareWraps".equals(name) && opcode == Opcodes.INVOKESPECIAL) {
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, itf);
				return;
			}
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}

	}

}
