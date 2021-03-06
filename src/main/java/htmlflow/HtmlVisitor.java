/*
 * MIT License
 *
 * Copyright (c) 2014-16, Miguel Gamboa (gamboa.pt)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package htmlflow;

import org.xmlet.htmlapi.Element;

import java.io.PrintStream;

/**
 * An optimized version of HtmlVisitorBinder which suppresses the binder check
 * on traversal of children elements.
 * This Vistor does not have a model and thus it does not need to check whether
 * the elements have a binder, or not.
 * Since it does not have a model, then it has no object to pass to the binders.
 *
 * @author Miguel Gamboa
 *         created on 17-01-2018
 */
public class HtmlVisitor extends HtmlVisitorBinder<Object> {

    public HtmlVisitor(PrintStream out) {
        super(out, null);
    }

    @Override
    protected <U extends Element> void visitChildrem(Element<U, ?> elem) {
        elem.getChildren().forEach(item -> item.accept(this));
    }
}
