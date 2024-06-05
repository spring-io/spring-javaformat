{
  "files": [
    {
      "aql": {
        "items.find": {
          "$and": [
            {
              "@build.name": "${buildName}",
              "@build.number": "${buildNumber}",
              "name": {
                "$nmatch": "*.zip"
              },
              "name": {
                "$nmatch": "*.zip.asc"
              }
            }
          ]
        }
      },
      "target": "nexus/"
    }
  ]
}
