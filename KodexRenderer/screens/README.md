# Building a dashboard

A dashboard is one JSON file in this directory. The server shows it two ways from the same file:

- as a **picture** for an e-ink device: `GET /render/<name>.png`
- as **JSON** for an app that renders it natively with Piktographs: `GET /content/<name>.json`

This guide builds a dashboard from an empty file. The finished examples are next to it:

| File | Shows | |
|---|---|---|
| [`dashboard.json`](dashboard.json) | Date, two readings, a temperature line, an agenda | [picture](../docs/images/dashboard.png) |
| [`dashboard-landscape.json`](dashboard-landscape.json) | The same in two columns, for a landscape panel | [picture](../docs/images/dashboard-landscape.png) |
| [`home.json`](home.json) | Clock, a grid of rooms from a list, a bar chart, device states | [picture](../docs/images/home.png) |
| [`status.json`](status.json) | Three figures, then a card per server with a meter and a load line | [picture](../docs/images/status.png) |

To see your own file while you edit it:

```shell
./gradlew :KodexRenderer:renderSample -Pscreen=<name>   # writes KodexRenderer/build/render/<name>.png
./gradlew :KodexRenderer:run                            # then open http://localhost:8090/render/<name>.png
```

Files are read on every request, so with the server running you only save and reload.

## 1. A file with content and data

```json
{
  "content": {
    "elements": [
      { "type": "topbar", "constants": { "text": "Home" } },
      { "type": "text", "constants": { "text": "Hello", "style": "headlineMedium" } }
    ]
  },
  "data": {}
}
```

`content.elements` is the page, top to bottom. `data` is whatever your dashboard shows: write it by
hand, or have a script (cron, Home Assistant, a shell one-liner with `jq`) rewrite the file.
Keeping the two apart means the script only ever touches `data`.

Every element has a `type`. Its fixed values go in `constants`; values taken from `data` go in
`processors`.

## 2. Show a value from the data

```json
{
  "content": {
    "elements": [
      {
        "type": "stat",
        "id": "inside",
        "constants": { "text": "Living room", "unit": "°C", "icon": "home" },
        "processors": {
          "value": [{ "type": "path", "elements": ["home", "inside"] }]
        }
      }
    ]
  },
  "data": { "home": { "inside": 21.5 } }
}
```

A processor list is a pipeline: each step gets the previous step's result.

| Step | Does | Example |
|---|---|---|
| `path` | Walks into the data by key (or array index) | `{ "type": "path", "elements": ["home", "inside"] }` → `21.5` |
| `string` | Puts the value into a sentence, at `%` | `{ "type": "string", "element": "Updated at %" }` |
| `styler` | Changes case | see the [Kodices README](../../Kodices/README.md) |
| `map` | Replaces a value with the one listed for it | `{ "type": "map", "values": { "0": "neutral" }, "default": "warning" }` |

If a key is in both `constants` and `processors`, the constant wins.

The server adds the current time to the data as `now`, so a screen can show it without anyone
writing it:

```json
{
  "type": "text",
  "id": "updated",
  "processors": {
    "text": [
      { "type": "path", "elements": ["now", "time"] },
      { "type": "string", "element": "Updated at %" }
    ]
  }
}
```

`now` has `date` ("Thursday, November 19, 2026"), `dateShort` ("Nov 19"), `weekday`, `time`
("14:05"), `time12` ("2:05 PM"), `year` and `iso`. Time zone and language come from
`KODEX_TIMEZONE` and `KODEX_LOCALE`.

## 3. One element per item of a list

`expandWithProcessors` points at an array; the element is repeated for each entry, and inside it
the paths are relative to that entry.

```json
{
  "content": {
    "elements": [
      {
        "type": "listItem",
        "id": "agenda",
        "expandWithProcessors": [{ "type": "path", "elements": ["agenda"] }],
        "constants": { "icon": "calendar" },
        "processors": {
          "text": [{ "type": "path", "elements": ["title"] }],
          "textSecondary": [{ "type": "path", "elements": ["where"] }],
          "value": [{ "type": "path", "elements": ["time"] }]
        }
      }
    ]
  },
  "data": {
    "agenda": [
      { "title": "Stand-up", "where": "Kitchen table", "time": "09:30" },
      { "title": "Dentist", "where": "Dr. Rivera", "time": "12:15" }
    ]
  }
}
```

An empty array gives no elements, so the section disappears on a day with nothing in it. To say so
instead, give the element an `emptyElement`: it is shown in its place when the array is empty or
missing.

```json
{
  "type": "listItem",
  "expandWithProcessors": [{ "type": "path", "elements": ["alerts"] }],
  "processors": { "text": [{ "type": "path", "elements": ["title"] }] },
  "emptyElement": {
    "type": "listItem",
    "constants": { "text": "No alerts", "icon": "check_circle", "tone": "success" }
  }
}
```

A path to a key that an entry does not have logs a warning and leaves that part out. An `action` on
a repeated element is on every copy.

## 4. Lay things out

Elements stack vertically. To put them side by side, nest them:

```json
{
  "type": "grid",
  "constants": { "columns": 2 },
  "nestedElements": [
    { "type": "stat", "constants": { "text": "Inside", "value": 21.5, "unit": "°C" } },
    { "type": "stat", "constants": { "text": "Outside", "value": 7, "unit": "°C" } },
    { "type": "stat", "constants": { "text": "Power", "value": 412, "unit": "W" } }
  ]
}
```

- `grid` gives every child the same width and wraps after `columns`. Put an expanding element
  inside it to get a tile per list entry ([`home.json`](home.json) does this for rooms). With
  `"minColumnWidth": 150` it fits as many columns as are at least that wide, so one screen is two
  columns on a phone and eight in a desktop window; `columns` is then the most it uses. Two cards in
  a grid with `"minColumnWidth": 420` sit side by side when there is room and stack when there is not.
- `container` is a row whose children keep their own width; give a child `"weight": 1.0` to make it
  take the space that is left. `"arrangement": "space_between"` pushes children to the two ends,
  which is how [`home.json`](home.json) puts a badge opposite the clock.
- A `container` with `"orientation": "vertical"` is a column, for stacking inside a row: two of them
  with `"weight": 1.0` each are a two-column page
  ([`dashboard-landscape.json`](dashboard-landscape.json)).
- Older screens say `hrow` and `vrow` for the two. They still render, with a warning in the log.
- `card` draws a border around its title and children ([`status.json`](status.json)).
- `separator` is a gap, or a rule with `"style": "line"`.

## 5. Elements for dashboards

Every key below goes in `constants`, or in `processors` when it comes from the data.

### `text`

```json
{ "type": "text", "constants": { "text": "Agenda", "style": "titleLarge" } }
```

| Key | |
|---|---|
| `text` | What to show |
| `style` | `displayLarge/Medium/Small`, `headline…`, `title…`, `body…`, `label…` (default `bodyLarge`) |
| `align` | `start`, `center`, `end` |
| `fontWeight` | `normal`, `medium`, `semibold`, `bold` |
| `maxLines` | Cut longer text with an ellipsis |
| `fontFamily` | `monospace`, `serif` or `sans` |
| `tone` | `info`, `success`, `warning` or `error`: colored from the theme (see [Tones](#tones)) |
| `color` | `#RRGGBB`. Leave it out for e-ink: anything but black turns grey or disappears |

### `stat`

A figure with its label.

```json
{
  "type": "stat",
  "constants": {
    "text": "Living room", "value": 21.5, "unit": "°C",
    "textSecondary": "2° warmer than yesterday", "trend": "up", "icon": "home"
  }
}
```

| Key | |
|---|---|
| `text` | Label above the figure |
| `value` | The figure: a number or a string (`21.5`, `"18/18"`) |
| `unit` | Shown smaller, after the figure |
| `textSecondary` | Caption underneath |
| `trend` | `up` or `down`: an arrow before the caption |
| `icon` | Icon before the label |
| `align` | `start` (default), `center`, `end` |
| `size` | Of the figure: `large` (default), `medium`, `small` |
| `tone` | Colors the figure |

With an `action` the tile is clickable.

### `listItem`

```json
{
  "type": "listItem",
  "constants": { "text": "Front door", "textSecondary": "Locked at 08:12", "icon": "lock", "value": "Locked" }
}
```

| Key | |
|---|---|
| `text`, `textSecondary` | Title and subtitle |
| `icon` | Icon at the start |
| `value` | Text at the end: a number or a string |
| `badge` | A short label in a pill before the value; a blank one is left out |
| `trailingIcon` | Icon at the end |
| `tone` | Colors the icon and the badge |
| `variant` | `tonal`: the line on a rounded fill in its tone, for an alert |
| `dense` | `true`: smaller text and less padding, for long lists and key/value lines |

### `chart`

One series, as a line or as bars. No axes: it shows the shape of the numbers.

```json
{
  "type": "chart",
  "constants": {
    "text": "Energy this week, kWh",
    "variant": "bar",
    "values": [8.2, 7.4, 9.1, 0, 0, 0, 0],
    "labels": ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
  }
}
```

| Key | |
|---|---|
| `values` | Array of numbers. Usually from the data: `"processors": { "values": [{ "type": "path", "elements": ["energy", "week"] }] }` |
| `variant` | `line` (default) or `bar` |
| `height` | Height of the plot in dp (default 64) |
| `min`, `max` | Value range. By default a line spans its own lowest to highest value, and bars start at zero. Set both to compare charts, as [`status.json`](status.json) does for CPU load |
| `labels` | Array of strings under the plot: one per bar, or spread evenly under a line |
| `text`, `textSecondary` | Title above, caption below |
| `tone` | Draws the series in that color |

Fewer `labels` than bars mark equal stretches of them: `["00", "06", "12", "18"]` under 24 hourly
bars sit under bars 0, 6, 12 and 18.

### `badge`

```json
{ "type": "badge", "constants": { "text": "Armed", "variant": "filled" } }
```

`variant` is `filled` (default), `outlined` or `tonal` (a faint fill). `tone` colors it. A badge
with no text draws nothing, so one bound to a value that is usually empty needs no special case.

### `tableHeader` and `tableRow`

A table is a header and one `tableRow` per entry of an array, sharing the same `columns`. Rows are
elements of the page, so a table of hundreds of rows scrolls like any other list.

```json
{ "type": "tableHeader", "constants": { "columns": [
  { "key": "time", "label": "Time", "width": 104, "display": "mono" },
  { "key": "level", "label": "Level", "width": 68, "display": "badge" },
  { "key": "message", "label": "Message", "display": "mono", "primary": true }
] } },
{
  "type": "tableRow",
  "constants": { "dense": true, "columns": [
    { "key": "time", "label": "Time", "width": 104, "display": "mono" },
    { "key": "level", "label": "Level", "width": 68, "display": "badge" },
    { "key": "message", "label": "Message", "display": "mono", "primary": true }
  ] },
  "expandWithProcessors": [{ "type": "path", "elements": ["lines"] }],
  "processors": {
    "values": { "type": "path" },
    "tone": [{ "type": "path", "elements": ["severity"] }]
  },
  "emptyElement": { "type": "text", "constants": { "text": "The log is empty" } }
}
```

`"values": { "type": "path" }` hands the row its whole array entry, so each column's `key` is a key
of the entry. A value can also be bound on its own (`"processors": { "time": [...] }`), which wins.

| Column key | |
|---|---|
| `key` | Which of the row's values the cell shows |
| `label` | The column title |
| `width` or `weight` | A fixed width in dp, or a share of what is left (default `weight` 1) |
| `align` | `start` (default), `center`, `end` |
| `display` | `text` (default), `mono` (ids, timestamps, log lines), `badge` (a pill) or `icon` (the value is an icon name) |
| `secondaryKey` | A second value under the first, smaller |
| `toneKey` | Which of the row's values is this cell's tone. Without it a `badge` or `icon` cell takes the row's `tone` |
| `maxLines` | Default 2 |
| `primary` | `true` on the column that titles a collapsed row (default: the first text column) |

| Row key | |
|---|---|
| `tone` | A `warning` or `error` row gets a faint fill |
| `dense` | `true`: less padding, smaller text |
| `collapseBelow` | In dp, default 600. In less width the row stacks: the primary cell as a title, the other values under it with their labels, and the header draws nothing. Give the header the same value. `0` never collapses |

With an `action` the row is clickable.

### `tabs`

```json
{ "type": "tabs", "nestedElements": [
  { "type": "tab", "constants": { "text": "Overview", "icon": "home", "selected": true } },
  { "type": "tab", "constants": { "text": "Logs", "icon": "list" },
    "action": { "type": "navigate", "constants": { "targetRoute": "logs" } } }
] }
```

Links between screens. Each screen has the same tabs with `"selected": true` on its own. As a
top-level element the row stays above the page while the rest scrolls. See
[Link screens](#7-link-screens-apps-only).

### Tones

`text`, `badge`, `stat`, `listItem`, `chart` and `tableRow` take a `tone`: `info`, `success`,
`warning` or `error`. Log-level names (`WARN`, `ERROR`...) and `ok`, `danger`, `critical` work too;
anything else is neutral. The app's theme decides the colors, and on e-ink every tone is black.

A tone usually comes from the data, either as it is or through `map`:

```json
{
  "type": "stat",
  "constants": { "text": "Push failures" },
  "processors": {
    "value": [{ "type": "path", "elements": ["today", "pushFailures"] }],
    "tone": [
      { "type": "path", "elements": ["today", "pushFailures"] },
      { "type": "map", "values": { "0": "neutral" }, "default": "error" }
    ]
  }
}
```

### `progress`

```json
{ "type": "progress", "constants": { "text": "Disk", "textSecondary": "5.7 of 8 TB used", "progress": 0.71 } }
```

`progress` is 0 to 1. `"variant": "circular"` draws a ring.

### `image`

```json
{ "type": "image", "constants": { "url": "asset:kodices.png", "width": 96, "height": 96, "cornerRadius": 16 } }
```

`asset:<file>` is a file in [`assets/`](assets); it loads without network access. `http(s)://`
URLs work too, and the picture is only taken once they have loaded.

### Icons

`icon` and `trailingIcon` take a name: `home`, `place`, `calendar`, `email`, `phone`, `lock`,
`notifications`, `person`, `settings`, `search`, `star`, `favorite`, `info`, `warning`, `check`,
`check_circle`, `close`, `add`, `edit`, `delete`, `refresh`, `menu`, `list`, `send`, `share`,
`build`, `cart`, `play`, `arrow_up`, `arrow_down`, `back`, `forward`. An unknown name shows
nothing in `stat` and `listItem`.

## 6. Make it fit the device

A picture does not scroll. The default panel is 954×1696 px at 2 px per dp, so the page is
**477×848 dp**; in landscape 848×477 dp. Anything past the bottom edge is cut off, so render the
screen and look at it.

Rough heights at the e-ink sizes: top bar 64 dp, `stat` 110 dp, `listItem` with a subtitle 82 dp,
a `chart` its `height` plus about 70 dp with a title and labels.

When it does not fit: fewer list entries (trim the array in the script that writes `data`), a
lower chart `height`, more `grid` columns, or a second screen.

A layout suits one orientation. Name the other one `<name>-landscape.json` and point the landscape
device at it.

## 7. Link screens (apps only)

An app can move between screens; a picture cannot. An element with a `navigate` action opens
another screen by name in [KodexViewer](../../KodexViewer/README.md) and in the SampleApp's
Dashboard tab, and is simply not clickable on e-ink:

```json
{
  "type": "listItem",
  "constants": { "text": "Servers", "icon": "list", "trailingIcon": "forward" },
  "action": { "type": "navigate", "constants": { "targetRoute": "status" } }
}
```

A screen with a [`tabs`](#tabs) element brings its navigation with it: the viewer then leaves out
its own list of screens.

To see a screen the way an app draws it, in color and at a window's size rather than the panel's:

```shell
./gradlew :KodexViewer:renderScreen -Pfile=KodexRenderer/screens/status.json -Psize=1280x900
```

## When something is wrong

| You see | It means |
|---|---|
| `422` from `/render` or `/content` | The file is not valid JSON, or an element has no `type`. The server log says which |
| `"<type>" is not a supported element` on the page | A typo in `type` |
| An element with no text | Its values are outside `constants`/`processors`, or a path does not match the data. `GET /content/<name>.json` shows the data exactly as the processors see it |
| A repeated element that never appears | `expandWithProcessors` does not point at an array |
| `asset:` image missing | The file is not in `assets/` |
