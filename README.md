# AasaMedChem — Inventory & Order Management System

## Project Overview

A full-stack inventory and quotation/order management system built for chemical/medchem products.
Supports multi-unit quantities (g, kg, mL, L, count), INR pricing, and role-based access for
Admin, Seller, and Buyer roles.

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Spring Boot 3.2, Spring Security    |
| Auth       | JWT (JJWT 0.11)                     |
| Database   | Neon PostgreSQL (hosted)            |
| ORM        | Spring Data JPA / Hibernate         |
| Frontend   | Next.js (separate repo)             |
| Deploy     | EC2 (backend) / Vercel (frontend)   |

---

## Database Schema

### users
| Column        | Type           | Notes                        |
|---------------|----------------|------------------------------|
| id            | BIGSERIAL PK   |                              |
| name          | VARCHAR(255)   |                              |
| email         | VARCHAR(255)   | unique login identifier      |
| password_hash | VARCHAR(255)   | BCrypt                       |
| role          | VARCHAR(20)    | ADMIN \| SELLER \| BUYER     |
| is_active     | BOOLEAN        | soft disable                 |
| created_at    | TIMESTAMP      |                              |

### products
| Column        | Type            | Notes                                        |
|---------------|-----------------|----------------------------------------------|
| id            | BIGSERIAL PK    |                                              |
| name          | VARCHAR(255)    |                                              |
| sku           | VARCHAR(100)    | unique                                       |
| description   | TEXT            |                                              |
| category      | VARCHAR(100)    |                                              |
| base_unit     | VARCHAR(10)     | **g** \| **mL** \| **count** (storage unit)  |
| base_price    | NUMERIC(18,4)   | INR per 1 base_unit                          |
| stock_in_base | NUMERIC(18,4)   | always in base_unit                          |
| is_active     | BOOLEAN         | soft delete                                  |
| created_by    | BIGINT FK→users |                                              |

### orders
| Column     | Type          | Notes                                      |
|------------|---------------|--------------------------------------------|
| id         | BIGSERIAL PK  |                                            |
| user_id    | BIGINT FK     |                                            |
| status     | VARCHAR(30)   | PENDING \| CONFIRMED \| DISPATCHED \| CANCELLED |
| total_inr  | NUMERIC(18,2) | sum of all line totals                     |
| notes      | TEXT          |                                            |

### order_items
| Column         | Type          | Notes                          |
|----------------|---------------|--------------------------------|
| id             | BIGSERIAL PK  |                                |
| order_id       | BIGINT FK     | cascade delete                 |
| product_id     | BIGINT FK     |                                |
| ordered_unit   | VARCHAR(10)   | unit buyer chose (g/kg/mL/L/count) |
| ordered_qty    | NUMERIC(18,4) | value as buyer entered         |
| base_qty       | NUMERIC(18,4) | converted to base_unit         |
| unit_price_inr | NUMERIC(18,4) | snapshot of base_price at order time |
| line_total_inr | NUMERIC(18,2) | base_qty × unit_price_inr      |

**Why NUMERIC(18,4)?** Chemical quantities can be very small (e.g. ₹0.0001/g for bulk salts)
or very large (millions of mL). NUMERIC avoids floating-point rounding errors and handles
up to 14 digits before decimal + 4 after — sufficient for any realistic inventory value.

---

## Unit Storage & Conversion Strategy

### Internal storage
- Weight → always **grams (g)**. 1 kg entered → stored as 1000 g.
- Volume → always **millilitres (mL)**. 1 L entered → stored as 1000 mL.
- Count  → stored as-is.

### Why this approach?
One base unit per dimension eliminates the need for on-the-fly conversion when comparing
stock levels, or summing totals. All arithmetic happens in a single unit.

### Conversion table
| User enters | Stored as | Multiply by |
|-------------|-----------|-------------|
| kg          | g         | × 1000      |
| g           | g         | × 1         |
| L           | mL        | × 1000      |
| mL          | mL        | × 1         |
| count       | count     | × 1         |

### Where conversions happen
`UnitConversionService` is the single place for all unit math:
1. **On order placement** (`OrderService.place`) — `toBase()` converts user qty to base before saving `base_qty`.
2. **Price calculation** — `lineTotal()` multiplies `base_qty × base_price` (both in base unit).
3. **Display** — `allowedUnits()` tells the frontend which units the buyer can choose per product.
4. **Price preview** — `GET /api/products/{id}/price-preview?qty=2&unit=kg` lets the frontend show live INR total before placing an order.

### Price storage
`base_price` = INR per **1 base unit**. Always stored at the most granular level:
- Sodium Chloride: ₹0.015 per gram = ₹15/kg
- Ethanol: ₹0.05 per mL = ₹50/L

---

## API Endpoints

### Auth (public)
```
POST /api/auth/login      { email, password }  → { token, email, name, role }
POST /api/auth/register   { name, email, password }  → BUYER role
```

### Health (public)
```
GET /api/health/ping      → { status: "UP" }
GET /api/health/db        → { status, db_time, user_count }
```

### Products (authenticated)
```
GET  /api/products                   → paginated list
GET  /api/products/search?q=sodium   → search by name/sku/category
GET  /api/products/categories        → distinct category list
GET  /api/products/by-category?category=Acids
GET  /api/products/{id}
GET  /api/products/{id}/price-preview?qty=2&unit=kg  → live price calc
POST /api/products           [ADMIN, SELLER]
PUT  /api/products/{id}      [ADMIN, SELLER]
PATCH /api/products/{id}/stock  [ADMIN, SELLER]  { delta: 500 }
DELETE /api/products/{id}    [ADMIN]  (soft delete)
```

### Orders (authenticated)
```
POST  /api/orders              → place order (any role)
GET   /api/orders/mine         → buyer's own orders
GET   /api/orders/mine/{id}
DELETE /api/orders/mine/{id}   → cancel own PENDING order

GET   /api/orders              [ADMIN] all orders, ?status=PENDING
GET   /api/orders/stats        [ADMIN] counts by status
GET   /api/orders/{id}         [ADMIN]
PATCH /api/orders/{id}/status  [ADMIN] { status: "CONFIRMED" }
```

### Admin — User Management
```
GET   /api/admin/users              [ADMIN]
POST  /api/admin/users              [ADMIN] create any role user
PATCH /api/admin/users/{id}/role    [ADMIN] { role: "SELLER" }
PATCH /api/admin/users/{id}/toggle-active  [ADMIN]
```

---

## Role Access Matrix

| Action                      | ADMIN | SELLER | BUYER |
|-----------------------------|:-----:|:------:|:-----:|
| Browse products             | ✅    | ✅     | ✅    |
| Price preview               | ✅    | ✅     | ✅    |
| Create product              | ✅    | ✅     | ❌    |
| Edit product / adjust stock | ✅    | ✅     | ❌    |
| Delete product              | ✅    | ❌     | ❌    |
| Place order                 | ✅    | ✅     | ✅    |
| View own orders             | ✅    | ✅     | ✅    |
| View ALL orders             | ✅    | ❌     | ❌    |
| Update order status         | ✅    | ❌     | ❌    |
| Manage users                | ✅    | ❌     | ❌    |

---

## Local Setup

### Prerequisites
- Java 17+, Maven 3.8+
- A Neon account (free tier works)

### 1. Run migrations in Neon SQL editor
```sql
-- Run V1__init_schema.sql first, then V2__seed_data.sql
```

### 2. Configure `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://hostep-nameless-base-aqoqzxpg-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=neondb_owner
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

### 3. Run
```bash
cd backend
mvn spring-boot:run
```

### 4. Test DB connection
```bash
curl http://localhost:8080/api/health/db
# Expected: { "status": "OK", "user_count": 3 }
```

---

## Test Credentials

| Email             | Password   | Role   |
|-------------------|------------|--------|
| admin@aasa.com    | Admin@123  | ADMIN  |
| seller@aasa.com   | Seller@123 | SELLER |
| buyer@aasa.com    | Buyer@123  | BUYER  |

---

## Deploying to EC2

```bash
mvn clean package -DskipTests
scp target/medchem-0.0.1-SNAPSHOT.jar ec2-user@YOUR_EC2_IP:/home/ec2-user/
ssh ec2-user@YOUR_EC2_IP
java -jar medchem-0.0.1-SNAPSHOT.jar \
  --spring.datasource.password=YOUR_PASS \
  --jwt.secret=YOUR_SECRET
```
Open port 8080 in your EC2 security group.

---

## Git Commit Strategy

```
feat: initial schema migration (V1)
feat: auth — JWT login/register, 3 roles
feat: product CRUD with unit validation
feat: order placement with unit conversion
feat: admin user management
feat: price-preview endpoint
fix: stock deduction on order placement
feat: seed data (V2)
```