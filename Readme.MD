# Zen, in-app firewall for Java | by Aikido
[![Codecov](https://img.shields.io/codecov/c/github/AikidoSec/firewall-java?style=flat-square&token=AJK9LU35GY)](https://app.codecov.io/gh/aikidosec/firewall-java) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Unit tests](https://github.com/AikidoSec/firewall-java/actions/workflows/gradle-tests.yml/badge.svg)](https://github.com/AikidoSec/firewall-java/actions/workflows/gradle-tests.yml)
[![End to end tests](https://github.com/AikidoSec/firewall-java/actions/workflows/end2end.yml/badge.svg)](https://github.com/AikidoSec/firewall-java/actions/workflows/end2end.yml)

Zen, your in-app firewall for peace of mind– at runtime.

Zen is an embedded Web Application Firewall that autonomously protects your Java apps against common and critical attacks.

Zen protects your Java apps by preventing user input containing dangerous strings, which allow SQL injections. It runs on the same server as your Python app for easy installation and zero maintenance.

## Features

Zen will autonomously protect your Python applications from the inside against:

* 🛡️ [NoSQL injection attacks](https://www.aikido.dev/blog/web-application-security-vulnerabilities)
* 🛡️ [SQL injection attacks](https://www.aikido.dev/blog/the-state-of-sql-injections)
* 🛡️ [Command injection attacks](https://www.aikido.dev/blog/command-injection-in-2024-unpacked)
* 🛡️ [Path traversal attacks](https://owasp.org/www-community/attacks/Path_Traversal)
* 🛡️ [Server-side request forgery (SSRF)](./docs/ssrf.md)

Zen operates autonomously on the same server as your Java app to:

* ✅ Secure your app like a classic web application firewall (WAF), but with none of the infrastructure or cost.

## Installation
WIP: Download the zip file [here]() and store it in a directory (e.g. `/opt/aikido-zen`).
Please ensure you do not alter the structure of this folder and that the process can read & write to it.