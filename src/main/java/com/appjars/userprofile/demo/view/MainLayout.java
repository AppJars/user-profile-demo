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

import com.appjars.userprofile.demo.service.DemoService;
import com.appjars.userprofile.demo.view.tour.DemoTours;
import com.appjars.userprofile.demo.view.tour.DemoTours.DemoTour;
import com.appjars.userprofile.flow.view.ProfilesListView;
import com.appjars.userprofile.flow.view.UserProfileView;
import com.appjars.userprofile.model.UserProfileDto;
import com.appjars.userprofile.service.UserProfileService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

// Anonymous so the public landing page can render in this layout; beforeEnter guards the rest
@AnonymousAllowed
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainLayout extends AppLayout
    implements BeforeEnterObserver, AfterNavigationObserver {

  static final String KEY_TITLE = "appjars.userprofiledemo.mainlayout.title";
  static final String KEY_NAV_TITLE = "appjars.userprofiledemo.mainlayout.nav.title";
  static final String KEY_NAV_MAIN = "appjars.userprofiledemo.mainlayout.nav.main";
  static final String KEY_NAV_USERPROFILE = "appjars.userprofiledemo.mainlayout.nav.userprofile";
  static final String KEY_NAV_USERLIST = "appjars.userprofiledemo.mainlayout.nav.userlist";
  static final String KEY_NAV_HOME = "appjars.userprofiledemo.mainlayout.nav.home";
  static final String KEY_MENU_LOGOUT = "appjars.userprofiledemo.mainlayout.menu.logout";
  static final String KEY_MENU_LOGIN = "appjars.userprofiledemo.mainlayout.menu.login";
  static final String KEY_TOUR = "appjars.userprofiledemo.mainlayout.tour";
  static final String KEY_TOUR_THIS_PAGE = "appjars.userprofiledemo.mainlayout.tour.thispage";
  static final String KEY_TOUR_LABEL_PREFIX = "appjars.userprofiledemo.demo.home.tour.";

  final DemoService demoService;
  final UserProfileService userProfileService;

  H3 title;
  SubMenu tourSubMenu;

  public MainLayout(DemoService demoService, UserProfileService userProfileService) {
    this.demoService = demoService;
    this.userProfileService = userProfileService;

    setPrimarySection(Section.DRAWER);
    setDrawerContent();
    setHeaderContent();
  }

  private void setHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.getElement().setAttribute("aria-label", "Menu toggle");

    HorizontalLayout titleLayout = new HorizontalLayout();
    titleLayout.addClassNames(AlignItems.CENTER, Gap.XSMALL);

    titleLayout.setWidthFull();

    title = new H3(getTranslation(KEY_TITLE));

    titleLayout.add(toggle, title, createTourMenu());
    titleLayout.expand(title);

    addToNavbar(true, titleLayout);
  }

  /** Tour menu of the navigation bar: starts a tour of the current view or of a specific one. */
  private MenuBar createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addClassName("navbar-tour-menu");
    menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    menu.setOpenOnHover(true);
    tourSubMenu = menu.addItem(item(VaadinIcon.MAP_MARKER, getTranslation(KEY_TOUR))).getSubMenu();
    refreshTourMenu();
    return menu;
  }

  // Toggling the enabled state does not re-render a menu bar, so the items are rebuilt instead
  private void refreshTourMenu() {
    tourSubMenu.removeAll();
    MenuItem thisPage = tourSubMenu.addItem(
        item(VaadinIcon.LOCATION_ARROW, getTranslation(KEY_TOUR_THIS_PAGE)),
        e -> startCurrentTour());
    thisPage.setEnabled(currentTour().isPresent());
    tourSubMenu.addSeparator();

    tourSubMenu.addItem(item(VaadinIcon.USER_CARD, tourLabel("myprofile")),
        e -> startTour(DemoTour.MY_PROFILE));
    if (isAdmin()) {
      tourSubMenu.addItem(item(VaadinIcon.USERS, tourLabel("userlist")),
          e -> startTour(DemoTour.USER_LIST));
    }
  }

  private static Div item(VaadinIcon icon, String label) {
    Div content = new Div(icon.create(), new Span(label));
    content.getStyle().set("display", "flex").set("align-items", "center")
        .set("gap", "var(--lumo-space-s)");
    return content;
  }

  private String tourLabel(String view) {
    return getTranslation(KEY_TOUR_LABEL_PREFIX + view);
  }

  // A tour of another view is stashed in the session and started once that view renders
  private void startTour(DemoTour tour) {
    Class<? extends Component> target = pendingTourView(tour);
    if (target.equals(currentView())) {
      runTour(tour);
    } else {
      VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
      getUI().ifPresent(ui -> ui.navigate(target));
    }
  }

  private void startCurrentTour() {
    currentTour().ifPresent(this::runTour);
  }

  private void runTour(DemoTour tour) {
    DemoTours.start(tour, this, this::getTranslation);
  }

  private Optional<DemoTour> currentTour() {
    Class<?> current = currentView();
    return Arrays.stream(DemoTour.values()).filter(tour -> pendingTourView(tour).equals(current))
        .findFirst();
  }

  private Class<?> currentView() {
    return getContent() == null ? null : getContent().getClass();
  }

  private Class<? extends Component> pendingTourView(DemoTour tour) {
    return switch (tour) {
      case MY_PROFILE -> UserProfileView.class;
      case USER_LIST -> ProfilesListView.class;
    };
  }

  private void setDrawerContent() {
    VerticalLayout drawerLayout = new VerticalLayout();
    drawerLayout.addClassNames(Margin.NONE, Padding.NONE, AlignItems.STRETCH, Gap.XSMALL);
    drawerLayout.setSizeFull();

    Image logo = new Image("/icons/icon.png", null);
    logo.setHeight("5vh");
    logo.setWidth("5vh");

    H3 title = new H3(getTranslation(KEY_NAV_TITLE));

    Header header = new Header(logo, title);
    header.addClassNames(Display.FLEX, Gap.XSMALL, AlignItems.CENTER, Margin.MEDIUM);
    title.addClassName(Flex.GROW);

    Scroller scroller = new Scroller(getNavigation());

    Footer footer = createFooter();
    footer.getStyle().set("padding", "var(--lumo-space-s)");

    drawerLayout.add(header, scroller);
    drawerLayout.expand(scroller);

    addToDrawer(drawerLayout, footer);
  }

  private SideNav getNavigation() {
    SideNav nav = new SideNav();
    nav.addClassName(Padding.XSMALL);

    SideNavItem userProfileNav = new SideNavItem(getTranslation(KEY_NAV_MAIN));
    userProfileNav.setPrefixComponent(VaadinIcon.USER.create());
    userProfileNav.setExpanded(true);

    userProfileNav.addItem(
        new SideNavItem(getTranslation(KEY_NAV_USERPROFILE), UserProfileView.class));
    if (isAdmin()) {
      userProfileNav.addItem(
          new SideNavItem(getTranslation(KEY_NAV_USERLIST), ProfilesListView.class));
    }

    SideNavItem homeNav = new SideNavItem(getTranslation(KEY_NAV_HOME), HomeView.class);
    homeNav.setPrefixComponent(VaadinIcon.HOME.create());

    nav.addItem(homeNav, userProfileNav);

    return nav;
  }

  private Footer createFooter() {
    Footer layout = new Footer();

    Optional<Principal> userOpt =
        Optional.ofNullable(VaadinRequest.getCurrent().getUserPrincipal());

    if (userOpt.isPresent()) {
      UserProfileDto myProfile;
      Optional<UserProfileDto> profileOpt =
          userProfileService.findByUsername(userOpt.get().getName());
      if (profileOpt.isPresent()) {
        myProfile = profileOpt.get();
      } else {
        myProfile = new UserProfileDto();
        myProfile.setUsername(userOpt.get().getName());
      }
      Avatar avatar = new Avatar();
      configProfileAvatar(myProfile, avatar);

      MenuBar userMenu = new MenuBar();
      userMenu.setThemeName("tertiary-inline contrast");

      MenuItem userName = userMenu.addItem("");
      Div div = new Div();
      div.add(avatar);
      div.add(myProfile.getUsername());
      div.add(LumoIcon.DROPDOWN.create());
      div.getElement().getStyle().set("display", "flex");
      div.getElement().getStyle().set("align-items", "center");
      div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
      userName.add(div);
      userName.getSubMenu().addItem(item(VaadinIcon.SIGN_OUT, getTranslation(KEY_MENU_LOGOUT)),
          e -> demoService.logout());

      layout.add(userMenu);
    } else {
      Button loginBtn =
          new Button(getTranslation(KEY_MENU_LOGIN), VaadinIcon.SIGN_IN.create());
      loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      Anchor loginLink = new Anchor("login", loginBtn);
      loginLink.getStyle().set("text-decoration", "none");
      layout.add(loginLink);
    }

    return layout;
  }

  private void configProfileAvatar(UserProfileDto profile, Avatar avatar) {
    String name = profile.getUsername();
    if (name != null) {
      avatar.setName(name);
      avatar.setAbbreviation(name.substring(0, 1).toUpperCase());
    } else {
      avatar.setName(null);
      avatar.setAbbreviation(null);
    }
    byte[] picture = profile.getAvatar();
    if (picture != null) {
      // The appjar stores the cropped avatar as PNG
      avatar.setImageHandler(DownloadHandler.fromInputStream(event -> new DownloadResponse(
          new ByteArrayInputStream(picture), "avatar.png", "image/png", picture.length)));
    } else {
      avatar.setImage(null);
    }

    avatar.setThemeName("xsmall");
    avatar.getElement().setAttribute("tabindex", "-1");
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    if (!demoService.isAuthenticated()
        && !HomeView.class.equals(beforeEnterEvent.getNavigationTarget())) {
      beforeEnterEvent.rerouteTo(LoginView.class);
      return;
    }
    // The appjar leaves view access control to the application: here the list is admin only
    if (ProfilesListView.class.equals(beforeEnterEvent.getNavigationTarget()) && !isAdmin()) {
      beforeEnterEvent.rerouteToError(AccessDeniedException.class);
    }
  }

  private boolean isAdmin() {
    VaadinRequest request = VaadinRequest.getCurrent();
    return request != null && request.isUserInRole("ADMIN");
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    updateTitle();
    refreshTourMenu();
    startPendingTour();
  }

  private void updateTitle() {
    if (getContent() instanceof HasDynamicTitle hasTitle) {
      title.setText(hasTitle.getPageTitle());
    } else {
      title.setText(getTranslation(KEY_TITLE));
    }
  }

  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && pendingTourView(pending).equals(currentView())) {
      session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
      runTour(pending);
    }
  }
}
