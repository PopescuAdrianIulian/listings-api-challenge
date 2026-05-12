# 🏗️ Listings API — High-Performance Real Estate Search Engine

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-005571?style=flat-square&logo=elasticsearch)

O aplicație Spring Boot de înaltă performanță proiectată pentru gestionarea și interogarea anunțurilor imobiliare la scară mare. Sistemul utilizează o **arhitectură hibridă** (Polyglot Persistence) pentru a procesa peste 1 milion de înregistrări cu latență minimă.

---

## 🎯 Stack Tehnologic

| Componentă    | Tehnologie |
|---------------|---|
| **Limbaj**    | Java 17 |
| **Framework** | Spring Boot 3.5.x |
| **Bază de date**| MySQL 8.x |
| **Search Engine**| Elasticsearch 8.x |
| **Cache**     | Caffeine Cache |
| **Validare**  | Jakarta Validation |
| **Utilități** | Lombok, JPA Projections |

---

## ⚡ Performance Benchmarks (1M Records)

Testat cu `evaluate_1m.py` pe un dataset de **1.000.000 anunțuri**:

### **Listări cu Filtrare Geografică (Cursor-Based)**
| Scenariu | Mean Latency | P95 | Status |
| :--- | :--- | :--- | :--- |
| `1m_listings_tight_bbox` | **203.99ms** | 203.99ms | ⭐ 66% Faster |
| `1m_listings_region_sale` | **137.18ms** | 137.18ms | ⭐ 76% Faster |
| `1m_listings_large_limit` | **31.22ms** | 31.22ms | ⭐ 66% Faster |

### **Clustering (Agregare pură în SQL)**
| Scenariu | Mean Latency | P95 | Status |
| :--- | :--- | :--- | :--- |
| `1m_clusters_heavy_relaxed` | **4.23ms** | 4.23ms | 🔥 INSANELY FAST |
| `1m_clusters_national_relaxed` | **3.00ms** | 3.00ms | 🚀 ULTRA-FAST |

---

## 🏗️ Arhitectură Hibridă: SQL + Elasticsearch

Sistemul alege unealta optimă pentru fiecare tip de interogare:

### **De ce MySQL (SQL)?**
*   **Sursa de Adevăr:** Garantează integritatea datelor și tranzacții ACID.
*   **Clustering Geografic:** Calculele matematice pentru grid-clustering (`GROUP BY` pe coordonate) sunt optimizate direct prin Native SQL (3-5ms).
*   **Keyset Pagination:** Ideal pentru parcurgerea stabilă a milioanelor de rânduri folosind indecși pe ID.

### **De ce Elasticsearch (NoSQL)?**
*   **Full-Text Search:** Căutare ultra-rapidă în câmpuri masive de text (`title`, `description`).
*   **Fuzzy Matching:** Permite găsirea rezultatelor chiar dacă utilizatorul scrie greșit.
*   **Tag Filtering:** Eficiență sporită în filtrarea seturilor de atribute (ex: "quiet", "parking").

---

## 🚀 Optimizări de Performanță

*   ✅ **Grid-based clustering**: Executat direct în MySQL (nu în Java) — evită `OutOfMemoryError`.
*   ✅ **Cursor-based pagination**: Keyset pagination evită `OFFSET + LIMIT` care devine scump la 1M+ rânduri.
*   ✅ **JPA Projections**: Selectează doar coloanele necesare, reducând payload-ul de rețea.
*   ✅ **Caffeine Caching**: Previne recalculări pe parametri identici (TTL 10 min).

---

## 🔌 API Endpoints & Exemple `curl`

### 1. Clustering Geografic
Grupează anunțurile sub formă de clustere (grid-based).
```bash
curl "http://localhost:8080/listings/clusters?min_lat=44.3&max_lat=44.6&min_lon=25.9&max_lon=26.2&max_clusters=10"

# Prima pagină
curl -i "http://localhost:8080/listings?listing_type=sale&limit=50"

# Pagina următoare (folosind cursorul din X-Next-Cursor)
curl "http://localhost:8080/listings?limit=50&after=apt-050"

# Căutare după titlu
curl "http://localhost:8080/search/title?title=apartament"

# Căutare după tag
curl "http://localhost:8080/search/tag?tag=quiet"