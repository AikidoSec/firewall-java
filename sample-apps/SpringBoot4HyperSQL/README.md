# SpringBoot 4 + HyperSQL vulnerable sample app

Spring Boot 4 (Spring Framework 7, Jakarta) app on HyperSQL. It registers a `RequestContextListener`
bean, which makes `WebMvcAutoConfiguration` skip the auto `RequestContextFilter`. Zen must then create
the Spring MVC context from `FrameworkServlet#processRequest` instead of the filter.

- Inserting a malicious dog : `Malicious Pet', 'Gru from the Minions') -- `
