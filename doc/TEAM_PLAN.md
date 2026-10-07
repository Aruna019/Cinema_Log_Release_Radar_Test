# แผนการทำงานของทีม — Cinema Log & Release Radar

กลุ่ม Section 03 · repo: `github.com/Akiixz/Cinema_Log_Release_Radar`

| คน | Branch | ส่วนที่รับผิดชอบ (รายชื่อไฟล์อยู่ในแผน commit ด้านล่าง) |
|---|---|---|
| ปวริศา 673380592-3 | `Pawarisa_673380592-3_03` | ตั้งโปรเจกต์, Security/Login, Profile, Reminder, Notification, Error handling, Layout/Nav, Docker/CI/Deploy |
| ปรายฝน 673380591-5 | `Prayfon_673380591-5_03` | Movie, Genre, TMDB (Adapter), ค้นหา/กรอง/แบ่งหน้า (Builder), แนะนำหนัง, หน้า Films/Catalog/Movie, CSS + doodles |
| นันทพร 673380409-0 | `Nunthaporn_673380409-0_03` | Diary (CRUD), Watchlist, Like, Collection (CRUD), Review, Stats, หน้า Diary/Library/Collections |

> ใครเป็นคน commit ไฟล์ไหน ดูจากแผน commit ของแต่ละคนในหัวข้อ 4–6

---

## 1. ปวริศาต้องเตรียมอะไรก่อน (ทำก่อนเพื่อนเริ่ม)

1. **ติดตั้งเครื่องมือ** (ทุกคนต้องมี)
   - JDK 17 ขึ้นไป (Temurin)
   - IntelliJ IDEA (มี Maven ในตัว) หรือ Maven 3.9
   - Git
   - PostgreSQL 16 **หรือ** Docker Desktop (แนะนำ Docker: ไม่ต้องลง Postgres เอง)
2. **สร้างฐานข้อมูลในเครื่อง** (ถ้าไม่ใช้ Docker)
   ```sql
   -- เปิด psql ด้วย user postgres
   CREATE USER cinemalog WITH PASSWORD 'cinemalog';
   CREATE DATABASE cinemalog OWNER cinemalog;
   ```
   ไม่ต้องสร้างตารางเอง Flyway สร้างให้ตอนรันแอปครั้งแรก (V1–V7)
   ถ้าใช้ Docker: `docker compose up db` ได้ฐานข้อมูลพร้อมใช้ทันที
3. **TMDB API key ที่ขอไว้แล้ว**
   - ห้ามใส่ key ลงในโค้ดหรือ commit เด็ดขาด ให้ใส่เป็น environment variable ชื่อ `TMDB_API_KEY`
   - IntelliJ: Run → Edit Configurations → Environment variables → `TMDB_API_KEY=xxxx`
   - ใช้ได้ทั้ง "API Key" (v3, 32 ตัวอักษร) และ "API Read Access Token" (v4, ยาว) โค้ดเลือกวิธีส่งให้เอง
   - ส่ง key ให้เพื่อนทาง LINE ส่วนตัว ไม่ใช่ใน repo
4. **ตั้ง git ให้ตรงกับบัญชี GitHub ของตัวเอง** (ทุกคนทำในเครื่องตัวเอง)
   ```bash
   git config --global user.name  "ชื่อบัญชี GitHub"
   git config --global user.email "อีเมลที่ผูกกับ GitHub"
   ```
5. **ตั้งค่า repo**: Settings → Collaborators → เชิญอาจารย์ (หรือตั้ง repo เป็น Public) และเพิ่มเพื่อนทั้งสองคน
6. **สมัครไว้ก่อน ใช้ตอน deploy**: [Render](https://render.com) (รัน Docker ฟรี) + [Supabase](https://supabase.com) (PostgreSQL ฟรี)
7. **ดูว่า branch ตอนนี้มีอะไรแล้ว** (แต่ละ branch มี 4 commits อยู่แล้ว)
   ```bash
   git fetch --all
   git log --oneline origin/develop
   git log --oneline origin/develop..origin/Pawarisa_673380592-3_03
   ```
   ถ้ามีไฟล์เก่าที่ซ้ำกับโค้ดชุดนี้ (เช่น pom.xml) ให้ใช้ของชุดนี้แทน แล้ว commit เป็น `refactor: replace initial skeleton`

---

## 2. วิธีทำงานกับ git (ทุกคนทำเหมือนกัน)

```bash
git clone https://github.com/Akiixz/Cinema_Log_Release_Radar.git
cd Cinema_Log_Release_Radar
git checkout Pawarisa_673380592-3_03          # ← เปลี่ยนเป็น branch ของตัวเอง
git pull origin develop                        # เอางานล่าสุดของเพื่อนมาก่อนเริ่มทุกครั้ง

# คัดลอกไฟล์ของ commit นั้นจากโฟลเดอร์ที่ได้รับ ไปวางที่ path เดียวกันใน repo
git add <ไฟล์ของ commit นั้น>
git commit -m "feat: add release reminder API"
git push origin Pawarisa_673380592-3_03
```

**ส่งงานเข้า develop**: GitHub → Pull requests → New → base `develop` ← compare `branch ตัวเอง` → ใส่ Reviewer เป็นเพื่อนอย่างน้อย 1 คน → เพื่อนกด Approve → Merge

**กฎ**
- commit และ push ด้วยบัญชีตัวเองเท่านั้น ห้ามฝากเพื่อน
- อย่า commit รวดเดียว ให้ทยอย 1–3 commits ต่อวัน จนครบอย่างน้อย 15 ครั้ง (แผนด้านล่างมี 16–21 commits ต่อคน)
- ข้อความ commit ใช้รูปแบบ `<type>: <สิ่งที่ทำ>` เช่น `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `style:`, `chore:`
- ส่วน `main`: merge จาก `develop` ผ่าน PR เฉพาะเวอร์ชันที่ส่งอาจารย์

---

## 3. ลำดับการรวมงาน (สำคัญ: โค้ดของแต่ละคนพึ่งพากัน)

| รอบ | ใคร | ทำอะไร | ต้องรอ |
|---|---|---|---|
| 1 | ปวริศา | โครงโปรเจกต์ + error handling | — |
| 2 | ปรายฝน | Movie, Genre, TMDB, sync | รอบ 1 merge แล้ว |
| 3 | ปวริศา | User, Profile, Security, Login pages, layout | รอบ 2 (ใช้ `Genre`) |
| 4 | นันทพร | ตาราง/entity/service/API ของ Diary, Watchlist, Like, Collection, Review | รอบ 3 (ใช้ `User`, `Movie`) |
| 5 | ปวริศา | Reminder + Notification (patterns), หน้า Profile | รอบ 4 (ฟัง `DiaryEntryLoggedEvent`) |
| 6 | ปรายฝน | ค้นหา, แนะนำหนัง, Movie API, หน้า Films/Catalog/Movie, CSS | รอบ 4–5 |
| 7 | นันทพร | Library state/stats, หน้า Diary/Library/Collections | รอบ 5 |
| 8 | ทุกคน | Tests + เอกสาร; ปวริศา Docker/CI/Deploy | ทุกรอบ |

หลังแต่ละรอบ merge แล้ว คนถัดไป `git pull origin develop` ก่อนเริ่ม
ถ้า Flyway ฟ้องว่า migration ลำดับไม่ตรง (เพราะไฟล์ SQL มาไม่เรียงเลข) ให้ล้าง DB ในเครื่องแล้วรันใหม่: `docker compose down -v` หรือ `DROP DATABASE cinemalog; CREATE DATABASE cinemalog OWNER cinemalog;`
หน้าเว็บ (JS) จะทำงานครบเมื่อรวมงานทุกคนแล้ว ระหว่างทางทดสอบผ่าน Swagger ได้

Path ย่อ: `java/…` = `code/src/main/java/com/cinemalog/…` · `res/…` = `code/src/main/resources/…` · `test/…` = `test/java/com/cinemalog/…`

---

## 3.5 สร้างโปรเจกต์จาก Spring Initializr (ปวริศา, commit 1)

เปิด https://start.spring.io แล้วตั้งค่า:

| ช่อง | ค่า |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | **4.1.1** (ตัวที่ไม่มีคำว่า SNAPSHOT/M) |
| Group | `com.cinemalog` |
| Artifact | `cinema-log` |
| Name | `cinema-log` (จะได้คลาส `CinemaLogApplication` ตรงกับของเรา) |
| Description | Personal movie diary and release reminder system (CP353002) |
| Package name | `com.cinemalog` |
| Packaging | Jar |
| Configuration | YAML |
| Java | 17 |

Dependencies (กด ADD DEPENDENCIES): Spring Web, Thymeleaf, Spring Security, Validation, Spring Data JPA, PostgreSQL Driver, Flyway Migration, Java Mail Sender, Spring Boot Actuator
(Swagger/springdoc ไม่มีใน Initializr เราเพิ่มเองใน `pom.xml` ของ commit 1b)

กด **GENERATE** → แตก zip → เปลี่ยนชื่อโฟลเดอร์ `cinema-log` เป็น `code` แล้ววางไว้ที่ root ของ repo
ลบ `code/src/test/` ทิ้ง (เทสต์ของเราอยู่ในโฟลเดอร์ `test/` ตามที่วิชากำหนด และเทสต์ที่ Initializr สร้างจะ fail เพราะต้องต่อฐานข้อมูล) แล้ว commit
ไฟล์ `mvnw`, `mvnw.cmd`, `.mvn/` เก็บไว้ ทำให้รัน `.\mvnw.cmd spring-boot:run` ได้โดยไม่ต้องลง Maven

## 4. แผน commit — ปวริศา (23 commits)

**รอบ 1**
0. `chore: initial commit with gitignore` — `.gitignore`, `.env.example` (ทำบน `main` ก่อนสร้าง `develop`)
1. `chore: generate project with Spring Initializr` — ทั้งโฟลเดอร์ `code/` ที่ได้จาก start.spring.io (ลบ `code/src/test/` ออกก่อน) ดูการตั้งค่าในหัวข้อ 3.5 ด้านบน
1b. `chore: configure dependencies, settings and README` — แทนที่ `code/pom.xml` ด้วยของ zip, ลบ `code/src/main/resources/application.yaml` (หรือ `.properties`) ของ Initializr แล้วใส่ `res/application.yml`, แทนที่ `java/CinemaLogApplication.java` (ของเราเพิ่ม `@EnableScheduling` กับ `@ConfigurationPropertiesScan`) + `README.md`
2. `feat: add app properties and shared clock` — `java/config/AppProperties.java`, `java/config/ClockConfig.java`
3. `feat: add API exceptions and global error handler` — `java/exception/*` (7 ไฟล์), `java/dto/response/ErrorResponse.java`
4. `feat: add current user abstraction and swagger config` — `java/common/CurrentUserProvider.java`, `java/config/OpenApiConfig.java`

**รอบ 3**

5. `feat: add users and profiles with favorite genres` — `res/db/migration/V1__create_users_and_profiles.sql`, `V4__create_reminders_and_notifications.sql`, `java/domain/enums/AvatarStyle.java`, `AvatarColor.java`, `java/domain/entity/User.java`, `UserProfile.java`, `java/repository/UserRepository.java`
6. `feat: add registration and profile services` — `java/dto/request/RegisterRequest.java`, `UpdateProfileRequest.java`, `FavoriteGenresRequest.java`, `java/dto/response/UserProfileResponse.java`, `java/mapper/UserMapper.java`, `java/service/AuthService.java`, `UserProfileService.java`, `java/service/impl/AuthServiceImpl.java`, `UserProfileServiceImpl.java`
7. `feat: add session security with onboarding redirect` — `java/config/SecurityConfig.java`, `java/security/*` (4 ไฟล์)
8. `feat: add auth and profile endpoints` — `java/controller/api/AuthController.java`, `UserController.java`
9. `feat: add base layout and frontend core` — `res/templates/fragments/layout.html`, `res/templates/error.html`, `res/static/js/core/dom.js`, `http.js`, `actions.js`, `res/static/js/app.js`
10. `feat: add login, register and onboarding pages` — `res/templates/auth/*` (3 ไฟล์), `java/controller/web/AuthPageController.java`, `CurrentUserModelAdvice.java`, `SessionUserView.java`, `res/static/js/pages/auth.js`, `res/static/js/api/account.js`
11. `feat: seed demo accounts` — `res/db/migration/V6__seed_demo_users.sql`

**รอบ 5**

12. `feat: add reminder entity with state pattern` — `java/domain/enums/ReminderStatus.java`, `NotificationChannel.java`, `java/domain/entity/Reminder.java`, `java/repository/ReminderRepository.java`
13. `feat: add notifications and reminder mapper` — `java/domain/enums/NotificationType.java`, `java/domain/entity/Notification.java`, `java/repository/NotificationRepository.java`, `java/dto/response/NotificationResponse.java`, `ReminderResponse.java`, `java/mapper/ReminderMapper.java`, `java/service/NotificationService.java`, `java/service/impl/NotificationServiceImpl.java`
14. `feat: add notification strategies, template method and factory` — `java/service/notification/NotificationMessage.java`, `NotificationSender.java`, `AbstractNotificationSender.java`, `InAppNotificationSender.java`, `EmailNotificationSender.java`, `NotificationSenderFactory.java`
15. `feat: dispatch due reminders through events` — `java/domain/event/ReminderDueEvent.java`, `java/service/ReminderDispatchService.java`, `java/service/impl/ReminderDispatchServiceImpl.java`, `java/service/notification/NotificationEventListener.java`, `java/service/job/ReminderJob.java`
16. `feat: add release reminder and notification endpoints` — `java/dto/request/ReminderRequest.java`, `java/service/ReminderService.java`, `java/service/impl/ReminderServiceImpl.java`, `java/controller/api/ReminderController.java`, `NotificationController.java`
17. `feat: add profile page, nav dropdowns and remind-me modal` — `res/templates/profile/profile.html`, `java/controller/web/ProfilePageController.java`, `res/static/js/pages/profile.js`, `res/static/js/components/nav.js`, `remind-modal.js`, `profile-modal.js`

**รอบ 8**

18. `test: add reminder state, service and dispatch tests` — `test/domain/ReminderStatusTest.java`, `test/service/ReminderServiceImplTest.java`, `ReminderDispatchServiceImplTest.java`, `test/service/impl/ReminderDispatchServiceImplTestAccess.java`
19. `test: add notification, auth and reminder API tests` — `test/service/notification/*` (2 ไฟล์), `test/service/AuthServiceImplTest.java`, `test/controller/ReminderControllerTest.java`
20. `chore: add Docker, docker-compose and CI pipeline` — `Dockerfile`, `docker-compose.yml`, `.dockerignore`, `.github/workflows/ci.yml`, `test/reports/README.md`
21. `docs: add SOLID analysis, pattern table and diagrams` — `doc/solid-analysis.md`, `doc/design-patterns.md`, `doc/diagrams/01-use-case.md`, `07-component-deployment.md`, `08-state-diagram.md`, `doc/slide/README.md`

## 5. แผน commit — ปรายฝน (17 commits)

**รอบ 2**
1. `feat: add genre and movie schema` — `res/db/migration/V2__create_movies_and_genres.sql`, `java/domain/entity/Genre.java`, `Movie.java`, `java/domain/model/ExternalMovie.java`
2. `feat: add movie repositories and sort options` — `java/repository/GenreRepository.java`, `MovieRepository.java`, `java/domain/enums/MovieSortOption.java`, `MovieListType.java`
3. `feat: add TMDB settings and HTTP client` — `java/config/TmdbProperties.java`, `TmdbClientConfig.java`, `java/dto/external/tmdb/*` (6 ไฟล์), `java/service/external/tmdb/TmdbClient.java`
4. `feat: adapt TMDB responses to MovieCatalogSource` — `java/service/external/MovieCatalogSource.java`, `java/service/external/tmdb/TmdbMovieCatalogAdapter.java`
5. `feat: add movie DTOs and mapper` — `java/dto/response/MovieSummaryResponse.java`, `MovieDetailResponse.java`, `GenreResponse.java`, `PageResponse.java`, `java/mapper/MovieMapper.java`
6. `feat: sync catalog from TMDB on startup and every 6 hours` — `java/service/MovieSyncService.java`, `java/service/impl/MovieSyncServiceImpl.java`, `java/service/job/MovieSyncJob.java`
7. `feat: seed genres and starter movies` — `res/db/migration/V5__seed_genres_and_movies.sql`

**รอบ 6**

8. `feat: build catalog search with specification builder` — `java/repository/specification/MovieSpecificationBuilder.java`, `java/dto/request/MovieSearchCriteria.java`, `java/service/MovieQueryService.java`, `java/service/impl/MovieQueryServiceImpl.java`
9. `feat: add discovery shelves and recommendations` — `java/service/MovieDiscoveryService.java`, `GenreService.java`, `java/service/impl/MovieDiscoveryServiceImpl.java`, `GenreServiceImpl.java`
10. `feat: add movie and genre endpoints` — `java/controller/api/MovieController.java`, `GenreController.java`
11. `style: add hand-drawn design system and doodles` — `res/static/css/app.css`, `res/static/js/core/doodles.js`, `res/static/img/logo.svg`, `img/*.svg`
12. `feat: add movie cards, action menu and search popover` — `res/static/js/components/movie-card.js`, `movie-menu.js`, `search.js`, `res/static/js/api/movies.js`
13. `feat: add films home and catalog pages` — `res/templates/films/films.html`, `catalog.html`, `java/controller/web/FilmsPageController.java`, `res/static/js/pages/films.js`, `catalog.js`
14. `feat: add movie detail page with reviews and similar films` — `res/templates/films/movie.html`, `res/static/js/pages/movie.js`

**รอบ 8**

15. `test: add TMDB adapter and specification builder tests` — `test/service/external/TmdbMovieCatalogAdapterTest.java`, `test/repository/MovieSpecificationBuilderTest.java`, `test/repository/specification/MovieSpecificationBuilderAccess.java`
16. `test: add movie service and controller tests` — `test/service/MovieQueryServiceImplTest.java`, `MovieDiscoveryServiceImplTest.java`, `test/service/impl/MovieDiscoveryServiceImplAccess.java`, `test/controller/MovieControllerTest.java`
17. `docs: add class and sequence diagrams` — `doc/diagrams/03-class-diagram.md`, `04-sequence-diagrams.md`

## 6. แผน commit — นันทพร (18 commits)

**รอบ 4**
1. `feat: add diary, watchlist, likes and collections schema` — `res/db/migration/V3__create_library_tables.sql`
2. `feat: add library entities` — `java/domain/enums/WatchPlace.java`, `java/domain/entity/WatchedMovie.java`, `WatchlistItem.java`, `LikedMovie.java`, `MovieCollection.java`, `CollectionMovie.java`
3. `feat: add library repositories` — `java/repository/WatchedMovieRepository.java`, `WatchlistItemRepository.java`, `LikedMovieRepository.java`, `MovieCollectionRepository.java`
4. `feat: add watchlist and like services` — `java/service/WatchlistService.java`, `LikeService.java`, `java/service/impl/WatchlistServiceImpl.java`, `LikeServiceImpl.java`
5. `feat: add diary service with query/command split` — `java/dto/request/DiaryEntryRequest.java`, `UpdateDiaryEntryRequest.java`, `java/dto/response/DiaryEntryResponse.java`, `ReviewResponse.java`, `java/mapper/DiaryMapper.java`, `java/domain/event/DiaryEntryLoggedEvent.java`, `java/service/DiaryQueryService.java`, `DiaryCommandService.java`, `java/service/impl/DiaryServiceImpl.java`
6. `feat: add collections service` — `java/dto/request/CollectionRequest.java`, `java/dto/response/CollectionSummaryResponse.java`, `CollectionDetailResponse.java`, `java/mapper/CollectionMapper.java`, `java/service/CollectionService.java`, `java/service/impl/CollectionServiceImpl.java`
7. `feat: add reviews service` — `java/dto/response/ReviewListResponse.java`, `java/service/ReviewService.java`, `java/service/impl/ReviewServiceImpl.java`
8. `feat: add diary, collection, watchlist, like and review endpoints` — `java/controller/api/DiaryController.java`, `CollectionController.java`, `WatchlistController.java`, `LikeController.java`, `ReviewController.java`

**รอบ 7**

9. `feat: add library state and profile stats` — `java/dto/response/LibraryStateResponse.java`, `StatsResponse.java`, `java/service/LibraryService.java`, `java/service/impl/LibraryServiceImpl.java`, `java/controller/api/LibraryController.java`
10. `feat: add shared UI kit and library state cache` — `res/static/js/core/ui.js`, `state.js`, `res/static/js/api/library.js`
11. `feat: add log, rate and review modal with like/watchlist actions` — `res/static/js/components/log-modal.js`, `library-actions.js`
12. `feat: add diary calendar page and day pop-up` — `res/templates/library/diary.html`, `java/controller/web/LibraryPageController.java`, `res/static/js/pages/diary.js`, `res/static/js/components/day-popup.js`
13. `feat: add watched, watchlist and liked pages` — `res/templates/library/list.html`, `res/static/js/pages/list.js`, `library-parts.js`
14. `feat: add collections pages and picker` — `res/templates/library/collections.html`, `collection.html`, `res/static/js/pages/collections.js`, `res/static/js/components/collect-modal.js`
15. `feat: seed demo diary and collections` — `res/db/migration/V7__seed_demo_library.sql`

**รอบ 8**

16. `test: add diary and collection service tests` — `test/service/DiaryServiceImplTest.java`, `CollectionServiceImplTest.java`, `test/domain/MovieCollectionTest.java`
17. `test: add watchlist, library and collection API tests` — `test/service/WatchlistServiceImplTest.java`, `LibraryServiceImplTest.java`, `test/controller/CollectionControllerTest.java`, `test/resources/application-test.yml`
18. `docs: add domain model, activity, ER diagram and data dictionary` — `doc/diagrams/02-domain-model.md`, `05-activity-diagram.md`, `06-er-diagram.md`, `doc/data-dictionary.md`

---

## 7. รันและทดสอบ

```bash
docker compose up --build          # ทั้งแอป + ฐานข้อมูล → http://localhost:8080
# หรือ
cd code && mvn spring-boot:run      # ต้องมี Postgres ในเครื่อง + env vars
cd code && mvn verify               # รันเทสต์ทั้งหมด (อ่านจากโฟลเดอร์ test/)
```
- บัญชีเดโม: `demo@cinemalog.app` / `cinema123`
- Swagger: http://localhost:8080/swagger-ui.html (login ที่ /login ก่อน)
- Test report: `code/target/reports/surefire.html` → คัดลอกไปไว้ `test/reports/` ก่อนส่ง

## 8. Deploy (ปวริศา, ท้ายโปรเจกต์)

1. Supabase → สร้าง project สำหรับ deploy (แยกจาก project ที่ใช้พัฒนา) → Connect → Session pooler → คัดลอก host/user/password
2. Render → New → Web Service → เลือก repo → Runtime: Docker (ใช้ `Dockerfile` ที่ root)
3. Environment: `DB_URL=jdbc:postgresql://<host>.pooler.supabase.com:5432/postgres?sslmode=require`, `DB_USERNAME=postgres.<project-ref>`, `DB_PASSWORD`, `TMDB_API_KEY`
4. Render → Settings → Deploy Hook → คัดลอก URL → GitHub → Settings → Secrets → `RENDER_DEPLOY_HOOK` (CI จะ deploy ให้เองเมื่อ merge เข้า `main`)
5. ใส่ URL จริงใน README หัวข้อ Deployment URL

## 9. ก่อนนำเสนอ: ทุกคนต้องอธิบายโค้ดของตัวเองได้

ข้อ 13 ในเอกสาร: อธิบายโค้ดตัวเองไม่ได้ = 0 คะแนนส่วนนั้น ให้แต่ละคนอ่านไฟล์ของตัวเองจนตอบได้ว่า "ทำไมเขียนแบบนี้"

- **ปวริศา**
  - ทำไม Reminder ใช้ State pattern (`ReminderStatus`)
  - Template Method ใน `AbstractNotificationSender.send()` ทำงานยังไง
  - Factory เลือก sender จากอะไร
  - Observer ผ่าน `@EventListener` ทำงานยังไง
  - ทำไม `/api/**` ไม่ใช้ CSRF token แต่ใช้ SameSite cookie แทน
  - `GlobalExceptionHandler` ตัดสิน HTTP status จาก `ApiException` ยังไง
- **ปรายฝน**
  - Adapter แยก `TmdbClient` กับ `TmdbMovieCatalogAdapter` เพราะอะไร
  - Builder ใน `MovieSpecificationBuilder` ข้าม filter ที่ว่างยังไง และทำไมใช้ EXISTS สำหรับ genre
  - `@BatchSize` แก้ปัญหา N+1 ยังไง
  - ทำไม sync ใช้ `noRollbackFor`
  - ระบบแนะนำหนังให้คะแนนยังไง
- **นันทพร**
  - ทำไมแยก `DiaryQueryService` กับ `DiaryCommandService` (ISP)
  - ทำไม `CollectionMovie` เป็น entity ไม่ใช้ `@ManyToMany`
  - `cascade` + `orphanRemoval` ทำอะไร
  - กฎวันที่ในอนาคตอยู่ที่ไหน
  - การบันทึกหนังลบออกจาก watchlist และส่ง event ไปที่ไหน
