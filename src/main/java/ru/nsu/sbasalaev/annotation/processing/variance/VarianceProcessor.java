/*
 * The MIT License
 *
 * Copyright 2018, 2022, 2023 Sergey Basalaev.
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
package ru.nsu.sbasalaev.annotation.processing.variance;

import java.util.Set;
import java.util.function.*;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import static ru.nsu.sbasalaev.API.list;
import static ru.nsu.sbasalaev.API.maybe;
import ru.nsu.sbasalaev.annotation.In;
import ru.nsu.sbasalaev.annotation.Out;
import ru.nsu.sbasalaev.annotation.processing.ProcessorBase;
import static ru.nsu.sbasalaev.annotation.processing.variance.Messages.*;
import ru.nsu.sbasalaev.collection.List;
import ru.nsu.sbasalaev.collection.Map;

/**
 * Processes @In and @Out annotations in Java source.
 *
 * @author Sergey Basalaev
 */
@SupportedAnnotationTypes({
    "ru.nsu.sbasalaev.annotation.In",
    "ru.nsu.sbasalaev.annotation.Out"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class VarianceProcessor extends ProcessorBase {

    public VarianceProcessor() { }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var annotated = roundEnv.getElementsAnnotatedWithAny(annotations.toArray(TypeElement[]::new));
        for (Element e : annotated) {
            if (e instanceof TypeParameterElement tp) {
                if (tp.getAnnotation(In.class) != null && tp.getAnnotation(Out.class) != null) {
                    error(msgInAndOut(tp), tp);
                }
                new InOutElementVisitor(tp).visit(tp.getEnclosingElement());
            }
        }
        return true;
    }

    /**
     * Visits elements and checks given type parameter in their type expressions.
     */
    private final class InOutElementVisitor implements ElementVisitor<Void, Void> {

        private final TypeParameterElement checkedTypeParameter;

        InOutElementVisitor(TypeParameterElement typeParameter) {
            this.checkedTypeParameter = typeParameter;
        }

        @Override
        public Void visit(Element e, Void nil) {
            var suppressed = e.getAnnotation(SuppressWarnings.class);
            if (suppressed != null) {
                var kinds = suppressed.value();
                if (list(kinds).exists("variance"::equals)) {
                    return nil;
                }
            }
            return e.accept(this, nil);
        }

        @Override
        public Void visitExecutable(ExecutableElement execElement, Void nil) {
            for (var typeParam : execElement.getTypeParameters()) {
                visit(typeParam);
            }
            if (execElement.getModifiers().contains(Modifier.PRIVATE)) {
                return nil;
            }
            var typeVisitor = new InOutTypeVisitor(checkedTypeParameter, execElement);
            typeVisitor.visit(execElement.getReturnType(), Variance.Out);
            for (var parameter : execElement.getParameters()) {
                visit(parameter);
            }
            for (var thrownType : execElement.getThrownTypes()) {
                typeVisitor.visit(thrownType, Variance.Out);
            }
            return null;
        }

        @Override
        public Void visitPackage(PackageElement e, Void nil) {
            return visitUnknown(e, nil);
        }

        @Override
        public Void visitRecordComponent(RecordComponentElement e, Void nil) {
            var typeVisitor = new InOutTypeVisitor(checkedTypeParameter, e);
            typeVisitor.visit(e.getAccessor().getReturnType(), Variance.Out);
            return null;
        }

        @Override
        public Void visitType(TypeElement typeElement, Void nil) {
            var typeVisitor = new InOutTypeVisitor(checkedTypeParameter, typeElement);
            for (var tp : typeElement.getTypeParameters()) {
                visit(tp);
            }
            typeVisitor.visit(typeElement.getSuperclass(), Variance.Out);
            for (var iface : typeElement.getInterfaces()) {
                typeVisitor.visit(iface, Variance.Out);
            }
            for (var subClass : typeElement.getPermittedSubclasses()) {
                typeVisitor.visit(subClass, Variance.Out);
            }
            for (var comp : typeElement.getRecordComponents()) {
                visit(comp);
            }
            for (var element : typeElement.getEnclosedElements()) {
                visit(element);
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterElement tpElement, Void nil) {
            return null;
        }

        @Override
        public Void visitUnknown(Element e, Void nil) {
            warning(msgNotImplementedElement(e), e);
            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Void nil) {
            var typeVisitor = new InOutTypeVisitor(checkedTypeParameter, e);
            Variance variance;
            switch (e.getKind()) {
                case FIELD ->  {
                    if (e.getModifiers().contains(Modifier.PRIVATE)) {
                        return null;
                    } else if (e.getModifiers().contains(Modifier.FINAL)) {
                        variance = Variance.Out;
                    } else {
                        variance = Variance.Invariant;
                    }
                }
                case PARAMETER ->  {
                    Element owner = e.getEnclosingElement();
                    if (owner.getKind() == ElementKind.CONSTRUCTOR || owner.getModifiers().contains(Modifier.PRIVATE)) {
                        return null;
                    } else {
                        variance = Variance.In;
                    }
                }
                default -> {
                    return visitUnknown(e, nil);
                }
            }
            typeVisitor.visit(e.asType(), variance);
            return null;
        }
    }

    /** Visits type expressions and checks usages of given type parameter in them. */
    private final class InOutTypeVisitor implements TypeVisitor<Void, Variance> {

        /** Type parameter usage of which is checked. */
        private final TypeParameterElement checkedTypeParameter;
        /** Element types of which are checked. */
        private final Element visitedElement;

        InOutTypeVisitor(TypeParameterElement typeParameter, Element forElement) {
            this.checkedTypeParameter = typeParameter;
            this.visitedElement = forElement;
        }

        @Override
        public Void visit(TypeMirror type, Variance variance) {
            return type.accept(this, variance);
        }

        @Override
        public Void visitArray(ArrayType arrayType, Variance variance) {
            return visit(arrayType.getComponentType(), variance);
        }

        @Override
        public Void visitDeclared(DeclaredType declaredType, Variance variance) {
            TypeElement clazz = (TypeElement) declaredType.asElement();
            var params = clazz.getTypeParameters();
            var args = declaredType.getTypeArguments();
            if (params.size() == args.size()) {
                for (int i=0; i < params.size(); i++) {
                    var param = params.get(i);
                    var arg = args.get(i);
                    if (arg instanceof WildcardType wildcard) {
                        for (var extendsBound : maybe(wildcard.getExtendsBound())) {
                            if (isInTypeParameter(clazz, param)) {
                                warning(msgUselessInExtends(param), visitedElement);
                            } else {
                                visit(extendsBound, variance);
                            }
                        }
                        for (var superBound : maybe(wildcard.getSuperBound())) {
                            if (isOutTypeParameter(clazz, param)) {
                                warning(msgUselessOutSuper(param), visitedElement);
                            } else {
                                visit(superBound, variance.inverse());
                            }
                        }
                    } else if (isOutTypeParameter(clazz, param)) {
                        visit(arg, variance);
                    } else if (isInTypeParameter(clazz, param)) {
                        visit(arg, variance.inverse());
                    } else {
                        visit(arg, Variance.Invariant);
                    }
                }
                return null;
            }
            return null;
        }

        @Override
        public Void visitError(ErrorType t, Variance variance) {
            return null;
        }

        @Override
        public Void visitExecutable(ExecutableType executableType, Variance variance) {
            return visitUnknown(executableType, variance);
        }

        @Override
        public Void visitIntersection(IntersectionType intersection, Variance variance) {
            for (TypeMirror bound : intersection.getBounds()) {
                visit(bound, variance);
            }
            return null;
        }

        @Override
        public Void visitNoType(NoType t, Variance variance) {
            return null;
        }

        @Override
        public Void visitNull(NullType t, Variance variance) {
            return null;
        }

        @Override
        public Void visitPrimitive(PrimitiveType t, Variance variance) {
            return null;
        }

        @Override
        public Void visitTypeVariable(TypeVariable typeVar, Variance variance) {
            if (!typeVar.asElement().equals(checkedTypeParameter)) {
                return null;
            }
            if (checkedTypeParameter.getAnnotation(In.class) != null) {
                switch (variance) {
                    case Out, Invariant -> {
                        error(msgIllegalPositionForIn(typeVar, variance), visitedElement);
                    }
                }
            }
            if (checkedTypeParameter.getAnnotation(Out.class) != null) {
                switch (variance) {
                    case In, Invariant -> {
                        error(msgIllegalPositionForOut(typeVar, variance), visitedElement);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitUnion(UnionType union, Variance variance) {
            for (TypeMirror alternative : union.getAlternatives()) {
                visit(alternative, variance);
            }
            return null;
        }

        @Override
        public Void visitUnknown(TypeMirror type, Variance variance) {
            warning(msgNotImplementedType(type));
            return null;
        }

        @Override
        public Void visitWildcard(WildcardType wildcard, Variance variance) {
            TypeMirror bound = wildcard.getExtendsBound();
            if (bound != null) {
                visit(bound, variance);
            }
            bound = wildcard.getSuperBound();
            if (bound != null) {
                visit(bound, variance.inverse());
            }
            return null;
        }
    }

    /** Names of the type parameters with known variance. */
    private record Params(List<String> inParams, List<String> outParams) { }

    /** 
     * Java names with recognisable type parameter variance.
     */
    private static final Map<String, Params> recognisedNames = Map.<String,Params>build()
        // java.lang
        .add(Iterable.class.getName(),          new Params(list(), list("T")))
        // java.util
        .add(java.util.Iterator.class.getName(),new Params(list(), list("E")))
        .add(java.util.Optional.class.getName(),new Params(list(), list("T")))
        // java.util.function
        .add(BiConsumer.class.getName(),        new Params(list("T","U"),  list()))
        .add(BiFunction.class.getName(),        new Params(list("T","U"),  list("R")))
        .add(BiPredicate.class.getName(),       new Params(list("T","U"),  list()))
        .add(Consumer.class.getName(),          new Params(list("T"),      list()))
        .add(DoubleFunction.class.getName(),    new Params(list(),         list("R")))
        .add(Function.class.getName(),          new Params(list("T"),      list("R")))
        .add(IntFunction.class.getName(),       new Params(list(),         list("R")))
        .add(LongFunction.class.getName(),      new Params(list(),         list("R")))
        .add(ObjDoubleConsumer.class.getName(), new Params(list("T"),      list()))
        .add(ObjIntConsumer.class.getName(),    new Params(list("T"),      list()))
        .add(ObjLongConsumer.class.getName(),   new Params(list("T"),      list()))
        .add(Predicate.class.getName(),         new Params(list("T"),   list()))
        .add(Supplier.class.getName(),          new Params(list(),         list("T")))
        .add(ToDoubleBiFunction.class.getName(),new Params(list("T", "U"), list()))
        .add(ToDoubleFunction.class.getName(),  new Params(list("T"),      list()))
        .add(ToIntBiFunction.class.getName(),   new Params(list("T", "U"), list()))
        .add(ToIntFunction.class.getName(),     new Params(list("T"),      list()))
        .add(ToLongBiFunction.class.getName(),  new Params(list("T", "U"), list()))
        .add(ToLongFunction.class.getName(),    new Params(list("T"),      list()))
        .toMap();

    /**
     * Whether this type parameter is regarded as @Out type parameter.
     * In addition to the type parameters explicitly marked as such, we
     * recognise some type parameters of {@code java.util.function.*} classes.
     */
    private static boolean isOutTypeParameter(TypeElement clazz, TypeParameterElement typeParameter) {
        if (typeParameter.getAnnotation(Out.class) != null) {
            return true;
        }
        for (var params : recognisedNames.get(clazz.getQualifiedName().toString())) {
            return params.outParams.exists(typeParameter.getSimpleName().toString()::equals);
        }
        return false;
    }

    /**
     * Whether this type parameter is regarded as @In type parameter.
     * In addition to the type parameters explicitly marked as such, we
     * recognise some type parameters of {@code java.util.function.*} classes.
     */
    private static boolean isInTypeParameter(TypeElement clazz, TypeParameterElement typeParameter) {
        if (typeParameter.getAnnotation(In.class) != null) {
            return true;
        }
        for (var params : recognisedNames.get(clazz.getQualifiedName().toString())) {
            return params.inParams.exists(typeParameter.getSimpleName().toString()::equals);
        }
        return false;
    }
}
