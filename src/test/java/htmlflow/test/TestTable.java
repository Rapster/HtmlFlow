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
package htmlflow.test;

import htmlflow.HtmlView;
import htmlflow.test.model.Priority;
import htmlflow.test.model.Status;
import htmlflow.test.model.Task;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlet.htmlapi.Body;
import org.xmlet.htmlapi.Div;
import org.xmlet.htmlapi.Head;
import org.xmlet.htmlapi.Html;
import org.xmlet.htmlapi.Table;
import org.xmlet.htmlapi.Tr;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static htmlflow.test.Utils.htmlWrite;
import static java.util.stream.Collectors.toList;
import static junit.framework.Assert.assertEquals;

/**
 * @author Miguel Gamboa
 */
public class TestTable {

    private static final Logger LOGGER = Logger.getLogger("htmlflow.test");

    @Test
    public void testSimpleTableRender() throws ParserConfigurationException, SAXException, IOException{
        /*
         * Arrange
         */
        int[][] output = {{1,2,3},{4,5,6}, {7,8,9}};
        HtmlView<?> view = HtmlTables.simpleTableView(output);
        /*
         * Act
         */
        String html = view.render();
        /*
         * Assert
         */
        assertSimpleHtmlView(html.getBytes(), output);
    }
    @Test
    public void testSimpleTableWrite() throws ParserConfigurationException, SAXException, IOException{
        /*
         * Arrange
         */
        int[][] output = {{1,2,3},{4,5,6}, {7,8,9}};
        HtmlView<?> view = HtmlTables.simpleTableView(output);
        /*
         * Act
         */
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        view
                .setPrintStream(new PrintStream(mem))
                .write();
        /*
         * Assert
         */
        assertSimpleHtmlView(mem.toByteArray(), output);
    }
    @Test
    public void testNestedTable(){
        /*
         * Arrange
         */
        Pattern NEWLINE = Pattern.compile("\n");
        Iterator<String> expected = Utils.loadLines("nestedTable.html").iterator();
        /*
         * Act
         */
        String html = HtmlTables.nestedTable.render();
        /*
         * Assert
         */
        NEWLINE
            .splitAsStream(html)
            .forEach(actual -> assertEquals(expected.next(), actual));

    }
    @Test
    public void testTableWithBinding() throws ParserConfigurationException, SAXException, IOException{
        /*
         * Act
         */
        Task t1 = new Task("ISEL MPD project", "A Java library for serializing objects in HTML.", Priority.High, Status.Progress);
        Task t2 = new Task("Special dinner", "Have dinner with someone!", Priority.Normal, Status.Completed);
        Task t3 = new Task("Manchester City - Sporting", "1/8 Final UEFA Europa League. VS. Manchester City - Sporting!", Priority.High, Status.Deferred);
        List<Task> output = Arrays.asList(t1, t2, t3);
        String html = HtmlTables.taskTableView.render(output);
        /*
         * Assert
         */
        assertTaskHtmlView(html.getBytes(), output);
    }

    @Test
    public void testTableBindingForReadmeTwice() throws IOException, ParserConfigurationException, SAXException {
        validateBindingTable(HtmlTables.taskListView);
        validateBindingTable(HtmlTables.taskListView);
    }

    static void validateBindingTable(HtmlView<Iterable<Task>> taskView){
        List<Task> dataSource = Arrays.asList(
                new Task("ISEL MPD project", "A Java library for serializing objects in HTML.", Priority.High),
                new Task("Special dinner", "Have dinner with someone!", Priority.Normal),
                new Task("Manchester City - Sporting", "1/8 Final UEFA Europa League. VS. Manchester City - Sporting!", Priority.High)
        );
        List<String> actual = htmlWrite(taskView, dataSource).collect(toList());
        Iterator<String> iter = actual.iterator();
        actual.forEach(System.out::println);
        Utils
                .loadLines("TaskList.html")
                .forEach(expected -> assertEquals(expected, iter.next()));

    }

    private static void assertTaskHtmlView(byte[] html, List<Task> output) throws UnsupportedEncodingException {
        org.w3c.dom.Element elem = Utils.getRootElement(html);
        assertEquals(Html.class.getSimpleName().toLowerCase(), elem.getNodeName());
        NodeList childNodes = elem.getChildNodes();
        assertEquals(Head.class.getSimpleName().toLowerCase(), childNodes.item(0).getNodeName());
        assertEquals(Body.class.getSimpleName().toLowerCase(), childNodes.item(2).getNodeName());
        Node div = childNodes.item(2).getChildNodes().item(5);
        assertEquals(Div.class.getSimpleName().toLowerCase(), div.getNodeName());
        Node table = div.getChildNodes().item(1);
        assertEquals(Table.class.getSimpleName().toLowerCase(), table.getNodeName());
        Node tbody = table.getChildNodes().item(1);
        for (int i = 0; i < output.size(); i++) {
            Node tr = tbody .getChildNodes().item(2 + i*2);
            assertEquals(Tr.class.getSimpleName().toLowerCase(), tr.getNodeName());
            /*
             * Check title
             */
            String title = tr.getChildNodes().item(1).getFirstChild().getNodeValue().trim();
            assertEquals(output.get(i).getTitle(), title);
            /*
             * Check task description
             */
            String desc = tr.getChildNodes().item(3).getFirstChild().getNodeValue().trim();
            assertEquals(output.get(i).getDescription(), desc);
            /*
             * Check task priority
             */
            String prio = tr.getChildNodes().item(5).getFirstChild().getNodeValue().trim();
            assertEquals(output.get(i).getPriority().toString(), prio);
        }
        // LOGGER.log(Level.INFO, mem.toString());
    }

    private static void assertSimpleHtmlView(byte[] html, int[][] output) throws UnsupportedEncodingException {
        org.w3c.dom.Element elem = Utils.getRootElement(html);
        assertEquals(Html.class.getSimpleName().toLowerCase(), elem.getNodeName());
        NodeList childNodes = elem.getChildNodes();
        assertEquals(Head.class.getSimpleName().toLowerCase(), childNodes.item(0).getNodeName());
        assertEquals(Body.class.getSimpleName().toLowerCase(), childNodes.item(2).getNodeName());
        Node div = childNodes.item(2).getChildNodes().item(5);
        assertEquals(Div.class.getSimpleName().toLowerCase(), div.getNodeName());
        Node table = div.getChildNodes().item(1);
        assertEquals(Table.class.getSimpleName().toLowerCase(), table.getNodeName());
        Node tbody = table.getChildNodes().item(1);
        for (int i = 0; i < output.length; i++) {
            Node tr = tbody.getChildNodes().item(2 + i*2);
            assertEquals(Tr.class.getSimpleName().toLowerCase(), tr.getNodeName());
            for (int j = 0; j < output[i].length; j++) {
                Node td = tr.getChildNodes().item(j*2 + 1);
                assertEquals("td", td.getNodeName());
                String val = td.getFirstChild().getNodeValue().replaceAll("\\s+","");
                assertEquals(output[i][j], Integer.parseInt(val));
            }
        }
        // LOGGER.log(Level.INFO, mem.toString());
    }
}
