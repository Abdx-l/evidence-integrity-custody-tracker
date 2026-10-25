# Evidence Integrity and Chain-of-Custody Tracker

JavaFX evidence intake and custody-event UI backed by SQLite, streamed SHA-256 fingerprints, deterministic hash-linked event payloads, and tamper-detection tests. Chain validation detects changed links; external anchors are needed to detect complete history replacement or tail truncation.

## Run

Java 17 and Maven: `mvn test`, then `mvn javafx:run`. Select a file, record intake and transfers, verify its digest, or validate the custody chain.
