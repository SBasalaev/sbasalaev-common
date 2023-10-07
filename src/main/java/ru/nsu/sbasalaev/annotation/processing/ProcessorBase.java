/*
 * The MIT License
 *
 * Copyright 2018 Sergey Basalaev.
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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author Sergey Basalaev
 */
abstract class ProcessorBase extends AbstractProcessor {

    protected Filer files() {
        return processingEnv.getFiler();
    }

    protected Elements elements() {
        return processingEnv.getElementUtils();
    }

    protected Types types() {
        return processingEnv.getTypeUtils();
    }

	protected void error(String message, Element e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
	}

	protected void error(String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
	}

	protected void warning(String message, Element e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, e);
	}

	protected void warning(String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
	}

	protected void message(String message, Element e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, e);
	}

	protected void message(String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
	}
}
