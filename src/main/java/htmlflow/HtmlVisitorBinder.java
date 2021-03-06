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
import org.xmlet.htmlapi.ElementVisitor;
import org.xmlet.htmlapi.Text;
import org.xmlet.htmlapi.TextFunction;

import java.io.PrintStream;
import java.util.List;

/**
 * Type parameter T is the type of the model bound to this Visitor.
 *
 * @author Miguel Gamboa
 *         created on 17-01-2018
 */
public class HtmlVisitorBinder<T> extends ElementVisitor<T> {

    protected final PrintStream out;
    protected final T model;
    private int depth;

    public HtmlVisitorBinder(PrintStream out, T model) {
        this.out = out;
        this.model = model;
    }

    protected final void incTabs() { depth++;}
    protected final void decTabs() { depth--;}

    @Override
    public final <U extends Element> void sharedVisit(Element<U,?> elem) {
        out.println();
        tabs();
        HtmlTags.printOpenTag(out, elem);
        incTabs();
        visitChildrem(elem);
        decTabs();
        if(HtmlTags.isVoidElement(elem)) return;
        out.println();
        tabs();
        HtmlTags.printCloseTag(out, elem);
    }

    protected <U extends Element> void visitChildrem(Element<U, ?> elem) {
        if(elem.isBound()) {
            /**
             * Some binding function append new elements and change the original
             * parent element. Thus we clone it to preserve its original structure
             * and guarantee the same behavior on next traversals.
             */
            List<Element> children = elem.cloneElem().bindTo(model).getChildren();
            children.forEach(item -> item.accept(this));
        } else {
            elem.getChildren().forEach(item -> item.accept(this));
        }

    }

    /**
     * Type parameter R is the type of property in model T.
     */
    @Override
    public <R> void visit(TextFunction<T, R, ?> text) {
        out.println();
        tabs();
        // The text element checks whether it has a model binder.
        // It returns NULL whenever there is NO model binder.
        R val = text.getValue(model);
        out.print(val);
    }

    /**
     * An optimized version of Text without binder.
     */
    @Override
    public void visit(Text text) {
        out.println();
        tabs();
        out.print(text.getValue());
    }

    /*=========================================================================*/
    /*--------------------    Auxiliary Methods    ----------------------------*/
    /*=========================================================================*/

    final void tabs(){
        for (int i = 0; i < depth; i++)
            out.print('\t');
    }
}
