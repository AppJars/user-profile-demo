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
package com.appjars.userprofile.demo.view.error;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Vertical;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@AnonymousAllowed
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteNotFoundView extends RouteNotFoundError {

  static final String KEY_TITLE = "appjars.userprofiledemo.routenotfound.title";
  static final String KEY_DESC = "appjars.userprofiledemo.routenotfound.desc";

  @Override
  public int setErrorParameter(BeforeEnterEvent event,
      ErrorParameter<NotFoundException> parameter) {
    this.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Display.FLEX, FlexDirection.COLUMN,
        Vertical.MEDIUM, Gap.SMALL, Height.FULL);
    H3 title = new H3(getTranslation(KEY_TITLE));
    super.getElement().appendChild(title.getElement(),
        new Text(getTranslation(KEY_DESC)).getElement());
    return HttpServletResponse.SC_NOT_FOUND;
  }
}
