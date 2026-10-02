package com.astavet.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

class ControllerRepositoryBoundaryTest {

    private static final String REPOSITORY_PACKAGE = "com.astavet.repository.";

    @Test
    void controllersDoNotReferenceRepositoriesDirectly() throws IOException {
        Path controllerDirectory = Path.of("src/main/java/com/astavet/controller");
        assertThat(controllerDirectory).exists().isDirectory();

        List<Path> controllerFiles;
        try (var paths = Files.walk(controllerDirectory)) {
            controllerFiles = paths.filter(path -> path.toString().endsWith(".java")).toList();
        }
        assertThat(controllerFiles).isNotEmpty();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("The architecture check requires a JDK").isNotNull();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
            assertThat(repositoryReferences(compiler, files.getJavaFileObjectsFromPaths(controllerFiles)))
                    .as("Controllers must call services instead of repositories")
                    .isEmpty();
        }
    }

    @Test
    void detectsImportsAndFullyQualifiedRepositoryReferences() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("The architecture check requires a JDK").isNotNull();
        JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///Example.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return """
                        import com.astavet.repository.order.OrderRepository;
                        class Example {
                            com.astavet.repository.product.ProductRepository productRepository;
                        }
                        """;
            }
        };

        assertThat(repositoryReferences(compiler, List.of(source)))
                .anyMatch(reference -> reference.contains("OrderRepository"))
                .anyMatch(reference -> reference.contains("ProductRepository"));
    }

    private static List<String> repositoryReferences(JavaCompiler compiler, Iterable<? extends JavaFileObject> sources)
            throws IOException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavacTask task = (JavacTask) compiler.getTask(null, null, diagnostics, List.of("-proc:none"), null, sources);
        TreeSet<String> references = new TreeSet<>();

        for (var unit : task.parse()) {
            new TreeScanner<Void, Void>() {
                @Override
                public Void visitMemberSelect(MemberSelectTree tree, Void unused) {
                    String name = tree.toString();
                    if (name.startsWith(REPOSITORY_PACKAGE)) {
                        references.add(unit.getSourceFile().getName() + ": " + name);
                    }
                    return super.visitMemberSelect(tree, unused);
                }
            }.scan(unit, null);
        }

        List<String> errors = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                errors.add(diagnostic.toString());
            }
        }
        assertThat(errors).as("Controller sources must parse successfully").isEmpty();
        return List.copyOf(references);
    }
}
