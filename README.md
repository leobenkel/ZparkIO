[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Gitter](https://badges.gitter.im/zparkio/community.svg)](https://gitter.im/zparkio/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![release-badge][]][release]
[![maven-central-badge][]][maven-central-link]

[![Build Status](https://travis-ci.com/leobenkel/zparkio.svg?branch=master)](https://travis-ci.com/leobenkel/zparkio)
[![BCH compliance](https://bettercodehub.com/edge/badge/leobenkel/zparkio?branch=master)](https://bettercodehub.com/)
[![Coverage Status](https://coveralls.io/repos/github/leobenkel/zparkio/badge.svg?branch=master)](https://coveralls.io/github/leobenkel/zparkio?branch=master)
[![Mutation testing badge](https://badge.stryker-mutator.io/github.com/leobenkel/zparkio/master)](https://stryker-mutator.github.io)


[release]:              https://github.com/leobenkel/zparkio/releases
[release-badge]:        https://img.shields.io/github/tag/leobenkel/zparkio.svg?label=version&color=blue
[maven-search]:         https://search.maven.org/search?q=g:com.leobenkel%20a:zparkio
[leobenkel-github-badge]:     https://img.shields.io/badge/-Github-yellowgreen.svg?style=social&logo=GitHub&logoColor=black
[leobenkel-github-link]:      https://github.com/leobenkel
[leobenkel-linkedin-badge]:     https://img.shields.io/badge/-Linkedin-yellowgreen.svg?style=social&logo=LinkedIn&logoColor=black
[leobenkel-linkedin-link]:      https://linkedin.com/in/leobenkel
[leobenkel-personal-badge]:     https://img.shields.io/badge/-Website-yellowgreen.svg?style=social&logo=data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9JzMwMHB4JyB3aWR0aD0nMzAwcHgnICBmaWxsPSIjMDAwMDAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHg9IjBweCIgeT0iMHB4IiB2aWV3Qm94PSIwIDAgNjQgNjQiIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDY0IDY0IiB4bWw6c3BhY2U9InByZXNlcnZlIj48Zz48Zz48cGF0aCBkPSJNNDEuNiwyNy4yYy04LjMsMC0xNSw2LjctMTUsMTVzNi43LDE1LDE1LDE1YzguMywwLDE1LTYuNywxNS0xNVM0OS45LDI3LjIsNDEuNiwyNy4yeiBNNTEuNSwzNmgtMy4zICAgIGMtMC42LTEuNy0xLjQtMy4zLTIuNC00LjZDNDguMiwzMi4yLDUwLjIsMzMuOSw1MS41LDM2eiBNNDEuNiwzMS41YzEuMywxLjIsMi4zLDIuNywzLDQuNGgtNkMzOS4zLDM0LjIsNDAuNCwzMi43LDQxLjYsMzEuNXogICAgIE0zNy40LDMxLjNjLTEsMS40LTEuOCwyLjktMi40LDQuNmgtMy4zQzMzLjEsMzMuOSwzNS4xLDMyLjIsMzcuNCwzMS4zeiBNMzAuMyw0NWMtMC4yLTAuOS0wLjQtMS44LTAuNC0yLjhjMC0xLDAuMS0yLDAuNC0yLjkgICAgaDMuOWMtMC4xLDEtMC4yLDEuOS0wLjIsMi45YzAsMC45LDAuMSwxLjksMC4yLDIuOEgzMC4zeiBNMzEuNyw0OC4zSDM1YzAuNiwxLjcsMS40LDMuNCwyLjQsNC44QzM1LDUyLjIsMzMsNTAuNSwzMS43LDQ4LjN6ICAgICBNNDEuNiw1Mi45Yy0xLjMtMS4yLTIuMy0yLjgtMy4xLTQuNWg2LjFDNDQsNTAuMSw0Mi45LDUxLjcsNDEuNiw1Mi45eiBNMzcuNiw0NWMtMC4yLTAuOS0wLjItMS44LTAuMi0yLjhjMC0xLDAuMS0yLDAuMy0yLjloOCAgICBjMC4yLDAuOSwwLjMsMS45LDAuMywyLjljMCwxLTAuMSwxLjktMC4yLDIuOEgzNy42eiBNNDUuOCw1My4xYzEtMS40LDEuOC0zLDIuNC00LjhoMy4zQzUwLjIsNTAuNSw0OC4yLDUyLjIsNDUuOCw1My4xeiBNNDksNDUgICAgYzAuMS0wLjksMC4yLTEuOCwwLjItMi44YzAtMS0wLjEtMi0wLjItMi45aDMuOWMwLjIsMC45LDAuNCwxLjksMC40LDIuOWMwLDEtMC4xLDEuOS0wLjQsMi44SDQ5eiI+PC9wYXRoPjxwYXRoIGQ9Ik0zNCwyNS45Yy0wLjktMC43LTEuOC0xLjMtMi45LTEuOGMyLTIuMSwzLjItNC45LDMuMi03LjljMC02LjMtNS4xLTExLjQtMTEuNC0xMS40UzExLjYsOS45LDExLjYsMTYuMiAgICBjMCwzLjEsMS4yLDUuOSwzLjIsNy45Yy00LjEsMi02LjgsNS40LTcuMSw5LjRsLTAuMywzLjhjMCwyLDcsMy42LDE1LjYsMy42YzAuMiwwLDAuNSwwLDAuNywwQzI0LjIsMzQuMywyOC4yLDI4LjYsMzQsMjUuOXogICAgIE0yMyw4LjhjNC4xLDAsNy40LDMuMyw3LjQsNy40cy0zLjMsNy40LTcuNCw3LjRzLTcuNC0zLjMtNy40LTcuNFMxOC45LDguOCwyMyw4Ljh6Ij48L3BhdGg+PC9nPjwvZz48L3N2Zz4=&logoColor=black
[leobenkel-personal-link]:      https://leobenkel.com
[maven-central-link]:                             https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio
[maven-central-badge]:          https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio/badge.svg



# Zparkio
Boiler plate framework to use Spark and ZIO together.

The goal of this framework is to blend Spark and ZIO in an easy to use system for data engineers.

Allowing them to use Spark is a new, faster, more reliable way, leveraging ZIO power.
