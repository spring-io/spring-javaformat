/*
 * Copyright 2017-2021 the original author or authors.
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
import java.util.function.Function;

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

	public void rewrite(JdkVersion jdkVersion, String file) throws IOException {
		System.out.println("Rewriting classes in " + file);
		URI uri = URI.create("jar:file:" + Paths.get(file).toUri().getPath());
		try (FileSystem zip = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"))) {
			rewrite(jdkVersion, zip);
		}
	}

	private void rewrite(JdkVersion jdkVersion, FileSystem zip) throws IOException {
		rewrite(zip, "org/eclipse/jdt/internal/formatter/DefaultCodeFormatter.class",
				DefaultCodeFormatterManipulator::new);
		if (jdkVersion == JdkVersion.V8) {
			rewrite(zip, "org/eclipse/osgi/util/NLS$1.class", NlsJdk8Manipulator::new);
		}
		else {
			rewrite(zip, "org/eclipse/osgi/util/NLS.class", NlsJdk11Manipulator::new);
		}
	}

	private void rewrite(FileSystem zip, String name, Function<ClassWriter, ClassVisitor> manipulator)
			throws IOException {
		ClassWriter classWriter = new ClassWriter(0);
		Path path = zip.getPath(name);
		try (InputStream in = Files.newInputStream(path)) {
			ClassReader reader = new ClassReader(in);
			reader.accept(manipulator.apply(classWriter), 0);
		}
		Files.copy(new ByteArrayInputStream(classWriter.toByteArray()), path, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void main(String[] args) throws Exception {
		new EclipseRewriter().rewrite(JdkVersion.valueOf("V" + args[0]), args[1]);
	}

	/**
	 * {@link ClassVisitor} to make some fields and methods from
	 * {@code DefaultCodeFormatter} public.
	 */
	private static class DefaultCodeFormatterManipulator extends ClassVisitor {

		DefaultCodeFormatterManipulator(ClassVisitor visitor) {
			super(Opcodes.ASM7, visitor);
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

	/**
	 * {@link MethodVisitor} to make some fields and methods from
	 * {@code DefaultCodeFormatter} public.
	 */
	private static class DefaultCodeFormatterMethodManipulator extends MethodVisitor {

		DefaultCodeFormatterMethodManipulator(MethodVisitor mv) {
			super(Opcodes.ASM7, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (opcode == Opcodes.INVOKESPECIAL && UPDATED_METHODS.contains(name)) {
				opcode = Opcodes.INVOKEVIRTUAL;
			}
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}

	}

	/**
	 * {@link ClassVisitor} to update the {@code NLS} class in the JDK 8 version so it
	 * doesn't use a System property to disable warning messages.
	 */
	private static class NlsJdk8Manipulator extends ClassVisitor {

		NlsJdk8Manipulator(ClassVisitor visitor) {
			super(Opcodes.ASM7, visitor);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if ("run".equals(name) && desc.contains("Boolean")) {
				return new NslJdk8MethodManipulator(super.visitMethod(access, name, desc, signature, exceptions));
			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

	}

	/**
	 * {@link MethodVisitor} to update the {@code NLS} class in the JDK 8 version so it
	 * doesn't use a System property to disable warning messages.
	 */
	private static class NslJdk8MethodManipulator extends MethodVisitor {

		private final MethodVisitor methodVisitor;

		NslJdk8MethodManipulator(MethodVisitor mv) {
			super(Opcodes.ASM7, null);
			this.methodVisitor = mv;
		}

		@Override
		public void visitEnd() {
			MethodVisitor mv = this.methodVisitor;
			mv.visitCode();
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

	}

	/**
	 * {@link ClassVisitor} to update the {@code NLS} class in the JDK 8 version so it
	 * doesn't use a System property to disable warning messages.
	 */
	private static class NlsJdk11Manipulator extends ClassVisitor {

		NlsJdk11Manipulator(ClassVisitor visitor) {
			super(Opcodes.ASM7, visitor);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if ("<clinit>".equals(name)) {
				return new NslJdk11MethodManipulator(super.visitMethod(access, name, desc, signature, exceptions));
			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

	}

	/**
	 * {@link MethodVisitor} to update the {@code NLS} class in the JDK 8 version so it
	 * doesn't use a System property to disable warning messages.
	 */
	private static class NslJdk11MethodManipulator extends MethodVisitor {

		private final MethodVisitor methodVisitor;

		NslJdk11MethodManipulator(MethodVisitor mv) {
			super(Opcodes.ASM7, null);
			this.methodVisitor = mv;
		}

		@Override
		public void visitEnd() {
			MethodVisitor mv = this.methodVisitor;
			mv.visitCode();
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
			mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/eclipse/osgi/util/NLS", "EMPTY_ARGS", "[Ljava/lang/Object;");
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/eclipse/osgi/util/NLS", "ignoreWarnings", "Z");
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Object");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/eclipse/osgi/util/NLS", "ASSIGNED", "Ljava/lang/Object;");
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}

	}

	enum JdkVersion {

		V8, V11

	}

}
