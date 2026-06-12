#!/usr/bin/env python3
"""MCP tool: send image to Agnes Vision API and return description."""
import json, sys, base64, os

AGNES_KEY = "sk-6yfohD4OMlKx8ppGBFdhPEwW1UxetJ3L2dS9YDg0h16gHumV"
AGNES_URL = "https://apihub.agnes-ai.com/v1/chat/completions"

def image_to_base64(path):
    with open(path, "rb") as f:
        return base64.b64encode(f.read()).decode()

def analyze_image(image_path, prompt="详细描述这张图片的内容，包括文字、UI元素、布局等所有细节"):
    import urllib.request
    b64 = image_to_base64(image_path)
    payload = json.dumps({
        "model": "agnes-2.0-flash",
        "messages": [{"role": "user", "content": [
            {"type": "text", "text": prompt},
            {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{b64}"}}
        ]}]
    }).encode()
    req = urllib.request.Request(AGNES_URL, data=payload,
        headers={"Content-Type": "application/json",
                 "Authorization": f"Bearer {AGNES_KEY}"})
    resp = urllib.request.urlopen(req, timeout=60)
    result = json.loads(resp.read())
    return result["choices"][0]["message"]["content"]

# Simple CLI interface
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('Usage: python agnes_vision_mcp.py <image_path> [prompt]')
        sys.exit(1)
    path = sys.argv[1]
    prompt = sys.argv[2] if len(sys.argv) > 2 else "详细描述这张图片的内容"
    if not os.path.exists(path):
        print(f"File not found: {path}")
        sys.exit(1)
    result = analyze_image(path, prompt)
    print(result)
