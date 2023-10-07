/*
 * The MIT License
 *
 * Copyright 2018, 2022 Sergey Basalaev.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ru.nsu.sbasalaev.annotation.processing;

import java.io.IOException;
import java.io.PrintWriter;
import static java.text.MessageFormat.format;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Sergey Basalaev
 */
@SupportedAnnotationTypes({"ru.nsu.sbasalaev.annotation.Is"})
@SupportedSourceVersion(SourceVersion.RELEASE_14)
public final class IsProcessor extends ProcessorBase {

    private TypeElement isAnnotationElement;
    private ExecutableElement isAnnotationValueField;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        isAnnotationElement = elements().getTypeElement("ru.nsu.sbasalaev.annotation.Is");
        if (isAnnotationElement == null) return;
        isAnnotationValueField = (ExecutableElement) isAnnotationElement
            .getEnclosedElements().stream()
            .filter(e -> e.getSimpleName().toString().equals("value"))
            .findAny().orElseThrow();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement anno : annotations) {
            var annotated = roundEnv.getElementsAnnotatedWith(anno);
            for (Element e : annotated) try {
                checkIs((TypeElement) e);
            } catch (IOException ioe) {
                error(ioe.toString());
            }
        }
        return true;
    }

    private void checkIs(TypeElement typeElement) throws IOException {
        AnnotationMirror isAnno = typeElement.getAnnotationMirrors().stream()
            .filter(a -> a.getAnnotationType().asElement().equals(isAnnotationElement))
            .findAny().orElseThrow();
        @SuppressWarnings("unchecked")
        List<AnnotationValue> typeValues = (List<AnnotationValue>) isAnno
            .getElementValues().get(isAnnotationValueField).getValue();
        List<TypeMirror> caseTypes = typeValues.stream()
            .map(v -> (TypeMirror) v.getValue())
            .collect(Collectors.toList());
        if (caseTypes.isEmpty()) {
            warning("No classes in @Is annotation", typeElement);
        }
        if (typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
            error("Сode generation for nested types is not supported", typeElement);
            return;
        }
        if (!typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(typeElement.asType() + " is not abstract", typeElement);
            return;
        }
        for (TypeMirror type : caseTypes) {
            if (!types().isAssignable(type, typeElement.asType())) {
                error(type + " is not a subtype of " + typeElement.asType(), typeElement);
                return;
            }
        }
        List<CharSequence> ofTypeNames = caseTypes.stream()
            .map(t -> ((DeclaredType) t).asElement().getSimpleName())
            .collect(Collectors.toList());
        int caseTypeCount = caseTypes.size();
        var baseName = typeElement.getSimpleName();
        String packageName;
        {
            String qname = typeElement.getQualifiedName().toString();
            int dot = qname.lastIndexOf('.');
            packageName = dot > 0 ? qname.substring(0, dot) : "";
        }
        var file = files().createSourceFile(typeElement.getQualifiedName() + "s", typeElement);
        try (var w = new PrintWriter(file.openWriter())) {
            if (!packageName.isEmpty()) {
                w.println(format("package {0};", packageName));
                w.println();
            }
            w.println("""
                import java.util.function.Function;
                import ru.nsu.sbasalaev.API;
                import ru.nsu.sbasalaev.Opt;
                """);
            w.println(format(
                """
                /** Visitors and pattern matching for {0}.
                  * WARNING: this file is generated, any changes will be lost.
                  */""", baseName));
            if (typeElement.getModifiers().contains(Modifier.PUBLIC)) {
                w.print("public ");
            }
            w.println(format("""
                final class {0}s '{'
                    private {0}s() '{ }'

                    public static <R> Match.MatchTotal{1}<R> match(Dispatcher value) '{'
                        return new Match.MatchTotal{1}<>(value, new CaseFunction<>());
                    '}'

                    public static interface Dispatcher '{'

                        <R, D> R accept(Visitor<R, D> visitor, D d);""",
                baseName, ofTypeNames.get(0)
            ));
            for (int i=0; i<caseTypeCount; i++) {
                w.println(format("""
                    \s
                            default boolean is{1}() '{'
                                return {0}s.<Boolean>match(this).if{1}_(true).otherwise_(false);
                            '}'

                            default Opt<{2}> as{1}() '{'
                                return {0}s.<Opt<{2}>>match(this)
                                    .if{1}(API::some)
                                    .otherwise(API.constant(API.none()));
                            '}'""",
                    baseName, ofTypeNames.get(i), caseTypes.get(i)
                ));
            }
            w.println("    }");

            w.println();
            w.println("    public static interface Visitor<R, D> {");
            for (int i=0; i<caseTypeCount; i++) {
                w.println(format("        R visit{0}({1} t, D d);", ofTypeNames.get(i), caseTypes.get(i)));
            }
            w.println("    }");

            w.println(format("""
                \s
                    public static interface DefaultVisitor<R, D> extends Visitor<R, D> '{'
                        R defaultAction({0} t, D d);""", typeElement
            ));
            for (int i=0; i<caseTypeCount; i++) {
                w.println(format("""
                    \s
                            @Override
                            default R visit{0}({1} t, D d) '{'
                                return defaultAction(t, d);
                            '}'""", ofTypeNames.get(i), caseTypes.get(i)
                ));
            }
            w.println("    }");

            w.println("""
                \s
                    private static final class CaseFunction<R> implements Function<Dispatcher, R>, Visitor<R, Void> {
                        private CaseFunction() { }

                        @Override
                        public R apply(Dispatcher t) {
                            return t.accept(this, null);
                        }""");
            for (int i=0; i<caseTypeCount; i++) {
                w.println(format("""
                    \s
                            private Function<? super {1}, ? extends R> if{0};

                            @Override
                            public R visit{0}({1} t, Void v) '{'
                                return if{0} != null ? if{0}.apply(t) : otherwise.apply(t);
                            '}'""", ofTypeNames.get(i), caseTypes.get(i)
                ));
            }
            w.println(format("""
                \s
                        private Function<? super {0}, ? extends R> otherwise;
                    '}'""", baseName
            ));

            w.println("""
                \s
                    public static final class Match {
                        private Match() { }""");
            for (int i=0; i < caseTypeCount-1; i++) {
                w.println(format("""
                    \s
                            public static final class MatchTotal{0}<R> extends MatchPartial{1}<R> '{'

                                private MatchTotal{0}(Dispatcher value, CaseFunction<R> function) '{'
                                    super(value, function);
                                '}'

                                public MatchTotal{1}<R> if{0}(Function<? super {2}, ? extends R> f) '{'
                                    function.if{0} = f;
                                    return new MatchTotal{1}<>(value, function);
                                '}'

                                public MatchTotal{1}<R> if{0}_(R r) '{'
                                    function.if{0} = API.constant(r);
                                    return new MatchTotal{1}<>(value, function);
                                '}'
                            '}'""", ofTypeNames.get(i), ofTypeNames.get(i+1), caseTypes.get(i)
                ));
            }
            w.println(format("""
                \s
                        public static final class MatchTotal{0}<R> extends MatchPartial<R> '{'

                            private MatchTotal{0}(Dispatcher value, CaseFunction<R> function) '{'
                                super(value, function);
                            '}'

                            public R if{0}(Function<? super {1}, ? extends R> f) '{'
                                function.if{0} = f;
                                return function.apply(value);
                            '}'

                            public R if{0}_(R r) '{'
                                function.if{0} = API.constant(r);
                                return function.apply(value);
                            '}'
                        '}'""", ofTypeNames.get(caseTypeCount-1), caseTypes.get(caseTypeCount-1)
            ));
            for (int i=1; i < caseTypeCount; i++) {
                w.println(format("""
                        \s
                                public static class MatchPartial{0}<R> extends MatchPartial{1}<R> '{'

                                    private MatchPartial{0}(Dispatcher value, CaseFunction<R> function) '{'
                                        super(value, function);
                                    '}'

                                    public MatchPartial{1}<R> if{0}(Function<? super {2}, ? extends R> f) '{'
                                        function.if{0} = f;
                                        return this;
                                    '}'

                                    public MatchPartial{1}<R> if{0}_(R r) '{'
                                        function.if{0} = API.constant(r);
                                        return this;
                                    '}'
                                '}'""", ofTypeNames.get(i)
                  , i < caseTypeCount -1 ? ofTypeNames.get(i+1) : ""
                  , caseTypes.get(i)
                ));
            }
            w.println(format("""
                \s
                        public static class MatchPartial<R> '{'

                            final Dispatcher value;
                            final CaseFunction<R> function;

                            private MatchPartial(Dispatcher value, CaseFunction<R> function) '{'
                                this.value = value;
                                this.function = function;
                            '}'

                            public R otherwise(Function<? super {0}, ? extends R> f) '{'
                                function.otherwise = f;
                                return function.apply(value);
                            '}'

                            public R otherwise_(R r) '{'
                                function.otherwise = API.constant(r);
                                return function.apply(value);
                            '}'
                        '}'""", typeElement
            ));
            w.println("    }");

            w.println('}');
            w.flush();
        }
    }
}
