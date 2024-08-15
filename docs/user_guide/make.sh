#!/bin/bash

source venv/bin/activate
sphinx-build -M html . _build
deactivate
