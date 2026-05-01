"""
VibeVision Dining - Dish Mention Extractor
==========================================
Scans the Yelp Open Dataset and extracts the top dish/food mentions
per restaurant business_id using a curated keyword vocabulary.

Output: dish_mentions_by_business.json
  { "<normalized_name>|<normalized_city>": ["Pizza", "Pasta", ...], ... }

  Key format: lowercase, alphanumeric+spaces only, pipe-separated name and city.
  Example: "mario's pizzeria|new york" -> ["Pizza", "Pasta", "Tiramisu"]

Run:
    python extract_dish_mentions.py

Optional env vars:
    YELP_MAX_REVIEWS  - max reviews to scan (default 500000)
    TOP_DISHES        - max dishes to keep per restaurant (default 6)
"""

import json
import os
import re
from collections import defaultdict

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------
BASE_DIR        = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
DATASET_DIR     = os.path.join(BASE_DIR, 'Dataset')
BUSINESS_PATH   = os.path.join(DATASET_DIR, 'yelp_academic_dataset_business.json')
REVIEW_PATH     = os.path.join(DATASET_DIR, 'yelp_academic_dataset_review.json')
OUTPUT_PATH     = os.path.join(os.path.dirname(__file__), 'dish_mentions_by_business.json')

MAX_REVIEWS = int(os.environ.get('YELP_MAX_REVIEWS', '500000'))
TOP_DISHES  = int(os.environ.get('TOP_DISHES', '6'))

# ---------------------------------------------------------------------------
# Keyword vocabulary  (mirrors cuisineSpecificKeywords in RestaurantApiService.kt)
# ---------------------------------------------------------------------------
CUISINE_KEYWORDS: dict[str, set[str]] = {
    'japanese': {
        'ramen', 'sushi', 'sashimi', 'udon', 'tempura', 'gyoza', 'tonkatsu',
        'edamame', 'miso', 'yakitori', 'donburi', 'onigiri', 'takoyaki',
    },
    'mexican': {
        'taco', 'burrito', 'enchilada', 'quesadilla', 'tamale', 'pozole',
        'mole', 'carnitas', 'guacamole', 'nachos', 'chilaquiles', 'tostada',
    },
    'italian': {
        'pizza', 'pasta', 'risotto', 'lasagna', 'gnocchi', 'tiramisu',
        'bruschetta', 'carbonara', 'bolognese', 'ravioli', 'focaccia', 'cannoli',
    },
    'chinese': {
        'dumpling', 'dimsum', 'wonton', 'fried rice', 'lo mein', 'kung pao',
        'mapo tofu', 'peking duck', 'congee', 'char siu', 'bao', 'hot pot',
        'chow mein', 'spring roll', 'egg roll',
    },
    'indian': {
        'curry', 'naan', 'biryani', 'tikka masala', 'dal', 'samosa',
        'tandoori', 'palak paneer', 'chana masala', 'korma', 'vindaloo',
        'raita', 'lassi', 'gulab jamun',
    },
    'thai': {
        'pad thai', 'green curry', 'red curry', 'tom yum', 'som tum',
        'massaman', 'satay', 'larb', 'khao pad', 'mango sticky rice',
    },
    'american': {
        'burger', 'steak', 'ribs', 'wings', 'fries', 'brisket', 'pulled pork',
        'mac and cheese', 'fried chicken', 'biscuit', 'gravy', 'coleslaw',
        'chili', 'clam chowder', 'lobster roll',
    },
    'mediterranean': {
        'falafel', 'shawarma', 'kebab', 'hummus', 'pita', 'gyro', 'tabbouleh',
        'fattoush', 'baklava', 'moussaka', 'tzatziki',
    },
    'korean': {
        'bibimbap', 'bulgogi', 'kimchi', 'japchae', 'tteokbokki', 'galbi',
        'doenjang jjigae', 'sundubu', 'pajeon', 'kimbap',
    },
    'vietnamese': {
        'pho', 'banh mi', 'bun bo hue', 'goi cuon', 'com tam', 'bun cha',
        'ca phe', 'banh xeo', 'che', 'hu tieu',
    },
    'bar': {
        'wings', 'nachos', 'sliders', 'fries', 'burger', 'pretzel',
        'flatbread', 'tacos', 'quesadilla',
    },
}

# Generic fallback used for any cuisine not in the map above
GENERIC_FOOD_KEYWORDS: set[str] = {
    'pizza', 'pasta', 'burger', 'taco', 'burrito', 'ramen', 'sushi', 'sandwich',
    'salad', 'steak', 'fries', 'wings', 'noodles', 'dumplings', 'curry', 'pho',
    'risotto', 'paella', 'falafel', 'shawarma', 'kebab', 'gyoza', 'tempura',
    'ceviche', 'lasagna', 'brisket', 'ribs', 'nachos', 'quesadilla', 'omelet',
    'pancakes', 'waffles', 'chowder', 'soup', 'tiramisu', 'cheesecake',
    'fried chicken', 'fish and chips', 'bibimbap', 'bulgogi', 'banh mi',
    'pad thai', 'green curry', 'dim sum', 'spring roll', 'egg roll', 'bao',
    'croissant', 'waffle', 'crepe', 'quiche', 'souvlaki', 'hummus', 'pita',
    'gyro', 'empanada', 'arepa', 'jerk chicken', 'oxtail', 'plantain',
    'lobster roll', 'clam chowder', 'po boy', 'gumbo', 'jambalaya',
}

ALL_KEYWORDS = GENERIC_FOOD_KEYWORDS.copy()
for kw_set in CUISINE_KEYWORDS.values():
    ALL_KEYWORDS.update(kw_set)


def cuisine_keywords_for(category_str: str) -> set[str]:
    cat = (category_str or '').lower()
    matched: set[str] = set()
    for key, kws in CUISINE_KEYWORDS.items():
        if key in cat:
            matched.update(kws)
    return matched if matched else GENERIC_FOOD_KEYWORDS


def extract_dishes(text: str, vocab: set[str]) -> list[str]:
    """Return all keyword hits found in text (multi-word keywords handled)."""
    lower = text.lower()
    found = []
    for kw in vocab:
        # Use word-boundary aware search so 'pho' doesn't hit 'phone'
        if re.search(r'\b' + re.escape(kw) + r'\b', lower):
            found.append(kw)
    return found


# ---------------------------------------------------------------------------
# Step 1 – collect restaurant business_ids and their cuisine strings
# ---------------------------------------------------------------------------
print("Step 1/3 – scanning businesses …")
restaurant_info: dict[str, tuple[str, str]] = {}   # business_id -> (name, city, categories)

with open(BUSINESS_PATH, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        biz = json.loads(line)
        cats = (biz.get('categories') or '')
        if 'restaurant' in cats.lower():
            bid = biz.get('business_id')
            name = (biz.get('name') or '').strip()
            city = (biz.get('city') or '').strip()
            if bid and name:
                restaurant_info[bid] = (name, city, cats)

print(f"  Found {len(restaurant_info):,} restaurant businesses.")

# ---------------------------------------------------------------------------
# Step 2 – scan reviews and count dish keyword hits per restaurant
# ---------------------------------------------------------------------------
print(f"Step 2/3 – scanning up to {MAX_REVIEWS:,} reviews …")

# { business_id: { dish_keyword: hit_count } }
dish_counts: dict[str, dict[str, int]] = defaultdict(lambda: defaultdict(int))

scanned = 0
with open(REVIEW_PATH, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        review = json.loads(line)
        bid = review.get('business_id')
        if bid not in restaurant_info:
            continue

        text = (review.get('text') or '').strip()
        if not text:
            continue

        vocab = cuisine_keywords_for(restaurant_info[bid][2])
        for dish in extract_dishes(text, vocab):
            dish_counts[bid][dish] += 1

        scanned += 1
        if scanned % 50000 == 0:
            print(f"  … {scanned:,} reviews processed")
        if scanned >= MAX_REVIEWS:
            break

print(f"  Processed {scanned:,} reviews across {len(dish_counts):,} restaurants.")

def normalize_key(name: str, city: str) -> str:
    """Normalize restaurant name+city into a lookup key matching the Android app."""
    def clean(s: str) -> str:
        return re.sub(r'[^a-z0-9 ]', '', s.lower()).strip()
    return f"{clean(name)}|{clean(city)}"


# ---------------------------------------------------------------------------
# Step 3 – build output: top-N dishes per restaurant, title-cased
# ---------------------------------------------------------------------------
print(f"Step 3/3 – writing output (top {TOP_DISHES} dishes per restaurant) …")

output: dict[str, list[str]] = {}
for bid, counts in dish_counts.items():
    info = restaurant_info.get(bid)
    if not info:
        continue
    name, city, _ = info
    key = normalize_key(name, city)
    top = sorted(counts.items(), key=lambda x: x[1], reverse=True)[:TOP_DISHES]
    if top:
        output[key] = [dish.title() for dish, _ in top]

with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
    json.dump(output, f, indent=2)

print(f"Done. {len(output):,} restaurants with dish data written to:")
print(f"  {OUTPUT_PATH}")
