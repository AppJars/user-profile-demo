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

import com.appjars.userprofile.demo.view.tour.DemoTours;
import com.appjars.userprofile.demo.view.tour.DemoTours.DemoTour;
import com.appjars.userprofile.flow.view.ProfilesListView;
import com.appjars.userprofile.flow.view.UserProfileView;
import com.appjars.userprofile.service.UserProfileService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Public landing page: appjar features, demo accounts, license model and guided tours. */
@SuppressWarnings("serial")
@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.userprofiledemo.demo.home.";

  private static final String APPJARS_SITE_URL = "https://www.appjars.com";
  private static final String GITHUB_ORG_URL = "https://github.com/AppJars";
  private static final String DOCS_URL = "https://docs.appjars.com/user-profile/overview/";
  private static final String LOGIN_NEWUSER_TITLE_KEY =
      "appjars.userprofiledemo.login.newuser.title";

  private final transient UserProfileService userProfileService;

  public HomeView(UserProfileService userProfileService) {
    this.userProfileService = userProfileService;
    addClassName("home-view");
    add(createHero(), createFeaturesSection(), createTryItSection(), createLicenseSection(),
        createLinksSection());
    setAlignItems(Alignment.STRETCH);
  }

  private Component createHero() {
    Image logo = new Image("icons/icon-appjars-full.png", t("hero.title"));
    logo.addClassName("home-hero-logo");
    H1 title = new H1(t("hero.title"));
    Paragraph tagline = new Paragraph(t("hero.tagline"));
    tagline.addClassName("home-tagline");

    Div hero = new Div(logo, title, tagline);
    hero.setId("home-hero");
    hero.addClassName("home-hero");
    return hero;
  }

  private Component createFeaturesSection() {
    Div cards = new Div(
        featureCard(VaadinIcon.USER_CARD, "features.myprofile"),
        featureCard(VaadinIcon.CAMERA, "features.avatar"),
        featureCard(VaadinIcon.USERS, "features.administration"),
        featureCard(VaadinIcon.MOBILE, "features.responsive"),
        featureCard(VaadinIcon.SIGN_IN, "features.redirect"),
        featureCard(VaadinIcon.COG, "features.configurable"),
        featureCard(VaadinIcon.CONNECT, "features.events"));
    cards.addClassName("home-features");

    return section("home-features", t("features.title"), cards);
  }

  private Card featureCard(VaadinIcon icon, String key) {
    Card card = new Card();
    card.addClassName("home-feature-card");
    Icon prefix = icon.create();
    prefix.addClassName("home-feature-icon");
    card.setHeaderPrefix(prefix);
    card.setTitle(t(key + ".title"));
    card.add(new Paragraph(t(key + ".desc")));
    return card;
  }

  private Component createTryItSection() {
    Paragraph intro = new Paragraph(t("tryit.intro"));

    Div credentials = new Div(
        credentialRow(displayName("admin"), t("tryit.admin")),
        credentialRow(displayName("clara"), t("tryit.clara")),
        credentialRow(getTranslation(LOGIN_NEWUSER_TITLE_KEY), t("tryit.johndoe")));
    credentials.addClassName("home-credentials");

    Button myProfile = new Button(t("tryit.myprofile"),
        e -> getUI().ifPresent(ui -> ui.navigate(UserProfileView.class)));
    myProfile.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    Button userList = new Button(t("tryit.userlist"),
        e -> getUI().ifPresent(ui -> ui.navigate(ProfilesListView.class)));
    userList.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Div actions = new Div(myProfile, userList, createTourMenu());
    actions.addClassName("home-actions");

    Paragraph adminHint = new Paragraph(t("tryit.adminhint"));
    adminHint.addClassName("home-hint");

    return section("home-tryit", t("tryit.title"), intro, credentials, actions, adminHint);
  }

  private String displayName(String username) {
    return userProfileService.findByUsername(username).map(profile -> {
      String first = profile.getFirstName() == null ? "" : profile.getFirstName();
      String last = profile.getLastName() == null ? "" : profile.getLastName();
      String name = (first + " " + last).trim();
      return name.isEmpty() ? username : name;
    }).orElse(username);
  }

  private Div credentialRow(String credentials, String description) {
    Span code = new Span(credentials);
    code.addClassName("home-credential-code");
    Div row = new Div(code, new Span(description));
    row.addClassName("home-credential");
    return row;
  }

  private Component createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
    menu.setOpenOnHover(true);
    SubMenu tours = menu.addItem(item(VaadinIcon.MAP_MARKER, t("tour.button"))).getSubMenu();
    tours.addItem(item(VaadinIcon.USER_CARD, t("tour.myprofile")),
        e -> startViewTour(DemoTour.MY_PROFILE, UserProfileView.class));
    tours.addItem(item(VaadinIcon.USERS, t("tour.userlist")),
        e -> startViewTour(DemoTour.USER_LIST, ProfilesListView.class));
    return menu;
  }

  private static Div item(VaadinIcon icon, String label) {
    Div content = new Div(icon.create(), new Span(label));
    content.getStyle().set("display", "flex").set("align-items", "center")
        .set("gap", "var(--lumo-space-s)");
    return content;
  }

  private void startViewTour(DemoTour tour, Class<? extends Component> view) {
    VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
    getUI().ifPresent(ui -> ui.navigate(view));
  }

  private Component createLicenseSection() {
    Paragraph desc = new Paragraph(t("license.desc"));
    Anchor link = new Anchor(APPJARS_SITE_URL, t("license.link"));
    link.setTarget("_blank");
    return section("home-license", t("license.title"), desc, new Paragraph(link));
  }

  private Component createLinksSection() {
    Anchor github = new Anchor(GITHUB_ORG_URL, t("links.github"));
    github.setTarget("_blank");
    Anchor readme = new Anchor(DOCS_URL, t("links.readme"));
    readme.setTarget("_blank");
    Div links = new Div(github, readme);
    links.addClassName("home-links");
    return section("home-links", t("links.title"), links);
  }

  private Div section(String id, String title, Component... content) {
    Div section = new Div();
    section.setId(id);
    section.addClassName("home-section");
    section.add(new H3(title));
    section.add(content);
    return section;
  }

  private String t(String key) {
    return getTranslation(KEY_PREFIX + key);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
