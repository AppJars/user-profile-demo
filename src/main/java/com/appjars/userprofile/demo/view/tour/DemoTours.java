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
package com.appjars.userprofile.demo.view.tour;

import com.appjars.userprofile.flow.util.TestIds;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/** Guided tours of the appjar views, anchored to the {@code data-testid} attributes it exposes. */
public final class DemoTours {

  /** Session attribute used to start a tour after navigating (and logging in) to its view. */
  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.userprofiledemo.demo.tour.";

  /** Marker attribute the client-side resolver places on the element each step must point at. */
  private static final String TARGET_ATTR = "data-antler-target";

  // Selectors can match a hidden duplicate of the target, so each step follows the first visible
  // match instead. Takes a JSON map of {stepId: cssSelector}

  private static final String RESOLVE_TARGETS_JS =
      """
      const MAP = JSON.parse($0);
      const ATTR = 'data-antler-target';
      const resolve = () => {
        Object.keys(MAP).forEach(id => {
          let pick = null;
          for (const el of document.querySelectorAll(MAP[id])) {
            const r = el.getBoundingClientRect();
            if (r.width > 4 && r.height > 4) { pick = el; break; }
          }
          document.querySelectorAll("[" + ATTR + "='" + id + "']")
              .forEach(el => { if (el !== pick) { el.removeAttribute(ATTR); } });
          if (pick && pick.getAttribute(ATTR) !== id) { pick.setAttribute(ATTR, id); }
        });
      };
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      let scheduled = false;
      const schedule = () => { if (scheduled) return; scheduled = true;
        requestAnimationFrame(() => { scheduled = false; resolve(); }); };
      resolve();
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['hidden', 'style', 'class']});
      window.__antlerResolver = { stop() { obs.disconnect();
        document.querySelectorAll('[' + ATTR + ']').forEach(el => el.removeAttribute(ATTR));
        window.__antlerResolver = null; } };
      """;

  // Driver forces overflow:hidden on the parent of the highlighted element, clipping its siblings

  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  private static final String STOP_JS = """
      window.__antlerResolver?.stop();
      document.getElementById('demo-tour-css')?.remove();
      """;

  public enum DemoTour {
    MY_PROFILE, USER_LIST
  }

  /** A step definition; a {@code null} selector makes the step centered. */
  private record StepDef(String key, String selector, String position, boolean first,
      boolean last) {

    String id() {
      return key.replace('.', '-');
    }
  }

  private DemoTours() {}

  public static Tour create(DemoTour tour, SerializableFunction<String, String> translator) {
    // Driver.js (MIT) is set explicitly: the Shepherd.js engine is not free for commercial use
    return Tour.builder().engineType(EngineType.DRIVER)
        .steps(steps(tour).stream().map(def -> step(translator, def)).toList())
        .showCancelButton(true).allowClose(true).build();
  }

  /** Creates the tour, attaches it to {@code host} and starts it, detaching it when it ends. */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    Tour t = create(tour, translator);
    host.getElement().appendChild(t.getElement());
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(RESOLVE_TARGETS_JS, targetJson(steps(tour)));
    t.addTourCompletedListener(e -> stop(t, host));
    t.addTourCanceledListener(e -> stop(t, host));
    t.start();
  }

  private static void stop(Tour tour, Component host) {
    host.getElement().executeJs(STOP_JS);
    tour.getElement().removeFromParent();
  }

  private static List<StepDef> steps(DemoTour tour) {
    return switch (tour) {
      case MY_PROFILE -> myProfileSteps();
      case USER_LIST -> userListSteps();
    };
  }

  /** Every tour opens with a centered step, so the view is laid out before anything is anchored. */
  private static List<StepDef> myProfileSteps() {
    return List.of(
        new StepDef("myprofile.intro", null, null, true, false),
        new StepDef("myprofile.form", testId(TestIds.PROFILE_DIALOG), "bottom", false, false),
        new StepDef("myprofile.username", testId(TestIds.USERNAME_FIELD), "bottom", false, false),
        new StepDef("myprofile.avatar", "vaadin-upload", "bottom", false, false),
        new StepDef("myprofile.save", testId(TestIds.SAVE_DIALOG), "bottom", false, false),
        new StepDef("myprofile.finish", null, null, false, true));
  }

  private static List<StepDef> userListSteps() {
    return List.of(
        new StepDef("userlist.intro", null, null, true, false),
        new StepDef("userlist.grid", "vaadin-grid", "top", false, false),
        new StepDef("userlist.filters", "#top-filters", "bottom", false, false),
        new StepDef("userlist.create", testId(TestIds.NEW_PROFILE_BUTTON), "bottom", false, false),
        new StepDef("userlist.license", null, null, false, true));
  }

  private static String testId(String id) {
    return "[data-testid='" + id + "']";
  }

  private static String targetJson(List<StepDef> defs) {
    return defs.stream().filter(def -> def.selector() != null)
        .map(def -> "\"" + def.id() + "\":\"" + def.selector().replace("\\", "\\\\")
            .replace("\"", "\\\"") + "\"")
        .collect(Collectors.joining(",", "{", "}"));
  }

  private static TourStep step(SerializableFunction<String, String> t, StepDef def) {
    List<TourButton> buttons = new ArrayList<>();
    if (!def.first()) {
      buttons.add(TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
          .type(TourButtonType.PREVIOUS).build());
    }
    buttons
        .add(TourButton.builder().label(t.apply(KEY_PREFIX + (def.last() ? "btn.done" : "btn.next")))
            .type(TourButtonType.NEXT).build());
    String attachTo =
        def.selector() == null ? null : "[" + TARGET_ATTR + "='" + def.id() + "']";
    return TourStep.builder().id(def.id()).attachTo(attachTo).position(def.position())
        .title(t.apply(KEY_PREFIX + def.key() + ".title"))
        .content(t.apply(KEY_PREFIX + def.key() + ".desc")).buttons(buttons).build();
  }
}
