#!/usr/bin/env python3
"""
Clean reference.png by removing text content and leaving only UI elements.
This creates a template suitable for use as texture atlas.
"""

from PIL import Image, ImageDraw
import json

def clean_reference(input_path, output_path, regions_to_clear):
    """
    Clean reference image by filling specified regions with solid colors.
    
    Args:
        input_path: Path to reference.png
        output_path: Path to save cleaned image
        regions_to_clear: List of (x, y, w, h, color) tuples
    """
    img = Image.open(input_path)
    draw = ImageDraw.Draw(img)
    
    for region in regions_to_clear:
        x, y, w, h, color = region
        draw.rectangle([x, y, x + w, y + h], fill=color)
    
    img.save(output_path)
    print(f"Cleaned reference saved to {output_path}")

def main():
    # Define regions to clear based on VS Code layout
    # Format: (x, y, width, height, fill_color)
    
    regions_to_clear = [
        # Activity bar icons - fill with activity bar background
        (0, 43, 52, 950, (51, 51, 51)),
        
        # Sidebar file tree area - fill with sidebar background
        (52, 43, 256, 950, (37, 37, 38)),
        
        # Editor area - fill with editor background
        (308, 80, 1612, 913, (30, 30, 30)),
        
        # Tab bar text - fill with tab background
        (308, 43, 1612, 37, (45, 45, 45)),
        
        # Title bar text - fill with title background
        (0, 0, 1920, 43, (60, 60, 60)),
        
        # Status bar text - fill with status background
        (0, 993, 1920, 27, (0, 122, 204)),
    ]
    
    clean_reference('reference.png', 'reference_clean.png', regions_to_clear)
    print("\nNext steps:")
    print("1. Review reference_clean.png")
    print("2. Manually edit if needed (remove any remaining text)")
    print("3. Replace reference.png with cleaned version")
    print("4. Re-run asset slicer to generate new atlas")

if __name__ == '__main__':
    main()
