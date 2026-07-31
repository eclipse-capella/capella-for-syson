##################################################################################
# Copyright (c) 2026 Obeo.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Obeo - initial API and implementation
##################################################################################
import os
import xml.etree.ElementTree as ET

# CONFIGURATION
source_folder = "src/main/resources/icons/full/obj16/"
output_file = "icons-grid.svg"
icons_per_row = 10
icon_size = 16
padding = 4  # spacing between icons
cell_size = icon_size + padding

# Create the root SVG element
svg_ns = "http://www.w3.org/2000/svg"
ET.register_namespace("", svg_ns)
sprite_svg = ET.Element("svg", xmlns=svg_ns)

# Collect all SVG file paths recursively
svg_paths = []
for root, _, files in os.walk(source_folder):
    for file in files:
        if file.endswith(".svg"):
            svg_paths.append(os.path.join(root, file))

# Sort for consistent layout
svg_paths.sort()

# Parse and add each SVG
x, y = 0, 0
row_height = cell_size
for idx, path in enumerate(svg_paths):
    tree = ET.parse(path)
    icon = tree.getroot()

    # Wrap icon in <g transform="translate(x, y)">
    g = ET.SubElement(sprite_svg, "g", transform=f"translate({x},{y})")
    for elem in list(icon):
        g.append(elem)

    x += cell_size
    if (idx + 1) % icons_per_row == 0:
        x = 0
        y += row_height

# Set final width and height
num_rows = (len(svg_paths) + icons_per_row - 1) // icons_per_row
sprite_svg.set("width", str(icons_per_row * cell_size))
sprite_svg.set("height", str(num_rows * cell_size))

# Write to output file
tree = ET.ElementTree(sprite_svg)
tree.write(output_file, encoding="utf-8", xml_declaration=True)

print(f"Generated {output_file} with a grid of {len(svg_paths)} icons (recursive).")
