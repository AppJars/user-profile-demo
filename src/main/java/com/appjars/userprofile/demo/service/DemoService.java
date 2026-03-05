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
package com.appjars.userprofile.demo.service;

import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DemoService {

  final AuthenticationContext authenticationContext;

  public DemoService(AuthenticationContext authenticationContext,
      DemoInitializerService demoDataInitializer) {
    this.authenticationContext = authenticationContext;

    demoDataInitializer.initUserProfiles();
  }

  public Optional<String> getUsername() {
    return authenticationContext.getPrincipalName();
  }

  public boolean isAuthenticated() {
    return authenticationContext.isAuthenticated();
  }

  public void logout() {
    authenticationContext.logout();
  }

  @Bean
  public InMemoryUserDetailsManager getDemoUserDetails() {
    UserDetails[] usersDetails = new UserDetails[] {
        User.builder().username("admin").password("{noop}admin").roles("ADMIN").build(),

        User.builder().username("clara").password("{noop}clara").roles("USER").build(),

        // Pre-created user WITHOUT a profile ("John Doe"): logging in triggers the post-login
        // redirect to create one. Logins for user-added profiles are registered on demand by
        // the login view (password == username).
        User.builder().username("johndoe").password("{noop}johndoe").roles("USER").build(),};

    return new InMemoryUserDetailsManager(usersDetails);
  }

}
