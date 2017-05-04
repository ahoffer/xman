package app;

import java.util.List;

import org.w3c.dom.Node;

public class App {
    public static String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<!-- /**\n"
                    + " * Copyright (c) Codice Foundation\n" + " *\n"
                    + " * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either\n"
                    + " * version 3 of the License, or any later version.\n" + " *\n"
                    + " * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n"
                    + " * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at\n"
                    + " * <http://www.gnu.org/licenses/lgpl.html>.\n" + " *\n" + " **/ -->\n"
                    + "<csw:GetRecords\n" + "        xmlns:ogc=\"http://www.opengis.net/ogc\"\n"
                    + "        xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"\n"
                    + "xmlns=\"uriblahblah\"\n" + "        resultType=\"results\"\n"
                    + "        outputFormat=\"application/xml\" outputSchema=\"http://www.opengis.net/cat/csw/2.0.2\"\n"
                    + "        startPosition=\"1\" maxRecords=\"20\" service=\"CSW\" version=\"2.0.2\">\n"
                    + "    <csw:Query typeNames=\"csw:Record\">\n"
                    + "        <csw:ElementSetName>full</csw:ElementSetName>\n"
                    + "        <csw:Constraint version=\"1.1.0\">\n" + "            <ogc:Filter>\n"
                    + "                <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">\n"
                    + "                    <ogc:PropertyName>AnyText</ogc:PropertyName>\n"
                    + "                    <ogc:Literal>PlainXml *r</ogc:Literal>\n"
                    + "                </ogc:PropertyIsLike>\n" + "            </ogc:Filter>\n"
                    + "        </csw:Constraint>\n" + "    </csw:Query>\n"
                    + "    <csw:DistributedSearch hopCount=\"2\"/>\n" + "</csw:GetRecords>";

    public static void main(String args[]) {
        Xman xman = Xman.newInstance()
                .setXmlText(xmlString);
        xman.setXpathText("/GetRecords");
        String out = xman.evaluateToString();
        List<Node> nodes = xman.evaluateToNodes();
        xman.setXpathText("/csw:GetRecords/");
        out = xman.evaluateToString();
        nodes = xman.evaluateToNodes();
    }
}


