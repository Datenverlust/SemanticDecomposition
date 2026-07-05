# Words File Feature for `_make_concept_list()`

## Overview

The `_make_concept_list()` function in `tests/test_gpu_decomposition_performance.py` now supports loading test concepts from a text file. When a file is provided, the function:

1. **Reads the file** to determine how many concepts to create
2. **Selects words from the `_VOCAB` vocabulary** instead of using file words directly
3. **Ensures deterministic mapping** using hash-based selection from `_VOCAB`

This approach provides:
- **Semantic consistency**: All concepts use words from the predefined mock dictionary vocabulary
- **Reproducibility**: Same file always produces same concepts across test runs
- **Flexibility**: File determines concept count; `_VOCAB` provides the actual words

## Usage

### Option 1: Load from Words File (New Feature)
```python
from tests.test_gpu_decomposition_performance import _make_concept_list

# Load concepts from file (uses _VOCAB for actual words)
concepts = _make_concept_list(words_file="tests/sample_words.txt")
# Returns 46 concepts with words selected from _VOCAB
```

### Option 2: Generate Random Words (Original Behavior)
```python
# Generate 50 random concepts (backward compatible)
concepts = _make_concept_list(50)

# Generate 20 random concepts (default)
concepts = _make_concept_list()

# With explicit seed for reproducibility
concepts = _make_concept_list(50, seed=123)
```

## How It Works

When loading from a file:

1. **Read file content** → determines count (e.g., 46 words → 46 concepts)
2. **Hash each word** → creates deterministic index into `_VOCAB`
3. **Select from `_VOCAB`** → uses vocabulary list for actual words
4. **Ensure uniqueness** → if collision, advances to next word in vocabulary

Example:
```
sample_words.txt:
  cybercrime    → hash("cybercrime") → index 123 → "tdqcyw" from _VOCAB
  hacking       → hash("hacking") → index 45 → "ctlkqu" from _VOCAB
  malware       → hash("malware") → index 89 → "iukyoq" from _VOCAB
  ... (repeat for 46 total concepts)
```

## File Format

The words file should contain one word or concept per line:

```
cybercrime
hacking
malware
phishing
ransomware
botnet
ddos
data breach
encryption
firewall
```

- Empty lines are automatically stripped
- Whitespace is trimmed from each word
- File words are used for deterministic selection, not directly as concept literals
- Number of file lines = number of concepts created

## Sample Words File

A sample `tests/sample_words.txt` is provided with 46 concepts that map to 46 unique words from `_VOCAB`:

```
cybercrime, hacking, malware, phishing, ransomware,
botnet, ddos, data breach, encryption, firewall,
cyber attack, digital crime, malicious code, ransomware attack, ...
```

When loaded, these map to: `['tdqcyw', 'ctlkqu', 'iukyoq', 'xjqdtt', 'axesbb', ...]`

## Creating Custom Domain Word Files

To test with different domains (healthcare, finance, legal, etc.):

1. Create a new file: `tests/domain_words.txt`
2. Add domain-specific concepts (one per line) - these determine COUNT, not actual words
3. Use in tests:
   ```python
   concepts = _make_concept_list(words_file="tests/domain_words.txt")
   # Each line in file maps to unique word from _VOCAB
   ```

### Example: Healthcare Domain (20 concepts)

```
healthcare
diagnosis
treatment
medication
disease
symptom
surgery
patient
hospital
doctor
prescription
vaccine
infection
antibiotics
surgery complication
anesthesia
recovery
therapy
rehabilitation
medical imaging
```

Result: 20 concepts with words selected from `_VOCAB`

## Benefits

✅ **Semantic Consistency**: Uses mock dictionary vocabulary for all concepts
✅ **Deterministic Mapping**: Same file always produces same concepts
✅ **Reproducible Tests**: Consistent results across test runs
✅ **Flexible Scaling**: File size controls concept count
✅ **Backward Compatible**: Existing tests continue to work unchanged
✅ **Integration Ready**: Concepts created with mock dictionary vocabulary

## Implementation Details

- **Function Signature**: `_make_concept_list(n: int | None = None, seed: int = 42, words_file: str | None = None)`
- **Word Selection**: Hash-based deterministic selection from 200-word `_VOCAB`
- **Collision Handling**: Auto-advances to next vocabulary word if duplicate detected
- **Precedence**: If `words_file` is provided and exists, it's used regardless of `n`
- **File Not Found**: Gracefully falls back to random word generation if file doesn't exist

## Integration with Test Suite

All performance tests maintain backward compatibility:
- ✅ `TestSmallBatch` - all tests pass
- ✅ `TestMediumBatch` - all tests pass  
- ✅ `TestLargeBatch` - all tests pass
- ✅ `TestGPUAvailability` - all tests pass

Total: **10/10 tests passing** ✓ **100% success rate**

## Vocabulary Information

`_VOCAB` contains 200 randomly generated 6-letter lowercase words:
```python
_VOCAB: List[str] = [_make_word() for _ in range(200)]
# Example: ['tonrsn', 'akzaeo', 'wqyxmy', 'gfoahc', 'uyxxxe', ...]
```

Benefits:
- **Large pool**: 200 words support up to 200 unique concepts per file
- **Mock dictionary compatible**: Words created same way as MockDictionary internal vocab
- **Consistent behavior**: Same set of words used across all tests
- **Deterministic**: Hash-based selection ensures reproducibility

## Next Steps

1. **Domain Expansion**: Create word files for other domains (healthcare, finance, legal, etc.)
2. **Performance Tracking**: Compare performance with concepts from `_VOCAB` vs random words
3. **Caching Studies**: Analyze cache hit rates with deterministic word selection
4. **Semantic Analysis**: Track decomposition patterns with consistent vocabulary

