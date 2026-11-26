# AKIDS-A3-PROJECTS-001 v0.1.0 — Private offline editable projects

- Stage: A3 implementation
- Status: IMPLEMENTED IN SOURCE; Android runtime verification NOT RUN
- Storage: app-private internal files directory; no external storage, network, cloud, account, or telemetry

## Scope

Aki Studio can now save and manage editable local projects. Each project contains:
- a private byte-for-byte copy of the selected original image;
- a versioned properties manifest containing the current deterministic edit recipe;
- an optional PNG foreground-protection mask when background replacement is enabled.

The original selected file is never overwritten. The private project copy remains available even if the original document-provider URI later becomes unavailable.

## Edit recipe currently persisted

- rotation;
- monochrome toggle;
- crop ratio and horizontal/vertical alignment;
- output dimensions and resize bounds;
- crop/resize label;
- background replacement enabled state and selected solid colour;
- head-height and eye-line guide values and guide visibility.

The flattened foreground mask is persisted as PNG. Individual mask stroke history is not yet preserved as editable vector strokes; reopening restores the flattened mask for continued mask editing and correction.

## Project operations

- Save project with a user-supplied name.
- List and reopen saved projects.
- Duplicate a project, including original image, edit recipe and optional mask.
- Rename a project.
- Delete a project after explicit confirmation. This deletes only Aki Studio's private project copy, not the original image elsewhere on the device.

## Safety and integrity

- Project manifest schema version is explicitly stored as `1`.
- Source image copy is bounded to 100 MB.
- Edit recipe keys and value lengths are bounded.
- Crop ratios, alignments, rotation, dimensions and guide percentages are validated when opening.
- Output and resize pixel limits are checked.
- A project claiming background replacement must contain its mask.
- Project directory identity and parent path are checked before loading/deletion.
- A project source image is decoded for validation before replacing the current editing session.
- Failed project creation removes its incomplete private directory.

## Known limitations

- No thumbnail index, search, tags, autosave, project recovery journal or multi-version history yet.
- Mask stroke-level undo/redo history is not preserved; only the current flattened mask is saved.
- Project manifests use Java Properties; schema migration beyond version 1 is not implemented.
- Large original files above 100 MB are rejected.
- Local project storage is removed if the user clears app data or uninstalls the app; no external backup is created.
- Android build, storage lifecycle, process-death, and device tests have not been run.

This is A3 implementation evidence only. It is not an A4 verification record or release authorization.
