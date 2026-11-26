# 🚢 Boat Route Service (`ship-service`)

Microservice zur Verwaltung und Abfrage von Schiffsrouten inklusive automatischer Rechnungserstellung über einen angebundenen gRPC-Billing-Service (Team Ship).

## 🏗️ Architektur & Begründung

### Architektur-Überblick

<img width="2449" height="1632" alt="image" src="https://github.com/user-attachments/assets/9cd47520-3c16-4c65-bdfd-04391faef4b2" />


Das System besteht aus zwei entkoppelten Microservices im Kubernetes-Namespace `ship`, die über unterschiedliche Kommunikationsprotokolle miteinander interagieren:

- **Externe Schnittstelle (REST / HTTP):** Clients kommunizieren über die REST-API (`/api/v1/boat-routes`, Port `8080`, nach außen abgesichert via Traefik Ingress mit TLS & Basic Auth) mit dem **Boat Route Service**.
   
- **Persistenz (Spring Data JPA / JDBC):** Der ship-service speichert und verwaltet alle Routendaten in einer externen, ausschließlich aus dem Cluster erreichbaren AWS RDS PostgreSQL-Datenbank (Port 21799).
   
- **Interne Service-to-Service-Kommunikation (gRPC):** Beim Anlegen einer Route ruft der `ship-service` als **gRPC-Client** den eigenständigen **Billing Service** (`billing-service:9001`) auf. Dieser agiert als **gRPC-Server**, erstellt die Rechnung (`CreateInvoice`) und sendet synchron eine strukturierte `InvoiceResponse` (`Bill-ID`, `Owner`, `Fee`, `Status`) zurück.

---

### Klassische Layered Architecture (`Controller` → `Service` → `Repository`)

Für diesen Microservice wurde bewusst eine **klassische Schichtenarchitektur (Layered Architecture)** gewählt:

1. **Pragmatismus & Single Responsibility:** Da unser `ship-service` eine klar abgegrenzte Domäne (CRUD & Suche für Schiffsrouten) abbildet, bietet eine 3-Schichten-Struktur eine optimale Balance zwischen sauberer Trennung der Verantwortlichkeiten und geringem Boilerplate-Code.
   
2. **Klare Trennung von API und Persistenz:** Durch den Einsatz von dedizierten DTOs (`RouteRequestDTO`, `RouteResponseDTO`) und `RouteMapper` bleibt die interne Datenbankstruktur (`Route`-Entity) vollständig vom externen REST-Vertrag entkoppelt.
   
3. **Hohe Wartbarkeit & Onboarding:** Neue Entwickler oder angrenzende Teams finden sich sofort zurecht, da der Datenfluss linear und transparent von der HTTP-Anfrage (`RouteController`) über die Geschäftslogik (`RouteServiceImp`) bis zur Datenbank (`RouteRepository`) verläuft.
   
4. **Isolierte Testbarkeit:** Jede Schicht lässt sich gezielt und unabhängig testen – von schnellen Unit-Tests in der Service- und Mapper-Schicht bis hin zu vollständigen End-to-End-Integrationstests auf Controller-Ebene mit Testcontainers.

---

## 🛠️ Verwendete Technologien

| Kategorie | Technologie / Tool |
| :--- | :--- |
| **Sprache & Runtime** | Java 21 (Eclipse Temurin) |
| **Framework** | Spring Boot 4 (Spring Web, Spring Data JPA, Spring Validation, Spring Boot Actuator) |
| **Kommunikation & Schnittstellen** | RESTful API (extern), **gRPC & Protocol Buffers** (interne Service-to-Service-Kommunikation)
| **API-Dokumentation** | OpenAPI 3 / Swagger UI, RFC 9457 (`ProblemDetail`) |
| **Code-Generierung** | Lombok |
| **Datenbank** | PostgreSQL (Produktiv: AWS RDS, Integrationstests: Testcontainers) |
| **Containerisierung & Orchestrierung** | Docker, Kubernetes (HTW-Cluster), Traefik Ingress (TLS & Basic Auth) |
| **CI/CD** | GitHub Actions (Automatisierte Tests, Docker Image Build & Push, Kubernetes Rollout) |

---

## 💳 Billing Service & gRPC-Kommunikation

### Warum gRPC für die interne Kommunikation?
Während nach außen **REST/JSON** für eine einfache Nutzung durch Web-Clients und Swagger UI eingesetzt wird, kommunizieren `ship-service` und `billing-service` intern über **gRPC (HTTP/2 + Protocol Buffers)**:
* **Strikter Vertrag (`.proto`):** Die Schnittstelle (`BillingService.CreateInvoice`) ist über `billing_service.proto` typsicher definiert – daraus werden Client- und Server-Stubs automatisch generiert.
* **Performance & Effizienz:** Binäre Serialisierung über Protocol Buffers und Multiplexing über HTTP/2 sorgen für minimale Latenz im Kubernetes-Cluster.

### Ablauf & Gebührenlogik (`billing-service`)
Beim Erstellen einer Route (`POST /api/v1/boat-routes`) sendet der `ship-service` synchron eine `InvoiceRequest` (`route_id`, `owner`, `start_location`, `end_location`) an `billing-service:9001`:

Die Erstellung einer Route kostet Geld. Für das Anfahren / Abfahren von Hauptstädten fällt eine Zusätzliche Gebühr an. VIP-Owner genießen ohne Einschränkung vollständig kostenlose Erstellungen von Routen.

* **VIP-Owner (`FreeOwner`):** `0,00 €` (Status: `PAID`)
* **Start & Ziel sind Landeshauptstädte (`StateCapital`):** `30,00 € + Grundgebühr` (Status: `ISSUED`)
* **Start oder Ziel ist eine Landeshauptstadt:** `15,00 € + Grundgebühr` (Status: `ISSUED`)
* **Keine Landeshauptstadt beteiligt:** `0,00 € + Grundgebühr` (Status: `ISSUED`)
* **Grundgebühr** `7,50€`

### Ausfallsicherheit (Resilienz / Graceful Degradation)
Im `BillingServiceGrpcClient` wird eine `StatusRuntimeException` abgefangen. Sollte der `billing-service` im Cluster vorübergehend nicht erreichbar sein, wird die Route dennoch erfolgreich gespeichert und mit dem Fallback-Status `BILLING_UNAVAILABLE` (`Bill-ID: PENDING`) protokolliert.

---

## 🌐 Live-Umgebung & API-Zugang

* **Basis-URL (Cluster / Ingress):** `https://ship.edu.smef.io`
* **REST-Endpunkt:** `https://ship.edu.smef.io/api/v1/boat-routes`
* **Swagger UI / OpenAPI:** `https://ship.edu.smef.io/swagger-ui.html`

> **Hinweis:** Der Zugriff über den Traefik Ingress ist mit **TLS** und **Basic Auth** abgesichert.
