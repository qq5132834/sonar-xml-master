/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.xml.checks.maven;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DependencyWithSystemScopeCheckTest {
  @Test
  void test() {
    DependencyWithSystemScopeCheck check = new DependencyWithSystemScopeCheck();
    SonarXmlCheckVerifier.verifyIssues("pom.xml", check);
    SonarXmlCheckVerifier.verifyNoIssue("../irrelevant.xml", check);
  }

  @Test
  void createVerifier() {
    File file = new File("src\\test\\resources\\checks\\DependencyWithSystemScopeCheck\\pom.xml");

    String filePath = file.getPath();
    String content;
    try (Stream<String> lines = Files.lines(file.toPath())) {
      content = lines.collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to load content of file %s", filePath), e);
    }

    DefaultInputFile defaultInputFile = TestInputFileBuilder.create("", filePath)
            .setType(InputFile.Type.MAIN)
            .initMetadata(content)
            .setLanguage("xml")
            .setCharset(StandardCharsets.UTF_8)
            .build();


    XmlFile xmlFile;
    try {
      xmlFile = XmlFile.create(defaultInputFile);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Unable to scan xml file %s", filePath), e);
    }
    Node node = xmlFile.getNamespaceUnawareDocument();

    XPathExpression dependencyExpression = getXPathExpression("//dependencies/dependency");
    List<Node> nodeList = XmlFile.asList(evaluate(dependencyExpression, node));

    if (nodeList != null) {
      nodeList.stream().forEach(n -> {
        XmlTextRange xmlTextRange = XmlFile.nodeLocation(n);
        System.out.println("依赖规则:" + xmlTextRange.getStartLine() + "," + xmlTextRange.getEndLine());
      });
    }
  }

  public XPathExpression getXPathExpression(String expression) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      return xpath.compile(expression);
    } catch (XPathExpressionException e) {
        return null;
    }
  }

  public NodeList evaluate(XPathExpression expression, Node node) {
    try {
      return (NodeList) expression.evaluate(node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      return null;
    }
  }

}
