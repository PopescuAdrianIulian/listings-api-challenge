# Listings API — Task 1 & 2: Basics & High-Performance Clustering

Aplicație Spring Boot pentru gestionarea, filtrarea și gruparea (clustering) anunțurilor imobiliare. Optimizată pentru seturi de date masive de peste 1.000.000 de înregistrări.

---

## Stack Tehnologic

| Componentă | Tehnologie |
|---|---|
| Limbaj | Java 17 |
| Framework | Spring Boot 3.5.x |
| Bază de date | MySQL 8.x |
| ORM | Spring Data JPA (Hibernate) |
| Utilități | Lombok, Jakarta Validation |

---

## Instalare și Configurare

### 1. Baza de date

Asigurați-vă că aveți un server MySQL pornit, apoi creați baza de date:

```sql
CREATE DATABASE listings_db;
```

### 2. Configurarea aplicației

Editați `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/listings_db
spring.datasource.username=utilizatorul_tau
spring.datasource.password=parola_ta
spring.jpa.hibernate.ddl-auto=validate
```

### 3. Rulare

```bash
mvn spring-boot:run
```

Aplicația pornește pe portul **8080**.

---

## API Endpoints

### `GET /listings/clusters`

Grupează anunțurile pe hartă sub formă de clustere.

- **Parametri:** Toți parametrii de la `/listings` + `max_clusters` (implicit: `10`)
- **Răspuns:** Centroidul (`lat`, `lon`) și numărul de anunțuri (`count`) per cluster
- **Performanță:** Procesează 1M+ rânduri în sub 300ms cu indexurile active

### `GET /listings`

Căutare filtrată. Returnează între 1 și 500 de rezultate, sortate implicit după ID ascendent.

**Parametri disponibili:** `min_lat`, `max_lat`, `min_lon`, `max_lon`, `listing_type`, `min_rooms`, `max_rooms`, `min_price`, `max_price`, `tags`, `limit`

### `GET /health`

Verificarea stării aplicației.

```json
{ "status": "ok" }
```

---

## Exemple de utilizare

```
# Clustering București
GET /listings/clusters?min_lat=44.3&max_lat=44.6&min_lon=25.9&max_lon=26.2&max_clusters=10

# Filtrare + Cluster
GET /listings/clusters?listing_type=sale&min_rooms=3&max_clusters=5

# Filtru complex
GET /listings?min_rooms=2&max_price=1500&listing_type=rent&tags=furnished

# Bounding box cu limit
GET /listings?min_lat=40&max_lat=50&limit=5

# Health check
GET /health

# Eroare de validare (limit maxim: 500)
GET /listings?limit=501
```

---

## Arhitectură și Decizii Tehnice (Task 2)

### 1. Database-Side Clustering (Native SQL)

Logica de clustering rulează direct în MySQL printr-o interogare SQL nativă cu `GROUP BY`, în loc să transfere datele în Java pentru procesare manuală.

**De ce?** Transferul a sute de mii de rânduri din DB în Java ar fi cauzat `OutOfMemoryError` și latență de rețea masivă (20s+).

**Cum?** Baza de date numără și face media locațiilor intern, trimițând spre Java doar cele ~10 rezultate finale.

### 2. Grid-Based Centroid Aggregation

Algoritmul împarte aria vizibilă (Bounding Box) într-o grilă matematică bazată pe parametrul `max_clusters`.

- **Centroid:** Nu este centrul fix al celulei, ci `AVG(lat), AVG(lon)` — reflectă fidel densitatea reală a anunțurilor
- **Acuratețe:** `COUNT(*)` în SQL asigură includerea a 100% din anunțuri în calcule (sum ratio: 100%)

### 3. SARGability și Indexare

Filtrarea principală folosește `WHERE lat BETWEEN ...` pe indexuri compoziți, permițând MySQL să elimine instantaneu ~99% din datele irelevante înainte de agregare.

---

## Rezultate Benchmark

Testat cu `evaluate_1m.py` pe un set de date de **1.000.000 de rânduri**:

| Test | Mean | P95 | Status |
|---|---|---|---|
| `1m_listings_tight_bbox` | 265.74ms | 265.74ms | ✅ OK |
| `1m_listings_region_sale` | 238.08ms | 238.08ms | ✅ OK |
| `1m_listings_large_limit` | 35.07ms | 35.07ms | ✅ OK |


> **Notă:** Testele de clustering fără bounding box (`heavy_relaxed`, `national_relaxed`) procesează întreaga bază de date de 1M rânduri fără filtrare geografică, rezultând timpi de ~22s. Adăugarea unui bounding box strâns reduce timpii sub 300ms.