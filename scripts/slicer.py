import json
import os
from PIL import Image

def slice_assets(image_path, json_path, output_dir):
    """
    Slices an image into multiple regions based on coordinates in a JSON file.
    """
    if not os.path.exists(image_path):
        print(f"Error: {image_path} not found.")
        return

    if not os.path.exists(json_path):
        print(f"Error: {json_path} not found.")
        return

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    # Load image
    img = Image.open(image_path)
    print(f"Loaded {image_path} ({img.width}x{img.height})")

    # Load JSON regions
    with open(json_path, 'r') as f:
        regions = json.load(f)

    for name, coords in regions.items():
        x, y, w, h = coords['x'], coords['y'], coords['w'], coords['h']
        
        # Crop region
        region_img = img.crop((x, y, x + w, y + h))
        
        # Save region
        output_path = os.path.join(output_dir, f"{name}.png")
        region_img.save(output_path)
        print(f"Saved: {output_path} ({w}x{h})")

    # Also create a combined atlas if needed, but for now individual slices are standard
    print("\nAsset processing complete. Slices saved to:", output_dir)

if __name__ == "__main__":
    # Settings based on KHGui structure
    IMAGE_PATH = "reference.png"
    JSON_PATH = "common/src/main/resources/assets/khgui/textures/gui/vscode_ui.json"
    OUTPUT_DIR = "common/src/main/resources/assets/khgui/textures/gui/slices"

    # Note: Using relative paths assuming script is run from project root
    slice_assets(IMAGE_PATH, JSON_PATH, OUTPUT_DIR)
