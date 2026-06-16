---
name: Arctic Vitality
colors:
  surface: '#fcf8ff'
  surface-dim: '#dad7f3'
  surface-bright: '#fcf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f2ff'
  surface-container: '#efecff'
  surface-container-high: '#e8e5ff'
  surface-container-highest: '#e2e0fc'
  on-surface: '#1a1a2e'
  on-surface-variant: '#3f4851'
  inverse-surface: '#2f2e43'
  inverse-on-surface: '#f2efff'
  outline: '#707882'
  outline-variant: '#bfc7d2'
  surface-tint: '#00639a'
  primary: '#006096'
  on-primary: '#ffffff'
  primary-container: '#007abc'
  on-primary-container: '#fdfcff'
  inverse-primary: '#96ccff'
  secondary: '#006876'
  on-secondary: '#ffffff'
  secondary-container: '#58e6ff'
  on-secondary-container: '#006573'
  tertiary: '#006385'
  on-tertiary: '#ffffff'
  tertiary-container: '#007ea7'
  on-tertiary-container: '#fbfcff'
  error: '#C62828'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#cee5ff'
  primary-fixed-dim: '#96ccff'
  on-primary-fixed: '#001d32'
  on-primary-fixed-variant: '#004a75'
  secondary-fixed: '#a1efff'
  secondary-fixed-dim: '#44d8f1'
  on-secondary-fixed: '#001f25'
  on-secondary-fixed-variant: '#004e59'
  tertiary-fixed: '#c2e8ff'
  tertiary-fixed-dim: '#75d1ff'
  on-tertiary-fixed: '#001e2b'
  on-tertiary-fixed-variant: '#004d67'
  background: '#fcf8ff'
  on-background: '#1a1a2e'
  surface-variant: '#e2e0fc'
  background-arctic: '#F0F4F8'
  surface-elevated: '#E8EEF4'
  text-secondary: '#546E7A'
  success: '#2E7D32'
  warning: '#F57C00'
  divider: '#ECEFF1'
  joint-cyan: '#00BCD4'
  session-dark-bg: '#000000'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.05em
  session-rep-count:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '800'
    lineHeight: 64px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-tablet: 32px
  touch-target: 48px
  touch-target-large: 64px
---

## Brand & Style

The design system embodies a **Corporate Modern** aesthetic tailored for the health-tech sector, prioritizing clarity, trust, and precision. It is designed to feel like a high-end medical or fitness professional—expert and reliable, yet encouraging. 

The "Arctic Focus" narrative utilizes a clean, cool-toned environment to reduce cognitive load during high-intensity workouts. The style leverages generous whitespace, a structured grid, and subtle depth to guide the user through complex AI-driven health data. The interface should feel breathable and "clinical-chic," avoiding the aggressive high-contrast "neon-on-black" typical of gym apps in favor of a sophisticated, accessibility-first approach suitable for the Egyptian and MENA professional market.

**Key visual principles:**
- **Clarity over Clutter:** Every element must serve a functional purpose.
- **Cool Sophistication:** Use of the Arctic palette to evoke a sense of calm and focus.
- **Precision:** Perfect alignment and consistent spacing to mirror the accuracy of the AI coaching.

## Colors

The palette is anchored by **Arctic Blue (#0288D1)**, providing a stable, professional foundation. **Cyan (#00BCD4)** serves as the high-energy accent, reserved for critical action points, AI feedback, and active session markers.

**Color Application:**
- **Primary:** Navigation, main CTAs, and brand identifiers.
- **Secondary (Accent):** Highlights, progress indicators, and "Active" state signals.
- **Background:** The soft blue-grey `#F0F4F8` is used for all main app backgrounds to reduce glare compared to pure white.
- **Session Theme:** A specialized Dark Mode is invoked during active workouts (`#000000`) to ensure the camera overlay and skeleton tracking remain the highest-contrast elements in the UI.

## Typography

The design system utilizes **Inter** exclusively to achieve a systematic, utilitarian feel that ensures high legibility across Android devices. 

- **Weight Strategy:** Use `SemiBold` (600) for hierarchy in titles and `Bold` (700+) for major display moments. Body text remains at `Regular` (400) for maximum readability in data-heavy screens.
- **RTL Readiness:** As the target market includes Egypt/MENA, Inter’s neutral character allows for seamless pairing with high-quality Arabic typefaces (like Noto Sans Arabic) in future localizations.
- **Session Typography:** The rep counter uses a specialized `display-lg` variant with extra-bold weight to ensure visibility from a distance when the phone is placed on the floor or a tripod.

## Layout & Spacing

The layout follows a **Fluid Grid** model based on a 4px baseline shift, optimizing for the diverse range of Android screen sizes in the MENA market.

- **Grid:** A 12-column grid is used for Tablet/Desktop, collapsing to a 4-column grid for Mobile.
- **Margins:** Standard mobile margins are set to 16px (`md`).
- **Touch Targets:** A strict minimum of 48px is enforced for all interactive elements. For high-stakes workout controls (Start/Pause/Stop), this is increased to 64px to accommodate sweaty or moving hands.
- **Vertical Rhythm:** Content blocks are separated by `lg` (24px) or `xl` (32px) units to maintain the "Arctic Focus" sense of openness.

## Elevation & Depth

This design system uses **Tonal Layers** combined with **Ambient Shadows** to create a sense of organized hierarchy without visual noise.

- **Surface Levels:** 
    - **Level 0 (Background):** `#F0F4F8` (Arctic Blue-Grey).
    - **Level 1 (Cards/Sheets):** Pure `#FFFFFF` with a very soft, 8% opacity shadow, tinted with the primary blue hue to maintain color harmony.
    - **Level 2 (Active/Elevated):** `#E8EEF4` for interactive elements that are currently being engaged or hovered.
- **Session Depth:** In the dark session theme, depth is conveyed through **Low-contrast outlines** (`#FFFFFF` at 10-15% opacity) rather than shadows, ensuring elements remain crisp against the camera feed.

## Shapes

The shape language is **Rounded**, strike a balance between professional rigor and approachable fitness coaching.

- **Standard Elements:** Buttons, Input Fields, and Cards use a 0.5rem (8px) radius.
- **Featured Elements:** Onboarding "Level Cards" and Progress Summary cards use `rounded-lg` (16px) to appear more inviting and softer.
- **Session Controls:** Buttons used during active workouts (Pause/Stop) are fully circular (Pill/Circle) to distinguish them from standard informational UI.

## Components

### Buttons
- **Primary:** Filled with `#0288D1`, white text, 8px corner radius. Includes a 300ms ripple effect.
- **Session Controls:** 64dp circles, `#00BCD4` (Accent) background for high visibility.
- **Secondary:** Outlined with `#CFD8DC`, text using `#546E7A`.

### Cards
- Surfaces use pure white against the Arctic background.
- Padding should be a minimum of `md` (16px).
- Exercises in lists use a subtle 1px border of `#ECEFF1` instead of heavy shadows.

### Input Fields
- Outlined style using `#CFD8DC` for borders. 
- Focus state switches border to `#0288D1` (Primary) with a 2px stroke.
- Error states use `#C62828` for both the border and the helper text.

### Chips
- Used for goal selection in onboarding.
- **Unselected:** Light grey background with secondary text.
- **Selected:** Cyan background (`#00BCD4`) with white text and a checkmark icon.

### Feedback & Overlays
- **Fatigue Warning:** Sliding bottom banner with `#F57C00` (Warning) background and high-contrast white text.
- **Skeleton Overlay:** Joints are rendered as 8dp circles in Cyan (Correct), Orange (Warning), or Red (Error). Lines connecting joints are white at 40% opacity.