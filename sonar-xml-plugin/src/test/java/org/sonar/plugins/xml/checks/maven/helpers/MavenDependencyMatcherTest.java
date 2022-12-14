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
package org.sonar.plugins.xml.checks.maven.helpers;

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MavenDependencyMatcherTest {

  private MavenDependencyMatcher matcher;

  @Test
  void no_name_should_fail() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> new MavenDependencyMatcher("", ""));
    assertThat(e.getMessage()).isEqualTo("Invalid dependency name. Should match '[groupId]:[artifactId]' use '*' as wildcard");
  }

  @Test
  void invalid_format_should_fail() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> new MavenDependencyMatcher(":", ""));
    assertThat(e.getMessage()).isEqualTo("Invalid dependency name. Should match '[groupId]:[artifactId]' use '*' as wildcard");
  }

  @Test
  void empty_dependencies_never_match() {
    matcher = new MavenDependencyMatcher("*:log", "");
    assertNotMatch("", "", "");
  }

  @Test
  void should_handle_wildcards_for_groupId() {
    matcher = new MavenDependencyMatcher("*:log", "");
    assertMatches("a.b.c", "log");
    assertNotMatch("log", "d");
  }

  @Test
  void should_handle_wildcards_for_artifactId() {
    matcher = new MavenDependencyMatcher("log:*", "");
    assertNotMatch("a.b.c", "log");
    assertMatches("log", "d");
  }

  @Test
  void should_handle_exact_values() {
    matcher = new MavenDependencyMatcher("log:log", "");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertMatches("log", "log");
  }

  @Test
  void should_handle_fixed_versions() {
    matcher = new MavenDependencyMatcher("log:log", "1.3");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
  }

  @Test
  void should_handle_pattern_version() {
    matcher = new MavenDependencyMatcher("log:log", "1.3.*");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
    assertMatches("log", "log", "1.3-SNAPSHOT");
  }

  @Test
  void should_handle_ranged_versions() {
    matcher = new MavenDependencyMatcher("log:log", "1.2.5-2");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
  }

  private void assertNotMatch(String groupId, String artifactId) {
    assertNotMatch(groupId, artifactId, "");
  }

  private void assertNotMatch(String groupId, String artifactId, @Nullable String version) {
    assertThat(matcher.matches(groupId, artifactId, version)).isFalse();
  }

  private void assertMatches(String groupId, String artifactId) {
    assertMatches(groupId, artifactId, "");
  }

  private void assertMatches(String groupId, String artifactId, String version) {
    assertThat(matcher.matches(groupId, artifactId, version)).isTrue();
  }
}
