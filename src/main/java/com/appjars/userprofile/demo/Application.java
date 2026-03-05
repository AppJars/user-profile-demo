/*-
 * #%L
 * User Profile AppJars - Demo
 * %%
 * Copyright (C) 2023 - 2026 AppJars
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.userprofile.demo;

import com.appjars.AppJarsAutoConfiguration;
import com.appjars.userprofile.UserProfileAutoConfiguration;
import com.appjars.userprofile.demo.view.MainLayout;
import com.appjars.userprofile.flow.util.RouteConfigurer;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Entry point of the demo application. */
@SpringBootApplication
@PWA(name = "Appjars User Profile Demo", shortName = "Demo")
@ComponentScan(
    basePackageClasses = {UserProfileAutoConfiguration.class, AppJarsAutoConfiguration.class})
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
@CssImport(value = "./styles/vaadin-date-time-picker.css", themeFor = "vaadin-date-time-picker")
public class Application implements AppShellConfigurator {

  private final RouteConfigurer routeConfigurer;

  public Application(RouteConfigurer routeConfigurer) {
    this.routeConfigurer = routeConfigurer;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  public void configure() {
    routeConfigurer.setViewsRouterLayout(MainLayout.class);
  }

  // @PWA alone does not set the browser tab icon
  @Override
  public void configurePage(AppShellSettings settings) {
    settings.addFavIcon("icon", "icons/icon.png", "180x180");
  }
}
