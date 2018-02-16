#!/usr/bin/env node
'use strict';
var spawn = require('child_process').spawn;
var path = require('path');
var os = require('os');
var fs = require('fs');

var isWindows = /^win/.test(process.platform);
var bin = isWindows ? 'lumo.cmd' : 'lumo';
var args = [
  '--classpath', path.join(__dirname, '..', 'src'),
  '--cache', path.join(os.homedir(), '.lumo_cache'),
  '-m', 'closh.main',
];

// NODE_PATH seems to be missing when running as global binary
var paths = [
  path.join(__dirname, '..', 'node_modules'),
  path.join(__dirname, '..', '..')
];

if (process.env.NODE_PATH) {
  paths.push(process.env.NODE_PATH);
}

process.env.NODE_PATH = paths.join(isWindows ? ';' : ':');
process.env.GCU_SOURCES_PATH = path.join(__dirname, '..');
spawn(bin, args, { stdio: 'inherit' }).on('exit', process.exit);
