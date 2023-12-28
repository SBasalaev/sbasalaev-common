/*
 * The MIT License
 *
 * Copyright 2023 Sergey Basalaev
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
package me.sbasalaev.annotation.processing.variance;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

/**
 * Error and warning messages for {@code InOutProcessor}.
 *
 * @author Sergey Basalaev
 */
final class Messages {

    private Messages() { }

    /* ERRORS */

    public static String msgInAndOut(TypeParameterElement tp) {
        return "[variance] Both @In and @Out are present on type parameter " + tp.getSimpleName();
    }

    public static String msgIllegalPositionForIn(TypeVariable typeVar, Variance variance) {
        return "[variance] @In type variable " + typeVar.asElement().getSimpleName()
                + " can not be used in " + variance + " position";
    }

    public static String msgIllegalPositionForOut(TypeVariable typeVar, Variance variance) {
        return "[variance] @Out type variable " + typeVar.asElement().getSimpleName()
                + " can not be used in " + variance + " position";
    }

    /* WARNINGS */

    public static String msgUselessInExtends(TypeParameterElement tp) {
        return "[variance] extends wildcard for @In type parameter " + tp.getSimpleName();
    }

    public static String msgUselessOutSuper(TypeParameterElement tp) {
        return "[variance] super wildcard for @Out type parameter " + tp.getSimpleName();
    }

    public static String msgNotImplementedElement(Element e) {
        return "[variance] @In and @Out checks are not implemented for " + e.getKind() + " element";
    }

    public static String msgNotImplementedType(TypeMirror type) {
        return "[variance] @In and @Out checks are not implemented for " + type.getKind() + " type " + type;
    }
}
