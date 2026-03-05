# User Profile Demo

A runnable Vaadin + Spring Boot application that shows the **User Profile** AppJar in action. Drop the AppJar into your project and your users get a complete profile screen: name, email, phone, address, password and an avatar with in-place cropping, plus an administration view where an administrator manages every profile in the system. No screens to build, no persistence layer to write.

This demo is the fastest way to see all of that working. It starts with an embedded database, seeds a handful of profiles and walks you through each view with a guided tour.

## What comes preloaded

- **Four user profiles**, one of them with an avatar picture, so both the grid and the profile view show the avatar feature right away. Every profile has its own email and phone number, which gives the filters something to filter.
- **A mix of access levels**: an administrator with a profile, regular users with profiles, and one user with no profile yet, to show the post-login redirect.
- **Two guided tours**, one per AppJar view.

The seeding runs only the first time, when the profile table is empty. Anything you create, edit or delete afterwards survives restarts.

## Prerequisites

- Java 21
- Maven 3.9 or later

Nothing else. The database is embedded and the frontend is built by the Vaadin Maven plugin.

## Run it

1. Clone this repository and enter the directory.
2. Run the application (`spring-boot:run` is the default goal):

   ```bash
   mvn
   ```

3. Wait for `Started Application` in the console. The first build downloads the Vaadin frontend and takes a few minutes.
4. Open <http://localhost:8080>.

To use another port: `mvn -Dspring-boot.run.arguments=--server.port=9090`.

## Using the demo

The landing page presents the AppJar features, the demo accounts, the free and full license models, and the entry points to the guided tours. Every view lives behind a login, so pick an account first.

### Demo accounts

Open **Log in** (or any protected view) to reach the login page. It shows one card per profile in the database, so there is nothing to type: click **Log in** on a card and you are in. Passwords match the usernames if you ever want to post the login form yourself.

| Account | Role | Profile | What it shows |
| --- | --- | --- | --- |
| `admin` (Steven Jackson) | ADMIN | yes | Both views, including the administration list |
| `clara` (Clara Lavaisse) | USER | yes, with avatar | A profile that already has an avatar picture |
| `mrivera` (Maria Rivera) | USER | yes | A plain user profile |
| `dchen` (David Chen) | USER | yes | A plain user profile |
| `johndoe` (John Doe) | USER | no | The automatic redirect to profile creation after login |

Profiles you create from the administration view get a login of their own, with the password equal to the username, and appear as new cards on the login page.

### The two views

**Profile** (`up/profile`, in the menu as *My Profile*) is the self-service view every user gets. It edits the profile of the logged-in user, or creates it if there is none. The username comes from the authentication layer and is read only. Use **Upload File...** to pick a picture (PNG, JPEG, GIF, BMP or WebP, up to 10 MB), crop it in the circular viewport and **Save**. **Delete profile** removes it.

**User Profiles list** (`up/profile-list`) is the administration view, restricted to the ADMIN role in this demo. It lists every profile with avatar, username, email and creation date, sortable server side. Filter by username, email, first name, last name or a creation date range; on small screens the filters collapse into a dialog. **New profile** opens the profile dialog, and each row has edit and delete actions.

Access control is up to the application, not the AppJar: this demo shows the list only to administrators. Log in as a regular user and open `up/profile-list` to see the access-denied page.

### Guided tours

The **Guided tour** menu in the top right of the navigation bar opens on hover and is available from every view:

- **This page** runs the tour of the view currently on screen, when it has one.
- **My Profile view** and **User Profiles list view** navigate to that view first and then start its tour.

The **Try it now** section of the landing page offers the same two tours. Tours are rendered with [Driver.js](https://driverjs.com/) and can be closed at any step.

### Free mode and the license

Without a license file the AppJar runs in **free mode**: every feature works, with a cap of **5 profiles**. The administration view shows a *Free version* badge with the profile count (`Profiles: 4 / 5`); once the cap is reached, **New profile** is disabled. A full license removes the cap and changes nothing else. The demo ships without a license, so this is what you see out of the box.

### The H2 console

The demo database is exposed at <http://localhost:8080/h2-console> so you can inspect what the AppJar persists. Connect with JDBC URL `jdbc:h2:./data/userprofiledb`, user `sa` and an empty password. The console is part of the demo only: it is reachable without authenticating, so do not copy this setup into an application of your own.

## Configuration

All settings live in `src/main/resources/application.properties`. The AppJar properties are optional: when unset, the default applies.

### View routes

| Property | Default | Description |
| --- | --- | --- |
| `com.appjars.userprofile.url.profile` | `up/profile` | Route of the current user's own profile view |
| `com.appjars.userprofile.url.profile-list` | `up/profile-list` | Route of the administration list of every profile |

### Behavior

| Property | Default | Description |
| --- | --- | --- |
| `com.appjars.userprofile.feat.redirect` | `true` | Send a user without a profile to the profile view right after login |

### Demo database

| Property | Value in the demo | Description |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:h2:./data/userprofiledb;FILE_LOCK=NO` | Embedded H2 database, stored under `./data` |
| `spring.datasource.username` / `spring.datasource.password` | `sa` / empty | Database credentials |
| `spring.h2.console.enabled` | `true` | Serves the H2 console at `/h2-console` |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creates and updates the schema on startup |
| `spring.jpa.show-sql` | `false` | Set to `true` to print the SQL the AppJar runs |

### Avatar uploads

| Property | Value in the demo | Description |
| --- | --- | --- |
| `spring.servlet.multipart.max-file-size` | `10MB` | Matches the limit the profile form enforces |
| `spring.servlet.multipart.max-request-size` | `10MB` | Maximum size of the whole upload request |

## Stopping and resetting

Stop the application with `Ctrl+C`. To start over from the preloaded data, stop it and delete the database:

```bash
rm -rf data
```

The next start recreates the schema and seeds the four profiles again.

## Learn more

- [User Profile documentation](https://docs.appjars.com/user-profile/overview/)
- [AppJars catalog](https://www.appjars.com/catalog/)
- [AppJars on GitHub](https://github.com/AppJars)
