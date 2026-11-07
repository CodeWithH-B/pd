# PhotoCraft for Android

<p align="center">
  <b>Professional layered image editing app built with Kotlin & Jetpack Compose, rewritten from PhotoCraft.</b><br>
  Layers, blend modes, adjustments, filters, brushes, vector shapes, type, and high-fidelity rendering on Android.
</p>

## Overview

PhotoCraft brings Photoshop-inspired creative image editing to Android with a modern, touch-optimized Material 3 UI.

### Features

- **Layer System**:
  - Multi-layer workflow with raster image layers, vector shape layers, type layers, and non-destructive adjustment layers.
  - 10 blend modes: Normal, Multiply, Screen, Overlay, Darken, Lighten, Color Dodge, Color Burn, Hard Light, Difference.
  - Per-layer opacity (0–100%), visibility toggle, lock toggle, duplicate, merge down, and reorder.
- **Tool Palette**:
  - **Move & Transform**: Drag and reposition layers with interactive visual bounding-box handles.
  - **Brush**: Freehand painting with customizable color, size, and opacity.
  - **Eraser**: Pixel erasure on active raster layers.
  - **Shapes**: Add vector shapes (Rectangles, Ellipses, Stars, Lines, Rounded Rectangles) with fill and stroke styling.
  - **Type**: Add and edit text layers with custom sizing, colors, bold, and italic styles.
  - **Eyedropper**: Sample color directly from any canvas pixel into the active brush.
  - **Crop**: Aspect ratio presets (Free, 1:1, 4:3, 16:9, 9:16).
- **Adjustments & Color Grading**:
  - Brightness & Contrast
  - Hue & Saturation
  - Levels (Shadows, Midtones/Gamma, Highlights) with histogram visualization
  - Vibrance
  - Color Balance (Cyan/Red, Magenta/Green, Yellow/Blue)
  - Black & White and Invert
- **Filters**:
  - Fast Gaussian Blur, Sharpening, Pixelate / Mosaic, Sepia Tone, Vignette, Posterize, and Emboss.
- **History & Undo/Redo**:
  - Complete history timeline with jump-to-state support and full undo/redo stack.
- **Export & Share**:
  - Export to PNG (lossless), JPEG, or WebP with adjustable compression quality.
  - Save directly to device storage (`Pictures/PhotoCraft`) or share via Android system share sheet.
- **Sample Projects**:
  - Preloaded with classic artwork presets (*The Great Wave off Kanagawa* by Hokusai, *Impression, Sunrise* by Monet, and Blank Canvas).
  - Import any photo from device storage using the zero-permission Android Photo Picker.

## Architecture

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with `StateFlow` and unidirectional data flow
- **Rendering**: Hardware-accelerated canvas compositing with PorterDuff transfer modes and ColorMatrix pipelines
