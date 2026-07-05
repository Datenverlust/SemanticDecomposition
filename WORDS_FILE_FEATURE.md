# Words File Feature for `_make_concept_list()`

## Overview

The `_make_concept_list()` function in `tests/test_gpu_decomposition_performance.py` now supports loading test concepts from a text file instead of generating random words.

## Usage

### Option 1: Generate Random Words (Original Behavior)
```python
from tests.test_gpu_decomposition_performance import _make_concept_list

# Generate 50 random concepts (backward compatible)
concepts = _make_concept_list(50)

# Generate 20 random concepts (default)
concepts = _make_concept_list()

# With explicit seed for reproducibility
concepts = _make_concept_list(50, seed=123)
```

### Option 2: Load from Words File (New Feature)
```python
# Load domain-specific concepts from a file
concepts = _make_concept_list(words_file="tests/sample_words.txt")

# Load from custom file
concepts = _make_concept_list(words_file="path/to/your/words.txt")
```

### File Format

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
- No special formatting required

## Sample Words File

A sample `tests/sample_words.txt` is provided with 46 cybercrime-related concepts:

- **Core concepts**: cybercrime, hacking, malware, phishing, ransomware
- **Tools/Methods**: botnet, ddos, rootkit, backdoor, exploit
- **Defenses**: encryption, firewall, patch, protection mechanism
- **Attacks**: social engineering, privilege escalation, zero day
- **Outcomes**: data breach, stolen credentials, compromised system

## Creating Custom Domain Word Files

To test with different domains (healthcare, finance, legal, etc.):

1. Create a new file: `tests/domain_words.txt`
2. Add domain-specific concepts (one per line)
3. Use in tests:
   ```python
   concepts = _make_concept_list(words_file="tests/domain_words.txt")
   ```

### Example: Healthcare Domain

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

## Benefits

✅ **Realistic Testing**: Use actual domain concepts instead of random words
✅ **Semantic Coherence**: All concepts are related within a domain
✅ **Extensibility**: Easy to add new domains for testing
✅ **Reproducibility**: Results consistent across test runs
✅ **Backward Compatible**: Existing tests continue to work unchanged

## Implementation Details

- **Function Signature**: `_make_concept_list(n: int | None = None, seed: int = 42, words_file: str | None = None)`
- **Precedence**: If `words_file` is provided and the file exists, it's used regardless of `n`
- **ID Assignment**: Each concept gets a unique sequential ID (1, 2, 3, ...)
- **File Not Found**: Gracefully falls back to random word generation if file doesn't exist

## Integration with Test Suite

All performance tests maintain backward compatibility:
- ✅ `TestSmallBatch` - all tests pass
- ✅ `TestMediumBatch` - all tests pass  
- ✅ `TestLargeBatch` - all tests pass
- ✅ `TestGPUAvailability` - all tests pass

Total: **10/10 tests passing**

## Next Steps

1. **Domain Expansion**: Create word files for other domains (healthcare, finance, legal, etc.)
2. **Visualization**: Add tools to visualize semantic relationships between file-loaded concepts
3. **Validation**: Implement concept validation to ensure semantic coherence
4. **Performance Tracking**: Compare performance across different domains
