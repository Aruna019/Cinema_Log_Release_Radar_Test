# ER diagram

```mermaid
erDiagram
    users ||--|| user_profiles : "1:1 user_id UNIQUE"
    user_profiles ||--o{ user_favorite_genres : ""
    genres ||--o{ user_favorite_genres : ""
    movies ||--o{ movie_genres : ""
    genres ||--o{ movie_genres : ""
    users ||--o{ watched_movies : logs
    movies ||--o{ watched_movies : ""
    users ||--o{ watchlist_items : ""
    movies ||--o{ watchlist_items : ""
    users ||--o{ liked_movies : ""
    movies ||--o{ liked_movies : ""
    users ||--o{ collections : owns
    collections ||--o{ collection_movies : contains
    movies ||--o{ collection_movies : ""
    users ||--o{ reminders : sets
    movies ||--o{ reminders : ""
    users ||--o{ notifications : receives
    movies |o--o{ notifications : "optional"

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        timestamptz created_at
    }
    user_profiles {
        bigint id PK
        bigint user_id FK,UK
        varchar display_name
        varchar bio
        varchar avatar_style
        varchar avatar_color
    }
    genres {
        int id PK
        varchar name UK
    }
    movies {
        bigint id PK
        bigint tmdb_id UK
        varchar title
        date release_date
        double vote_average
        double popularity
        int runtime
        varchar director
    }
    movie_genres {
        bigint movie_id PK,FK
        int genre_id PK,FK
    }
    user_favorite_genres {
        bigint profile_id PK,FK
        int genre_id PK,FK
    }
    watched_movies {
        bigint id PK
        bigint user_id FK
        bigint movie_id FK
        date watched_date
        int rating
        varchar review
        varchar place
    }
    watchlist_items {
        bigint id PK
        bigint user_id FK
        bigint movie_id FK
        timestamptz added_at
    }
    liked_movies {
        bigint id PK
        bigint user_id FK
        bigint movie_id FK
        timestamptz liked_at
    }
    collections {
        bigint id PK
        bigint user_id FK
        varchar name
        varchar description
    }
    collection_movies {
        bigint id PK
        bigint collection_id FK
        bigint movie_id FK
        timestamptz added_at
    }
    reminders {
        bigint id PK
        bigint user_id FK
        bigint movie_id FK
        int offset_days
        date reminder_date
        varchar channel
        varchar status
    }
    notifications {
        bigint id PK
        bigint user_id FK
        bigint movie_id FK
        varchar type
        varchar message
        boolean is_read
    }
```

Relationship checklist: **One-to-One** users–user_profiles · **One-to-Many** users→watched_movies, users→collections→collection_movies, users→reminders · **Many-to-Many** (bonus) movies–genres, user_profiles–genres, collections–movies (with `added_at`).
