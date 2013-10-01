#!/bin/bash
# Generates a metric craptonne of json
find data/heightData -maxdepth 1 -mindepth 1 -type d -exec node generateHeightmap.js {} \;
