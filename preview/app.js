const translations = {
  en: {
    previewLabel: "Theme preview",
    libraryTab: "Library",
    playerTab: "Player",
    lightMode: "Light",
    darkMode: "Dark",
    switchToLight: "Switch to light mode",
    switchToDark: "Switch to dark mode",
    chooseTitle: "Choose a visual world",
    chooseDescription: "Five visual worlds, carried into one player.",
    referenceNote: "Artwork and logos are intentionally replaced with neutral placeholders.",
    liveLabel: "LIVE PREVIEW",
    stageHint: "Tap a track to open the player. Play controls are interactive.",
    library: "Library",
    discover: "Discover",
    search: "Search your library",
    artists: "Artists",
    albums: "Albums",
    playlists: "Playlists",
    recent: "Recently added",
    songs: "Songs",
    all: "See all",
    monthlyRanking: "Monthly Ranking",
    popularPlaylist: "Popular Playlist",
    news: "News",
    topBeats: "Top Beats",
    songCount: "40 Songs",
    newsItem: "New audio added to your local library",
    artistA: "North Lines",
    artistB: "Low Tide",
    artistC: "Still Frame",
    albumA: "Quiet Hours",
    albumB: "Open Roads",
    albumC: "Soft Signals",
    tracks: [
      ["Evening Walk", "North Lines", "3:42"],
      ["Quran Recitation", "Local audio", "5:18"],
      ["Blue Window", "Low Tide", "4:06"],
      ["Slow Morning", "Still Frame", "3:51"]
    ],
    nowPlaying: "Now Playing",
    audioOutput: "Audio output",
    playlist: "PLAYLIST",
    quietAlbum: "Quiet Hours",
    quietArtist: "North Lines",
    previous: "Previous",
    next: "Next",
    favorite: "Favorite",
    queue: "Up next",
    share: "Share",
    lyrics: "Up next",
    queueEmpty: "Your next tracks",
    playing: "Playing",
    paused: "Paused",
    navigation: ["Home", "Browse", "Search", "Library"],
    duration: "3:42",
    time: "1:16",
    themeNames: ["Organic Light", "Outline Mono", "Violet Current", "Petal Player", "Neumorphic Blue"]
  },
  ar: {
    previewLabel: "معاينة الثيمات",
    libraryTab: "المكتبة",
    playerTab: "المشغّل",
    lightMode: "فاتح",
    darkMode: "داكن",
    switchToLight: "التبديل إلى الوضع الفاتح",
    switchToDark: "التبديل إلى الوضع الداكن",
    chooseTitle: "اختر المظهر",
    chooseDescription: "خمسة عوالم بصرية داخل مشغّل واحد.",
    referenceNote: "استُبدلت الأغلفة والشعارات بعناصر محايدة.",
    liveLabel: "معاينة مباشرة",
    stageHint: "اضغط على مقطع لفتح المشغّل. أزرار التشغيل تفاعلية.",
    library: "المكتبة",
    discover: "اكتشف",
    search: "ابحث في مكتبتك",
    artists: "الفنانون",
    albums: "الألبومات",
    playlists: "قوائم التشغيل",
    recent: "أُضيفت مؤخرًا",
    songs: "الأغاني",
    all: "عرض الكل",
    monthlyRanking: "ترتيب الشهر",
    popularPlaylist: "قائمة شائعة",
    news: "جديد المكتبة",
    topBeats: "إيقاعات مميزة",
    songCount: "٤٠ مقطعًا",
    newsItem: "تمت إضافة صوت جديد إلى مكتبتك المحلية",
    artistA: "خطوط الشمال",
    artistB: "مدّ هادئ",
    artistC: "إطار ساكن",
    albumA: "ساعات هادئة",
    albumB: "طرق مفتوحة",
    albumC: "إشارات ناعمة",
    tracks: [
      ["نزهة المساء", "خطوط الشمال", "٣:٤٢"],
      ["تلاوة قرآنية", "ملف صوتي", "٥:١٨"],
      ["نافذة زرقاء", "مدّ هادئ", "٤:٠٦"],
      ["صباح هادئ", "إطار ساكن", "٣:٥١"]
    ],
    nowPlaying: "يُشغّل الآن",
    audioOutput: "مخرج الصوت",
    playlist: "قائمة تشغيل",
    quietAlbum: "ساعات هادئة",
    quietArtist: "خطوط الشمال",
    previous: "السابق",
    next: "التالي",
    favorite: "المفضلة",
    queue: "التالي",
    share: "مشاركة",
    lyrics: "التالي",
    queueEmpty: "المقاطع التالية",
    playing: "قيد التشغيل",
    paused: "متوقف",
    navigation: ["الرئيسية", "استكشاف", "بحث", "المكتبة"],
    duration: "٣:٤٢",
    time: "١:١٦",
    themeNames: ["العضوي الفاتح", "أحادي بخطوط", "التيار البنفسجي", "مشغّل بتلات", "أزرق نيومورفي"]
  }
};

const root = document.documentElement;
const device = document.getElementById("device");
const themeOptions = document.getElementById("theme-options");
const screenHost = document.getElementById("app-screen");
const activeName = document.getElementById("active-theme-name");
const activeDescription = document.getElementById("active-theme-description");
const localeButton = document.getElementById("locale-switch");
const modeButton = document.getElementById("mode-switch");
const tokensPromise = fetch("../design-tokens.json").then((response) => {
  if (!response.ok) throw new Error(`Could not load design tokens (${response.status})`);
  return response.json();
});

let tokens;
let activeTheme = "theme1";
let activeView = "library";
let locale = "en";
let colorMode = "light";
let isPlaying = false;
let currentTrackIndex = 0;
let queueOpen = false;
let favorite = false;
let animationFrame = 0;
let startedAt = 0;

const t = (key) => translations[locale][key];
const icon = (name) => `<span class="ui-icon ui-icon-${name}" aria-hidden="true"></span>`;
const formatThemeTitle = (theme, index) => translations[locale].themeNames[index] || theme.name[locale];

function renderThemeChoices() {
  themeOptions.innerHTML = tokens.themes.map((theme, index) => `
    <button type="button" class="theme-choice" data-theme-choice="${theme.id}" aria-pressed="${theme.id === activeTheme}">
      <span class="theme-swatch" style="background:linear-gradient(135deg, ${theme.modes[colorMode].background} 0 58%, ${theme.modes[colorMode].accent} 58% 100%)"></span>
      <span class="theme-choice-copy">
        <span class="theme-choice-title">${formatThemeTitle(theme, index)}</span>
        <span class="theme-choice-subtitle">${theme.layout.library.replaceAll("-", " ")}</span>
      </span>
      <span class="choice-check">${icon("check")}</span>
    </button>
  `).join("");
}

function theme() {
  return tokens.themes.find((item) => item.id === activeTheme);
}

function trackArt(index, className = "track-art") {
  return `<span class="${className} art-${index + 1}" aria-hidden="true"></span>`;
}

function renderTrackRows() {
  return t("tracks").map((track, index) => `
    <button type="button" class="track-row" data-track-index="${index}" aria-label="${track[0]}, ${track[1]}">
      ${trackArt(index)}
      <span class="track-copy">
        <span class="track-name">${track[0]}</span>
        <span class="track-subtitle">${track[1]}</span>
      </span>
      <span class="track-duration">${track[2]}</span>
    </button>
  `).join("");
}

function renderArtists() {
  return [t("artistA"), t("artistB"), t("artistC")].map((artist, index) => `
    <div class="artist-item">
      <span class="artist-art art-${index + 1}" aria-hidden="true"></span>
      <span class="artist-name">${artist}</span>
    </div>
  `).join("");
}

function renderAlbums() {
  return [t("albumA"), t("albumB"), t("albumC")].map((album, index) => `
    <div class="album-card">
      <div class="album-art art-${index + 1}" aria-hidden="true"></div>
      <span class="album-title">${album}</span>
      <span class="album-caption">${t("tracks")[index][1]}</span>
    </div>
  `).join("");
}

function bottomNavigation() {
  const labels = t("navigation");
  const names = ["home", "explore", "search", "library"];
  return `<nav class="bottom-nav" aria-label="${t("library")}">${names.map((name, index) => `
    <button class="nav-item ${index === 0 ? "is-current" : ""}" type="button" data-nav="${name}">
      ${icon(name)}
      <span>${labels[index]}</span>
    </button>`).join("")}</nav>`;
}

function renderMiniPlayer() {
  const track = t("tracks")[currentTrackIndex % t("tracks").length];
  return `
    <div class="mini-player">
      ${trackArt(currentTrackIndex, "mini-art")}
      <span class="mini-copy"><span class="mini-title">${track[0]}</span><span class="mini-artist">${track[1]}</span></span>
      <button class="icon-button" type="button" data-action="toggle" aria-label="${isPlaying ? t("paused") : t("playing")}">
        ${icon(isPlaying ? "pause" : "play")}
      </button>
      <button class="icon-button" type="button" data-action="open-player" aria-label="${t("playerTab")}">${icon("next")}</button>
    </div>`;
}

function renderNeumorphicFeed() {
  const albumTiles = [0, 1, 2].map((index) => `
    <div class="neumo-ranking-art art-${index + 1}" aria-hidden="true">
      <span class="neumo-ranking-cover"></span>
    </div>`).join("");
  const playlistTiles = [0, 1].map((index) => `
    <button class="neumo-playlist-tile" type="button" data-track-index="${index}">
      <span class="neumo-playlist-art art-${index + 1}" aria-hidden="true"></span>
      <span class="neumo-playlist-name">${t("topBeats")}</span>
      <span class="neumo-playlist-count">${t("songCount")}</span>
    </button>`).join("");
  const newsTiles = [0, 1, 2].map((index) => `
    <button class="neumo-news-tile" type="button" data-track-index="${index}">
      <span class="neumo-news-art art-${index + 1}" aria-hidden="true"></span>
      <span>${t("newsItem")}</span>
    </button>`).join("");

  return `
    <div class="screen-scroll neumo-feed-screen">
      <div class="neumo-feed-content">
        <section class="neumo-feed-section">
          <div class="neumo-section-heading"><h2>${t("monthlyRanking")}</h2><span class="neumo-more" aria-hidden="true">${icon("more")}</span></div>
          <div class="neumo-ranking-row">${albumTiles}</div>
        </section>
        <section class="neumo-feed-section">
          <div class="neumo-section-heading"><h2>${t("popularPlaylist")}</h2><span class="neumo-more" aria-hidden="true">${icon("more")}</span></div>
          <div class="neumo-playlist-row">${playlistTiles}</div>
        </section>
        <section class="neumo-feed-section">
          <div class="neumo-section-heading"><h2>${t("news")}</h2><span class="neumo-more" aria-hidden="true">${icon("more")}</span></div>
          <div class="neumo-news-list">${newsTiles}</div>
        </section>
      </div>
    </div>`;
}

function renderNeumorphicPlayer() {
  const current = t("tracks")[currentTrackIndex % t("tracks").length];
  return `
    <div class="neumo-player-screen">
      <header class="neumo-player-heading">
        <h2>${current[0]}</h2>
        <p>${current[1]}</p>
        <span>${t("audioOutput")}</span>
      </header>
      <div class="neumo-player-row">
        <div class="neumo-transport">
          <button class="icon-button" type="button" data-action="repeat" aria-label="Repeat">${icon("repeat")}</button>
          <button class="icon-button" type="button" data-action="next" aria-label="${t("next")}">${icon("forward")}</button>
          <button class="icon-button neumo-transport-play" type="button" data-action="toggle" aria-label="${isPlaying ? t("paused") : t("playing")}">${icon(isPlaying ? "pause" : "play")}</button>
          <button class="icon-button" type="button" data-action="previous" aria-label="${t("previous")}">${icon("previous")}</button>
        </div>
        <div class="neumo-disc-clip" role="img" aria-label="${t("quietAlbum")} — abstract album art">
          <div class="neumo-disc">
            <span class="neumo-disc-art art-${currentTrackIndex + 1}"></span>
          </div>
        </div>
      </div>
    </div>`;
}

function renderOrganicDock() {
  const labels = t("navigation");
  const names = ["home", "explore", "search", "library"];
  return `
    <div class="organic-dock">
      <div class="organic-dock-controls">
        <div class="dock-progress"><span></span></div>
        <button class="icon-button" type="button" data-action="shuffle" aria-label="Shuffle">${icon("shuffle")}</button>
        <button class="icon-button" type="button" data-action="previous" aria-label="${t("previous")}">${icon("previous")}</button>
        <button class="icon-button dock-play" type="button" data-action="toggle" aria-label="${isPlaying ? t("paused") : t("playing")}">${icon(isPlaying ? "pause" : "play")}</button>
        <button class="icon-button" type="button" data-action="next" aria-label="${t("next")}">${icon("forward")}</button>
        <button class="icon-button" type="button" data-action="repeat" aria-label="Repeat">${icon("repeat")}</button>
      </div>
      <nav class="bottom-nav" aria-label="${t("library")}">${names.map((name, index) => `
        <button class="nav-item ${index === 3 ? "is-current" : ""}" type="button" data-nav="${name}">
          ${icon(name)}
          <span>${labels[index]}</span>
        </button>`).join("")}
      </nav>
    </div>`;
}

function renderLibrary() {
  if (activeTheme === "theme5") return renderNeumorphicFeed();
  const layout = theme().layout.library;
  const isOrganic = activeTheme === "theme1";
  const title = layout === "discover" ? t("discover") : t("library");
  const search = `<div class="search-box">${icon("search")}<span>${t("search")}</span></div>`;
  const artistSection = `
    <div class="section-row"><h3>${t("artists")}</h3><button type="button" class="text-action">${t("all")}</button></div>
    <div class="artist-row">${renderArtists()}</div>`;
  const albumsSection = `
    <div class="section-row"><h3>${isOrganic ? t("playlists") : layout === "midnight-feed" ? t("recent") : t("albums")}</h3><button type="button" class="text-action">${t("all")}</button></div>
    <div class="album-row">${renderAlbums()}</div>`;
  const tracksSection = `
    <div class="section-row"><h3>${isOrganic ? t("songs") : layout === "discover" ? t("recent") : t("library")}</h3><button type="button" class="text-action">${t("all")}</button></div>
    <div class="track-list">${renderTrackRows()}</div>`;

  return `
    <div class="screen-scroll">
      <header class="app-header">
        <button class="header-action" type="button" aria-label="${t("library")}">${icon("menu")}</button>
        <h2 class="app-title">${title}</h2>
        <button class="header-action" type="button" aria-label="${t("search")}">${icon("search")}</button>
      </header>
      <div class="screen-content library-content ${isOrganic ? "organic-library" : ""}" data-layout="${layout}">
        ${layout === "discover" ? search : ""}
        ${layout === "editorial" || layout === "organic-light" ? artistSection + albumsSection : ""}
        ${layout === "discover" ? artistSection + albumsSection : ""}
        ${layout === "midnight-feed" ? albumsSection + artistSection : ""}
        ${layout === "playlist-list" ? artistSection : ""}
        ${tracksSection}
      </div>
    </div>
    ${isOrganic ? renderOrganicDock() : `${renderMiniPlayer()}${bottomNavigation()}`}`;
}

function renderPlayer() {
  if (activeTheme === "theme1") return renderOrganicPlayer();
  if (activeTheme === "theme5") return renderNeumorphicPlayer();
  const current = t("tracks")[currentTrackIndex % t("tracks").length];
  const bars = [18,28,22,38,25,31,44,24,35,18,42,28,33,20,38,25,46,22,33,18,29,40,26,35,20,42,26,32];
  return `
    <div class="player-screen ${isPlaying ? "is-playing" : ""}">
      <div class="player-topline">
        <button class="icon-button" type="button" data-action="library" aria-label="${t("library")}">${icon("back")}</button>
        <strong>${t("nowPlaying")}</strong>
        <button class="icon-button" type="button" aria-label="${t("queue")}">${icon("more")}</button>
      </div>
      <div class="player-hero" role="img" aria-label="${t("quietAlbum")} — neutral cover placeholder"></div>
      <div class="player-track">
        <div class="player-track-copy"><h2>${current[0]}</h2><p>${current[1]}</p></div>
        <button class="icon-button ${favorite ? "is-favorite" : ""}" type="button" data-action="favorite" aria-label="${t("favorite")}" aria-pressed="${favorite}">${icon("heart")}</button>
      </div>
      <div class="player-timeline">
        <div class="timeline-track"><div class="timeline-fill"></div></div>
        <div class="time-row"><span>${t("time")}</span><span>${t("duration")}</span></div>
      </div>
      <div class="waveform" aria-hidden="true">${bars.map((height) => `<span style="--bar-height:${height}px"></span>`).join("")}</div>
      <div class="player-controls">
        <button class="icon-button" type="button" data-action="shuffle" aria-label="Shuffle">${icon("shuffle")}</button>
        <button class="icon-button" type="button" data-action="previous" aria-label="${t("previous")}">${icon("previous")}</button>
        <button class="play-button" type="button" data-action="toggle" aria-label="${isPlaying ? t("paused") : t("playing")}">${icon(isPlaying ? "pause" : "play")}</button>
        <button class="icon-button" type="button" data-action="next" aria-label="${t("next")}">${icon("forward")}</button>
        <button class="icon-button" type="button" data-action="repeat" aria-label="Repeat">${icon("repeat")}</button>
      </div>
      <div class="player-extra">
        <button class="extra-action" type="button">${icon("heart")}<span>${t("favorite")}</span></button>
        <button class="extra-action primary" type="button" data-action="queue">${icon("queue")}<span>${t("queue")}</span></button>
        <button class="extra-action" type="button">${icon("share")}<span>${t("share")}</span></button>
      </div>
    </div>`;
}

function renderOrganicCover() {
  const contour = "M120 12 C139 23 154 17 169 27 C184 37 184 54 199 65 C214 76 208 94 221 109 C235 125 222 142 214 157 C207 173 214 188 199 200 C184 213 166 205 152 220 C139 235 121 224 106 228 C88 234 78 218 62 211 C45 205 46 187 32 173 C18 159 25 142 14 126 C4 110 21 94 21 79 C21 63 39 56 49 41 C60 24 79 27 94 18 C105 11 113 14 120 12 Z";
  return `
    <svg class="organic-cover" viewBox="0 0 240 240" role="img" aria-label="Neutral abstract grayscale cover">
      <defs>
        <clipPath id="organic-clip"><path d="${contour}"/></clipPath>
        <linearGradient id="organic-ground" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#f8f7f3"/>
          <stop offset=".54" stop-color="#bcbab5"/>
          <stop offset="1" stop-color="#eceae5"/>
        </linearGradient>
      </defs>
      <path class="organic-cover-fill" d="${contour}"/>
      <g class="organic-cover-art" clip-path="url(#organic-clip)">
        <path fill="url(#organic-ground)" d="M0 0h240v240H0z"/>
        <path fill="#292927" d="M14 95c23-34 50-42 73-34 17 6 24 21 39 19 13-2 23-18 43-19 22-1 43 14 60 38v39c-24-13-41-10-58 6-22 21-28 49-53 54-25 5-38-18-58-29-18-10-32-8-46 2Z" opacity=".88"/>
        <path fill="#f3f1ed" d="M25 120c24-16 41-19 61-8 15 8 23 27 36 30 14 3 27-10 43-13 17-3 37 5 54 20v52c-17-13-35-17-50-10-24 11-36 34-57 35-20 0-34-19-51-31-13-9-25-12-36-7Z" opacity=".88"/>
        <path d="M44 75c27 13 36 31 52 36 22 7 36-14 54-12 17 2 22 19 42 28" fill="none" stroke="#111" stroke-opacity=".72" stroke-width="2"/>
        <path d="M37 157c20-6 34-3 50 7 21 13 34 32 53 29 18-2 30-21 48-24" fill="none" stroke="#fff" stroke-opacity=".8" stroke-width="2"/>
        <circle cx="73" cy="109" r="4" fill="#171715"/>
        <circle cx="173" cy="147" r="3" fill="#171715"/>
      </g>
      <path id="organic-contour-path" class="organic-cover-outline" d="${contour}" fill="none" stroke="#383733" stroke-width="1.35"/>
      <circle id="organic-progress-dot" class="cover-progress-dot" cx="25" cy="123" r="3.6"/>
    </svg>`;
}

function renderOrganicPlayer() {
  const current = t("tracks")[currentTrackIndex % t("tracks").length];
  return `
    <div class="player-screen organic-player ${isPlaying ? "is-playing" : ""}">
      <div class="player-topline organic-topline">
        <button class="icon-button" type="button" data-action="library" aria-label="${t("library")}">${icon("back")}</button>
        <strong>${t("nowPlaying")}</strong>
        <button class="icon-button ${favorite ? "is-favorite" : ""}" type="button" data-action="favorite" aria-label="${t("favorite")}" aria-pressed="${favorite}">${icon("heart")}</button>
      </div>
      <div class="organic-time"><span>${t("time")}</span><span aria-hidden="true">|</span><span>${t("duration")}</span></div>
      <div class="organic-cover-wrap">${renderOrganicCover()}</div>
      <div class="organic-track">
        <h2>${current[0]}</h2>
        <p>${current[1]}</p>
      </div>
      <div class="organic-controls">
        <button class="icon-button" type="button" data-action="repeat" aria-label="Repeat">${icon("repeat")}</button>
        <button class="icon-button" type="button" data-action="previous" aria-label="${t("previous")}">${icon("previous")}</button>
        <button class="play-button organic-play" type="button" data-action="toggle" aria-label="${isPlaying ? t("paused") : t("playing")}">${icon(isPlaying ? "pause" : "play")}</button>
        <button class="icon-button" type="button" data-action="next" aria-label="${t("next")}">${icon("forward")}</button>
        <button class="icon-button" type="button" data-action="shuffle" aria-label="Shuffle">${icon("shuffle")}</button>
      </div>
      <button class="up-next" type="button" data-action="queue" aria-expanded="${queueOpen}">
        <span>${t("queue")}</span>
        <span class="up-next-chevron">${icon("next")}</span>
      </button>
      ${queueOpen ? `<div class="queue-sheet" role="region" aria-label="${t("queue")}">
        <strong>${t("queueEmpty")}</strong>
        ${t("tracks").slice(1, 4).map((track, index) => `<div class="queue-row"><span class="queue-index">${String(index + 1).padStart(2, "0")}</span><span><b>${track[0]}</b><small>${track[1]}</small></span></div>`).join("")}
      </div>` : ""}
    </div>`;
}

function render() {
  if (!tokens) return;
  const active = theme();
  device.dataset.theme = active.id;
  device.dataset.mode = colorMode;
  Object.entries(active.modes[colorMode]).forEach(([key, value]) => {
    const cssKey = ({ background: "bg", textMuted: "muted" })[key]
      ?? key.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`);
    device.style.setProperty(`--app-${cssKey}`, value);
  });
  modeButton.querySelector(".mode-icon").innerHTML = icon(colorMode === "light" ? "moon" : "sun");
  modeButton.querySelector(".mode-label").textContent = t(colorMode === "light" ? "darkMode" : "lightMode");
  modeButton.setAttribute("aria-label", t(colorMode === "light" ? "switchToDark" : "switchToLight"));
  activeName.textContent = formatThemeTitle(active, tokens.themes.indexOf(active));
  activeDescription.textContent = active.description[locale];
  screenHost.innerHTML = activeView === "player" ? renderPlayer() : renderLibrary();
  document.querySelectorAll("[data-theme-choice]").forEach((button) => {
    button.setAttribute("aria-pressed", String(button.dataset.themeChoice === activeTheme));
  });
  document.querySelectorAll("[data-view]").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === activeView);
  });
  renderThemeChoices();
  if (isPlaying && activeView === "player" && document.visibilityState === "visible") startProgress();
  else stopProgress();
}

function startProgress() {
  if (animationFrame) return;
  startedAt = performance.now();
  const tick = (now) => {
    if (!isPlaying || activeView !== "player" || document.visibilityState !== "visible") {
      animationFrame = 0;
      return;
    }
    const fill = screenHost.querySelector(".timeline-fill");
    if (fill) {
      const progress = (34 + ((now - startedAt) / 1000) * 0.25) % 96;
      fill.style.width = `${progress}%`;
    }
    const contour = screenHost.querySelector("#organic-contour-path");
    const dot = screenHost.querySelector("#organic-progress-dot");
    if (contour && dot) {
      const length = contour.getTotalLength();
      const point = contour.getPointAtLength((((now - startedAt) / 12000) % 1) * length);
      dot.setAttribute("cx", String(point.x));
      dot.setAttribute("cy", String(point.y));
    }
    animationFrame = requestAnimationFrame(tick);
  };
  animationFrame = requestAnimationFrame(tick);
}

function stopProgress() {
  if (animationFrame) cancelAnimationFrame(animationFrame);
  animationFrame = 0;
}

function applyLocale(nextLocale) {
  locale = nextLocale;
  root.lang = locale;
  root.dir = locale === "ar" ? "rtl" : "ltr";
  localeButton.textContent = locale === "ar" ? "English" : "عربي";
  localeButton.setAttribute("aria-label", locale === "ar" ? "Switch to English" : "التبديل إلى العربية");
  document.querySelectorAll("[data-copy]").forEach((element) => {
    element.textContent = t(element.dataset.copy);
  });
  render();
}

document.addEventListener("click", (event) => {
  const choice = event.target.closest("[data-theme-choice]");
  if (choice) {
    activeTheme = choice.dataset.themeChoice;
    render();
    return;
  }

  if (event.target.closest("#mode-switch")) {
    colorMode = colorMode === "light" ? "dark" : "light";
    render();
    return;
  }

  const view = event.target.closest("[data-view]");
  if (view) {
    activeView = view.dataset.view;
    render();
    return;
  }

  const nav = event.target.closest("[data-nav]");
  if (nav && nav.dataset.nav === "home") {
    activeView = "library";
    render();
    return;
  }

  const track = event.target.closest("[data-track-index]");
  if (track) {
    currentTrackIndex = Number(track.dataset.trackIndex);
    activeView = "player";
    render();
    return;
  }

  const action = event.target.closest("[data-action]")?.dataset.action;
  if (action === "toggle") {
    isPlaying = !isPlaying;
    render();
  } else if (action === "next" || action === "previous") {
    const count = t("tracks").length;
    currentTrackIndex = (currentTrackIndex + (action === "next" ? 1 : count - 1)) % count;
    render();
  } else if (action === "library") {
    activeView = "library";
    render();
  } else if (action === "open-player") {
    activeView = "player";
    render();
  } else if (action === "queue") {
    queueOpen = !queueOpen;
    render();
  } else if (action === "favorite") {
    favorite = !favorite;
    render();
  }
});

localeButton.addEventListener("click", () => applyLocale(locale === "en" ? "ar" : "en"));
document.addEventListener("visibilitychange", () => {
  if (document.visibilityState !== "visible") stopProgress();
  else if (isPlaying && activeView === "player") startProgress();
});

tokensPromise.then((loadedTokens) => {
  tokens = loadedTokens;
  renderThemeChoices();
  applyLocale("en");
}).catch((error) => {
  screenHost.innerHTML = `<p role="alert" class="load-error">Theme preview could not start: ${error.message}</p>`;
  console.error(error);
});
