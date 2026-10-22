# Outbound domain blocking caveat

Java's `InetAddress.getByName()` handles both DNS lookups and parsing IP addresses. For example, Tomcat calls it to check whether an incoming request came through a trusted load balancer or proxy, without opening an outbound connection.

To avoid blocking these checks, Zen for Java skips literal IPv4 and IPv6 addresses when enforcing outbound domain rules. It also omits them from outbound reporting unless it can link them to an HTTP request.

Requests made directly to an IP bypass "block new outbound connections" and explicit IP block entries. Named domains still follow your allow/block rules, and SSRF checks still run.

Other Zen agents report outbound HTTP(S) traffic to literal IPs and block unapproved IP destinations when "block new outbound connections" is enabled. We're working on closing this gap in Java without disrupting inbound IP parsing.
