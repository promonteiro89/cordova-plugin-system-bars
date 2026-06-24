#!/usr/bin/env node
/**
 * Check or set the plugin version across package.json, plugin.xml, the wrapper
 * package.json, and the README install pins.
 *
 *   node scripts/version.mjs          # check that every file agrees
 *   node scripts/version.mjs 1.0.4    # set the version everywhere
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const read = (rel) => readFileSync(resolve(root, rel), 'utf8');
const write = (rel, text) => writeFileSync(resolve(root, rel), text);

const SEMVER = /^\d+\.\d+\.\d+$/;
const PKG_VERSION = /"version":\s*"(\d+\.\d+\.\d+)"/;
const XML_VERSION = /id="com\.outsystemscloud\.systembars"\s+version="(\d+\.\d+\.\d+)"/;

function extract(rel, pattern) {
    const m = read(rel).match(pattern);
    return m ? m[1] : null;
}

const arg = process.argv[2];

if (!arg) {
    // check mode
    const canonical = extract('package.json', PKG_VERSION);
    let ok = true;

    const others = {
        'plugin.xml': extract('plugin.xml', XML_VERSION),
        'packages/outsystems-wrapper/package.json': extract(
            'packages/outsystems-wrapper/package.json',
            PKG_VERSION
        ),
    };
    for (const [file, version] of Object.entries(others)) {
        if (version !== canonical) {
            console.error(`✗ ${file}: ${version} (expected ${canonical})`);
            ok = false;
        }
    }

    const readme = read('README.md');
    const referenced = [
        ...readme.matchAll(/cordova-plugin-system-bars\.git#(\d+\.\d+\.\d+)/g),
        ...readme.matchAll(/"version":\s*"(\d+\.\d+\.\d+)"/g),
    ].map((m) => m[1]);
    for (const version of referenced) {
        if (version !== canonical) {
            console.error(`✗ README.md references ${version} (expected ${canonical})`);
            ok = false;
        }
    }

    if (ok) {
        console.log(`✓ version ${canonical} is in sync across all files`);
        process.exit(0);
    }
    console.error(`\nPass a version to re-sync, e.g. node scripts/version.mjs ${canonical}`);
    process.exit(1);
}

// set mode
const next = arg.replace(/^v/, '');
if (!SEMVER.test(next)) {
    console.error(`Invalid version "${arg}" — expected MAJOR.MINOR.PATCH (e.g. 1.0.3).`);
    process.exit(1);
}

const current = extract('package.json', PKG_VERSION);

const edits = [
    ['package.json', (t) => t.replace(PKG_VERSION, `"version": "${next}"`)],
    ['packages/outsystems-wrapper/package.json', (t) => t.replace(PKG_VERSION, `"version": "${next}"`)],
    ['plugin.xml', (t) => t.replace(/(id="com\.outsystemscloud\.systembars"\s+version=")\d+\.\d+\.\d+(")/, `$1${next}$2`)],
    [
        'README.md',
        (t) =>
            t
                .replace(/(cordova-plugin-system-bars\.git#)\d+\.\d+\.\d+/g, `$1${next}`)
                .replace(/"version":\s*"\d+\.\d+\.\d+"/g, `"version": "${next}"`),
    ],
];

for (const [file, transform] of edits) {
    write(file, transform(read(file)));
}

console.log(`✓ set version ${current} → ${next} across package.json, plugin.xml, wrapper, and README.md`);
console.log('  Review the diff and update CHANGELOG.md before committing.');
