<!--
  Copyright (c) 2026 Obeo.
  This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v2.0
  which accompanies this distribution, and is available at
  https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors:
      Obeo - initial API and implementation
-->
# capella-model-edit

This project contains the **SVG icon set** for the Capella metamodel. It centralizes all the icons used to represent core model elements in both the desktop and web-based tooling environments (e.g. Capella Studio, Sirius Web).

## Purpose

The icons in this repository are designed with a focus on:
- **Visual clarity and consistency** across all abstraction layers (logical, physical, system, etc.)
- **Accessibility and contrast** (tested for 16x16 and 24x24 resolutions)
- **Alignment with modern UI principles**, and where applicable, compatibility with Sirius Web visual language

## Fundamentals

Font used for the text elements : Roboto condensed, in bold. Check out `_Function-wip-base with font.svg`to have an example you can copy from. Then make sure to transform the text into path.

**SVG Text elements should be transformed** to **path** using the `Object to path` elements in order to avoid the dependency to a font which might not be installed.

## Structure

All icons are located in:

```
src/main/resources/icons/full/obj16/

````

They follow the naming conventions of Capella element types and use SVG format to ensure scalability and crisp rendering on all displays.

## Optimizing with SVGO

Before committing new or updated icons, please run the **SVGO optimizer** to clean up and normalize SVG files. 
From the project root folder:

```bash
svgo --config svgo.config.js src/main/resources/icons/full/obj16/*.svg
````

This step:

* Reduces file size
* Ensures consistent formatting
* Removes unnecessary metadata

## Contribution Guidelines

When adding a new icon:

1. Follow the style used for similar elements (e.g., color palette, stroke width, layout).
2. Ensure good readability at 16x16 resolution.
3. Run the SVGO command above before committing.
4. Include only minimal metadata in the SVG (no Inkscape/Illustrator junk).
5. Test against a dark and light background if possible.

