/*
 * The MIT License
 *
 * Copyright 2018, 2023 Sergey Basalaev.
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

/**
 * Common goodies that I use across my projects.
 * <h2>Nullability</h2>
 * Methods in this module do not accept or return {@code null} unless
 * explicitly annotated as {@link ru.nsu.sbasalaev.annotation.Nullable }.
 * {@link ru.nsu.sbasalaev.Opt } is used for optional parameters and return types.
 */
module ru.nsu.sbasalaev.common {
    requires static java.compiler;
    requires static transitive org.checkerframework.checker.qual;

    exports ru.nsu.sbasalaev;
    exports ru.nsu.sbasalaev.annotation;
    exports ru.nsu.sbasalaev.collection;
    exports ru.nsu.sbasalaev.staque;

    provides javax.annotation.processing.Processor
        with ru.nsu.sbasalaev.annotation.processing.InOutProcessor;
}
