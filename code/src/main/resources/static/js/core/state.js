import { $$ } from './dom.js';
import { ic } from './doodles.js';
import { library } from '../api/library.js';

/** Filled by the Thymeleaf layout: <span id="session" data-auth="true" data-email="…" …> */
export const session = document.getElementById('session')?.dataset || {};

export const st = {
  loggedIn: session.auth === 'true',
  email: session.email || '',
  liked: new Set(),
  watchlist: new Set(),
  watched: new Map(),     // movieId -> { entryId, watchedDate, rating, reviewed }
  reminders: new Map(),   // movieId -> { offsetDays, channel, status }
  unread: 0
};

/** Every movie the page has drawn, by id — menus and modals read titles from here. */
export const MOVIES = new Map();

export const cache = m => {
  (Array.isArray(m) ? m : [m]).forEach(x => {
    if (x && x.id) MOVIES.set(x.id, x);
  });
  return m;
};

/*
 * Keep the user's library state while navigating between pages.
 * A short TTL prevents stale state from surviving for too long.
 */
const LIBRARY_CACHE_KEY = 'poppy-night:library-state';
const LIBRARY_CACHE_TTL = 5 * 60 * 1000;

function applyLibraryState(s) {
  if (!s) return;

  st.liked = new Set(s.likedMovieIds || []);
  st.watchlist = new Set(s.watchlistMovieIds || []);

  st.watched = new Map(
    (s.watched || []).map(w => [w.movieId, w])
  );

  st.reminders = new Map(
    (s.reminders || []).map(r => [r.movieId, r])
  );

  st.unread = s.unreadNotifications || 0;

  document.dispatchEvent(
    new CustomEvent('library:changed')
  );
}

function getCachedLibrary() {
  try {
    const raw = sessionStorage.getItem(LIBRARY_CACHE_KEY);

    if (!raw) return null;

    const cached = JSON.parse(raw);

    if (!cached.savedAt || !cached.data) {
      sessionStorage.removeItem(LIBRARY_CACHE_KEY);
      return null;
    }

    if (Date.now() - cached.savedAt > LIBRARY_CACHE_TTL) {
      sessionStorage.removeItem(LIBRARY_CACHE_KEY);
      return null;
    }

    return cached.data;
  } catch {
    sessionStorage.removeItem(LIBRARY_CACHE_KEY);
    return null;
  }
}

function saveLibraryCache(data) {
  try {
    sessionStorage.setItem(
      LIBRARY_CACHE_KEY,
      JSON.stringify({
        savedAt: Date.now(),
        data
      })
    );
  } catch {
    // Storage unavailable — continue without cache.
  }
}

/**
 * Refresh the user's library.
 *
 * force = false:
 *   use cached state when navigating between pages.
 *
 * force = true:
 *   always request fresh state from the server.
 */
export async function refreshLibrary(force = false) {
  if (!st.loggedIn) return;

  if (!force) {
    const cached = getCachedLibrary();

    if (cached) {
      applyLibraryState(cached);
      return;
    }
  }

  const s = await library.state();

  applyLibraryState(s);
  saveLibraryCache(s);
}

/** Flip ♡ and + buttons for one movie without redrawing the page. */
export function syncButtons(id) {
  $$(`[data-act="like"][data-id="${id}"]`).forEach(b => {
    const on = st.liked.has(+id);

    b.classList.toggle('on', on);
    b.setAttribute('aria-pressed', on);

    const l = b.querySelector('.lbl');

    if (l) {
      l.textContent = on ? l.dataset.on : l.dataset.off;
    }
  });

  $$(`[data-act="wl"][data-id="${id}"]`).forEach(b => {
    const on = st.watchlist.has(+id);

    b.classList.toggle('on', on);
    b.setAttribute('aria-pressed', on);

    const l = b.querySelector('.lbl');

    if (l) {
      l.textContent = on ? l.dataset.on : l.dataset.off;
    } else {
      const svg = b.querySelector('svg');

      if (svg) {
        svg.outerHTML = ic(on ? 'check' : 'plus');
      }
    }
  });
}

/* the current page tells us how to redraw itself, and for which kinds of change */
let renderer = null;

export function setPageRenderer(fn, kinds = []) {
  renderer = { fn, kinds };
}

export async function afterChange(movieId, kind) {
  /*
   * Something changed (like/watchlist/diary/collection/reminder),
   * so never trust the previous cached library state.
   */
  await refreshLibrary(true);

  if (movieId) {
    syncButtons(movieId);
  }

  if (
    renderer &&
    (
      renderer.kinds.includes('*') ||
      renderer.kinds.includes(kind)
    )
  ) {
    const y = scrollY;

    await renderer.fn();

    scrollTo(0, y);
  }
}