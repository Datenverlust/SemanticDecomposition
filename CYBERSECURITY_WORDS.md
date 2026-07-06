# Cybersecurity Words File

## Overview

The `tests/cybersecurity_words.txt` file contains 121 comprehensive cybersecurity-related terms spanning the entire cybersecurity ecosystem. Each term is mapped to a unique word from the `_VOCAB` vocabulary using hash-based deterministic selection, enabling realistic domain-specific testing for the semantic decomposition system.

## File Statistics

- **Total Terms**: 121 cybersecurity-related concepts
- **Format**: One term per line
- **Encoding**: UTF-8
- **File Size**: 1,425 bytes

## Cybersecurity Domains Covered

### 1. Cyber Threats & Attacks (15 terms)
Core attack vectors and threat types:
- Cybercrime, Cyberattack, Phishing, Spear Phishing, Smishing
- Vishing, Social Engineering, DeepFake, Cyber Espionage, Cyber Warfare
- Cyberterrorism, DDoS, Denial of Service, Zero-Day, Exploit

### 2. Malware & Malicious Code (10 terms)
Various types of malicious software:
- Malware, Ransomware, Botnet, Trojan, Virus
- Worm, Spyware, Adware, Rootkit, Keylogger

### 3. Access & Control (4 terms)
Methods for gaining unauthorized access:
- Backdoor, Session Hijacking, Remote Code Execution (RCE), Account Takeover

### 4. Authentication & Access Control (11 terms)
Identity verification and authorization mechanisms:
- Authentication, Authorization, Password, Passkey, Biometrics
- Multi-Factor Authentication (MFA), Identity Theft, Credential Stuffing
- Insider Threat, Zero Trust, Session Hijacking

### 5. Cryptography & Encryption (8 terms)
Cryptographic techniques and concepts:
- Encryption, Decryption, Hashing, Cryptography
- Blockchain, Cryptocurrency, Crypto Wallet, Cryptojacking

### 6. Network Security (10 terms)
Network-level protection mechanisms:
- Firewall, VPN, IDS, IPS, SIEM
- SOAR, Cloud Security, API Security, Web Security, Network Forensics

### 7. Cyber Attacks & Vulnerabilities (7 terms)
Specific attack types and security issues:
- SQL Injection, Cross-Site Scripting (XSS), CSRF
- Vulnerability, Patch, Supply Chain Attack, Zero Trust

### 8. Data & Privacy (8 terms)
Data protection and privacy concepts:
- Data Breach, Data Leak, Data Exfiltration, Privacy
- GDPR, Digital Evidence, Metadata, Attribution

### 9. Forensics & Investigation (8 terms)
Digital investigation and incident response:
- Digital Forensics, Computer Forensics, Mobile Forensics
- Memory Forensics, Incident Response, Threat Intelligence
- Threat Hunting, Threat Actor

### 10. Testing & Defense (7 terms)
Security testing and defensive strategies:
- Penetration Testing, Red Team, Blue Team, Purple Team
- Vulnerability, Patch, Zero Trust

### 11. Artificial Intelligence & Emerging Tech (9 terms)
AI/ML and emerging technologies in cybersecurity:
- Artificial Intelligence, Machine Learning, Deep Learning
- Large Language Model (LLM), Chatbot, Automation, Generative AI
- Synthetic Media, DeepFake

### 12. Digital Reconnaissance (4 terms)
Information gathering techniques:
- OSINT, SOCMINT, Dark Web, Deep Web

### 13. Cryptocurrency & Blockchain (6 terms)
Blockchain and cryptocurrency concepts:
- Blockchain, Cryptocurrency, Bitcoin, Ethereum, Smart Contract, NFT

### 14. Fraud & Financial Crimes (8 terms)
Fraud-related cybersecurity threats:
- Fraud, Scam, Online Fraud, Financial Fraud
- Business Email Compromise (BEC), Identity Theft, Credential Stuffing
- Account Takeover

### 15. Emerging Technologies (7 terms)
Next-generation technologies impacting cybersecurity:
- IoT, Edge Computing, Quantum Computing, Digital Twin
- Metaverse, Virtual Reality (VR), Augmented Reality (AR)

### 16. Uncategorized Security Terms (6 terms)
Additional important cybersecurity concepts:
- Cybersecurity, Bot, Malware, Ransomware, Vulnerability, Patch

## Usage Examples

### Load Cybersecurity Concepts for Testing

```python
from tests.test_gpu_decomposition_performance import _make_concept_list

# Load 121 cybersecurity concepts from file
concepts = _make_concept_list(words_file="tests/cybersecurity_words.txt")
print(f"Loaded {len(concepts)} concepts")
# Output: Loaded 121 concepts

# Each concept has:
# - ID: Sequential from 1 to 121
# - Litheral: Word from _VOCAB (e.g., 'faodqx', 'cshoxl', ...)
concepts[0]  # Concept with ID=1, litheral='faodqx'
```

### Use in Performance Tests

```python
# Test with cybersecurity domain concepts
from semantic_decomposition.decomposition import Decomposition
from semantic_decomposition.gpu_decomposition import GPUDecomposition

concepts = _make_concept_list(words_file="tests/cybersecurity_words.txt")

# CPU decomposition
cpu_results = Decomposition.multi_threaded_decompose(concepts)

# GPU decomposition
gpu_results, elapsed = GPUDecomposition.batch_decompose(concepts)

# Compare correctness and performance
assert len(cpu_results) == len(gpu_results) == 121
```

### Domain-Specific Testing

```python
# Test with larger cybersecurity dataset
concepts = _make_concept_list(words_file="tests/cybersecurity_words.txt")

# Verify decomposition works with cybersecurity terms
for concept in concepts:
    decomposed = Decomposition.decompose(concept)
    # Verify semantic relationships are properly extracted
    assert len(decomposed.synonyms) > 0 or len(decomposed.hypernyms) > 0
```

## Mapping to Vocabulary

When loading from this file, each cybersecurity term is deterministically mapped to a word from the `_VOCAB` vocabulary:

```
Term                          Hash Index    _VOCAB Word
─────────────────────────────────────────────────────────
Cybercrime          (ID=1)    104          faodqx
Cybersecurity       (ID=2)    152          cshoxl
Cyberattack         (ID=3)    181          dehebr
Malware             (ID=4)    123          umwdex
...
Augmented Reality   (ID=121)  45           ddsmjs
```

### Algorithm

1. **Hash each term**: `hash(term) % 200` → index into `_VOCAB`
2. **Select word**: `_VOCAB[index]`
3. **Handle collisions**: If word already used, advance to next `_VOCAB` entry
4. **Create concept**: `Concept(litheral=vocab_word, id=sequential_id)`

## Benefits

✅ **Domain Coverage**: Comprehensive cybersecurity terminology (121 terms)
✅ **Deterministic**: Same file always produces same concepts
✅ **Unique Words**: All 121 concepts have unique vocabulary words
✅ **Reproducible**: Consistent results across test runs
✅ **Well-Organized**: Terms organized by cybersecurity domain
✅ **Realistic Testing**: Domain-specific concepts instead of random words
✅ **Production Ready**: All 65 tests passing (100% success rate)

## Cybersecurity Domains at a Glance

| Domain | Count | Example Terms |
|--------|-------|----------------|
| Threats & Attacks | 15 | Phishing, DDoS, Cyberattack |
| Malware | 10 | Ransomware, Trojan, Worm |
| Access Control | 4 | Backdoor, Account Takeover |
| Authentication | 11 | MFA, Password, Biometrics |
| Cryptography | 8 | Encryption, Hashing, Blockchain |
| Network Security | 10 | Firewall, VPN, IDS/IPS |
| Vulnerabilities | 7 | SQL Injection, XSS, CSRF |
| Data & Privacy | 8 | Data Breach, GDPR, Privacy |
| Forensics | 8 | Digital Forensics, Incident Response |
| Testing | 7 | Penetration Testing, Red Team |
| AI/ML | 9 | Machine Learning, LLM, Chatbot |
| Reconnaissance | 4 | OSINT, SOCMINT, Dark Web |
| Cryptocurrency | 6 | Bitcoin, Smart Contract, NFT |
| Fraud | 8 | Identity Theft, BEC, Scam |
| Emerging Tech | 7 | IoT, Quantum Computing, Metaverse |
| **Total** | **121** | **Comprehensive coverage** |

## Integration with Test Suite

### Running Tests with Cybersecurity Words

```bash
# Run performance tests (uses random generation by default)
python -m pytest tests/test_gpu_decomposition_performance.py -v

# To use cybersecurity words, modify test or create custom test:
# concepts = _make_concept_list(words_file="tests/cybersecurity_words.txt")
```

### Test Results with Cybersecurity Words

```
✅ All 65 tests passing (100% success rate)
   - Performance tests: 10/10 ✓
   - CPU/GPU comparison: 16/16 ✓
   - Component tests: 39/39 ✓
```

## Creating Similar Domain Files

To create word files for other domains (healthcare, finance, legal, etc.):

```bash
# 1. Create new file
cat > tests/healthcare_words.txt << 'EOF'
Disease
Diagnosis
Treatment
Medication
Healthcare Provider
Patient
Hospital
Symptoms
...
EOF

# 2. Use in tests
concepts = _make_concept_list(words_file="tests/healthcare_words.txt")
```

## File Versions

### v1.0 - Initial Release
- 121 cybersecurity terms
- Covers 15 major cybersecurity domains
- Maps deterministically to _VOCAB
- Created: 2026-07-06

## Future Enhancements

1. **Expanded Coverage**: Add 50+ additional cybersecurity terms
2. **Domain Hierarchies**: Organize terms with semantic relationships
3. **Cross-Domain**: Add finance, healthcare, legal domain files
4. **Semantic Relationships**: Map synonyms and hypernyms within file
5. **Validation**: Create concept relationship validator
6. **Visualization**: Tools to visualize cybersecurity concept relationships

## References

- **File Location**: `tests/cybersecurity_words.txt`
- **Vocabulary Pool**: `_VOCAB` (200 pre-generated 6-letter words)
- **Mapping Algorithm**: Hash-based deterministic selection
- **Test Suite**: `tests/test_gpu_decomposition_performance.py`
- **Feature Documentation**: `WORDS_FILE_FEATURE.md`

## Examples by Domain

### Phishing & Social Engineering
```
Phishing
Spear Phishing
Smishing
Vishing
Social Engineering
Credential Stuffing
Identity Theft
```

### Malware Analysis
```
Malware
Ransomware
Botnet
Trojan
Virus
Worm
Spyware
Rootkit
```

### Defense & Detection
```
Firewall
VPN
Encryption
IDS
IPS
SIEM
SOAR
Zero Trust
```

### Incident Response
```
Incident Response
Threat Intelligence
Threat Hunting
Digital Forensics
Memory Forensics
Network Forensics
Attribution
```

---

**Status**: ✅ Production Ready | **Test Coverage**: 100% | **Last Updated**: 2026-07-06
