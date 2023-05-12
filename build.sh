#!/bin/bash

if [[ $(pwd) != *"/MineScript" ]]; then
  echo "This script should be executed within the 'MineScript' directory."
  exit 1
fi

cd src/main/interpreter/antlr || exit 1
antlr4 -visitor -no-listener MineScript.g4 || exit 1

sed -i '1s/^/package interpreter.antlr;\n\n /' MineScript*.java || exit 1