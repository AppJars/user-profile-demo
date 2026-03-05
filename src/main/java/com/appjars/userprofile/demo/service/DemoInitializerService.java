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

import com.appjars.userprofile.model.UserProfileDto;
import com.appjars.userprofile.service.UserProfileService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DemoInitializerService {

  static final String AVATAR_RESOURCE = "/demo-data/avatar-sample.png";

  final UserProfileService userProfileService;

  public DemoInitializerService(UserProfileService userProfileService) {
    this.userProfileService = userProfileService;
  }

  @Transactional
  public void initUserProfiles() {
    if (!userProfileService.findAll().isEmpty()) {
      return;
    }
    Instant now = Instant.now();
    UserProfileDto[] userProfiles = new UserProfileDto[] {
        UserProfileDto.builder().username("admin").firstName("Steven").lastName("Jackson")
            .phoneNumber("+1-555-0100").address("1 Market Street, Suite 300")
            .email("steven.jackson@example.com").creationTime(now).build(),

        UserProfileDto.builder().username("clara").firstName("Clara").lastName("Lavaisse")
            .phoneNumber("+1-555-0142").address("45 Rivergate Avenue, Apt. 12")
            .email("clara.lavaisse@example.com").avatar(sampleAvatar()).creationTime(now).build(),

        UserProfileDto.builder().username("mrivera").firstName("Maria").lastName("Rivera")
            .phoneNumber("+1-555-0177").address("8 Kingsway Road")
            .email("maria.rivera@example.org").creationTime(now).build(),

        UserProfileDto.builder().username("dchen").firstName("David").lastName("Chen")
            .phoneNumber("+1-555-0198").address("220 Cedar Lane, Floor 2")
            .email("david.chen@example.net").creationTime(now).build()};

    for (UserProfileDto dto : userProfiles) {
      userProfileService.save(dto);
    }
  }

  private byte[] sampleAvatar() {
    try (InputStream in = getClass().getResourceAsStream(AVATAR_RESOURCE)) {
      return in == null ? null : in.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
