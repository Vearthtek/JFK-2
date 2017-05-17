package pl.edu.wat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import javax.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        final String fileName = "src\\Class.java";
        final String alteredFileName = "src\\ClassAltered.java";
        CompilationUnit cu;
        try (FileInputStream in = new FileInputStream(fileName)) {
            cu = JavaParser.parse(in);
        }

        new Rewriter().visit(cu, null);
        cu.getClassByName("Class").get().setName("ClassAltered");

        try (FileWriter output = new FileWriter(new File(alteredFileName), false)) {
            output.write(cu.toString());
        }

        File[] files = {new File(alteredFileName)};
        String[] options = {"-d", "out//production//Synthesis"};

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
            compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    Arrays.asList(options),
                    null,
                    compilationUnits).call();

            diagnostics.getDiagnostics().forEach(d -> System.out.println(d.getMessage(null)));
        }
    }

    private static class Rewriter extends VoidVisitorAdapter<Void> {

        private static final Map<String, Boolean> map = new HashMap<String, Boolean>() {{
            put("byte", false);
            put("short", false);
            put("int", false);
            put("long", false);
            put("float", false);
            put("double", false);
            put("char", false);
            put("boolean", false);
        }};

        @Override
        public void visit(MethodDeclaration n, Void arg) {BlockStmt body = n.getBody().get();
            int notObjectsCounter = 0;
            for (int i = 0; i < n.getParameters().size(); i++) {
                if (!map.containsKey(n.getParameter(i).getType().toString())) {
                    BinaryExpr be = new BinaryExpr();
                    be.setLeft(new NameExpr(n.getParameter(i).getName()));
                    be.setOperator(BinaryExpr.Operator.EQUALS);
                    be.setRight(new NullLiteralExpr());

                    ObjectCreationExpr oce = new ObjectCreationExpr();
                    oce.setType(IllegalArgumentException.class);
                    oce.addArgument(new StringLiteralExpr(n.getParameter(i).getName() + " is equal to null"));

                    ThrowStmt ts = new ThrowStmt();
                    ts.setExpression(oce);

                    IfStmt if_statement = new IfStmt();
                    if_statement.setCondition(be);
                    if_statement.setThenStmt(ts);
                    body.addStatement(notObjectsCounter++, if_statement);
                }
            }
        }
    }
}
