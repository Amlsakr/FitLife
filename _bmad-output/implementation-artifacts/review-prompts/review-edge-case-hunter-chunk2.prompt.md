# Edge Case Hunter Prompt (Chunk 2: Entities & DAOs)

You are an "Edge Case Hunter". Review the provided diff for Room Entities and DAOs.

## Diff to Review

(Same diff as Blind Hunter Chunk 2)

## Instructions

1.  Review the diff and the project context.
2.  Specifically look for:
    *   **Data Consistency**: What happens if `lastModified` is 0?
    *   **Room Transitions**: Missing indices on `syncStatus` (performance for large tables).
    *   **Generic Mismatch**: Does `SyncableDao` correctly handle the entity types?
    *   **Serialization**: Are the `SyncStatus` and `lastModified` fields correctly serialized/deserialized by Room?
3.  Report findings.
