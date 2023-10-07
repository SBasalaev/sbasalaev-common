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

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import ru.nsu.sbasalaev.annotation.In;
import ru.nsu.sbasalaev.annotation.Out;

/**
 *
 * @author Sergey Basalaev
 */
@SupportedAnnotationTypes({
    "ru.nsu.sbasalaev.annotation.In",
    "ru.nsu.sbasalaev.annotation.Out"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class InOutProcessor extends ProcessorBase {

    public InOutProcessor() { }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var annotated = roundEnv.getElementsAnnotatedWithAny(annotations.toArray(TypeElement[]::new));
        for (Element e : annotated) {
            var tp = (TypeParameterElement) e;
            if (tp.getAnnotation(In.class) != null && tp.getAnnotation(Out.class) != null) {
                warning("Both @In and @Out are present on type parameter " + tp.getSimpleName(), tp);
            }
            new InOutElementVisitor(tp).visit(tp.getEnclosingElement());
        }
        return true;
    }

    private enum Variance {
        In,
        Out,
        Exact,
        Any;

        Variance opposite() {
            return switch (this) {
                case In -> Out;
                case Out -> In;
                default -> this;
            };
        }
    }

    /**
     * Visits language elements and checks given type parameter.
     */
    private final class InOutElementVisitor implements ElementVisitor<Void, Void> {

        private final TypeParameterElement typeParameter;

        InOutElementVisitor(TypeParameterElement typeParameter) {
            this.typeParameter = typeParameter;
        }

        @Override
        public Void visit(Element e, Void p) {
            return e.accept(this, p);
        }

        @Override
        public Void visitExecutable(ExecutableElement execElement, Void p) {
            for (var typeParam : execElement.getTypeParameters()) {
                typeParam.accept(this, p);
            }
            var typeVisitor = new InOutTypeVisitor(typeParameter, execElement);
            boolean isPrivate = execElement.getModifiers().contains(Modifier.PRIVATE);
            typeVisitor.visit(execElement.getReturnType(), isPrivate ? Variance.Any : Variance.Out);
            for (var parameter : execElement.getParameters()) {
                parameter.accept(this, p);
            }
            for (var thrownType : execElement.getThrownTypes()) {
                typeVisitor.visit(thrownType, isPrivate ? Variance.Any : Variance.Out);
            }
            return null;
        }

        @Override
        public Void visitPackage(PackageElement e, Void p) {
            return visitUnknown(e, p);
        }

        @Override
        public Void visitType(TypeElement typeElement, Void p) {
            for (var param : typeElement.getTypeParameters()) {
                param.accept(this, p);
            }
            var typeVisitor = new InOutTypeVisitor(typeParameter, typeElement);
            typeVisitor.visit(typeElement.getSuperclass(), Variance.Out);
            for (var iface : typeElement.getInterfaces()) {
                typeVisitor.visit(iface, Variance.Out);
            }
            for (var subClass : typeElement.getPermittedSubclasses()) {
                typeVisitor.visit(subClass, Variance.Exact);
            }
            for (var comp : typeElement.getRecordComponents()) {
                visit(comp, p);
            }
            for (var element : typeElement.getEnclosedElements()) {
                element.accept(this, p);
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterElement tpElement, Void p) {
            var typeVisitor = new InOutTypeVisitor(typeParameter, tpElement);
            for (var bound : tpElement.getBounds()) {
                typeVisitor.visit(bound, Variance.Any);
            }
            return null;
        }

        @Override
        public Void visitUnknown(Element e, Void p) {
            warning("@In and @Out checks are not implemented for " + e.getKind() + " element", e);
            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Void p) {
            var typeVisitor = new InOutTypeVisitor(typeParameter, e);
            Variance v;
            switch (e.getKind()) {
                case FIELD ->  {
                    if (e.getModifiers().contains(Modifier.PRIVATE)) {
                        v = Variance.Any;
                    } else if (e.getModifiers().contains(Modifier.FINAL)) {
                        v = Variance.Out;
                    } else {
                        v = Variance.Exact;
                    }
                }
                case PARAMETER ->  {
                    Element owner = e.getEnclosingElement();
                    if (owner.getKind() == ElementKind.CONSTRUCTOR) {
                        v = Variance.Any;
                    } else if (owner.getModifiers().contains(Modifier.PRIVATE)) {
                        v = Variance.Any;
                    } else {
                        v = Variance.In;
                    }
                }
                case RECORD_COMPONENT -> {
                    v = Variance.Out;
                }
                default -> {
                    return visitUnknown(e, p);
                }
            }
            typeVisitor.visit(e.asType(), v);
            return null;
        }
    }

    /** Visits type expressions and checks given type parameter in them. */
    private final class InOutTypeVisitor implements TypeVisitor<Void, Variance> {

        private final TypeParameterElement typeParameter;
        private final Element forElement;

        InOutTypeVisitor(TypeParameterElement typeParameter, Element forElement) {
            this.typeParameter = typeParameter;
            this.forElement = forElement;
        }

        @Override
        public Void visit(TypeMirror type, Variance v) {
            return type.accept(this, v);
        }

        @Override
        public Void visitArray(ArrayType arrayType, Variance v) {
            return visit(arrayType.getComponentType(), v);
        }

        @Override
        public Void visitDeclared(DeclaredType declaredType, Variance v) {
            Element e = declaredType.asElement();
            switch (e.getKind()) {
                case CLASS, INTERFACE -> {
                    var clazz = (TypeElement) e;
                    var params = clazz.getTypeParameters();
                    var args = declaredType.getTypeArguments();
                    if (params.size() == args.size()) {
                        for (int i=0; i < params.size(); i++) {
                            var param = params.get(i);
                            var arg = args.get(i);
                            if (param.getAnnotation(Out.class) != null) {
                                visit(arg, v);
                            } else if (param.getAnnotation(In.class) != null) {
                                visit(arg, v.opposite());
                            } else {
                                visit(arg, Variance.Any);
                            }
                        }
                        return null;
                    }
                }
            }
            for (var typeArgument : declaredType.getTypeArguments()) {
                visit(typeArgument, Variance.Any);
            }
            return null;
        }

        @Override
        public Void visitError(ErrorType t, Variance v) {
            return null;
        }

        @Override
        public Void visitExecutable(ExecutableType executableType, Variance v) {
            return visitUnknown(executableType, v);
        }

        @Override
        public Void visitIntersection(IntersectionType intersection, Variance v) {
            for (TypeMirror bound : intersection.getBounds()) {
                visit(bound, v);
            }
            return null;
        }

        @Override
        public Void visitNoType(NoType t, Variance v) {
            return null;
        }

        @Override
        public Void visitNull(NullType t, Variance v) {
            return null;
        }

        @Override
        public Void visitPrimitive(PrimitiveType t, Variance v) {
            return null;
        }

        @Override
        public Void visitTypeVariable(TypeVariable typeVar, Variance v) {
            if (typeVar.asElement().equals(typeParameter)) {
                if (typeParameter.getAnnotation(In.class) != null) {
                    switch (v) {
                        case In, Any -> { }
                        default -> error("@In type variable " + typeVar.asElement().getSimpleName()
                            + " can not be in " + v + " position", forElement);
                    }
                }
                if (typeParameter.getAnnotation(Out.class) != null) {
                    switch (v) {
                        case Out, Any -> { }
                        default -> error("@Out type variable " + typeVar.asElement().getSimpleName()
                            + " can not be in " + v + " position", forElement);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitUnion(UnionType union, Variance v) {
            for (TypeMirror alternative : union.getAlternatives()) {
                visit(alternative, v);
            }
            return null;
        }

        @Override
        public Void visitUnknown(TypeMirror type, Variance v) {
            warning("@In and @Out checks are not implemented for " + type.getKind() + " type " + type);
            return null;
        }

        @Override
        public Void visitWildcard(WildcardType wildcard, Variance v) {
            TypeMirror bound = wildcard.getExtendsBound();
            if (bound != null) {
                visit(bound, v);
            }
            bound = wildcard.getSuperBound();
            if (bound != null) {
                visit(bound, v.opposite());
            }
            return null;
        }
    }
}
