// Renders the listing artwork in marketing/images/ from real in-game screenshots and the mod's bundled fonts.
// Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(root, '1.21.1', 'common', 'src', 'main', 'resources', 'assets', 'rpg_attribute_system', 'font');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

const css = `
@font-face { font-family: RasSerif; src: url('${url(path.join(fonts, 'ras_serif.ttf'))}'); }
@font-face { font-family: RasDisplay; src: url('${url(path.join(fonts, 'ras_display.ttf'))}'); }
:root { --text:#dad3c1; --dim:#9c9584; --faint:#6e685c; --header:#e8dfc8; --gold:#d4b06a; --gold-hi:#f0d58c;
        --blue:#79a2ff; --red:#e0645a; --bg:#0b0a09; --line:rgba(216,207,184,.32); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: RasSerif, serif; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 30% 40%, #1c1915 0%, #0b0a09 70%); }
.display { font-family: RasDisplay, serif; letter-spacing: .04em; }
.gold { background: linear-gradient(180deg, #fff2c8 0%, #f0d58c 35%, #d4b06a 70%, #8c7444 100%);
        -webkit-background-clip: text; background-clip: text; color: transparent; }
.glow { filter: drop-shadow(0 0 18px rgba(212,176,106,.35)); }
.kicker { font-family: RasDisplay, serif; color: var(--gold); letter-spacing: .22em; font-size: 22px; }
.rule { height: 1px; background: linear-gradient(90deg, rgba(212,176,106,0), rgba(212,176,106,.9) 20%, rgba(212,176,106,.9) 70%, rgba(212,176,106,0)); }
.diamond { display: inline-block; width: 9px; height: 9px; background: var(--gold); transform: rotate(45deg); margin: 0 12px 3px 0; }
.frame { border: 1px solid rgba(216,207,184,.45); box-shadow: 0 0 0 1px rgba(0,0,0,.6), 0 30px 80px rgba(0,0,0,.65), 0 0 60px rgba(212,176,106,.16); }
.frame img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 45%, rgba(0,0,0,.75) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(16px) brightness(.42) saturate(1.1); }
.pill { display: inline-block; border: 1px solid rgba(216,207,184,.55); padding: 6px 14px 4px; margin-right: 10px;
        font-family: RasDisplay, serif; font-size: 18px; letter-spacing: .12em; color: var(--header); background: rgba(0,0,0,.35); }
.callout { position: absolute; font-family: RasDisplay, serif; font-size: 22px; letter-spacing: .14em; color: var(--header);
           background: rgba(10,9,8,.86); border: 1px solid rgba(212,176,106,.8); padding: 8px 16px 6px; white-space: nowrap;
           box-shadow: 0 8px 26px rgba(0,0,0,.6); }
.callout small { display: block; font-family: RasSerif, serif; letter-spacing: 0; font-size: 19px; color: var(--dim); margin-top: 2px; }
.badge { position: absolute; width: 46px; height: 46px; margin: -23px 0 0 -23px; border-radius: 50%; background: #12100d;
         border: 2px solid var(--gold); box-shadow: 0 0 18px rgba(212,176,106,.7); font-family: RasDisplay, serif; font-size: 26px;
         color: var(--gold-hi); display: flex; align-items: center; justify-content: center; }
.ring { position: absolute; border: 2px solid var(--gold); border-radius: 4px; box-shadow: 0 0 18px rgba(212,176,106,.55); }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 26px; margin: 0 0 14px; font-size: 27px; line-height: 1.32; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 2px; top: 13px; width: 8px; height: 8px; background: var(--gold); transform: rotate(45deg); }
.k { display: inline-block; border: 1px solid rgba(216,207,184,.6); padding: 0 8px; margin: 0 2px; background: rgba(0,0,0,.35); font-size: .9em; }
`;

// Slow golden motes, matching the in-game menu ambience. Deterministic so renders are reproducible.
const motes = `<canvas class="motes"></canvas><script>
(() => { const c = document.querySelector('canvas.motes'); const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = 7;
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  for (let i = 0; i < 90; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .25 + rnd() * .6, z = 1 + rnd() * 2.2;
    const h = g.createRadialGradient(x, y, 0, x, y, z * 5); h.addColorStop(0, 'rgba(246,222,156,' + a * .55 + ')'); h.addColorStop(1, 'rgba(246,222,156,0)');
    g.fillStyle = h; g.beginPath(); g.arc(x, y, z * 5, 0, Math.PI * 2); g.fill();
    g.fillStyle = 'rgba(255,240,200,' + a + ')'; g.fillRect(x - z / 2, y - z / 2, z, z); } })();
</script>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:28px;background:linear-gradient(90deg,rgba(0,0,0,0),rgba(20,18,15,.9) 30%,rgba(20,18,15,.9) 70%,rgba(0,0,0,0))">
    <div class="rule" style="width:300px;transform:scaleX(-1)"></div>
    <span class="diamond" style="margin:0"></span>
    <div class="display gold glow" style="font-size:44px;white-space:nowrap">${title}</div>
    <span class="diamond" style="margin:0"></span>
    <div class="rule" style="width:300px"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.55"></div>
  <div class="vignette"></div>${motes}
  <div style="position:absolute;left:80px;top:0;bottom:0;width:${width - artWidth - 150}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"><span class="diamond"></span>${kicker}</div>
    <div class="display gold glow" style="font-size:58px;line-height:1.08;margin:18px 0 22px">${title}</div>
    <div class="rule" style="width:80%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:60px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

const assets = {
  banner: () => page(1800, 600, `
    <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}')"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(8,7,6,.94) 0%,rgba(8,7,6,.78) 42%,rgba(8,7,6,.15) 100%)"></div>
    <div class="vignette"></div>${motes}
    <div style="position:absolute;right:-60px;top:70px;width:980px;transform:perspective(1600px) rotateY(-16deg) rotateX(3deg);transform-origin:left center">
      <div class="frame"><img src="${shot('level-up.jpg')}"></div></div>
    <div style="position:absolute;left:90px;top:92px;width:900px">
      <div class="kicker"><span class="diamond"></span>5.0 · The Ascendant Update</div>
      <div class="display gold glow" style="font-size:78px;line-height:1.06;margin:22px 0 26px;white-space:nowrap">RPG Attribute<br>System</div>
      <div class="rule" style="width:640px;margin-bottom:26px"></div>
      <div style="font-size:34px;color:var(--header);line-height:1.3">Stage your build. Preview every stat.<br>Confirm once.</div>
      <div style="margin-top:36px"><span class="pill">Fabric</span><span class="pill">Forge</span><span class="pill">NeoForge</span>
        <span style="font-size:24px;color:var(--dim);margin-left:8px">1.20.1 · 1.21.1 · 26.1.2 · 26.2 · 26.3</span></div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 45%,#2a241c 0%,#120f0c 55%,#070605 100%)"></div>${motes}
    <div style="position:absolute;left:56px;top:56px;width:400px;height:400px;border-radius:50%;border:3px solid var(--gold);box-shadow:0 0 40px rgba(212,176,106,.45), inset 0 0 40px rgba(212,176,106,.25)"></div>
    <div style="position:absolute;left:78px;top:78px;width:356px;height:356px;border-radius:50%;border:1px solid rgba(212,176,106,.55)"></div>
    ${[[256, 56], [456, 256], [256, 456], [56, 256]].map(([x, y]) => `<div style="position:absolute;left:${x - 12}px;top:${y - 12}px;width:24px;height:24px;background:var(--gold);transform:rotate(45deg);box-shadow:0 0 16px rgba(240,213,140,.8)"></div>`).join('')}
    <div style="position:absolute;left:116px;top:104px;width:280px;height:280px;background:radial-gradient(circle,rgba(240,213,140,.35),rgba(240,213,140,0) 65%)"></div>
    <img src="${source('tome.png')}" style="position:absolute;left:126px;top:114px;width:260px;height:260px;image-rendering:pixelated;filter:drop-shadow(0 10px 24px rgba(0,0,0,.7))">
    <div class="display gold" style="position:absolute;left:0;right:0;top:384px;text-align:center;font-size:50px;letter-spacing:.18em">RAS</div>`),

  'card-level-up': () => card({
    kicker: 'The Level Up screen', title: 'Plan your build<br>before you commit',
    points: ['<b style="color:var(--gold-hi)">1</b> Stage points with <span class="k">←</span><span class="k">→</span> or the chevrons',
      '<b style="color:var(--gold-hi)">2</b> Every attribute, stat and total reads <b style="color:var(--header)">current → after</b>',
      '<b style="color:var(--gold-hi)">3</b> One <span class="k">Enter</span> commits the whole build', 'Blue shows gains, red the points you keep. <span class="k">Reset</span> undoes everything'],
    art: `<div style="position:relative"><div class="frame"><img src="${shot('level-up.jpg')}"></div>
      <div class="ring" style="left:4.6%;top:30.6%;width:30.3%;height:5.6%"></div><div class="badge" style="left:2.4%;top:33.4%">1</div>
      <div class="ring" style="left:39%;top:15.8%;width:25%;height:4.4%"></div><div class="badge" style="left:36.7%;top:18%">2</div>
      <div class="ring" style="left:4.6%;top:88.4%;width:21.1%;height:5.4%"></div><div class="badge" style="left:2.4%;top:91.1%">3</div></div>` }),

  'card-measured': () => card({
    kicker: 'Live build preview', title: 'Numbers you<br>can trust',
    points: ['Stats are measured on the server, so gear, effects and other mods are included', 'Damage 9 → 9.6 already counts the netherite sword', 'Previews use Minecraft\'s real modifier math and caps',
      'The selected attribute marks every stat it changes', 'Fixes stats the client never receives, such as attack damage'],
    art: `<div style="position:relative"><div class="frame" style="height:830px;overflow:hidden">
      <img src="${shot('level-up.jpg')}" style="width:369%;margin-left:-139.5%;margin-top:-22.1%"></div>
      <div class="ring" style="left:2%;top:23.6%;width:96%;height:10.8%"></div></div>` }),

  'card-banner': () => card({
    kicker: 'Cinematic feedback', title: 'Every level<br>feels earned',
    points: ['A gold banner fades in when your level rises', 'Expanding rules, a shimmer and a bell', 'Shows the levels and points you gained, and your key',
      'Your progress is always visible in a slim, matching HUD'],
    art: `<div class="frame"><img src="${shot('level-up-banner.jpg')}"></div>` }),

  'card-pages': () => card({
    kicker: 'Status & Records', title: 'Your whole<br>character at a glance',
    points: ['<b style="color:var(--header)">Status</b>: your live model, measured stats and attribute values', '<b style="color:var(--header)">Records</b>: experience earned, points invested and bonuses',
      'Switch pages with <span class="k">Tab</span>', 'Hover any row for exact values'],
    art: `<div style="position:relative;height:640px">
      <div class="frame" style="position:absolute;left:0;top:0;width:78%"><img src="${shot('status.jpg')}"></div>
      <div class="frame" style="position:absolute;right:0;bottom:0;width:78%"><img src="${shot('records.jpg')}"></div></div>` }),

  'card-responsive': () => card({
    kicker: 'Readable everywhere', title: 'Built for every<br>GUI scale',
    points: ['Three columns on wide screens, two at GUI scale 4, one on tiny windows', 'Fonts rasterized for your GUI scale, so text stays crisp',
      'Long lists scroll, and every control is at least 20 pixels', 'Keyboard first, with full mouse support'],
    art: `<div class="frame"><img src="${shot('two-column.jpg')}"></div>` }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Every effect<br>has a switch',
    points: ['Serif or vanilla pixel font', 'Animation speed or no motion at all', 'Blur, motes, sounds and the banner, each on or off', 'Soulslike or classic HUD'],
    art: `<div class="frame" style="background:rgba(12,11,9,.92);padding:40px 50px;font-family:monospace;font-size:30px;line-height:1.55;color:var(--text)">
      <div style="color:var(--dim)">config/ras/display/ui.json</div>
      <pre style="margin:16px 0 0;font-family:inherit">{
  <span style="color:var(--gold)">"font"</span>: <span style="color:var(--blue)">"serif"</span>,
  <span style="color:var(--gold)">"animations"</span>: <span style="color:var(--blue)">true</span>,
  <span style="color:var(--gold)">"animation_speed"</span>: <span style="color:var(--blue)">1.0</span>,
  <span style="color:var(--gold)">"ambient_particles"</span>: <span style="color:var(--blue)">true</span>,
  <span style="color:var(--gold)">"background_blur"</span>: <span style="color:var(--blue)">true</span>,
  <span style="color:var(--gold)">"menu_sounds"</span>: <span style="color:var(--blue)">true</span>,
  <span style="color:var(--gold)">"hud_style"</span>: <span style="color:var(--blue)">"soulslike"</span>,
  <span style="color:var(--gold)">"hud_scale"</span>: <span style="color:var(--blue)">1.0</span>,
  <span style="color:var(--gold)">"level_up_banner"</span>: <span style="color:var(--blue)">true</span>,
  <span style="color:var(--gold)">"level_up_sound"</span>: <span style="color:var(--blue)">true</span>
}</pre></div>` }),

  'card-server': () => card({
    kicker: 'Server authoritative', title: 'All or nothing',
    points: ['Confirm sends one request with exact points per attribute', 'The server checks locks, IDs and your points before spending anything',
      'Points then apply one by one, with your existing caps and commands', 'A refused request changes nothing'],
    art: `<div style="display:flex;flex-direction:column;gap:26px">
      ${[['Client', 'Vitality +4 · Attack Power +3 · Toughness +1'], ['Server validates', 'configured · unlocked · 8 ≤ 9 points'], ['Server applies', 'per point: cap check · on_level_event · commands'], ['One sync', 'the screen shows "Attributes strengthened"']]
        .map(([t, s], i) => `<div class="frame" style="background:rgba(12,11,9,.9);padding:26px 34px;display:flex;align-items:center;gap:26px">
          <div class="display gold" style="font-size:52px;width:60px">${i + 1}</div>
          <div><div class="display" style="font-size:32px;color:var(--header)">${t}</div><div style="font-size:26px;color:var(--dim);margin-top:4px">${s}</div></div></div>`).join('')}</div>` }),
};

const headers = { 'header-level-up': 'The Level Up Screen', 'header-preview': 'Live Build Preview', 'header-banner': 'Cinematic Level Ups',
  'header-pages': 'Status & Records', 'header-everything': 'Everything Else', 'header-compatibility': 'Downloads & Compatibility',
  'header-config': 'Configuration', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:18px">
  <div class="rule" style="width:520px;transform:scaleX(-1)"></div><span class="diamond" style="margin:0"></span><div class="rule" style="width:520px"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch();
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
