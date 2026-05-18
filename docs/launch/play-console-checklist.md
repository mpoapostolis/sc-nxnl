# Skin Coach — Google Play launch checklist

The app file and all the copy are ready. This is what **you** do in the
browser — I can't create your account or click through the Console for you.

## 0. Developer account (one-time)

- Go to **play.google.com/console** and sign up as a developer.
- One-time fee: **$25**. Identity verification can take a day or two.

## 1. Create the app

- Play Console → **Create app**.
- App name: `Skin Coach` · Default language: English · Type: App · Free.

## 2. Upload the app file

- The signed file is at:
  `app/build/outputs/bundle/release/app-release.aab`
- **Recommended:** upload it to the **Internal testing** track first, not
  Production. That lets you install it from Play and check everything before
  the public sees it. Promote to Production once you're happy.
- Release → Testing → Internal testing → Create new release → upload the `.aab`.

## 3. Store listing

- Use the text in `play-store-listing.md` (title, short & full description).
- Category: **Beauty**.
- Assets you must supply:
  - **App icon** — 512×512 PNG.
  - **Feature graphic** — 1024×500 PNG.
  - **Phone screenshots** — 2–8 images. Grab them on your phone from the
    Home, Results, Today and Progress screens.

## 4. Privacy policy

- First, open `privacy-policy.html` and replace `[YOUR CONTACT EMAIL]` with a
  real email.
- Host the file somewhere public — **GitHub Pages, Netlify Drop or Cloudflare
  Pages** are all free — and paste that URL into the listing.

## 5. Data safety form

- Skin Coach processes the camera photo **on-device** and does not transmit or
  collect any data. Answer the questionnaire accordingly: no data collected,
  no data shared. (Camera is a permission, not data collection.)

## 6. Content rating

- Fill the questionnaire honestly → it will come out **Everyone**.

## 7. Countries & pricing

- Free app. Choose which countries to launch in (or all of them).

## 8. Submit

- Submit for review. The first review for a new account can take several days.

---

## Keep this safe — important

- `release-key.jks` and `keystore.properties` are your **signing key**. If you
  ever change phones or computers, you need them to publish app updates.
- **Back both files up** somewhere safe. They are gitignored — never committed
  to the repo — so they exist only on this machine right now.
- The keystore password is inside `keystore.properties`.
