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
package com.appjars.userprofile.demo.view;

import com.appjars.userprofile.model.UserProfileDto;
import com.appjars.userprofile.service.UserProfileService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import java.util.Comparator;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * Login screen of the demo. It lists one card per user profile currently in the database (so a
 * profile the evaluator adds shows up here too), plus a fixed card for a pre-created user without a
 * profile, which demonstrates the post-login redirect. Each card submits the standard Spring
 * Security form login (with the CSRF token), so the redirect and any pending guided tour still work.
 */
@SuppressWarnings("serial")
@Route("/login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.userprofiledemo.login.";
  /** Pre-created account that intentionally has no profile, to demonstrate the redirect. */
  private static final String NO_PROFILE_USERNAME = "johndoe";

  private final transient UserProfileService userProfileService;
  private final transient InMemoryUserDetailsManager userDetailsManager;

  public LoginView(UserProfileService userProfileService,
      InMemoryUserDetailsManager userDetailsManager) {
    this.userProfileService = userProfileService;
    this.userDetailsManager = userDetailsManager;

    addClassName("login-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    H1 title = new H1(getTranslation(KEY_PREFIX + "title"));
    title.addClassName("login-title");
    Paragraph intro = new Paragraph(getTranslation(KEY_PREFIX + "intro"));
    intro.addClassName("login-intro");

    Div cards = new Div();
    cards.addClassName("login-card-row");
    // One card per profile currently in the database (the demo may hold up to 5 in free mode).
    userProfileService.findAll().stream()
        .sorted(Comparator.comparing(p -> p.getUsername() == null ? "" : p.getUsername()))
        .forEach(profile -> cards.add(profileCard(profile)));
    // Plus the pre-created user without a profile, to show the post-login redirect.
    cards.add(noProfileCard());

    Div container = new Div(title, intro, cards);
    container.addClassName("login-container");
    add(container);
  }

  private Div profileCard(UserProfileDto profile) {
    String username = profile.getUsername();
    ensureLoginable(username);
    boolean admin = "admin".equals(username);
    Div card = card(admin ? VaadinIcon.USER_STAR : VaadinIcon.USER, displayName(profile),
        getTranslation(KEY_PREFIX + (admin ? "desc.admin" : "desc.user")));
    card.add(loginButton(username));
    return card;
  }

  private Div noProfileCard() {
    Div card = card(VaadinIcon.USER_CARD, getTranslation(KEY_PREFIX + "newuser.title"),
        getTranslation(KEY_PREFIX + "newuser.desc"));
    card.add(loginButton(NO_PROFILE_USERNAME));
    return card;
  }

  // Built as a flex-column Div (not vaadin-card) so the login button can be pinned to the bottom
  // with margin-top:auto and stay aligned across cards of different heights.
  private Div card(VaadinIcon icon, String title, String description) {
    Icon prefix = icon.create();
    prefix.addClassName("login-card-icon");
    Div header = new Div(prefix, new Span(title));
    header.addClassName("login-card-header");

    Paragraph desc = new Paragraph(description);
    desc.addClassName("login-card-desc");

    Div card = new Div(header, desc);
    card.addClassName("login-card");
    return card;
  }

  private Button loginButton(String username) {
    // The demo convention is password == username.
    Button loginBtn =
        new Button(getTranslation(KEY_PREFIX + "button"), e -> loginAs(username, username));
    loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    loginBtn.setWidthFull();
    return loginBtn;
  }

  private String displayName(UserProfileDto profile) {
    String first = profile.getFirstName() == null ? "" : profile.getFirstName();
    String last = profile.getLastName() == null ? "" : profile.getLastName();
    String name = (first + " " + last).trim();
    return name.isEmpty() ? profile.getUsername() : name;
  }

  /** Registers a login for the profile's username (password == username) if none exists yet. */
  private void ensureLoginable(String username) {
    if (username != null && !username.isBlank() && !userDetailsManager.userExists(username)) {
      userDetailsManager.createUser(
          User.withUsername(username).password("{noop}" + username).roles("USER").build());
    }
  }

  /**
   * Submits the standard Spring Security form login for the chosen demo account by posting a hidden
   * form (with the CSRF token) to the login processing URL, exactly as the login form would.
   */
  private void loginAs(String username, String password) {
    CsrfToken csrf = (CsrfToken) VaadinRequest.getCurrent().getAttribute(CsrfToken.class.getName());
    String csrfParam = csrf != null ? csrf.getParameterName() : "_csrf";
    String csrfToken = csrf != null ? csrf.getToken() : "";
    getUI().ifPresent(ui -> ui.getPage().executeJs(
        """
        const f = document.createElement('form');
        f.method = 'POST';
        f.action = 'login';
        const add = (n, v) => {
          const i = document.createElement('input');
          i.type = 'hidden';
          i.name = n;
          i.value = v;
          f.appendChild(i);
        };
        add('username', $0);
        add('password', $1);
        if ($2) { add($2, $3); }
        document.body.appendChild(f);
        f.submit();
        """,
        username, password, csrfParam, csrfToken));
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    // Spring Security redirects failed logins back to /login?error.
    if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
      Notification notification = Notification.show(getTranslation(KEY_PREFIX + "error"));
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }

  @Override
  public String getPageTitle() {
    return getTranslation(KEY_PREFIX + "title");
  }
}
