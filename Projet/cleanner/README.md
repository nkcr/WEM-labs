## batchProcessPP

Ce script utilise `processLine.py` pour purifier le contenu des documents. 
Lancer avec `go run batchProcessPP.go`. Ce script va lancer `processLine.py`
qui doit être dans le même dossier.

## ProcessLine

Example of `processLine.py` usage:

```
$ echo "Monsieur le président à mangé sa chausette en conduisant son volant." | python3 textProcessing.py > test.txt
```

will wite to *stdin*:

`monsieur président mang chauset condui son vol`

